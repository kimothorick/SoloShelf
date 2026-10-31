# 📚 SoloShelf

> [!IMPORTANT]
> 
> This project is currently in early active development.
> Features may be incomplete, some buttons may not work, designs may not be finalized, and breaking changes are expected etc. Data structure is subject to change.
> The current branch is set to "Development" for better tracking incase a user wants to see what I am working on. In the future, this will be reverted back to "main" for releases.

**SoloShelf** is a private, offline-first ebook reader for Android. It focuses on simplicity, privacy, and ownership—your library lives on your device, not in the cloud.

---

## 🚧 Development Roadmap

We are currently building the core **MVP (Minimum Viable Product)**.

- [x] **Project Setup** (Gradle, Dependency Injection, Navigation)
- [x] **Local Database** (Room Database Schema setup)
- [x] **Library UI** (Grid/List view of books)
- [x] **EPUB Parsing** (Metadata extraction using Readium)
- [x] **Basic Reader** (Text rendering, font resizing)
- [ ] **Collections/Shelves management**
- [x] **PDF Support** (Rendering engine)
- [x] **Basic Settings** (Dark mode, Theme customization)

**Future Goals (Post-MVP):**
- [ ] Reading statistics & goals

---

## 📱 Features

* **Offline First:** No internet required. No accounts. No tracking.
* **Universal Support:** Reads EPUB and PDF books.
* **Clean Library:** Manage your ebook collection in one clean interface.
* **Private Data:** All bookmarks, notes, and highlights are stored locally in a Room database.

---

## 🛠 Tech Stack & Architecture

SoloShelf is built using modern Android development practices:

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture principles.
* **Local Data (Source of Truth):** [Room Database](https://developer.android.com/training/data-storage/room)
* **Dependency Injection:** [Hilt](https://dagger.dev/hilt/)
* **Ebook Engine:** [Readium Kotlin Toolkit](https://github.com/readium/kotlin-toolkit)

### Data Flow
The app follows a strict **Offline-First** repository pattern. The UI observes `Flow` data from the Room database. Changes are written to the database asynchronously, which then emits new state to the UI.

---

## 🚀 Getting Started

To build and run this app locally:

1.  **Clone the repo:**
    ```bash
    git clone https://github.com/kimothorick/SoloShelf.git
    ```
2.  **Open in Android Studio:**
    * Requires Android Studio.
    * Sync Gradle project.
3.  **Run:**
    * Select an emulator or physical device (Min SDK 28+).
    * Click **Run**.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
