package com.ak11.uberclone;

import com.parse.Parse;
import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("au0MDIVlkAxt91XCxQQX5y0PTfdaKGeanFaIkkAC")
                // if defined
                .clientKey("bfPwCfKuCVtN234Tlf6PesVJKtQhxX28BI5zaIUZ")
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}