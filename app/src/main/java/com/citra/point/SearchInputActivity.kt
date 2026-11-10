package com.citra.point

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class SearchInputActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initial = intent.getStringExtra(EXTRA_QUERY).orEmpty()
        setCitraContent("search") { vm ->
            SearchInputScreen(
                vm, initial,
                onSubmit = { q ->
                    vm.addHistory(q)
                    setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_QUERY, q))
                    finish()
                },
                onBack = { finish() }
            )
        }
    }
}
