package com.apps.inventoryapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;

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

    @AfterViews
    public void init(){
        //Init Realm
        realm = Realm.getDefaultInstance();

        //Init Shared Prefs
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String item_uuid = prefs.getString("item_uuid","");

        //Get Item Object
        Item item = realm.where(Item.class)
                .equalTo("uuid", item_uuid)
                .findFirst();

        //Set Text View Text
        NameDetails.setText(item.getName());
        QuantityDetails.setText(String.valueOf(item.getQuantity()));
        DescriptionDetails.setText(item.getDescription());
    }
    //Starts Edit Activity
    @Click(R.id.EditDetails)
    public void editDetails(){
        ItemEdit_.intent(this).start();
    }
    //Deletes Item Object
    @Click(R.id.DeleteDetails)
    public void delete(){
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String item_uuid = prefs.getString("item_uuid","");

        //Get Item Object
        final Item item = realm.where(Item.class)
                .equalTo("uuid", item_uuid)
                .findFirst();
        //Confirmation on Delete
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Delete Item");
        builder.setMessage(String.format("Delete item %s?",item.getName()));
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (item.isValid()){
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
                //do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    public void onDestroy(){
        super.onDestroy();
        realm.close();
        ItemInventory_.intent(this).start();
    }
}