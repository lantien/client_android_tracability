package com.example.boudalia.platformtracability;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class makeRequest {

    public makeRequest(int method, String url, final String token,final JSONObject data, Response.Listener<JSONObject> respListen,
                        Response.ErrorListener errorListener, RequestQueue queue) {

        final String mRequestBody = data.toString();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(method, url,null,
                respListen,
                errorListener)

        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<>();
                params.put("x-access-token", token);

                return params;
            }

            @Override
            public byte[] getBody() {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }

        };

        queue.add(jsObjRequest);
    }
}
