package com.apps.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

@EActivity (R.layout.activity_admin)
public class UserAdmin extends AppCompatActivity {

    Realm realm;

    @ViewById
    RecyclerView user_recycler;

    UserAdapter adapter;

    @AfterViews
    public void init() {
        // init realm
        realm = Realm.getDefaultInstance();

        // Layout Manager for RecyclerView
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        user_recycler.setLayoutManager(mLayoutManager);

        // get list of users
        RealmResults<User> list = realm.where(User.class)
                .findAll()
                .sort("username", Sort.ASCENDING); // you can add some sorting here

        // set adapter to recyclerView
        adapter = new UserAdapter(this, list, true);
        user_recycler.setAdapter(adapter);
    }

    // ADD BUTTON IS PRESSED
    @Click(R.id.add_button)
    public void register() {
        // Initiate register activity
        UserRegister_.intent(this).start();
    }

    // CLEAR BUTTON IS PRESSED
    @Click (R.id.clear_button)
    public void clear() {
        // Delete all users
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        deleteSharedPrefsUuid(); // make sure no uuid is in prefs
    }

    // CALLED FROM ADAPTER
    public void edit(View view) {
        // Get tag
        User user = (User) view.getTag();
        // Add username of user to shared prefs
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("editUser", user.getUsername());
        editor.apply();
    }

    // CALLED FROM ADAPTER
    public void delete(View view) {
        // Get tag
        User user = (User) view.getTag();
        // double-click catch
        if (user.isValid()) {
            // If uuid in shared prefs is same as user to delete, delete uuid in shared prefs
            if (user.getUuid().equals(checkUuid())) {
                deleteSharedPrefsUuid();
            }
            // delete user
            realm.beginTransaction();
            user.deleteFromRealm();
            realm.commitTransaction();
        }
    }

    // CLOSE REALM
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    //---------------------------------------------------------------------------------
    // INTERNAL FUNCTIONS
    private void deleteSharedPrefsUuid() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("uuid");
        editor.apply();
    }

    private String checkUuid() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        return prefs.getString("uuid", null);
    }
    //---------------------------------------------------------------------------------
}