package com.jamesjmtaylor.mgrswear

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MgrsActivity : WearableActivity() {
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private lateinit var locationTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            location?.latitude?.let { lat -> location.longitude.let { long ->
                val locationText = Coordinates.mgrsFromLatLon(lat,long)
                locationTextView.text = locationText
            }}
        }

    }
    private fun hasGps(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    fun onLocationChanged(p0: Location?) {

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
