# Katiba App

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-1.7.3-green.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-lightgrey.svg)]()

## 📖 About Katiba

**Katiba** (Swahili for "Constitution") is a Kotlin Multiplatform mobile application designed to empower Kenyan citizens through accessible civic education. Combining the engaging learning model of Duolingo with the content-rich experience of YouVersion, Katiba makes understanding the Kenyan Constitution intuitive, interactive, and rewarding.

Our mission is to bridge the gap between legal complexity and public understanding, enabling every Kenyan to become a more informed and engaged citizen.

---

## 🎯 Key Features

### 🏠 Home Tab - Daily Learning Hub
- **Clause of the Day**: Featured constitutional clause updated daily
- **AI-Powered Insights**: Intelligent summaries explaining clause significance
- **Civic Educator Videos**: Short, engaging video explanations from professional educators
- **Practical Application**: AI-generated tips on leveraging clauses in daily life

### 📜 Constitution Tab - Digital Reader
- Browse the complete Kenyan Constitution
- Navigate through 18 chapters and clauses seamlessly
- Bible-reader-like experience for formatted constitutional text

### 📚 Learning Plans Tab - Gamified Education
- Bite-sized lessons following the Constitution sequentially
- Gamified learning with XP, streaks, and achievements
- Milestone-based progress tracking with shield-shaped nodes

### 👤 Profile Tab - Your Civic Journey
- Personal statistics (residence, learning streak, achievements)
- Badge gallery with earned and locked achievements
- Activity history and settings access

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version | Purpose |
|-------------|---------|---------|
| **JDK** | 17+ | Java Development Kit |
| **Android Studio** | Latest stable | IDE + Android SDK |
| **Git** | Any | Version control |
| **Xcode** | 15+ | iOS development (macOS only) |
| **CocoaPods** | Latest | iOS dependencies (macOS only) |

### Installation

#### 1. Clone the Repository

```bash
git clone https://github.com/your-org/katiba.git
cd katiba
```

#### 2. Open in Android Studio

1. Launch **Android Studio**
2. Select **File → Open**
3. Navigate to the cloned project folder
4. Select the root directory and click **OK**
5. Wait for Gradle sync to complete

---

## 📱 Running on Android

### Option A: Using Android Studio (Recommended)

1. **Create/Select an Emulator:**
   - Go to **Tools → Device Manager**
   - Create a new device or select an existing one
   - Use API 34 (Android 14) for best compatibility

2. **Run the App:**
   - Select your emulator/device from the toolbar dropdown
   - Click the **Run** button (▶) or press `Shift+F10`

### Option B: Using Command Line

```powershell
# Build debug APK
.\gradlew.bat :composeApp:assembleDebug

# Install on connected device/emulator
.\gradlew.bat :composeApp:installDebug
```

### Option C: Physical Android Device

1. Enable **Developer Options** on your device:
   - Go to **Settings → About Phone**
   - Tap **Build number** 7 times
2. Enable **USB Debugging** in Developer Options
3. Connect device via USB
4. Accept the debugging prompt on your device
5. Select your device in Android Studio and run

---

## 🍎 Running on iOS

> **Note:** iOS development requires macOS with Xcode installed.

### Prerequisites (macOS only)

```bash
# Install Xcode from Mac App Store, then:
xcode-select --install

# Install CocoaPods
sudo gem install cocoapods
```

### Running on iOS Simulator

1. **Open the iOS Project:**
   ```bash
   open iosApp/iosApp.xcworkspace
   ```

2. **Select a Simulator:**
   - Choose an iOS simulator from Xcode's device dropdown
   - Recommended: iPhone 14 Pro or newer

3. **Build and Run:**
   - Press `Cmd+R` or click the **Run** button

### Alternative: Command Line Build

```bash
# Build iOS framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

---

## ✨ Try Key Features

Once the app is running, explore these features:

| Feature | How to Access |
|---------|---------------|
| **Daily Clause** | Open app → Home tab → Tap first card |
| **AI Insights** | Home tab → Tap second card → Swipe for video |
| **Browse Constitution** | Tap "Katiba" tab → Select chapter → Read articles |
| **Learning Path** | Tap "Plans" tab → View your learning journey |
| **Profile & Badges** | Tap "Profile" tab → View statistics and achievements |

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 2.1.0 | Programming language |
| **Compose Multiplatform** | 1.7.3 | Shared UI framework |
| **Kotlin Coroutines** | 1.9.0 | Asynchronous programming |
| **Kotlin Serialization** | 1.7.3 | JSON parsing |
| **Navigation Compose** | 2.8.0-alpha10 | Type-safe navigation |
| **AndroidX Lifecycle** | 2.8.4 | ViewModel & lifecycle |

### Platform Requirements

- **Android**: Min SDK 24 (Android 7.0), Target SDK 34 (Android 14)
- **iOS**: iOS 14+

---

## 📐 Kotlin Coding Conventions

This project follows the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html). Here's how we align:

### Naming Conventions
- **Packages**: Lowercase, no underscores (e.g., `com.katiba.app.ui.home`)
- **Classes/Objects**: UpperCamelCase (e.g., `HomeScreen`, `ClauseRepository`)
- **Functions**: lowerCamelCase (e.g., `getDailyClause`, `navigateToProfile`)
- **Properties**: lowerCamelCase (e.g., `currentStreak`, `isLoading`)

### Source Organization
- **Directory Structure**: Follows package structure (`ui/`, `data/`, `navigation/`)
- **File Naming**: Single class files match class name (e.g., `HomeScreen.kt`)
- **Multiplatform**: Platform-specific files in `androidMain/`, `iosMain/`

### Code Style
- **Indentation**: 4 spaces
- **Max Line Length**: 120 characters
- **Trailing Commas**: Used in multiline declarations
- **Expression Bodies**: Used for simple functions

### Compose Best Practices
- Small, focused composables
- State hoisting for shared state
- `remember` for local state
- `LaunchedEffect` for side effects

---

## 📁 Project Structure

```
katiba/
├── composeApp/                    # Shared KMP module
│   └── src/
│       ├── commonMain/            # Shared code (UI + Business Logic)
│       │   └── kotlin/com/katiba/app/
│       │       ├── App.kt         # Main entry point
│       │       ├── data/          # Models, repositories, API
│       │       └── ui/            # Screens, components, theme
│       ├── androidMain/           # Android-specific code
│       └── iosMain/               # iOS-specific code
├── docs/                          # Documentation
├── gradle/                        # Gradle wrapper & version catalog
├── build.gradle.kts               # Root build configuration
└── settings.gradle.kts            # Project settings
```

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

We welcome contributions! Please see our contributing guidelines:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -m "feat: add new feature"`
4. Push to branch: `git push origin feature/your-feature`
5. Open a Pull Request

For detailed guidelines, see our [documentation](docs/documentation.md).

---

## 📚 Documentation

For comprehensive technical documentation, see:
- [Full Documentation](docs/documentation.md) - Complete technical breakdown
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) - Official style guide

---

## 📞 Contact

- **Issues**: [GitHub Issues](https://github.com/katiba-app/katiba/issues)
- **Discussions**: [GitHub Discussions](https://github.com/katiba-app/katiba/discussions)

---

<div align="center">

**Made with ❤️ for Kenya**

*Empowering citizens through constitutional literacy*

</div>
