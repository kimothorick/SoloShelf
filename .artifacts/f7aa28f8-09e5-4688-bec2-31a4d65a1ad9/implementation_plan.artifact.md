# Refactor Library Screen to use HorizontalPager and Independent Grids

The current implementation shares a single `LazyVerticalGrid` between the Ebooks and Audiobooks tabs. This causes scroll position loss and layout flickering when switching tabs. This plan refactors the UI to use a `HorizontalPager` where each tab has its own dedicated `LazyVerticalGrid`.

## User Review Required

> [!IMPORTANT]
> This change introduces `HorizontalPager` from Compose Foundation, which adds swipe-to-switch functionality between library tabs.
> We will also separate the `allBooks` and `recentBooks` flows in the `LibraryViewModel` into tab-specific flows to allow each tab to maintain its own state.

## Proposed Changes

### [Data & ViewModel Layer]

#### [MODIFY] [LibraryViewModel.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/ui/screens/LibraryViewModel.kt)
- Replace the shared `allBooks` and `recentBooks` flows with tab-specific versions: `ebooks`, `audiobooks`, `recentEbooks`, and `recentAudiobooks`.
- This ensures that switching tabs doesn't interrupt the paging state of the other tab.

### [UI Layer]

#### [MODIFY] [Library.kt](file:///Users/kimothorick/Desktop/Android Apps/SoloShelf/app/src/main/java/com/kimothorick/soloshelf/ui/screens/Library.kt)
- Refactor `LibraryContent` to use `HorizontalPager`.
- Extract the library grid logic into a reusable `LibraryGrid` component.
- Sync the `PagerState` with the `TabRow` selection.
- Update `Library` to collect all four flows and pass them to the refactored `LibraryContent`.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to the Library screen.
- Scroll down in the Ebooks tab, switch to Audiobooks, then switch back to Ebooks. Verify the scroll position is preserved.
- Swipe horizontally between tabs and ensure the layout (item widths) transitions smoothly.
- Verify that "Currently Reading" and "All Books" labels update correctly for each tab.
