package com.qboxus.musictok.ApiClasses;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.MainMenu.MainMenuActivity;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class ApiRequest {


    public static void callApi(final Activity context, final String url, JSONObject jsonObject,
                               final Callback callback) {


        final String[] urlsplit = url.split("/");
        Functions.printLog(Constants.tag, url);

        if (jsonObject != null)
            Functions.printLog(Constants.tag + urlsplit[urlsplit.length - 1], jsonObject.toString());


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {


                        final String[] urlsplit = url.split("/");
                        Functions.printLog(Constants.tag + urlsplit[urlsplit.length - 1], response.toString());


                        if (callback != null) {
                            callback.onResponce(response.toString());
                        }

                        if (response.optString("code", "").equalsIgnoreCase("501")) {
                            logout(context);
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                final String[] urlsplit = url.split("/");
                Functions.printLog(Constants.tag + urlsplit[urlsplit.length - 1], error.toString());


                if (callback != null)
                    callback.onResponce(error.toString());

            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Api-Key", Constants.API_KEY);
                headers.put("User-Id", Functions.getSharedPreference(context).getString(Variables.U_ID, null));
                headers.put("Auth-Token", Functions.getSharedPreference(context).getString(Variables.AUTH_TOKEN, null));
                headers.put("device", "android");
                headers.put("version", android.os.Build.VERSION.RELEASE);
                headers.put("ip", Functions.getSharedPreference(context).getString(Variables.DEVICE_IP, null));
                headers.put("device_token", Functions.getSharedPreference(context).getString(Variables.DEVICE_TOKEN, null));
                Functions.printLog(Constants.tag, headers.toString());
                return headers;

            }
        };
        try {

            if (context != null) {
                RequestQueue requestQueue = Volley.newRequestQueue(context);
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                requestQueue.getCache().clear();
                requestQueue.add(jsonObjReq);
            }


        } catch (Exception e) {
            Functions.printLog(Constants.tag, e.toString());
        }

    }


    public static void callApiGetRequest(final Activity context, final String url,
                                         final Callback callback) {


        Functions.printLog(Constants.tag, url);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        final String[] urlsplit = url.split("/");
                        Functions.printLog(Constants.tag + urlsplit[urlsplit.length - 1], response.toString());


                        if (callback != null) {
                            callback.onResponce(response.toString());
                        }

                        if (response.optString("code", "").equalsIgnoreCase("501")) {
                            logout(context);
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                final String[] urlsplit = url.split("/");
                Functions.printLog(Constants.tag + urlsplit[urlsplit.length - 1], error.toString());


                if (callback != null)
                    callback.onResponce(error.toString());

            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Api-Key", Constants.API_KEY);
                headers.put("User-Id", Functions.getSharedPreference(context).getString(Variables.U_ID, null));
                headers.put("Auth-Token", Functions.getSharedPreference(context).getString(Variables.AUTH_TOKEN, null));
                headers.put("device", "android");
                headers.put("version", android.os.Build.VERSION.RELEASE);
                headers.put("ip", Functions.getSharedPreference(context).getString(Variables.DEVICE_IP, null));
                headers.put("device_token", Functions.getSharedPreference(context).getString(Variables.DEVICE_TOKEN, null));
                Functions.printLog(Constants.tag, headers.toString());
                return headers;

            }
        };
        try {

            if (context != null) {
                RequestQueue requestQueue = Volley.newRequestQueue(context);
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                requestQueue.getCache().clear();
                requestQueue.add(jsonObjReq);
            }
        } catch (Exception e) {
            Functions.printLog(Constants.tag, e.toString());
        }
    }


    // logout to app automatically when the login token expire
    public static void logout(Activity activity) {

        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, gso);
        googleSignInClient.signOut();

        LoginManager.getInstance().logOut();

        Functions.removeMultipleAccount(activity);

        SharedPreferences.Editor editor = Functions.getSharedPreference(activity).edit();
        Paper.book(Variables.PrivacySetting).destroy();
        editor.clear();
        editor.commit();
        activity.finish();

        Functions.setUpExistingAccountLogin(activity);

        activity.startActivity(new Intent(activity, MainMenuActivity.class));

    }


}
