package com.linote.debug

import android.content.Context
import androidx.room.withTransaction
import com.linote.data.Note
import com.linote.data.NoteDao
import com.linote.data.linoteDatabase
import com.linote.editor.Piece
import com.linote.editor.writePieces
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Random
import java.util.UUID

object DebugNoteSeed {
    const val SEED = 20261011L

    fun notes(): List<Note> {
        val kindOf = mixedKinds()
        val used = HashSet<String>()
        val seen = IntArray(Kind.entries.size)
        val span = END - START
        return List(COUNT) { index ->
            val kind = kindOf[index]
            val n = seen[kind.ordinal]++
            val text = when (kind) {
                Kind.EDGE -> edgeText(n).also { check(used.add(it)) { "duplicate edge note" } }
                else -> uniqueText(target(kind, n), index, used)
            }
            val createdAt = START + span * index / (COUNT - 1)
            val modifiedAt = if (index % 4 == 0) minOf(END, createdAt + DAY) else createdAt
            Note(
                uuid = uuidV7(createdAt, index),
                createdAt = createdAt,
                modifiedAt = modifiedAt,
                content = writePieces(listOf(Piece.Text(text))),
                title = null,
                referencedNoteID = null,
            )
        }
    }

    fun seedWork(notes: List<Note>, existing: Set<UUID>, replace: Boolean): SeedWork {
        // shortcut: a handwritten note whose uuid lands in this fixed set is treated as synthetic, add a marker column if that ever happens
        val present = notes.map { it.uuid }.filter { it in existing }
        if (present.isNotEmpty() && !replace) {
            return SeedWork("Already seeded. Left every note in place.", emptyList(), emptyList())
        }
        if (present.isEmpty()) {
            return SeedWork("Inserted ${notes.size} notes.", emptyList(), notes)
        }
        val noun = if (present.size == 1) "note" else "notes"
        return SeedWork(
            "Replaced ${present.size} synthetic $noun. Left other notes in place.",
            present,
            notes,
        )
    }

    private enum class Kind { SHORT, MEDIUM, LONG, VERY_LONG, EDGE }

    private fun mixedKinds(): List<Kind> = buildList {
        repeat(200) { add(Kind.SHORT) }
        repeat(400) { add(Kind.MEDIUM) }
        repeat(250) { add(Kind.LONG) }
        repeat(100) { add(Kind.VERY_LONG) }
        repeat(50) { add(Kind.EDGE) }
    }.toMutableList().also { it.shuffle(Random(SEED)) }

    private fun target(kind: Kind, n: Int): Int = when (kind) {
        Kind.SHORT -> if (n < 20) 1 else 2 + (n % 9)
        Kind.MEDIUM -> 11 + (n % 30)
        Kind.LONG -> 41 + (n % 60)
        Kind.VERY_LONG -> 101 + (n % 150)
        Kind.EDGE -> error("edge notes have their own text")
    }

    private fun uniqueText(target: Int, index: Int, used: MutableSet<String>): String {
        var cursor = index
        repeat(64) {
            val text = compose(target, cursor)
            if (used.add(text)) return text
            cursor += 31
        }
        error("no unique note of $target words at $index")
    }

    private fun compose(target: Int, cursor: Int): String {
        val parts = ArrayList<String>()
        var left = target
        var slot = cursor
        while (left > 0) {
            val choices = counted.filter { it.second <= left }
            check(choices.isNotEmpty()) { "no phrase fits $left words" }
            val phrase = choices[slot.mod(choices.size)]
            parts += phrase.first
            left -= phrase.second
            slot += 1 + cursor.mod(5)
        }
        return parts.joinToString(" ")
    }

    private fun uuidV7(timestampMillis: Long, index: Int): UUID {
        val randA = index and 0xfff
        val most = ((timestampMillis and 0xffff_ffff_ffffL) shl 16) or (0x7L shl 12) or randA.toLong()
        val least = ((index.toLong() shl 20) and 0x3fff_ffff_ffff_ffffL) or (1L shl 63)
        return UUID(most, least)
    }

    private fun edgeText(n: Int): String = when (n) {
        in 0..11 -> singleChars[n]
        in 12..23 -> punctuation[n - 12]
        in 24..36 -> multiline[n - 24]
        in 37..49 -> longWord(n - 37)
        else -> error("edge $n")
    }

    private fun longWord(n: Int): String {
        val token = heads[n] + LONG_TAIL
        check(token.length >= 60 && token.none { it.isWhitespace() })
        return "Left a scratch in the margin $token before I could talk myself out of keeping it."
    }
}

data class SeedWork(
    val message: String,
    val delete: List<UUID>,
    val insert: List<Note>,
)

// shortcut: chunks of 400 stay under SQLite's 999-variable cap on API 24, raise it if minSdk is 30+
private const val CHUNK = 400

suspend fun seedDatabase(context: Context, replace: Boolean): String = withContext(Dispatchers.IO) {
    val notes = DebugNoteSeed.notes()
    val db = linoteDatabase(context)
    val dao = db.noteDao()
    db.withTransaction {
        val existing = loadExisting(dao, notes)
        val work = DebugNoteSeed.seedWork(notes, existing, replace)
        work.delete.chunked(CHUNK).forEach { dao.deleteByUuids(it) }
        if (work.insert.isNotEmpty()) dao.insertAll(work.insert)
        if (work.insert.isNotEmpty()) {
            val present = loadExisting(dao, notes).size
            check(present == notes.size) { "expected ${notes.size} synthetic notes, found $present" }
        }
        work.message
    }
}

private suspend fun loadExisting(dao: NoteDao, notes: List<Note>): Set<UUID> {
    val found = mutableSetOf<UUID>()
    notes.map { it.uuid }.chunked(CHUNK).forEach { chunk ->
        found += dao.existingUuids(chunk)
    }
    return found
}

private const val COUNT = 1000
private const val DAY = 24L * 60 * 60 * 1000
private val START = Instant.parse("2026-04-01T12:00:00Z").toEpochMilli()
private val END = Instant.parse("2026-10-01T12:00:00Z").toEpochMilli()
private const val LONG_TAIL = "untilthethoughtfinallycooleddownenoughtoletgo"

private val singleChars = listOf("a", "I", "c", "e", "k", "m", "q", "r", "t", "w", "y", "z")
private val punctuation = listOf("...", "???", "!!!", "?!?", "---", "***", "...?", "!!?", "??!", ",,,", ";;;", "--?")
private val heads = listOf(
    "whydoikeepopeningthesametab",
    "hallwaylightaftermidnight",
    "oatsaisleandafoldedshoppinglist",
    "librarybookduebeforefriday",
    "kettleclickinthestillkitchen",
    "balconyrailinthewetcoldair",
    "notebookmarginhalfwritten",
    "trainwindowandthemissedstop",
    "sinkfullandthequietradio",
    "plantleaningintothewindowglass",
    "voicenoteleftunsentonpurpose",
    "saturdaymarketwithnorushleft",
    "stairwellthatsmellslikerain",
)
private val multiline = listOf(
    "Rain on the skylight again.\nI keep meaning to write the dream about the station.\nThe mug is still by the sink.",
    "Grocery list, rewritten:\nmilk\nlemons\noats\nand whatever herb smells like the one from Tuesday.",
    "Question I did not ask out loud.\nWould the day feel shorter if I left the phone in the other room?",
    "The train sat between stations.\nSomeone was peeling an orange.\nI thought about the email and let it go for one more stop.",
    "Idea, before it leaves:\na small shelf by the door for keys, stamps, and the notebook I keep losing.",
    "Woke up convinced I had replied.\nI had not.\nThe draft is still sitting there with the cursor in the middle of sorry.",
    "Walked the long way home.\nThe bakery was out of the dark loaf.\nBought the plain one and ate the end on the steps.",
    "Reminder for the quiet hour.\nWater the basil.\nSend the photo of the chair.\nDo not open the work chat until the kettle is empty.",
    "The meeting could have been a sentence.\nI wrote that sentence on the walk back and it was kinder than what I almost said.",
    "Half a thought from the balcony.\nThe city sounds thinner after rain.\nI want to remember that without turning it into a task.",
    "Left this for tomorrow morning:\ncall the clinic,\nreturn the blue jacket,\nand decide whether the plant actually needs a bigger pot.",
    "Something about the kitchen light.\nIt hits the wall warmer after six.\nI stood there longer than the toast required.",
    "For myself, and not a lecture.\nYou can answer the message and still go to bed.\nThe reply does not have to solve the whole week.",
)

private val phrases = listOf(
    "The kettle clicked off before the kitchen got quiet.",
    "Need to return the library book before Friday.",
    "Saw the neighbor's dog waiting at the wrong door.",
    "What if the essay opened with the train instead?",
    "Felt lighter after leaving the group chat muted.",
    "Buy oats, lemons, and the cheap olive oil.",
    "Why does the hallway light feel colder at night?",
    "Remember to ask Sam about the spare key.",
    "The basil leaned toward the window again.",
    "Drafted the apology and then deleted the second paragraph.",
    "Walked home the long way past the closed bakery.",
    "Is the blue chair worth fixing or just replacing?",
    "Left the umbrella by the stairwell on purpose.",
    "A small idea: stamps, keys, and a hook by the door.",
    "The orange on the train smelled louder than the announcement.",
    "Should I write to Maya or wait until Sunday?",
    "Rain on the skylight made the afternoon feel shorter.",
    "Paid the phone bill and immediately forgot the confirmation.",
    "The soup needed salt and another ten minutes.",
    "Keep the receipt until the jacket is clearly mine.",
    "Woke up sure I had replied, and I had not.",
    "The plant might want a wider pot, not more water.",
    "Someone in the laundromat was reading the same novel.",
    "Call the clinic before they close on Thursday.",
    "I keep opening the draft and changing one adjective.",
    "The river path was muddier than it looked from the road.",
    "Put the charger in the bag so tomorrow starts less annoyed.",
    "What was the name of the herb from the Tuesday market?",
    "The meeting shrank to one sentence on the walk back.",
    "Fed the cat and then stood at the sink too long.",
    "Leave the work chat closed until the mug is empty.",
    "Found a ticket stub in the coat I almost donated.",
    "The balcony railing was cold enough to wake my hands.",
    "Maybe the story is about the missed stop, not the city.",
    "Text Jordan that the chair can be picked up Saturday.",
    "The radio in the other room was playing something familiar.",
    "Do not buy another notebook until this one is finished.",
    "Light through the curtain looked warmer than the clock suggested.",
    "Ask the baker whether the dark loaf returns next week.",
    "I left a voice note and then wished it had been shorter.",
    "The sink is full and the quiet is doing more than the dishes.",
    "Bring the blue jacket back before the receipt fades.",
    "A question for the morning: what can wait without guilt?",
    "The stairwell smelled like rain and somebody's laundry soap.",
    "Saved the tab and told myself I would answer it tomorrow.",
    "Pack the library card, not just the overdue book.",
    "The toast burned while I was staring at the wall.",
    "Would a shorter walk still count if I actually took it?",
    "Wrote three lines and liked the middle one best.",
    "The market was louder near the flowers than the fruit.",
    "Remember the clinic opens late on the first Monday.",
    "I almost sent the sharp version and closed the laptop instead.",
    "There is a crack in the mug I have decided to trust.",
    "Leave a note on the fridge about the basil, not about chores.",
    "The platform clock was two minutes kinder than my phone.",
    "What am I avoiding by reorganizing the desk drawer?",
    "Bought the plain loaf and ate the heel on the steps.",
    "The email can be one honest paragraph.",
    "Heard my name and it was for someone else.",
    "Set the alarm across the room so I have to stand up.",
    "The dream about the station faded before coffee.",
    "Return the jar and keep the rubber band, for some reason.",
    "I want the shelf by the door more than a new routine.",
    "The cat chose the warm laptop and ended the essay.",
    "Check whether the train still stops at the small station.",
    "A reminder with no lecture: water, reply, then go outside.",
    "The photo of the chair is still sitting in the camera roll.",
    "Why did I fold the shopping list into my coat?",
    "Stood in the aisle forgetting the reason for the list.",
    "The evening light hits the wall and makes the room look finished.",
    "Send the kinder sentence, not the thorough one.",
    "I am curious what the week looks like without the extra tab.",
    "Lock the door and also check that the keys are actually in my hand.",
    "The orange peel made a neat spiral and then I had nowhere to put it.",
    "Maybe rest is the task, and the inbox can survive it.",
    "Asked for the recipe and forgot to write down the second step.",
    "The bookstore cat was asleep on the travel section.",
    "Bring cash for the market in case the card reader sulks.",
    "I noticed the city sounding thinner after the rain.",
    "The draft apology is stuck on the word sorry.",
    "Would it help to name the feeling before answering?",
    "Left the window open a crack and the room smells like wet stone.",
    "Call mom from the walk, not from the couch.",
    "The cheap olive oil is fine for weeknight eggs.",
    "An idea that might be nothing: write the train scene first.",
    "I kept the stamp in my pocket until the corner bent.",
    "The neighbor waved and I waved back a second too late.",
    "Stretch before opening anything with a red badge on it.",
    "There was a child explaining pigeons to a patient adult.",
    "File the receipt where I will actually look in March.",
    "The kettle is loud enough to mark the end of deciding.",
    "What if I described the room instead of the mood?",
    "Bought lemons because the bowl looked unfinished.",
    "I sat on the steps until the loaf was gone.",
    "Remember that Thursday clinic slot, not the vague later.",
    "The laundromat novel had a cracked spine and no jacket.",
    "A quiet thought: the day does not need a concluding sentence.",
    "Wash the mug so tomorrow does not start with yesterday's ring.",
    "The missed stop would make a better ending than the arrival.",
    "Text the spare-key question before it becomes a whole saga.",
    "I heard the orange being peeled three seats away.",
    "Leave the phone in the other room for one song.",
    "The basil looks dramatic and is probably just thirsty.",
    "Why am I keeping the rubber band from the jar?",
    "The group chat can stay muted through dinner.",
    "Wrote the sentence I wished the meeting had been.",
    "Find the blue jacket before the return window closes.",
    "The stairwell light buzzes when someone is on the landing.",
    "An observation, nothing to solve: the wall turns gold at six.",
    "I deleted the second paragraph and the first one breathed.",
    "Pack tomorrow's charger tonight, future self is tired.",
    "The river path squelched and I was glad I wore the old shoes.",
    "Should the essay admit I missed the stop on purpose?",
    "The confirmation email is somewhere and the bill is paid.",
    "Ask whether the dark loaf comes back, then let it be.",
    "I stood at the sink because the water was warmer than the thought.",
    "Close the extra tabs and keep the one that is actually a letter.",
    "The dream left a station platform and nothing else.",
    "Bring the photo when I pick up the chair on Saturday.",
    "A casual inventory: keys, stamp, receipt, slight worry.",
    "The announcement was muffled and the orange was not.",
    "Water the plant, then decide about the pot.",
    "I want to remember the thin city sound without making it useful.",
    "The desk drawer is a delay tactic with better lighting.",
    "Reply in the morning when my voice is less sharp.",
    "The coat pocket had a ticket and a folded list.",
    "Is this restlessness or just the unread message?",
    "Left the umbrella where I will trip on it and remember.",
    "The novel in the laundromat was open to a middle chapter.",
    "Eat something before rewriting the opening again.",
    "The clock on the platform disagreed with me kindly.",
    "Send the chair photo and the time I can carry it.",
    "I almost organized the drawer instead of writing the line.",
    "The wet stone smell means the window did its job.",
    "Call from outside so the walk is not only for errands.",
    "The eggs will be fine with the ordinary oil.",
    "A reflection I do not need to post: muting it helped.",
    "The bent stamp still works and I am oddly proud of that.",
    "Someone else has my name and a louder friend.",
    "Set the alarm far away and forgive the first groan.",
    "The camera roll is full of chairs and one good shadow.",
    "What belongs in the note is the basil, not the lecture.",
    "I folded the list so small it stopped being a list.",
    "The aisle was bright and my reason for being there left.",
    "Gold light on the wall, toast getting away from me.",
    "Choose the kinder sentence even if it explains less.",
    "Curiosity is enough of a plan for the empty tab.",
    "Keys in hand, door locked, then a small sigh of relief.",
    "The orange peel had nowhere to go and neither did I.",
    "Inbox, you can wait until the kettle finishes.",
    "The second step of the recipe is still a rumor.",
    "Travel books made a bed for the bookstore cat.",
    "Cash in the inner pocket, card reader forgiven in advance.",
    "After rain the streets sound like they have more room.",
    "Sorry is stuck because I want it to do too much.",
    "Name the feeling, then the reply gets shorter.",
    "Wet stone, open window, no need to capture it better.",
    "Mom gets the walking version of the story, not the polished one.",
    "Weeknight eggs do not require a better bottle.",
    "Train scene first, mood second, if the idea survives lunch.",
    "The pocket stamp is a little ruin I am keeping.",
    "My wave arrived late and the neighbor did not mind.",
    "Stretch, then the red badges, in that order.",
    "Pigeons got a serious explanation and I slowed down to hear it.",
    "March me will not search the coat, so file the receipt now.",
    "When the kettle starts I am allowed to stop choosing.",
    "Describe the room and let the mood sneak in.",
    "The bowl looked lonely, so lemons.",
    "Heel of the loaf, cold step, no rush to go up.",
    "Thursday, clinic, actual time, not a foggy intention.",
    "No jacket on that novel, just a spine that had been loved.",
    "Not every day needs a last line to be finished.",
    "A clean mug is a modest gift to the morning.",
    "Missing the stop might be the scene worth keeping.",
    "Spare key, one question, send it before it grows.",
    "Three seats away an orange was the loudest thing.",
    "One song in the other room without the phone.",
    "Dramatic basil is usually just asking for water.",
    "The rubber band is staying and I refuse to justify it.",
    "Dinner is quieter with the chat left on mute.",
    "The meeting became a sentence and the sentence was enough.",
    "Return window, blue jacket, do it while the tag is still on.",
    "Buzzing stairwell light means someone is coming or going.",
    "Nothing to solve at six except noticing the gold.",
    "The first paragraph fits now that the second is gone.",
    "Tonight's charger is tomorrow's less annoyed pocket.",
    "Old shoes, muddy path, a walk that felt like a decision.",
    "Admit the missed stop if the essay can tell the truth.",
    "Paid, confirmed, and already unsure where I put the proof.",
    "Dark loaf next week is a question, not a project.",
    "Warm water won against the thought I was avoiding.",
    "One letter stays open and the rest of the tabs can go.",
    "Only a platform remained from the dream.",
    "Saturday, the chair, the photo, my two hands.",
    "Keys, stamp, receipt, and a worry small enough to carry.",
    "Muffled speakers, clear orange, a good trade.",
    "Water first, pot decision later, no drama for the plant.",
    "Useful is not the same as worth remembering.",
    "The drawer can wait; the line cannot.",
    "Morning voice, shorter reply, less armor.",
    "Ticket stub, folded paper, a coat that keeps secrets.",
    "Maybe it is the message and not my mood.",
    "Trip on the umbrella, remember the rain, fair system.",
    "Middle chapter, public washing machine, private book.",
    "Food before another opening paragraph, please.",
    "The platform gave me two minutes I had not earned.",
    "Photo, time, arms, chair, send the practical text.",
    "Writing the line beat organizing the pencils.",
    "The room smells like outside and that is the point.",
    "Errands are the excuse and the call is the walk.",
    "Ordinary oil, ordinary eggs, a decent plate.",
    "Muting the chat was a kindness, mostly to me.",
    "A bent corner and the stamp still sticks.",
    "Loud friend, same name, not my conversation.",
    "Groan, stand, alarm across the room, day begins.",
    "One good shadow among too many pictures of furniture.",
    "Basil on the fridge, lecture nowhere.",
    "The list got small enough to ignore, which is a clue.",
    "Bright aisle, empty reason, go home.",
    "Toast versus gold light, the light won and the toast lost.",
    "Less explanation, more kindness, send it.",
    "An empty tab is not a plan and does not have to be.",
    "Locked, keys checked, shoulders down.",
    "Nowhere for the peel, nowhere I needed to be.",
    "The kettle is a boundary the inbox has to respect.",
    "Recipe rumor, second step missing, soup still edible.",
    "A cat asleep on other people's journeys.",
    "Inner pocket, paper money, less faith in the reader.",
    "Streets with more room in them after weather.",
    "Too much work for one sorry.",
    "Shorter reply once the feeling has a name.",
    "No better sentence than the open window.",
    "Walking story for mom, couch version later if ever.",
    "No better bottle required for eggs.",
    "If the idea survives lunch, start with the train.",
    "Keeping the ruined stamp feels like a vote for it.",
    "Late wave, kind neighbor, enough social success.",
    "Body first, badges second.",
    "I slowed down for the pigeon lecture and do not regret it.",
    "Coat pockets are not a filing system.",
    "The kettle is my permission to stop.",
    "Room first, feeling second.",
    "Lemons were a decoration that I can also eat.",
    "Cold step, warm bread, a pause with a crust.",
    "Put the clinic on Thursday or it will dissolve.",
    "Loved books can look a little broken.",
    "A day can end without a summary.",
    "Tomorrow's mug is tonight's small effort.",
    "Keep the scene where I stayed on the train too long.",
    "One question about the key, then leave it.",
    "The orange had the better announcement.",
    "A single song is a long enough break.",
    "Thirst looks like drama on a basil plant.",
    "Some objects stay for no good reason and that is fine.",
    "Mute through dinner was the right call.",
    "Enough was a sentence long.",
    "Tags still on, jacket goes back.",
    "Someone on the landing, light complaining.",
    "Six o'clock, gold wall, nothing else required.",
    "Brevity saved the paragraph.",
    "Future pocket, present charger.",
    "The old shoes were the correct vote.",
    "Truth in the essay includes the missed station.",
    "Proof of payment is a problem for later and that is acceptable.",
    "A loaf can be a question mark.",
    "Avoidance lost to warm water.",
    "Letters deserve a tab and clutter does not.",
    "The dream kept the platform and threw away the plot.",
    "Two hands are enough for that chair.",
    "Small worry, real pocket, still walking.",
    "I will take a clear orange over a clear announcement.",
    "The plant does not need a meeting.",
    "Remembering is allowed to be useless.",
    "Pencils are not the work.",
    "Armor off, reply short.",
    "Secrets in a coat are usually just paper.",
    "The unread message can wear my mood's coat.",
    "A system made of an umbrella on the floor.",
    "Public machines, private chapters.",
    "Please eat before you edit.",
    "Unearned minutes from a station clock.",
    "Practical texts are a form of care.",
    "The line got written, the pencils stayed messy.",
    "Outside in the room is a success.",
    "The walk was for the call.",
    "A decent plate is a fine ambition.",
    "Kindness with a mute button.",
    "It sticks, so it counts.",
    "Not every loud conversation is mine.",
    "Begin vertical, forgive the sound.",
    "Furniture photos, one shadow worth keeping.",
    "No lecture on the fridge.",
    "Ignoring the tiny list taught me it was optional.",
    "Home is a valid exit from a bright aisle.",
    "Burned toast, better light, fair exchange.",
    "Send the kind version.",
    "Plans can be as small as curiosity.",
    "Shoulders down is a complete status.",
    "The peel and I were both between places.",
    "Respect the kettle.",
    "Edible is a successful soup.",
    "Other journeys, sleeping cat.",
    "Paper money is a backup personality.",
    "Weather makes space on a street.",
    "One word cannot carry a whole apology.",
    "A named feeling takes up less room.",
    "The window already wrote it.",
    "Save the polish, tell it while walking.",
    "Eggs have low standards and I respect that.",
    "Lunch is the editor for fragile ideas.",
    "A vote for a bent stamp.",
    "Social success can be a delayed wave.",
    "Order of operations: stretch, then news.",
    "Pigeons, explained seriously, improved the block.",
    "File it where March can find it.",
    "Permission to stop sounds like boiling.",
    "Let the room do some of the talking.",
    "Decorations you can squeeze are the best kind.",
    "Crust, cold concrete, a decent pause.",
    "If it is not on Thursday it is not real.",
    "Broken-looking and still a good book.",
    "Summaries are optional.",
    "Small effort, clean morning.",
    "Too long on the train is still a scene.",
    "Leave the key question small.",
    "Better announcements come from fruit.",
    "Breaks can be measured in songs.",
    "Water before interpretation.",
    "No justification required for the rubber band.",
    "Right call, quiet dinner.",
    "A sentence was the whole meeting.",
    "While the tag is on, the decision is easy.",
    "The light complains so people do not have to.",
    "Required: notice the wall. Optional: everything else.",
    "Cutting the extra paragraph was the edit.",
    "Annoyed tomorrow is avoided tonight.",
    "Shoes that can take mud are a kind of optimism.",
    "The station I missed belongs in the piece.",
    "Later can hold the receipt hunt.",
    "Not every question needs a project plan.",
    "The thought lost and I am glad.",
    "Clutter does not get a tab.",
    "Plot discarded, platform kept.",
    "Carry the chair, not the indecision.",
    "Still walking, worry and all.",
    "Fruit over speakers, today.",
    "No meeting for a houseplant.",
    "Useless memories are still mine.",
    "Messy pencils, finished line.",
    "Short on purpose.",
    "Paper secrets, cloth coat.",
    "Mood wearing a message's coat.",
    "Floor umbrella, working memory.",
    "Chapters in a loud room.",
    "Edit second, food first.",
    "The clock was generous.",
    "Care looks like a time and a photo.",
    "Mess left in place on purpose.",
    "Success smelled like weather.",
    "The call was the destination.",
    "Ambition the size of a plate.",
    "A mute button and a meal.",
    "Sticky enough to count.",
    "Their conversation, my quiet.",
    "Vertical start, forgiven groan.",
    "Keep the shadow, delete nothing else yet.",
    "Fridge stays kind.",
    "Optional lists can stay tiny.",
    "Exit the aisle.",
    "Fair trade with breakfast.",
    "Kind version, sent.",
    "Curiosity as the whole plan.",
    "Status: shoulders down.",
    "Between places, peel and person.",
    "Boiling means the boundary held.",
    "Soup succeeded by being edible.",
    "Sleeping on the travel shelf.",
    "Backup personality: cash.",
    "More room after rain.",
    "Apologies are not pack animals.",
    "Names shrink feelings.",
    "Already written by the weather.",
    "Walk first, polish never.",
    "Low standards, high respect, eggs.",
    "Fragile ideas meet lunch.",
    "Bent and still valid.",
    "A late wave counts.",
    "News after the body.",
    "The block got better.",
    "March needs a real folder.",
    "Boiling as a bell.",
    "The room can narrate.",
    "Squeeze the decoration.",
    "Pause with a crust.",
    "Thursday or nothing.",
    "A good broken book.",
    "No summary required.",
    "Effort the size of a mug.",
    "The long ride stays.",
    "Keep the question small enough to send.",
    "Fruit made the clearer sound.",
    "One song, then back.",
    "Interpret after watering.",
    "Fine without a reason.",
    "Dinner won.",
    "The meeting fit in a line.",
    "Easy while the tag remains.",
    "Complaining light, passing person.",
    "The wall was the event.",
    "The edit was a deletion.",
    "Tonight saves tomorrow a frown.",
    "Optimism you can walk in.",
    "Put the missed station on the page.",
    "The hunt for proof can wait.",
    "A question mark with a crust.",
    "Glad the thought lost.",
    "Tabs are for letters.",
    "Platform yes, plot no.",
    "Indecision stays home.",
    "Walking anyway.",
    "Speakers lost to citrus.",
    "Houseplants hate agendas.",
    "Mine even if useless.",
    "Line done.",
    "Cloth over paper.",
    "Borrowed mood, unread text.",
    "Memory on the floor.",
    "Loud room, quiet page.",
    "Food, then sentences.",
    "Generous clock.",
    "A photo is a plan.",
    "Mess can be intentional.",
    "Weather in the room.",
    "Destination: the call.",
    "Plate-sized hope.",
    "Meal plus silence.",
    "It counts if it sticks.",
    "Quiet was available.",
    "Groan included, day started.",
    "Shadow kept.",
    "Kind fridge.",
    "Tiny on purpose.",
    "Aisle exited.",
    "Breakfast negotiated.",
    "Sent with less armor.",
    "Plan: look again later.",
    "Down, in a good way.",
    "Person and peel, unscheduled.",
    "The boundary held.",
    "Edible victory.",
    "Shelf of sleeps.",
    "Cash as courage.",
    "Rain opened the street.",
    "One apology is plenty.",
    "The name helped.",
    "Weather wrote the line.",
    "Never polish a walk.",
    "Eggs, respected.",
    "Lunch decides.",
    "Valid despite the bend.",
    "Wave delivered.",
    "Body, then the rest.",
    "Better block, same pigeons.",
    "A folder March can open.",
    "Bell in the kitchen.",
    "Narration by furniture and light.",
    "Lemon, squeezed or not.",
    "Crust time.",
    "Real because Thursday.",
    "Book with a history.",
    "Ended without a bow.",
    "Mug washed, gift given.",
    "Stayed on the train.",
    "Small enough to send.",
    "Citrus won the carriage.",
    "Back after the song.",
    "Water, then theory.",
    "Reason not required.",
    "Quiet plate, right choice.",
    "Line length: enough.",
    "Tag on, decision made.",
    "Person passing, light buzzing.",
    "Event: the color of the wall.",
    "Deletion was the craft.",
    "Frown avoided.",
    "Walkable optimism.",
    "Page includes the miss.",
    "Proof can be a later problem.",
    "Crust and a question.",
    "Lost thought, better evening.",
    "Letters only.",
    "Kept the useful piece of the dream.",
    "Chair yes.",
    "Worry walked too.",
    "Citrus over noise.",
    "No agenda, just leaves.",
    "Kept a useless bright bit.",
    "Done for now.",
    "Later",
    "Maybe",
    "Enough",
    "Tomorrow",
    "Noted",
    "Wait",
    "Hungry",
    "Restless",
    "Grateful",
    "Stuck",
    "Curious",
    "Tired",
    "Quiet",
    "Rest",
    "Focus",
    "Pause",
    "Listen",
    "Breathe",
    "Remember",
    "Sunday",
    "Almost",
    "Perhaps",
    "Sigh",
    "Okay",
    "Noon",
    "Dusk",
    "Dawn",
    "Rain",
    "Tea",
    "Inbox",
)

private val counted = phrases.map { phrase ->
    phrase to phrase.split(Regex("\\s+")).count { it.isNotEmpty() }
}
