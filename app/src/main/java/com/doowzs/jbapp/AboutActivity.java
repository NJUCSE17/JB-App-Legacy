package com.doowzs.jbapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AboutActivity extends AppCompatActivity {
    // Application Helper
    private JBAppApplication mApp = null;

    // Layout Component
    private CoordinatorLayout mCoordinatorLayout = null;

    // Volley Request Queue
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mApp = new JBAppApplication();
        mCoordinatorLayout = findViewById(R.id.about_coordinator_layout);
        mQueue = Volley.newRequestQueue(AboutActivity.this);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception ex) {
            Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        TextView versionTextView = findViewById(R.id.versionTextView);
        versionTextView.setText(BuildConfig.VERSION_NAME);

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
                new CheckUpdateTask().execute();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


    /**
     * Represents an asynchronous task to check update of app.
     */
    public class CheckUpdateTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                JsonObjectRequest getUpdateRequest = new JsonObjectRequest(
                        Request.Method.POST, mApp.getUpdateURL(), null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject data) {
                                try {
                                    final JSONObject version = data.getJSONObject("data");
                                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                    if (version.getInt("number") > pInfo.versionCode) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
                                        builder.setIcon(R.drawable.ic_arrow_up)
                                                .setTitle(getString(R.string.update_title))
                                                .setMessage(getString(R.string.update_current_version) + pInfo.versionName + "\n"
                                                        + getString(R.string.update_latest_version) + version.getString("name") + "\n\n"
                                                        + getString(R.string.update_contents) + version.getString("info") + "\n\n"
                                                        + getString(R.string.update_confirm))
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        try {
                                                            Intent updateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(version.getString("link")));
                                                            startActivity(updateIntent);
                                                        } catch (JSONException jex) {
                                                            Toast.makeText(AboutActivity.this, jex.toString(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //
                                                    }
                                                })
                                                .show();
                                    }
                                } catch (Exception ex) {
                                    Snackbar.make(mCoordinatorLayout, ex.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError vex) {
                        Snackbar.make(mCoordinatorLayout, vex.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("User-Agent", mApp.getAgentName());
                        headers.put("Accept", "application/json");
                        return headers;
                    }
                };
                mQueue.add(getUpdateRequest);
                return true;
            } catch (Exception ex) {
                Snackbar.make(mCoordinatorLayout, ex.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                return false;
            }
        }
    }
}
