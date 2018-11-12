package com.jamesjmtaylor.mgrswear

import gov.nasa.worldwind.geom.Angle

import gov.nasa.worldwind.geom.coords.MGRSCoord
import gov.nasa.worldwind.geom.coords.UTMCoord

object Coordinates {

    fun mgrsFromLatLon(lat: Double, lon: Double): String {
        val latitude = Angle.fromDegrees(lat)
        val longitude = Angle.fromDegrees(lon)

        return MGRSCoord
            .fromLatLon(latitude, longitude)
            .toString()
    }

    fun utmFromLatLon(lat: Double, lon: Double): String {
        val latitude = Angle.fromDegrees(lat)
        val longitude = Angle.fromDegrees(lon)

        val utm = UTMCoord.fromLatLon(latitude, longitude)
        val zone = utm.zone
        val letter = utm
        val easting = utm.easting.toInt()
        val northing = utm.northing.toInt()
        return "$zone $easting E $northing N"
    }

    fun latLonFromMgrs(mgrs: String): DoubleArray {
        val coord = MGRSCoord.fromString(mgrs)
        return doubleArrayOf(coord.getLatitude().degrees, coord.getLongitude().degrees)
    }

}