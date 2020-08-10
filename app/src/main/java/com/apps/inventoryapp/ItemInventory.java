package com.apps.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

@EActivity(R.layout.activity_item_inventory)
public class ItemInventory extends AppCompatActivity {

    Realm realm;

    @ViewById
    Button item_add_button;
    @ViewById
    Button item_clear_button;
    @ViewById
    RecyclerView item_recycler;

    ItemAdapter adapter;
    @AfterViews
    public void init()
    {
        //init realm
        realm = Realm.getDefaultInstance();

        //init SharedPrefs
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

        //LinearLayoutManager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        item_recycler.setLayoutManager(mLayoutManager);

        //get list of items
        RealmResults<Item> list = realm.where(Item.class)
                .equalTo("user_uuid",prefs.getString("uuid",""))
                .findAll()
                .sort("name", Sort.ASCENDING);

        //Set adapter to recyclerView
        adapter = new ItemAdapter(this, list, true);
        item_recycler.setAdapter(adapter);
    }

    @Click(R.id.item_add_button)
    public void addItem(){
        ItemAdd_.intent(this).start();
    }
    @Click(R.id.item_clear_button)
    public void clearItem(){
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        RealmResults<Item> itemResults = realm.where(Item.class)
                .equalTo("user_uuid",prefs.getString("uuid",""))
                .findAll();

        //Deleting all the images
        if (itemResults != null && itemResults.size() != 0){
            for (int i = 0; i < itemResults.size(); i++){
                Log.d("Image Exception","Working here");
                try {
                    Item item = itemResults.get(i);
                    Log.d("Image Exception",item.getImage());
                    File savedImage = new File(item.getImage());
                    savedImage.delete();
                }
                catch (Exception e){
                    Log.d("Image Exception","Didn't find this file");
                }
            }
        }

        //Deleting all from Realm
        realm.beginTransaction();
        itemResults.deleteAllFromRealm();
        realm.commitTransaction();
    }

    //Called from adapter
    public void add(View view){
        //Fail safe -- Checks if realm is closed
        if ((realm == null) || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }
        //Get Tag
        Item item = (Item) view.getTag();
        int current_quantity = item.getQuantity();
        realm.beginTransaction();
        item.setQuantity(current_quantity + 1);
        realm.commitTransaction();
    }

    //Called from adapter
    public void subtract(View view){

        //Fail safe -- Checks if realm is closed
        if ((realm == null) || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }

        //Get Tag
        Item item = (Item) view.getTag();
        int current_quantity = item.getQuantity();

        if (current_quantity != 0) {
            realm.beginTransaction();
            item.setQuantity(current_quantity - 1);
            realm.commitTransaction();
        }
    }
    //Called from adapter
    public void item_edit(View view){
        //Get Tag
        Item item = (Item) view.getTag();
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("item_uuid",item.getUuid());
        editor.apply();

        ItemDetails_.intent(this).start();
    }

    //Realm close on Destroy
    public void onDestroy(){
        super.onDestroy();
        realm.close();
    }
}