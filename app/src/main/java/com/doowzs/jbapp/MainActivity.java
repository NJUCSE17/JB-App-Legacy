package com.doowzs.jbapp;
import com.doowzs.jbapp.utils.JSONSharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
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
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Application and Shared Preferences
    private JBAppApplication mApp = null;
    private SharedPreferences mPrefs = null;
    private Context mContext = null;

    // Layout Components
    private DisplayMetrics mDisplayMetrics = null;
    private DrawerLayout mDrawerLayout = null;
    private CoordinatorLayout mCoordinatorLayout = null;
    private LinearLayout mLinearLayout = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;

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

        mDisplayMetrics = mContext.getResources().getDisplayMetrics();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mCoordinatorLayout = findViewById(R.id.main_coordinator_layout);
        mLinearLayout = findViewById(R.id.assignment_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        if (menuItem.getItemId() == R.id.nav_about) {
                            Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                            startActivity(aboutIntent);
                        } else if (menuItem.getItemId() == R.id.nav_logout) {
                            performLogout();
                        }
                        return true;
                    }
                });

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mGetAssignmentsTask = new GetAssignmentsTask();
                        mGetAssignmentsTask.execute();
                    }
                }
        );

        try {
            JSONArray assignmentsArray = JSONSharedPreferences.loadJSONArray(mContext, getPackageName(), mApp.getAssignmentsKey());
            loadAssignmentsToLayout(assignmentsArray);
        } catch (JSONException jex) {
            Toast.makeText(this, jex.toString(), Toast.LENGTH_LONG).show();
        }

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
     * Logout from app.
     */
    public void performLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.confirm_logout_title))
                .setMessage(getString(R.string.confirm_logout_content)
                        + mPrefs.getString(mApp.getIdKey(), "404") + " &#8212; "
                        + mPrefs.getString(mApp.getNameKey(), "Anonymous"))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mPrefs.edit().remove(mApp.getTokenKey())
                                .remove(mApp.getIdKey())
                                .remove(mApp.getNameKey())
                                .apply();
                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(loginIntent, REQUEST_LOGIN);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(getDrawable(R.drawable.ic_exclamation_circle))
                .show();
    }

    /**
     * Load assignments from an array to linear layout.
     * @param assignmentArray a JSON array of assignments
     */
    public void loadAssignmentsToLayout(JSONArray assignmentArray) {
        int dp5 = dp2pixelInt(5), dp8 = dp2pixelInt(8);
        mLinearLayout.removeAllViews();
        if(assignmentArray.length() == 0) {
            mLinearLayout.setGravity(Gravity.CENTER_VERTICAL);
            TextView textView = new TextView(mContext);
            textView.setTextColor(getColor(R.color.colorBlack));
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setText(getString(R.string.info_no_assignment));
            mLinearLayout.addView(textView);
        } else {
            mLinearLayout.setGravity(Gravity.NO_GRAVITY);
            try {
                for (int i = 0; i < assignmentArray.length(); ++i) {
                    JSONObject assignmentObject = assignmentArray.getJSONObject(i);
                    CardView cardView = new CardView(mContext);
                    cardView.setClickable(true);
                    cardView.setUseCompatPadding(true);
                    cardView.setRadius(dp8);
                    cardView.setContentPadding(dp8, dp8, dp8, dp8);
                    cardView.setCardElevation(dp5);

                    RelativeLayout relativeLayout = new RelativeLayout(mContext);
                    LinearLayout linearLayout = new LinearLayout(mContext);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setPadding(15, 10, 15, 10);

                    TextView textView = new TextView(mContext);
                    textView.setText(assignmentObject.getString("name"));
                    textView.setTextSize(16);
                    textView.setTextColor(getColor(R.color.colorBlack));

                    WebView webView = new WebView(mContext);
                    webView.loadData(assignmentObject.getString("content"), "text/html; charset=UTF-8", null);

                    TextView textView2 = new TextView(mContext);
                    textView2.setText(assignmentObject.getString("due_time"));
                    textView2.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

                    linearLayout.addView(textView);
                    linearLayout.addView(webView);
                    linearLayout.addView(textView2);
                    relativeLayout.addView(linearLayout);
                    cardView.addView(relativeLayout);
                    mLinearLayout.addView(cardView);
                }
                TextView textView = new TextView(mContext);
                String assignmentCountStr = getString(R.string.assignment_count_left) + " "
                        + assignmentArray.length() + " " + getString(R.string.assignment_count_right);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setText(assignmentCountStr);
                textView.setPadding(dp8, dp8 * 2, dp8, dp8 * 2);
                mLinearLayout.addView(textView);
            } catch (JSONException jex) {
                Toast.makeText(this, jex.toString(), Toast.LENGTH_LONG).show();
            }
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
                                    JSONSharedPreferences.remove(mContext, getPackageName(), mApp.getAssignmentsKey());
                                    JSONSharedPreferences.saveJSONArray(mContext, getPackageName(), mApp.getAssignmentsKey(), assignmentArray);
                                    loadAssignmentsToLayout(assignmentArray);
                                    if (mSwipeRefreshLayout.isRefreshing()) {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                    Snackbar.make(mCoordinatorLayout, getString(R.string.info_updated), Snackbar.LENGTH_SHORT).show();
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

    /**
     * Convert dp to pixel
     * @param dp int
     * @return the pixel value
     */
    public float dp2pixel(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mDisplayMetrics);
    }

    /**
     * Convert dp to pixel (integer)
     * @param dp int
     * @return the pixel value (integer)
     */
    public int dp2pixelInt(int dp) {
        return Math.round(dp2pixel(dp));
    }
}
