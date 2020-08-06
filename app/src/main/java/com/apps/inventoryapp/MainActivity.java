package com.apps.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;

@EActivity (R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.username_input)
    EditText username;

    @ViewById(R.id.password_input)
    EditText password;

    @ViewById(R.id.rememberme)
    CheckBox check;

    Realm realm;

    @AfterViews
    public void init() {
        realm = Realm.getDefaultInstance();

        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // If there is a uuid in prefs
        if (prefs.getString("uuid", null) != null) {
            // if remembered is true (defaulted to false if no "remembered" in uuid)
            if (prefs.getBoolean("remembered", false)) {
                // get object of uuid
                User user = realm.where(User.class)
                        .equalTo("uuid", prefs.getString("uuid", null))
                        .findFirst();

                // Pre-set name, password, and checkbox
                username.setText(user.getUsername());
                password.setText(user.getPassword());
                check.setChecked(true);
            }
        } else {
            // If no uuid in prefs, remember me will always start false
            editor.putBoolean("remembered", false);
            check.setChecked(false);
            editor.apply();
        }
    }

    // LOGIN IS PRESSED
    @Click(R.id.signin_button)
    public void loginCheck(){

        // get user inputs
        String userInput = username.getText().toString();
        String passInput = password.getText().toString();

        // get user with username stated in input
        User checkUser = realm.where(User.class)
                .equalTo("username", userInput)
                .findFirst();

        // IF USER DOES NOT EXIST:
        if (checkUser == null) {
            Toast.makeText(this, "No User found", Toast.LENGTH_SHORT).show();
        }

        // IF USER EXISTS:
        else {
            // get password of checkUser
            String checkPass = checkUser.getPassword();

            // IF CORRECT PASSWORD:
            if (passInput.equals(checkPass)) {

                // Get shared preferences and editor
                SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                // add checkUser uuid in shared pref
                editor.putString("uuid", checkUser.getUuid());

                // apply changes
                editor.apply();

                // Sign in user
                UserItems_.intent(this).start();
            }

            // IF WRONG PASSWORD:
            else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Click (R.id.rememberme)
    public void addRememberMe() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("remembered", check.isChecked());
        editor.apply();
    }

    @Click (R.id.admin_button)
    public void admin() {
        // Start register activity
        UserAdmin_.intent(this).start();
    }

    // CLOSE REALM
    public void onDestroy()
    {
        super.onDestroy();
        realm.close();
    }
}