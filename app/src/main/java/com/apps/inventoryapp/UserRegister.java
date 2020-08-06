package com.apps.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.UUID;

import io.realm.Realm;

@EActivity (R.layout.activity_user_register)
public class UserRegister extends AppCompatActivity {

    @ViewById(R.id.register_username_input)
    EditText name_input;

    @ViewById (R.id.register_password_input)
    EditText password_input;

    @ViewById (R.id.register_confirm_input)
    EditText confirm_input;

    Realm realm;

    @AfterViews
    public void init() {
        realm = Realm.getDefaultInstance();
    }


    @Click(R.id.register_register_button)
    public void register() {
        // Get string of inputs
        String name_string = name_input.getText().toString();
        String pass_string = password_input.getText().toString();
        String confirm_string = confirm_input.getText().toString();

        // If name is empty, toast
        if (name_string.isEmpty()) {
            Toast.makeText(this, "Name must not be blank", Toast.LENGTH_SHORT).show();
        }

        // If password and confirm pass are the same and is not empty
        else if (pass_string.equals(confirm_string) && !pass_string.equals("")){
            // Check if valid
            if (isValid(name_string)) {
                // Create Account
                createAccount(name_string, pass_string);
                // end Activity
                finish();
            }
            else {
                // Toast user already exists
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else {
            // else, toast that passwords don't match
            Toast.makeText(this, "Confirm password do not match", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // If user cancels, end activity
    @Click (R.id.register_cancel_button)
    public void cancel() {
        finish();
    }

    // Search name and return true if valid
    private boolean isValid(String name){
        User result = realm.where(User.class)
                .equalTo("username", name)
                .findFirst();
        return result == null;
    }

    // Create new object user
    private void createAccount(String name, String pass) {
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        // Set username and password
        user.setUsername(name);
        user.setPassword(pass);
        // call to insert data in db
        insertData(user);
    }

    // Insert into realm db
    private void insertData(User user) {
        long count; // initialize count
        realm.beginTransaction(); // begin transaction
        realm.copyToRealmOrUpdate(user);  // save (add the item)
        count = realm.where(User.class).count(); // how many users are in there
        realm.commitTransaction(); // end transaction
        Toast.makeText(this, "New User saved. Total: " + count, Toast.LENGTH_LONG)
                .show();
    }

    // CLOSE REALM
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}