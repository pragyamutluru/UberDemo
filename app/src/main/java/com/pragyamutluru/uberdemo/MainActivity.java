package com.pragyamutluru.uberdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import static com.pragyamutluru.uberdemo.R.color.cast_expanded_controller_background_color;

public class MainActivity extends AppCompatActivity {
    //TextView text;
    LinearLayout linearLayout;
    ImageView image;
    Switch aSwitch;

    public void changeImage(View view){
        image.setImageResource(R.color.cast_expanded_controller_background_color);
        linearLayout.setVisibility(View.VISIBLE);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout= (LinearLayout) findViewById(R.id.linearLayout);
        // text=(TextView) findViewById(R.id.text);
        image= (ImageView) findViewById(R.id.imageView);
        aSwitch=(Switch) findViewById(R.id.switch1);
        if(ParseUser.getCurrentUser()==null){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e==null && user!=null){
                       // Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_LONG).show();
                    }
                    else{

                        Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


        if(ParseUser.getCurrentUser()!=null && ParseUser.getCurrentUser().get("riderOrDriver")!=null){
            String str= ParseUser.getCurrentUser().get("riderOrDriver").toString();
            if(str.equals("rider")){

                riderLogin();
            }
            else{
                driverLogin();
            }

        }


    }
    public void driverLogin(){
        if(ParseUser.getCurrentUser()==null){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e==null && user!=null){
                       // Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_LONG).show();
                    }
                    else{

                        Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        Toast.makeText(MainActivity.this, "Driver Login", Toast.LENGTH_LONG).show();
        ParseUser.getCurrentUser().put("riderOrDriver", "driver");
        ParseUser.getCurrentUser().saveInBackground();


        startDriverActivity();
    }
    public void riderLogin(){


            Toast.makeText(MainActivity.this, "Rider Login", Toast.LENGTH_SHORT).show();
            ParseUser.getCurrentUser().put("riderOrDriver", "rider");
            ParseUser.getCurrentUser().saveInBackground();

//        String str= ParseUser.getCurrentUser().get("riderOrDriver").toString();
//        Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();


        startRiderActivity();
    }

    public void startRiderActivity(){
        Intent intent= new Intent(getApplicationContext(), RiderActivity.class);
        startActivity(intent);
    }
    public void startDriverActivity(){
        Intent intent= new Intent(getApplicationContext(), DriverActivity.class);
        startActivity(intent);
    }


    public void onClick(View view){
        if(aSwitch.isChecked()){
            driverLogin();
        }
        else{
            riderLogin();
        }
    }
}
