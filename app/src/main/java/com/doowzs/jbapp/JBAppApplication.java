package com.doowzs.jbapp;

import android.app.Application;

public class JBAppApplication extends Application {
    private final String agentName = "JB App (Android)";
    private final String tokenKey =  "com.doowzs.jbapp.token";
    private final String idKey =  "com.doowzs.jbapp.user_id";
    private final String nameKey =  "com.doowzs.jbapp.user_name";
    private final String assignmentsKey =  "com.doowzs.jbapp.assignments";
    private final String rootURL = "https://njujb.com/api/";

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
}
