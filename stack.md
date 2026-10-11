Here’s the stack I’d use:

UI: Jetpack Compose, with LazyColumn for the timeline
Database: Room over SQLite, with an @Fts4 table for full-text search (skip Realm, since MongoDB has deprecated it)
Pagination: Paging 3, which plugs straight into Room (its insertSeparators is handy for day headers)
Media: Coil for images, Media3 (ExoPlayer) for video, and the system Photo Picker for choosing files, which needs no storage permission
Rich text: compose-rich-editor for bold/italic/underline, or a plain EditText with spans inside AndroidView if you hit input quirks. This is the least settled part of the stack, so prototype it first.
Plumbing: ViewModel, Coroutines/Flow, Hilt
Performance tooling: Baseline Profiles and Macrobenchmark

The libraries matter less than a few design decisions:

Reverse the list. Use LazyColumn(reverseLayout = true) and query notes newest-first. Index 0 then sits at the bottom, so the app opens on the latest note with no scrolling. Older pages load in above without the view jumping. It’s the standard chat-app trick.
Never load everything. Page from Room, index the timestamp column, and key list items by note ID. Show long notes as a truncated preview with “Read more,” as WhatsApp does.
Separate text from formatting. Store each note’s plain text plus a list of style ranges (e.g. “bold, characters 4–11”) rather than HTML. The plain text feeds the search index cleanly, and you can build the styled text off the main thread. You can also swap editor libraries later without migrating data. Only one editor is ever on screen, so it needs to be robust rather than fast, while the timeline just renders read-only text.
Keep media cheap. Copy files into app storage and generate a small thumbnail on insert, so the list only ever decodes thumbnails. Load full-size images and video players on tap. Model a note as a list of blocks (text, image, video) instead of embedding images inside the text, which is far simpler to build and render.
Plan for search jumps. Showing results is easy, but tapping one and landing in the middle of the timeline is the tricky part. Count how many notes are newer than the match, then start a new Pager at that position via initialKey.