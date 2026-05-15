package dev.salim.backend.route.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.salim.backend.route.config.OpenRouteServiceProperties;
import dev.salim.backend.tour.api.TourRequest;
import dev.salim.backend.tour.domain.TransportType;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenRouteServiceRouteService implements RouteService {

    private static final String FALLBACK_GEOJSON = """
        {"type":"LineString","coordinates":[[16.3738,48.2082],[16.3738,48.2082]]}
        """;

    private final OpenRouteServiceProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RouteResult resolveRoute(TourRequest request) {
        if (!properties.isEnabled() || properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return fallback(request);
        }

        try {
            double[] from = geocode(request.fromLocation());
            double[] to = geocode(request.toLocation());
            List<String> routeStops = normalizedRouteStops(request.routeStops());
            List<double[]> waypoints = routeStops.isEmpty()
                ? parseRouteWaypoints(request.routeWaypoints())
                : geocodeRouteStops(routeStops);
            return directions(request, from, waypoints, routeStops, to);
        } catch (RestClientException | IllegalArgumentException ex) {
            log.warn("OpenRouteService lookup failed for {} -> {}: {}",
                request.fromLocation(),
                request.toLocation(),
                ex.getMessage()
            );
            return fallback(request);
        }
    }

    private RouteResult directions(
        TourRequest request,
        double[] from,
        List<double[]> waypoints,
        List<String> routeStops,
        double[] to
    ) {
        String profile = profileFor(request.transportType());
        URI uri = URI.create(properties.getBaseUrl() + "/v2/directions/" + profile + "/geojson");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", properties.getApiKey());

        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(List.of(from[0], from[1]));
        waypoints.forEach(point -> coordinates.add(List.of(point[0], point[1])));
        coordinates.add(List.of(to[0], to[1]));

        Map<String, Object> body = Map.of("coordinates", coordinates);

        String rawResponse = restTemplate.postForObject(uri, new HttpEntity<>(body, headers), String.class);
        JsonNode response = readJson(rawResponse);
        if (response == null || response.path("features").isEmpty()) {
            throw new IllegalArgumentException("ORS directions response did not contain a route");
        }

        JsonNode feature = response.path("features").get(0);
        JsonNode summary = feature.path("properties").path("summary");
        double fallbackDistanceKm = fallbackDistanceKm(request);
        int fallbackMinutes = fallbackEstimatedMinutes(request, fallbackDistanceKm);
        double distanceKm = Math.round(summary.path("distance").asDouble(fallbackDistanceKm * 1000) / 100.0) / 10.0;
        int estimatedMinutes = Math.max(1, (int) Math.round(summary.path("duration").asDouble(fallbackMinutes * 60.0) / 60.0));
        String routeGeoJson = feature.path("geometry").toString();
        String via = routeStops.isEmpty()
            ? (waypoints.isEmpty() ? "" : " via " + waypoints.size() + " waypoints")
            : " via " + String.join(", ", routeStops);
        String routeInformation = "OpenRouteService " + profile + ": "
            + request.fromLocation() + " -> " + request.toLocation()
            + via;
        return new RouteResult(distanceKm, estimatedMinutes, routeInformation, routeGeoJson);
    }

    private double[] geocode(String location) {
        URI uri = UriComponentsBuilder
            .fromUriString(properties.getBaseUrl() + "/geocode/search")
            .queryParam("api_key", properties.getApiKey())
            .queryParam("text", location)
            .queryParam("size", 1)
            .build()
            .encode()
            .toUri();

        String rawResponse = restTemplate.getForObject(uri, String.class);
        JsonNode response = readJson(rawResponse);
        if (response == null || response.path("features").isEmpty()) {
            throw new IllegalArgumentException("Could not geocode " + location);
        }

        JsonNode coordinates = response.path("features").get(0).path("geometry").path("coordinates");
        return new double[] {coordinates.get(0).asDouble(), coordinates.get(1).asDouble()};
    }

    private String profileFor(TransportType transportType) {
        return switch (transportType) {
            case BIKE -> "cycling-regular";
            case HIKE, RUNNING -> "foot-walking";
            case CAR, TRAIN, PLANE -> "driving-car";
        };
    }

    private RouteResult fallback(TourRequest request) {
        String routeInformation = request.routeInformation() == null || request.routeInformation().isBlank()
            ? "Manual route: " + request.fromLocation() + " -> " + request.toLocation() + fallbackStops(request)
            : request.routeInformation();
        String manualRoute = manualGeoJson(request.routeWaypoints());
        double distanceKm = fallbackDistanceKm(request);
        return new RouteResult(
            distanceKm,
            fallbackEstimatedMinutes(request, distanceKm),
            routeInformation,
            manualRoute == null ? fallbackGeoJson() : manualRoute
        );
    }

    private String fallbackStops(TourRequest request) {
        List<String> stops = normalizedRouteStops(request.routeStops());
        return stops.isEmpty() ? "" : " via " + String.join(", ", stops);
    }

    private String fallbackGeoJson() {
        return FALLBACK_GEOJSON.trim();
    }

    private double fallbackDistanceKm(TourRequest request) {
        if (request.distanceKm() != null && request.distanceKm() > 0) {
            return request.distanceKm();
        }

        List<double[]> points = parseRouteWaypointsLenient(request.routeWaypoints());
        if (points.size() < 2) {
            return 1;
        }

        double meters = 0;
        for (int i = 1; i < points.size(); i++) {
            meters += haversineMeters(points.get(i - 1), points.get(i));
        }
        return Math.max(1, Math.round(meters / 100.0) / 10.0);
    }

    private int fallbackEstimatedMinutes(TourRequest request, double distanceKm) {
        if (request.estimatedTimeMinutes() != null && request.estimatedTimeMinutes() > 0) {
            return request.estimatedTimeMinutes();
        }

        double speedKmh = switch (request.transportType()) {
            case HIKE -> 4.5;
            case RUNNING -> 9.0;
            case BIKE -> 18.0;
            case CAR -> 60.0;
            case TRAIN -> 95.0;
            case PLANE -> 700.0;
        };
        return Math.max(1, (int) Math.round(distanceKm / speedKmh * 60));
    }

    private List<double[]> parseRouteWaypointsLenient(String routeWaypoints) {
        try {
            return parseRouteWaypoints(routeWaypoints);
        } catch (IllegalArgumentException ex) {
            return List.of();
        }
    }

    private double haversineMeters(double[] first, double[] second) {
        double earthRadiusMeters = 6_371_000;
        double firstLat = Math.toRadians(first[1]);
        double secondLat = Math.toRadians(second[1]);
        double deltaLat = Math.toRadians(second[1] - first[1]);
        double deltaLon = Math.toRadians(second[0] - first[0]);
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
            + Math.cos(firstLat) * Math.cos(secondLat) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        return earthRadiusMeters * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private List<double[]> parseRouteWaypoints(String routeWaypoints) {
        if (routeWaypoints == null || routeWaypoints.isBlank()) {
            return List.of();
        }

        List<double[]> points = new ArrayList<>();
        for (String item : routeWaypoints.split(";")) {
            String trimmed = item.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            String[] parts = trimmed.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Route waypoint must be lon,lat");
            }
            points.add(new double[] {
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim())
            });
        }
        return points;
    }

    private List<String> normalizedRouteStops(List<String> routeStops) {
        if (routeStops == null || routeStops.isEmpty()) {
            return List.of();
        }

        return routeStops.stream()
            .map(stop -> stop == null ? "" : stop.trim())
            .filter(stop -> !stop.isBlank())
            .toList();
    }

    private List<double[]> geocodeRouteStops(List<String> routeStops) {
        return routeStops.stream()
            .map(this::geocode)
            .toList();
    }

    private String manualGeoJson(String routeWaypoints) {
        List<double[]> points;
        try {
            points = parseRouteWaypoints(routeWaypoints);
        } catch (IllegalArgumentException ex) {
            log.warn("Manual route waypoints could not be parsed: {}", ex.getMessage());
            return null;
        }
        if (points.size() < 2) {
            return null;
        }

        String coordinates = points.stream()
            .map(point -> "[" + point[0] + "," + point[1] + "]")
            .reduce((left, right) -> left + "," + right)
            .orElse("");
        return "{\"type\":\"LineString\",\"coordinates\":[" + coordinates + "]}";
    }

    private JsonNode readJson(String rawResponse) {
        try {
            return objectMapper.readTree(rawResponse);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not parse ORS response", ex);
        }
    }
}
