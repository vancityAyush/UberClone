package com.ak11.uberclone;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class ViewLocationsMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_locations_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
//
//        // Add a marker in Sydney and move the camera
//        mMap.addMarker(new MarkerOptions().position(dLocation).title("You"));
//        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dLocation,15));
//
//       );
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pLocation,15));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng pLocation = new LatLng(getIntent().getDoubleExtra("pLatitude",0),getIntent().getDoubleExtra("pLongitude",0));
        LatLng dLocation = new LatLng(getIntent().getDoubleExtra("dLatitude",0),getIntent().getDoubleExtra("dLongitude",0));
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("You"));
        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title(getIntent().getStringExtra("username")));

        ArrayList<Marker> myMarker = new ArrayList<>();
        myMarker.add(driverMarker);
        myMarker.add(passengerMarker);
        for(Marker marker : myMarker){
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,0);
        mMap.animateCamera(cameraUpdate);



    }
}