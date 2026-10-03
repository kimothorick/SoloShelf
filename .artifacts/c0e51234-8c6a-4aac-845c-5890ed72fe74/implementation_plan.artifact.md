# Implementation Plan - Unified Asset Discovery

Transition the import process from extension-based identification to Readium's unified asset discovery. This leverages `AssetRetriever` to "sniff" file contents (magic bytes) and determine the `MediaType`, making the app much more robust against mislabeled files and complex containers like ZIPs.

## User Review Required

> [!IMPORTANT]
> This change shifts the primary source of truth for file types from file extensions to content-based MediaTypes. While we will still respect the list of supported formats in `Constants.kt`, the app will now be able to correctly identify an EPUB even if its extension is `.zip` or missing.

## Proposed Changes

### Domain Layer

#### [MODIFY] [BookMetadataExtractor.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/domain/BookMetadataExtractor.kt)
- Update `extractMetadata` to leverage the `MediaType` provided by the `Asset`.
- Improve parser selection logic using `MediaType` properties (e.g., `mediaType.isAudio`, `mediaType == MediaType.EPUB`).

---

### UI Layer

#### [MODIFY] [MainViewModel.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/ui/main/MainViewModel.kt)
- Refactor `addSingleBook` to use `asset.format.mediaType` for determining if a book is an audiobook or ebook.
- Use `MediaType` to extract the canonical file extension for the database `format` field.
- Clean up `addMultiFileAudiobook` to use similar discovery logic for consistency.

---

### Utility Layer

#### [MODIFY] [Constants.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/util/Constants.kt)
- Add a helper extension or utility to check if a `MediaType` is supported based on the existing extension lists.

## Verification Plan

### Automated Tests
- I will verify the build remains stable after these changes.
- (Optional) I can add a unit test for `MediaType` mapping if requested.

### Manual Verification
- **Import EPUB**: Verify it is identified as an ebook.
- **Import MP3/M4A**: Verify they are identified as audiobooks and metadata is extracted via fallback.
- **Import ZIP**: Verify that if it contains an EPUB, it is imported correctly as an ebook.
- **Unsupported File**: Verify the app shows a proper error message when a non-supported MediaType is detected.
