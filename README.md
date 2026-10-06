# Dialect — Wasm AI Voice Assistant

[![Project Status](https://img.shields.io/badge/status-active-brightgreen)]()
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84)]()
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

> **Short Pitch / Executive Summary:**
> Dialect is an offline-resilient, hands-free Arabic/English voice assistant for Android that turns a device into a continuously available conversational agent. Speech is captured by a lifecycle-aware **Foreground Service**, reasoning is performed by **Google Gemini** (streamed), and every answer is synthesised into natural speech by a **VITS text-to-speech** model, then played back through **Media3 ExoPlayer** — creating a full *listen → think → speak → listen again* loop that keeps working while the app is backgrounded or the screen is off. The codebase is a production-shaped reference implementation of **Clean Architecture (feature-first) + MVVM**, with Hilt dependency injection, sealed `Resource<T>` state handling, and a strict separation between domain logic, data sources, and presentation.

---

## 📖 Table of Contents

- [Overview & Key Features](#-overview--key-features)
- [System Architecture & Design Patterns](#-system-architecture--design-patterns)
- [Tech Stack](#-tech-stack)
- [Directory Structure](#-directory-structure)
- [Getting Started](#-getting-started)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Environment Variables & Configuration](#environment-variables--configuration)
- [Building & Running](#building--running)
- [Usage & API Documentation](#-usage--api-documentation)
- [Testing](#-testing)
- [Engineering Best Practices](#-engineering-best-practices)
- [License & Contact](#-license--contact)

---

## 🌟 Overview & Key Features

- **Core Functionality:** A full duplex voice conversation loop. The user speaks, the recognised transcript is streamed to Gemini for a contextual reply, the reply is split into sentences, each sentence is converted to speech by a VITS TTS endpoint, and the audio is rendered through ExoPlayer. When playback completes, the recogniser is re-armed automatically — no user interaction is required between turns. The UI is delivered in **Jetpack Compose + Material 3** with three destinations: **Home**, **Robot Speech** (service control), and **Settings** (theme, language).
- **Background Execution & Services:** The conversation runs in a **Foreground Service** (`RecordVoiceLifeCycleService`, a `LifecycleService`) with `foregroundServiceType="microphone|camera|mediaPlayback|dataSync|remoteMessaging"` and a persistent, user-dismissible notification, so the assistant survives screen-off, app-switching, and process pressure. A `START_STICKY` restart policy, a `BootBroadcastReceiver` that restores the service after reboot (state persisted via `ExternalStorage` flags), a `NetworkChangeReceiver` for connectivity transitions, and a `NetworkCheckWorker` (WorkManager) round out the resilience story. Service start/stop is idempotent and centralised in `ManageService`, which checks running foreground state before dispatching intents.
- **Generative AI & Models:** **Google Gemini** (`com.google.ai.client.generativeai`) provides both blocking and *streaming* generation with a seeded multi-turn chat history and a domain-specific system prompt; **VITS** Arabic TTS (`vits-ar-sa-huba-v2`, served over the Hugging Face Inference API) converts text into raw audio bytes. Both are abstracted behind repository interfaces so models can be swapped without touching the domain or UI layers.
- **Audio Processing / DSP:** `ExoPlayerMedia` wraps Media3 ExoPlayer with a `ByteArrayDataSource` for zero-disk, in-memory streaming playback, exposes a `suspendCancellableCoroutine`-based `playerMediaStreamAndWait()` API for sequencing, and configures Android audio effects — **NoiseSuppressor, AcousticEchoCanceler, BassBoost, Equalizer and LoudnessEnhancer** — plus speech-optimised `AudioAttributes`. A `Semaphore(1)` and an explicit listening-state flag guarantee that only one turn (recognise → generate → speak) executes at a time.

Additional capabilities:

- **Arabic-first localisation** with per-app locales (`generateLocaleConfig`), `values-ar` resources, and runtime language switching persisted in **DataStore**.
- **Material 3 dynamic theming** with a dark/light toggle persisted across sessions.
- **Reactive, compile-time-safe state** — every screen consumes `StateFlow<UiState>` / sealed `Resource<T>` via `collectAsState()`, eliminating null-driven UI bugs.
- **Centralised error taxonomy** — `ServerException`, `AiSafetyException`, `ConnectErrorException`, `FailureMsg` and the `safeExecuteCallbackTask` helper convert low-level failures into user-meaningful outcomes.

---

## 🏗 System Architecture & Design Patterns

- **Architectural Style:** Clean Architecture (Feature-First) + MVVM. Each feature owns a `presentation` package; shared, domain-agnostic capabilities are lifted into `core/`, and the cross-cutting `core/features/wasmSpeech` vertical carries the full `domain` → `data` slice for the AI/TTS pipeline.
- **Background Strategy:** Android Foreground Service, lifecycle-aware (`LifecycleService` + `lifecycleScope`), with boot restore, connectivity observers, and idempotent start/stop helpers.
- **Key Design Patterns Applied:** Repository Pattern, Use Cases (single-responsibility, `operator fun invoke`), Dependency Injection (Hilt / Dagger `SingletonComponent`), Sealed States (`Resource<T>`, `UiState`), Strategy (swappable TTS/Gemini sources), Observer (Flow/StateFlow), Template callback (`ICustomPlayerListener`, `IListenerStream`), Singleton (DataStore + `LanguageControls`).
- **Data & Workflow Pipeline:**

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│                            PRESENTATION LAYER                               │
│  MainActivity ── NavHost ──► HomeScreen / RobotSpeechScreen / SettingsScreen │
│                                     ▲  StateFlow<UiState> + Resource<T>      │
│                              WasmSpeechViewModel / SettingViewModel          │
└──────────────────────────────────────────────────────────────────────────────┘
                                     │  Use Cases (Hilt-injected)
┌────────────────────────────────────▼──────────────────────────────────────────┐
│                              DOMAIN LAYER                                    │
│  GeminiTextWasmQueryStreamUseCase ─┐                                         │
│  GeminiTextWasmQueryUseCase        ├─►  emit Resource.Loading / Success /     │
│  GeminiTextStreamUseCase           │      Error / FinalError / Complete      │
│  WasmQueryUseCase / SecondQuery  ──┘                                         │
│  Repositories: GeminiAiRepository · WasmTextToSpeechRepository  (interfaces) │
└──────────────────────────────────────────────────────────────────────────────┘
                                     │  implementations
┌────────────────────────────────────▼──────────────────────────────────────────┐
│                                DATA LAYER                                    │
│  GeminiApiClient (GenerativeModel / Chat, streaming Flow<String>)            │
│  WasmApiRemoteImpl (Retrofit2 + HttpURLConnection retry, ByteArray audio)    │
│  DataStorePreferenceRepository (local persistence)                           │
└──────────────────────────────────────────────────────────────────────────────┘

═══════════════════════ RUNTIME VOICE LOOP (Foreground Service) ═══════════════

  🎤 SpeechRecognizerService          transcript (String)
        │  continuous listening, auto re-arm
        ▼
  GeminiTextWasmQueryStreamUseCase ── Flow<String> sentence fragments
        │  (delimited by END_SYMBOL "###", back-pressure aware, cancellable)
        ▼
  WasmTextToSpeechRepository ──────── ByteArray (VITS audio)
        │
        ▼
  ExoPlayerMedia ──► 🔊 speaker ── onCompletion / onError
        │                                 │
        └──────── restart recognition ◄───┘
```

Layer dependency rule: `presentation → domain ← data`. The domain layer has **no Android or framework imports**, which keeps business rules unit-testable on the JVM.

---

## 🛠 Tech Stack

- **Core Language & OS:** Kotlin 1.9.0, Android SDK (compileSdk / targetSdk **34**, minSdk **21**), Gradle 8.7, AGP 8.6.0, JDK 17, Kotlin Version Catalog (`gradle/libs.versions.toml`).
- **UI:** Jetpack Compose (BOM 2024.04.01), Material 3, Navigation Compose, Material Icons Extended, Edge-to-Edge, per-app locales.
- **Architecture & DI:** Clean Architecture, MVVM, Hilt 2.52 (KAPT), `androidx.hilt:hilt-navigation-compose`, WorkManager, Lifecycle / `LifecycleService`, DataStore Preferences.
- **AI & Media:** Gemini Generative AI SDK 0.2.2, VITS TTS (Hugging Face Inference API) via Retrofit2 2.9.0 + Gson converter, Media3 ExoPlayer 1.4.1 (`exoplayer`, `dash`, `ui`), Android `SpeechRecognizer`, Android audio FX (NoiseSuppressor / AEC / BassBoost / Equalizer / LoudnessEnhancer).

---

## 📂 Directory Structure

```text
.
├── app/
│   ├── build.gradle.kts                       # SDK levels, plugins, BuildConfig key injection
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml            # Permissions, foreground services, receivers
│       │   ├── res/
│       │   │   ├── values/strings.xml         # English resources
│       │   │   └── values-ar/strings.xml      # Arabic resources
│       │   └── java/com/example/wasmapplication/
│       │       ├── MainActivity.kt            # Single-activity host, NavHost, permission gating, locales
│       │       ├── Screens.kt                 # Route definitions (sealed class Screens)
│       │       ├── WasmApplication.kt         # @HiltAndroidApp + WorkManager bootstrap
│       │       ├── broadcasts/                # Broadcasts
│       │       │   ├── BootBroadcastReceiver.kt      # Restore service after reboot
│       │       │   └── NetworkChangeReceiver.kt      # Connectivity transition handling
│       │       ├── core/                      # Shared wrappers, Foreground Service glue, ExoPlayer, SpeechRecognizer
│       │       │   ├── Resource.kt · Either.kt · TestConnection.kt
│       │       │   ├── android_api/
│       │       │   │   ├── media/ExoPlayerMedia.kt              # Media3 + DSP effects playback engine
│       │       │   │   └── speech_recognizer/
│       │       │   │       ├── ISpeechRecognizerServices.kt
│       │       │   │       └── SpeechRecognizerService.kt       # Continuous listening wrapper
│       │       │   ├── components/            # Reusable Compose UI (BasicButton, BottomNavigationBar, NavigationPage)
│       │       │   ├── constant/              # Constants, AppBuildConfig, StorageFlags
│       │       │   ├── data/local/            # DataStorePreferenceRepository
│       │       │   ├── enums/ · error/ · helpers/ · interfaces/ · local/ · local_storage/
│       │       │   ├── Notifications/         # LocalNotification, NotificationControl
│       │       │   ├── verifications/         # VerificationJobs (connectivity)
│       │       │   └── features/wasmSpeech/   # Shared AI vertical (domain + data)
│       │       │       ├── data/
│       │       │       │   ├── remote/        # GeminiApiClient, IWasmApiServices, WasmApiRemote
│       │       │       │   └── repository/    # GeminiAiRepositoryImpl, WasmTextToSpeechRepositoryImpl
│       │       │       └── domain/
│       │       │           ├── repository/    # GeminiAiRepository, WasmTextToSpeechRepository
│       │       │           └── use_case/      # Gemini*UseCase, WasmQueryUseCase
│       │       ├── di/                        # Hilt modules
│       │       │   └── AppModule.kt           # Retrofit, Gemini, repositories, singletons
│       │       ├── features/                  # Feature-first modules (data, domain, presentation)
│       │       │   ├── UiState.kt             # Sealed UI state hierarchy
│       │       │   ├── home/presentation/     # HomeScreen
│       │       │   ├── settings/presentation/ # SettingsScreen, SettingViewModel
│       │       │   └── wasmSpeech/presentation/
│       │       │       ├── RobotSpeechScreen.kt
│       │       │       ├── WasmSpeechViewModel.kt · WasmSpeechState.kt
│       │       │       └── components/
│       │       ├── services/                  # Services
│       │       │   ├── RecordVoiceLifeCycleService.kt  # Foreground voice loop (LifecycleService)
│       │       │   ├── RecordVoiceService.kt           # Foreground voice loop (Service)
│       │       │   ├── MyFirebaseMessagingService.kt   # Push message hook (stub)
│       │       │   └── TestService.kt
│       │       ├── ui/                        # Jetpack Compose Theme & Design System
│       │       │   └── theme/Color.kt · Theme.kt · Type.kt
│       │       └── worker/                    # Worker
│       │           └── NetworkCheckWorker.kt  # Periodic connectivity verification
│       ├── test/java/…/ExampleUnitTest.kt     # JVM unit tests
│       └── androidTest/java/…/ExampleInstrumentedTest.kt
├── gradle/libs.versions.toml                  # Version catalog (single source of truth)
├── local.properties                           # ⚠ Git-ignored — API keys live here
├── .env                                       # ⚠ Git-ignored — local key mirror
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version / Notes |
| --- | --- |
| Android Studio | Koala (2024.1) or newer, with Android Gradle Plugin support |
| JDK | **17** (bundled with recent Android Studio) |
| Android SDK | API **34** (compile/target), platform-tools installed |
| Device / Emulator | Android **5.0 (API 21)** or higher; a physical device is recommended for microphone tests |
| Network | Outbound HTTPS to Gemini and the Hugging Face Inference API |
| API Keys | A **Gemini API key** ([Google AI Studio](https://aistudio.google.com/apikey)) and a **Hugging Face token** with Inference API access |

### Installation & Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/Azzaldeen98/wasm.git
   cd wasm
   ```

2. **Create `local.properties`** in the project root (this file is `.gitignore`d and is created automatically by Android Studio, but you must add the keys):

   ```properties
   # Android SDK location (Android Studio writes this for you)
   sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

   # --- Application secrets (never commit these) ---
   GEMINI_API_KEY=<YOUR_GEMINI_API_KEY>
   WASM_API_KEY=Bearer <YOUR_HUGGINGFACE_TOKEN>
   ```

3. **Open the project** in Android Studio (`File ▸ Open…` → select the root folder) and let Gradle sync finish.

4. **Grant runtime permissions** on first launch — the app requests `RECORD_AUDIO` (required) and `CAMERA` via an in-app dialog; `POST_NOTIFICATIONS` is required on Android 13+ for the foreground-service notification.

> **Note on build variants:** `app/build.gradle.kts` reads `local.properties` at configuration time and emits `BuildConfig.GEMINI_API_KEY` / `BuildConfig.WASM_API_KEY`. If a key is missing, the corresponding `buildConfigField` is skipped rather than failing the build — the AI features will then fail at runtime with an authentication error.

### Environment Variables & Configuration

| Key | Source | Consumed by | Purpose |
| --- | --- | --- | --- |
| `GEMINI_API_KEY` | `local.properties` → `BuildConfig` | `GeminiApiClient` via `AppModule` | Authenticates Google Gemini generation requests |
| `WASM_API_KEY` | `local.properties` → `BuildConfig` | `AppModule`, `WasmApiRemoteImpl` | `Authorization: Bearer …` header for the VITS TTS endpoint |
| `WASM_BASE_URL` | `core/constant/Constants.kt` | Retrofit + `HttpURLConnection` | Base URL of the TTS inference API |
| `END_SYMBOL` | `core/constant/Constants.kt` | Streaming use cases | Token that terminates a streamed response (`###`) |

**Security & secrets hygiene**

- `local.properties` and `.env` are git-ignored; keep real credentials there only.
- Do **not** hardcode credentials in source files — rotate immediately any key that has ever been committed (GitHub secret scanning will flag it), and prefer `BuildConfig`/`secrets-gradle-plugin` injection over literals.
- Release builds should source keys from CI secrets or a signing-time property file, never from the repository.

### Building & Running

**From Android Studio:** select a device/emulator and press **Run ▶** (`app` configuration).

**From the command line:**

```bash
# Debug build
./gradlew :app:assembleDebug

# Install on a connected device / emulator
./gradlew :app:installDebug

# Release APK (requires your own signing config for distribution)
./gradlew :app:assembleRelease

# Clean rebuild
./gradlew clean :app:assembleDebug
```

On Windows, use `gradlew.bat` in place of `./gradlew`. Launch the app from the launcher icon (**Dialect Application**).

---

## 💡 Usage & API Documentation

### Using the application

1. **Home** — entry screen; the permission dialog is presented here on first launch.
2. **Robot Speech** — tap **Start voice chat service**. A foreground notification appears and the device begins listening continuously. Tap **Stop voice chat service** to tear the loop down (the recogniser, player and coroutines are released in `onDestroy`).
3. **Settings** — toggle dark/light appearance and switch language (English/Arabic); preferences are persisted with DataStore and applied immediately via per-app locales.

**Interaction lifecycle:** speech → transcript → Gemini (streamed) → sentence → VITS TTS → audio bytes → ExoPlayer playback → automatic re-listening. Connectivity is validated with `TestConnection.isOnline()` before each request; on failure the recogniser is re-armed and the user is notified.

### API documentation

**1. Gemini — text/chat generation (SDK)**

```kotlin
// domain
suspend fun sendMessageStream(text: String): Flow<String>?
```

Wraps `GenerativeModel`/`Chat` with a seeded conversation, `generationConfig`, `flowOn(Dispatchers.IO)`, `catch` and `onCompletion` mapping into `Resource<T>` emissions.

**2. VITS Text-to-Speech — REST**

```http
POST {WASM_BASE_URL}vits-ar-sa-huba-v2
Authorization: Bearer <WASM_API_KEY>
Content-Type: application/json

{"inputs": "<TEXT_TO_SYNTHESISE>"}
```

```bash
curl -X POST "https://api-inference.huggingface.co/models/wasmdashai/vits-ar-sa-huba-v2" \
  -H "Authorization: Bearer $WASM_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"inputs":"مرحبا بك"}' \
  --output response.audio
```

**Response:** raw audio bytes (streamed directly into `ExoPlayerMedia` via `ByteArrayDataSource` — no temp file is written). **Errors:** non-2xx responses are surfaced as `ServerException` / `HttpException`, retried up to 3 attempts in the `HttpURLConnection` path, and mapped to `Resource.Error` / `Resource.FinalError`.

---

## 🧪 Testing

| Layer | Framework | Command |
| --- | --- | --- |
| Unit tests (JVM) | JUnit 4 | `./gradlew :app:testDebugUnitTest` |
| Instrumented tests | AndroidX JUnit + Espresso | `./gradlew :app:connectedDebugAndroidTest` |
| Compose UI tests | `androidx.compose.ui:ui-test-junit4` | included in `connectedDebugAndroidTest` |
| Everything | — | `./gradlew test connectedAndroidTest` |

Coverage currently ships as scaffolding (`ExampleUnitTest`, `ExampleInstrumentedTest`). Because domain use cases depend only on repository interfaces, they are ideal candidates for JVM tests using a fake/in-memory `GeminiAiRepository` and `WasmTextToSpeechRepository`; ViewModel tests can assert `StateFlow<UiState>` transitions (`Initial → Loading → Success/Error`).

---

## 🛡 Engineering Best Practices

- **Separation of Concerns / SOLID** — Activities and Composables render only; ViewModels orchestrate; use cases express single business rules; repositories abstract data sources. Nothing in `domain/` imports Android or Retrofit types.
- **Dependency Inversion with Hilt** — every collaborator is bound to an interface in `AppModule` (`@Singleton` scope), so data sources are replaceable and test doubles are trivial to introduce.
- **Immutability & exhaustive state handling** — sealed `Resource<T>` (`Success`, `Error`, `FinalError`, `Loading`, `Complete`) and sealed `UiState` force the compiler to handle every branch; UI observes unidirectional `StateFlow`.
- **Structured concurrency** — `lifecycleScope`, `Dispatchers.IO` offloading, cancellable `Flow`s with `catch`/`onCompletion`/`cancellable()`, a `Semaphore(1)` turn-gate, and deterministic resource release in `onDestroy` prevent leaks and interleaved turns.
- **Clean Code & consistency** — version catalog for all dependencies, one `Constants` object, `I*` interfaces for contracts, `Helper`/`ManageService` utilities for cross-cutting behaviour, and commented-out experiments kept out of the hot path.
- **Resilience by design** — connectivity guards, retry loops, `START_STICKY` recovery, boot-time restoration, idempotent service control, and user-visible failure messages instead of silent crashes.
- **Privacy & platform compliance** — foreground-service types are declared explicitly, permissions are requested through the modern Activity Result API with rationale, and audio processing stays on-device until the user initiates a turn.
- **Localisation & accessibility** — English/Arabic resources with pseudo-locale generation enabled for debug builds to expose hard-coded strings early.

---

## 📄 License & Contact

This project is released under the **MIT License** — see [`LICENSE`](LICENSE) for the full text.

**Author:** Azzaldeen Mansour
**Repository:** [https://github.com/Azzaldeen98/wasm](https://github.com/Azzaldeen98/wasm)

Issues and pull requests are welcome. For major changes, please open an issue first to discuss the proposed architecture or API impact.
