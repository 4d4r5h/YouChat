package com.anull.youchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ShowingImage extends AppCompatActivity {

    private ImageView downloadImage;
    private ImageView imageToShow;
    private ProgressDialog sProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showing_image);

        sProgress = new ProgressDialog(this);
        sProgress.setTitle("Loading Image");
        sProgress.setMessage("Please wait while the image is being loaded.");
        sProgress.setCanceledOnTouchOutside(false);
        sProgress.show();

        imageToShow = (ImageView) findViewById(R.id.image_to_show);
        final String imageURL = getIntent().getStringExtra("imageURL");

        downloadImage = (ImageView) findViewById(R.id.download_image);
        downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uriURL = Uri.parse(imageURL);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriURL);
                startActivity(launchBrowser);

            }
        });

        Picasso.get().setIndicatorsEnabled(false);
        Picasso.get().load(imageURL).networkPolicy(NetworkPolicy.OFFLINE).into(imageToShow, new Callback() {

            @Override
            public void onSuccess() {
                sProgress.dismiss();
            }

            @Override
            public void onError(Exception e) {

                Picasso.get().load(imageURL).into(imageToShow, new Callback() {
                    @Override
                    public void onSuccess() {
                        sProgress.dismiss();
                    }

                    @Override
                    public void onError(Exception e) {
                        sProgress.dismiss();
                        Toast.makeText(getApplicationContext(), "There was some error in loading image.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }
}
