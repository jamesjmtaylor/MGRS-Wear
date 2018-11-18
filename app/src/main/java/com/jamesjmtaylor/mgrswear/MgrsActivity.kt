package com.jamesjmtaylor.mgrswear

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.wear.widget.BoxInsetLayout
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.location.*
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class MgrsActivity : WearableActivity() {
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private lateinit var locationTextView: TextView
    private lateinit var accTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var formatButton: Button
    private lateinit var backgroundView: BoxInsetLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequest = LocationRequest.create()
    private var lastLocation : Location? = null
    private var df = SimpleDateFormat("yyy" +
            "y-MM-dd HH:mmZ", Locale.US)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mgrs)
        setAmbientEnabled()

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(20 * 1000)
        backgroundView = findViewById(R.id.backgroundView)
        locationTextView = findViewById(R.id.locationTextView)
        accTextView = findViewById(R.id.accuracyTextView)
        timeTextView = findViewById(R.id.timeTextView)
        formatButton = findViewById(R.id.formatButton)
        formatButton.setOnClickListener(formatButtonOnClickListener(WeakReference(this)))


        if (checkLocationPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.requestLocationUpdates(locationRequest, GpsCallback(WeakReference(this)), null)
        }
    }
    enum class format { MGRS, UTM, LATLONG }
    var selectedFormat = format.MGRS
    private class formatButtonOnClickListener(val activity: WeakReference<MgrsActivity>): View.OnClickListener {
        override fun onClick(p0: View?) {
            activity.get()?.let { a->
                when (a.selectedFormat) {
                    format.MGRS -> a.selectedFormat = format.UTM
                    format.UTM -> a.selectedFormat = format.LATLONG
                    format.LATLONG -> a.selectedFormat = format.MGRS
                }
                a.updateUi()
            }
        }
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
        backgroundView.setBackgroundColor(Color.BLACK)
        locationTextView.paint.isAntiAlias = false
        timeTextView.setTextColor(Color.WHITE)
        timeTextView.paint.isAntiAlias = false
        accTextView.setTextColor(Color.WHITE)
        accTextView.paint.isAntiAlias = false
        formatButton.visibility = GONE
     }

    override fun onExitAmbient() {
        super.onExitAmbient()
        backgroundView.setBackgroundColor(Color.DKGRAY)
        locationTextView.paint.isAntiAlias = true
        timeTextView.setTextColor(Color.LTGRAY)
        timeTextView.paint.isAntiAlias = true
        accTextView.setTextColor(Color.LTGRAY)
        accTextView.paint.isAntiAlias = true
        formatButton.visibility = VISIBLE
    }

    private class GpsCallback(val activity: WeakReference<MgrsActivity>) : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let { result -> activity.get()?.let { a ->
                a.lastLocation = result.lastLocation //Most recent location available in this result
                a.updateUi()
            }}}
    }

    private fun updateUi() {
        lastLocation?.let { loc -> this.runOnUiThread {
            val lat = loc.latitude; val long = loc.longitude
            when (selectedFormat){
                format.MGRS -> locationTextView.text = Coordinates.mgrsFromLatLon(lat, long)
                format.UTM -> locationTextView.text = Coordinates.utmFromLatLon(lat,long)
                format.LATLONG -> { val latLong = "$lat, $long"; locationTextView.text = latLong}
            }
            val timeText = "Last update: " + df.format(loc.time)
            val accuracyText = "Accuracy: +/- " + loc.accuracy.toInt().toString() + "m"
            timeTextView.text = timeText
            accTextView.text = accuracyText
            formatButton.text = selectedFormat.name
        }}
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkLocationPermission()){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.requestLocationUpdates(locationRequest, GpsCallback(WeakReference(this)), null)
        } else { this.runOnUiThread {
            locationTextView.text = getString(R.string.permission_error)
            timeTextView.visibility = GONE
            accTextView.visibility = GONE
            formatButton.visibility = GONE
        }}
        return
    }
}

private const val TAG = "MGRSactivity"
