# Tour Planner – WIP Protocol

## Architektur & Aktueller Stand

- **Backend**: Spring Boot 4 mit Schichten `Controller → Service → Repository`. Domain-Modelle `TourEntity` und `TourLogEntity` sind via JPA/PostgreSQL abgebildet (inkl. Seed-Daten & UUIDs). Ein globaler Exception-Handler sowie Log4j2 sind aktiv, CORS erlaubt das Angular-Frontend. Tests (`TourServiceTest`) verwenden H2, so dass die Grundfunktionen (Tour/Log CRUD) automatisch geprüft werden.
- **Frontend**: Standalone-Angular (v17) mit klarer MVVM-Trennung. Die ViewModel-Schicht (`TourStore`, `TourFormViewModel`, `TourLogFormViewModel`) kapselt State, Formularlogik und REST-Aufrufe. Die View (`DashboardPage`) bindet ausschließlich an Signale & FormControls. Ein `UiCardComponent` fungiert als erstes wiederverwendbares UI-Element.
- **Kommunikation**: HTTP/JSON über `/api/tours` Endpunkte. Konfiguration (DB, Ports) erfolgt über `.env` Variablen bzw. Docker Compose für PostgreSQL.

## UX Snapshot & Wireframe

Die aktuelle Oberfläche liefert bereits eine adaptive Zweispalten-Struktur: links die Tour-Liste, rechts Detailbereich + Formular/Logs. Nachfolgend ein textuelles Wireframe der Zielansicht (mobile Breakpoints auf einspaltig):

```
┌──────────────────────┬────────────────────────────────────────────┐
│ Sidebar              │ Content                                   │
│ ┌──────────────┐     │ ┌─────────────── Tour Header ───────────┐ │
│ │ Tourliste    │     │ │ Details / Editor (Map-Placeholder)    │ │
│ │ + Create Btn │     │ └───────────────────────────────────────┘ │
│ └──────────────┘     │ ┌────────────── Logs + Actions ─────────┐ │
│ Responsive Stack ↓   │ │ Logliste + Formular zum Erfassen      │ │
└──────────────────────┴────────────────────────────────────────────┘
```

## Nächste Schritte für Intermediate-Abgabe

- **Backend**: Benutzerkonzept & Auth aufbauen, Datei-Speicher für Bilder anbinden, OpenRouteService + Leaflet-Route-Caching, Such-/Filter-Endpunkte sowie berechnete Attribute (Popularity, Child-Friendliness).
- **Frontend**: Auth-Flow ergänzen, Map-Komponente mit Leaflet implementieren, Validierungsfeedback (Inline-Errors) verbessern, Suche/Filter + Responsive Navigation finalisieren, Import/Export-UI.
- **Testing & Docs**: Angular-Komponententests, mehr Spring-Tests (≥20), UML-Diagramme (Use-Case, Klassen, Sequenz) erstellen, Wireframes finalisieren (Figma/Miro) und Zeitprotokoll ergänzen.

Dieser Stand erfüllt bereits die Grundintegration (UI ↔ Backend, Formularlogik, persistente Daten), bleibt aber bewusst ein WIP, damit die Intermediate-Abgabe noch erweiterbar ist.***
