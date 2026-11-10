package com.citra.point

import android.os.Bundle
import androidx.activity.ComponentActivity

class ProductDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(EXTRA_PRODUCT_ID, 1)
        setCitraContent("home") { ProductDetailScreen(id, it) }
    }
}
