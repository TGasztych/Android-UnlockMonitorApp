package com.example.unlockmonitorapp

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException

class DeviceAdmin : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d("DeviceAdmin", "Device admin enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d("DeviceAdmin", "Device admin disabled")
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        val serviceIntent = Intent(context, CameraService::class.java).apply {
            action = CameraService.ACTION_TAKE_PHOTO
        }
        context.startForegroundService(serviceIntent)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val datetime = dateFormat.format(Date())
        Log.d("DeviceAdmin", "Password attempt failed at $datetime")

        CoroutineScope(Dispatchers.IO).launch {
            Thread.sleep(10000)
            val sharedPreferences = context.getSharedPreferences("PhotoPrefs", Context.MODE_PRIVATE)
            val photoPath = sharedPreferences.getString("lastPhotoPath", "default/path/to/captured/photo.jpg")
            val settingsPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val webhookUrl = settingsPrefs.getString("webhookUrl", "")

            val latitude = 0.0  // Placeholder latitude
            val longitude = 0.0  // Placeholder longitude

            val db = AppDatabase.getDatabase(context)
            db.attemptDao().insertAttempt(UnlockAttempt(dateTime = datetime, photoPath = photoPath!!, latitude = latitude, longitude = longitude))

            if (!webhookUrl.isNullOrEmpty()) {
                sendWebhookMessage(datetime, photoPath, latitude, longitude, webhookUrl)
            } else {
                Log.e("Webhook", "No webhook URL configured")
            }
        }
    }

    private fun sendWebhookMessage(dateTime: String, photoPath: String?, latitude: Double, longitude: Double, webhookUrl: String) {
        val client = OkHttpClient()
        val jsonMediaType = "application/json".toMediaTypeOrNull()
        val jpegMediaType = "image/jpeg".toMediaTypeOrNull()

        val content = """
        {
            "embeds": [{
                "title": "Failed Unlock Attempt",
                "description": "Attempt detected at $dateTime",
                "fields": [
                    {"name": "Latitude", "value": "$latitude"},
                    {"name": "Longitude", "value": "$longitude"}
                ],
                "image": {"url": "attachment://photo.jpg"}
            }]
        }
        """.trimIndent()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("payload_json", content)
            .addFormDataPart(
                "photo",
                "photo.jpg",
                RequestBody.create(jpegMediaType, File(photoPath ?: ""))
            )
            .build()

        val request = Request.Builder()
            .url(webhookUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("Webhook", "Failed to send message")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("Webhook", "Failed to send message", e)
            }
        })
    }
}
