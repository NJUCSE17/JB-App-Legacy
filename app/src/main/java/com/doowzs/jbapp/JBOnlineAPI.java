package com.doowzs.jbapp;

import android.util.Log;

public class JBOnlineAPI {
    /**
     * Server API address
     */
    private final String RSAddr = "https://njujb.com/api/";
    private final String loginAddr = RSAddr + "login";
    private final String logoutAddr = RSAddr + "logout";
    private final String assignmentAddr = RSAddr + "assignments";

    public boolean hasToken() {
        // TODO: check Token
        return false;
    }

    public boolean login(final String studentID, final String password) throws Exception {
        Log.i("info", "SID: " + studentID);
        Log.i("info", "PSW: " + password);
        // TODO: login
        return false;
    }

    public boolean logout() {
        // TODO: logout
        return false;
    }

    private void saveToken(String token) {
        // TODO: save token
    }

    private String getToken() {
        // TODO: get token
        return "";
    }
}
