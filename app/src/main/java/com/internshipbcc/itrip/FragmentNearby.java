package com.internshipbcc.itrip;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.internshipbcc.itrip.Util.CustomWindowInfoAdapter;

import java.io.IOException;
import java.util.List;

/**
 * Created by Sena on 18/03/2018.
 */

public class FragmentNearby extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 648;
    public boolean mLocationPermissionGranted = false;
    MapView mMapView;
    GoogleMap gMap;

    GeoDataClient mGeoDataClient;
    PlaceDetectionClient mPlaceDetectionClient;
    FusedLocationProviderClient mFusedLocationProviderClient;

    RelativeLayout rlInfoWindowDim;
    CardView cvInfoWindow, btnUpdateLocation;
    Location mLastKnownLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mMapView.getMapAsync(this);

        rlInfoWindowDim = rootView.findViewById(R.id.info_window_dim_layout);
        cvInfoWindow = rootView.findViewById(R.id.cv_info_window);
        rlInfoWindowDim.setOnClickListener(this);
        cvInfoWindow.setTranslationY(dpToPx(150));

        btnUpdateLocation = rootView.findViewById(R.id.btn_update_location);
        btnUpdateLocation.setOnClickListener(v -> {
            updateLocationUI();
            getDeviceLocation();
        });

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;

        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        //Set all marker
        setAllMarker();
        gMap.setInfoWindowAdapter(new CustomWindowInfoAdapter(getActivity()));
        gMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null)
            mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null)
            mMapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (gMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                gMap.setMyLocationEnabled(true);
//                gMap.getUiSettings().setMyLocationButtonEnabled(true);
                gMap.getUiSettings().setMapToolbarEnabled(true);
                gMap.getUiSettings().setAllGesturesEnabled(true);
                gMap.getUiSettings().setCompassEnabled(true);

            } else {
                gMap.setMyLocationEnabled(false);
                gMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = (Location) task.getResult();
                        if (mLastKnownLocation != null)
                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 14f));
                        else {
                            LatLng malang = new LatLng(-7.977266, 112.634030);
                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malang, 13f));
                            gMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    } else {
                        Log.d("itrip maps", "Current location is null. Using defaults.");
                        Log.e("itrip maps", "Exception: %s", task.getException());
                        LatLng malang = new LatLng(-7.977266, 112.634030);
                        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malang, 13f));
                        gMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int dpToPx(int dp) {
        try {
            DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
            float px = dp * (metrics.densityDpi / 160f);
            return Math.round(px);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        rlInfoWindowDim.setVisibility(View.VISIBLE);
        cvInfoWindow.animate().translationY(0).setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.anim.decelerate_interpolator));

//            float container_height = getResources().getDimension(R.dimen.DP_300);
//
        Projection projection = gMap.getProjection();

        Point markerScreenPosition = projection.toScreenLocation(marker.getPosition());
        Point pointHalfScreenAbove = new Point(markerScreenPosition.x, markerScreenPosition.y);

        LatLng aboveMarkerLatLng = projection.fromScreenLocation(pointHalfScreenAbove);
        CameraUpdate center = CameraUpdateFactory.newLatLng(aboveMarkerLatLng);
//            gMap.moveCamera(center);
        gMap.animateCamera(center);

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_window_dim_layout:
                rlInfoWindowDim.setVisibility(View.GONE);
                cvInfoWindow.animate().translationY(dpToPx(150)).setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.anim.decelerate_interpolator));
                break;

        }
    }

    private void setAllMarker() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("/items/");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    String id = item.getKey();
                    String title = item.child("title").getValue(String.class);
                    String location = item.child("location").getValue(String.class);
                    String desc = item.child("desc").getValue(String.class);
                    desc = desc.substring(0, desc.length() > 20 ? 20 : desc.length()) + "...";
                    Geocoder geocoder = new Geocoder(getActivity());
                    List<Address> address;
                    try {
                        address = geocoder.getFromLocationName(title + " " + location, 3);
                        if (!address.isEmpty()) {
                            Address alamat = address.get(0);
                            gMap.addMarker(new MarkerOptions()
                                    .title(title)
                                    .snippet(id + "," + desc)
                                    .position(new LatLng(alamat.getLatitude(), alamat.getLongitude())));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
