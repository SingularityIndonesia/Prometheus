# Prometheus

A Kotlin Multiplatform desktop application built with Compose Multiplatform for model management and cataloguing.

## Features

- **Model Catalogue**: Browse and manage your models
- **Model Creation**: Create new models with an intuitive form interface
- **Desktop-first Experience**: Optimized for desktop use with proper window management

## Tech Stack

- **Kotlin Multiplatform**: Cross-platform development
- **Compose Multiplatform**: Modern UI framework
- **Material 3**: Modern design system
- **Navigation Compose**: Type-safe navigation
- **Lifecycle ViewModel**: State management

## Getting Started

### Prerequisites

- JDK 17 or higher
- Gradle 8.9 or higher

### Running the Application

```bash
# Clone the repository
git clone <repository-url>
cd Prometheus

# Run the desktop application
./gradlew app:run

# Or on Windows
gradlew.bat app:run
```

### Building Distributions

```bash
# Create native distribution packages
./gradlew app:createDistributable

# Create platform-specific packages
./gradlew app:packageDmg      # macOS
./gradlew app:packageMsi      # Windows
./gradlew app:packageDeb      # Linux
```

## Project Structure

```
├── app/                          # Main application module
│   ├── src/
│   │   ├── commonMain/          # Shared code
│   │   └── desktopMain/         # Desktop-specific code
│   └── build.gradle.kts
├── gradle/                       # Gradle configuration
└── build.gradle.kts             # Root build script
```

## Development

The application follows a modular architecture with separate UI components for different features:

- `ModelCatalogue`: Main screen for browsing models
- `CreateModelForm`: Interface for creating new models
- `DesktopScaffold`: Main application layout

## License

[Add your license information here]

## Contributing

[Add contribution guidelines here]
