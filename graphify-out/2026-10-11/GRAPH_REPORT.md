# Graph Report - Linote  (2026-10-11)

## Corpus Check
- 38 files · ~20,447 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 346 nodes · 783 edges · 26 communities (13 shown, 10 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 68 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- layout
- Piece
- NotesViewModel.kt
- idleEditor
- Composer.kt
- NoteList.kt
- Note
- DocumentInputConnection
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

## God Nodes (most connected - your core abstractions)
1. `Editor` - 34 edges
2. `Piece` - 25 edges
3. `Composer()` - 22 edges
4. `idleEditor()` - 22 edges
5. `layout()` - 20 edges
6. `Note` - 17 edges
7. `NotesViewModel` - 16 edges
8. `DocumentBody()` - 15 edges
9. `DocumentInputConnection` - 14 edges
10. `DocLayout` - 14 edges

## Surprising Connections (you probably didn't know these)
- `Composer Notes Specification` --references--> `Jetpack Compose`  [INFERRED]
  docs/superpowers/specs/2026-10-10-composer-notes-design.md → stack.md
- `Task 6: Editor Locking` --conceptually_related_to--> `Room over SQLite`  [INFERRED]
  .superpowers/sdd/composer_notes_implementation_a4a81be5.plan/progress.md → stack.md
- `Composer()` --calls--> `DocumentBody()`  [INFERRED]
  app/src/main/java/com/linote/Composer.kt → app/src/main/java/com/linote/DocumentBody.kt
- `Composer()` --calls--> `stringWidth()`  [INFERRED]
  app/src/main/java/com/linote/Composer.kt → app/src/main/java/com/linote/DocumentBody.kt
- `Composer()` --calls--> `ArrowUpIcon()`  [INFERRED]
  app/src/main/java/com/linote/Composer.kt → app/src/main/java/com/linote/Icons.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Composer Implementation Flow** — composer_notes_design, composer_viewmodel, composer_document_model, composer_draft_json [EXTRACTED 1.00]
- **Android Compatibility Strategy** — superpowers_sdd_composer_notes_implementation_a4a81be5_plan_progress_task_5, stack_media_media3, stack_ui [INFERRED 0.70]

## Communities (26 total, 10 thin omitted)

### Community 0 - "layout"
Cohesion: 0.13
Nodes (25): dragSelection(), BlinkingCaret(), DocumentBody(), Color, Modifier, TextStyle, stringWidth(), caretPosition() (+17 more)

### Community 1 - "Piece"
Cohesion: 0.12
Nodes (31): android, Media, MediaKind, FILE, IMAGE, VIDEO, Piece, decodeImage() (+23 more)

### Community 2 - "NotesViewModel.kt"
Cohesion: 0.13
Nodes (17): DockedComposer(), androidx, Bundle, ComponentActivity, Modifier, MainActivity, NotesScreen(), Context (+9 more)

### Community 3 - "idleEditor"
Cohesion: 0.09
Nodes (14): DraftStore, Cursor, jsonString(), parsePieces(), readContent(), readDraft(), writeDraft(), writePieces() (+6 more)

### Community 4 - "Composer.kt"
Cohesion: 0.16
Nodes (34): Activity, Alignment, activityOf(), Composer(), ComposerHit(), ComposerInputState, Dp, Modifier (+26 more)

### Community 5 - "NoteList.kt"
Cohesion: 0.22
Nodes (15): cardMediaWidth(), formatNoteTime(), CardBody(), HomeHeader(), androidx, Dp, Modifier, TextStyle (+7 more)

### Community 6 - "Note"
Cohesion: 0.07
Nodes (22): DebugNoteSeed, Kind, EDGE, LONG, MEDIUM, SHORT, VERY_LONG, Context (+14 more)

### Community 7 - "DocumentInputConnection"
Cohesion: 0.11
Nodes (10): ComposerInputElement, ComposerInputNode, DocumentInputConnection, BaseInputConnection, ExtractedText, ExtractedTextRequest, Job, ModifierNodeElement (+2 more)

### Community 9 - "Icons.kt"
Cohesion: 0.67
Nodes (9): ArrowUpIcon(), FileGlyph(), Color, Dp, Modifier, OverflowIcon(), PaperclipIcon(), SearchIcon() (+1 more)

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
- **20 isolated node(s):** `SHORT`, `MEDIUM`, `LONG`, `VERY_LONG`, `EDGE` (+15 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 62 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **10 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Piece` connect `Piece` to `layout`, `idleEditor`, `Composer.kt`, `NoteList.kt`, `Note`?**
  _High betweenness centrality (0.216) - this node is a cross-community bridge._
- **Why does `Editor` connect `Composer.kt` to `layout`, `NotesViewModel.kt`, `idleEditor`, `NoteList.kt`, `Note`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Why does `Note` connect `Note` to `NotesViewModel.kt`, `DebugNoteSeedTest`, `NoteList.kt`?**
  _High betweenness centrality (0.109) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `Composer()` (e.g. with `DocumentBody()` and `stringWidth()`) actually correct?**
  _`Composer()` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `idleEditor()` (e.g. with `finishInsert()` and `.draft_keepsTimestamp_andRejectsGarbage()`) actually correct?**
  _`idleEditor()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 10 inferred relationships involving `layout()` (e.g. with `normalize()` and `.caret_onWrappedLine_newline_andBlock()`) actually correct?**
  _`layout()` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `SHORT`, `MEDIUM`, `LONG` to the rest of the system?**
  _20 weakly-connected nodes found - possible documentation gaps or missing edges._