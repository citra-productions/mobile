package com.citra.point

import android.os.Bundle
import androidx.activity.ComponentActivity

class FavoriteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCitraContent("favorites") { FavoritesScreen(it) }
    }
}
