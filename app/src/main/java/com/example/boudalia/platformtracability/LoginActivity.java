package com.example.boudalia.platformtracability;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN : ";
    private String url;

    private RequestQueue queue;


    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = "http://" + getResources().getString(R.string.hostname) + ":" +
                getResources().getString(R.string.port) + "/node";

        queue = Volley.newRequestQueue(this);

        try {
            checkIfLogged();
        } catch (JSONException e) {
            setView();
            e.printStackTrace();
        }




    }

    private void setView() {
        setContentView(R.layout.activity_login);


        Log.d(TAG, "Login activity started");

        SSLSocketFactory myCert = new SSLSocketFactory();



        //queue = Volley.newRequestQueue(this, new HurlStack(null, myCert.getSocketFactory(this)));

        username = findViewById(R.id.loginInput);

        password = findViewById(R.id.passwordInput);

        Button connect = findViewById(R.id.loginButton);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Login en cours...");
                makeLoginRequest(username.getText().toString(), password.getText().toString());
            }
        });
    }

    private void makeLoginRequest(String username, String password) {

        String pathRequest = url +"/login";


        JSONObject data = new JSONObject();
        try {
            data.put("login",username);
            data.put("password",password);
        } catch(org.json.JSONException e) {
            e.printStackTrace();

        }
        new makeRequest(Request.Method.POST, pathRequest, "", data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {

                        Log.d(TAG,"Response: " + response.toString());
                        saveToken(response);
                        switchActivity(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d(TAG,"ERROR IN REQUEST" + error.getMessage());
                    }
                },
                queue);
    }

    private void checkIfLogged() throws JSONException {
        final String strObj = PreferenceManager.
                getDefaultSharedPreferences(this).getString("jsonToken","");

        Log.d(TAG, strObj);



        final JSONObject jsonObj = new JSONObject(strObj);
        String token = jsonObj.getString("tokenJSON");


        String pathRequest = url +"/api/test?token=" + token;

        new makeRequest(Request.Method.POST, pathRequest, token, jsonObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {

                        Log.d(TAG,"Response: " + response.toString());
                        switchActivity(jsonObj);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setView();
                        Log.d(TAG,"ERROR IN REQUEST" + error.getMessage());
                    }
                }, queue);
    }

    private void saveToken(JSONObject token) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                .putString("jsonToken",token.toString()).apply();
    }

    private void switchActivity(JSONObject token) {

        try {
            Intent intent = new Intent(this, MainActivity.class);// New activity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("token", token.get("tokenJSON").toString());
            intent.putExtra("userID", token.get("userID").toString());
            startActivity(intent);
            finish(); // Call once you redirect to another activity
        } catch (JSONException e) {
            Log.d(TAG, "Erreur dans la recuperation du token");
        }
    }

}
