package com.pragyamutluru.uberdemo;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double poLon;
    double doLon;
    double doLat;
    double poLat;
    LatLng driverLoc;
    LatLng riderLoc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);
        Intent intent =getIntent();
        poLat=intent.getDoubleExtra("poLat",0.0);
        poLon=intent.getDoubleExtra("poLon",0.0);
        doLat=intent.getDoubleExtra("doLat",0.0);
        doLon=intent.getDoubleExtra("doLon",0.0);

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

        // Add a marker in Sydney and move the camera
        driverLoc= new LatLng(doLat,doLon);
        riderLoc = new LatLng(poLat, poLon);
        mMap.addMarker(new MarkerOptions().position(riderLoc).title("Marker of Rider")).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.man));

        mMap.addMarker(new MarkerOptions().position(driverLoc).title("Marker of Driver").flat(true)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(riderLoc,13));

    }

    public void acceptButtonClicked(View view){
        ParseGeoPoint temp= new ParseGeoPoint(poLat,poLon);

        ParseQuery<ParseObject> obj= ParseQuery.getQuery("Request");
        obj.whereEqualTo("Location", temp);
        obj.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null&& objects!=null){
                    objects.get(0).put("DriverUsername", ParseUser.getCurrentUser().getUsername() );
                    objects.get(0).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                Toast.makeText(getApplicationContext(), "Driver name associated with request", Toast.LENGTH_SHORT).show();
                            }
                        else{
                            e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "ERROR (DRIVER REQ)", Toast.LENGTH_SHORT).show();
                        }}
                    });
                }
            }
        });

                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+Double.toString(poLat)+","+Double.toString(poLon));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);

    }
}
