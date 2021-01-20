package com.ak11.uberclone;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

public class ViewLocationsMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnRide;
    private String username;
    private LatLng dLocation, pLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_locations_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnRide = findViewById(R.id.btnRide);
        username=getIntent().getStringExtra("pUsername");


        btnRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> requestCar = ParseQuery.getQuery("RequestCar");
                requestCar.whereEqualTo("username",username);
                requestCar.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if(objects.size()>0 && e==null){
                            for(ParseObject object : objects){
                                object.put("driverOfMe", ParseUser.getCurrentUser().getUsername());
                                object.put("requestAccepted",true);
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null){
                                            FancyToast.makeText(ViewLocationsMapsActivity.this,"Request Sent1",
                                                    Toast.LENGTH_SHORT,FancyToast.SUCCESS,false).show();
                                            Intent googleIntent = new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://maps.google.com/maps?saddr="+dLocation.latitude+","+dLocation.longitude
                                                            +"&"+"daddr="+pLocation.latitude+","+pLocation.longitude));
                                            googleIntent.setClassName("com.google.android.apps.maps",
                                                    "com.google.android.maps.MapsActivity");

                                            startActivity(googleIntent);
                                        }
                                    }
                                });
                            }
                        }
                        else{
                            FancyToast.makeText(ViewLocationsMapsActivity.this,e.getMessage(),Toast.LENGTH_SHORT,FancyToast.ERROR,false).show();
                        }
                    }
                });

            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        pLocation = new LatLng(getIntent().getDoubleExtra("pLatitude",0),getIntent().getDoubleExtra("pLongitude",0));
        dLocation = new LatLng(getIntent().getDoubleExtra("dLatitude",0),getIntent().getDoubleExtra("dLongitude",0));
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("You"));
        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title(username));

        ArrayList<Marker> myMarker = new ArrayList<>();
        myMarker.add(driverMarker);
        myMarker.add(passengerMarker);
        for(Marker marker : myMarker){
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,50);
        mMap.animateCamera(cameraUpdate);



    }
}