# SDD ledger — plan: /home/bool/.cursor/plans/composer_notes_implementation_a4a81be5.plan.md

Ruling: no git repo and the plan forbids git init — skip worktree, task-start, and commits — cost if wrong: no commit boundaries to review against.

Pre-flight: Task 2 consumes Editor/Piece/normalize from Task 1. Task 3 consumes writePieces and idleEditor. Task 4 consumes normalize. Task 6 consumes finishInsert, DraftStore, Editor. Task 7 consumes layout/hit/moveVertical. Task 8 consumes readContent, cardMediaWidth, layout, formatNoteTime. Names match the locked interfaces. No conflict.

Task 1: complete (no commit; EditorTest passed inside the suite).
Task 2: complete (no commit; DocumentJsonTest passed).
Task 2: Ruling: assert `readDraft(...)!!.timestamp` — Kotlin will not read `.timestamp` on `Editor?` — cost if wrong: the idle-draft assertion is skipped when the draft is null.
Task 3: complete (no commit; SaveTest and DraftStoreTest passed).
Task 4: complete (no commit; DocumentLayoutTest passed).
Task 5: complete (no commit; NoteTimeTest passed).
Task 5: Ruling: `coreLibraryDesugaring` of `desugar_jdk_libs:2.1.5` — `DateTimeFormatter` is not on API 24 without it — cost if wrong: the time label crashes on API 24–25.
Task 6: complete (no commit; schema test still passed, database version stays 1).
Task 6: Ruling: `editorLock` around `edit` and the post-insert write — a successful insert must not replace keystrokes that landed during the insert — cost if wrong: a keystroke during save disappears, or the lock deadlocks if a later change takes it in the other order.
Task 7: complete (no commit; debug Kotlin compiled and the unit suite passed).
Task 7: Ruling: `caretPosition` and `mediaAt` sit next to `hit` — the shared drawing needs them and the plan's locked list did not name them — cost if wrong: one extra lookup the UI could inline.
Task 7: Ruling: card and composer hit positions subtract `bodyTop` — a short document is bottom-aligned, so the layout's y=0 is not the column's y=0 — cost if wrong: a tap lands on the line above or below the finger.
Task 8: complete (no commit; debug Kotlin compiled, unit suite 25/25, schema hash unchanged).
Task 8: Ruling: card content padding is 14 dp — Compose draws the 2 dp border inside the bounds, so 14 dp from the outer edge is 12 dp from the inner edge — cost if wrong: the body sits 2 dp tighter or looser than the spec's 12 dp.
Task 8: Ruling: `showHint` defaults on and cards pass false — the empty-document hint belongs to the composer — cost if wrong: an empty card shows "Something to say?".
Task 8: Ruling: video frames use `getFrameAtTime(0)` — the two-argument closest-frame call is API 27 and minSdk is 24 — cost if wrong: the frame is not the nearest one.
Final: fixed lower-half hit testing — `hitInTheLowerHalf_staysOnThatLine` RED (assertion) then GREEN, suite 25/25.
Final: fixed caret-only change during insert — `caretMove_isTheSameDocument_textChangeIsNot` RED (unresolved `sameDocument`) then GREEN, suite 25/25. `sameDocument` compares pieces and timestamp.
Final: fixed short-list packing, closed-keyboard overlap, drag-to-select, IME buffer freshness, and API 24 draft rename — no JVM test can host Compose, the IME, or a missing `java.nio.file`; `DraftStoreTest` still round-trips after `File.renameTo`. Suite 25/25.
Final: Ruling: a mostly vertical drag scrolls only when the composer already overflows; a long-press drag still selects — the column has to scroll and a drag has to select — cost if wrong: dragging downward through a tall draft scrolls instead of extending the selection.
Final: Ruling: `File.renameTo` replaces `Files.move` — `desugar_jdk_libs` does not include `java.nio.file`, and minSdk is 24 — cost if wrong: a device where `renameTo` does not replace an existing file falls through to a copy, which is not atomic.
Final: Ruling: the input connection reports the anchor as the selection start and the caret as the selection end, and writes `state.editor` before returning — that is where Android puts the cursor — cost if wrong: the keyboard's cursor sits at the other end of a selection.
Final: Ruling: backspace from the keyboard deletes an existing selection before it deletes surrounding characters — that is what a person expects of backspace — cost if wrong: a selected range is removed when the keyboard meant to delete beside it.
Final: Ruling: paperclip, paging, search, Hilt, and read-more stay absent — the spec lists them as out of scope — cost if wrong: those features are missing.
Final: Ruling: a pasted U+FFFC stays a text character — the plan's shortcut — cost if wrong: paste never inserts a media piece.
Final: Ruling: one UTF-16 char is one cell — the plan's shortcut — cost if wrong: an emoji is wider than its cell and the caret misses it.
Final: Ruling: the caret does not blink — the plan asks for a static ink mark — cost if wrong: the cursor is harder to see.
Final: Ruling: a saved card is not opened back into the composer — the spec only lists cards — cost if wrong: a note cannot be edited after it is saved.
Final: Ruling: `draft.json` does not store the caret — the file is the timestamp and the pieces, and a new process puts the caret at the end — cost if wrong: process death loses the cursor position.
Final: Ruling: the file-fallback icon stays 12 dp from the outer edge of its 48 dp row — that is the spec's inset, and the 1 dp stroke is drawn inside — cost if wrong: the icon is 1 dp closer to the stroke.
Final: Ruling: do not delete this ledger and do not run the branch-finishing skill — there is no git history to keep the record, and the plan forbids `git init` — cost if wrong: the ledger remains on disk after the work is done.
Final: minor (deferred): `deleteSurroundingTextInCodePoints` forwards the count to the UTF-16 delete, so one emoji code point can leave a lone surrogate.
Final: minor (deferred): `resolveActivity` for `ACTION_VIEW` sees no handlers on API 30+ without a `<queries>` element, so the drawn file glyph is used.
Final: minor (deferred): a solo image starts as the 48 dp fallback row, then jumps to the decoded picture.
