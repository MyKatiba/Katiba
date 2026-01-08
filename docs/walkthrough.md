# Katiba - Kenyan Constitution Civic Education App

## Summary

A Kotlin Multiplatform (KMP) civic education app for Kenyan citizens to learn about their constitution in an engaging, bite-sized format.

## What Was Built

### Project Structure
- Complete KMP project with Compose Multiplatform for shared UI
- Android and iOS targets configured
- Type-safe navigation using Kotlin Serialization
- Kenyan-themed design system with flag colors and beadwork patterns

### Screens Implemented

#### Home Tab (YouVersion-inspired)
- **HomeScreen**: 3 interactive cards for daily content
- **ClauseDetailScreen**: Focused view of the Clause of the Day
- **AIDescriptionScreen**: Multi-page view with AI explanation + video
- **TipsScreen**: Multi-page view with next steps + daily life tips

#### Constitution Tab (Bible Reader-inspired)
- **ConstitutionScreen**: Chapter listing with 18 chapters
- **ClauseGridScreen**: Article grid navigation per chapter
- **ReadingScreen**: Full reading view with clause numbers

#### Plans Tab (Duolingo-inspired)
- **PlansScreen**: Learning path with milestone nodes, XP rewards

#### Profile Tab
- **ProfileScreen**: User bio, residence, streak, badges, activity
- **SettingsScreen**: Account, notifications, display, about settings

### Data Layer
- Data models for Constitution, Articles, Clauses
- User profile and learning progress models
- Sample repository with 18 chapters of Kenyan Constitution content

## Build Instructions

Since the Gradle wrapper couldn't be downloaded automatically, you have two options:

### Option 1: Open in Android Studio
1. Open the project folder in Android Studio
2. Android Studio will automatically download Gradle and sync the project
3. Run on Android emulator or device

### Option 2: Manual Gradle Setup
1. Install Gradle 8.10+ globally, or
2. Download gradle-wrapper.jar from [Gradle distributions](https://services.gradle.org/distributions/)
3. Place it in `gradle/wrapper/` folder
4. Run: `.\gradlew.bat assembleDebug`

## Next Steps
- PDF parsing for full Constitution text
- Backend API integration (Node.js + SQLite)
- Lesson quiz/interactive content
- Actual video player implementation
- iOS testing via Xcode

## Files Created

```
d:\ProjectDev\katiba\
├── settings.gradle.kts
├── build.gradle.kts
├── gradlew.bat
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
└── composeApp/
    ├── build.gradle.kts
    └── src/
        ├── androidMain/
        │   ├── AndroidManifest.xml
        │   ├── kotlin/.../MainActivity.kt
        │   └── res/values/strings.xml
        ├── commonMain/kotlin/com/katiba/app/
        │   ├── App.kt
        │   ├── data/
        │   │   ├── model/Models.kt
        │   │   └── repository/SampleDataRepository.kt
        │   └── ui/
        │       ├── components/Cards.kt, PageIndicator.kt
        │       ├── constitution/ConstitutionScreen.kt, ClauseGridScreen.kt, ReadingScreen.kt
        │       ├── home/HomeScreen.kt, ClauseDetailScreen.kt, AIDescriptionScreen.kt, TipsScreen.kt
        │       ├── navigation/Routes.kt, BottomNavigation.kt
        │       ├── plans/PlansScreen.kt
        │       ├── profile/ProfileScreen.kt, SettingsScreen.kt
        │       └── theme/Color.kt, Type.kt, Theme.kt
        └── iosMain/kotlin/.../MainViewController.kt
```
