# Tour Planner

Tour Planner ist eine Webanwendung zum Planen, Verwalten und Auswerten von Bike-, Hike-, Running- und Vacation-Touren. Das Projekt besteht aus einem Angular-Frontend und einem Spring-Boot-Backend mit REST-API, JPA/Hibernate, PostgreSQL und Log4j2.

Git Repository: https://github.com/salimsn/swen2-tourplanner.git

## Finaler Funktionsumfang

### Benutzer und Isolation

- Self-Register und Login mit Benutzername/Passwort
- Touren und Tour-Logs werden pro Benutzer getrennt
- Frontend sendet die aktive User-ID über `X-User-Id`
- Backend filtert alle Tour-Operationen nach Owner-ID

### Touren

- Touren anzeigen, erstellen, bearbeiten und löschen
- Attribute: Name, Beschreibung, Start, Ziel, Transporttyp, automatisch berechnete Distanz, automatisch berechnete Zeit, Route, Karten-Geometrie und Bildpfad
- Start, Ziel und optionale Zwischenstopps werden als normale Ortsnamen eingegeben, z. B. `Stephansplatz, Vienna`
- Distanz, Zeit und Route werden je nach Transporttyp über die `RouteService`-Abstraktion und OpenRouteService bestimmt
- OpenRouteService ist per Konfiguration aktivierbar
- Ohne API-Key nutzt die Anwendung einen stabilen Fallback, damit Entwicklung und Tests offline laufen

### Karten und Bilder

- Leaflet-Karte im Angular-Frontend
- Route wird aus `routeGeoJson` als GeoJSON-Linie gerendert
- OpenStreetMap-Tiles werden über Leaflet geladen
- Tourbilder werden als externer Pfad gespeichert, die Bilddatei selbst liegt nicht in der Datenbank

### Tour-Logs

- Logs pro Tour anzeigen, erstellen, bearbeiten und löschen
- Logdaten: Datum/Zeit, Kommentar, Schwierigkeit, Gesamtzeit und Rating
- Die Log-Distanz wird aus der Tour übernommen und nicht manuell eingegeben
- Mehrere Logs pro Tour

### Suche und berechnete Attribute

- Volltextsuche über Tourdaten, Tour-Logs und berechnete Werte
- Berechnete Attribute:
  - Popularity aus Anzahl der Logs
  - Child-Friendliness aus Distanz, Dauer und Log-Schwierigkeiten
  - Achievement Badge als Unique Feature
- Suchbegriffe wie `popular`, `child-friendly`, Log-Kommentare oder Routenorte werden berücksichtigt

### Import und Export

- JSON-Export aller Tourdaten des aktiven Benutzers
- JSON-Import erzeugt neue Touren und Logs für den aktiven Benutzer
- IDs werden beim Import bewusst neu vergeben

### Qualität

- Layered Architecture im Backend: Controller -> Service -> Repository
- MVVM-orientiertes Angular-Frontend mit Store und Form-ViewModels
- Reusable UI-Komponente `UiCardComponent`
- Input-Validierung im Frontend und Backend
- Zentrale Fehlerbehandlung über `GlobalExceptionHandler`
- Log4j2 Logging für Auth, CRUD, Import/Export, Fehler und technische Abläufe
- 29 Backend-Tests plus Angular-App-Tests

## Demo-Login

Beim Start mit leerer Datenbank wird ein Demo-User angelegt:

```text
Benutzername: demo
Passwort: password
```

Du kannst alternativ direkt im Frontend einen neuen Benutzer registrieren.

## Projektstruktur

```text
tour-planner
|-- backend     Spring Boot REST API, JPA, PostgreSQL, Tests
|-- frontend    Angular App mit MVVM, Leaflet, Suche und Import/Export
|-- docs        Protokoll mit Architektur, UML, Wireframes und Testentscheidungen
`-- README.md   Projektübersicht
```

## Konfiguration

Backend-Konfiguration liegt in `backend/src/main/resources/application.yml` und kann über `.env` überschrieben werden. Die lokale `.env` ist in `.gitignore` ausgeschlossen; nur `.env.example` ist als sichere Vorlage für Git gedacht.

Wichtige Variablen:

```text
DB_URL=jdbc:postgresql://localhost:5432/tourplanner
DB_USER=tourplanner
DB_PASSWORD=development
PORT=8080
ORS_ENABLED=false
ORS_API_KEY=
ORS_BASE_URL=https://api.openrouteservice.org
TOUR_IMAGE_BASE_DIRECTORY=./tour-images
DOCKER_COMPOSE_ENABLED=false
```

Für echte OpenRouteService-Abfragen:

```text
ORS_ENABLED=true
ORS_API_KEY=<dein-openrouteservice-api-key>
```

## Lokale Entwicklung

### Backend starten

```powershell
cd backend
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Das Backend läuft standardmäßig auf:

```text
http://localhost:8080
```

### Frontend starten

```powershell
cd frontend
npm install
npm start
```

Das Frontend läuft standardmäßig auf:

```text
http://localhost:4200
```

## REST API

Basis-URL:

```text
http://localhost:8080/api
```

Alle Tour-Endpunkte erwarten den Header:

```text
X-User-Id: <uuid-des-eingeloggten-users>
```

### Auth

| Methode | Pfad | Beschreibung |
| --- | --- | --- |
| `POST` | `/auth/register` | Benutzer registrieren |
| `POST` | `/auth/login` | Benutzer einloggen |

### Touren

| Methode | Pfad | Beschreibung |
| --- | --- | --- |
| `GET` | `/tours` | Eigene Touren laden |
| `GET` | `/tours?search={term}` | Volltextsuche |
| `GET` | `/tours/{id}` | Einzelne eigene Tour laden |
| `POST` | `/tours` | Neue Tour erstellen |
| `PUT` | `/tours/{id}` | Eigene Tour bearbeiten |
| `DELETE` | `/tours/{id}` | Eigene Tour löschen |
| `GET` | `/tours/export` | Eigene Tourdaten exportieren |
| `POST` | `/tours/import` | Tourdaten importieren |

### Tour-Logs

| Methode | Pfad | Beschreibung |
| --- | --- | --- |
| `GET` | `/tours/{id}/logs` | Logs einer eigenen Tour laden |
| `POST` | `/tours/{id}/logs` | Neuen Log erstellen |
| `PUT` | `/tours/{tourId}/logs/{logId}` | Log bearbeiten |
| `DELETE` | `/tours/{tourId}/logs/{logId}` | Log löschen |

## Tests

Backend:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend
npm test
```

Verifizierter Stand:

```text
Backend: 29 Tests, 0 Failures
Frontend: 2 Tests, 0 Failures
Angular build: erfolgreich
```
