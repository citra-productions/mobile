package com.citra.point

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCitraContent("search") { vm ->
            // Layar input pencarian = Activity terpisah, hasil query dikirim balik lewat Activity Result
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
                if (r.resultCode == Activity.RESULT_OK) vm.query = r.data?.getStringExtra(EXTRA_QUERY).orEmpty()
            }
            SearchScreen(vm) {
                launcher.launch(
                    Intent(this@SearchActivity, SearchInputActivity::class.java).putExtra(EXTRA_QUERY, vm.query)
                )
            }
        }
    }
}
