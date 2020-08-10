package com.apps.inventoryapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmChangeListener;

@EActivity(R.layout.activity_item_details)
public class ItemDetails extends AppCompatActivity {

    Realm realm;

    @ViewById
    TextView NameDetails;
    @ViewById
    TextView QuantityDetails;
    @ViewById
    TextView DescriptionDetails;
    @ViewById
    Button EditDetails;
    @ViewById
    Button DeleteDetails;
    @ViewById(R.id.imageView2)
    ImageView imageView;

    @AfterViews
    public void init() {

        //Init Realm
        realm = Realm.getDefaultInstance();

        //Init Shared Prefs
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String item_uuid = prefs.getString("item_uuid", "");

        //Get Item Object
        Item item = realm.where(Item.class)
                .equalTo("uuid", item_uuid)
                .findFirst();


        //Set Text View Text
        NameDetails.setText(item.getName());
        QuantityDetails.setText(String.valueOf(item.getQuantity()));
        DescriptionDetails.setText(item.getDescription());

        if (item.getImage() != null) {
            //Get File from item's realm path string
            File savedImage = new File(item.getImage());
            //Load Picasso from Realm's image
            Picasso.get()
                    .load(savedImage)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(imageView);
        }
    }

    //Starts Edit Activity
    @Click(R.id.EditDetails)
    public void editDetails() {
        ItemEdit_.intent(this).start();
    }

    //Deletes Item Object
    @Click(R.id.DeleteDetails)
    public void delete() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String item_uuid = prefs.getString("item_uuid", "");

        //Get Item Object
        final Item item = realm.where(Item.class)
                .equalTo("uuid", item_uuid)
                .findFirst();
        //Confirmation on Delete
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Delete Item");
        builder.setMessage(String.format("Delete item %s?", item.getName()));
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (item.getImage() != null) {
                            File savedImage = new File(item.getImage());
                            savedImage.delete();
                        }
                        if (item.isValid()) {
                            realm.beginTransaction();
                            item.deleteFromRealm();
                            realm.commitTransaction();
                        }
                        finish();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    public void onResume() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String item_uuid = prefs.getString("item_uuid", "");

        super.onResume();
        Item item = realm.where(Item.class)
                .equalTo("uuid", item_uuid)
                .findFirst();

        NameDetails.setText(item.getName());
        QuantityDetails.setText(String.valueOf(item.getQuantity()));
        DescriptionDetails.setText(item.getDescription());

        if (item.getImage() != null) {
            //Get File from item's realm path string
            File savedImage = new File(item.getImage());
            //Load Picasso from Realm's image
            Picasso.get()
                    .load(savedImage)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(imageView);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

}