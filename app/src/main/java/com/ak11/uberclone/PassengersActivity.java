package com.ak11.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PassengersActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Button btnRequestCar;
    private boolean isRideCancelled = true;
    private boolean isCarReady = false;
    private Timer t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passengers);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRequestCar = findViewById(R.id.btnRequestCar);
        btnRequestCar.setOnClickListener(this);

        ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
        carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects.size()>0 && e==null){
                    changeButton();
                    getDriverUpdates();
                }
            }
        });

        findViewById(R.id.btnLogoutPassengerActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 ParseUser.logOutInBackground(new LogOutCallback() {
                     @Override
                     public void done(ParseException e) {
                         if(e==null){
                             startActivity(new Intent(PassengersActivity.this,MainActivity.class));
                         }
                     }
                 });
            }
        });

        findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDriverUpdates();
            }
        });
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateCameraPassengerLocation(location);
            }
        };
        if(Build.VERSION.SDK_INT<23){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }else {
            if(ContextCompat.checkSelfPermission(PassengersActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(PassengersActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);

            }

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1000 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            updateCameraPassengerLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }
    }
    private void updateCameraPassengerLocation(Location pLocation){
        if(!isCarReady) {
            LatLng passengerLocation = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15));
            mMap.addMarker(new MarkerOptions().position(passengerLocation)).setTitle("You are Here!");
        }

    }

    @Override
    public void onClick(View v) {
        if(isRideCancelled) {
            if (ContextCompat.checkSelfPermission(PassengersActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location passengerCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (passengerCurrentLocation != null) {
                    ParseObject requestCar = new ParseObject("RequestCar");
                    requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                    ParseGeoPoint userLocation = new ParseGeoPoint(passengerCurrentLocation.getLatitude(),
                            passengerCurrentLocation.getLongitude());
                    requestCar.put("passengerLocation", userLocation);
                    requestCar.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                FancyToast.makeText(PassengersActivity.this, "A car request is sent!",
                                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                                changeButton();
                            } else {
                                FancyToast.makeText(PassengersActivity.this, e.getMessage(),
                                        FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();

                            }
                        }
                    });
                }
                else {
                    FancyToast.makeText(PassengersActivity.this, "Unknown Error! Something went wrong.", Toast.LENGTH_SHORT,
                            FancyToast.ERROR, false).show();
                }
            }
        }
        else
        {
            ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
            carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> requestList, ParseException e) {
                    if(requestList.size()>0 && e==null){
                        changeButton();
                        for (ParseObject carRequest : requestList){
                            carRequest.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null){
                                        FancyToast.makeText(PassengersActivity.this, "Requests deleted",Toast.LENGTH_SHORT,
                                                FancyToast.INFO,false).show();
                                    }
                                    else {
                                        FancyToast.makeText(PassengersActivity.this, e.getMessage(),Toast.LENGTH_SHORT,
                                                FancyToast.ERROR,false).show();

                                    }
                                }
                            });
                        }

                    }
                }
            });
        }
    }
    private void changeButton(){
        if(isRideCancelled){
            btnRequestCar.setText("Cancel your ride!");
            btnRequestCar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.btn1)));
            isRideCancelled=false;
        }
        else{
            isRideCancelled=true;
            btnRequestCar.setText("Request a car!");
            btnRequestCar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.btn2)));

        }
    }
    private  void getDriverUpdates(){
        t= new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ParseQuery<ParseObject> requestDriverQuery = ParseQuery.getQuery("RequestCar");
                requestDriverQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                requestDriverQuery.whereEqualTo("requestAccepted",true);
                requestDriverQuery.whereExists("driverOfMe");

                requestDriverQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size() > 0 && e == null) {
                            for (ParseObject requestObject : objects) {
                                ParseQuery<ParseUser> driver = ParseUser.getQuery();
                                driver.whereEqualTo("username", requestObject.getString("driverOfMe"));
                                driver.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> drivers, ParseException e) {
                                        if (objects.size() > 0 && e == null) {
                                            isCarReady = true;
                                            for (ParseUser driver : drivers) {
                                                ParseGeoPoint driverLocation = driver.getParseGeoPoint("driverLocation");
                                                @SuppressLint("MissingPermission") Location passengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                                ParseGeoPoint pLocationGeoPoint = new ParseGeoPoint(passengerLocation.getLatitude(), passengerLocation.getLongitude());
                                                double kmsDistance = driverLocation.distanceInKilometersTo(pLocationGeoPoint);
                                                 if (kmsDistance<0.1){
                                                     requestObject.deleteInBackground(new DeleteCallback() {
                                                         @Override
                                                         public void done(ParseException e) {

                                                             isCarReady=false;
                                                             isRideCancelled=false;
                                                             changeButton();
                                                             FancyToast.makeText(PassengersActivity.this,"Your car is arrived",
                                                                     Toast.LENGTH_SHORT,FancyToast.SUCCESS,false).show();

                                                         }
                                                     });
                                                }
                                                float roundedDistance = Math.round(kmsDistance * 100) / 100;
                                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                LatLng pLocation = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
                                                LatLng dLocation = new LatLng(pLocationGeoPoint.getLatitude(), pLocationGeoPoint.getLongitude());
                                                Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation));
                                                Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Driver Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

                                                ArrayList<Marker> myMarker = new ArrayList<>();
                                                myMarker.add(driverMarker);
                                                myMarker.add(passengerMarker);
                                                for (Marker marker : myMarker) {
                                                    builder.include(marker.getPosition());
                                                }
                                                LatLngBounds bounds = builder.build();
                                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                                                mMap.animateCamera(cameraUpdate);

                                            }
                                        }
                                    }
                                });


                            }
                        }
                        else {
                            isCarReady=false;
                            // e.printStackTrace();
                        }

                    }
                });

            }
        },0,3000);

    }

}