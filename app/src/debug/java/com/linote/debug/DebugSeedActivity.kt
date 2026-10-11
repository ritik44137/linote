package com.linote.debug

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DebugSeedActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var status: TextView
    private lateinit var seedButton: Button
    private lateinit var replaceButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()
        status = TextView(this).apply {
            setTextColor(Color.BLACK)
            textSize = 16f
            setPadding(0, 0, 0, pad)
            text = savedInstanceState?.getString(STATUS_KEY) ?: INTRO
        }
        seedButton = button("Seed 1,000 notes", pad) { launch(replace = false) }
        replaceButton = button("Replace synthetic notes", pad) { launch(replace = true) }
        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.WHITE)
                setPadding(pad, pad, pad, pad)
                addView(status)
                addView(seedButton)
                addView(replaceButton)
            },
        )
        if (savedInstanceState == null && intent.getBooleanExtra("seed", false)) {
            launch(intent.getBooleanExtra("replace", false))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATUS_KEY, status.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun button(label: String, pad: Int, onClick: () -> Unit) = Button(this).apply {
        text = label
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = pad / 2 }
        setOnClickListener { onClick() }
    }

    private fun launch(replace: Boolean) {
        seedButton.isEnabled = false
        replaceButton.isEnabled = false
        status.text = "Seeding…"
        scope.launch {
            status.text = try {
                seedDatabase(applicationContext, replace)
            } catch (e: Exception) {
                "Seed failed: ${e.javaClass.simpleName}: ${e.message}"
            }
            seedButton.isEnabled = true
            replaceButton.isEnabled = true
        }
    }

    private companion object {
        const val STATUS_KEY = "status"
        const val INTRO =
            "Debug only. This writes 1,000 synthetic notes into the local database. Replace rebuilds those notes and leaves notes you wrote."
    }
}
