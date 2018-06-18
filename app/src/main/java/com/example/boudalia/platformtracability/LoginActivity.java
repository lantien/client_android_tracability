package com.example.boudalia.platformtracability;

import android.content.Intent;
import android.os.Bundle;
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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN : ";
    private String url;

    private RequestQueue queue;


    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        url = "http://" + getResources().getString(R.string.hostname) + ":" +
                getResources().getString(R.string.port) + "/node";


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d(TAG, "Login activity started");

        SSLSocketFactory myCert = new SSLSocketFactory();

        queue = Volley.newRequestQueue(this);

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

        final String mRequestBody = data.toString();


        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST,pathRequest,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {

                        Log.d(TAG,"Response: " + response.toString());
                        switchActivity(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d(TAG,"ERROR IN REQUEST" + error.getMessage());
                    }
                })

        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Log.d(TAG, "ERROR IN GET BODY");
                    return null;
                }
            }

        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
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
