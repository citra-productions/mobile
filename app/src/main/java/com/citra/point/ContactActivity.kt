package com.citra.point

import android.os.Bundle
import androidx.activity.ComponentActivity

class ContactActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCitraContent("others") { ContactScreen() }
    }
}
