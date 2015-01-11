/*
 * Project:  NextGIS Mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * *****************************************************************************
 * Copyright (c) 2012-2015. NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.maplib.util;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.nextgis.maplib.api.GpsEventListener;

import java.util.ArrayList;
import java.util.List;


public class GpsEventSource
{
    protected List<GpsEventListener> mListeners;

    protected LocationManager     mLocationManager;
    protected GpsLocationListener mGpsLocationListener;
    protected GpsStatusListener   mGpsStatusListener;


    public GpsEventSource(Context context)
    {
        mListeners = new ArrayList<>();

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mGpsLocationListener = new GpsLocationListener();
        mGpsStatusListener = new GpsStatusListener();
    }


    /**
     * Add new listener for GPS events. You will likely want to call addListener() from your
     * Activity's or Fragment's onResume() method, to enable the features. Remember to call the
     * corresponding removeListener() in your Activity's or Fragment's onPause() method, to prevent
     * unnecessary use of the battery.
     *
     * @param listener
     *         A listener class implements GpsEventListener adding to listeners array
     */
    public void addListener(GpsEventListener listener)
    {
        if (mListeners != null && !mListeners.contains(listener)) {
            mListeners.add(listener);

            if (mListeners.size() == 1) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                                                        mGpsLocationListener);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                                                        mGpsLocationListener);
                mLocationManager.addGpsStatusListener(mGpsStatusListener);
            }
        }
    }


    /**
     * Remove listener from listeners of GPS events. You will likely want to call removeListener()
     * from your Activity's or Fragment's onPause() method, to prevent unnecessary use of the
     * battery. Remember to call the corresponding addListener() in your Activity's or Fragment's
     * onResume() method.
     *
     * @param listener
     *         A listener class implements GpsEventListener removing from listeners array
     */
    public void removeListener(GpsEventListener listener)
    {
        if (mListeners != null) {
            mListeners.remove(listener);

            if (mListeners.size() == 0) {
                mLocationManager.removeUpdates(mGpsLocationListener);
                mLocationManager.removeGpsStatusListener(mGpsStatusListener);
            }
        }
    }


    protected final class GpsLocationListener
            implements LocationListener
    {

        public void onLocationChanged(Location location)
        {
            for (GpsEventListener listener : mListeners) {
                listener.onLocationChanged(location);
            }
        }


        public void onProviderDisabled(String arg0)
        {
            // TODO Auto-generated method stub

        }


        public void onProviderEnabled(String provider)
        {
            // TODO Auto-generated method stub

        }


        public void onStatusChanged(
                String provider,
                int status,
                Bundle extras)
        {
            // TODO Auto-generated method stub

        }
    }


    private final class GpsStatusListener
            implements GpsStatus.Listener
    {

        @Override
        public void onGpsStatusChanged(int event)
        {
            for (GpsEventListener listener : mListeners) {
                listener.onGpsStatusChanged(event);
            }
        }
    }
}
