package com.ak11.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    enum State {LOGIN,SIGNUP}

    private State state;
    private Button btnSignUpLogin, btnOneTimeLogin;
    private EditText edtUsername, edtPassword;
    private RadioButton rbPassenger, rbDriver;
    private ToggleButton tbUserCategory;
    private ConstraintLayout root;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        if (ParseUser.getCurrentUser() != null) {
            transitionToPassengerActivity();
            transitionToDriverActivity();
        }

        state = State.SIGNUP;
        btnSignUpLogin = findViewById(R.id.btnSingUpLogin);
        btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        rbPassenger = findViewById(R.id.rbPassenger);
        rbDriver = findViewById(R.id.rbDriver);
        tbUserCategory = findViewById(R.id.tbUserCategory);
        root=findViewById(R.id.root_main);
        radioGroup=findViewById(R.id.radioGroup);

        btnSignUpLogin.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        btnSignUpLogin.setOnClickListener(this);
        btnOneTimeLogin.setOnClickListener(this);
        root.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item:
                if(state == State.SIGNUP){
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogin.setText("Login");
                    btnSignUpLogin.setBackgroundTintList(ColorStateList.valueOf(Color.CYAN));
                    radioGroup.animate().alpha(0f).setDuration(500).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            radioGroup.setVisibility(View.GONE);

                        }
                    });

                }else {
                    state = State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUpLogin.setText("Signup");
                    btnSignUpLogin.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    radioGroup.animate().alpha(1f).setDuration(500).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            radioGroup.setVisibility(View.VISIBLE);
                        }
                    });

                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSingUpLogin:

                if(state == State.SIGNUP){
                    if(rbDriver.isChecked()==rbPassenger.isChecked()) {
                        Toast.makeText(MainActivity.this, "Are you a Driver or a Passenger?", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUsername.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    appUser.put("SpyPassword",edtPassword.getText().toString());

                    if(rbDriver.isChecked())
                        appUser.put("as","Driver");
                    else if (rbPassenger.isChecked())
                        appUser.put("as", "Passenger");

                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                FancyToast.makeText(MainActivity.this,"Signed Up Successfully",Toast.LENGTH_SHORT,
                                        FancyToast.SUCCESS,false).show();
                                transitionToPassengerActivity();
                                transitionToDriverActivity();
                            }else
                                FancyToast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT,
                                        FancyToast.ERROR,false).show();
                        }
                    });

                }
                else if (state == State.LOGIN){
                    ParseUser.logInInBackground(edtUsername.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(user!=null &&  e==null) {
                                FancyToast.makeText(MainActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT,
                                        FancyToast.SUCCESS, false).show();
                                transitionToPassengerActivity();
                                transitionToDriverActivity();

                            }
                            else
                                FancyToast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT,
                                        FancyToast.ERROR,false).show();
                        }
                    });}
                    break;
            case  R.id.btnOneTimeLogin:
                if(ParseUser.getCurrentUser()==null){
                    ParseAnonymousUtils.logIn(new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(user!=null && e==null){
                                FancyToast.makeText(MainActivity.this,"We have an anonymous user",
                                        Toast.LENGTH_SHORT,FancyToast.SUCCESS,false).show();
                                String userCategory = tbUserCategory.isChecked()?"Driver":"Passenger";
                                user.put("as",userCategory);
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null){
                                            transitionToPassengerActivity();
                                            transitionToDriverActivity();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }

                break;
            case R.id.root_main:
                try{
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }

    }
    private void transitionToPassengerActivity(){
        if(ParseUser.getCurrentUser()!=null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent = new Intent(MainActivity.this,PassengersActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
    private void transitionToDriverActivity(){
        if(ParseUser.getCurrentUser()!=null){
            if(ParseUser.getCurrentUser().get("as").equals("Driver")){
                Intent intent = new Intent(MainActivity.this,DriverRequestListActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}