# Graph Report - Linote  (2026-10-11)

## Corpus Check
- 38 files · ~19,381 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 332 nodes · 738 edges · 27 communities (14 shown, 10 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 62 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Composer
- MediaPreview.kt
- idleEditor
- Piece
- Composer.kt
- NoteList.kt
- DebugNoteSeed
- DocumentInputConnection
- ComposerInputNode
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
1. `Editor` - 32 edges
2. `Composer()` - 23 edges
3. `Piece` - 23 edges
4. `idleEditor()` - 22 edges
5. `Note` - 17 edges
6. `layout()` - 17 edges
7. `DocumentInputConnection` - 14 edges
8. `NotesViewModel` - 14 edges
9. `DebugNoteSeedTest` - 14 edges
10. `DocumentBody()` - 13 edges

## Surprising Connections (you probably didn't know these)
- `Composer Notes Specification` --references--> `Jetpack Compose`  [INFERRED]
  docs/superpowers/specs/2026-10-10-composer-notes-design.md → stack.md
- `Task 6: Editor Locking` --conceptually_related_to--> `Room over SQLite`  [INFERRED]
  .superpowers/sdd/composer_notes_implementation_a4a81be5.plan/progress.md → stack.md
- `Composer()` --calls--> `ArrowUpIcon()`  [INFERRED]
  app/src/main/java/com/linote/Composer.kt → app/src/main/java/com/linote/Icons.kt
- `Composer()` --calls--> `PaperclipIcon()`  [INFERRED]
  app/src/main/java/com/linote/Composer.kt → app/src/main/java/com/linote/Icons.kt
- `NotesScreen()` --calls--> `Composer()`  [INFERRED]
  app/src/main/java/com/linote/MainActivity.kt → app/src/main/java/com/linote/Composer.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Composer Implementation Flow** — composer_notes_design, composer_viewmodel, composer_document_model, composer_draft_json [EXTRACTED 1.00]
- **Android Compatibility Strategy** — superpowers_sdd_composer_notes_implementation_a4a81be5_plan_progress_task_5, stack_media_media3, stack_ui [INFERRED 0.70]

## Communities (27 total, 10 thin omitted)

### Community 0 - "Composer"
Cohesion: 0.13
Nodes (28): Composer(), dragSelection(), Dp, Modifier, DocumentBody(), Color, Modifier, stringWidth() (+20 more)

### Community 1 - "MediaPreview.kt"
Cohesion: 0.12
Nodes (27): android, MediaKind, FILE, IMAGE, VIDEO, decodeImage(), FileFallback(), Icon (+19 more)

### Community 2 - "idleEditor"
Cohesion: 0.08
Nodes (18): DraftStore, Note, idleEditor(), finishInsert(), noteFrom(), sameDocument(), Context, NotesViewModel (+10 more)

### Community 3 - "Piece"
Cohesion: 0.15
Nodes (12): Cursor, jsonString(), parsePieces(), readContent(), readDraft(), writeDraft(), writePieces(), cardMediaWidth() (+4 more)

### Community 4 - "Composer.kt"
Cohesion: 0.19
Nodes (28): Activity, activityOf(), ComposerInputState, showImeNow(), Atom, backspace(), Ch, commitText() (+20 more)

### Community 5 - "NoteList.kt"
Cohesion: 0.14
Nodes (18): Alignment, ComposerHit(), formatNoteTime(), Bundle, ComponentActivity, MainActivity, NotesScreen(), CardBody() (+10 more)

### Community 6 - "DebugNoteSeed"
Cohesion: 0.09
Nodes (17): DebugNoteSeed, Kind, EDGE, LONG, MEDIUM, SHORT, VERY_LONG, Context (+9 more)

### Community 7 - "DocumentInputConnection"
Cohesion: 0.21
Nodes (4): DocumentInputConnection, BaseInputConnection, ExtractedText, ExtractedTextRequest

### Community 8 - "ComposerInputNode"
Cohesion: 0.22
Nodes (6): ComposerInputElement, ComposerInputNode, Job, ModifierNodeElement, Node, PlatformTextInputModifierNode

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
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 63 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **10 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Piece` connect `Piece` to `Composer`, `MediaPreview.kt`, `Composer.kt`, `NoteList.kt`, `DebugNoteSeed`?**
  _High betweenness centrality (0.204) - this node is a cross-community bridge._
- **Why does `Editor` connect `Composer.kt` to `Composer`, `idleEditor`, `Piece`, `NoteList.kt`?**
  _High betweenness centrality (0.149) - this node is a cross-community bridge._
- **Why does `Note` connect `idleEditor` to `DebugNoteSeedTest`, `NoteList.kt`, `DebugNoteSeed`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `Composer()` (e.g. with `DocumentBody()` and `stringWidth()`) actually correct?**
  _`Composer()` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `idleEditor()` (e.g. with `finishInsert()` and `.draft_keepsTimestamp_andRejectsGarbage()`) actually correct?**
  _`idleEditor()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **What connects `SHORT`, `MEDIUM`, `LONG` to the rest of the system?**
  _20 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Composer` be split into smaller, more focused modules?**
  _Cohesion score 0.1282051282051282 - nodes in this community are weakly interconnected._