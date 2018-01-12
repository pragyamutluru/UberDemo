package com.pragyamutluru.uberdemo;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class DriverActivity extends AppCompatActivity {
    ListView listView;
    TextView textView;
    ParseGeoPoint driverLoc;
    LatLng driverLocCord;
    ArrayList<String> arrayList;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<ParseObject> parseObjectArrayList;
    LocationManager locationManager;
    LocationListener locationListener;
    android.os.Handler handler= new android.os.Handler();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                updateLoc();
            }
        }

    }

    public void updateLoc()
    {
        //Toast.makeText(getApplicationContext(), driverLocCord.toString(), Toast.LENGTH_LONG).show();
       // textView.setText(driverLocCord.toString());
        driverLoc = new ParseGeoPoint(driverLocCord.latitude, driverLocCord.longitude);
        ParseUser.getCurrentUser().put("Location", driverLoc);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Toast.makeText(getApplicationContext(), "Location Updated", Toast.LENGTH_SHORT).show();
                }
                else{

                    Toast.makeText(getApplicationContext(), "Error in Location Update", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });


        populate();

    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
       // textView= (TextView)  findViewById(R.id.textView3);
        listView=(ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position<parseObjectArrayList.size()){
                    ParseObject po= parseObjectArrayList.get(position);
                    ParseGeoPoint pog= (ParseGeoPoint)po.get("Location");
                    double poLat=pog.getLatitude();
                    double poLon= pog.getLongitude();
                    double doLat= driverLoc.getLatitude();
                    double doLon= driverLoc.getLongitude();

                    Intent intent= new Intent(getApplicationContext(), DriverMapsActivity.class);
                    intent.putExtra("poLat", poLat);
                    intent.putExtra("poLon", poLon);
                    intent.putExtra("doLat", doLat);

                    intent.putExtra("doLon", doLon);
                    startActivity(intent);

                }
            }
        });

        //Start

        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                 driverLocCord = new LatLng(location.getLatitude(), location.getLongitude());
                updateLoc();


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
            updateLoc();


        }
        else{
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //ask

                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

            }
            else{

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnownLocation!=null)
                {driverLocCord = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                updateLoc();}



            }

        }


    }

    public void logOutButtonDriverClicked(View view){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(getApplicationContext(), "Could not log out", Toast.LENGTH_SHORT).show();


                }
            }
        });
        Intent intent= new Intent(getApplication(), MainActivity.class);
        startActivity(intent);
        startActivity(intent);
    }

    public void populate(){
        ParseQuery<ParseObject> query= ParseQuery.getQuery("Request");
        query.whereDoesNotExist("DriverUsername");
        query.whereNear("Location", driverLoc);
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null&&objects!=null) {
                    parseObjectArrayList = new ArrayList<>();
                    for(Object o: objects){
                        parseObjectArrayList.add((ParseObject) o);


                    }


                    arrayList= new ArrayList<>(parseObjectArrayList.size());

                    for(int i=0; i<parseObjectArrayList.size(); i++){
                        ParseObject po= parseObjectArrayList.get(i);
                        ParseGeoPoint pog= (ParseGeoPoint)po.get("Location");

                        Geocoder geocoder= new Geocoder(getApplicationContext(), Locale.getDefault());

                        try {
                            List<Address> listAddresses = geocoder.getFromLocation(pog.getLatitude(), pog.getLongitude(), 1);
                            if(listAddresses!=null && listAddresses.size()>0) {
                                //Log.i("Place we pulled", listAddresses.get(0).toString());
                                StringBuilder str = new StringBuilder();
                                Address loc = listAddresses.get(0);

                                str.append(loc.getSubLocality()).append("\n");
                                //str.append(loc.getLocality()).append("\n");

                                //str.append(loc.getCountryName()).append("\n");
                                arrayList.add(str.toString());
                            }
//                        double poLat=pog.getLatitude();
//                        double poLon= pog.getLongitude();
//                        double doLat=driverLoc.getLatitude();
//                        double doLon=driverLoc.getLatitude();
//                        double [] dist = new double[1];
//                        dist[0]= distance(doLat,doLon,poLat,poLon,"K");
//                        arrayList.add(Double.toString(dist[0]));
                    } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        arrayAdapter= new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
                    listView.setAdapter(arrayAdapter);


                }}
                else{
                    //textView.setText("NOT DONE ERROR");
                    Toast.makeText(getApplicationContext(), "Objects NOT saved in array", Toast.LENGTH_SHORT).show();
                }
            }
        });


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                populate();
            }
        }, 2000);


    }
//
//    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
//        double theta = lon1 - lon2;
//        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
//        dist = Math.acos(dist);
//        dist = rad2deg(dist);
//        dist = dist * 60 * 1.1515;
//        if (unit == "K") {
//            dist = dist * 1.609344;
//        } else if (unit == "N") {
//            dist = dist * 0.8684;
//        }
//
//        return (dist);
//    }
//
//    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
//	/*::	This function converts decimal degrees to radians						 :*/
//	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
//    private static double deg2rad(double deg) {
//        return (deg * Math.PI / 180.0);
//    }
//
//    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
//	/*::	This function converts radians to decimal degrees						 :*/
//	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
//    private static double rad2deg(double rad) {
//        return (rad * 180 / Math.PI);
//    }
//

}
