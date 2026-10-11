package com.linote

import android.content.Intent
import androidx.activity.ComponentActivity
import com.linote.debug.DebugSeedActivity

fun debugSeedLauncher(activity: ComponentActivity): (() -> Unit)? = {
    activity.startActivity(Intent(activity, DebugSeedActivity::class.java))
}
