package com.apps.inventoryapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.theartofdev.edmodo.cropper.CropImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_image)
public class ImageActivity extends AppCompatActivity {

    private String fileAuthority;

    public static int RESULT_CODE_IMAGE_TAKEN = 100;
    public static int MAX_WIDTH = 500;
    public static int MAX_HEIGHT = 500;
    @ViewById
    CropImageView cropImageView;


    @AfterViews
    public void init()
    {
        fileAuthority = getResources().getString(R.string.fileAuthority);
    }

    @Click(R.id.capture)
    public void loadImage()
    {
        startActivityForResult(getPickImageChooserIntent(), 200);
    }

    @Click(R.id.rotate)
    public void rotateImage()
    {
        cropImageView.rotateImage(90);
    }


    @Click(R.id.cancel)
    public void cancel()
    {
        finish();
    }

    @Click(R.id.crop)
    public void cropImage()
    {
        // extract to size
        Bitmap cropped =  cropImageView.getCroppedImage(MAX_WIDTH, MAX_HEIGHT);

        if (cropped != null)
        {

            System.out.println(cropped.getWidth()+" x "+cropped.getHeight());
            cropImageView.setImageBitmap(cropped);


            // encode image to JPEG format
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            cropped.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();


            // send this path back
            Intent i = new Intent();
            i.putExtra("rawJpeg", byteArray);

            setResult(RESULT_CODE_IMAGE_TAKEN, i);
            finish();
        }
    }



    // PHONE CAMERA APP will return here once done
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==200) {
            if (resultCode == Activity.RESULT_OK) {

                Uri imageUri = getPickImageResultUri(data);
                cropImageView.setImageUriAsync(imageUri);

                System.out.println("URI: " + imageUri.getPath());

                // update cropImageView
                cropImageView.setImageUriAsync(imageUri);
            }
        }
    }


    //==========================================================================================


    // from sample code
    // https://theartofdev.com/2015/02/15/android-cropping-image-from-camera-or-gallery/


    // NEEDED FOR ANDROID 9
    public Uri createUriFromFile(File file, boolean useFileProvider)
    {
        // may require API check for SDKs prior to N?

        if (!useFileProvider) {
            return Uri.fromFile(file);
        }
        else {
            return FileProvider.getUriForFile(this,
                    fileAuthority,
                    file);
        }
    }


    /**
     * Create a chooser intent to select the  source to get image from.<br/>
     * The source can be camera's  (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the  intent chooser.
     */
    public Intent getPickImageChooserIntent() {

// Determine Uri of camera image to  save.
        Uri outputFileUri =  getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager =  getPackageManager();

// collect all camera intents
        Intent captureIntent = new  Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam =  packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new  Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

// collect all gallery intents
        Intent galleryIntent = new  Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery =  packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new  Intent(galleryIntent);
            intent.setComponent(new  ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

// the main intent is the last in the  list (fucking android) so pickup the useless one
        Intent mainIntent =  allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if  (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity"))  {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

// Create a chooser from the main  intent
        Intent chooserIntent =  Intent.createChooser(mainIntent, "Select source");

// Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,  allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    /**
     * Get URI to image received from capture  by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImageDir = getExternalCacheDir();
        if (getImageDir != null) {
            outputFileUri = createUriFromFile(new  File(getImageDir.getPath(), "pickImageResult.jpeg"), true);
        }
        return outputFileUri;
    }

    /**
     * Get the URI of the selected image from  {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera  and gallery image.
     *
     * @param data the returned data of the  activity result
     */
    public Uri getPickImageResultUri(Intent  data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null  && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ?  getCaptureImageOutputUri() : data.getData();
    }


}
