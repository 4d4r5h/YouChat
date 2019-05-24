package com.anull.youchat;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutProject extends AppCompatActivity {

    private Toolbar aToolbar;
    private TextView aboutText;
    private Button visitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_project);

        aToolbar = (Toolbar) findViewById(R.id.about_project);
        setSupportActionBar(aToolbar);
        getSupportActionBar().setTitle("About Project");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();

        aboutText = (TextView) findViewById(R.id.about);

        String aboutTextString = "This project has been made on Android Studio using Java. For handling data, " +
                "I have used Google's Firebase Realtime Database. For handling push notifications, " +
                "I have used node.JS in addition to Firebase Cloud Messaging Service. \n \n" +
                "I have kept the source code for this project/app open. So anyone if willing to " +
                "make one, could take some help from it. This project is not free from bugs. The online feature doesn't work " +
                "properly. Function handing logout sometime crashes. But other than that, the app just works " +
                "fine. \n \n" + "Source code could be found on my Github account with repository named \"YouChat\".";

        aboutText.setText(aboutTextString);

        visitButton = (Button) findViewById(R.id.visit);

        visitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uriURL = Uri.parse("https://www.github.com/4d4r5h");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriURL);
                startActivity(launchBrowser);

            }
        });
    }
}
