package com.citra.point

import android.os.Bundle
import androidx.activity.ComponentActivity

class FaqActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCitraContent("others") { FaqScreen(it) }
    }
}
