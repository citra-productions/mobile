package com.citra.point

import android.os.Bundle
import androidx.activity.ComponentActivity

class ProductDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(EXTRA_PRODUCT_ID).orEmpty()
        setCitraContent("home") { ProductDetailScreen(id, it) }
    }
}
