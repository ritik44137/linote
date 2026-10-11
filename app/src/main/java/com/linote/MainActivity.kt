package com.linote

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val bars = if (night) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        setContent {
            val model: NotesViewModel = viewModel(factory = NotesViewModel.factory(this))
            val openSeed = remember { debugSeedLauncher(this) }
            NotesScreen(model, onLongTitle = openSeed)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotesScreen(model: NotesViewModel, onLongTitle: (() -> Unit)?) {
    val notes = model.notes.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    var followArmed by remember { mutableStateOf(false) }
    var followHead by remember { mutableStateOf<UUID?>(null) }
    val density = LocalDensity.current
    // The animation target is the final keyboard height on the first inset frame.
    val imePx = max(
        WindowInsets.ime.getBottom(density),
        WindowInsets.imeAnimationTarget.getBottom(density),
    )
    val navPx = WindowInsets.navigationBars.getBottom(density)
    val imePad = with(density) { imePx.toDp() }
    val navInset = with(density) { (navPx - imePx).coerceAtLeast(0).toDp() }
    val imeOpen = imePx > 0
    var imeWasOpen by remember { mutableStateOf(false) }
    var composerHeight by remember { mutableStateOf(56.dp) }
    LaunchedEffect(imeOpen) {
        if (imeWasOpen && !imeOpen) model.blur()
        imeWasOpen = imeOpen
    }
    BackHandler(enabled = imeOpen) { model.blur() }
    Column(
        Modifier
            .fillMaxSize()
            .background(colorResource(R.color.paper))
            .padding(bottom = imePad),
    ) {
        HomeHeader(onLongTitle = onLongTitle)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            NoteList(
                notes = notes,
                bottomInset = composerHeight + 8.dp,
                listState = listState,
                armed = followArmed,
                armedHead = followHead,
                onFollowSettled = { followArmed = false },
                modifier = Modifier.fillMaxSize(),
            )
            DockedComposer(
                model = model,
                onSave = {
                    val atEdge = atNewestEdge(
                        listState.firstVisibleItemIndex,
                        listState.firstVisibleItemScrollOffset,
                    )
                    followArmed = atEdge
                    if (atEdge) followHead = newestNoteId(notes)
                    model.save()
                },
                onHeight = { composerHeight = it },
                bottomInset = navInset,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun DockedComposer(
    model: NotesViewModel,
    onSave: () -> Unit,
    onHeight: (androidx.compose.ui.unit.Dp) -> Unit,
    bottomInset: androidx.compose.ui.unit.Dp,
    modifier: Modifier,
) {
    val screen by model.screen.collectAsState()
    Composer(
        editor = screen.editor,
        focused = screen.focused,
        onEdit = { block -> model.edit(block = block) },
        onFocus = model::focus,
        onSave = onSave,
        onHeight = onHeight,
        bottomInset = bottomInset,
        modifier = modifier,
    )
}
