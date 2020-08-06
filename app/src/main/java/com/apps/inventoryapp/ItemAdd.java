package com.apps.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.UUID;

import io.realm.Realm;

@EActivity(R.layout.activity_item_add)
public class ItemAdd extends AppCompatActivity {

    Realm realm;

    @ViewById
    EditText ItemName;
    @ViewById
    EditText ItemQuantity;
    @ViewById
    EditText ItemDescription;
    @ViewById
    Button add_item_button;
    @ViewById
    Button cancel_item_button;

    @AfterViews
    public void init(){
        realm = Realm.getDefaultInstance();
    }

    //Click Listener for Add Button
    @Click(R.id.add_item_button)
    public void addItem(){
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String uuid = prefs.getString("uuid","");

        String name = ItemName.getText().toString();
        int quantity = Integer.parseInt(ItemQuantity.getText().toString());
        String description = ItemDescription.getText().toString();

        //Check if there is item with same name for user
        Item result = realm.where(Item.class)
                .equalTo("user_uuid",uuid)
                .equalTo("name",name)
                .findFirst();

        if (result == null){
            Item newItem = new Item();
            newItem.setUuid(UUID.randomUUID().toString());
            newItem.setUser_uuid(uuid);
            newItem.setName(name);
            newItem.setQuantity(quantity);
            newItem.setDescription(description);

            try{
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(newItem);
                realm.commitTransaction();
                finish();
                ItemInventory_.intent(this).start();
            }
            catch(Exception e){
                Toast t = Toast.makeText(this,"Error saving", Toast.LENGTH_SHORT);
                t.show();
            }
        }
        else{
            Toast t = Toast.makeText(this,"Item name already taken", Toast.LENGTH_SHORT);
            t.show();
        }

    }

    //Click Listener for Cancel Button
    @Click(R.id.cancel_item_button)
    public void cancelItem(){
        finish();
    }

    //On Destroy
    public void onDestroy(){
        super.onDestroy();
        realm.close();
    }
}