/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

import androidx.annotation.NonNull;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.WWUtil;

/**
 * This immutable class holds a set of UTM coordinates along with it's corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 */

public class UTMCoord
{
    private final Angle latitude;
    private final Angle longitude;
    private final String hemisphere;
    private final int zone;
    private final double easting;
    private final double northing;
    private Angle centralMeridian;

    /**
     * Create a set of UTM coordinates from a pair of latitude and longitude for the given <code>Globe</code>.
     *
     * @param latitude  the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param globe     the <code>Globe</code> - can be null (will use WGS84).
     *
     * @return the corresponding <code>UTMCoord</code>.
     *
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null, or the conversion to
     *                                  UTM coordinates fails.
     */
    public static UTMCoord fromLatLon(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            throw new IllegalArgumentException("Latitude Or Longitude Is Null");
        }

        final UTMCoordConverter converter = new UTMCoordConverter();
        long err = converter.convertGeodeticToUTM(latitude.radians, longitude.radians);

        if (err != UTMCoordConverter.UTM_NO_ERROR)
        {
            throw new IllegalArgumentException("UTM Conversion Error");
        }

        return new UTMCoord(latitude, longitude, converter.getZone(), converter.getHemisphere(),
            converter.getEasting(), converter.getNorthing(), Angle.fromRadians(converter.getCentralMeridian()));
    }

    public static UTMCoord fromLatLon(Angle latitude, Angle longitude, String datum)
    {
        if (latitude == null || longitude == null)
        {
            throw new IllegalArgumentException("Latitude Or Longitude Is Null");
        }

        UTMCoordConverter converter;
        if (!WWUtil.isEmpty(datum) && datum.equals("NAD27"))
        {
            converter = new UTMCoordConverter(UTMCoordConverter.CLARKE_A, UTMCoordConverter.CLARKE_F);
            LatLon llNAD27 = UTMCoordConverter.convertWGS84ToNAD27(latitude, longitude);
            latitude = llNAD27.getLatitude();
            longitude = llNAD27.getLongitude();
        }
        else
        {
            converter = new UTMCoordConverter(UTMCoordConverter.WGS84_A, UTMCoordConverter.WGS84_F);
        }

        long err = converter.convertGeodeticToUTM(latitude.radians, longitude.radians);

        if (err != UTMCoordConverter.UTM_NO_ERROR)
        {
            throw new IllegalArgumentException("UTM Conversion Error");
        }

        return new UTMCoord(latitude, longitude, converter.getZone(), converter.getHemisphere(),
            converter.getEasting(), converter.getNorthing(), Angle.fromRadians(converter.getCentralMeridian()));
    }

    /**
     * Create a set of UTM coordinates for the given <code>Globe</code>.
     *
     * @param zone       the UTM zone - 1 to 60.
     * @param hemisphere the hemisphere, either {@link AVKey#NORTH} or {@link
     *                   AVKey#SOUTH}.
     * @param easting    the easting distance in meters
     * @param northing   the northing distance in meters.
     * @param globe      the <code>Globe</code> - can be null (will use WGS84).
     *
     * @return the corresponding <code>UTMCoord</code>.
     *
     * @throws IllegalArgumentException if the conversion to UTM coordinates fails.
     */
    public static UTMCoord fromUTM(int zone, String hemisphere, double easting, double northing)
    {
        final UTMCoordConverter converter = new UTMCoordConverter();
        long err = converter.convertUTMToGeodetic(zone, hemisphere, easting, northing);

        if (err != UTMCoordConverter.UTM_NO_ERROR)
        {
            throw new IllegalArgumentException("UTM Conversion Error");
        }

        return new UTMCoord(Angle.fromRadians(converter.getLatitude()),
            Angle.fromRadians(converter.getLongitude()),
            zone, hemisphere, easting, northing, Angle.fromRadians(converter.getCentralMeridian()));
    }

    /**
     * Convenience method for converting a UTM coordinate to a geographic location.
     *
     * @param zone       the UTM zone: 1 to 60.
     * @param hemisphere the hemisphere, either {@link AVKey#NORTH} or {@link
     *                   AVKey#SOUTH}.
     * @param easting    the easting distance in meters
     * @param northing   the northing distance in meters.
     * @param globe      the <code>Globe</code>. Can be null (will use WGS84).
     *
     * @return the geographic location corresponding to the specified UTM coordinate.
     */
    public static LatLon locationFromUTMCoord(int zone, String hemisphere, double easting, double northing)
    {
        UTMCoord coord = UTMCoord.fromUTM(zone, hemisphere, easting, northing);
        return new LatLon(coord.getLatitude(), coord.getLongitude());
    }

    /**
     * Create an arbitrary set of UTM coordinates with the given values.
     *
     * @param latitude   the latitude <code>Angle</code>.
     * @param longitude  the longitude <code>Angle</code>.
     * @param zone       the UTM zone - 1 to 60.
     * @param hemisphere the hemisphere, either {@link AVKey#NORTH} or {@link
     *                   AVKey#SOUTH}.
     * @param easting    the easting distance in meters
     * @param northing   the northing distance in meters.
     *
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null.
     */
    public UTMCoord(Angle latitude, Angle longitude, int zone, String hemisphere, double easting, double northing)
    {
        this(latitude, longitude, zone, hemisphere, easting, northing, Angle.fromDegreesLongitude(0.0));
    }

    /**
     * Create an arbitrary set of UTM coordinates with the given values.
     *
     * @param latitude        the latitude <code>Angle</code>.
     * @param longitude       the longitude <code>Angle</code>.
     * @param zone            the UTM zone - 1 to 60.
     * @param hemisphere      the hemisphere, either {@link AVKey#NORTH} or {@link
     *                        AVKey#SOUTH}.
     * @param easting         the easting distance in meters
     * @param northing        the northing distance in meters.
     * @param centralMeridian the cntral meridian <code>Angle</code>.
     *
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null.
     */
    public UTMCoord(Angle latitude, Angle longitude, int zone, String hemisphere, double easting, double northing,
        Angle centralMeridian)
    {
        if (latitude == null || longitude == null)
        {
            throw new IllegalArgumentException("Latitude Or Longitude Is Null");
        }

        this.latitude = latitude;
        this.longitude = longitude;
        this.hemisphere = hemisphere;
        this.zone = zone;
        this.easting = easting;
        this.northing = northing;
        this.centralMeridian = centralMeridian;
    }

    public Angle getCentralMeridian()
    {
        return this.centralMeridian;
    }

    public Angle getLatitude()
    {
        return this.latitude;
    }

    public Angle getLongitude()
    {
        return this.longitude;
    }

    public int getZone()
    {
        return this.zone;
    }

    public String getHemisphere()
    {
        return this.hemisphere;
    }

    public double getEasting()
    {
        return this.easting;
    }

    public double getNorthing()
    {
        return this.northing;
    }

    @NonNull
    public String toString()
    {
        return String.valueOf(zone) +
                " " + (AVKey.NORTH.equals(hemisphere) ? "N" : "S") +
                " " + ((int) easting) + "E" +
                " " + ((int) northing) + "N";
    }
}
