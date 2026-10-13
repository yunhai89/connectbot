# Repository Guidelines

ConnectBot is a Gradle Android SSH client (AGP 9.4.1, Kotlin 2.4.20, Java 17, Gradle 9.8.1). Modules: `:app`, `:mosh` (dynamic feature with no code of its own; carries Play-delivered mosh assets), `:translations` (Python/uv tooling only).

## Structure

- App code lives in `app/src/main/java/org/connectbot`: `ui/` Compose screens, `data/` Room + repositories, `di/` Hilt modules, `service/` terminal/session service, `transport/` SSH/Mosh/Telnet, `util/`. Native sources in `app/src/main/cpp`.
- Vendored third-party Java sits outside that package (`app/src/main/java/{com,de,org/apache,org/keyczar,org/openintents}`) and is excluded from Spotless, Kover, and Sonar. Never reformat or refactor it.
- Tests: `app/src/test/kotlin` (JVM/Robolectric), `app/src/androidTest` (instrumentation), shared helpers in `app/src/sharedTest/kotlin` (compiled into unit, flavor, and androidTest source sets).
- Room schemas in `app/schemas`, migrations in `data/migration`. `@Database(version = …)` in `ConnectBotDatabase.kt` drives a custom `generateExportSchema` task that writes the bundled `export_schema.json`; keep the version and schema JSON in sync.
- Product flavors `oss` and `google` pick the crypto provider and mosh delivery (`app/src/oss`, `app/src/google`).

## Commands

- Requires JDK 17 and an Android SDK (README uses `ANDROID_SDK_HOME`; `local.properties` is gitignored).
- Build: `./gradlew :app:assembleOssDebug` (or `google`). The oss flavor downloads pinned mosh binaries from `connectbot/mosh4android` (`gradle/mosh4android.version`) at build time, so it needs network.
- Verification matching CI: `./gradlew check koverXmlReportOssDebug koverXmlReportGoogleDebug -x :app:koverVerify`. CI excludes `koverVerify` because the aggregate has no rules but would compile release variants.
- Single unit test: `./gradlew :app:testOssDebugUnitTest --tests "org.connectbot.SomeTest"` (task is flavor-specific; plain `test` runs every variant).
- Instrumentation: `./gradlew :app:connectedOssDebugAndroidTest` (or `google`); needs an emulator, uses the Hilt runner and Android Test Orchestrator.
- Formatting: `./gradlew spotlessApply` / `spotlessCheck`. Spotless ratchets from `origin/main`, so it only checks files changed against that ref — fetch first. Java under `app/src/main/java/org/connectbot` uses google-java-format; Kotlin uses ktlint 1.8.0 with Compose rules; the `spotless/license-header.txt` header is enforced.
- Room/Kover migration tests follow `app/src/test/resources/robolectric.properties` (sdk 34, `w1000dp-h1200dp-xhdpi`, Hilt test application).

## Conventions

- Declare every dependency and plugin version in `gradle/libs.versions.toml` and reference aliases from build scripts.
- UI strings come from `app/src/main/res/values/strings.xml`, read via `stringResource()`; put an XML comment above each new string — translators and the LLM translator rely on it.
- Translations: `./gradlew :translations:translateStrings` uses a local Ollama (`translategemma:27b`, `OLLAMA_HOST`) and marks new entries `tools:machine_translated="true"`. Remove that attribute from human edits or the automated script may overwrite them (CI warns). Python tests: `cd translations && uv run pytest` (no Ollama needed).
- `TRANSLATIONS_ONLY=1` makes Gradle include only `:translations` (skips `:app`/`:mosh`).
- New project-owned files use the current-year Kenny Root copyright header.
- Mockito is the mocking library; both `org.mockito.Mockito` and `org.mockito.kotlin` styles are in use — match the file you edit instead of refactoring.

## Working Rules

- Never block the main UI thread; dispatch IPC, network, and disk I/O off it. In injected classes/ViewModels use the injected `CoroutineDispatchers`, not hardcoded dispatchers, so tests can control execution.
- Keep edits scoped; do not overwrite unrelated local changes or regenerate schema/translation files unless the change requires it.
- Commit subjects are short and imperative (e.g. `Fix pubkey list item compose lint`); PR expectations are in `CONTRIBUTING.md`.
