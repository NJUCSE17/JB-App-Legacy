package com.doowzs.jbapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    // Application and Shared Preferences
    private JBAppApplication mApp = null;
    private SharedPreferences mPrefs = null;

    // Drawer
    private DrawerLayout mDrawerLayout = null;

    // Constants
    private final int REQUEST_LOGIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check Login Status
        mApp = ((JBAppApplication) getApplication());
        mPrefs = this.getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        if (menuItem.getItemId() == R.id.nav_logout) {
                            mPrefs.edit().remove(mApp.getTokenKey())
                                    .remove(mApp.getIdKey())
                                    .remove(mApp.getNameKey())
                                    .apply();
                            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivityForResult(loginIntent, REQUEST_LOGIN);
                        }
                        return true;
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_LOGIN && resultCode == RESULT_CANCELED) {
                this.setResult(RESULT_CANCELED);
                finish();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
            this.setResult(RESULT_CANCELED);
            finish();
        }
    }
}
