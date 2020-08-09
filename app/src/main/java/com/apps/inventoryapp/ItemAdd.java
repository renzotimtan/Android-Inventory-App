package com.apps.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import io.realm.Realm;

@EActivity(R.layout.activity_item_add)
public class ItemAdd extends AppCompatActivity {

    Realm realm;

    @ViewById
    ImageView imageView;
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

    String image_uuid;

    @AfterViews
    public void checkPermissions()
    {

        // REQUEST PERMISSIONS
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                )

                .withListener(new BaseMultiplePermissionsListener()
                {
                    public void onPermissionsChecked(MultiplePermissionsReport report)
                    {
                        if (report.areAllPermissionsGranted())
                        {
                            // all permissions accepted proceed
                            init();
                        }
                        else
                        {
                            // notify about permissions
                            toastRequirePermissions();
                        }
                    }
                })
                .check();
    }

    public void toastRequirePermissions()
    {
        Toast.makeText(this, "You must provide permissions for app to run", Toast.LENGTH_LONG).show();
        finish();
    }

    public void init(){
        realm = Realm.getDefaultInstance();
        image_uuid = String.format("savedImage%s",UUID.randomUUID().toString());
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
            newItem.setImage(prefs.getString("image_string",""));
            newItem.setImage_uuid(prefs.getString("image_uuid",""));

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
        File getImageDir = getExternalCacheDir();
        File savedImage = new File(getImageDir, image_uuid);
        savedImage.delete();
        finish();
    }

    //On Destroy
    public void onDestroy(){
        super.onDestroy();
        realm.close();
    }

    public static int REQUEST_CODE_IMAGE_SCREEN = 0;

    @Click(R.id.add_image)
    public void addImage() {
        ImageActivity_.intent(this).startForResult(REQUEST_CODE_IMAGE_SCREEN);
    }

    public void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);

        if (requestCode==REQUEST_CODE_IMAGE_SCREEN)
        {
            if (responseCode==ImageActivity.RESULT_CODE_IMAGE_TAKEN)
            {
                // receive the raw JPEG data from ImageActivity
                // this can be saved to a file or save elsewhere like Realm or online
                byte[] jpeg = data.getByteArrayExtra("rawJpeg");

                try {
                    // save rawImage to file
                    File savedImage = saveFile(jpeg);

                    // load file to the image view via picasso
                    refreshImageView(savedImage);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    //uuid of this particular instance of activity


    private File saveFile(byte[] jpeg) throws IOException
    {
        // this is the root directory for the images
        File getImageDir = getExternalCacheDir();
        // just a sample, normally you have a diff image name each time
        File savedImage = new File(getImageDir, image_uuid);

        // Save to Shared Pref for later use
        String abspath = savedImage.getAbsolutePath();
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("image_string", abspath);
        editor.putString("image_uuid", image_uuid);
        editor.apply();

        FileOutputStream fos = new FileOutputStream(savedImage);
        fos.write(jpeg);
        fos.close();
        return savedImage;
    }

    private void refreshImageView(File savedImage) {

        // this will put the image saved to the file system to the imageview
        Picasso.get()
                .load(savedImage)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imageView);
    }
}