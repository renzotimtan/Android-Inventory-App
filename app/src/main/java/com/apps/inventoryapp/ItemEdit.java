package com.apps.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.Realm;

@EActivity(R.layout.activity_item_edit)
public class ItemEdit extends AppCompatActivity {

    @ViewById
    ImageView imageView;
    @ViewById
    EditText EditItemName;
    @ViewById
    EditText EditQuantity;
    @ViewById
    EditText EditDescription;


    Realm realm;

    byte[] init_jpeg;

    @AfterViews
    public void init() {
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

        // Checks if Item is null
        // This block is for loading the image
        if(item.getImage() != null) {
            //Get File from item's realm path string
            File savedImage = new File(item.getImage());
            //Load Picasso from Realm's image
            Picasso.get()
                    .load(savedImage)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(imageView);
            //Copies jpeg byte[] when user cancels
            try {
                init_jpeg = readFileToByteArray(savedImage);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
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

        if (check == null || name.equals(item.getName())){
            realm.beginTransaction();
            item.setName(name);
            item.setQuantity(quantity);
            item.setDescription(description);
            realm.commitTransaction();

            Toast t = Toast.makeText(this,"Item edits saved", Toast.LENGTH_SHORT);
            t.show();

            finish();
        }else{
            Toast t = Toast.makeText(this,"Name already taken", Toast.LENGTH_SHORT);
            t.show();
        }
    }


    //On Click of Cancel Button
    @Click(R.id.CancelEditButton)
    public void cancelEdit(){
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String item_uuid = prefs.getString("item_uuid","");

        //Get Item Object
        Item item = realm.where(Item.class)
                .equalTo("uuid", item_uuid)
                .findFirst();

        //Checks if image is null
        if(item.getImage()!=null) {
            //Get File from item's realm path string
            File savedImage = new File(item.getImage());
            //This Returns image to its original unchanged value
            try {
                FileOutputStream fos = new FileOutputStream(savedImage);
                fos.write(init_jpeg);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        finish();
    }

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

    private File saveFile(byte[] jpeg) throws IOException
    {
        if ((realm == null) || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // this is the root directory for the images
        File getImageDir = getExternalCacheDir();

        String item_uuid = prefs.getString("item_uuid","");

        //Get Item Object
        Item item = realm.where(Item.class)
                .equalTo("uuid", item_uuid)
                .findFirst();
        //Instead of saving a new one, it rewrites the old directory
        File savedImage = new File(getImageDir, item.getImage_uuid());

        // Save to Shared Pref for later use
        String abspath = savedImage.getAbsolutePath();
        editor.putString("image_string", abspath);
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

    //Converts File to Byte Array (For Saving Pictures)
    private byte[] readFileToByteArray(File savedImage) throws IOException {
        byte[] bArray = new byte[(int) savedImage.length()];
        FileInputStream fis = new FileInputStream(savedImage);
        fis.read(bArray); //read file into bytes[]
        fis.close();
        return bArray;
    }
}