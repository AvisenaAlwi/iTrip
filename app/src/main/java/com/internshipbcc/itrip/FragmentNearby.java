package com.internshipbcc.itrip;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.internshipbcc.itrip.Util.CalculateShortest;
import com.internshipbcc.itrip.Util.CustomWindowInfoAdapter;
import com.internshipbcc.itrip.Util.HTM;
import com.internshipbcc.itrip.Util.LocationKu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sena on 18/03/2018.
 */

public class FragmentNearby extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 648;
    public boolean mLocationPermissionGranted = false;
    public boolean isBudgetCalculatorShow = false;
    MapView mMapView;
    GoogleMap gMap;
    GeoDataClient mGeoDataClient;
    PlaceDetectionClient mPlaceDetectionClient;
    FusedLocationProviderClient mFusedLocationProviderClient;
    RelativeLayout rlInfoWindowDim;
    CardView cvInfoWindow, cvBudgetCalculate, btnUpdateLocation, btnBudgetCalculate;
    Location mLastKnownLocation;
    ConstraintLayout constraintResult;
    EditText etBudget;
    Button btnCalculate;
    SwitchCompat switchTransport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        // Construct a GeoDataClient.
        if (getActivity() != null)
            mGeoDataClient = Places.getGeoDataClient(getActivity(), null);
        else return null;
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mMapView.getMapAsync(this);

        rlInfoWindowDim = rootView.findViewById(R.id.info_window_dim_layout);
        cvInfoWindow = rootView.findViewById(R.id.cv_info_window);
        rlInfoWindowDim.setOnClickListener(this);
        cvInfoWindow.getChildAt(0).setOnClickListener(this);
        cvInfoWindow.setTranslationY(dpToPx(150));

        cvBudgetCalculate = rootView.findViewById(R.id.cv_budget_calculate);
        cvBudgetCalculate.setTranslationY(dpToPx(1200));
        cvBudgetCalculate.findViewById(R.id.btn_close).setOnClickListener(v -> toogleBudgetCalculator());

        constraintResult = rootView.findViewById(R.id.constraint_result);
        constraintResult.setVisibility(View.GONE);

        btnUpdateLocation = rootView.findViewById(R.id.btn_update_location);
        btnUpdateLocation.setOnClickListener(v -> {
            updateLocationUI();
            getDeviceLocation();
        });
        btnBudgetCalculate = rootView.findViewById(R.id.btn_budget_calculate);
        btnBudgetCalculate.setOnClickListener(v -> {
            toogleBudgetCalculator();
        });

        etBudget = rootView.findViewById(R.id.et_budget);
        btnCalculate = rootView.findViewById(R.id.btn_calculate);
        btnCalculate.setOnClickListener(this);
        switchTransport = rootView.findViewById(R.id.switch_transport);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;

        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Turn on the My LocationKu layer and the related control on the map.
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
        try {
            if (ContextCompat.checkSelfPermission(getContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                                            mLastKnownLocation.getLongitude()), 13f));
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
        //Set text i and All
        String[] snippet = marker.getSnippet().split(",");
        String id = snippet[0];
        String desc = snippet[1];
        String image = snippet[snippet.length - 3];
        ImageView imgInfo = cvInfoWindow.findViewById(R.id.img_info_window);
        TextView tvId = cvInfoWindow.findViewById(R.id.tv_id_hide);
        TextView tvTtile = cvInfoWindow.findViewById(R.id.tv_info_window_title);
        TextView tvDesc = cvInfoWindow.findViewById(R.id.tv_info_window_desc);
        TextView tvHtm = cvInfoWindow.findViewById(R.id.tv_info_window_htm);

        if (getActivity() != null)
            Glide.with(getActivity())
                    .load(image)
                    .thumbnail(0.5f)
                    .into(imgInfo);
        tvId.setText(id);
        tvTtile.setText(marker.getTitle());
        tvDesc.setText(desc);
        tvHtm.setText("Rp." + HTM.getHarga(snippet[snippet.length - 2] + "," + snippet[snippet.length - 1]));

//        rlInfoWindowDim.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        anim.setDuration(200);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                rlInfoWindowDim.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        rlInfoWindowDim.startAnimation(anim);

        cvInfoWindow.animate().translationY(0).setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.anim.decelerate_interpolator));

//            float container_height = getResources().getDimension(R.dimen.DP_300);
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
//                rlInfoWindowDim.setVisibility(View.GONE);
                Animation anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
                anim.setDuration(200);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        rlInfoWindowDim.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                rlInfoWindowDim.startAnimation(anim);
                cvInfoWindow.animate().translationY(dpToPx(150)).setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.anim.decelerate_interpolator));
                break;
            case R.id.card_view_inner:
                String id = ((TextView) cvInfoWindow.findViewById(R.id.tv_id_hide)).getText().toString();
                Intent i = new Intent(getActivity(), ViewDetails.class);
                i.putExtra("id", id);
                if (getActivity() != null)
                    getActivity().startActivity(i);
                break;
            case R.id.btn_calculate:
                calculate();
                break;
            case R.id.img_gojek:
                openGojek();
                break;
        }
    }

    private void calculate() {
        if (etBudget.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(getActivity(), "Masukkan budget Anda.", Toast.LENGTH_LONG).show();
            return;
        }
        int budget = Integer.parseInt(etBudget.getText().toString());
        ProgressDialog pg = new ProgressDialog(getActivity());
        pg.setIndeterminate(true);
        pg.show();
        ArrayList<LocationKu> data = new ArrayList<>();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("items");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    String id = item.getKey();
                    String title = item.child("title").getValue(String.class);
                    double jarak = 0;
                    if (title != null) {
                        if (title.contains("Brawijaya"))
                            jarak = 3.6;
                        else if (title.contains("Jodipan"))
                            jarak = 5.3;
                        else if (title.contains("Hawai"))
                            jarak = 9.8;
                        else jarak = 15;
                    }
                    data.add(
                            new LocationKu(id, title, jarak,
                                    HTM.getHarga(item.child("htm").getValue(String.class)), 0)
                    );
                }
                ArrayList<LocationKu> rekom = CalculateShortest.findRecommendation(data, budget, switchTransport.isChecked());
                Collections.sort(rekom, (o1, o2) -> {
                    if (o1.totalKebutuhan < o2.totalKebutuhan)
                        return -1;
                    else if (o1.totalKebutuhan > o2.totalKebutuhan)
                        return 1;
                    else return 0;
                });

                if (!rekom.isEmpty()) {
                    String html = "<h4>Rekomendasi Kami</h4>" +
                            "<b>Tempat Wisata : </b> " + rekom.get(0).name + "<br>" +
                            "<b>Tiket Masuk : </b>Rp." + rekom.get(0).htm + " KM<br>" +
                            "<b>Jarak dari tempat Anda : </b>" + rekom.get(0).distance + " KM<br>" +
                            "<b>Transportasi (GOJEK) PP : </b>Rp." + rekom.get(0).distance * 4000 + "<br>" +
                            "<b>Total Biaya : </b>Rp" + rekom.get(0).totalKebutuhan + "<br>" +
                            "<b>Anda Hemat : </b>Rp." + (budget - rekom.get(0).totalKebutuhan) + "<br>";
                    ((TextView) constraintResult.findViewById(R.id.tv_result)).setText(Html.fromHtml(html));
                    String image = dataSnapshot.child(rekom.get(0).id).child("images").getValue(String.class).split(",")[0];
                    Glide.with(getActivity())
                            .load(image)
                            .thumbnail(0.7f)
                            .into((ImageView) constraintResult.findViewById(R.id.img_result));

                    constraintResult.findViewById(R.id.btn_lihat_result).setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ViewDetails.class);
                        intent.putExtra("id", rekom.get(0).id);
                        intent.putExtra("title", rekom.get(0).name);
                        intent.putExtra("imageLink", image);
                        getActivity().startActivity(intent);
                    });
                    constraintResult.setVisibility(View.VISIBLE);
                    constraintResult.findViewById(R.id.img_gojek).setOnClickListener(FragmentNearby.this);
                    constraintResult.findViewById(R.id.btn_lihat_result).setVisibility(View.VISIBLE);
                    constraintResult.findViewById(R.id.textView7).setVisibility(View.VISIBLE);
                    constraintResult.findViewById(R.id.img_gojek).setVisibility(View.VISIBLE);
                } else {
                    ((ImageView) constraintResult.findViewById(R.id.img_result)).setImageBitmap(null);
                    ((TextView) constraintResult.findViewById(R.id.tv_result)).setText(Html.fromHtml("<center><h5>Tidak ditemukan tempat wisata yang cocok dengan budget Anda<h5/></center>"));
                    constraintResult.findViewById(R.id.btn_lihat_result).setVisibility(View.GONE);
                    constraintResult.findViewById(R.id.textView7).setVisibility(View.GONE);
                    constraintResult.findViewById(R.id.img_gojek).setVisibility(View.GONE);
                    constraintResult.setVisibility(View.VISIBLE);
                }

                pg.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setAllMarker() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("/items/");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    String location = item.child("location").getValue(String.class);
                    String id = item.getKey();
                    String title = item.child("title").getValue(String.class);
//                        String location = item.child("location").getValue(String.class);
                    String desc = item.child("desc").getValue(String.class);
                    String htm = item.child("htm").getValue(String.class);
                    String image = item.child("images").getValue(String.class).split(",")[0];
                    desc = desc.substring(0, desc.length() > 100 ? 100 : desc.length()) + "...";
                    if (getActivity() != null) {
                        Geocoder geocoder = new Geocoder(getActivity());
                        List<Address> address;
                        try {
                            address = geocoder.getFromLocationName(title + " " + location, 1);
                            if (!address.isEmpty()) {
                                Address alamat = address.get(0);
                                gMap.addMarker(new MarkerOptions()
                                        .title(title)
                                        .snippet(id + "," + desc + "," + image + "," + htm)
                                        .position(new LatLng(alamat.getLatitude(), alamat.getLongitude()))
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.loc_50px)));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void toogleBudgetCalculator() {
        if (isBudgetCalculatorShow)
            closeBudgetCalculator();
        else
            showBudgetCalculator();

        if (getActivity() != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showBudgetCalculator() {
        if (!isBudgetCalculatorShow) {
            isBudgetCalculatorShow = true;
            cvBudgetCalculate.animate()
                    .setInterpolator(new DecelerateInterpolator())
                    .translationY(0);
        }
    }

    private void closeBudgetCalculator() {
        if (isBudgetCalculatorShow) {
            isBudgetCalculatorShow = false;
            cvBudgetCalculate.animate()
                    .setInterpolator(new DecelerateInterpolator())
                    .translationY(dpToPx(1200));
            constraintResult.setVisibility(View.GONE);
            constraintResult.findViewById(R.id.btn_lihat_result).setOnClickListener(null);
            etBudget.setText("");
            ((TextView) constraintResult.findViewById(R.id.tv_result)).setText("");
            ((ImageView) constraintResult.findViewById(R.id.img_result)).setImageBitmap(null);
        }
    }


    public void openGojek() {
        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.gojek.app");
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.gojek.app"));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
    }
}
