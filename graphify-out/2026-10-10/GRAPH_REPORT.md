# Graph Report - Linote  (2026-10-10)

## Corpus Check
- 33 files · ~13,596 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 274 nodes · 622 edges · 25 communities (14 shown, 8 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 61 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Composer.kt
- MediaPreview.kt
- NotesViewModel.kt
- Piece
- Editor
- NoteList.kt
- idleEditor
- DocumentInputConnection
- ComposerInputNode
- Icons.kt
- LinoteDatabase.kt
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

## God Nodes (most connected - your core abstractions)
1. `Editor` - 32 edges
2. `idleEditor()` - 22 edges
3. `Composer()` - 21 edges
4. `Piece` - 21 edges
5. `layout()` - 17 edges
6. `DocumentInputConnection` - 14 edges
7. `NotesViewModel` - 14 edges
8. `DocumentBody()` - 13 edges
9. `DocLayout` - 13 edges
10. `decodeImage()` - 12 edges

## Surprising Connections (you probably didn't know these)
- `Composer Notes Specification` --references--> `Jetpack Compose`  [INFERRED]
  docs/superpowers/specs/2026-10-10-composer-notes-design.md → stack.md
- `Task 6: Editor Locking` --conceptually_related_to--> `Room over SQLite`  [INFERRED]
  .superpowers/sdd/composer_notes_implementation_a4a81be5.plan/progress.md → stack.md
- `NotesScreen()` --calls--> `Composer()`  [INFERRED]
  app/src/main/java/com/linote/MainActivity.kt → app/src/main/java/com/linote/Composer.kt
- `HomeHeader()` --calls--> `SearchIcon()`  [INFERRED]
  app/src/main/java/com/linote/NoteList.kt → app/src/main/java/com/linote/Icons.kt
- `HomeHeader()` --calls--> `OverflowIcon()`  [INFERRED]
  app/src/main/java/com/linote/NoteList.kt → app/src/main/java/com/linote/Icons.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Composer Implementation Flow** — composer_notes_design, composer_viewmodel, composer_document_model, composer_draft_json [EXTRACTED 1.00]
- **Android Compatibility Strategy** — superpowers_sdd_composer_notes_implementation_a4a81be5_plan_progress_task_5, stack_media_media3, stack_ui [INFERRED 0.70]

## Communities (25 total, 8 thin omitted)

### Community 0 - "Composer.kt"
Cohesion: 0.11
Nodes (36): Alignment, Composer(), ComposerHit(), ComposerInputState, dragSelection(), Dp, Modifier, DocumentBody() (+28 more)

### Community 1 - "MediaPreview.kt"
Cohesion: 0.12
Nodes (27): android, MediaKind, FILE, IMAGE, VIDEO, decodeImage(), FileFallback(), Icon (+19 more)

### Community 2 - "NotesViewModel.kt"
Cohesion: 0.15
Nodes (15): sameDocument(), MainActivity, NotesScreen(), Context, NotesViewModel, Factory, Screen, Bundle (+7 more)

### Community 3 - "Piece"
Cohesion: 0.14
Nodes (15): Cursor, jsonString(), parsePieces(), readContent(), readDraft(), writeDraft(), writePieces(), cardMediaWidth() (+7 more)

### Community 4 - "Editor"
Cohesion: 0.20
Nodes (21): DraftStore, Atom, backspace(), Ch, commitText(), deleteForward(), Editor, explode() (+13 more)

### Community 5 - "NoteList.kt"
Cohesion: 0.14
Nodes (11): Note, NoteDao, formatNoteTime(), HomeHeader(), Dp, Modifier, NoteCard(), NoteList() (+3 more)

### Community 6 - "idleEditor"
Cohesion: 0.18
Nodes (5): idleEditor(), finishInsert(), noteFrom(), EditorTest, SaveTest

### Community 7 - "DocumentInputConnection"
Cohesion: 0.21
Nodes (4): DocumentInputConnection, BaseInputConnection, ExtractedText, ExtractedTextRequest

### Community 8 - "ComposerInputNode"
Cohesion: 0.22
Nodes (6): ComposerInputElement, ComposerInputNode, Job, ModifierNodeElement, Node, PlatformTextInputModifierNode

### Community 9 - "Icons.kt"
Cohesion: 0.48
Nodes (11): ComposerAction(), Color, ArrowUpIcon(), FileGlyph(), Color, Dp, Modifier, OverflowIcon() (+3 more)

### Community 11 - "LinoteDatabase.kt"
Cohesion: 0.38
Nodes (4): Context, LinoteDatabase, UuidConverters, RoomDatabase

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
- **15 isolated node(s):** `Icon`, `IMAGE`, `VIDEO`, `FILE`, `Media` (+10 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 53 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Editor` connect `Editor` to `Composer.kt`, `NotesViewModel.kt`, `Piece`, `NoteList.kt`, `idleEditor`?**
  _High betweenness centrality (0.212) - this node is a cross-community bridge._
- **Why does `Piece` connect `Piece` to `Composer.kt`, `MediaPreview.kt`, `Editor`, `NoteList.kt`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **Why does `idleEditor()` connect `idleEditor` to `Composer.kt`, `NotesViewModel.kt`, `Piece`, `Editor`?**
  _High betweenness centrality (0.092) - this node is a cross-community bridge._
- **Are the 13 inferred relationships involving `idleEditor()` (e.g. with `finishInsert()` and `.draft_keepsTimestamp_andRejectsGarbage()`) actually correct?**
  _`idleEditor()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 3 inferred relationships involving `Composer()` (e.g. with `DocumentBody()` and `stringWidth()`) actually correct?**
  _`Composer()` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 7 inferred relationships involving `layout()` (e.g. with `normalize()` and `.hitInTheLowerHalf_staysOnThatLine()`) actually correct?**
  _`layout()` has 7 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Icon`, `IMAGE`, `VIDEO` to the rest of the system?**
  _15 weakly-connected nodes found - possible documentation gaps or missing edges._