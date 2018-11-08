package com.jamesjmtaylor.mgrswear

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.TextView
import com.google.android.gms.location.*
import java.lang.ref.WeakReference
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
    private var locationRequest = LocationRequest.create()

    private var df = SimpleDateFormat("yyyy-MM-dd HH:mmZ", Locale.US)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mgrs)
        checkLocationPermission()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(20 * 1000)

        locationTextView = findViewById(R.id.locationTextView)
        latTextView = findViewById(R.id.latTextView)
        longTextView = findViewById(R.id.longTextView)
        accTextView = findViewById(R.id.accuracyTextView)
        timeTextView = findViewById(R.id.timeTextView)
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            location?.let { loc ->
                this.runOnUiThread {
                    updateUi(loc)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, gpsCallback(WeakReference(this)), null)
    }

    private class gpsCallback(val activity: WeakReference<MgrsActivity>) : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let { result ->
                for (location in result.locations) {
                    activity.get()?.let { a -> location?.let { loc ->
                        a.updateUi(loc) }}
                }}}
    }


    private fun updateUi(loc: Location) {
        this.runOnUiThread {
            latTextView.text = loc.latitude.toString()
            longTextView.text = loc.longitude.toString()
            locationTextView.text = Coordinates.mgrsFromLatLon(loc.latitude, loc.longitude)
            val timeText = "Last update: " + df.format(loc.time)
            val accuracyText = "Accuracy: " + loc.accuracy.toString() + "m"
            timeTextView.text = timeText
            accTextView.text = accuracyText
        }
    }

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
