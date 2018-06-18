package com.example.boudalia.platformtracability;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MAIN :";
    private String url;// = "https://192.168.42.35:3000";

    private String token;
    private String userID;

    private Map<String, String> myProject;


    private final int RequestPermissionID = 1001;


    private FusedLocationProviderClient mFusedLocationClient;

    private LocationCallback mLocationCallback;

    private String currentQR = "";

    private RequestQueue queue;


    private SurfaceView cameraPreview;
    private BarcodeDetector myBarcodeDetector;
    private CameraSource myCamera;

    private double longitude = -1;
    private double latitude = -1;

    private Button renduRequest;

    private Button empruntRequest;
    CheckBox myBox;

    ImageView myImg;

    ImageView appImg;


    List<String> list;
    Spinner mySpinner;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myImg = findViewById(R.id.imageDroit);

        myProject = new HashMap<>();
        myBox = findViewById(R.id.is_incident);//make project spinner visible


        url = "http://" + getResources().getString(R.string.hostname) + ":" +
                getResources().getString(R.string.port) + "/node";

        Intent intent = getIntent();

        list = new ArrayList<String>();
        list.add("");
        mySpinner = findViewById(R.id.spinner);

        userID = intent.getStringExtra("userID");

        appImg = findViewById(R.id.imageApp);


        token = intent.getStringExtra("token");


        queue = Volley.newRequestQueue(this);

        getProfil(userID, 0);

        Button clear = findViewById(R.id.clearButton);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQR = "";
                makeButtonInvisble();
                appImg.setImageResource(0);
            }
        });

        cameraPreview = findViewById(R.id.cameraPreview);

        empruntRequest = findViewById(R.id.empruntButton);

        empruntRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEmpruntRequest(currentQR, 1);
            }
        });

        renduRequest = findViewById(R.id.rendreButton);

        renduRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEmpruntRequest(currentQR, 2);
            }
        });

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        // ...
                        Log.d(TAG, location.toString());
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                }

            };

    }

    private void constructDetector() {
        myBarcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
    }

    private void constructCamera() {
        myCamera = new CameraSource.Builder(this, myBarcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .build();
    }

    private void initCamera() {

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                        try {
                            myCamera.start(cameraPreview.getHolder());
                        } catch(SecurityException e) {

                            e.printStackTrace();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void initDetector() {


        myBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();



                if (qrcodes.size() != 0 && !(currentQR.matches(qrcodes.valueAt(0).displayValue))) {


                    currentQR = qrcodes.valueAt(0).displayValue;
                    makeRequest(currentQR);
                    makeDispoRequest(currentQR);

                    Log.d(TAG, "QR CODE :" + currentQR);

                    final TextView displayQR = findViewById(R.id.textQRCODE);

                    displayQR.post(new Runnable() {
                        @Override
                        public void run() {

                            displayQR.setText(currentQR);
                        }
                    });


                }
            }
        });
    }

    private void demarreCameraEtDetecteur() {

        constructDetector();
        constructCamera();

        initCamera();
        initDetector();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(hasPermission()) {
            Log.d(TAG,"PAS DE PERMISSION");
            makePermissionRequest();
        } else {

            Log.d(TAG, "permission deja dispo");
            demarreCameraEtDetecteur();
            startLocationUpdates();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case RequestPermissionID: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    demarreCameraEtDetecteur();

                    try {
                        myCamera.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch(SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    private boolean hasPermission() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    private void makePermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, RequestPermissionID);
    }

    private void startLocationUpdates() {



            try {
                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(10000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback,
                        null /* Looper */);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        onPause();
        onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(myCamera != null) {

            myCamera.stop();
        }
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(myCamera != null) {

            myCamera.stop();
        }
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void refreshSpinner() {
        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(adapter);
    }

    private void makeButtonInvisble() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                empruntRequest.setVisibility(View.INVISIBLE);
                renduRequest.setVisibility(View.INVISIBLE);

                myBox.setVisibility(View.INVISIBLE);
                mySpinner.setVisibility(View.INVISIBLE);
                myImg.setImageResource(0);

            }
        });
    }

    private void makeEmpruntRequest(String id, int method) {

        String pathRequest = url + "/api/emprunt";

        JSONObject data = new JSONObject();
        try {
            data.put("latitude",latitude);
            data.put("longitude",longitude);
            data.put("id_appareil",id);
            data.put("id", userID);

            String key = mySpinner.getSelectedItem().toString();

            if(key != "") {
                String idProjet = myProject.get(key);
                data.put("projetID", idProjet);
            } else {

                data.put("projetID", "");
            }



            if(method == 2) {
                android.widget.CheckBox myBox = findViewById(R.id.is_incident);
                if(myBox.isChecked()) {
                    data.put("incident",true);//make post to incident
                } else {
                    data.put("incident",false);
                }
            }
        } catch(org.json.JSONException e) {
            e.printStackTrace();

        }

        final String mRequestBody = data.toString();


        JsonObjectRequest jsObjRequest = new JsonObjectRequest(method,pathRequest,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {

                        Log.d(TAG,"Response: " + response.toString());
                        currentQR = "";
                        makeButtonInvisble();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d(TAG,"ERROR IN EMPRUNT" + error.toString());
                    }
                })

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
                    Log.d(TAG, "ERROR IN GET BODY");
                    return null;
                }
            }

        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }

    private void makeDispoRequest(String id) {

        String pathRequest = url + "/api/emprunt/"+ id +"?token=" + token;


        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET,pathRequest,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(TAG,"Response dispo : " + response.toString());

                        try {

                            if(response.getBoolean("disponible")) {

                                empruntRequest.setVisibility(View.VISIBLE);
                                mySpinner.setVisibility(View.VISIBLE);
                            } else {

                                renduRequest.setVisibility(View.VISIBLE);
                                myBox.setChecked(false);
                                myBox.setVisibility(View.VISIBLE);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d(TAG,"ERROR IN REQUEST DISPO" + error.toString());
                    }
                })

        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }

    private void makeProjetRequest(JSONArray id) {
        String pathRequest;

        for(int i = 0 ; i < id.length(); i++) {

            try {

                pathRequest = url + "/api/projets/" + id.get(i) + "?token=" + token;
                JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, pathRequest, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(final JSONObject response) {

                                Log.d(TAG, "Projet Response: " + response.toString());

                                try {
                                    myProject.put(response.getString("nom"),response.getString("_id"));

                                    list.add(response.getString("nom"));
                                    refreshSpinner();

                                } catch(JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Log.d(TAG, "ERROR IN PROJET REQUEST " + error.toString());
                            }
                        })

                {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                };

                // Access the RequestQueue through your singleton class.
                queue.add(jsObjRequest);
            } catch(JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void getProfil(String id, final int para) {

        String pathRequest = url + "/api/users/" + id + "?token=" + token;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET,pathRequest,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.d(TAG,"Response: " + response.toString());

                        if(para == 0 ) {
                            try {
                                makeProjetRequest(response.getJSONArray("projets"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"ERROR IN REQUEST" + error.toString());

                        final TextView displayQR = findViewById(R.id.textQRCODE);

                        displayQR.post(new Runnable() {
                            @Override
                            public void run() {

                                displayQR.setText(getResources().getString(R.string.request_error));
                            }
                        });
                    }
                })

        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

        };
        queue.add(jsObjRequest);
    }

    private void makeGroupeRequest(String id) {
        String pathRequest = url + "/api/groupe/me/droit/"+ id +"?token=" + token;


        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET,pathRequest,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {

                        Log.d(TAG,"Groupe Response: " + response.toString());
                        try {


                                if (response.getBoolean("has_right")) {
                                    myImg.setImageResource(R.drawable.ic_check_green_24dp);
                                } else {
                                    myImg.setImageResource(R.drawable.ic_do_not_disturb_alt_black_24dp);
                                }
                        } catch(JSONException e) {
                            e.printStackTrace();
                        }



                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d(TAG,"ERROR IN GROUPE REQUEST " + error.toString());
                    }
                })

        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }

    private void makeRequest(final String id) {



        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable(){
            @Override
            public void run() {
                findViewById(R.id.loadImg).setVisibility(View.VISIBLE);
                Picasso.get().load(url + "/appareils_image/" + id).into(appImg, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        findViewById(R.id.loadImg).setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        String pathRequest = url + "/api/appareils/"+ id +"?token=" + token;

        makeButtonInvisble();


        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET,pathRequest,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.d(TAG,"Response: " + response.toString());

                        try {
                            JSONArray jArray = response.getJSONArray("groupes");
                            if(jArray.length() > 0) {
                                makeGroupeRequest(id);
                            } else {
                                myImg.setImageResource(R.drawable.ic_do_not_disturb_alt_black_24dp);
                            }
                        } catch (JSONException e) {
                            myImg.setImageResource(0);
                            e.printStackTrace();
                        }

                        final TextView displayQR = findViewById(R.id.textQRCODE);

                        displayQR.post(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    displayQR.setText(response.getString("nom").toString());
                                } catch (JSONException e) {
                                    displayQR.setText("PAS DE NOM");
                                }
                            }
                        });


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"ERROR IN REQUEST" + error.toString());

                        final TextView displayQR = findViewById(R.id.textQRCODE);

                        displayQR.post(new Runnable() {
                            @Override
                            public void run() {

                                displayQR.setText(getResources().getString(R.string.request_error));
                            }
                        });
                    }
                })

        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }


}
