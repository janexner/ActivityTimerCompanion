package com.exner.tools.activitytimercompanion.ui.destinations

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exner.tools.activitytimercompanion.BuildConfig
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun About() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(PaddingValues(8.dp))
            .padding(8.dp)
            .imePadding()
    ) {
        Text(
            text = "About Activity Timer Companion",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(8.dp)
        )
        val localContext = LocalContext.current
        Spacer(modifier = Modifier.width(8.dp))
        // what's the orientation, right now?
        val configuration = LocalConfiguration.current
        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                // show horizontally
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    AboutVersionAndButton(localContext)
                    Spacer(modifier = Modifier.width(8.dp))
                    AboutText()
                }
            }

            else -> {
                // show
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    AboutVersionAndButton(localContext)
                    Spacer(modifier = Modifier.height(8.dp))
                    AboutText()
                }
            }
        }
    }
}

@Composable
private fun AboutVersionAndButton(localContext: Context) {
    Column {
        Text(
            text = "Activity Timer Companion ${BuildConfig.VERSION_NAME}",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                val webpage: Uri =
                    Uri.parse("https://jan-exner.de/software/android/activitytimer/")
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                localContext.startActivity(intent, null)
            },
        ) {
            Text(text = "Visit the Activity Timer web site")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                val webpage: Uri =
                    Uri.parse("https://jan-exner.de/software/android/fototimer/manual/")
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                localContext.startActivity(intent, null)
            },
        ) {
            Text(text = "Peruse the Activity Timer manual")
        }
    }
}

@Composable
fun AboutText() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Activity Timer Companion is an app for managing Activity Timer for TV processes.",
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = "It runs on Android phones and tablets running Android 10 or later. I aim to support the latest 3 versions of Android.",
            modifier = Modifier.padding(8.dp)
        )
    }
}

