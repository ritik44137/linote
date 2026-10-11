# Graph Report - Linote  (2026-10-11)

## Corpus Check
- 40 files · ~20,990 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 367 nodes · 815 edges · 30 communities (15 shown, 12 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 70 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `898f29d7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Composer
- MediaPreview.kt
- idleEditor
- Cursor
- Composer.kt
- NoteList.kt
- NotesViewModel.kt
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
- ComposerInputNode
- ComposerChromeTest

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

## Communities (30 total, 12 thin omitted)

### Community 0 - "Composer"
Cohesion: 0.13
Nodes (28): Composer(), dragSelection(), Modifier, BlinkingCaret(), DocumentBody(), Color, Modifier, TextStyle (+20 more)

### Community 1 - "MediaPreview.kt"
Cohesion: 0.12
Nodes (29): android, MediaKind, FILE, IMAGE, VIDEO, decodeImage(), FileFallback(), Icon (+21 more)

### Community 2 - "idleEditor"
Cohesion: 0.19
Nodes (3): idleEditor(), EditorTest, SaveTest

### Community 3 - "Cursor"
Cohesion: 0.16
Nodes (8): Cursor, jsonString(), parsePieces(), readContent(), readDraft(), writePieces(), noteFrom(), DocumentJsonTest

### Community 4 - "Composer.kt"
Cohesion: 0.13
Nodes (37): Activity, activityOf(), ComposerChrome, ComposerInputState, Dp, showImeNow(), DraftStore, writeDraft() (+29 more)

### Community 5 - "NoteList.kt"
Cohesion: 0.13
Nodes (26): Note, cardMediaWidth(), formatNoteTime(), NotesScreen(), atNewestEdge(), CardBody(), FollowNewest(), HomeHeader() (+18 more)

### Community 6 - "NotesViewModel.kt"
Cohesion: 0.10
Nodes (20): Alignment, ComposerHit(), finishInsert(), sameDocument(), DockedComposer(), androidx, Bundle, ComponentActivity (+12 more)

### Community 7 - "DocumentInputConnection"
Cohesion: 0.21
Nodes (4): DocumentInputConnection, BaseInputConnection, ExtractedText, ExtractedTextRequest

### Community 8 - "DebugNoteSeed"
Cohesion: 0.09
Nodes (17): DebugNoteSeed, Kind, EDGE, LONG, MEDIUM, SHORT, VERY_LONG, Context (+9 more)

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

### Community 28 - "ComposerInputNode"
Cohesion: 0.22
Nodes (6): ComposerInputElement, ComposerInputNode, Job, ModifierNodeElement, Node, PlatformTextInputModifierNode

## Knowledge Gaps
- **24 isolated node(s):** `SHORT`, `MEDIUM`, `LONG`, `VERY_LONG`, `EDGE` (+19 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 75 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **12 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Piece` connect `Composer.kt` to `Composer`, `MediaPreview.kt`, `Cursor`, `NoteList.kt`, `DebugNoteSeed`?**
  _High betweenness centrality (0.198) - this node is a cross-community bridge._
- **Why does `Editor` connect `Composer.kt` to `Composer`, `idleEditor`, `Cursor`, `NoteList.kt`, `NotesViewModel.kt`?**
  _High betweenness centrality (0.151) - this node is a cross-community bridge._
- **Why does `Note` connect `NoteList.kt` to `DebugNoteSeed`, `DebugNoteSeedTest`, `Cursor`, `NotesViewModel.kt`?**
  _High betweenness centrality (0.106) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `Composer()` (e.g. with `DocumentBody()` and `stringWidth()`) actually correct?**
  _`Composer()` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `idleEditor()` (e.g. with `finishInsert()` and `.draft_keepsTimestamp_andRejectsGarbage()`) actually correct?**
  _`idleEditor()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 10 inferred relationships involving `layout()` (e.g. with `normalize()` and `.caret_onWrappedLine_newline_andBlock()`) actually correct?**
  _`layout()` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `SHORT`, `MEDIUM`, `LONG` to the rest of the system?**
  _24 weakly-connected nodes found - possible documentation gaps or missing edges._