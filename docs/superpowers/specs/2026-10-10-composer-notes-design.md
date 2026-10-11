# Composer notes

The bottom bar is a composer. Finishing a draft inserts one `Note`. Notes appear as cards above the composer, newest at the bottom. The Room table does not change. Media can be represented and laid out now. The paperclip does not insert media.

## Document

A document is an ordered list of pieces.

- A text piece holds a string.
- A media piece is an image, a video, or a file. It has a UUID, a file name, a kind (`image`, `video`, or `file`), and a source string the preview can open.

The idle document is one empty text piece. Adjacent text pieces are merged. An empty text piece exists only when it is the only piece.

The keyboard edits a flat string of that document. Each text piece contributes its characters. Each media piece contributes one U+FFFC object character. The cursor and the selection are indexes into this string, and the cursor may sit at either end. U+FFFC is not drawn as a glyph.

Typing, a line break, and paste insert plain text at the cursor. Paste inserts the clipboard’s plain text. Enter inserts a line break and does not save.

Backspace immediately after a media piece, forward delete immediately before one, or deleting a selection that covers one removes that whole piece. The remaining pieces keep their order, and the text closes up.

A long-press on a word selects that word. A long-press on a media piece selects that piece. A drag extends the selection. A selection that covers a piece’s object character includes the whole piece.

An IME composition is an underlined range inside a text piece. It commits as ordinary text. A composition does not include an object character; an edit that would is committed first.

## Timestamp and save

These characters do not start a timestamp and do not allow a save: U+0020 space, U+000A line feed, U+000D carriage return. Every other character does, including a tab. A media piece does too.

The timestamp is recorded at the moment the document first contains such a character or a media piece. It is epoch milliseconds. If every such character and every media piece is removed, the timestamp is cleared immediately, even when spaces or line breaks remain.

Any character, or any media piece, hides “Something to say?” and shows the arrow-up. The arrow-up stays while any character or media piece remains. Its tap inserts a note only while a timestamp exists. A draft of only spaces or line breaks shows the arrow-up, and the tap does nothing. A draft that is only a media piece can be saved.

A successful insert sets `createdAt` and `modifiedAt` to that timestamp, then clears the document. The hint and the pen return. The keyboard stays open.

## Layout

The text column is the composer’s interior between the icon padding. Pieces are placed from top to bottom.

Text before a media piece uses the full column and ends above that piece. A preview block is a 96 by 96 dp box with 8 dp corners, then 2 dp, then the file name. The name is 11 sp monospace in `ink_faint` (`#9C8449`), one line, the width of the box, with an ellipsis when it is longer. The following text starts at the top of the block, 12 dp to its right, and wraps in that column. The next full-width line starts under the file name.

A later media piece starts on the left, immediately under the file name already placed. The text after it wraps the same way. When the right-hand column is narrower than one character, that text starts under the file name.

Inside a 96 dp box:

- An image is scaled to fill the box and clipped to the corners.
- A video uses its first frame, scaled the same way.
- A file uses a preview when one can be drawn: the first page of a PDF, or a bitmap when the type is an image. Otherwise it uses the Android icon for that extension or MIME type.
- A picture or frame that cannot be decoded uses that same Android icon. When the platform has no icon, the app draws one file glyph.

A tap in text moves the cursor to the nearest character. A tap on a block places the cursor just after that block, where backspace removes it. Left and right move one flat-string position, so a block is one step. Up and down move one visual line and keep the same horizontal position, in the right-hand column or the full column as that line allows. When the width changes, the same document position is laid out again.

## Composer chrome

The closed bar is a 56 dp pill: `paper_3` (`#F2DDAA`) fill, 1 dp `rule` (`#D3BA82`) border, 28 dp from each screen edge, and 32 dp above the navigation bar. The paperclip is on the left and the pen is on the right. Both are 22 dp, ink (`#4A3610`), drawn with the same stroke as the existing pen, and inset 22 dp from the pill’s side. The text column has 12 dp of clearance from each icon. The paperclip has no action.

“Something to say?” is 15 sp monospace in `ink_dim` (`#7D6528`). Typed text is 15 sp monospace in `ink_hot` (`#1C1203`). The cursor is ink. The hint and the cursor share the empty focused field, with the cursor at the start.

A tap focuses the composer, opens the keyboard, and shows the cursor. The keyboard lifts the screen. The 32 dp bottom margin is used only while the keyboard is closed. While it is open, the composer sits on the top edge of the keyboard. System back while the keyboard is open closes it and hides the cursor. The draft stays. System back with the keyboard closed leaves the app.

The text column keeps its side inset for the full height. The icons stay in the bottom 56 dp band while new lines appear above them. One line keeps the pill. Past one line the corners are 8 dp.

Let `L` be the line height of the 15 sp monospace face. The 56 dp bar already holds one line and the vertical padding. Each further line adds `L`. The composer grows through five lines, to `56 dp + 4L`, and then the text column scrolls inside it. A media block uses that same cap. Editing keeps the cursor in view. The icons do not scroll.

An empty composer shows the hint and the pen. The arrow-up is drawn in the pen’s place and in the pen’s style. Its hit target saves when a timestamp exists, and does nothing otherwise. Tapping the text column moves the cursor.

A process start shows a restored draft unfocused, with the keyboard closed and no cursor. A draft with any character or media piece shows the arrow-up. A configuration change keeps the keyboard, cursor, and selection.

## Draft file and notes

The draft file is `draft.json` in the app files directory. It is rewritten on each change by writing a temporary file in that directory and renaming it over `draft.json`. An empty document deletes the file. A file that cannot be read opens an empty composer, and the next edit replaces the file.

```json
{
  "timestamp": 1700000000123,
  "pieces": [
    {"type": "text", "value": "Hello"},
    {"type": "media", "kind": "image", "id": "00000000-0000-7000-8000-000000000001", "name": "forest.jpg", "source": "file:///path"}
  ]
}
```

`timestamp` is a number or `null`. A text piece has `type` and `value`. A media piece has `type`, `kind`, `id`, `name`, and `source`.

`Note.content` stores the pieces array only, without the timestamp. The Room columns stay as they are. A `content` string is a document when it parses as a JSON array whose every element is a valid text or media piece. Any other string, including invalid JSON, is one text piece. A note whose text is itself a valid pieces array is read back as a document.

The screen state lives in a `ViewModel`: the document, the flat-string selection, the timestamp, and whether the composer is focused. The `ViewModel` survives a configuration change. A new process creates a new `ViewModel`, which loads `draft.json` with focus cleared.

The arrow-up insert writes one row:

- `uuid` is a UUID v7 for the timestamp
- `createdAt` and `modifiedAt` are the timestamp
- `title` and `referencedNoteID` are null
- `content` is the pieces array

If the insert fails, the composer and `draft.json` stay as they are. After a successful insert the file is deleted and the composer is cleared.

Notes are listed with `createdAt` descending, then `uuid` descending, so equal timestamps stay in a stable order.

## Note list

Cards sit above the composer. The newest card is at the bottom, 8 dp above the composer, and the app opens with that card in view. Older cards are 8 dp apart, reached by scrolling up. Each card is inset 28 dp from the screen edges. The list clears the status bar and adds no extra top padding. While the newest card is in view, a new save appears at the bottom. While older cards are in view, the scroll stays where it is. An empty database shows the paper background and the composer.

A card is `paper_2` (`#F8E9C1`) with a 2 dp `rule` border and 8 dp corners. It has 12 dp of padding on the left, top, and right, and 12 dp under the time. The time is on its own line under the body, 12 dp from the right edge. The time is `createdAt` in the device zone, formatted `h:mm a` with English `AM`/`PM` markers, so 17:04 is `5:04 PM`, midnight is `12:00 AM`, and noon is `12:00 PM`. The time is 11 sp monospace in `ink_faint`. The card body is 15 sp monospace in ink. The whole body is shown.

## Media on a card

The card’s content width is its width inside the 2 dp border and the 12 dp side padding. A note with one media piece and no characters shows that piece across the content width.

- An image, a video’s first frame, or a file preview keeps its aspect ratio. The card grows with the picture. The picture is the whole block.
- A file with no preview, and a solo image or video that cannot be decoded, is a rectangle of that width and 48 dp tall, with 8 dp corners, a 1 dp `rule` border, and a `paper` (`#FDF3D5`) fill. Inside, 12 dp from the left, is a 24 dp Android icon for the extension, or the drawn file glyph when the platform has none, then 8 dp, then the file name at 15 sp monospace in ink, vertically centered, one line, with an ellipsis when it is longer.

A note that contains any character, including spaces, or more than one media piece, uses the composer preview for every piece: the 96 dp box, the file name underneath, and the text wrapping beside it.

## Tests

JVM tests, beside the existing schema test:

- The timestamp starts on the first non-space, non-line-break character, and on a media piece. It clears when none remain. Spaces and line breaks alone do not save. A media-only draft does.
- Backspace, forward delete, and a covering selection each remove a media piece.
- The pieces array round-trips. A plain `content` string reads as one text piece.
- Layout places the following text beside a block and the next line under its file name. A second block starts under that name.
- One media piece and no characters lays out at the card’s content width. Any character, or a second piece, uses the 96 dp preview.
- A fixed instant in a fixed zone formats as `5:04 PM`.

The keyboard, system back, and a killed process are checked manually on a device. This change adds no instrumented UI test.

## Out of scope

The paperclip does not open a picker. This change adds no paging, search index, Hilt graph, or “read more” control. `title` and `referencedNoteID` stay null. Paste does not insert media.
