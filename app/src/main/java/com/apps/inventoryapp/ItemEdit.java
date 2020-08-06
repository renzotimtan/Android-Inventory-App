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

@EActivity(R.layout.activity_item_edit)
public class ItemEdit extends AppCompatActivity {

    @ViewById
    EditText EditItemName;
    @ViewById
    EditText EditQuantity;
    @ViewById
    EditText EditDescription;


    Realm realm;

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

        EditItemName.setText(item.getName());
        EditQuantity.setText(String.valueOf(item.getQuantity()));
        EditDescription.setText(item.getDescription());
    }
    //On Click of Save Button
    @Click(R.id.SaveEditButton)
    public void saveEdit(){
        //Init Shared Prefs
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String item_uuid = prefs.getString("item_uuid","");

        //Get Item Object
        Item item = realm.where(Item.class)
                .equalTo("uuid", item_uuid)
                .findFirst();

        //Get Texts
        String name = EditItemName.getText().toString();
        int quantity = Integer.parseInt(EditQuantity.getText().toString());
        String description = EditDescription.getText().toString();

        //Check for new item name
        Item check = realm.where(Item.class)
                .equalTo("user_uuid",item.getUser_uuid())
                .equalTo("name",name)
                .findFirst();

        if (item == null || name.equals(item.getName())){
            realm.beginTransaction();
            item.setName(name);
            item.setQuantity(quantity);
            item.setDescription(description);
            realm.commitTransaction();

            Toast t = Toast.makeText(this,"Item edits saved", Toast.LENGTH_SHORT);
            t.show();

            finish();
            ItemInventory_.intent(this).start();
        }else{
            Toast t = Toast.makeText(this,"Name already taken", Toast.LENGTH_SHORT);
            t.show();
        }
    }


    //On Click of Cancel Button
    @Click(R.id.CancelEditButton)
    public void cancelEdit(){
        finish();
    }
    public void onDestroy(){
        super.onDestroy();
        realm.close();
    }
}