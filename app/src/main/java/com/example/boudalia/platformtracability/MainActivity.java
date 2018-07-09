package com.example.boudalia.platformtracability;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.android.volley.toolbox.JsonArrayRequest;
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

    private double longitude = 181;
    private double latitude = 181;

    private Button renduRequest;

    private Button retButton;
    private Button signaleButton;

    private Button empruntRequest;


    ImageView myImg;

    ImageView appImg;


    List<String> list;
    Spinner mySpinner;
    ArrayAdapter<String> adapter;

    private String appID;

    makeReport myRep;

    Response.ErrorListener myErrorListener;

    SurfaceHolder.Callback surfCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        myErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(TAG,"ERROR IN REQUEST" + error.getMessage());
            }
        };

        surfCallback = new SurfaceHolder.Callback() {
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
                Log.d(TAG, "surface changed !");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surface destroyed !");
            }
        };


        myImg = findViewById(R.id.imageDroit);

        myProject = new HashMap<>();


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


        retButton = findViewById(R.id.retButton);

        retButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                //cameraPreview.setAlpha(1);
                findViewById(R.id.layoutInfo).setVisibility(View.INVISIBLE);
                retButton.setVisibility(View.GONE);
                appImg.setVisibility(View.GONE);
                findViewById(R.id.loadImg).setVisibility(View.GONE);
                findViewById(R.id.textQRCODE).setVisibility(View.GONE);
                makeButtonInvisble();
                currentQR = "";
                //demarreCameraEtDetecteur();
            }
        });

        myRep = new makeReport();

        signaleButton = findViewById(R.id.signalerIncident);

        signaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle b = new Bundle();

                b.putString("appId", appID);
                b.putString("token", token);
                b.putString("userId", userID);

                myRep.setArguments(b);

                myRep.show(getFragmentManager(), TAG);
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

        cameraPreview.getHolder().addCallback(surfCallback);
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
    protected void onPause() {
        super.onPause();

        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();

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

                signaleButton.setVisibility(View.INVISIBLE);
                mySpinner.setVisibility(View.INVISIBLE);
                myImg.setImageResource(0);

            }
        });
    }

    private void makeEmpruntRequest(String id, int method) {

        String pathRequest = url + "/api/emprunt";

        JSONObject data = new JSONObject();
        try {
            data.put("latitude", latitude);
            data.put("longitude", longitude);
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


                if(myRep.is_send_report) {

                    data.put("incident",true);//make post to incident
                    data.put("incident_id", myRep.id_report);//make post to incident

                } else {

                    data.put("incident",false);
                }
            }
        } catch(org.json.JSONException e) {
            e.printStackTrace();

        }

        new makeRequest(method, pathRequest, token, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {

                        Log.d(TAG,"Response: " + response.toString());
                        currentQR = "";
                        makeButtonInvisble();

                    }
                }, myErrorListener, queue);
    }

    private void makeDispoRequest(String id) {

        String pathRequest = url + "/api/emprunt/"+ id;// +"?token=" + token;

        new makeRequest(Request.Method.GET, pathRequest, token, new JSONObject(),
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

                                signaleButton.setVisibility(View.VISIBLE);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, myErrorListener, queue);
    }

    private void makeMyGroupeRequest(String id) {
        String pathRequest = url + "/api/groupe/get/" + id + "?token=" + token;


            JsonArrayRequest jsObjRequest = new JsonArrayRequest(Request.Method.GET, pathRequest, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(final JSONArray response) {

                                Log.d(TAG, "groupe Response: " + response.toString());
                                for (int i = 0; i < response.length(); i++) {

                                    try {
                                        JSONObject row = response.getJSONObject(i);
                                        String nom = row.getString("nom");
                                        myProject.put(nom,row.getString("_id"));
                                        list.add(nom);
                                    } catch(JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                refreshSpinner();

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Log.d(TAG, "ERROR IN GROUPE REQUEST " + error.toString());
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

    private void getProfil(final String id, final int para) {

        String pathRequest = url + "/api/users/" + id;// + "?token=" + token;

        new makeRequest(Request.Method.GET, pathRequest, token, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.d(TAG,"Response: " + response.toString());

                        if(para == 0 ) {
                            makeMyGroupeRequest(id);
                        }
                    }
                }, myErrorListener, queue);
    }

    private void makeGroupeRequest(final String id) {
        String pathRequest = url + "/api/groupe/me/droit/"+ id;// +"?token=" + token;

        new makeRequest(Request.Method.GET, pathRequest, token, new JSONObject(),
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
                }, myErrorListener, queue);
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
                        appImg.setVisibility(View.VISIBLE);
                        findViewById(R.id.loadImg).setVisibility(View.GONE);
                        retButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        findViewById(R.id.loadImg).setVisibility(View.GONE);
                        retButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        String pathRequest = url + "/api/appareils/"+ id;// +"?token=" + token;

        makeButtonInvisble();

        new makeRequest(Request.Method.GET, pathRequest, token, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.d(TAG,"Response: " + response.toString());

                        appID = id;
                        //cameraPreview.setVisibility(View.INVISIBLE);
                        //cameraPreview.setAlpha(0);
                        findViewById(R.id.layoutInfo).setVisibility(View.VISIBLE);

                        try {
                            JSONArray jArray = response.getJSONArray("groupes");
                            if(jArray.length() > 0) {
                                makeGroupeRequest(id);
                            } else {
                                myImg.setImageResource(R.drawable.ic_do_not_disturb_alt_black_24dp);
                            }

                            String report = response.getString("report");

                            if(response.getBoolean("is_hs")) {

                                displayReport(report, 0);
                            } else if(response.getBoolean("is_part_hs")) {

                                displayReport(report, 1);
                            }

                        } catch (JSONException e) {

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
                                displayQR.setVisibility(View.VISIBLE);
                            }
                        });


                    }
                },
                myErrorListener, queue);
    }

    private void displayReport(String report, int state) {

        showReport show = new showReport();

        Bundle b = new Bundle();
        if(state != 0) {
            b.putString("message", "Attention : " + report);
        } else {
            b.putString("message", "Appareil hors service !");
        }

        show.setArguments(b);

        show.show(getFragmentManager(), TAG);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                logout();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void logout() {

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                .putString("jsonToken","").apply();
        Intent intent = new Intent(this, LoginActivity.class);// New activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activity
    }

}
