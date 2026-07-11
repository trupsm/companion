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

**Robust Demo Mode**: The app features a transparent fallback mechanism. If the Gemini API key is missing, invalid, or encounters rate limits (HTTP 429), it automatically generates realistic mock roadmaps, curriculum expansions, quizzes, and resources. This ensures the app is fully demonstrable and functional under any network or quota conditions.

---

## Features

### 🔐 Authentication
- Local sign-up and login with username + password.
- Passwords are hashed using **SHA-256** with a randomly generated Base64 salt — never stored in plaintext.
- Session persistence via encrypted `SharedPreferences` (`EncryptedSharedPreferences`).
- App auto-routes to Dashboard if a session is already active.

### 🗺️ Roadmap Management
- **AI Generation** — Describe your goal, pick duration and experience level, and Gemini generates a week-by-week skeleton roadmap.
- **Fail-safe Fallback** — Automatically falls back to high-quality, realistic mock data for generation, expansions, quizzes, and resources if the API key is missing or encounters HTTP 429 rate limits.
- **File Import** — Upload a `.txt` file **or paste raw text** into the import screen. Both paths are standardized by Gemini into a structured week/day skeleton, so any well-formatted plain-text roadmap is supported.
- **Hours Validation** — When creating a roadmap, hours/day for that course plus all existing active course hours must not exceed your global daily study budget (set in Settings).
- **Start/Pause/Resume** — New roadmaps default to `NOT_YET_STARTED`. Start to activate them. Pausing hides tasks from the dashboard, and resuming shifts the calendar timeline forward by the pause duration automatically.
- **Edit** — Update title and learning goal at any time via the Actions menu (⋮).
- **Delete** — Remove a roadmap and all its milestones and daily tasks via a confirmation dialog.

### 📅 Dashboard & Calendar
- Greets the user by name with today's date.
- Horizontal calendar: tap any date to see scheduled tasks.
- Tasks are calculated from the roadmap's `startedAt` date — only `ACTIVE` roadmaps appear.
- Task completion status is reflected (completed tasks shown with a check icon).
- **Calendar indicators**: dots on calendar dates that have pending tasks.
- **Analytics cards**: Overall completion percentage and progress bar per active course.
- **Day-level completion bars**: Visual progress bar inside each task card showing day completion ratio.

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
- Notes can be **edited** — tapping any saved note pre-fills the editor dialog for updates.
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
| **Dashboard** | Calendar + Today's Tasks with analytics cards and completion indicators |
| **Roadmaps (Home)** | List of all roadmaps with status badges |
| **Create Roadmap** | AI-powered goal input form |
| **Import Roadmap** | Tabbed: Upload `.txt` file **or** Paste raw text — both AI-standardized |
| **Roadmap Details** | Week tabs, milestone cards, daily topic list, lazy Gemini expansion, Start/Edit/Delete/Pause/Resume |
| **Topic Detail** | Topic view with IN_PROGRESS badge, AI resource chips (open in browser), Mark as Complete |
| **Quiz** | 5-question MCQ quiz per topic with colour-coded feedback, score screen, retake |
| **Notes** | Saved notes list with add/edit-dialog FAB and per-note delete |
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
│   │   │   ├── QuizDao.kt           # Quiz: insert, query, existence checks
│   │   │   └── UserDao.kt           # User authentication queries
│   │   ├── entity/
│   │   │   ├── RoadmapEntity.kt     # id, title, goal, hoursPerDay, startedAt, status...
│   │   │   ├── MilestoneEntity.kt   # Weekly milestones
│   │   │   ├── CurriculumItemEntity.kt  # Daily topics with status
│   │   │   ├── NoteEntity.kt        # Notes with dayDate + optional curriculumItemId
│   │   │   ├── QuizQuestionEntity.kt # Quiz questions for curriculum items
│   │   │   ├── UserEntity.kt        # User credentials (hash + salt)
│   │   │   └── ...                  # Streaks, Reviews
│   │   ├── security/
│   │   │   ├── SecureStorage.kt     # EncryptedSharedPreferences wrapper
│   │   │   └── PasswordHasher.kt    # SHA-256 + Base64 salt utility
│   │   └── LearningDatabase.kt      # Room @Database (version 3)
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
│   │   ├── CurriculumRepositoryImpl.kt
│   │   └── QuizRepositoryImpl.kt
│   │
│   └── mock/
│       └── SeedData.kt              # Sample data for development
│
├── domain/
│   ├── repository/
│   │   ├── RoadmapRepository.kt
│   │   ├── CurriculumRepository.kt
│   │   └── QuizRepository.kt
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
    ├── details/                     # Week tabs, lazy expansion, Pause/Resume, Start/Edit/Delete
    ├── create/                      # AI generation form
    ├── importroadmap/               # File upload + paste-text tabs, AI standardization
    ├── topic/                       # Topic detail + IN_PROGRESS auto-state + AI resources
    ├── quiz/                        # MCQ quiz screen (generate + cache + retake)
    ├── settings/                    # API key, study hours
    └── notes/                       # Multi-note list + add/edit FAB dialog
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

### Importing from File or Pasting Text
1. Tap **Import** on the Roadmaps screen.
2. Choose **Upload File** to pick a `.txt` file, or **Paste Text** to directly paste your roadmap content.
3. Fill in Title, Hours/Day, Duration, and Experience Level.
4. Gemini AI standardizes the raw input into a structured week/day skeleton — so both hand-written and AI-generated formats are supported.
5. Weeks and daily topics are saved to the database automatically.

#### Supported Input Format (File or Paste)

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

> The AI standardization step means even loosely structured plain-text roadmaps will be correctly parsed into milestones and daily tasks.

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

Room database (`LearningDatabase`, version 3):

| Table | Key Fields |
|---|---|
| `roadmaps` | `id`, `title`, `goal`, `duration`, `experienceLevel`, `hoursPerDay`, `status`, `startedAt`, `pausedAt`, `createdAt` |
| `milestones` | `id`, `roadmapId`, `weekNumber`, `title`, `summary`, `expansionStatus` |
| `curriculum_items` | `id`, `roadmapId`, `milestoneId`, `dayNumber`, `topic`, `description`, `estimatedTime`, `status` |
| `notes` | `id`, `roadmapId`, `curriculumItemId` (nullable), `dayDate`, `title`, `content`, `createdAt`, `updatedAt` |
| `users` | `id`, `username`, `passwordHash`, `salt`, `createdAt` |
| `quiz_questions` | `id`, `curriculumItemId`, `question`, `options`, `correctAnswer` |
| `streak_logs` | `id`, `date`, `wasActive` |
| `review_schedules` | `id`, `itemId`, `nextReviewDate` |

> Schema changes require `.\gradlew clean` — `fallbackToDestructiveMigration()` wipes and recreates the DB on version mismatch. The database is currently at **version 3**, bumped to add `QuizDao` for quiz question persistence.

---

## Roadmap Lifecycle

```
Created / Imported
        │
        ▼
 NOT_YET_STARTED ── Tap "Start" ──▶ ACTIVE ◀── Resume ── PAUSED (hides tasks, shifts dates)
                                      │
                                      ▼
                               Mark Complete
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
| Roadmap starting, pausing, and resuming with automatic calendar shift logic | ✅ Done |
| Edit and Delete roadmaps | ✅ Done |
| Task completion persisted to database | ✅ Done |

### ✅ Phase 3 — Curriculum Expansion (Complete)
Full lazy curriculum expansion, AI-generated quizzes, resource recommendations, and CurriculumItem state machine are all implemented.

| Feature | Status |
|---|---|
| **Pass 2: Lazy Weekly Expansion** | ✅ Done — Gemini expands a week when its tab is first opened |
| **Expansion status tracking** | ✅ Done — `PENDING` → `EXPANDING` → `EXPANDED` per milestone |
| **Retry on failure** | ✅ Done — Error card with a Retry button on expansion failure |
| **Resource recommendations** | ✅ Done — Gemini generates 4 curated resources (VIDEO/DOCS/PRACTICE/ARTICLE) per topic, displayed as tappable chips that open in browser |
| **Quiz generation** | ✅ Done — On-demand Gemini call generates 5 MCQ questions per topic; cached in Room so no re-generation on revisit |
| **Quiz UI** | ✅ Done — Per-question card with 4 colour-coded options, correct/wrong reveal, score screen with breakdown, Retake and Back actions |
| **Full `CurriculumItem` state machine** | ✅ Done — `NOT_STARTED` → `IN_PROGRESS` (auto on open) → `COMPLETED` (Mark as Complete button) |

### ✅ Phase 4 — Streaks & Reminders (Complete)
Habit-forming retention mechanics. (Note: Spaced repetition scheduling and background WorkManager review checks were skipped per user preference to streamline core retention).

| Feature | Status |
|---|---|
| **Daily study streak** | ✅ Done — Log `StreakLogEntity` on topic completion; calculate active streak; handle grace day protection and milestone rewards |
| **Spaced repetition scheduling** | ⏩ Skipped (Explicitly skipped by user) |
| **Daily reminder notifications** | ✅ Done — `AlarmManager` exact-time reminders, containing custom messages with motivational quotes and today's task summaries |
| **Background review alerts** | ⏩ Skipped (Explicitly skipped by user) |

### 🌐 Future Enhancements: AI Mentor Mode (Version 2) & MCP

To evolve the application from an AI-assisted tool into an autonomous agentic system, **AI Mentor Mode (Version 2)** will implement a multi-agent orchestration pattern coordinated by a central coordinator:

#### 🤖 Coordinated AI Agent Ecosystem
* **Learning Planner Agent**: Plans learning paths, estimates study time, splits difficult concepts, and updates roadmaps dynamically based on progress.
* **Resource Curator Agent**: Searches and selects official documentation, practical labs, repositories, and media adapted to the user's skill level.
* **Daily Mentor Agent**: Manages daily workloads, tracks study consistency, sends personalized encouragement/reminders, and adjusts schedule for inactivity.
* **Quiz Coach Agent**: Measures comprehension, dynamically scales MCQ/Scenario-based difficulty, tracks weak concepts, and prepares interview prep.
* **Revision Planner Agent**: Controls long-term retention using adaptive spaced repetition scheduled around quiz scores and study consistency.

#### 🔄 Cross-Agent Collaboration Loop
Agents communicate continuously through shared state to dynamically refine the user's curriculum:
* *Quiz Coach* flags weak concept → *Revision Planner* schedules review → *Learning Planner* updates roadmap → *Daily Mentor* adjusts tomorrow's load → *Resource Curator* injects targeted labs/docs.

#### 🔌 Standardized Model Context Protocol (MCP) Integration
Integrates standardized MCP servers to allow the AI Agents to orchestrate external resources modularly, including:
* LLM providers selection
* YouTube, GitHub, & Document searches
* Direct interfaces to Hack The Box, TryHackMe, AWS Skill Builder, & Microsoft Learn
* Auto-generated project recommendations and validation checks on URLs

---

---