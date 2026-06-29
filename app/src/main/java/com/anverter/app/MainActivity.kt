package com.anverter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anverter.app.ui.theme.AnverterTheme
import top.yukonga.miuix.kmp.basic.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AnverterTheme {
                Placeholder()
            }
        }
    }
}

@Composable
private fun Placeholder() {
    Text(
        text = "Anverter",
        modifier = Modifier.fillMaxSize().wrapContentSize(),
    )
}
