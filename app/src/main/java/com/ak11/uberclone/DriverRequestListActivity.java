package com.ak11.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.security.acl.Permission;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private  ListView listView;
    private ArrayList<String> driveRequests;
    private ArrayAdapter adapter;
    private ArrayList<Double> passengerLatitudes;
    private ArrayList<Double> passengerLongitudes;
    private ArrayList<String> passengerUsername;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);
        listView  = findViewById(R.id.requestListView);
        driveRequests = new ArrayList<>();
        passengerLatitudes=new ArrayList<>();
        passengerLongitudes = new ArrayList<>();
        passengerUsername = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, driveRequests);
        listView.setAdapter(adapter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if(ContextCompat.checkSelfPermission(DriverRequestListActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT <23){
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    updateRequestListView(location);
                }
                };
            }

        //Refresh Button codes
        swipeRefreshLayout.setOnRefreshListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(ContextCompat.checkSelfPermission(DriverRequestListActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Intent intent = new Intent(DriverRequestListActivity.this,ViewLocationsMapsActivity.class);
                    if(cdLocation!=null) {
                        intent.putExtra("dLatitude", cdLocation.getLatitude());
                        intent.putExtra("dLongitude", cdLocation.getLongitude());
                        intent.putExtra("pLatitude", passengerLatitudes.get(position));
                        intent.putExtra("pLongitude", passengerLongitudes.get(position));
                        intent.putExtra("pUsername", passengerUsername.get(position));

                        startActivity(intent);
                    }

                }
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
    }

    private void updateRequestListView(Location driverLocation) {
        if(driverLocation!=null) {
            ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(),driverLocation.getLongitude());
            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation",driverCurrentLocation);
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null) {
                        if (objects.size() > 0) {
                            if(driveRequests.size()>0)
                                driveRequests.clear();
                            if(passengerLongitudes.size()>0)
                                passengerLongitudes.clear();
                            if(passengerLatitudes.size()>0)
                                passengerLatitudes.clear();
                            for (ParseObject nearRequest : objects) {

                                ParseGeoPoint passengerGeoPoint = (ParseGeoPoint)  nearRequest.get("passengerLocation");
                                Double myDistanceToPassenger = driverCurrentLocation.distanceInKilometersTo(passengerGeoPoint);
                                float roundedDistanceValue = Math.round(myDistanceToPassenger * 100) / 100;
                                String username = (String) nearRequest.get("username");


                                passengerLatitudes.add(passengerGeoPoint.getLatitude());
                                passengerLongitudes.add(passengerGeoPoint.getLongitude());
                                passengerUsername.add(username);
                                driveRequests.add("There are " + roundedDistanceValue + " km to " + username);
                                adapter.notifyDataSetChanged();
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        } else {
                            FancyToast.makeText(DriverRequestListActivity.this, "Sorry no Requests here", Toast.LENGTH_SHORT,
                                    FancyToast.INFO, false);
                        }
                    }
                    else{
                        FancyToast.makeText(DriverRequestListActivity.this, e.getMessage(), Toast.LENGTH_SHORT,
                                FancyToast.ERROR, false);

                    }
                }
            });
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_driver_request_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.logout_item){
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if(e==null) {
                        finish();
                        startActivity(new Intent(DriverRequestListActivity.this, MainActivity.class));
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1000 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            updateRequestListView(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }
    }

    @Override
    public void onRefresh() {

            if(Build.VERSION.SDK_INT <23) {
                @SuppressLint("MissingPermission")
                Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentLocation);
            }
            else{
                if(ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(DriverRequestListActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);
                }
                else{
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateRequestListView(currentDriverLocation);

                }

            }
        }
    }