# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 3.3.4 web app that replaces the legacy Access database `Turni.mdb` used by USL Umbria2 to manage on-call ("reperibilità") shift schedules for the IT/telecom service. Server-rendered Thymeleaf shells + vanilla JS calling a JSON REST API, backed by an embedded H2 file database. Build/runtime uses Java 21, though `pom.xml` sets the compilation target (`java.version`) to 17.

## Commands

- Build: `mvn compile`
- Run: `mvn spring-boot:run` (serves on `:8080`, data file at `./data/reperibilita.mv.db`)
- Run all tests: `mvn test`
- Run a single test class: `mvn test -Dtest=GeneratoreTurniServiceTest`
- Run a single test method: `mvn test -Dtest=GeneratoreTurniServiceTest#continuaIlGiroDalMenoRecente`
- Package: `mvn package` (produces `target/reperibilita-web.jar`)
- One-time import of the legacy Access DB: `mvn spring-boot:run -Dspring-boot.run.arguments=--import.mdb.path=/path/to/turni.mdb`
- H2 console (when app is running): `http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:file:./data/reperibilita;AUTO_SERVER=TRUE`, user `sa`, empty password

## Architecture

### Layering

`web/api` (REST controllers) → `mapper` (entity↔DTO, static methods) → service facades (`scheduling`, `compenso`, `holiday`) → `repository` (Spring Data JPA) → `domain` (JPA entities). `web/PageController` just serves near-empty Thymeleaf shells (`src/main/resources/templates/`); every page loads its data client-side via `fetch()` against `/api/**` (see `static/js/*.js`, with `common.js`'s `Api` helper wrapping fetch/JSON/error handling). There is no separate SPA framework.

### Core domain concept

Both legacy tables `TAB_TURNI` (general on-call) and `TAB_TURNI_FONIA` (telephony on-call) are unified into one `Turno` entity, distinguished by the `Servizio` enum (`REPERIBILITA` / `FONIA`). A `Turno` is uniquely keyed by `(data, tipoTurno, servizio)` — this triple is treated as a "slot" throughout the codebase (see the unique constraint on `Turno` and the upsert logic in `TurnoService.assegna`).

### Deliberately-applied design patterns (see class-level Javadoc for each)

- **Chain of Responsibility** — `scheduling.TurnoValidationChain` links every `TurnoValidator` Spring bean (ordered via `@Order`) into a chain before assigning a shift. Add a new business rule by creating a new `@Component extends AbstractTurnoValidator`; never edit the chain class itself.
- **Decorator** — `compenso.CalcoloCompensoFactory` wraps `CompensoBase` with `MaggiorazioneNotturnaDecorator` then `MaggiorazioneFestivaDecorator` to compute pay for a shift. The factory is the single place that decides decorator composition order.
- **Template Method** — `export.TurnoExporter` defines the export skeleton (header/row/footer); `CsvTurnoExporter` and `IcsTurnoExporter` fill in format-specific syntax.
- **Strategy** — `holiday.HolidayRule` (`FixedHolidayRule`, `EasterRelativeHolidayRule`) computes Italian national holidays; `ItalianHolidayCalendarFactory` assembles them, `HolidayService` lazily persists/caches per-year and never overwrites manually-added custom holidays (`TipoFestivita.PERSONALIZZATA`).
- **Facade** — `TurnoService`, `CompensoService`, `HolidayService` are the sole entry points controllers use; they hide validation chains, related-entity loading, and upsert/caching logic.

### Shift generation (`scheduling.GeneratoreTurniService`)

Generates a full year of shifts by continuing the historical round-robin rather than requiring a hand-picked order: for each `Servizio`, active operators with a shift in the last 12 months are ranked from least-recently-scheduled to most-recently-scheduled, then assigned one per Friday–Thursday week block (matching the historical scheduling pattern). Weekend/holiday days get the "festivo" `TipoTurno`s, weekdays get the "feriale" one. It never overwrites an occupied slot — occupied slots are reported as `giaEsistente` and skipped, so manual assignments are preserved. `/api/turni/genera-anno/anteprima` previews proposals; `/api/turni/genera-anno` persists them.

### Legacy migration (`migration/`)

`MdbImportRunner` (a `CommandLineRunner`, `@Order(2)`, runs after `DataInitializer`) reads the old Access file via Jackcess (`LegacyMdbReader`) and imports it into H2. It only activates when `import.mdb.path` is set and is idempotent — safe to rerun without duplicating data.

### Startup seeding

`config.DataInitializer` (`@Order(1)`) seeds the three historical shift types (matching legacy `TAB_COD_TURNI`: Notturno feriale, Diurno festivo, Notturno festivo) and the current year's Italian holiday calendar on first boot, so the app is usable without importing the legacy database.

### Report/print customization

Text shown on the printable centralino handoff form (`/stampa`) — organization name, signature lines, logo path — is configured under the `report:` key in `application.yml`, not hardcoded, and is injected via `PageController`.