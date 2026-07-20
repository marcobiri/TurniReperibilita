# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`reperibilita-web` is a Spring Boot 3 (Java 17) application that manages on-call/standby shift
scheduling (*reperibilità*) for a healthcare IT department (USL Umbria2). It's a modern
replacement for a legacy Microsoft Access database (`Turni.mdb`). Server-rendered Thymeleaf
pages act as thin shells; all data interaction happens client-side via `fetch()` against REST
JSON endpoints — there is no separate SPA framework.

## Commands

```bash
mvn spring-boot:run                      # run the app (http://localhost:8080, redirects to /calendario)
mvn test                                 # run all tests
mvn test -Dtest=GeneratoreTurniServiceTest              # run a single test class
mvn test -Dtest=GeneratoreTurniServiceTest#continuaIlGiroDalMenoRecente  # run a single test method
mvn clean package                        # build the jar (target/reperibilita-web.jar)
```

One-time import from the legacy Access database (idempotent, safe to re-run):

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--import.mdb.path=/percorso/turni.mdb
```

Data persists in an embedded H2 file database at `./data/reperibilita` (gitignored). The H2
console is enabled at `/h2-console`. There is no separate dev/prod profile — `application.yml`
is the only config file.

## Domain model

Two independent shift rotations share the same schema, distinguished by the `Servizio` enum:
- `REPERIBILITA` — general on-call rotation (legacy `TAB_TURNI`)
- `FONIA` — telephony on-call rotation (legacy `TAB_TURNI_FONIA`)

Core entities (`domain/`):
- `Turno` — an operator assigned to a shift type on a date, for one `Servizio`. Uniquely keyed
  on `(data, tipoTurno, servizio)` — this is the "slot" that scheduling logic upserts into.
- `TipoTurno` — a shift type (e.g. "Notturno feriale", "Diurno festivo") with start/end time,
  duration, and whether it's a holiday-only shift.
- `Operatore` — an on-call operator, keyed by `codice` (string), with an `attivo` flag.
- `Festivita` — a calendar date marked as a holiday, either `NAZIONALE`/`RELIGIOSA` (auto-seeded)
  or `PERSONALIZZATA` (user-added).
- `Tariffa` — the current hourly rate config used for compensation calculations.
- `AbbinamentoOperatori` — pairings between operators (legacy carry-over concept).

## Architecture (by package)

- **`domain/`** — JPA entities and enums.
- **`repository/`** — Spring Data JPA repositories.
- **`scheduling/`** — shift assignment logic.
  - `TurnoService` is the facade every controller goes through to read/write `Turno`s; it owns
    the upsert-by-slot logic and runs new/changed assignments through `TurnoValidationChain`.
  - Validation is a **Chain of Responsibility**: each `@Component` implementing `TurnoValidator`
    (via `AbstractTurnoValidator`) is wired into a linked list by `TurnoValidationChain` at
    startup, in `@Order` sequence. Add a new business rule by creating a new validator component
    — never edit `TurnoValidationChain` itself.
  - `GeneratoreTurniService` generates a full year of proposed shifts by continuing the
    round-robin rotation observed in history (least-recently-scheduled operator goes next),
    in Friday–Thursday blocks, per `Servizio`. It never overwrites an occupied slot — existing
    assignments are reported as skipped so manual overrides survive regeneration. Preview via
    `calcolaProposte` (no persistence) vs. commit via `generaEPersisti`.
- **`compenso/`** — compensation calculation.
  - **Decorator pattern**: `CompensoBase` (base hourly rate) is wrapped by
    `MaggiorazioneNotturnaDecorator` (night surcharge) then `MaggiorazioneFestivaDecorator`
    (holiday surcharge). `CalcoloCompensoFactory` is the *only* place the decorator composition
    order is decided — go there to change how surcharges stack.
  - `CompensoService` is the facade controllers use to get compensation rows/totals for a period.
- **`holiday/`** — Italian public holiday calendar.
  - `HolidayRule` implementations: `FixedHolidayRule` (fixed calendar date) and
    `EasterRelativeHolidayRule` (offset from Easter, computed via `EasterCalculator`).
  - `ItalianHolidayCalendarFactory` builds the full national calendar for a year from the rules.
  - `HolidayService` lazily persists a year's calendar on first access via
    `assicuraCalendarioAnno`, and never overwrites existing/custom entries.
- **`export/`** — **Template Method** pattern: `TurnoExporter.esporta()` defines the fixed
  header/rows/footer skeleton; `CsvTurnoExporter` and `IcsTurnoExporter` fill in format-specific
  syntax only.
- **`migration/`** — one-time importer from the legacy Access `.mdb` file (via Jackcess), driven
  by `MdbImportRunner` (`@Order(2)`, runs after `DataInitializer`'s `@Order(1)` seed). Only
  activates when `import.mdb.path` is set; idempotent (checks existence before inserting).
- **`config/DataInitializer`** — seeds the 3 historical shift types and the current year's
  holiday calendar on first boot, so the app works without ever importing the legacy database.
- **`web/api/`** — REST controllers (`@RestController`), one per aggregate, plus
  `GlobalApiExceptionHandler` (`@RestControllerAdvice`, scoped to `web.api`) which translates
  domain exceptions (`TurnoValidationException` → 400, `EntityNotFoundException` → 404,
  `MethodArgumentNotValidException` → 400) into a uniform `ApiError` JSON body.
- **`web/PageController`** — serves the Thymeleaf page shells only; no business data.
- **`dto/` / `mapper/`** — request/response DTOs and static mapper classes between entities and
  DTOs. Controllers never expose entities directly.

## Frontend conventions

Static JS lives in `static/js/`, one file per page (`calendario.js`, `operatori.js`,
`festivita.js`, `report.js`, `stampa.js`), plus a shared `common.js` defining:
- `Api` — a small `fetch()` wrapper (`Api.get/post/put/delete`) that parses JSON and throws an
  `Error` using the API's `messaggio` field on non-2xx responses.
- `mostraErrore(elementId, error)` — shows a transient error message in a page element.
- `formatoData(date)` — formats a JS `Date` as `YYYY-MM-DD` for API calls.

No build step, no bundler, no npm — plain `<script>` includes from Thymeleaf templates.

## Notable conventions

- Code, comments, and domain terms are in Italian (`Turno`, `Operatore`, `Reperibilita`,
  `Servizio`, `Festivita`); keep new code consistent with this.
- Class-level Javadoc on services/facades explains *why*, not *what* — read it before modifying
  a class, and update it if the rationale changes.
- Facades (`TurnoService`, `CompensoService`, `HolidayService`) are the intended entry points for
  controllers; avoid calling repositories directly from `web/api`.