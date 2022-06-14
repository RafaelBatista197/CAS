package com.example.android.navigation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.testposedetection.ChatBotActivity

import com.example.testposedetection.R
import com.example.testposedetection.databinding.FragmentTitleBinding
import com.example.testposedetection.kotlin.CameraXLivePreviewActivity
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.fence.*
import java.util.ArrayList


class TitleFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101
    private lateinit var binding : FragmentTitleBinding
    val POSE = "pose"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        createNotificationChannel()


        if (!allRuntimePermissionsGranted()) {
            getRuntimePermissions()
        }

        setupFences()

        binding = DataBindingUtil.inflate<FragmentTitleBinding>(inflater,
            R.layout.fragment_title,container,false)

        //Set notification time, in seconds
        setTimeOnClick()

        //The complete onClickListener with Navigation
        binding.tryExercises.setOnClickListener { view : View ->
            val intent = Intent(context, CameraXLivePreviewActivity::class.java)
            intent.putExtra(POSE, "all_poses")
            startActivity(intent)
        }

        //Open Chat Bot
        binding.chatBot.setOnClickListener { view: View ->
            val intent = Intent(context, ChatBotActivity::class.java)
            startActivity(intent)
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_menu, menu)
    }

    // navigate to the Fragment that has the same id as the selected menu item
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.
        onNavDestinationSelected(item,requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }

    private fun allRuntimePermissionsGranted(): Boolean {
        for (permission in TitleFragment.REQUIRED_RUNTIME_PERMISSIONS) {
            permission?.let {
                if (!isPermissionGranted(requireContext(), it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in TitleFragment.REQUIRED_RUNTIME_PERMISSIONS) {
            permission?.let {
                if (!isPermissionGranted(requireContext(), it)) {
                    permissionsToRequest.add(permission)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                TitleFragment.PERMISSION_REQUESTS
            )
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TitleFragment.TAG, "Permission granted: $permission")
            return true
        }
        Log.i(TitleFragment.TAG, "Permission NOT granted: $permission")
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



    fun setTimeOnClick () {
        binding.setTime.setOnClickListener { view : View ->
            val stillFence: AwarenessFence =
                DetectedActivityFence.during(DetectedActivityFence.STILL)

            val nowMillis = System.currentTimeMillis()



            val getTime = (Integer.parseInt(binding.editTextNumber.getText().toString())).toLong()


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


    }


    ////// FENCES ///////


    private lateinit var fenceReceiver: FenceReceiver
    private lateinit var myPendingIntent: PendingIntent

    private fun setupFences() {
        val intent = Intent("FENCE_RECEIVER_ACTION")

        myPendingIntent = if(Build.VERSION.SDK_INT >= 31){
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }else{
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        fenceReceiver = FenceReceiver()
        registerReceiver(
            requireContext(),
            fenceReceiver,
            IntentFilter("FENCE_RECEIVER_ACTION",), RECEIVER_EXPORTED
        )
    }

    private fun addFence(fenceKey: String, fence: AwarenessFence) {
        Awareness.getFenceClient(requireContext()).updateFences(
            FenceUpdateRequest.Builder()
                .addFence(fenceKey, fence, myPendingIntent)
                .build()
        )
            .addOnSuccessListener {
                val text = "\n Fence successfully registered: $fenceKey "
                if (fenceKey == "timeAndStillFence") {
                    //binding.textView.text = "$text\n${binding.textView.text}"
                    Log.e(
                        "TAG_FENCES",
                        text
                    )
                    Toast.makeText(context, "You'll be notified when to exercise", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(
                        "TAG_FENCES",
                        text
                    )
                }
            }
            .addOnFailureListener { e ->
                val text =
                    "\n Fence could not be registered: ${fenceKey}. Error: ${e.message}"
                //binding.textView.text = "$text\n${binding.textView.text}"
                if (fenceKey == "timeAndStillFence") {
                    //binding.textView.text = "$text\n${binding.textView.text}"
                    Log.e(
                        "TAG_FENCES",
                        "Error: ${text})"
                    )
                    Toast.makeText(context, "Error registering notification", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(
                        "TAG_FENCES",
                        "Error: ${text})"
                    )
                }
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
                        //sendNotification()
                        fenceInfo = "UNKNOWN: "
                }
                else -> fenceInfo = "Error: unknown fence."
            }

            val text = "\n[Fence ${fenceState.fenceKey} - $fenceInfo"
            //binding.textView.text = "$text\n${binding.textView.text}"
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
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {

        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, CameraXLivePreviewActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setContentTitle("It's time to exercise!")
            .setContentText("Get up, it's time to work!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(requireContext())) {
            notify(notificationId, builder.build())
        }
    }
}