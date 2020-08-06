package com.apps.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;

// Hey alexis, this is just for testing if the login works but u can change everything

@EActivity (R.layout.activity_user_items)
public class UserItems extends AppCompatActivity {

    @ViewById (R.id.randomtext)
    TextView random_text;

    Realm realm;

    @AfterViews
    public void init(){
        realm = Realm.getDefaultInstance();

        // get shared preferences
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

        // store uuid in variable uuid
        String uuid = prefs.getString("uuid", null);

        // get class of uuid
        User user = realm.where(User.class)
                .equalTo("uuid", uuid)
                .findFirst();

        // get name
        String username = user.getUsername();

        // store remember variable String
        String remember = "";

        if (prefs.getBoolean("remembered", false)) {
            remember = "(User is remembered)";
        }

        // Set text of welcoming
        random_text.setText( username + "'s Items " + remember);
    }

    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}