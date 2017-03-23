package com.boo.app.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.boo.app.App;
import com.boo.app.R;
import com.boo.app.api.GetDataTask;
import com.boo.app.api.OnTaskCompleted;
import com.boo.app.utility.LocationTracker;
import com.boo.app.utility.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class FindIt extends BaseActivity implements OnMapReadyCallback, OnTaskCompleted {
    private int PLACE_PICKER_REQUEST = 1;

    private ProgressDialog mProgressDialog;
    private GoogleMap map;
    private TextView tvTitle;
    private MapView mapView;
    private LatLng currentLatLng;

    private String strText = "", strPhoto = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_it);

        mapView = _findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        initUI();
        showPlacePicker();
    }

    @Override
    public void onBackPressed() {
        onBack(0);
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void initUI() {
        tvTitle = _findViewById(R.id.tv_find_it_title);

        _findViewById(R.id.iv_find_it_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBack(0);
            }
        });

        _findViewById(R.id.iv_find_it_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apply();
            }
        });

        _findViewById(R.id.iv_find_it_place).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlacePicker();
            }
        });
    }

    private void onBack(int result) {
        Intent returnIntent = new Intent();
        if (result == 0) {
            setResult(Activity.RESULT_CANCELED, returnIntent);
        } else {
            setResult(Activity.RESULT_OK, returnIntent);
        }

        finish();
    }

    private void apply() {
        if (strText.equals("")) return;
        map.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                strPhoto = Utility.compressBitmap(bitmap);
                post();
            }
        });
    }

    private void post() {
        String url = URL_SERVER + URL_MANAGE_MY_POSTS;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(USER_ID, ((App)getApplication()).getCurrentUser().user_id);
            jsonRequest.put(MANAGE_TYPE, "add");

            LocationTracker mLocationTracker = new LocationTracker(this);
            double latitude  = mLocationTracker.getLatitude();
            double longitude = mLocationTracker.getLongitude();

            jsonRequest.put(POST_LOCATION_LATITUDE, latitude);
            jsonRequest.put(POST_LOCATION_LONGITUDE, longitude);
            jsonRequest.put(POST_TEXT, strText);
            jsonRequest.put(POST_PHOTO, strPhoto);

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Posting this place...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            new GetDataTask(url, jsonRequest, this).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.clear();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16);
        googleMap.moveCamera(cameraUpdate);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);

        googleMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .title(strText));

        map = googleMap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                currentLatLng = place.getLatLng();
                strText = place.getName().toString();
                tvTitle.setText(strText);
                mapView.getMapAsync(this);
            }
        }
    }

    @Override
    public void onTaskSuccess(JSONObject jsonResponse) {
        mProgressDialog.dismiss();
        onBack(1);
    }

    @Override
    public void onTaskError(String msg) {
        mProgressDialog.dismiss();
        Utility.showToast(this, msg);
    }
}
