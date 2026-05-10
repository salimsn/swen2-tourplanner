package dev.salim.backend.tour.api;

import java.time.Instant;
import java.util.List;

public record TourExportPayload(
    String version,
    Instant exportedAt,
    List<TourExportItem> tours
) {}
