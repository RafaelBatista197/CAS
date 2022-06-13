package com.example.testposedetection

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testposedetection.kotlin.CameraXLivePreviewActivity
import androidx.databinding.DataBindingUtil
import java.util.ArrayList
import android.app.NotificationChannel
import android.app.NotificationManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
//import com.example.testposedetection.databinding.ActivityMainBinding
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.fence.*


class MainActivity : AppCompatActivity() , ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var textView: TextView
    private lateinit var editText: EditText

    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        editText = findViewById(R.id.editTextNumber)

        textView = findViewById(R.id.textView)


        createNotificationChannel()




        findViewById<Button>(R.id.button).setOnClickListener {
            val intent = Intent(this, CameraXLivePreviewActivity::class.java)
            startActivity(intent)
        }

        if (!allRuntimePermissionsGranted()) {
            getRuntimePermissions()
        }

        setupFences()
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

    private fun allRuntimePermissionsGranted(): Boolean {
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    permissionsToRequest.add(permission)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUESTS
            )
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permission granted: $permission")
            return true
        }
        Log.i(TAG, "Permission NOT granted: $permission")
        return false
    }

    companion object {
        private const val TAG = "EntryChoiceActivity"
        private const val PERMISSION_REQUESTS = 1


        private val REQUIRED_RUNTIME_PERMISSIONS =
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
    }


    fun setTimeOnClick (view: View) {

        val stillFence: AwarenessFence =
            DetectedActivityFence.during(DetectedActivityFence.STILL)

        val nowMillis = System.currentTimeMillis()

        val getTime = (Integer.parseInt(editText.getText().toString())).toLong()


        val oneMinuteMilis = 300L * 1000L
        val thirtySecondsMillis = getTime * 1000L
        val timeFence: AwarenessFence = TimeFence.inInterval(
            nowMillis + thirtySecondsMillis,  // starting in thirty seconds
            nowMillis + thirtySecondsMillis + oneMinuteMilis // lasts for one minute
        )
        addFence("timeFence", timeFence)

        addFence("stillFence", stillFence)

        val timeAndStillFence = AwarenessFence.and(
            stillFence, timeFence)

        addFence("timeAndStillFence", timeAndStillFence)
    }


    ////// FENCES ///////


    private lateinit var fenceReceiver: FenceReceiver
    private lateinit var myPendingIntent: PendingIntent

    private fun setupFences() {
        val intent = Intent("FENCE_RECEIVER_ACTION")
        myPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        fenceReceiver = FenceReceiver()
        registerReceiver(
            fenceReceiver,
            IntentFilter("FENCE_RECEIVER_ACTION")
        )
    }

    private fun addFence(fenceKey: String, fence: AwarenessFence) {
        Awareness.getFenceClient(this).updateFences(
            FenceUpdateRequest.Builder()
                .addFence(fenceKey, fence, myPendingIntent)
                .build()
        )
            .addOnSuccessListener {
                val text = "\n Fence successfully registered: $fenceKey "
                textView.text = "$text\n${textView.text}"
            }
            .addOnFailureListener { e ->
                val text =
                    "\n Fence could not be registered: ${fenceKey}. Error: ${e.message}"
                textView.text = "$text\n${textView.text}"
            }
    }

    private inner class FenceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action != "FENCE_RECEIVER_ACTION") {
                Log.e(
                    "TAG_FENCES",
                    "Error: unsupported action (${intent.action})"
                )
                return
            }
            val fenceState: FenceState = FenceState.extract(intent)
            var fenceInfo: String? = null
            when (fenceState.fenceKey) {
                "stillFence" -> when (fenceState.currentState) {
                    FenceState.TRUE -> fenceInfo = "TRUE: Still."
                    //FenceState.FALSE -> //fences()
                    //FenceState.UNKNOWN -> //fences()
                }
                "timeFence" -> when (fenceState.currentState) {
                    FenceState.TRUE -> fenceInfo = "TRUE: Within timeslot."
                    FenceState.FALSE -> fenceInfo = "FALSE: Out of timeslot."
                    FenceState.UNKNOWN -> fenceInfo = "Error: unknown state."
                }
                "timeAndStillFence" -> when (fenceState.currentState) {
                    FenceState.TRUE ->
                        sendNotification()
                    FenceState.FALSE -> fenceInfo = "FALSE: "
                    FenceState.UNKNOWN ->
                        fenceInfo = "UNKNOWN: "
                }
                else -> fenceInfo = "Error: unknown fence."
            }

            val text = "\n[Fence ${fenceState.fenceKey} - $fenceInfo"
            textView.text = "$text\n${textView.text}"
        }
    }





    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Title"
            val descriptionText = "Notification Descritpion"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {

        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setContentTitle("Example title")
            .setContentText("Example description")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

}