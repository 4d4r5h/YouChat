package com.anull.youchat;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mainAuth;
    private ViewPager tabPager;
    private Toolbar mToolbar;
    private SectionsPageAdapter mSectionsPageAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainAuth = FirebaseAuth.getInstance();

        if(mainAuth.getCurrentUser() != null)
        {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mainAuth.getCurrentUser().getUid());
        }
        tabPager = findViewById(R.id.tabPager);

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        tabPager.setAdapter(mSectionsPageAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(tabPager);

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("YouChat");

        tabPager.setCurrentItem(1);

    }

    public void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mainAuth.getCurrentUser();

        if(currentUser == null)
        {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
            finish();
        }
        else
        {
            mUserRef.child("online").setValue("true");
        }
    }

    public void onStop()
    {
        super.onStop();
        FirebaseUser currentUser = mainAuth.getCurrentUser();

        if(currentUser != null)
        {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int i = item.getItemId();

        switch(i)
        {
            case R.id.main_logout_btn:

                mainAuth.signOut();

                Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_LONG).show();
                Intent logIntent = new Intent(MainActivity.this, Register.class);
                startActivity(logIntent);
                finish();

                break;

            case R.id.main_profile_btn:

                Intent profIntent = new Intent(MainActivity.this, Profile.class);
                startActivity(profIntent);

                break;

            case R.id.main_project_btn:

                Intent projIntent = new Intent(MainActivity.this, AboutProject.class);
                startActivity(projIntent);

                break;

            case R.id.main_users_btn:

                Intent allIntent = new Intent(MainActivity.this, AllUsers.class);
                allIntent.putExtra("current_user", Objects.requireNonNull(mainAuth.getCurrentUser()).getUid());
                startActivity(allIntent);

                break;
        }

        return true;
    }

}
