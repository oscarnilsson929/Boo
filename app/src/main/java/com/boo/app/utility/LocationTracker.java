package com.boo.app.utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.boo.app.AppConfig;

import java.util.List;

public class LocationTracker {
    private Context context;
    private double latitude, longitude;

    public LocationTracker(Context context) {
        this.context = context;
        getLocation();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Utility.showToast(context, "Please turn on the location permission of your device.");
            latitude = longitude = 0;
            return;
        }
        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        List<String> matchingProviders = mLocationManager.getAllProviders();
        for (String providerItem : matchingProviders) {
            Location location = mLocationManager.getLastKnownLocation(providerItem);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d(AppConfig.APP_NAME + " : LocationChanged", "Latitude:" + latitude + ", Longitude:" + longitude);
            }
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
