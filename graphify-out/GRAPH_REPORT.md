# Graph Report - Linote  (2026-10-11)

## Corpus Check
- 39 files · ~20,874 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 363 nodes · 810 edges · 28 communities (14 shown, 11 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 70 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `36d69aa5`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Composer.kt
- MediaPreview.kt
- idleEditor
- Cursor
- Editor
- NoteList.kt
- Note
- DocumentInputConnection
- DebugNoteSeed
- Icons.kt
- DebugNoteSeedTest
- DebugSeedActivity
- Note UUID Tests
- Composer Notes Specification
- Gradle Wrapper Script
- UUID v7 Generator
- Room over SQLite
- draft.json
- Document Model
- Coil
- Media3 (ExoPlayer)
- Android Architecture Components
- Task 5: API 24 Compatibility
- release/java/com/linote/DebugSeedLauncher.kt
- NewestFollowTest

## God Nodes (most connected - your core abstractions)
1. `Editor` - 34 edges
2. `Piece` - 25 edges
3. `Composer()` - 22 edges
4. `idleEditor()` - 22 edges
5. `layout()` - 20 edges
6. `Note` - 19 edges
7. `NotesViewModel` - 16 edges
8. `DocumentBody()` - 15 edges
9. `DocumentInputConnection` - 14 edges
10. `DocLayout` - 14 edges

## Surprising Connections (you probably didn't know these)
- `Composer Notes Specification` --references--> `Jetpack Compose`  [INFERRED]
  docs/superpowers/specs/2026-10-10-composer-notes-design.md → stack.md
- `Task 6: Editor Locking` --conceptually_related_to--> `Room over SQLite`  [INFERRED]
  .superpowers/sdd/composer_notes_implementation_a4a81be5.plan/progress.md → stack.md
- `Composer()` --calls--> `ArrowUpIcon()`  [INFERRED]
  app/src/main/java/com/linote/Composer.kt → app/src/main/java/com/linote/Icons.kt
- `Composer()` --calls--> `PaperclipIcon()`  [INFERRED]
  app/src/main/java/com/linote/Composer.kt → app/src/main/java/com/linote/Icons.kt
- `DockedComposer()` --calls--> `Composer()`  [INFERRED]
  app/src/main/java/com/linote/MainActivity.kt → app/src/main/java/com/linote/Composer.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Composer Implementation Flow** — composer_notes_design, composer_viewmodel, composer_document_model, composer_draft_json [EXTRACTED 1.00]
- **Android Compatibility Strategy** — superpowers_sdd_composer_notes_implementation_a4a81be5_plan_progress_task_5, stack_media_media3, stack_ui [INFERRED 0.70]

## Communities (28 total, 11 thin omitted)

### Community 0 - "Composer.kt"
Cohesion: 0.11
Nodes (38): Activity, Alignment, activityOf(), Composer(), ComposerHit(), ComposerInputState, dragSelection(), Dp (+30 more)

### Community 1 - "MediaPreview.kt"
Cohesion: 0.12
Nodes (29): android, MediaKind, FILE, IMAGE, VIDEO, decodeImage(), FileFallback(), Icon (+21 more)

### Community 2 - "idleEditor"
Cohesion: 0.16
Nodes (4): idleEditor(), DraftStoreTest, EditorTest, SaveTest

### Community 3 - "Cursor"
Cohesion: 0.15
Nodes (8): DraftStore, Cursor, jsonString(), readContent(), readDraft(), writeDraft(), writePieces(), DocumentJsonTest

### Community 4 - "Editor"
Cohesion: 0.21
Nodes (27): parsePieces(), cardMediaWidth(), Atom, backspace(), Ch, commitText(), deleteForward(), Editor (+19 more)

### Community 5 - "NoteList.kt"
Cohesion: 0.11
Nodes (29): formatNoteTime(), DockedComposer(), androidx, Bundle, ComponentActivity, Modifier, MainActivity, NotesScreen() (+21 more)

### Community 6 - "Note"
Cohesion: 0.07
Nodes (24): Context, loadExisting(), seedDatabase(), SeedWork, Context, LinoteDatabase, UuidConverters, Note (+16 more)

### Community 7 - "DocumentInputConnection"
Cohesion: 0.11
Nodes (10): ComposerInputElement, ComposerInputNode, DocumentInputConnection, BaseInputConnection, ExtractedText, ExtractedTextRequest, Job, ModifierNodeElement (+2 more)

### Community 8 - "DebugNoteSeed"
Cohesion: 0.22
Nodes (7): DebugNoteSeed, Kind, EDGE, LONG, MEDIUM, SHORT, VERY_LONG

### Community 9 - "Icons.kt"
Cohesion: 0.58
Nodes (10): ArrowUpIcon(), FileGlyph(), Color, Dp, Modifier, OverflowIcon(), PaperclipIcon(), SearchIcon() (+2 more)

### Community 11 - "DebugSeedActivity"
Cohesion: 0.30
Nodes (7): DebugSeedActivity, Bundle, ComponentActivity, debugSeedLauncher(), ComponentActivity, Button, TextView

### Community 13 - "Composer Notes Specification"
Cohesion: 0.40
Nodes (5): Composer Notes Specification, Jetpack Compose, SDD Progress Ledger, Buttercup.sh Light Theme, Linote Composer Theme

### Community 14 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 16 - "Room over SQLite"
Cohesion: 0.67
Nodes (3): Room over SQLite, Paging 3, Task 6: Editor Locking

## Knowledge Gaps
- **24 isolated node(s):** `SHORT`, `MEDIUM`, `LONG`, `VERY_LONG`, `EDGE` (+19 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 73 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Piece` connect `Editor` to `Composer.kt`, `MediaPreview.kt`, `Cursor`, `NoteList.kt`, `Note`, `DebugNoteSeedTest`?**
  _High betweenness centrality (0.202) - this node is a cross-community bridge._
- **Why does `Editor` connect `Editor` to `Composer.kt`, `idleEditor`, `Cursor`, `NoteList.kt`, `Note`?**
  _High betweenness centrality (0.153) - this node is a cross-community bridge._
- **Why does `Note` connect `Note` to `DebugNoteSeed`, `DebugNoteSeedTest`, `NoteList.kt`?**
  _High betweenness centrality (0.109) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `Composer()` (e.g. with `DocumentBody()` and `stringWidth()`) actually correct?**
  _`Composer()` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `idleEditor()` (e.g. with `finishInsert()` and `.draft_keepsTimestamp_andRejectsGarbage()`) actually correct?**
  _`idleEditor()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 10 inferred relationships involving `layout()` (e.g. with `normalize()` and `.caret_onWrappedLine_newline_andBlock()`) actually correct?**
  _`layout()` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `SHORT`, `MEDIUM`, `LONG` to the rest of the system?**
  _24 weakly-connected nodes found - possible documentation gaps or missing edges._