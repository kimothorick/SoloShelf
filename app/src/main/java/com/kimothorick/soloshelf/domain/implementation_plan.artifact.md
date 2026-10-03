# Support MetaDataKit JSON Metadata Extraction

This plan adds support for parsing metadata from JSON files exported by online metadata viewers (specifically the MetaDataKit format) during audiobook imports.

## Proposed Changes

### Domain Layer

#### [MODIFY] [BookMetadataExtractor.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/domain/BookMetadataExtractor.kt)

- Update the JSON parsing logic in `extractAudiobookMetadata` to handle the `MetaDataKit` structure.
- Extract enriched metadata:
    - **Title**: Prioritize `Album` or `File Attributes.data.Album`.
    - **Author**: Prioritize `AlbumArtist` or `File Attributes.data.AlbumArtist`.
    - **Synopsis**: Extract from `File Attributes.data.Description`.
    - **Publication Date**: Extract from `File Attributes.data.ReleaseDate` or `Year`.
    - **Narrator**: (If possible, though MetaDataKit doesn't seem to have a dedicated field for it, sometimes it's in `Comment` or `artist` if `AlbumArtist` is different).
    - **Identifier/URL**: Extract from `File Attributes.data.Wwwaudiofile`.

## Verification Plan

### Manual Verification
1. Import an audiobook folder containing a `.m4b` file and the provided `@The Cult of Dr. Black - E.B.Hudspeth-MetaDataKit-2026-09-16-23-36-58.json`.
2. Verify that the book title is "The Cult of Dr. Black".
3. Verify that the author is "E. B. Hudspeth".
4. Verify that the synopsis is correctly populated with the long description.
5. Verify that the publication date is "08-Sep-2026".
