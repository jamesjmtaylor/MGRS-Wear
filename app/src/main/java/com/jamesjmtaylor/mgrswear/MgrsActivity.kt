package com.jamesjmtaylor.mgrswear

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientLifecycleObserver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale

class MgrsActivity : FragmentActivity() {
    private val requestPermissionTag = 99
    private lateinit var locationTextView: TextView
    private lateinit var accTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var formatButton: Button
    private lateinit var scrollView: ScrollView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    private var df = SimpleDateFormat("yyyy-MM-dd HH:mmZ", Locale.US)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(ambientObserver)
        setContentView(R.layout.activity_mgrs)
        val builder = LocationRequest.Builder(20 * 1000)
        builder.setPriority(Priority.PRIORITY_HIGH_ACCURACY )
        locationRequest = builder.build()
        scrollView = findViewById(R.id.scrollView)
        locationTextView = findViewById(R.id.locationTextView)
        accTextView = findViewById(R.id.accuracyTextView)
        timeTextView = findViewById(R.id.timeTextView)
        formatButton = findViewById(R.id.formatButton)
        formatButton.setOnClickListener(FormatButtonOnClickListener(WeakReference(this)))

        scrollView.requestFocus()
        if (checkLocationPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.requestLocationUpdates(locationRequest, GpsCallback(WeakReference(this)), null)
        }
    }




    enum class Format { MGRS, UTM, LATLONG }
    var selectedFormat = Format.MGRS
    private class FormatButtonOnClickListener(val activity: WeakReference<MgrsActivity>): View.OnClickListener {
        override fun onClick(p0: View?) {
            activity.get()?.let { a->
                when (a.selectedFormat) {
                    Format.MGRS -> a.selectedFormat = Format.UTM
                    Format.UTM -> a.selectedFormat = Format.LATLONG
                    Format.LATLONG -> a.selectedFormat = Format.MGRS
                }
                a.updateUi()
            }
        }
    }


    private class GpsCallback(val activity: WeakReference<MgrsActivity>) : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.let { result -> activity.get()?.let { mgrsActivity ->
                mgrsActivity.lastLocation = result.lastLocation //Most recent location available in this result
                mgrsActivity.updateUi()
            }}
        }
    }

    private fun updateUi() {
        lastLocation?.let { loc -> this.runOnUiThread {
            val lat = loc.latitude; val long = loc.longitude
            when (selectedFormat){
                Format.MGRS -> locationTextView.text = Coordinates.mgrsFromLatLon(lat, long)
                Format.UTM -> locationTextView.text = Coordinates.utmFromLatLon(lat,long)
                Format.LATLONG -> { val latLong = "$lat, $long"; locationTextView.text = latLong}
            }
            val timeText = "Last update: " + df.format(loc.time)
            val accuracyText = "Accuracy: +/- " + loc.accuracy.toInt().toString() + "m"
            timeTextView.text = timeText
            accTextView.text = accuracyText
            formatButton.text = selectedFormat.name
        }}
    }
    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("MGRS Wear requires location permission")
                    .setPositiveButton("OK"
                    ) { _, _ -> //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this@MgrsActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            requestPermissionTag
                        )
                    }
                    .create()
                    .show()
            } else { // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestPermissionTag)
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

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            formatButton.visibility = GONE
        }

        override fun onExitAmbient() {
            formatButton.visibility = VISIBLE
        }

        override fun onUpdateAmbient() {
            this@MgrsActivity.updateUi()
        }
    }
    private val ambientObserver = AmbientLifecycleObserver(this, ambientCallback)
}