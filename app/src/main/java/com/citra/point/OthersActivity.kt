package com.citra.point

import android.os.Bundle
import androidx.activity.ComponentActivity

class OthersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCitraContent("others") { OthersScreen(it) }
    }
}
