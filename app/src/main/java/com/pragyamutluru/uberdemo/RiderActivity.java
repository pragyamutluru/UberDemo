package com.pragyamutluru.uberdemo;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    LocationManager locationManager;
    Marker driverMarker;
    Marker riderMarker;
    TextView infoTextView;
    LocationListener locationListener;
    Double[] result;
    LatLng userLocation;

    Button callButton;
    boolean requested=false;
    String reqID;
    android.os.Handler handler= new android.os.Handler();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }

    }

    public void updateOnParse(){

        ParseGeoPoint userloc= new ParseGeoPoint(userLocation.latitude, userLocation.longitude);
        ParseUser.getCurrentUser().put("Location", userloc);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){

                    Toast.makeText(getApplicationContext(), "Location Updated", Toast.LENGTH_SHORT).show();
                }
                else{

                    Toast.makeText(getApplicationContext(), "Location Not Updated", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });



    }
    public void logOutButtonClicked(View view){
        if(requested) {
            cancelRequest();
        }
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Toast.makeText(getApplicationContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent= new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Error in Logging Out", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    public void checkForUpdates(){
        ParseQuery<ParseObject> query= ParseQuery.getQuery("Request");
        query.whereEqualTo("Username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("DriverUsername");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null&& objects.size()>0){
                    //Textview update that driver is on the way!

                    ParseQuery<ParseUser> query= ParseUser.getQuery();
                    query.whereEqualTo("username", objects.get(0).getString("DriverUsername"));
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            if(e==null && objects.size()>0){
                                ParseGeoPoint driverLocation= objects.get(0).getParseGeoPoint("Location");
                                LatLng temp= new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude());
                                if(driverMarker!=null)
                                driverMarker.remove();
                                driverMarker=mMap.addMarker(new MarkerOptions().position(temp).title("Marker at Driver's Loc"));
                                driverMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverMarker.getPosition(),15));


                                float[] distance= new float[1];
                                Location.distanceBetween(driverLocation.getLatitude(),driverLocation.getLongitude(),userLocation.latitude,userLocation.longitude, distance);
                                infoTextView.setText("Your driver is "+distance[0]+ " meters away!");



                            }
                            else{
                                if(e!=null){
                                    e.printStackTrace();
                                }
                                infoTextView.setText("Your driver is on the way!");


                            }
                        }
                    });



                    callButton.setEnabled(false);
                    callButton.setVisibility(View.INVISIBLE);

                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(ParseUser.getCurrentUser()!=null)
                            checkForUpdates();
                    }
                }, 2000);

            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        infoTextView= (TextView) findViewById(R.id.infoTextView);

        callButton= (Button) findViewById(R.id.callButton);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        result= new Double[2];



        if(ParseUser.getCurrentUser()!=null)
        checkForUpdates();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(ParseUser.getCurrentUser()!=null)
                checkForUpdates();
            }
        }, 2000);

    }
    public void callButtonClicked(View view){
        Button temp= (Button) view;
        if(temp.getText().equals("Call Uber")){
            callUber();
        }
        else{
            cancelUber();
        }


    }

    public void callUber(){
        updateOnParse();
        newRequest();
        Toast.makeText(getApplicationContext(), "Uber called", Toast.LENGTH_LONG).show();

    }

    public void newRequest(){
        final ParseObject request= new ParseObject("Request");
        request.put("Username", ParseUser.getCurrentUser().getUsername().toString());

        ParseGeoPoint userloc= new ParseGeoPoint(userLocation.latitude, userLocation.longitude);

        request.put("Location",userloc);
        request.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null)
                {
                    Toast.makeText(getApplicationContext(), "Req saved", Toast.LENGTH_LONG).show();
                    reqID=request.getObjectId().toString();
                    callButton.setText("Cancel Uber");
                    requested=true;

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(ParseUser.getCurrentUser()!=null)
                            checkForUpdates();
                        }
                    }, 2000);



                }
                else{
                    Toast.makeText(getApplicationContext(), "Error in Request", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    requested=false;
                }
            }
        });

    }
    public void cancelRequest(){
        ParseQuery<ParseObject> query= ParseQuery.getQuery("Request");
        query.getInBackground(reqID, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e==null&&object!=null){
                    object.deleteInBackground();
                    Toast.makeText(getApplicationContext(), "Uber cancelled", Toast.LENGTH_LONG).show();

                }
                else{
                    Toast.makeText(getApplicationContext(), "Error in Cancelling Uber", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    public void cancelUber(){
        cancelRequest();
        callButton.setText("Call Uber");

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
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                updateUserLocation(userLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        if(Build.VERSION.SDK_INT<23){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        else{
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //ask
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnownLocation!=null) {
                    LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                        updateUserLocation(userLocation);
                }


            }

        }


        // Add a marker in Sydney and move the camera
    }

    public void updateUserLocation(LatLng userLocation){
        if(riderMarker!=null)
            riderMarker.remove();
        if(this.userLocation!=userLocation) {
            riderMarker=mMap.addMarker(new MarkerOptions().position(userLocation).title("Marker at User's Loc"));
            riderMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.man));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18.0f));
            this.userLocation=userLocation;

        }
    }
}


