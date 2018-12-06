package com.doowzs.jbapp;

import android.app.Application;

public class JBAppApplication extends Application {
    private final String agentName = "JB App (Android)";
    private final String tokenKey =  "com.doowzs.jbapp.token";
    private final String rootURL = "http://192.168.1.140:8000/api/";

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
     * Get the login API URL.
     * @return loginURL
     */
    public String getLoginURL() {
        return rootURL + "login";
    }
}
