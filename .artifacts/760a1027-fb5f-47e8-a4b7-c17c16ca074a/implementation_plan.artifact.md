# Audiobook Import Enhancements Plan

This plan aims to implement multi-file audiobook support, allowing users to import multiple audio files as a single "Book" (Audiobook) in SoloShelf. We will also ensure that all audio formats supported by Readium are handled correctly.

## User Review Required

> [!IMPORTANT]
> To support multi-file audiobooks, we will introduce a new `Track` entity in the database. This is a significant change to the data model but will allow for better organization and per-track progress tracking in the future.

## Proposed Changes

### Data Layer

#### [MODIFY] [Book.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/data/models/Book.kt)
- Add `isAudiobook: Boolean` to the `Book` entity for easier filtering.
- Clarify that `totalPages` represents total duration (in seconds) for audiobooks.

#### [NEW] [Track.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/data/models/Track.kt)
- Create a new entity to store individual tracks of an audiobook.
- Fields: `trackId`, `bookId` (Foreign Key to `Book`), `filePath`, `title`, `duration` (seconds), `trackNumber`.

#### [MODIFY] [BookDao.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/data/daos/BookDao.kt)
- Add `@Insert` method for `Track` entities.
- Add a `@Transaction` query to fetch a `Book` with its `Track`s.

#### [MODIFY] [BookDatabase.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/data/BookDatabase.kt)
- Register the new `Track` entity.

### Domain Layer

#### [MODIFY] [BookMetadataExtractor.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/domain/BookMetadataExtractor.kt)
- Update to handle multiple assets for a single book import.
- Calculate total duration by summing up individual track durations.

### UI / ViewModel Layer

#### [MODIFY] [MainViewModel.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/ui/main/MainViewModel.kt)
- Update `addBook` to handle a list of URIs.
- Implement logic to create `Track` entries when multiple audio files are selected.
- Add persistable URI permission handling for all selected files.

#### [MODIFY] [MainScreen.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/ui/screens/main/MainScreen.kt)
- Change the audiobook picker from `OpenDocument` to `OpenMultipleDocuments`.
- Update the FAB action to handle the result list of URIs.

## Verification Plan

### Automated Tests
- Unit tests for `BookMetadataExtractor` with multiple audio assets.
- Room database tests for the new `Track` entity and relations.

### Manual Verification
- Import a single `.m4b` file and verify it appears as an audiobook with duration.
- Import multiple `.mp3` files at once and verify they are grouped under one book with the correct total duration.
- Verify that metadata (Title/Author) is correctly extracted from the audio files.
