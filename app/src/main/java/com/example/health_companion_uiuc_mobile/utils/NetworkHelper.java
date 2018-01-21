/**
 * Source: David Gassner, Android App Development: RESTful Web Services
 */

package com.example.health_companion_uiuc_mobile.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * helper class handling network-related stuff
 */
public class NetworkHelper {

    /**
     * check whether we have the network access or not
     * @param context the activity context
     */
    public static boolean checkNetworkAccess(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
