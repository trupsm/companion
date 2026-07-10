# 📚 Learning Companion — AI-Powered Study Roadmap App

> A native Android application that helps learners build, track, and complete structured study roadmaps — powered by the Gemini AI API with full offline support.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Screenshots & Screens](#screenshots--screens)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [How Roadmaps Work](#how-roadmaps-work)
- [Authentication](#authentication)
- [Notes System](#notes-system)
- [Database Schema](#database-schema)
- [Roadmap Lifecycle](#roadmap-lifecycle)
- [What's Implemented vs. What's Planned](#whats-implemented-vs-whats-planned)

---

## Overview

**Learning Companion** is an offline-first Android app designed to help users manage long-term learning goals by generating structured, week-by-week study roadmaps. Users can either describe their learning goal and let the **Gemini AI** generate a skeleton roadmap, or import a pre-made roadmap from a `.txt` file.

Every roadmap is broken into **weekly milestones** and **daily curriculum items**, each of which can be tracked, marked complete, and reviewed on a per-day calendar on the Dashboard.

---

## Features

### 🔐 Authentication
- Local sign-up and login with username + password.
- Passwords are hashed using **SHA-256** with a randomly generated Base64 salt — never stored in plaintext.
- Session persistence via encrypted `SharedPreferences` (`EncryptedSharedPreferences`).
- App auto-routes to Dashboard if a session is already active.

### 🗺️ Roadmap Management
- **AI Generation** — Describe your goal, pick duration and experience level, and Gemini generates a week-by-week skeleton roadmap.
- **File Import** — Upload a `.txt` file. The built-in parser handles `=== WEEK N ===` headers and `Day N:` entries, supporting line-wrapped text and multiple days per line.
- **Hours Validation** — When creating a roadmap, hours/day for that course plus all existing active course hours must not exceed your global daily study budget (set in Settings).
- **Start Scheduling** — New roadmaps default to `NOT_YET_STARTED`. Tap **Start Roadmap** on the details screen to activate calendar scheduling from today.
- **Edit** — Update title and learning goal at any time via the Actions menu (⋮).
- **Delete** — Remove a roadmap and all its milestones and daily tasks via a confirmation dialog.

### 📅 Dashboard & Calendar
- Greets the user by name with today's date.
- Horizontal calendar: tap any date to see scheduled tasks.
- Tasks are calculated from the roadmap's `startedAt` date — only `ACTIVE` roadmaps appear.
- Task completion status is reflected (completed tasks shown with a check icon).

### 📖 Roadmap Details
- Week-by-week **tab navigation** (Week 1, Week 2, ...).
- Each tab shows the milestone summary plus the list of daily topics for that week.
- Completed tasks are visually distinguished (grey, checked icon).
- `NOT_YET_STARTED` roadmaps display a highlighted banner with a **Start** button.

### ✅ Task Completion
- Tap any daily task to open its **Topic Detail** screen.
- Tap **Mark as Complete** to permanently write `COMPLETED` status to Room.
- Status reflects immediately on the Dashboard calendar and the weekly topic list.

### 📝 Notes (Multiple per Day)
- Add multiple notes per study session or topic.
- Each note has an optional title and full content area.
- Notes are saved to the database — they persist across sessions and app restarts.
- Notes can be deleted individually.
- Grouped by date (today's notes) or by topic when opened from a curriculum item.

### ⚙️ Settings
- Set your **Gemini API Key** (stored in encrypted `SharedPreferences`).
- Configure **global daily study hours** used for roadmap validation.

---

## Screenshots & Screens

| Screen | Description |
|---|---|
| **Auth Screen** | Tabbed Login / Sign Up with password visibility toggle |
| **Dashboard** | Calendar + Today's Tasks with user greeting |
| **Roadmaps (Home)** | List of all roadmaps with status badges |
| **Create Roadmap** | AI-powered goal input form |
| **Import Roadmap** | Upload `.txt` file with roadmap structure |
| **Roadmap Details** | Week tabs, milestone cards, daily topic list, Start/Edit/Delete |
| **Topic Detail** | Full topic view, status, Mark as Complete button |
| **Notes** | Saved notes list with add-dialog FAB and per-note delete |
| **Settings** | API key, study hours |

---

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with a clean separation of layers:

```
┌─────────────────────────────────────────┐
│              UI Layer                    │
│  Jetpack Compose Screens + ViewModels   │
└────────────────────┬────────────────────┘
                     │ observes StateFlow
┌────────────────────▼────────────────────┐
│           Domain Layer                  │
│  Repository Interfaces + Provider API  │
└────────────────────┬────────────────────┘
                     │ injected via Hilt
┌────────────────────▼────────────────────┐
│             Data Layer                  │
│  Room Database │ Gemini API │ Parser    │
└─────────────────────────────────────────┘
```

- **UI Layer**: Compose screens, each driven by a `@HiltViewModel`, all state exposed as `StateFlow<UiState>`.
- **Domain Layer**: Repository interfaces (`RoadmapRepository`, `CurriculumRepository`) and `LlmProvider` abstract the data sources.
- **Data Layer**: Room for persistence, Gemini REST API via OkHttp, `RoadmapTextParser` for offline document parsing.
- **Dependency Injection**: Hilt modules for database, networking, and repository bindings.

---

## Tech Stack

| Category | Library | Version |
|---|---|---|
| Language | Kotlin | 1.9.22 |
| UI | Jetpack Compose + Material 3 | BOM 2024.02.00 |
| Navigation | Navigation Compose | 2.7.7 |
| DI | Hilt (Dagger) | 2.50 |
| DB | Room | 2.6.1 |
| KSP | Kotlin Symbol Processing | 1.9.22-1.0.17 |
| Networking | OkHttp + Retrofit | 4.12.0 / 2.9.0 |
| Serialization | kotlinx.serialization | 1.6.3 |
| Security | EncryptedSharedPreferences | 1.1.0-alpha06 |
| Coroutines | kotlinx.coroutines | 1.7.3 |
| AI Provider | Google Gemini 1.5 Flash | REST API |
| Build | Gradle KTS + Version Catalogs | AGP 8.2.2 |

---

## Project Structure

```
app/src/main/java/com/companion/learning/
│
├── LearningCompanionApp.kt          # @HiltAndroidApp Application
├── MainActivity.kt                  # Single Activity host
│
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── RoadmapDao.kt        # Roadmap + milestone CRUD, start/edit/delete
│   │   │   ├── CurriculumDao.kt     # Daily topics CRUD + status update
│   │   │   ├── NoteDao.kt           # Notes: insert, query by date/topic, delete
│   │   │   └── UserDao.kt           # User authentication queries
│   │   ├── entity/
│   │   │   ├── RoadmapEntity.kt     # id, title, goal, hoursPerDay, startedAt, status...
│   │   │   ├── MilestoneEntity.kt   # Weekly milestones
│   │   │   ├── CurriculumItemEntity.kt  # Daily topics with status
│   │   │   ├── NoteEntity.kt        # Notes with dayDate + optional curriculumItemId
│   │   │   ├── UserEntity.kt        # User credentials (hash + salt)
│   │   │   └── ...                  # Quizzes, Streaks, Reviews
│   │   ├── security/
│   │   │   ├── SecureStorage.kt     # EncryptedSharedPreferences wrapper
│   │   │   └── PasswordHasher.kt    # SHA-256 + Base64 salt utility
│   │   └── LearningDatabase.kt      # Room @Database (version 1)
│   │
│   ├── remote/
│   │   ├── GeminiProvider.kt        # Gemini 1.5 Flash REST API call + JSON parse
│   │   └── dto/
│   │       └── RoadmapSkeletonDto.kt # MilestoneDto, DayDto
│   │
│   ├── parser/
│   │   ├── RoadmapTextParser.kt     # Marker-based WEEK/Day parser
│   │   └── DocumentParser.kt        # File URI → raw text extractor (.txt)
│   │
│   ├── repository/
│   │   ├── RoadmapRepositoryImpl.kt
│   │   └── CurriculumRepositoryImpl.kt
│   │
│   └── mock/
│       └── SeedData.kt              # Sample data for development
│
├── domain/
│   ├── repository/
│   │   ├── RoadmapRepository.kt
│   │   └── CurriculumRepository.kt
│   └── provider/
│       └── LlmProvider.kt
│
├── di/
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   ├── RepositoryModule.kt
│   └── ParserModule.kt
│
└── ui/
    ├── navigation/AppNavigation.kt  # Auth → Dashboard routing + BottomNav
    ├── auth/                        # Login + SignUp screens
    ├── dashboard/                   # Calendar + Today's Tasks
    ├── home/                        # All roadmaps list
    ├── details/                     # Week tabs, Start/Edit/Delete
    ├── create/                      # AI generation form
    ├── importroadmap/               # File import form
    ├── topic/                       # Topic detail + Mark Complete
    ├── settings/                    # API key, study hours
    └── notes/                       # Multi-note list + FAB dialog
```

---

## Getting Started

### 📦 Step 1: Clone the Repository
Clone the codebase to your local machine using Git:
```bash
git clone <YOUR_GITHUB_REPO_URL>
cd companion
```

### 🛠️ Step 2: Import into Android Studio
1. Open **Android Studio** (Hedgehog 2023.1.1 or later recommended).
2. Click **File -> New -> Import Project...** (or **Open** from the welcome screen).
3. Select the `companion` directory where you cloned the repo.
4. Wait for Android Studio to sync the Gradle build files. It will automatically download JDK 17 (if not present) and all project dependencies listed in the Version Catalog (`gradle/libs.versions.toml`).

### 🔑 Step 3: Prerequisites & Setup
- **Android SDK**: Verify that you have Android SDK API 26+ installed (check under **Tools -> SDK Manager**).
- **Gemini API Key**: You'll need a Google Gemini API Key ([Get one here](https://makersuite.google.com/app/apikey)) to use AI features.

### 🏗️ Step 4: Build & Run the Debug APK
You can build the app directly from your terminal or Android Studio:
```powershell
# Clean build cache (important when database or generated dependencies change)
.\gradlew clean

# Build the debug APK
.\gradlew assembleDebug
```
The compiled debug APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 💻 Developer Guide: Making Further Changes

Follow these structured steps when modifying the codebase:

### 1. Modifying Database Entities & Schema
The database is built on **Room**. If you add/modify fields in database tables (e.g. `RoadmapEntity`, `CurriculumItemEntity`, `NoteEntity`):
1. Update the corresponding Kotlin data class in `com/companion/learning/data/local/entity/`.
2. Increment the `version` number in [LearningDatabase.kt](file:///C:/Users/trupthi/OneDrive/Desktop/companion/app/src/main/java/com/companion/learning/data/local/LearningDatabase.kt).
3. **Crucial**: Run `.\gradlew clean` before compiling. Because Room uses **KSP** (Kotlin Symbol Processing) to generate implementation classes, caching errors (`duplicate class` or `NonExistentClass`) will occur if the cache isn't cleared.

### 2. Updating Repository Layer (Domain/Data Separation)
We use the Repository pattern to isolate UI code from the database/network source:
1. Define repository methods in the interface under `com/companion/learning/domain/repository/` (e.g. `CurriculumRepository.kt`).
2. Implement those methods in `com/companion/learning/data/repository/` (e.g. `CurriculumRepositoryImpl.kt`).
3. Since we bind these interfaces in Hilt's [RepositoryModule.kt](file:///C:/Users/trupthi/OneDrive/Desktop/companion/app/src/main/java/com/companion/learning/di/RepositoryModule.kt), your additions will automatically be injectable into ViewModels.

### 3. Adding/Modifying UI Screens (Jetpack Compose)
1. Add new screens or components inside `com/companion/learning/ui/`.
2. If the screen needs database or network logic, create a `@HiltViewModel` in the same package (e.g. `NotesViewModel`). Inject repositories/DAOs via the constructor.
3. Wire the new view models and compose views inside [AppNavigation.kt](file:///C:/Users/trupthi/OneDrive/Desktop/companion/app/src/main/java/com/companion/learning/ui/navigation/AppNavigation.kt).

### 4. Running Verification Checks
To verify that everything is functionally sound and compiling cleanly:
```powershell
# Build and check for compilation errors
.\gradlew assembleDebug

# Run Unit tests (if any)
.\gradlew test
```

---

## Configuration

### Gemini API Key
1. Open the app → **Settings** (bottom navigation).
2. Paste your **Gemini API Key** into the "API Key" field.
3. The key is saved in `EncryptedSharedPreferences` — never sent to a third party.

### Global Study Hours
Set your maximum study hours per day in **Settings**. When creating a new roadmap, the hours you allocate plus all other active course hours must not exceed this budget.

---

## How Roadmaps Work

### Creating with AI
1. Tap **+** → **Create with AI**.
2. Fill in Goal, Duration, Experience Level, and Hours/Day.
3. Gemini 1.5 Flash generates a JSON skeleton parsed into milestones.
4. Roadmap is saved in `NOT_YET_STARTED` state.

### Importing from File
1. Tap **Import** on the Roadmaps screen.
2. Select a `.txt` file and fill in the Title, Hours, Duration, and Level.
3. `RoadmapTextParser` identifies `=== WEEK N ===` headers and `Day N:` entries.
4. Weeks and daily topics are saved to the database automatically.

#### Supported `.txt` Format

```
======================== WEEK 1 ========================
Day 1: Time Complexity, Space Complexity, Arrays, Prefix Sum
Day 2: Hashing (HashMap, HashSet)
Day 3: Two Pointers
...

======================== WEEK 2 ========================
Day 8: Binary Search
Day 9: Advanced Binary Search
...
```

> The parser handles **line-wrapped** content and **multiple days on one line** using marker-position splitting.

---

## Authentication

Fully local and offline — no external identity provider used.

| Step | What Happens |
|---|---|
| **Sign Up** | SHA-256 hash + random Base64 salt; stored as `UserEntity` in Room |
| **Login** | Input hashed with stored salt and compared |
| **Session** | Username saved to `EncryptedSharedPreferences`; skips Auth on relaunch |

---

## Notes System

Notes are stored in the `notes` Room table with the following design decisions:

- **Multiple notes per day**: Each note is a separate record. There is no limit on how many notes can be saved for a given date.
- **Date grouping**: Notes opened from the Notes screen in the bottom nav show today's notes (`dayDate = LocalDate.now()`).
- **Topic grouping**: Notes opened from a specific topic's detail screen are filtered by `curriculumItemId`.
- **Persistence**: Notes survive app restarts, reinstalls (Room), and are never auto-deleted.
- **Individual delete**: Each note card has a delete button; deletion is permanent.

---

## Database Schema

Room database (`LearningDatabase`, version 1):

| Table | Key Fields |
|---|---|
| `roadmaps` | `id`, `title`, `goal`, `duration`, `experienceLevel`, `hoursPerDay`, `status`, `startedAt`, `createdAt` |
| `milestones` | `id`, `roadmapId`, `weekNumber`, `title`, `summary`, `expansionStatus` |
| `curriculum_items` | `id`, `roadmapId`, `milestoneId`, `dayNumber`, `topic`, `description`, `estimatedTime`, `status` |
| `notes` | `id`, `roadmapId`, `curriculumItemId` (nullable), `dayDate`, `title`, `content`, `createdAt`, `updatedAt` |
| `users` | `id`, `username`, `passwordHash`, `salt`, `createdAt` |
| `quiz_questions` | `id`, `roadmapId`, `question`, `answer` |
| `streak_logs` | `id`, `date`, `wasActive` |
| `review_schedules` | `id`, `itemId`, `nextReviewDate` |

> Schema changes require `.\gradlew clean` — `fallbackToDestructiveMigration()` wipes and recreates the DB on version mismatch.

---

## Roadmap Lifecycle

```
Created / Imported
        │
        ▼
 NOT_YET_STARTED ── Tap "Start Roadmap" ──▶ ACTIVE
                                                │
                               Tasks appear on Dashboard Calendar
                                                │
                               Mark individual tasks as COMPLETED
                                                │
                                     (Planned) ──▶ ARCHIVED
```

---

## What's Implemented vs. What's Planned

This section is derived from the original phased implementation plan and the current state of the codebase.

### ✅ Phase 1 — Foundation (Complete)
The offline skeleton with seeded mock data, Room schema, Hilt DI, and all core UI screens.

| Feature | Status |
|---|---|
| Room database with all entities | ✅ Done |
| Hilt dependency injection | ✅ Done |
| MVVM + Repository pattern | ✅ Done |
| Bottom navigation (Dashboard, Roadmaps, Settings) | ✅ Done |
| Dashboard with calendar + today's tasks | ✅ Done |
| All roadmaps list screen | ✅ Done |
| Settings screen (API key, study hours) | ✅ Done |
| Notes: persistent multi-note list per day/topic | ✅ Done |
| Local authentication (Sign Up / Login / Session) | ✅ Done |

### ✅ Phase 2 — First LLM Integration (Complete)
Gemini 1.5 Flash integrated via direct REST API using OkHttp. Structured JSON responses are parsed into `RoadmapSkeletonDto` and saved to Room.

| Feature | Status |
|---|---|
| Gemini REST API integration | ✅ Done |
| `LlmProvider` abstraction interface | ✅ Done |
| `SecureStorage` for API key (EncryptedSharedPreferences) | ✅ Done |
| AI roadmap generation (Pass 1 milestone skeleton) | ✅ Done |
| File import with `RoadmapTextParser` | ✅ Done |
| Hours-per-day validation across active courses | ✅ Done |
| Roadmap `NOT_YET_STARTED` → `ACTIVE` lifecycle | ✅ Done |
| Edit and Delete roadmaps | ✅ Done |
| Task completion persisted to database | ✅ Done |

### 🔜 Phase 3 — Curriculum Expansion & Full Study Loop (Next)
The current AI integration only generates **milestone skeletons** (Pass 1). Phase 3 implements the full daily curriculum expansion.

| Feature | Plan |
|---|---|
| **Pass 2: Lazy Weekly Expansion** | When a user opens a Week tab, trigger a Gemini call to expand that week into full `CurriculumItem` records with descriptions, resources, and estimated times |
| **Resource recommendations** | Parse and display links/books/tutorials per topic |
| **Quiz generation** | On-demand Gemini call to generate `QuizDto` for a completed topic |
| **Quiz UI** | Multiple-choice quiz screen wired to `QuizQuestionEntity` |
| **Full `CurriculumItem` state machine** | `NOT_STARTED` → `IN_PROGRESS` → `COMPLETED` / `SKIPPED` with timestamps |

### 🔜 Phase 4 — Streaks, Spaced Repetition & Notifications
Habit-forming retention mechanics. The `StreakLogEntity` and `ReviewScheduleEntity` tables are already in the schema, ready to be activated.

| Feature | Plan |
|---|---|
| **Daily study streak** | Log `StreakLogEntity` on topic completion; calculate streak count; handle timezone boundaries and grace days |
| **Spaced repetition scheduling** | Auto-schedule review sessions at Day 3, 7, 30 after completion; mark `ORPHANED` if topic is deleted |
| **Daily reminder notifications** | `AlarmManager` for user-configured exact-time notifications |
| **Background review alerts** | `WorkManager` job to notify when spaced repetition reviews are due |

### 🔜 Phase 5 — Multi-Provider, Resilience & Polish
| Feature | Plan |
|---|---|
| **Additional LLM providers** | Add Claude / OpenAI by implementing the `LlmProvider` interface — no core code changes needed |
| **Network resilience** | Exponential backoff, rate-limit queue via `WorkManager` |
| **MCP seam** | Add `callTool(ToolCallRequest)` stub to `LlmProvider` to prepare for multi-agent Model Context Protocol workflows |
| **Diagnostics panel** | Hidden Settings option to view raw LLM JSON responses for debugging |
| **Logout** | Clear username from `SecureStorage` and route to Auth screen |
| **Animations & theming** | Polish transitions, dark mode variants, custom typography |

### 🌐 Post-Version 1 — Future Vision
As defined in the original architecture plan, these features are explicitly out of scope for Version 1 but the architecture is designed to support them without a rewrite:

| Feature | Why the Architecture Supports It |
|---|---|
| **MCP (Model Context Protocol)** | `LlmProvider.callTool()` seam is ready; MCP servers plug into it |
| **Multi-agent AI workflows** | Multiple `LlmProvider` instances can cooperate through the same abstraction |
| **Automatic resource discovery** | `WorkManager` jobs can run `HEAD` validation checks on AI-generated URLs |
| **Cloud & cross-device sync** | Room is the single source of truth; a sync adapter or Firebase layer can mirror it |
| **Certificate & resume builder** | `ARCHIVED` roadmaps + completed `CurriculumItems` form the data foundation |
| **AI flashcards & interview prep** | `QuizQuestionEntity` already exists; extend with card-style UI |
| **Learning analytics dashboard** | `StreakLogEntity`, `ReviewScheduleEntity`, and completion timestamps provide the raw data |
| **Community & shared roadmaps** | The `RoadmapSkeletonDto` + `DayDto` structure is serializable and shareable |

---

## License

MIT License — Copyright (c) 2026 Learning Companion
