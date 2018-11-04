package com.jamesjmtaylor.mgrswear

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class MgrsActivity : WearableActivity() {
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private lateinit var locationTextView: TextView
    private lateinit var latTextView: TextView
    private lateinit var longTextView: TextView
    private lateinit var accTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var df = SimpleDateFormat("yyyy-MM-dd HH:mmZ", Locale.US)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mgrs)
        checkLocationPermission()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (!hasGps()) {
            Log.e(TAG, "This hardware doesn't have GPS.")
            // Fall back to functionality that does not use location or
            // warn the user that location function is not available.
        }
        locationTextView = findViewById(R.id.locationTextView)
        latTextView = findViewById(R.id.latTextView)
        longTextView = findViewById(R.id.longTextView)
        accTextView = findViewById(R.id.accuracyTextView)
        timeTextView = findViewById(R.id.timeTextView)
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            location?.let { loc ->

                latTextView.text = loc.latitude.toString()
                longTextView.text = loc.longitude.toString()
                locationTextView.text = Coordinates.mgrsFromLatLon(loc.latitude,loc.longitude)
                val timeText = "Last update: " + df.format(loc.time)
                val accuracyText = "Accuracy: " + loc.accuracy.toString() + "m"
                timeTextView.text = timeText
                accTextView.text = accuracyText
            }
        }
    }

    private fun hasGps(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("MGRS Wear requires location permission")
                    .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this@MgrsActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                MY_PERMISSIONS_REQUEST_LOCATION)}
                    })
                    .create()
                    .show()
            } else {// No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION)
            }
            return false
        } else {
            return true
        }
    }
}
private const val TAG = "MGRSactivity"
