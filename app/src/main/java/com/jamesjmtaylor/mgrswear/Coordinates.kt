package com.jamesjmtaylor.mgrswear

import gov.nasa.worldwind.avlist.AVKey
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
        return utm.zone.toString() +
                " " + (getZoneLetter(lat,lon)) +
                " " + utm.easting.toInt() + "E" +
                " " + utm.northing.toInt() + "N"
    }

    fun latLonFromMgrs(mgrs: String): DoubleArray {
        val coord = MGRSCoord.fromString(mgrs)
        return doubleArrayOf(coord.getLatitude().degrees, coord.getLongitude().degrees)
    }

    fun getZoneLetter(Lat: Double, Lon: Double): String {
        var zone = Math.floor(Lon / 6 + 31).toInt()
        if (Lat < -72) return "C"
        else if (Lat < -64) return "D"
        else if (Lat < -56) return "E"
        else if (Lat < -48) return "F"
        else if (Lat < -40) return "G"
        else if (Lat < -32) return "H"
        else if (Lat < -24) return "J"
        else if (Lat < -16) return "K"
        else if (Lat < -8)  return "L"
        else if (Lat < 0)   return "M"
        else if (Lat < 8)   return "N"
        else if (Lat < 16)  return "P"
        else if (Lat < 24)  return "Q"
        else if (Lat < 32)  return "R"
        else if (Lat < 40)  return "S"
        else if (Lat < 48)  return "T"
        else if (Lat < 56)  return "U"
        else if (Lat < 64)  return "V"
        else if (Lat < 72)  return "W"
        else return "X"
    }

}