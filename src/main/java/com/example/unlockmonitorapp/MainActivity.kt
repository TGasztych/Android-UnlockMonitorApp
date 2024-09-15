package com.example.unlockmonitorapp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.unlockmonitorapp.ui.theme.UnlockMonitorAppTheme

import coil.compose.rememberImagePainter

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: UnlockViewModel
    private lateinit var viewModelForSettings: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[UnlockViewModel::class.java]
        viewModelForSettings = ViewModelProvider(this)[SettingsViewModel::class.java]

        setContent {
            UnlockMonitorAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(modifier = Modifier.padding(innerPadding), viewModel = viewModel)
                }
            }
        }
    }

    @Composable
    fun MainContent(modifier: Modifier, viewModel: UnlockViewModel) {
        var showSettings by remember { mutableStateOf(false) }

        if (showSettings) {
            SettingsScreen(
                onBack = { showSettings = false },
                onEnableAdmin = { enableDeviceAdmin() }
            )
        } else {
            Column(modifier = modifier) {
                Button(onClick = { showSettings = true }) {
                    Text("Open Settings")
                }
                UnlockAttemptList(viewModel) // Composable to display the list
            }
        }
    }

    private fun enableDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                ComponentName(this@MainActivity, DeviceAdmin::class.java)
            )
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "This app requires device admin permissions to monitor unlock attempts."
            )
        }
        startActivityForResult(intent, RESULT_ENABLE)
    }

    companion object {
        private const val RESULT_ENABLE = 1
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen(onBack: () -> Unit, onEnableAdmin: () -> Unit) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                DeviceAdminButton(onEnableAdmin = onEnableAdmin)
                WebhookInputField(viewModelForSettings)
            }
        }
    }


    @Composable
    fun WebhookInputField(viewModel: SettingsViewModel) {
        var text by remember { mutableStateOf("") }

        LaunchedEffect(key1 = Unit) {
            text = viewModel.getWebhookUrl()  // Load saved URL when the screen is composed
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Webhook URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { viewModel.saveWebhookUrl(text) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Save URL")
        }
    }


    @Composable
    fun DeviceAdminButton(modifier: Modifier = Modifier, onEnableAdmin: () -> Unit) {
        Button(onClick = onEnableAdmin, modifier = modifier) {
            Text("Enable Device Admin")
        }
    }

    @Composable
    fun UnlockAttemptList(viewModel: UnlockViewModel) {
        val attempts by viewModel.attempts.observeAsState(initial = listOf())

        LazyColumn {
            items(attempts) { attempt ->
                UnlockAttemptItem(attempt)
            }
        }
    }

    @Composable
    fun UnlockAttemptItem(attempt: UnlockAttempt) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = attempt.dateTime)
                    Text(text = "${attempt.latitude}, ${attempt.longitude}")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Image(
                    painter = rememberImagePainter(
                        data = attempt.photoPath,
                        builder = {
                            crossfade(true)
                            placeholder(R.drawable.ic_photo)
                            error(R.drawable.ic_photo_error)
                        }
                    ),
                    contentDescription = "Captured Photo",
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }
}
