package com.jamesjmtaylor.mgrswear

import gov.nasa.worldwind.geom.Angle

import gov.nasa.worldwind.geom.coords.MGRSCoord

object Coordinates {

    fun mgrsFromLatLon(lat: Double, lon: Double): String {

        val latitude = Angle.fromDegrees(lat)

        val longitude = Angle.fromDegrees(lon)

        return MGRSCoord
            .fromLatLon(latitude, longitude)
            .toString()
    }

    fun latLonFromMgrs(mgrs: String): DoubleArray {

        val coord = MGRSCoord.fromString(mgrs)

        return doubleArrayOf(coord.getLatitude().degrees, coord.getLongitude().degrees)
    }

}