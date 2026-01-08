# Katiba - Comprehensive Project Documentation

> **Version:** 1.0  
> **Last Updated:** December 30, 2025  
> **Platform:** Kotlin Multiplatform Mobile (Android & iOS)

---

## 📑 Table of Contents

1. [Project Overview](#project-overview)
2. [Vision & Mission](#vision--mission)
3. [Key Features](#key-features)
4. [Technical Architecture](#technical-architecture)
5. [Design System](#design-system)
6. [Application Structure](#application-structure)
7. [Data Models](#data-models)
8. [Screen Specifications](#screen-specifications)
9. [Development Setup](#development-setup)
10. [Build & Deployment](#build--deployment)
11. [Future Roadmap](#future-roadmap)
12. [Contributing Guidelines](#contributing-guidelines)

---

## 🎯 Project Overview

**Katiba** (Swahili for "Constitution") is a revolutionary mobile application designed to democratize civic education in Kenya. By combining the engaging gamification principles of Duolingo with the content-rich experience of YouVersion Bible app, Katiba transforms the Kenyan Constitution from an intimidating legal document into an accessible, interactive learning platform.

### Core Objectives

- **Accessibility**: Make constitutional knowledge available to every Kenyan citizen, regardless of their legal background
- **Engagement**: Transform legal education through gamification, bite-sized lessons, and interactive content
- **Empowerment**: Enable citizens to understand their rights, responsibilities, and the legal framework that governs their nation
- **Consistency**: Build learning habits through streaks, rewards, and daily content updates
- **Community**: Foster a community of informed, civically-engaged citizens

---

## 🌟 Vision & Mission

### Vision
To create a Kenya where every citizen understands their constitution, knows their rights, and actively participates in democratic processes through informed civic engagement.

### Mission
Bridge the gap between legal complexity and public understanding by providing an intuitive, engaging, and culturally-relevant platform for constitutional education that meets users where they are in their learning journey.

### Target Audience

- **Primary**: Kenyan citizens aged 16-45 who want to understand their constitutional rights
- **Secondary**: Students studying civics, law, or political science
- **Tertiary**: Civic educators, community leaders, and advocacy organizations

---

## 🎯 Key Features

### 1. Home Tab - Daily Learning Hub

The Home Tab serves as the primary engagement point, featuring three distinct cards that refresh daily:

#### Card 1: Clause of the Day
- **Purpose**: Introduce users to a single constitutional clause each day
- **Content**: Full text of the clause with chapter and article references
- **Interaction**: Single-page focused view for deep reading
- **Actions**: Share, bookmark, and save for later reference

#### Card 2: AI-Powered Insights & Video Education
- **Purpose**: Explain the significance and meaning of the daily clause
- **Content**: 
  - Page 1: AI-generated explanation in plain language
  - Page 2: Professional civic educator video (uploaded daily by admins)
- **Navigation**: Horizontal swipe or tap left/right to switch between pages
- **Features**: Video player with standard playback controls

#### Card 3: Practical Application Tips
- **Purpose**: Connect constitutional knowledge to daily life
- **Content**:
  - Page 1: AI-powered "next steps" - actions citizens can take
  - Page 2: Practical tips for leveraging the clause in real-life situations
- **Navigation**: Multi-page horizontal navigation
- **Value**: Transforms abstract legal concepts into actionable insights

### 2. Constitution Tab - Digital Reader

Inspired by popular Bible reader apps, this tab provides a seamless reading experience:

#### Features
- **Complete Text**: Full Kenyan Constitution from Preamble through all Schedules
- **Chapter Navigation**: 18 chapters organized hierarchically
- **Article & Clause Structure**: Numbered sections for easy reference
- **Vertical Scrolling**: Natural reading flow for long-form content
- **Quick Navigation**: Jump to any chapter, article, or clause
- **Search Function**: (Planned) Find specific terms or concepts
- **Bookmarks**: (Planned) Mark important sections for reference

#### Content Structure
```
Preamble
├── Chapter 1: Sovereignty of the People and Supremacy of the Constitution
├── Chapter 2: The Republic
├── Chapter 3: Citizenship
├── Chapter 4: The Bill of Rights
│   ├── General Provisions
│   ├── Rights and Fundamental Freedoms
│   └── Application of Rights
├── Chapter 5: Land
├── Chapter 6: Leadership and Integrity
├── Chapter 7: Representation of the People
├── Chapter 8: The Legislature
├── Chapter 9: The Executive
├── Chapter 10: Judiciary
├── Chapter 11: Devolved Government
├── Chapter 12: Public Finance
├── Chapter 13: The Public Service
├── Chapter 14: National Security
├── Chapter 15: Commissions and Independent Offices
├── Chapter 16: Amendment of the Constitution
├── Chapter 17: General Provisions
└── Chapter 18: Transitional and Consequential Provisions
Schedules (7 total)
```

### 3. Learning Plans Tab - Gamified Education

Modeled after Duolingo's successful learning path approach:

#### Learning Path Structure
- **Vertical Progress Journey**: Visual representation of learning journey
- **Milestone Nodes**: Shield-shaped nodes representing chapters (culturally relevant)
- **Lesson Modules**: Bite-sized lessons covering constitutional topics sequentially
- **State Indicators**:
  - ✅ Completed (green shield)
  - 🎯 Current (pulsing animation)
  - 🔒 Locked (requires previous completion)

#### Gamification Elements
- **XP Points**: Earn experience points for completing lessons
- **Streak Counter**: Track consecutive days of learning
- **Achievement Badges**: Unlock badges for milestones
- **Progress Bar**: Visual feedback on overall completion
- **Level System**: Progress through beginner → intermediate → advanced

#### Lesson Structure
Each lesson includes:
- Introduction to topic
- Key concepts explanation
- Interactive quiz questions
- Real-world application examples
- Progress checkpoint
- XP reward upon completion

### 4. Profile Tab - Personal Civic Journey

Track your learning progress and civic engagement:

#### Profile Sections

**Bio Card**
- User name and avatar
- Join date
- Email/contact information
- Location details

**Residence Information**
- County selection
- Constituency
- Ward (optional)
- Purpose: Connect learning to local governance structures

**Streak Display**
- Current learning streak (consecutive days)
- Longest streak achieved
- Fire icon animation for active streaks
- Motivation for daily engagement

**Badges Gallery**
- Visual display of earned achievement badges
- Locked badges showing requirements
- Badge categories:
  - Chapter completions
  - Streak milestones
  - Quiz performance
  - Special achievements

**Activity History**
- Recent learning activities
- Lessons completed
- Clauses read
- Videos watched
- Time-stamped activity log

**Settings Access**
- Cog icon in top-right corner
- Access to app configuration

### 5. Settings Screen

Comprehensive app configuration:

#### Settings Categories

**Account Settings**
- Profile information
- Email/phone verification
- Password management
- Account deletion

**Notification Preferences**
- Daily clause reminders
- Streak notifications
- Achievement alerts
- Learning recommendations
- Quiet hours configuration

**Display Preferences**
- Light/Dark theme toggle
- Text size adjustment
- Reading mode preferences
- Accessibility options

**Language Settings**
- English/Swahili toggle
- Additional languages (future)

**About Section**
- App version
- Credits and acknowledgments
- Privacy policy
- Terms of service
- Contact support

---

## 🏗 Technical Architecture

### Technology Stack

#### Core Technologies
- **Language**: Kotlin 2.1.0
- **Platform**: Kotlin Multiplatform Mobile (KMM)
- **UI Framework**: Compose Multiplatform 1.7.3
- **Build System**: Gradle 8.5+ with Kotlin DSL

#### Key Libraries & Dependencies

```toml
[Core]
- Kotlin Coroutines 1.9.0 (asynchronous programming)
- Kotlin Serialization 1.7.3 (JSON parsing)
- Kotlinx DateTime 0.6.1 (date handling)

[UI & Navigation]
- Compose Multiplatform 1.7.3 (shared UI)
- Material 3 (design components)
- Navigation Compose 2.8.0-alpha10 (type-safe navigation)

[Lifecycle & Architecture]
- AndroidX Lifecycle ViewModel 2.8.4
- AndroidX Lifecycle Runtime Compose 2.8.4
- AndroidX Activity Compose 1.9.3

[Platform-Specific]
- Android Min SDK: 24 (Android 7.0)
- Android Target SDK: 34 (Android 14)
- Android Compile SDK: 34
- iOS: iOS 14+
```

### Project Architecture

The app follows Clean Architecture principles with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Compose UI + ViewModels)              │
│  - Screens                              │
│  - Components                           │
│  - Navigation                           │
│  - Theme                                │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│          Domain Layer                   │
│  (Business Logic)                       │
│  - Use Cases                            │
│  - Domain Models                        │
│  - Repository Interfaces                │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│           Data Layer                    │
│  (Data Management)                      │
│  - Repository Implementations           │
│  - Data Sources (Local/Remote)          │
│  - DTOs & Mappers                       │
└─────────────────────────────────────────┘
```

### Kotlin Multiplatform Structure

```
katiba/
├── composeApp/                    # Shared KMP module
│   ├── src/
│   │   ├── commonMain/           # Shared code (UI + Business Logic)
│   │   │   ├── kotlin/
│   │   │   │   └── com/katiba/app/
│   │   │   │       ├── App.kt                    # Main app entry
│   │   │   │       ├── data/
│   │   │   │       │   ├── model/                # Data models
│   │   │   │       │   └── repository/           # Data repositories
│   │   │   │       └── ui/
│   │   │   │           ├── components/           # Reusable UI components
│   │   │   │           ├── constitution/         # Constitution tab screens
│   │   │   │           ├── home/                 # Home tab screens
│   │   │   │           ├── navigation/           # Navigation logic
│   │   │   │           ├── plans/                # Plans tab screens
│   │   │   │           ├── profile/              # Profile tab screens
│   │   │   │           └── theme/                # Design system
│   │   │   └── resources/                        # Shared resources
│   │   ├── androidMain/          # Android-specific code
│   │   │   ├── kotlin/
│   │   │   │   └── com/katiba/app/
│   │   │   │       └── MainActivity.kt
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/
│   │   └── iosMain/              # iOS-specific code
│   │       └── kotlin/
│   │           └── com/katiba/app/
│   │               └── MainViewController.kt
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml        # Dependency versions
│   └── wrapper/
├── build.gradle.kts              # Root build file
└── settings.gradle.kts           # Project settings
```

### Navigation Architecture

The app uses type-safe navigation with Kotlin Serialization:

```kotlin
// Navigation Routes (Serializable)
@Serializable object HomeRoute
@Serializable object ConstitutionRoute
@Serializable object PlansRoute
@Serializable object ProfileRoute
@Serializable data class ClauseDetailRoute(val clauseId: String)
@Serializable data class AIDescriptionRoute(val clauseId: String)
// ... additional routes
```

**Navigation Flow:**
```
Bottom Navigation Bar
├── Home Tab → HomeScreen
│   ├── Card 1 → ClauseDetailScreen
│   ├── Card 2 → AIDescriptionScreen (multi-page)
│   └── Card 3 → TipsScreen (multi-page)
├── Katiba Tab → ConstitutionScreen
│   ├── Chapter Selection → ClauseGridScreen
│   └── Clause Selection → ReadingScreen
├── Plans Tab → PlansScreen
│   └── Lesson Selection → LessonScreen
└── Profile Tab → ProfileScreen
    └── Settings Icon → SettingsScreen
```

---

## 🎨 Design System

### Color Palette

The design system is rooted in Kenyan national identity, drawing directly from the national flag:

#### Primary Colors

| Color | Hex Code | Symbolism | Usage |
|-------|----------|-----------|-------|
| **Kenya Black** | `#000000` | Represents the people of Kenya | Primary text, headers, navigation icons |
| **Kenya Red** | `#BB0000` | Blood shed during independence | Accents, active states, CTAs, error states |
| **Kenya Green** | `#006600` | Natural wealth and fertility | Success states, progress indicators, completion |
| **Kenya White** | `#FFFFFF` | Peace and unity | Backgrounds, cards, inverse text |

#### Extended Palette

| Color | Hex Code | Purpose |
|-------|----------|---------|
| Dark Green | `#004D00` | Hover states, dark mode accents |
| Light Green | `#00CC00` | Highlights, success animations |
| Dark Red | `#8B0000` | Error states, critical actions |
| Light Red | `#FF4444` | Warnings, incomplete states |

#### Beadwork-Inspired Accents

Traditional Kenyan beadwork colors for cultural authenticity:

| Color | Hex Code | Usage |
|-------|----------|-------|
| Bead Brown | `#8B4513` | Borders, decorative elements |
| Bead Orange | `#D2691E` | Badges, achievement highlights |
| Bead Gold | `#DAA520` | Premium features, special rewards |

#### Neutral Colors

| Color | Hex Code | Purpose |
|-------|----------|---------|
| Background | `#F5F5F5` | Main background (light mode) |
| Surface | `#FFFFFF` | Cards, elevated surfaces |
| Surface Variant | `#E8E8E8` | Secondary surfaces, dividers |
| On Surface | `#1A1A1A` | Primary text on surfaces |
| On Surface Variant | `#666666` | Secondary text, metadata |

#### Dark Theme

| Color | Hex Code | Purpose |
|-------|----------|---------|
| Dark Background | `#121212` | Main background (dark mode) |
| Dark Surface | `#1E1E1E` | Cards in dark mode |
| Dark Surface Variant | `#2D2D2D` | Elevated elements |
| On Dark Surface | `#E0E0E0` | Primary text (dark mode) |
| On Dark Surface Variant | `#9E9E9E` | Secondary text (dark mode) |

### Typography

Clear, readable typography suitable for legal text and educational content:

#### Type Scale

| Style | Size | Weight | Line Height | Usage |
|-------|------|--------|-------------|-------|
| Display Large | 57sp | Regular | 64sp | Hero text, special headers |
| Display Medium | 45sp | Regular | 52sp | Section headers |
| Display Small | 36sp | Regular | 44sp | Screen titles |
| Headline Large | 32sp | Regular | 40sp | Card titles |
| Headline Medium | 28sp | Regular | 36sp | Subsection headers |
| Headline Small | 24sp | Regular | 32sp | Component headers |
| Title Large | 22sp | Medium | 28sp | List item titles |
| Title Medium | 16sp | Medium | 24sp | Card subtitles |
| Title Small | 14sp | Medium | 20sp | Labels, captions |
| Body Large | 16sp | Regular | 24sp | Main content text |
| Body Medium | 14sp | Regular | 20sp | Secondary content |
| Body Small | 12sp | Regular | 16sp | Supporting text |
| Label Large | 14sp | Medium | 20sp | Button text |
| Label Medium | 12sp | Medium | 16sp | Form labels |
| Label Small | 11sp | Medium | 16sp | Metadata, timestamps |

### Cultural Design Elements

#### Kenyan Shield Icon
- **Inspiration**: Traditional Maasai warrior shield
- **Usage**: App logo, milestone markers, achievement badges
- **Variations**: Filled, outlined, animated
- **Symbolism**: Protection of rights, strength of law

#### Beadwork Patterns
- **Inspiration**: Traditional Kenyan beadwork jewelry
- **Usage**: Card borders, dividers, decorative accents
- **Colors**: Brown, orange, red, green, gold
- **Implementation**: Repeating pattern borders on cards

#### Spear/Arrow Progress Indicators
- **Inspiration**: Traditional Maasai spear
- **Usage**: Progress bars, loading indicators, navigation arrows
- **Symbolism**: Progress, forward movement, achievement

### Component Library

#### KatibaCard
Reusable card component with cultural elements:
- Rounded corners (12dp)
- Beadwork-inspired border pattern
- Elevation shadow (light mode)
- Border glow (dark mode)
- Ripple effect on tap

#### ShieldBadge
Achievement and milestone indicator:
- Shield-shaped container
- Icon or number inside
- Color coding: gray (locked), green (completed), red (current)
- Pulse animation for active state

#### ProgressSpear
Linear progress indicator:
- Spear-tip shaped indicator
- Green track for progress
- Light gray for remaining
- Animated fill on progress update

#### PageIndicator
Multi-page navigation indicator:
- Horizontal dot array
- Active page: filled Kenya Red circle
- Inactive pages: outlined gray circles
- Smooth transition animation

---

## 📊 Data Models

### Constitution Data Structure

#### Chapter Model
```kotlin
data class Chapter(
    val number: Int,              // Chapter number (1-18)
    val title: String,            // Chapter title
    val articles: List<Article>   // Articles in chapter
)
```

#### Article Model
```kotlin
data class Article(
    val number: Int,              // Article number within chapter
    val title: String,            // Article title
    val clauses: List<Clause>     // Clauses within article
)
```

#### Clause Model
```kotlin
data class Clause(
    val number: String,           // Clause number (e.g., "10.1", "10.2.a")
    val text: String,             // Full clause text
    val subClauses: List<String>  // Sub-clauses if applicable
)
```

### Daily Content Model

```kotlin
data class DailyContent(
    val id: String,                   // Unique identifier
    val date: String,                 // ISO date format (YYYY-MM-DD)
    val clause: Clause,               // Featured clause
    val chapterTitle: String,         // Parent chapter title
    val articleTitle: String,         // Parent article title
    val articleNumber: Int,           // Article number reference
    val aiDescription: String,        // AI-generated explanation
    val videoUrl: String,             // Video file URL/path
    val videoThumbnailUrl: String,    // Video thumbnail
    val educatorName: String,         // Civic educator name
    val nextSteps: List<String>,      // AI-generated next steps
    val tips: List<String>            // Practical application tips
)
```

### User Profile Model

```kotlin
data class UserProfile(
    val id: String,                   // Unique user ID
    val name: String,                 // User display name
    val email: String,                // Email address
    val avatarUrl: String,            // Profile picture URL
    val county: String,               // Kenyan county
    val constituency: String,         // User's constituency
    val ward: String,                 // User's ward
    val joinedDate: String,           // ISO date of account creation
    val streak: Int,                  // Current learning streak
    val longestStreak: Int,           // Longest streak achieved
    val totalLessonsCompleted: Int,   // Total lessons completed
    val badges: List<Badge>           // Earned badges
)
```

### Badge Model

```kotlin
data class Badge(
    val id: String,                   // Badge identifier
    val name: String,                 // Badge name
    val description: String,          // How to earn badge
    val iconUrl: String,              // Badge icon image
    val earnedDate: String?,          // When earned (null if not earned)
    val isEarned: Boolean             // Earned status
)
```

### Learning Progress Model

```kotlin
data class LearningProgress(
    val userId: String,               // User ID reference
    val completedLessons: List<String>, // List of completed lesson IDs
    val currentLessonId: String,      // Current lesson in progress
    val totalXp: Int                  // Total experience points
)
```

### Lesson Model

```kotlin
data class Lesson(
    val id: String,                   // Lesson identifier
    val title: String,                // Lesson title
    val description: String,          // Lesson description
    val chapterNumber: Int,           // Related chapter
    val order: Int,                   // Order in learning path
    val xpReward: Int,                // XP earned on completion
    val isCompleted: Boolean,         // Completion status
    val isLocked: Boolean,            // Access status
    val isCurrent: Boolean            // Currently active
)
```

### Activity Record Model

```kotlin
data class ActivityRecord(
    val id: String,                   // Activity identifier
    val type: ActivityType,           // Activity type enum
    val title: String,                // Activity description
    val date: String,                 // ISO timestamp
    val xpEarned: Int                 // XP earned (if applicable)
)

enum class ActivityType {
    LESSON_COMPLETED,
    BADGE_EARNED,
    STREAK_MILESTONE,
    CHAPTER_COMPLETED,
    DAILY_CLAUSE_READ
}
```

---

## 📱 Screen Specifications

### Home Tab Screens

#### HomeScreen
**Purpose**: Main engagement hub with daily content cards

**Layout**:
- Top app bar: "Katiba" title, notification icon, search icon
- Scrollable card list
- 3 main cards: Clause of Day, AI Description, Tips

**Interactions**:
- Tap card → Navigate to detail screen
- Pull-to-refresh → Load latest content
- Bottom navigation visible

**State Management**:
- Loading state: Skeleton cards
- Error state: Retry button
- Empty state: "No content today" message

---

#### ClauseDetailScreen
**Route**: `ClauseDetailRoute(clauseId: String)`

**Purpose**: Focused view of daily constitutional clause

**Layout**:
- Top app bar: Back button, share icon, bookmark icon
- Chapter and article reference breadcrumb
- Clause number badge
- Full clause text (scrollable)
- Bottom action buttons: Share, Bookmark, Read Full Chapter

**Features**:
- Vertical scroll for long clauses
- Share functionality (system share sheet)
- Bookmark save (local storage)
- Navigation to full chapter in Constitution tab

---

#### AIDescriptionScreen
**Route**: `AIDescriptionRoute(clauseId: String)`

**Purpose**: Multi-page view with AI explanation and educator video

**Layout**:
- Top app bar: Back button, page indicator (1/2 or 2/2)
- Page 1: AI-generated text explanation
- Page 2: Video player with educator commentary

**Navigation**:
- Horizontal swipe gestures
- Tap left edge → Previous page
- Tap right edge → Next page
- Page dots indicator at top

**Video Player Features**:
- Play/pause button
- Seek bar
- Volume control
- Fullscreen option
- Educator name overlay
- Video duration display

---

#### TipsScreen
**Route**: `TipsRoute(clauseId: String)`

**Purpose**: Practical application guidance

**Layout**:
- Top app bar: Back button, page indicator
- Page 1: "Next Steps" - actionable items
- Page 2: "Daily Life Tips" - practical applications

**Content Format**:
- Numbered list for next steps
- Card-based tips with icons
- "Learn More" links (future)

---

### Constitution Tab Screens

#### ConstitutionScreen
**Purpose**: Chapter listing and navigation

**Layout**:
- Top app bar: Search icon, bookmarks icon, settings
- Scrollable chapter list
- Each chapter card shows:
  - Chapter number badge
  - Chapter title
  - Article count
  - Chevron right icon

**Interactions**:
- Tap chapter → Navigate to ClauseGridScreen
- Search → Filter chapters (future)
- Bookmarks → Show saved clauses

---

#### ClauseGridScreen
**Route**: `ClauseGridRoute(chapterNumber: Int)`

**Purpose**: Article navigation within chapter

**Layout**:
- Top app bar: Back button, chapter title
- Grid of article cards (2 columns)
- Each card shows:
  - Article number
  - Article title
  - Clause count

**Interactions**:
- Tap article → Navigate to ReadingScreen

---

#### ReadingScreen
**Route**: `ReadingRoute(chapterNumber: Int, articleNumber: Int)`

**Purpose**: Full-text reading experience

**Layout**:
- Top app bar: Back button, font size, bookmark
- Chapter and article header
- Scrollable clause text
- Clause numbers in left margin
- Bottom navigation: Previous/Next article

**Features**:
- Vertical scroll (natural reading)
- Adjustable font size (3 sizes)
- Bookmark current position
- Share specific clauses
- Search within article (future)

---

### Plans Tab Screens

#### PlansScreen
**Purpose**: Learning path visualization

**Layout**:
- Top app bar: "Learning Path", streak counter
- Scrollable vertical path
- Shield-shaped milestone nodes
- Connecting lines between milestones
- XP progress bar at top

**Milestone States**:
- ✅ Completed: Green shield, checkmark
- 🎯 Current: Red shield, pulsing animation
- 🔒 Locked: Gray shield, lock icon

**Layout Pattern**:
```
    [Shield 1] ← Completed
        |
    [Shield 2] ← Current (pulsing)
        |
    [Shield 3] ← Locked
        |
```

**Interactions**:
- Tap completed/current → Navigate to LessonScreen
- Tap locked → Show unlock requirements tooltip
- Scroll to view entire path

---

#### LessonScreen
**Route**: `LessonRoute(lessonId: String)`

**Purpose**: Interactive lesson content

**Layout**:
- Progress bar at top (e.g., 3/10)
- Lesson content area
- Question cards
- Answer options
- Next button at bottom

**Lesson Flow**:
1. Introduction screen
2. Content explanation
3. Interactive questions
4. Feedback on answers
5. Summary and XP reward

**Question Types**:
- Multiple choice
- True/False
- Fill in the blank
- Matching
- Scenario-based

**Completion**:
- Show XP earned animation
- Update streak
- Unlock next lesson
- Return to PlansScreen

---

### Profile Tab Screens

#### ProfileScreen
**Purpose**: User information and learning statistics

**Layout**:
- Top app bar: "Profile", settings cog icon (top-right)
- Profile header:
  - Avatar (circular)
  - Name
  - Join date
- Card grid (2 columns):
  - Residence card (County, Constituency, Ward)
  - Streak card (Current streak, longest streak, fire icon)
  - Badges card (Achievement gallery)
  - Activity card (Recent learning history)

**Cards Detail**:

**Residence Card**:
- Icon: Location pin
- County name
- Constituency name
- Ward (optional)

**Streak Card**:
- Fire emoji/icon (animated if active)
- Large number: Current streak
- Small text: "Longest: X days"
- Color: Orange/red gradient

**Badges Card**:
- Grid of badge icons
- Earned badges: Full color
- Locked badges: Grayscale with lock
- Tap badge → Show details modal

**Activity Card**:
- List of recent activities
- Activity type icon
- Activity description
- Timestamp
- XP earned (if applicable)

---

#### SettingsScreen
**Route**: `SettingsRoute`

**Purpose**: App configuration and preferences

**Layout**:
- Top app bar: Back button, "Settings" title
- Grouped settings sections
- Standard settings list UI

**Sections**:

1. **Account**
   - Edit profile
   - Change email
   - Change password
   - Delete account (with confirmation)

2. **Notifications**
   - Daily clause reminder (toggle + time picker)
   - Streak reminders (toggle)
   - Achievement notifications (toggle)
   - Learning tips (toggle)
   - Quiet hours (start/end time)

3. **Display**
   - Theme: Light/Dark/System
   - Font size: Small/Medium/Large
   - Reading mode preferences

4. **Language**
   - English (default)
   - Swahili (future)

5. **About**
   - App version
   - Terms of service (link)
   - Privacy policy (link)
   - Licenses (link)
   - Contact support (email)
   - Rate app (store link)

---

## 🛠 Development Setup

### Prerequisites

#### Required Software
1. **JDK 17+**: Java Development Kit
   - Download: [OpenJDK](https://adoptium.net/) or Oracle JDK
   - Verify: `java -version`

2. **Android Studio**: Latest stable version
   - Download: [Android Studio](https://developer.android.com/studio)
   - Includes Android SDK and emulator

3. **Git**: Version control
   - Download: [Git](https://git-scm.com/)
   - Verify: `git --version`

#### For iOS Development (Optional)
4. **macOS**: Required for iOS builds
5. **Xcode 15+**: Apple's IDE
   - Download from Mac App Store
   - Install command line tools: `xcode-select --install`
6. **CocoaPods**: iOS dependency manager
   - Install: `sudo gem install cocoapods`

### Project Setup

#### 1. Clone Repository
```powershell
git clone https://github.com/your-org/katiba.git
cd katiba
```

#### 2. Open in Android Studio
```
1. Launch Android Studio
2. File → Open
3. Navigate to project folder
4. Select the root folder
5. Wait for Gradle sync
```

#### 3. SDK Setup
Android Studio will prompt to install:
- Android SDK Platform 34
- Build Tools 34.0.0
- Android Emulator
- Accept licenses when prompted

#### 4. Gradle Configuration

The project uses Gradle wrapper. First sync will download Gradle automatically.

**Manual Gradle Setup (if needed)**:
```powershell
# Download gradle-wrapper.jar
# Place in gradle/wrapper/ folder
# Or install Gradle globally
choco install gradle
```

#### 5. Verify Setup
```powershell
# Windows
.\gradlew.bat tasks

# Mac/Linux
./gradlew tasks
```

### Running the App

#### Android Emulator
```
1. Tools → Device Manager
2. Create Device → Select Phone model
3. Select System Image (API 34 recommended)
4. Finish setup
5. Run → Select emulator
```

#### Physical Android Device
```
1. Enable Developer Options on device
2. Enable USB Debugging
3. Connect device via USB
4. Accept debugging permission
5. Run → Select device
```

#### iOS Simulator (Mac only)
```powershell
# Open Xcode project
open iosApp/iosApp.xcworkspace

# Or command line
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Development Tools

#### Recommended Plugins
- **Kotlin**: Official Kotlin support
- **Compose Multiplatform**: Compose tooling
- **Material Theme UI**: Theme preview
- **Rainbow Brackets**: Code readability

#### Useful Gradle Commands
```powershell
# Clean build
.\gradlew.bat clean

# Build debug APK
.\gradlew.bat :composeApp:assembleDebug

# Install on device
.\gradlew.bat :composeApp:installDebug

# Run tests
.\gradlew.bat :composeApp:testDebugUnitTest

# Generate release APK
.\gradlew.bat :composeApp:assembleRelease
```

---

## 🚀 Build & Deployment

### Build Variants

#### Debug Build
- Development and testing
- Includes debugging symbols
- Larger APK size
- Not optimized

```powershell
.\gradlew.bat :composeApp:assembleDebug
# Output: composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

#### Release Build
- Production deployment
- ProGuard/R8 optimization
- Code obfuscation
- Smaller APK size

```powershell
.\gradlew.bat :composeApp:assembleRelease
# Output: composeApp/build/outputs/apk/release/composeApp-release.apk
```

### Signing Configuration

Create `keystore.properties` in project root:
```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=YOUR_KEY_ALIAS
storeFile=PATH_TO_KEYSTORE_FILE
```

**⚠️ Important**: Add `keystore.properties` to `.gitignore`

### Version Management

Update in `composeApp/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 1        // Increment for each release
    versionName = "1.0.0"  // Semantic versioning
}
```

### Android Deployment

#### Google Play Store
1. Create developer account ($25 one-time fee)
2. Create app listing
3. Generate signed release APK/AAB
4. Upload to Play Console
5. Fill in store listing details
6. Submit for review

**App Bundle (Recommended)**:
```powershell
.\gradlew.bat :composeApp:bundleRelease
# Output: composeApp/build/outputs/bundle/release/composeApp-release.aab
```

### iOS Deployment

#### App Store
1. Enroll in Apple Developer Program ($99/year)
2. Create App ID in Developer Portal
3. Configure signing in Xcode
4. Archive app in Xcode
5. Upload to App Store Connect
6. Submit for review

**TestFlight (Beta Testing)**:
- Internal testing: Up to 100 testers
- External testing: Up to 10,000 testers
- No app review required for internal

### Continuous Integration/Deployment (CI/CD)

#### GitHub Actions Example
```yaml
name: Android Build

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build with Gradle
        run: ./gradlew assembleDebug
      - name: Run tests
        run: ./gradlew test
```

---

## 🔮 Future Roadmap

### Phase 1: Core Enhancement (Q1 2026)

#### Features
- [ ] Full Constitution PDF parsing and integration
- [ ] Search functionality across all constitutional text
- [ ] Bookmarking system with sync
- [ ] Offline mode for all content
- [ ] Push notifications for daily clauses

#### Technical
- [ ] Backend API development (Node.js + PostgreSQL)
- [ ] User authentication (OAuth + JWT)
- [ ] Cloud storage for videos (AWS S3/CloudFront)
- [ ] Analytics integration (Firebase Analytics)

### Phase 2: Content Expansion (Q2 2026)

#### Features
- [ ] Swahili language support (full translation)
- [ ] Audio narration for clauses (text-to-speech)
- [ ] Interactive quizzes in lessons
- [ ] Discussion forums per clause
- [ ] Expert commentary from constitutional lawyers

#### Content
- [ ] 100+ lessons covering full constitution
- [ ] Daily educator videos (365 videos)
- [ ] Case law examples and precedents
- [ ] Historical context for amendments

### Phase 3: Community Features (Q3 2026)

#### Social Learning
- [ ] User profiles and connections
- [ ] Leaderboards (national, county, constituency)
- [ ] Study groups and communities
- [ ] Shared learning paths
- [ ] Commenting on clauses (moderated)

#### Engagement
- [ ] Weekly challenges and competitions
- [ ] Civic action campaigns
- [ ] Event calendar (civic events, town halls)
- [ ] Petition creation and tracking

### Phase 4: Advanced Features (Q4 2026)

#### AI Integration
- [ ] AI chatbot for constitutional questions
- [ ] Personalized learning recommendations
- [ ] AI-powered quiz generation
- [ ] Natural language clause search
- [ ] Clause comparison tool

#### Accessibility
- [ ] Screen reader optimization
- [ ] High contrast mode
- [ ] Dyslexia-friendly font option
- [ ] Sign language video translations
- [ ] Multi-language support (15+ languages)

### Phase 5: Ecosystem Expansion (2027)

#### Platforms
- [ ] Web application (responsive)
- [ ] Desktop apps (Windows, macOS, Linux)
- [ ] Smart TV apps
- [ ] Voice assistant integration (Alexa, Google)

#### Integrations
- [ ] School curriculum integration
- [ ] NGO partnership programs
- [ ] Government civic education initiatives
- [ ] Legal aid organization partnerships

### Long-term Vision (2027+)

- [ ] Expand to other African countries' constitutions
- [ ] Regional human rights law integration
- [ ] International law education modules
- [ ] Civic participation tracking and rewards
- [ ] Real-world impact measurement

---

## 🤝 Contributing Guidelines

### Getting Started

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/your-feature-name`
3. **Make your changes**
4. **Write tests** for new functionality
5. **Commit with clear messages**: `git commit -m "feat: add user search"`
6. **Push to your fork**: `git push origin feature/your-feature-name`
7. **Open a Pull Request**

### Code Standards

#### Kotlin Style Guide
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Maximum line length: 120 characters
- Use spaces, not tabs (4 spaces)

#### Compose Best Practices
- Keep composables small and focused
- Extract reusable components
- Use `remember` for state
- Hoist state when shared
- Use `LaunchedEffect` for side effects

#### Git Commit Messages
Follow [Conventional Commits](https://www.conventionalcommits.org/):
```
feat: Add new feature
fix: Bug fix
docs: Documentation changes
style: Code formatting
refactor: Code restructuring
test: Adding tests
chore: Maintenance tasks
```

### Pull Request Process

1. **Ensure CI passes**: All tests must pass
2. **Update documentation**: README, comments, etc.
3. **Add screenshots**: For UI changes
4. **Request review**: Tag appropriate reviewers
5. **Address feedback**: Make requested changes
6. **Squash commits**: Before merging

### Testing Requirements

#### Unit Tests
- Write tests for new business logic
- Maintain >80% code coverage
- Use descriptive test names

```kotlin
@Test
fun `given valid clause when formatting then returns formatted text`() {
    // Test implementation
}
```

#### UI Tests
- Test critical user flows
- Use Compose testing framework
- Test accessibility

### Documentation

#### Code Comments
- Document public APIs with KDoc
- Explain complex logic
- Avoid obvious comments

```kotlin
/**
 * Fetches the daily constitutional clause for the specified date.
 *
 * @param date The date to fetch content for (ISO format)
 * @return DailyContent or null if unavailable
 */
suspend fun getDailyClause(date: String): DailyContent?
```

#### README Updates
- Keep README current
- Document new features
- Update setup instructions

### Code Review Guidelines

#### For Authors
- Keep PRs small and focused
- Provide context in PR description
- Respond to feedback promptly
- Test thoroughly before submitting

#### For Reviewers
- Be respectful and constructive
- Check for code quality and standards
- Test the changes locally
- Approve once satisfied

### Community Guidelines

- **Be respectful**: Treat everyone with kindness
- **Be inclusive**: Welcome all contributors
- **Be constructive**: Provide helpful feedback
- **Be patient**: Remember everyone is learning
- **Follow code of conduct**: Maintain professional behavior

### Reporting Issues

#### Bug Reports
Include:
- Device/OS information
- Steps to reproduce
- Expected vs actual behavior
- Screenshots/logs if applicable

#### Feature Requests
Include:
- Problem statement
- Proposed solution
- Use cases
- Mockups (if applicable)

### Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Acknowledged in release notes
- Featured on project website (coming soon)

---

## 📄 License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2025 Katiba App

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🙏 Acknowledgments

### Inspiration
- **Duolingo**: Gamification and learning path design
- **YouVersion Bible App**: Card-based home screen and reading experience
- **Kenya Law Reform Commission**: Constitutional content and resources

### Design Inspiration
- Kenyan national symbols and colors
- Traditional Maasai beadwork patterns
- East African cultural aesthetics

### Dependencies
- JetBrains: Kotlin Multiplatform and Compose
- Google: Android platform and Material Design
- Open source community: All library authors

### Contributors
See [CONTRIBUTORS.md](CONTRIBUTORS.md) for a list of amazing people who have contributed to this project.

---

## 📞 Contact & Support

### Development Team
- **Email**: dev@katibaapp.com
- **GitHub**: [github.com/katiba-app](https://github.com/katiba-app)
- **Website**: [www.katibaapp.com](https://www.katibaapp.com) (coming soon)

### Social Media
- **Twitter**: [@KatibaApp](https://twitter.com/KatibaApp)
- **Facebook**: [Katiba App](https://facebook.com/KatibaApp)
- **LinkedIn**: [Katiba App](https://linkedin.com/company/katiba-app)

### Support
- **User Support**: support@katibaapp.com
- **Bug Reports**: [GitHub Issues](https://github.com/katiba-app/katiba/issues)
- **Feature Requests**: [GitHub Discussions](https://github.com/katiba-app/katiba/discussions)

---

## 📚 Additional Resources

### Learning Materials
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform Guides](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Material Design 3](https://m3.material.io/)
- [Android Developer Guides](https://developer.android.com/guide)

### Kenyan Constitution Resources
- [Kenya Law](http://www.kenyalaw.org/) - Official legal resource portal
- [Constitution of Kenya 2010](http://www.kenyalaw.org/lex/actview.xql?actid=Const2010) - Full text
- [Kenya Law Reform Commission](http://www.klrc.go.ke/)

### Civic Education
- [IEBC Civic Education](https://www.iebc.or.ke/civic-education/)
- [Katiba Institute](https://katibainstitute.org/)
- [Transparency International Kenya](https://tikenya.org/)

---

<div align="center">

**Made with ❤️ for Kenya**

*Empowering citizens through constitutional literacy*

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Compose-1.7.3-green.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)

[Documentation](docs/) • [Contributing](CONTRIBUTING.md) • [Issues](https://github.com/katiba-app/katiba/issues) • [Discussions](https://github.com/katiba-app/katiba/discussions)

</div>

