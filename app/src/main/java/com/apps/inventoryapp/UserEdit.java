package com.apps.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;

@EActivity (R.layout.activity_user_edit)
public class UserEdit extends AppCompatActivity {

    @ViewById(R.id.edit_username_input)
    EditText name_input;

    @ViewById (R.id.edit_password_input)
    EditText password_input;

    @ViewById (R.id.edit_confirm_input)
    EditText confirm_password;

    Realm realm;

    @AfterViews
    public void init() {
        realm = Realm.getDefaultInstance();

        // Get username (of User to edit) from prefs and return the User itself
        User user = getUserFromPrefs();

        // set the inputs with defaults
        name_input.setText(user.getUsername());
        password_input.setText(user.getPassword());
        confirm_password.setText(user.getPassword());
    }

    // SAVE IS PRESSED
    @Click(R.id.edit_save_button)
    public void save() {

        // Get string of inputs
        String name_string = name_input.getText().toString();
        String pass_string = password_input.getText().toString();
        String confirm_string = confirm_password.getText().toString();

        // Get user to edit
        User user = getUserFromPrefs();

        // If name is empty, toast
        if (name_string.isEmpty()) {
            Toast.makeText(this, "Name must not be blank", Toast.LENGTH_SHORT).show();
        }

        // If password and confirm pass are the same
        else if (pass_string.equals(confirm_string) && !pass_string.equals("")) {
            // Check if valid
            if (isValid(user.getUsername(), name_string)) {
                // Edit Account
                editAccount(user, name_string, pass_string);
                // end Activity
                endActivity();
            }
            else {
                // Toast user already exists
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        // else, toast that passwords don't match
        else {
            Toast.makeText(this, "Confirm password do not match", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // If user cancels, end activity
    @Click(R.id.edit_cancel_button)
    public void cancel() {
        endActivity();
    }

    //----------------------------------------------------
    // INTERNAL FUNCTIONS

    // Getting the user to edit from prefs
    private User getUserFromPrefs() {
        // Get username of User to edit from shared preferences
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String editUser = prefs.getString("editUser", null);

        // Get the User itself from realm db
        User user = realm.where(User.class)
                .equalTo("username", editUser)
                .findFirst();
        return user;
    }

    // Search name and return true if valid
    private boolean isValid(String usernameBefore, String usernameAfter){
        // If username stays the same, it's valid
        if (usernameBefore.equals(usernameAfter)) {
            return true;
        }
        // If different username, check if other users have that username
        User result = realm.where(User.class)
                .equalTo("username", usernameAfter)
                .findFirst();
        // Valid if result is null
        return result == null;
    }

    // edit the account in realm database
    private void editAccount(User user, String name, String pass) {
        realm.beginTransaction();
        user.setUsername(name);
        user.setPassword(pass);
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
        // If uuid in shared prefs is same as user to edit, delete uuid in shared prefs
        if (user.getUuid().equals(checkUuid())) {
            deleteSharedPrefsUuid();
        }
        // toast
        Toast.makeText(this, "Edited", Toast.LENGTH_SHORT).show();
    }

    // When the activity ends, delete the user to be editted in prefs
    private void endActivity() {
        deleteEditUser();
        finish();
    }

    private void deleteEditUser() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        prefs.edit().remove("editUser").commit();
    }

    private String checkUuid() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        return prefs.getString("uuid", null);
    }

    // Just in case a user that was deleted/edited was stored in the shared prefs
    private void deleteSharedPrefsUuid() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("uuid");
        editor.apply();
    }


}