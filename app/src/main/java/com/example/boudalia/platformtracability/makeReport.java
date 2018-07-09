package com.example.boudalia.platformtracability;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class makeReport extends DialogFragment {

    private static final String TAG = "MAKE REPORT : ";

    Response.ErrorListener myErrorListener;

    private String url;

    private RequestQueue queue;

    boolean is_send_report = false;
    String id_report = "";

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        url = "http://" + getResources().getString(R.string.hostname) + ":" +
                getResources().getString(R.string.port) + "/node";

        queue = Volley.newRequestQueue(getActivity());

        myErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(TAG,"ERROR IN REQUEST" + error.toString());
                Log.d(TAG,"ERROR IN REQUEST" + error.getMessage());
            }
        };


        builder.setView(R.layout.report_incident)
                // Add action buttons
                .setPositiveButton(R.string.sendReport, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {



                        EditText reportEdit = getDialog().findViewById(R.id.reportText);
                        Spinner mySpinner = getDialog().findViewById(R.id.stateSpinner);
                        String selected = mySpinner.getSelectedItem().toString();

                        String report = reportEdit.getText().toString();


                        int state;
                        if(selected.matches("HS")) {
                            is_send_report = true;
                            state = 1;
                        } else if(selected.matches("Partiel")) {
                            is_send_report = true;
                            state = 2;
                        } else {
                            is_send_report = false;
                            state = 3;
                        }

                        try {
                            sendHistoReport(selected, report);
                            sendReport(state, report);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        is_send_report = false;
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void sendReport(int state, String report) throws JSONException {

        Bundle bd = getArguments();

        String pathRequest = url + "/api/appareils/etat";//?token=" + bd.getString("token"); //+ bd.getString("appID") +"?token=" + bd.getString("token");

        JSONObject data = new JSONObject();

        data.put("appId", bd.getString("appId"));
        data.put("state", state);
        data.put("report", report + "");

        new makeRequest(Request.Method.POST, pathRequest, bd.getString("token"), data,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(final JSONObject response) {

                        Log.d(TAG,"Response: " + response.toString());

                        try {
                            id_report = response.getString("_id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, myErrorListener, queue);
    }


    private void sendHistoReport(String state, String report) throws  JSONException {
        Bundle bd = getArguments();

        String pathRequest = url + "/api/incident";//?token=" + bd.getString("token"); //+ bd.getString("appID") +"?token=" + bd.getString("token");

        JSONObject data = new JSONObject();

        data.put("app_id", bd.getString("appId"));
        data.put("userID", bd.getString("userId"));
        data.put("state", state);
        data.put("report", report + "");

        final String mRequestBody = data.toString();

        Log.d(TAG, mRequestBody);

        new makeRequest(Request.Method.POST, pathRequest, bd.getString("token"), data,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(final JSONObject response) {

                        Log.d(TAG,"Response: " + response.toString());

                    }
                }, myErrorListener, queue);
    }
}
