package com.ak11.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;

import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {
    enum State {LOGIN,SIGNUP}

    private State state;
    private Button btnSignUpLogin, btnOneTimeLogin;
    private EditText edtUsername, edtPassword;
    private RadioButton rbPassenger, rbDriver;
    private Switch switchUserCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        if(ParseUser.getCurrentUser()!=null){
            ParseUser.logOut();
        }

        state = State.SIGNUP;
        btnSignUpLogin=findViewById(R.id.btnSingUpLogin);
        btnOneTimeLogin= findViewById(R.id.btnOneTimeLogin);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.signup_item:
                if(state == State.SIGNUP){

                }else if(state == State.LOGIN){

                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}