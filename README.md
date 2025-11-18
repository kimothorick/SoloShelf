# 📚 SoloShelf

> [!IMPORTANT]  
> Crucial information necessary for users to succeed.
> 
> This project is currently in early active development.
> Features may be incomplete, and breaking changes are expected. Data structure is subject to change.

**SoloShelf** is a private, offline-first ebook and audiobook reader for Android. It focuses on simplicity, privacy, and ownership—your library lives on your device, not in the cloud.

---

## 🚧 Development Roadmap

We are currently building the core **MVP (Minimum Viable Product)**.

- [ ] **Project Setup** (Gradle, Dependency Injection, Navigation)
- [ ] **Local Database** (Room Database Schema setup)
- [ ] **Library UI** (Grid/List view of books)
- [ ] **EPUB Parsing** (Metadata extraction using Readium)
- [ ] **Basic Reader** (Text rendering, font resizing)
- [ ] **Audiobook Player** (ExoPlayer integration)
- [ ] **Collections/Shelves management**
- [ ] **PDF Support** (Rendering engine)
- [ ] **Basic Settings** (Dark mode, Theme customization)

**Future Goals (Post-MVP):**
- [ ] Reading statistics & goals
- [ ] "Whispersync"-style position syncing (optional/manual)

---

## 📱 Features (Planned)

* **Offline First:** No internet required. No accounts. No tracking.
* **Universal Support:** Reads EPUB, PDF, and plays M4B/MP3 audiobooks.
* **Hybrid Library:** Manage your ebooks and audiobooks in one clean interface.
* **Private Data:** All bookmarks, notes, and highlights are stored locally in a Room database.

---

## 🛠 Tech Stack & Architecture

SoloShelf is built using modern Android development practices:

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture principles.
* **Local Data (Source of Truth):** [Room Database](https://developer.android.com/training/data-storage/room)
* **Dependency Injection:** [Hilt](https://dagger.dev/hilt/)
* **Ebook Engine:** [Readium Kotlin Toolkit](https://github.com/readium/kotlin-toolkit) (planned)
* **Audio Engine:** [ExoPlayer](https://github.com/google/ExoPlayer) (Media3)

### Data Flow
The app follows a strict **Offline-First** repository pattern. The UI observes `Flow` data from the Room database. Changes are written to the database asynchronously, which then emits new state to the UI.

---

## 🚀 Getting Started

To build and run this app locally:

1.  **Clone the repo:**
    ```bash
    git clone [https://github.com/yourusername/soloshelf.git](https://github.com/yourusername/soloshelf.git)
    ```
2.  **Open in Android Studio:**
    * Requires Android Studio.
    * Sync Gradle project.
3.  **Run:**
    * Select an emulator or physical device (Min SDK 26+).
    * Click **Run**.

---

## 🤝 Contributing

Contributions are not being accepted at the moment as the project is in early development. It will be open for contributions once it is launched.

If you'd like to help:
1.  Check the [Issues](https://github.com/yourusername/soloshelf/issues) tab for "Good First Issue" tags.
2.  Fork the repository.
3.  Create a feature branch (`git checkout -b feature/AmazingFeature`).
4.  Commit your changes.
5.  Open a Pull Request.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.