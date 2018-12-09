package com.doowzs.jbapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class AboutActivity extends AppCompatActivity {
    // Application Helper
    private JBAppApplication mApp = null;

    // Layout Component
    private AlertDialog.Builder mBuilder = null;

    // Volley Request Queue
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mApp = (JBAppApplication) getApplication();
        mBuilder = new AlertDialog.Builder(AboutActivity.this);
        mQueue = Volley.newRequestQueue(AboutActivity.this);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception ex) {
            Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        TextView versionTextView = findViewById(R.id.versionTextView);
        versionTextView.setText(mApp.versionName);

        Button clButton = findViewById(R.id.changeLogButton);
        clButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
                builder.setTitle(getString(R.string.btn_changelog))
                        .setMessage(getString(R.string.string_changelog))
                        .show();
            }
        });

        Button scButton = findViewById(R.id.RTFSCButton);
        scButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rtfsc = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_repo)));
                startActivity(rtfsc);
            }
        });

        Button upButton = findViewById(R.id.updateButton);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQueue.add(mApp.checkUpdateRequest(mBuilder));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
