package com.doowzs.jbapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // Application Context
    JBAppApplication mApp = null;
    SharedPreferences mPrefs = null;

    // Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask mAuthTask = null;
    private RequestQueue mQueue = null;

    // Intent for reporting result.
    Intent mIndent = null;
    Context mContext = null;

    // UI references.
    private TextInputEditText mStudentIDView;
    private TextInputEditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mApp = ((JBAppApplication) getApplication());
        mPrefs = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(this);
        mIndent = getIntent();
        mContext = getBaseContext();

        // Set up the login form.
        mStudentIDView = (TextInputEditText) findViewById(R.id.student_id);
        mPasswordView = (TextInputEditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mStudentIDView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String studentID = mStudentIDView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid student ID.
        if (TextUtils.isEmpty(studentID)) {
            mStudentIDView.setError(getString(R.string.error_field_required));
            focusView = mStudentIDView;
            cancel = true;
        } else if (!isStudentIDValid(studentID)) {
            mStudentIDView.setError(getString(R.string.error_invalid_student_id));
            focusView = mStudentIDView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(studentID, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isStudentIDValid(String studentID) {
        Integer sid = Integer.valueOf(studentID);
        return sid >= 170000000 && sid <= 300000000;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mStudentID;
        private final String mPassword;

        UserLoginTask(String studentID, String password) {
            mStudentID = studentID;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Map<String, String> jParams = new HashMap<String, String>();
                jParams.put("student_id", mStudentID);
                jParams.put("password", mPassword);
                JSONObject jsonParams = new JSONObject(jParams);
                JsonObjectRequest loginRequest = new JsonObjectRequest(
                        Request.Method.POST, mApp.getLoginURL(), jsonParams,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject data) {
                                mAuthTask = null;
                                showProgress(false);
                                try {
                                    if (data.getString("status").equals("success")) {
                                        mPrefs.edit().putString(mApp.getTokenKey(), data.getString("token"))
                                                .putString(mApp.getIdKey(), data.getString("user_id"))
                                                .putString(mApp.getNameKey(), data.getString("user_name"))
                                                .apply();
                                        setResult(RESULT_OK, mIndent);
                                        finish();
                                    } else {
                                        mPasswordView.setText("");
                                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                                        mPasswordView.requestFocus();
                                    }
                                } catch (JSONException jex) {
                                    mAuthTask = null;
                                    showProgress(false);
                                    Toast.makeText(mContext, jex.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError vex) {
                        mAuthTask = null;
                        showProgress(false);
                        if (vex.getClass() == AuthFailureError.class) {
                            mPasswordView.setText("");
                            mPasswordView.setError(getString(R.string.error_incorrect_password));
                            mPasswordView.requestFocus();
                        } else {
                            Toast.makeText(mContext, vex.toString(), Toast.LENGTH_SHORT).show();
                        }
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
                mQueue.add(loginRequest);
                return true;
            } catch (Exception ex) {
                Toast.makeText(mContext, ex.toString(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            // Let Volley handle the result.
            if (!success) {
                mAuthTask = null;
                showProgress(false);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

