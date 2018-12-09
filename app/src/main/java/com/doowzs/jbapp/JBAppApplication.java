package com.doowzs.jbapp;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
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
    private final String agentName = "JB App (Android)";
    private final String tokenKey =  "com.doowzs.jbapp.token";
    private final String idKey =  "com.doowzs.jbapp.user_id";
    private final String nameKey =  "com.doowzs.jbapp.user_name";
    private final String assignmentsKey =  "com.doowzs.jbapp.assignments";

    private final String rootURL = "https://njujb.com/api/";

    public final int versionCode = BuildConfig.VERSION_CODE;
    public final String versionName = BuildConfig.VERSION_NAME;

    /**
     * Get the agent name
     * @return agent name
     */
    public String getAgentName() {
        return agentName;
    }

    /**
     * Get the key to token in shared prefs.
     * @return key to token
     */
    public String getTokenKey() {
        return tokenKey;
    }

    /**
     * Get the key to user id in shared prefs.
     * @return key to ID
     */
    public String getIdKey() {
        return idKey;
    }

    /**
     * Get the key to user name in shared prefs.
     * @return key to username
     */
    public String getNameKey() {
        return nameKey;
    }

    /**
     * Get the key to assignments in shared prefs.
     * @return key to assignments
     */
    public String getAssignmentsKey() {
        return assignmentsKey;
    }

    /**
     * Get the update API URL.
     * @return updateURL
     */
    public String getUpdateURL() {
        return rootURL + "app";
    }

    /**
     * Get the login API URL.
     * @return loginURL
     */
    public String getLoginURL() {
        return rootURL + "login";
    }

    /**
     * Get the assignments API URL.
     * @return assignmentsURL
     */
    public String getAssignmentsURL() {
        return rootURL + "assignments";
    }


    /**
     * Represents an asynchronous task to check update of app.
     */
    public Request checkUpdateRequest(final AlertDialog.Builder builder) {
        return new JsonObjectRequest(
                Request.Method.POST, getUpdateURL(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject data) {
                        try {
                            final JSONObject version = data.getJSONObject("data");
                            if (version.getInt("number") > versionCode) {
                                builder.setIcon(R.drawable.ic_arrow_up)
                                        .setTitle(getString(R.string.update_title))
                                        .setMessage(getString(R.string.update_current_version) + versionName + "\n"
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
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-Agent", getAgentName());
                headers.put("Accept", "application/json");
                return headers;
            }
        };
    }
}
