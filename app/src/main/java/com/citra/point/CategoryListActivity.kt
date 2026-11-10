package com.citra.point

import android.os.Bundle
import androidx.activity.ComponentActivity

class CategoryListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(EXTRA_CATEGORY_ID).orEmpty()
        setCitraContent("home") { CategoryScreen(id, it) }
    }
}
