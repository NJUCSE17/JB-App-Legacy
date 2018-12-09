package com.doowzs.jbapp;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;

public class JBAppApplication extends Application {

    // App version code and name
    public  final int versionCode = BuildConfig.VERSION_CODE;
    public  final String versionName = BuildConfig.VERSION_NAME;

    // Package name and keys to shared prefs
    private final String pkgName = "com.doowzs.jbapp";
    public  final String tokenKey       = pkgName + ".token";
    public  final String idKey          = pkgName + ".user_id";
    public  final String nameKey        = pkgName + ".user_name";
    public  final String assignmentsKey = pkgName + ".assignments";

    // URLs for API connections
    public  final String agentName = "JB App (Android)";
    public  final String repoURL = "https://github.com/doowzs/JB-App";
    private final String rootURL = "https://njujb.com/api";
    public  final String updateURL      = rootURL + "/app";
    public  final String loginURL       = rootURL + "/login";
    public  final String assignmentsURL = rootURL + "/assignments";
    public String assignmentStatusURL (int id, Boolean toSetFinished) {
        return rootURL + "/assignment/" + String.valueOf(id) + "/"
                + (toSetFinished ? "finish" : "reset");
    }

    // Shared Preference
    private SharedPreferences mPrefs = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = getSharedPreferences(pkgName, MODE_PRIVATE);
    }

    /**
     * Represents an asynchronous task to check update of app.
     */
    public Request checkUpdateRequest(final AlertDialog.Builder builder) {
        return new AppJsonObjectRequest(
                Request.Method.POST, updateURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject data) {
                        try {
                            final JSONObject version = data.getJSONObject("data");
                            if (version.getInt("number") > versionCode) {
                                builder.setIcon(R.drawable.ic_cloud_upload_alt)
                                        .setTitle(getString(R.string.update_title))
                                        .setMessage(getString(R.string.update_current_version) + versionName + "\n"
                                                + getString(R.string.update_latest_version) + version.getString("name") + "\n\n"
                                                + getString(R.string.update_contents) + version.getString("info") + "\n\n"
                                                + getString(R.string.update_confirm))
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(version.getString("link"))));
                                                } catch (JSONException jex) {
                                                    Log.e("UpdateIntent", jex.getLocalizedMessage());
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
                                Log.d("debug", "Request Finished.");
                            }
                        } catch (Exception ex) {
                            Log.e("UpdateSuccess", ex.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError vex) {
                Log.e("UpdateError", vex.getLocalizedMessage());
            }
        });
    }

    /**
     * Represent a JsonObjectRequest with automatic credential headers.
     */
    public class AppJsonObjectRequest extends JsonObjectRequest {
        public AppJsonObjectRequest(
                int method,
                String url,
                JSONObject jsonRequest,
                Response.Listener<JSONObject> listener,
                Response.ErrorListener errorListener) {
            super(
                    method,
                    url,
                    jsonRequest,
                    listener,
                    errorListener);
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("User-Agent", agentName);
            headers.put("Accept", "application/json");
            if (mPrefs.contains(tokenKey)) {
                headers.put("Authorization", "Bearer " + mPrefs.getString(tokenKey, null));
            }
            return headers;
        }
    }
}
