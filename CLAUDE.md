# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AppCoins Wallet for Android - a crypto wallet app (forked from Trust Wallet) supporting Ethereum, AppCoins (APPC), and fiat payments. Package: `com.appcoins.wallet` (release) / `com.appcoins.wallet.dev` (debug).

## Build Commands

```bash
# Full build
./gradlew build

# Build specific variant (flavor + buildType)
./gradlew assembleAptoideDebug     # Aptoide debug
./gradlew assembleGpDebug          # Google Play debug
./gradlew assembleAptoideRelease   # Aptoide Release
./gradlew assembleGpRelease        # Google Play release

# Run unit tests
./gradlew test                             # All tests
./gradlew :app:testGpDebugUnitTest         # App module tests (gp debug)
./gradlew :app:testGpDebugUnitTest --tests "com.appcoins.wallet.SomeTest"  # Single test class

# Run module-specific tests
./gradlew :feature:backup:data:test
./gradlew :core:arch:test

# Clean
./gradlew clean
```

**Build variants**: 2 flavors (`gp`, `aptoide`) x 3 build types (`debug`, `release`, `staging`) = 6 variants. Debug adds `.dev` suffix to applicationId and versionName.

## Build Configuration

- **Min SDK**: 24 | **Target/Compile SDK**: 35 | **Java**: 17 | **Kotlin**: 1.9.23
- **Gradle**: 8.7.0 with Kotlin DSL (`.gradle.kts`)
- **NDK**: 28.2.13676358 (native C++ via CMake)
- Dependencies managed via version catalog: `gradle/libs.versions.toml`
- Convention plugins in `/convention` standardize module configuration (see plugin IDs like `appcoins.android.library`, `appcoins.hilt`, `appcoins.room`)
- Signing keys and API keys configured via `gradle.properties` (placeholder values in repo)

## Module Structure

```
app/                    Main application (entry point, DI, navigation)
convention/             Gradle convention plugins for build standardization
core/
  arch/                 Base ViewModels, ViewState, SideEffect, Async<T>, DataResult
  analytics/            Analytics abstraction layer
  shared-preferences/   SharedPreferences wrapper
  walletServices/       Wallet service layer
  legacy-base/          Legacy base classes
  network/
    base/               OkHttp client, authenticators, core networking
    backend/            Main backend API (Retrofit)
    bds/                BDS (billing) API
    microservices/      Microservices API
    analytics/          Analytics API
    airdrop/            Airdrop API
    zendesk/            Zendesk support API
    flagr/              Feature flags (Flagr)
  utils/
    android-common/     Android utility functions
    jvm-common/         Pure Kotlin utilities
    properties/         Constant properties
feature/
  backup/{data,ui}/     Wallet backup
  change-currency/{data,ui}/  Currency switching
  promo-code/data/      Promotional codes
  support/data/         Customer support
  wallet-info/data/     Wallet information
  vk-pay/               VK Pay integration
legacy/                 Legacy modules (airdrop, billing, gamification, etc.)
ui/
  common/               Shared Compose components
  widgets/              Reusable UI widgets
```

Modules are auto-discovered by `settings.gradle.kts` — any directory with a `build.gradle.kts` is included as a module.

## Architecture

**Clean Architecture + MVVM** with three layers:
- **Presentation**: Fragments/Compose screens + ViewModels with `ViewState` and `SideEffect`
- **Domain**: Use cases, repository interfaces
- **Data**: Repository implementations, Retrofit APIs, Room DAOs, SharedPreferences

### Key Base Classes (`core/arch`)
- `BaseViewModel<S : ViewState, E : SideEffect>` — RxJava-based ViewModel
- `NewBaseViewModel<S : ViewState, E : SideEffect>` — Coroutines Flow-based ViewModel
- `Async<T>` — sealed class for Loading/Success/Fail states
- `SingleStateFragment` — interface for lifecycle-aware state/event collection

### Reactive Patterns
The codebase uses **both RxJava 2 and Kotlin Coroutines**. Legacy code is RxJava-based; newer features use Coroutines + Flow. `kotlin-coroutines-rx2` bridge is available.

### Dependency Injection
**Hilt** (Dagger 2). Entry point is `App.kt` (`@HiltAndroidApp`). Modules use `@InstallIn(SingletonComponent::class)`. Feature modules define their own Hilt modules. Common qualifiers: `@Named("package-name")`, `@ApplicationContext`.

### Navigation
Fragment-based using **AndroidX Navigation Component** with **SafeArgs** for type-safe arguments. `MainActivity` hosts the `NavHostFragment` with bottom navigation.

### Feature Module Pattern
New features follow `feature/{name}/data` + `feature/{name}/ui` split. Data modules contain repositories and API clients; UI modules contain ViewModels and Compose/Fragment screens.

## Key Frameworks & Libraries

- **Networking**: Retrofit + OkHttp + Gson
- **Database**: Room
- **UI**: Mix of Jetpack Compose (Material 3) and XML Views with ViewBinding
- **Images**: Coil (Compose), Glide (legacy XML views)
- **Animations**: Lottie
- **Payments**: Adyen, TrueLayer, VK Pay
- **Blockchain**: Web3j, Kethereum (ERC-681)
- **Analytics**: Firebase Analytics, Sentry, Flurry, AppsFlyer, Indicative
- **Crash Reporting**: Firebase Crashlytics, Sentry
- **Testing**: JUnit 5, Mockito, MockK, Turbine (Flow testing), Espresso (UI)
- **Error Handling**: kotlin-result (`DataResult<T>`)