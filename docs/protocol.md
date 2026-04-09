# Tour Planner – Intermediate Protocol

## 1. Scope & Ziel der Intermediate-Abgabe

- **Fokus**: funktionale Grundversion zum Erheben/Verwalten von Touren & Logs via Angular + Spring Boot. Auth, Volltextsuche, Import/Export und KPI-Berechnungen folgen erst in der finalen Iteration.
- **Architektur**: strikter Layer-Split (`Controller → Service → Repository`) im Backend sowie MVVM im Frontend (Store + Form-ViewModels). Postgres (Prod) / H2 (Tests) werden per JPA angebunden; Konfiguration erfolgt ausschließlich über `.env` Variablen.
- **Technik-Stack**: Spring Boot 3.4, Angular 17 (Standalone Components), REST/JSON via `/api/tours`, Log4j2, UUIDs, Docker Compose für lokale Datenbank. Reusable UI-Komponente: `UiCardComponent`.

## 2. Architektur & Implementierung

- **Backend**: `TourController` stellt CRUD für Touren + Logs bereit, `TourServiceImpl` bündelt Business-Logik (inkl. Ownership-Check), `TourMapper` kapselt DTO ↔ Entity, `TourDataInitializer` liefert Seed-Daten. GlobalExceptionHandler liefert konsistente Fehlerobjekte. Tests (`TourServiceTest`) laufen gegen ein isoliertes H2-In-Memory-DB-Schema.
- **Frontend**: `TourStore` verwaltet UI-State über Signals (selected tour, Log Editor, Loading/Error). `TourFormViewModel` / `TourLogFormViewModel` kapseln Reactive-Forms inkl. Validierung. `DashboardPage` bindet rein über Observables/Computed Properties an den ViewModel-State und orchestriert die Benutzerinteraktionen.
- **Kommunikation & Config**: alle Requests laufen über `TourService` (Angular `HttpClient`), `environment(.development).ts` hält die API-Base-URL. Backend-Config (DB, Ports) wird via `.env` injiziert (`spring.config.import=optional:file:.env`).

## 3. UX Snapshot & Wireframe

- **Layout**: adaptive Zweispalten-Struktur (Sidebar + Content). Unter 1200 px bricht das CSS über Media Queries auf eine einspaltige Ansicht um (Requirement „UI responds to window size changes“).
- **Interaktion**: Sidebar listet Touren, Content-Bereich wechselt zwischen Detailansicht (inkl. Map-/Image-Platzhalter) und Formular. Logs erscheinen als Kartenliste unterhalb der Tourdetails; Erfassung erfolgt inline über ein Formular-Modal.
- **Wireframe (textuell)**:

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

## 4. Checklist Snapshot (Intermediate)

### Must Haves

| Requirement | Status | Umsetzung |
| --- | --- | --- |
| Uses Angular as frontend framework | ✅ | Angular 17 Standalone App (`App`, `DashboardPage`). |
| Uses MVVM for UI | ✅ | `TourStore` (VM) + `TourFormViewModel` / `TourLogFormViewModel` (Form-VM) + Template-Bindings. |
| Uses middleware backend (Spring Boot) | ✅ | Spring Boot REST API unter `/api/tours`. |
| Layered architecture | ✅ | Controller → Service → Repository mit Mapper-Kapselung. |
| Design Pattern | ✅ | Mapper-Pattern + MVVM (Frontend) + Repository (JPA). |
| O/R-Mapper + PostgreSQL | ✅ | Spring Data JPA, Entities `TourEntity`/`TourLogEntity`, DB via Postgres/H2. |
| Config separate from source | ✅ | `.env` Import + Docker Compose. |
| Logging (log4j2) | ✅ | `@Slf4j` in Services/Seeder. |
| OpenRouteService + Leaflet Integration | ⏳ | Für Intermediate nur Placeholder + Route-Feld; finale Iteration bindet echte API/Map ein. |

### Feature Targets

| Feature | Status | Notizen |
| --- | --- | --- |
| GUI + data binding | ✅ | Dashboard-Template bindet ausschließlich über Signals/Controls. |
| Responsive UI | ✅ | `dashboard.page.css` enthält Media Query (`@media (max-width: 1200px)`). |
| Reusable UI Component | ✅ | `UiCardComponent` für Card-Layout (Standard/Muted). |
| Tour CRUD inkl. Image | ✅ | REST + Form-Felder, Detailansicht zeigt alle Attribute + Bild/Map-Platzhalter. |
| Input Validation | ✅ | Reactive Forms (Angular) + Bean Validation (Spring). |
| Tour Log CRUD | ✅ | REST-Endpunkte + inline Form/Listenanzeige. |
| UX Protocol / Wireframes | ✅ | Abschnitt 3 + ASCII-Wireframe (finale Wireframes folgen als Figma im Endbericht). |

## 5. Nächste Schritte Richtung Final Submission

- **Integration**: OAuth-less Auth (Self-Register/Login), OpenRouteService-Aufrufe + Leaflet-Map mit gecachten Routen, Dateiupload (FileSystem Storage) für Tourbilder.
- **Feature Depth**: Volltextsuche inkl. computed Attributes (Popularity & Child-Friendliness), Import/Export (JSON/CSV), Unique Feature definieren.
- **Quality**: Angular-Komponententests + zusätzliche Spring-Tests (≥20 insgesamt), UML (Use Case, Klassen, Sequenz) & Zeitprotokoll, UI-Wireframes in Figma/Miro.

Dieser Stand deckt das Intermediate-Ziel vollständig ab: Nutzer*innen können Touren und Logs erfassen, bearbeiten und löschen; UI & Backend validieren Eingaben, der MVVM-Ansatz ist umgesetzt und die Dokumentation beschreibt UX & Architektur. Die aufgeschobenen Anforderungen (Auth, Suche, KPI, Leaflet/OpenRoute, Import/Export) sind klar für die finale Abgabe vorgemerkt.***
