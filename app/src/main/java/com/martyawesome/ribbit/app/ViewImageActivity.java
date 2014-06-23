package com.martyawesome.ribbit.app;

import android.app.Activity;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class ViewImageActivity extends Activity {
    public static final String TAG = ViewImageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Uri imageUri = getIntent().getData();
        Picasso.with(this).load(imageUri.toString()).into(imageView);

        try {
            ExifInterface exif = new ExifInterface(imageUri.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case 3:
                    imageView.setRotation(180);
                    break;
                case 6:
                    imageView.setRotation(90);
                    break;
                case 8:
                    imageView.setRotation(270);
                    break;
                default:
                    imageView.setRotation(270);
                    break;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        },10*1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

}
