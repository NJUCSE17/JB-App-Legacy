package com.doowzs.jbapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Application and Shared Preferences
    private JBAppApplication mApp = null;
    private SharedPreferences mPrefs = null;
    private Context mContext = null;

    // Layout Components
    private DrawerLayout mDrawerLayout = null;
    private CoordinatorLayout mCoordinatorLayout = null;
    private LinearLayout mLinearLayout = null;

    // Volley Request Queue
    private RequestQueue mQueue = null;
    private GetAssignmentsTask mGetAssignmentsTask = null;

    // Constants
    private final int REQUEST_LOGIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check Login Status
        mApp = ((JBAppApplication) getApplication());
        mPrefs = this.getSharedPreferences(getPackageName(), MODE_PRIVATE);
        mContext = this.getBaseContext();
        mQueue = Volley.newRequestQueue(mContext);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mCoordinatorLayout = findViewById(R.id.main_coordinator_layout);
        mLinearLayout = findViewById(R.id.assignment_layout);

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

        mGetAssignmentsTask = new GetAssignmentsTask();
        mGetAssignmentsTask.execute();
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


    /**
     * Represents an asynchronous task to fetch assignmens.
     */
    public class GetAssignmentsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                JsonObjectRequest getAssignmentsRequest = new JsonObjectRequest(
                        Request.Method.POST, mApp.getAssignmentsURL(), null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject data) {
                                try {
                                    JSONArray assignmentArray = data.getJSONArray("data");
                                    for (int i = 0; i < assignmentArray.length(); ++i) {
                                        JSONObject assignmentObject = assignmentArray.getJSONObject(i);
                                        CardView cardView = new CardView(mContext);
                                        RelativeLayout relativeLayout = new RelativeLayout(mContext);
                                        LinearLayout linearLayout = new LinearLayout(mContext);
                                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                                        linearLayout.setPadding(15, 10, 15, 10);
                                        WebView webView = new WebView(mContext);
                                        webView.loadData(assignmentObject.getString("content"), "text/html; charset=UTF-8", null);
                                        TextView textView = new TextView(mContext);
                                        textView.setText(assignmentObject.getString("due_time"));
                                        textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

                                        linearLayout.addView(webView);
                                        linearLayout.addView(textView);
                                        relativeLayout.addView(linearLayout);
                                        cardView.addView(relativeLayout);
                                        mLinearLayout.addView(cardView);
                                    }
                                } catch (JSONException jex) {
                                    Snackbar.make(mCoordinatorLayout, jex.toString(), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError vex) {
                        Snackbar.make(mCoordinatorLayout, vex.toString(), Snackbar.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("User-Agent", mApp.getAgentName());
                        headers.put("Accept", "application/json");
                        headers.put("Authorization", "Bearer " + mPrefs.getString(mApp.getTokenKey(), null));
                        return headers;
                    }
                };
                mQueue.add(getAssignmentsRequest);
                return true;
            } catch (Exception ex) {
                Snackbar.make(mCoordinatorLayout, ex.toString(), Snackbar.LENGTH_LONG).show();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //
        }

        @Override
        protected void onCancelled() {
            //
        }
    }
}
