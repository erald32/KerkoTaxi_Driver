package com.jeddigital.kerkotaxi_vehicle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.jeddigital.kerkotaxi_vehicle.R;

public class ShowClientOnMap extends FragmentActivity implements LocationListener {
    GoogleMap googleMap;
    ImageButton btnlogout;
    Location vehicle_location;
    Calendar c = Calendar.getInstance();

    MediaPlayer Notifikimi_kerkeses;
    Button Stadi_Makines_Free_Going;
    Button Stadi_Makines_Busy;
    TextView Gjenjda_Text;

    AlertDialog checkRequestAD;
    AlertDialog.Builder checkRequestADBuilder;

    Handler handler;

    public Boolean ndaloEkzekutimin_bookingstatus = false;
    final Runnable on_off_get_bookingstatus = new Runnable() {
        public void run() {
            if(!ndaloEkzekutimin_bookingstatus){
                check_booking_status();
                Log.d("qqq", "PO EKZEKUTOHET CHECK BOOKING STATUS");
                handler.postDelayed(this, 10000);
            }
        }
    };
    public Boolean ndaloEkzekutimin_requests = false;
    final Runnable on_off_get_request_list = new Runnable() {
        public void run() {
            if(!ndaloEkzekutimin_requests){
                get_request_list();
                Log.d("qqq", "PO EKZEKUTOHET GET REQUEST LIST");
                handler.postDelayed(this, 10000);
            }
        }
    };
    public Boolean ndaloEkzekutimin_if_request_valid = false;
    final Runnable on_off_get_if_request_still_valid = new Runnable() {
        public void run() {
            if(!ndaloEkzekutimin_if_request_valid){
                check_if_request_still_valid();
                Log.d("qqq", "PO EKZEKUTOHET REQUEST VALIDITY");
                handler.postDelayed(this, 30000);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.show_client_on_map);
        Log.d("qqq", "Entereedddddddddddddd oncreateeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
        handler = new Handler();

        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        googleMap = supportMapFragment.getMap();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        vehicle_location = locationManager.getLastKnownLocation(bestProvider);
        if (vehicle_location != null) {
            onLocationChanged(vehicle_location);
        }

        locationManager.requestLocationUpdates(bestProvider, 10000, 0,  this);

        checkRequestADBuilder = new AlertDialog.Builder(this ,R.style.AD_logout_style);              //   **********  Alert Dialog kur ka kerkesa te reja per taksistin ****************************
        checkRequestADBuilder.setCancelable(false);
        checkRequestADBuilder.setPositiveButton("Po", null)
                .setNegativeButton("Jo", null);

        checkRequestAD = checkRequestADBuilder.create();
        checkRequestAD.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button pranoj = checkRequestAD.getButton(AlertDialog.BUTTON_POSITIVE);
                pranoj.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        accept_requests();
                    }
                });

                Button refuzimi = checkRequestAD.getButton(AlertDialog.BUTTON_NEGATIVE);
                refuzimi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deny_requests();
                    }
                });
            }
        });

        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        btnlogout = (ImageButton) findViewById(R.id.button_logout);
        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                logout();
            }
        });

        Gjenjda_Text = (TextView) findViewById(R.id.stadiMakinesGjendja);

        Stadi_Makines_Free_Going = (Button) findViewById(R.id.stadiMakines);
        Stadi_Makines_Busy = (Button) findViewById(R.id.stadiMakinesbusy);
        Log.d("qqqoncreate_booking_id", Preferencat.getString(Config.BOOKING_ID_PREF, ""));
        Log.d("qqqoncreate_id_shoferit",Preferencat.getString(Config.ID_E_SHOFERIT_PREF,""));
        Log.d("qqqoncreate_id_makines", Preferencat.getString(Config.ID_E_MAKINES_PREF, ""));
        Log.d("qqq", "start checking booking status");
        handler.post(on_off_get_bookingstatus); // po spate internet ne momentin e create lloce  shikoje me prioritet shume e rendesishme (dytesore dhe stadet 3 dhe 5 e njejta gje )

        LatLng qendra_tiranes = new LatLng(41.327911, 19.818506);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom((qendra_tiranes), 14.0F));

        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        System.exit(0);
                        Log.d("qqq", "u be restarti 20 minutesshhhhhhhhhhhhhhhhhhhhhhhhhhhh");
                    }
                },
                1200000 // cdo 20 minuta
        );
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    public void onLocationChanged(Location location) {
        vehicle_location = location;

        double latitude_shoferit = vehicle_location.getLatitude();
        double longitude_shoferit = vehicle_location.getLongitude();
        Log.d("qqqlatishof", String.valueOf(latitude_shoferit));
        Log.d("qqqlongushof", String.valueOf(longitude_shoferit));

        LatLng latLng_Shoferit = new LatLng(latitude_shoferit, longitude_shoferit);

        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor set_Prefs = Preferencat.edit();
        set_Prefs.putString(Config.lATITTUDE_MAKINES_PREF, String.valueOf(latitude_shoferit));
        set_Prefs.putString(Config.lONGITUDE_MAKINES_PREF, String.valueOf(longitude_shoferit));
        set_Prefs.commit();

        send_vehicle_location();
        Log.d("qqq","u derguan koordinatat on  Locatrion Changed()");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void check_booking_status(){
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String booking_status = "bookingstatus";
        final String bookiiing_id =  Preferencat.getString(Config.BOOKING_ID_PREF, "vlera default");
        final String vehicle_id = Preferencat.getString(Config.ID_E_MAKINES_PREF, "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray getJSONArray;
                        try {
                            getJSONArray = new JSONArray(response);

                            String STATUS_ID =getJSONArray.getString(0);
                            String LAT = getJSONArray.getString(1);
                            String LONG = getJSONArray.getString(2);
                            Log.d("qqq_Id_statusit_booking", STATUS_ID);

                            if (STATUS_ID.equals("2")) {
                                Log.d("qqq", "booking status 22222" + response);
                                ndaloEkzekutimin_if_request_valid = false;                          // start check if still valid
                                handler.post(on_off_get_if_request_still_valid);
                                Log.d("qqq", "filloi check a e shte e vlefshme kerkesaaaa edhe pas crashittttttttttttt");
                                Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.going);
                                Gjenjda_Text.setText("PO MERR KLIENTIN");
                                Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View arg0) {
                                        arrived();
                                    }
                                });
                                double Latitude_e_klientit = Double.parseDouble(LAT);
                                double Longitude_e_klientit = Double.parseDouble(LONG);
                                LatLng pozicioni_klientit_mbas_crashit = new LatLng(Latitude_e_klientit, Longitude_e_klientit);
                                googleMap.addMarker(new MarkerOptions().position(pozicioni_klientit_mbas_crashit));
                                ndaloEkzekutimin_bookingstatus = true;                               // stop checking booking status
                                handler.post(on_off_get_bookingstatus);
                            }
                            else if (STATUS_ID.equals("3")) {
                                Log.d("qqq", "booking status 3333" + response);
                                ndaloEkzekutimin_if_request_valid = false;                          // start check if still valid
                                handler.post(on_off_get_if_request_still_valid);
                                Log.d("qqq", "filloi check a e shte e vlefshme kerkesaaaa edhe pas crashittttttttttttt");
                                Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.arrived);
                                Gjenjda_Text.setText("PO PRET KLIENTIN");
                                Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View arg0) {
                                        mora_klientin();
                                    }
                                });
                                /*double Latitude_e_klientit = Double.parseDouble(LAT);
                                double Longitude_e_klientit = Double.parseDouble(LONG);
                                LatLng pozicioni_klientit_mbas_crashit = new LatLng(Latitude_e_klientit, Longitude_e_klientit);
                                googleMap.addMarker(new MarkerOptions().position(pozicioni_klientit_mbas_crashit));*/
                                ndaloEkzekutimin_bookingstatus = true;                               // stop checking booking status
                                handler.post(on_off_get_bookingstatus);
                            }

                            else if (STATUS_ID.equals("4")) {
                                Log.d("qqq", "booking status 4" + response);

                                Stadi_Makines_Free_Going.setVisibility(View.INVISIBLE);
                                Stadi_Makines_Busy.setVisibility(View.VISIBLE);
                                Stadi_Makines_Busy.setOnClickListener(new View.OnClickListener() {                   ///////////////////////////////////////**//////////////////////////////
                                    @Override
                                    public void onClick(View arg0) {
                                        lashe_klientin();
                                    }
                                });
                                Gjenjda_Text.setText("I ZENE");
                                googleMap.clear();
                                ndaloEkzekutimin_bookingstatus = true;                               // stop checking booking status
                                handler.post(on_off_get_bookingstatus);
                            }
                            else {
                                Log.d("qqq", "Nuk ka booking 2 , 3 , 4 ose 13");
                                Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.free);
                                Gjenjda_Text.setText("I LIRE");
                                Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View arg0) {
                                       // shoferi_merr_vete_klientin();
                                    }
                                });
                                handler.post(on_off_get_request_list);                               // start checking for requests
                                ndaloEkzekutimin_bookingstatus = true;                               // stop checking booking status
                                handler.post(on_off_get_bookingstatus);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponse: " + error.networkResponse);
                if(error instanceof NoConnectionError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju nuk keni internet per momentin." , Toast.LENGTH_SHORT).show();
                }
                else if( error instanceof TimeoutError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureError: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerError: " + error.getMessage());
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkError: " + error.getMessage());
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseError: " + error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(Config.KEY_GET_BOOKING_STATUS, booking_status);
                params.put(Config.KEY_BOOKING_ID, bookiiing_id);
                params.put(Config.KEY_VEHICLE_ID, vehicle_id);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void send_vehicle_location(){
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        final String vehicles_location= "vehicleLocation";
        final String vehicle_id = Preferencat.getString(Config.ID_E_MAKINES_PREF, "");
        final String send_latitude  = String.valueOf(vehicle_location.getLatitude()).toString().trim();
        final String send_longitude = String.valueOf(vehicle_location.getLongitude()).toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.equalsIgnoreCase(String.valueOf(Config.RESPONSE_SUCCESS))) {
                            Log.d("qqq", "Po dergohen te dhenat e vendndodhjes se makines");
                        } else {
                            Log.d("qqq", " erroriiiiiiiiiiiiiiiiiiiiiii *********:" + response);
                            Toast.makeText(ShowClientOnMap.this, "ServerError: Send_Vehicle_Location()"+ response , Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponseOnLocationChanged: " + error.getMessage());
                if(error instanceof NoConnectionError) {
                    Log.d("qqq", "NoConnectionError: " + error.getMessage());
                }
                else if( error instanceof TimeoutError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureError: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerError: " + error.getMessage());
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkError: " + error.getMessage());
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseError: " + error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(Config.KEY_VEHICLE_LOCATION, vehicles_location);
                params.put(Config.KEY_VEHICLE_ID, vehicle_id);
                params.put(Config.KEY_LAT_MAKINES, send_latitude);
                params.put(Config.KEY_LNG_MAKINES, send_longitude);

                return params;
            }
        };
        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private void get_request_list(){

        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String get_request_list= "getrequestlist";
        final String vehicle_id_to_send = Preferencat.getString(Config.ID_E_MAKINES_PREF, "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray getJSONArray;
                        try {
                            getJSONArray = new JSONArray(response);

                            String Booking_ID =getJSONArray.getString(0);
                            String latitude_e_klientit = getJSONArray.getString(1);
                            String longitude_e_klientit = getJSONArray.getString(2);

                            SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor set_Prefs = Preferencat.edit();
                            set_Prefs.putString(Config.BOOKING_ID_PREF, Booking_ID);
                            set_Prefs.putString(Config.LATITUDE_KLIENTIT_PREF, latitude_e_klientit);
                            set_Prefs.putString(Config.lONGITUDE_KLIENTIT_PREF, longitude_e_klientit);
                            set_Prefs.commit();
                            Log.d("qqqwwwwwwww", Booking_ID);

                            if (Booking_ID.equals("0")) {
                                Log.d("qqq","Nuk ka kerkesa per momentin tek get request list");
                                                  /*  if(checkRequestAD.isShowing())
                                                        {                                 // Kjo ndodh qe anullohet porosia
                                                            checkRequestAD.dismiss();
                                                            Notifikimi_kerkeses.stop();
                                                            googleMap.clear();
                                                            Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.free);
                                                            Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View arg0) {
                                                                    shoferi_merr_vete_klientin();
                                                                }
                                                            });
                                                            Log.d("qqq","Porosia juaj u anullua nga qendra tek get request list");
                                                            new AlertDialog.Builder(ShowClientOnMap.this)
                                                                    .setTitle("Porosia juaj u anullua nga qendra")
                                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            dialog.dismiss();
                                                                        }
                                                                    }).show();
                                                            FileWriter fw = null;
                                                            try
                                                            {
                                                                fw = new FileWriter("/sdcard/speedtaxi_logs.txt",true);
                                                                BufferedWriter bw = new BufferedWriter(fw);
                                                                bw.write("Porosia juaj u anullua nga qendra tek get request list(),"+  c.getTime());
                                                                bw.close();
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                   */
                            }
                            else {
                                Log.d("qqq_BookID@GetRequest", Booking_ID);
                                ndaloEkzekutimin_requests = true;                                                    // ndalo ekzekutimin e requesteve
                                handler.post(on_off_get_request_list);
                                Log.d("qqq", "u ndalua kerkimi per requeste pasi u gjet kerkesa");

                                show_request();                                                            //  shfaq Alertdialog
                                Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View arg0) {
                                        arrived();
                                    }
                                });
                                Gjenjda_Text.setText("PO MERR KLIENTIN");
                                Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.going);
                                for (int i = 0; i < getJSONArray.length(); i++) {
                                    Log.d("qqq itelmlist json", getJSONArray.getString(i));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponse: " + error.getMessage());
                if(error instanceof NoConnectionError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju nuk keni internet per momentin dhe nuk mund te merrni kerkesa nga qendra", Toast.LENGTH_SHORT).show();
                    Log.d("qqq", "NoConnectionError: " + error.getMessage());
                }
                else if( error instanceof TimeoutError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureError: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerError: " + error.getMessage());
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkError: " + error.getMessage());
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseError: " + error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(Config.KEY_GET_REQUEST_LIST, get_request_list);
                params.put(Config.KEY_VEHICLE_ID, vehicle_id_to_send);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void show_request(){

        Notifikimi_kerkeses = MediaPlayer.create(this, R.raw.clock);
        Notifikimi_kerkeses.start();
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String Adresa = Preferencat.getString(Config.ADRESA_KLIENTIT_PREF, "");
        String Nr_telefonit_kientt = Preferencat.getString(Config.NR_CEL_KLIENTIT_PREF, "");
        String Emri_klientit =Preferencat.getString(Config.EMRI_KLIENTIT_PREF, "");
        String Notes =Preferencat.getString(Config.NOTES_E_KLIENTIT_PREF, "");

        String Mesazhi_ne_Alert = "KERKESE NGA QENDRA: " + System.getProperty("line.separator") + "Adresa:" + Adresa + System.getProperty("line.separator")+
                "Nr.Tel:" + Nr_telefonit_kientt + System.getProperty("line.separator")+
                "Emri Klientit:" + Emri_klientit + System.getProperty("line.separator")+  "Shenime:" + Notes;
        if(!checkRequestAD.isShowing()){
            checkRequestAD.setMessage(Mesazhi_ne_Alert);
            checkRequestAD.show();
        }
       /* double Latitude_e_klientit = Double.parseDouble(Preferencat.getString(Config.LATITUDE_KLIENTIT_PREF, ""));
        double Longitude_e_klientit = Double.parseDouble(Preferencat.getString(Config.lONGITUDE_KLIENTIT_PREF, ""));
        LatLng pozicioni_klientit = new LatLng(Latitude_e_klientit, Longitude_e_klientit);
        googleMap.addMarker(new MarkerOptions().position(pozicioni_klientit));   ktu ka qene per here te pare */
    }                                                                 // *********** nuk eshte servis
    private void accept_requests(){

        final SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String accept_request = "acceptrequest";
        final String booking_id_to_send =  Preferencat.getString(Config.BOOKING_ID_PREF, "vlera default");
        final String vehicle_id = Preferencat.getString(Config.ID_E_MAKINES_PREF, "");
        final String send_latitude  = Preferencat.getString(Config.lATITTUDE_MAKINES_PREF,"");
        final String send_longitude = Preferencat.getString(Config.lONGITUDE_MAKINES_PREF,"");

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase(String.valueOf(Config.RESPONSE_SUCCESS))){
                            Log.d("qqq", "u pranua kerkesa nga shoferi");
                            checkRequestAD.dismiss();
                            Notifikimi_kerkeses.stop();

                            double Latitude_e_klientit = Double.parseDouble(Preferencat.getString(Config.LATITUDE_KLIENTIT_PREF, ""));
                            double Longitude_e_klientit = Double.parseDouble(Preferencat.getString(Config.lONGITUDE_KLIENTIT_PREF, ""));
                            LatLng pozicioni_klientit = new LatLng(Latitude_e_klientit, Longitude_e_klientit);
                            googleMap.addMarker(new MarkerOptions().position(pozicioni_klientit));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom((pozicioni_klientit), 14.0F));

                            ndaloEkzekutimin_if_request_valid = false;                               // start check if still valid
                            handler.post(on_off_get_if_request_still_valid);
                        }

                        else if(response.equalsIgnoreCase((Config.BOOKING_NOT_VALID_SYSTEM_REFUSE))){

                                        if(checkRequestAD.isShowing()){
                                            checkRequestAD.dismiss();
                                            Notifikimi_kerkeses.stop();
                                            googleMap.clear();
                                            Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.free);
                                           /* Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View arg0) {
                                                  //  shoferi_merr_vete_klientin();
                                                }
                                            });*/
                                            new AlertDialog.Builder(ShowClientOnMap.this)
                                                    .setTitle("Porosia juaj u anullua nga sistemi per arsye vonese ne pergjigje")
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    }).show();

                                            FileWriter fw = null;
                                            try
                                            {
                                                fw = new FileWriter("/sdcard/speedtaxi_logs.txt",true);
                                                BufferedWriter bw = new BufferedWriter(fw);
                                                bw.write("Porosia juaj u anullua nga qendra on AcceptRequest() nga sistemi"+ c.getTime());
                                                bw.close();

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                            ndaloEkzekutimin_requests= false;
                            handler.post(on_off_get_request_list);                                   // start checking for requests
                        }

                        else if(response.equalsIgnoreCase((Config.BOOKING_NOT_VALID_CLIENT_REFUSE))){

                            if(checkRequestAD.isShowing()){
                                checkRequestAD.dismiss();
                                Notifikimi_kerkeses.stop();
                                googleMap.clear();
                                Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.free);
                                           /* Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View arg0) {
                                                  //  shoferi_merr_vete_klientin();
                                                }
                                            });*/
                                new AlertDialog.Builder(ShowClientOnMap.this)
                                        .setTitle("Porosia juaj u anullua nga klienti")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();

                                FileWriter fw = null;
                                try
                                {
                                    fw = new FileWriter("/sdcard/speedtaxi_logs.txt",true);
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    bw.write("Porosia juaj u anullua nga qendra on AcceptRequest() nga klienti"+ c.getTime());
                                    bw.close();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            ndaloEkzekutimin_requests= false;
                            handler.post(on_off_get_request_list);                                   // start checking for requests
                        }
                        else {
                            Log.d("qqqServeReqAccepRequest", response);
                            Log.d("qqqServeReqAccepRequest", "Error Database");
                            Toast.makeText(ShowClientOnMap.this, "ServerError: accept_requests()"+ response , Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponse: " + error.getMessage());
                if(error instanceof NoConnectionError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju nuk keni internet per momentin. Ju lutem provoni perseri pas 5 sekondash" , Toast.LENGTH_SHORT).show();
                }
                else if( error instanceof TimeoutError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureError: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerError: " + error.getMessage());
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkError: " + error.getMessage());
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseError: " + error.getMessage());
                }else if (error instanceof TimeoutError) {
                    Log.d("qqq", "TimeoutError: " + error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(Config.KEY_ACCEPT_REQUEST, accept_request);
                params.put(Config.KEY_BOOKING_ID, booking_id_to_send);
                params.put(Config.KEY_VEHICLE_ID, vehicle_id);
                params.put(Config.KEY_LAT_MAKINES, send_latitude);
                params.put(Config.KEY_LNG_MAKINES, send_longitude);

                Log.d("qqqBookingIDonAccept: ", booking_id_to_send);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void check_if_request_still_valid(){
        final SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String validity_of_request = "getrequeststatus";
        final String booking_iddddja =  Preferencat.getString(Config.BOOKING_ID_PREF, "vlera default");
        final String vehicle_iddddja = Preferencat.getString(Config.ID_E_MAKINES_PREF,"");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.equalsIgnoreCase(String.valueOf(Config.RESPONSE_ERROR))) {
                            Log.d("qqq", "Porosia juaj u anullua nga qendra still valid() "+response);

                            FileWriter fw = null;
                            try {
                                fw = new FileWriter("/sdcard/speedtaxi_logs.txt",true);
                                BufferedWriter bw = new BufferedWriter(fw);
                                bw.write("Porosia juaj u anullua nga qendra still valid(),"+ c.getTime());
                                bw.close();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ndaloEkzekutimin_if_request_valid= true;                                    // ndalojme ekzekutimin e if request is still valid
                            handler.post(on_off_get_if_request_still_valid);

                            ndaloEkzekutimin_requests = false;                                           // restart checkun e requesteve
                            handler.post(on_off_get_request_list);
                            googleMap.clear();

                            new AlertDialog.Builder(ShowClientOnMap.this)
                                    .setTitle("Porosia juaj u anullua.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.free);
                            Gjenjda_Text.setText("I LIRE");
                            Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                 //   shoferi_merr_vete_klientin();
                                }
                            });
                        }
                        else if (response.equalsIgnoreCase(String.valueOf(Config.RESPONSE_SUCCESS))) {
                            Log.d("qqq", "Porosia valid akoma  " + response);

                            double Latitude_e_klientit = Double.parseDouble(Preferencat.getString(Config.LATITUDE_KLIENTIT_PREF, ""));
                            double Longitude_e_klientit = Double.parseDouble(Preferencat.getString(Config.lONGITUDE_KLIENTIT_PREF, ""));
                            LatLng pozicioni_klientit = new LatLng(Latitude_e_klientit, Longitude_e_klientit);
                            double Latitude_e_makines = Double.parseDouble(Preferencat.getString(Config.lATITTUDE_MAKINES_PREF, ""));
                            double Longitude_e_makines =Double.parseDouble(Preferencat.getString(Config.lONGITUDE_MAKINES_PREF, ""));
                            LatLng pozicioni_makines = new LatLng(Latitude_e_makines, Longitude_e_makines);

                            LatLngBounds.Builder route_bounds_builder = new LatLngBounds.Builder();
                            route_bounds_builder.include(new LatLng(Latitude_e_klientit,Longitude_e_klientit));
                            route_bounds_builder.include(new LatLng(Latitude_e_makines,Longitude_e_makines));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(route_bounds_builder.build(), 150));

                            // Getting URL to the Google Directions API
                        /*  String url = getDirectionsUrl(pozicioni_makines, pozicioni_klientit);
                            DownloadTask downloadTask = new DownloadTask();
                            // Start downloading json data from Google Directions API
                            downloadTask.execute(url);
                        */
                        }
                        else  {
                            Log.d("qqq", "Response else stillvalid()" + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponse: " + error.getMessage());
                if(error instanceof NoConnectionError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju nuk keni internet per momentin dhe porosia juaj mund te jete anulluar", Toast.LENGTH_SHORT).show();
                    Log.d("qqq", "NoConnectionError: " + error.getMessage());
                }
                else if( error instanceof TimeoutError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureError: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerError: " + error.getMessage());
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkError: " + error.getMessage());
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseError: " + error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(Config.KEY_GET_STATUS_REQUEST, validity_of_request);
                params.put(Config.KEY_BOOKING_ID, booking_iddddja);
                params.put(Config.KEY_VEHICLE_ID, vehicle_iddddja);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void deny_requests(){
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        final String deny_request = "cancelrequest";
        final String booking_id_to_send =  Preferencat.getString(Config.BOOKING_ID_PREF, "vlera default");
        final String vehicle_id = Preferencat.getString(Config.ID_E_MAKINES_PREF,"");
        final String send_latitude  = Preferencat.getString(Config.lATITTUDE_MAKINES_PREF,"");
        final String send_longitude = Preferencat.getString(Config.lONGITUDE_MAKINES_PREF,"");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.equalsIgnoreCase(String.valueOf(Config.RESPONSE_SUCCESS))) {
                            Log.d("qqq", "u refuzua kerkesa nga shoferi");

                            Notifikimi_kerkeses.stop();
                            checkRequestAD.dismiss();
                            googleMap.clear();
                            Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.free);
                            Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                  //  shoferi_merr_vete_klientin();
                                }
                            });
                            Gjenjda_Text.setText("I LIRE");
                            ndaloEkzekutimin_requests= false;
                            handler.post(on_off_get_request_list);                                   // start checking for requests
                        }
                        else if (response.equalsIgnoreCase(String.valueOf(Config.BOOKING_NOT_VALID_SYSTEM_REFUSE))) {

                            checkRequestAD.dismiss();
                            Notifikimi_kerkeses.stop();
                            googleMap.clear();
                            Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.free);
                            Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                              //      shoferi_merr_vete_klientin();
                                }
                            });
                            new AlertDialog.Builder(ShowClientOnMap.this)
                                    .setTitle("Porosia juaj eshte anulluar nga sistemi per arsye vonese ne pergjigje")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();

                            ndaloEkzekutimin_requests= false;
                            handler.post(on_off_get_request_list);                                   // start checking for requests
                        }

                        else if (response.equalsIgnoreCase(String.valueOf(Config.BOOKING_NOT_VALID_CLIENT_REFUSE))) {

                            checkRequestAD.dismiss();
                            Notifikimi_kerkeses.stop();
                            googleMap.clear();
                            Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.free);
                            Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    //      shoferi_merr_vete_klientin();
                                }
                            });
                            new AlertDialog.Builder(ShowClientOnMap.this)
                                    .setTitle("Porosia juaj eshte anulluar nga klienti")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();

                            ndaloEkzekutimin_requests= false;
                            handler.post(on_off_get_request_list);                                   // start checking for requests
                        }

                        else {
                            Log.d("qqq", response);
                            Toast.makeText(ShowClientOnMap.this, "ServerError: deny_requests()"+ response , Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponse: " + error.getMessage());
                if(error instanceof NoConnectionError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju nuk keni internet per momentin. Ju lutem provoni perseri pas 5 sekondash" , Toast.LENGTH_SHORT).show();
                }
                else if( error instanceof TimeoutError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureError: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerError: " + error.getMessage());
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkError: " + error.getMessage());
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseError: " + error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(Config.KEY_DENY_REQUEST, deny_request);
                params.put(Config.KEY_BOOKING_ID, booking_id_to_send);
                params.put(Config.KEY_VEHICLE_ID, vehicle_id);
                params.put(Config.KEY_LAT_MAKINES, send_latitude);
                params.put(Config.KEY_LNG_MAKINES, send_longitude);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private void arrived(){

        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        final String arrivedToClient = "arrived_to_client";
        final String booking_id = Preferencat.getString(Config.BOOKING_ID_PREF, "vlera default");
        final String id_e_makines = Preferencat.getString(Config.ID_E_MAKINES_PREF, "");

        final String send_latitude  = Preferencat.getString(Config.lATITTUDE_MAKINES_PREF,"");
        final String send_longitude = Preferencat.getString(Config.lONGITUDE_MAKINES_PREF,"");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equalsIgnoreCase((Config.RESPONSE_SUCCESS))){
                            Log.d("qqq", "Driver arriti ne destinacion" + response );

                            Gjenjda_Text.setText("PO PRET KLIENTIN");
                            Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.arrived);
                            Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    mora_klientin();
                                }
                            });
                        }
                        else if(response.equalsIgnoreCase((Config.BOOKING_NOT_VALID))){
                            Log.d("qqq", "Porosia juaj u anullua nga qendra ne arrived "+ response);

                            ndaloEkzekutimin_requests = false;      // restart checkun e requesteve
                            handler.post(on_off_get_request_list);

                            ndaloEkzekutimin_if_request_valid =true;           // stop check te validitetit
                            handler.post(on_off_get_if_request_still_valid);
                            googleMap.clear();

                            new AlertDialog.Builder(ShowClientOnMap.this)
                                    .setTitle("Porosia juaj u anullua nga qendra ne arrived")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.free);
                            Gjenjda_Text.setText("I LIRE");

                        }
                        else{
                            Log.d("qqq else arrived()", response);
                            Toast.makeText(ShowClientOnMap.this, "ServerError: arrived()"+ response , Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponsearrived: " + error.getMessage());
                Toast.makeText(ShowClientOnMap.this, "General_Error"+ error.getMessage() , Toast.LENGTH_SHORT).show();

                if(error instanceof NoConnectionError) {
                    Log.d("qqq", "NoConnectionErrorarrived:: " + error.getMessage());
                    Toast.makeText(ShowClientOnMap.this, "Ju nuk keni internet per momentin. Ju lutem provoni perseri pas 5 sekondash" , Toast.LENGTH_SHORT).show();
                } else if(error instanceof TimeoutError) {
                    Log.d("qqq", "TimeoutErrorarrived:: " + error.getMessage());
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                } else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureErrorarrived:: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerErrorarrived:: " + error.getMessage());
                    Toast.makeText(ShowClientOnMap.this, "Error Serveri" + error.getMessage() , Toast.LENGTH_SHORT).show();
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkErrorarrived:: " + error.getMessage());
                    Toast.makeText(ShowClientOnMap.this, "Error Networku" + error.getMessage() , Toast.LENGTH_SHORT).show();
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseErrorarrived:: " + error.getMessage());
                    Toast.makeText(ShowClientOnMap.this, "Error Parsimi"+ error.getMessage() , Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(Config.KEY_ARRIVED_TO_CLIENT, arrivedToClient);
                params.put(Config.KEY_BOOKING_ID, booking_id);
                params.put(Config.KEY_VEHICLE_ID, id_e_makines);
                params.put(Config.KEY_LAT_MAKINES, send_latitude);
                params.put(Config.KEY_LNG_MAKINES, send_longitude);
                Log.d("qqqbook_IDarrivedClient", booking_id);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void mora_klientin(){
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        final String pickup_klient = "pickupClient";
        final String booking_id = Preferencat.getString(Config.BOOKING_ID_PREF, "vlera default");
        final String id_e_makines = Preferencat.getString(Config.ID_E_MAKINES_PREF, "");

        final String send_latitude  = Preferencat.getString(Config.lATITTUDE_MAKINES_PREF,"");
        final String send_longitude = Preferencat.getString(Config.lONGITUDE_MAKINES_PREF,"");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equalsIgnoreCase((Config.RESPONSE_SUCCESS))){
                            Log.d("qqq", "U MOR KLIENTI " + response );

                            Stadi_Makines_Free_Going.setVisibility(View.INVISIBLE);
                            Stadi_Makines_Busy.setVisibility(View.VISIBLE);
                            Gjenjda_Text.setText("I ZENE");
                            Stadi_Makines_Busy.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    lashe_klientin();
                                }
                            });
                            googleMap.clear();
                        }
                        else if(response.equalsIgnoreCase((Config.BOOKING_NOT_VALID))){
                            Log.d("qqq", "Porosia juaj u anullua nga qendra ne mora kleintin"+ response);

                            ndaloEkzekutimin_requests = false;      // restart checkun e requesteve
                            handler.post(on_off_get_request_list);
                            googleMap.clear();

                            new AlertDialog.Builder(ShowClientOnMap.this)
                                    .setTitle("Porosia juaj u anullua nga qendra ne mora klientin")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            Stadi_Makines_Free_Going.setBackgroundResource(R.drawable.free);
                            Gjenjda_Text.setText("I LIRE");
                            Stadi_Makines_Free_Going.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    //    shoferi_merr_vete_klientin();
                                }
                            });
                        }
                        else{
                            Log.d("qqq else arrived()", response);
                            Toast.makeText(ShowClientOnMap.this, "ServerError: mora_klientin()"+ response , Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponsemora: " + error.getMessage());
                Toast.makeText(ShowClientOnMap.this, "General_Error"+ error.getMessage() , Toast.LENGTH_SHORT).show();

                if(error instanceof NoConnectionError) {
                    Log.d("qqq", "NoConnectionErrormora: " + error.getMessage());
                    Toast.makeText(ShowClientOnMap.this, "Ju nuk keni internet per momentin. Ju lutem provoni perseri pas 5 sekondash" , Toast.LENGTH_SHORT).show();
                } else if(error instanceof TimeoutError) {
                    Log.d("qqq", "TimeoutErrormora: " + error.getMessage());
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                } else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureErrormora: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerErrormora: " + error.getMessage());
                    Toast.makeText(ShowClientOnMap.this, "Error Serveri" + error.getMessage() , Toast.LENGTH_SHORT).show();
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkErrormora: " + error.getMessage());
                    Toast.makeText(ShowClientOnMap.this, "Error Networku" + error.getMessage() , Toast.LENGTH_SHORT).show();
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseErrormora: " + error.getMessage());
                    Toast.makeText(ShowClientOnMap.this, "Error Parsimi"+ error.getMessage() , Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                ndaloEkzekutimin_if_request_valid = true;                                           // ndalojme ekzekutimin e if request is still valid
                handler.post(on_off_get_if_request_still_valid);
                Log.d("qqq", "u ndalua kerkimi per still valid ne Map<String>  arrived() before returning parameters");

                params.put(Config.KEY_PICKUP_CLIENT, pickup_klient);
                params.put(Config.KEY_BOOKING_ID, booking_id);
                params.put(Config.KEY_VEHICLE_ID, id_e_makines);
                params.put(Config.KEY_LAT_MAKINES, send_latitude);
                params.put(Config.KEY_LNG_MAKINES, send_longitude);
                Log.d("qqqbook_IDmora_klientin", booking_id);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private void lashe_klientin(){
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        final String pullover_klient = "pullOverClient";
        final String booking_idl = Preferencat.getString(Config.BOOKING_ID_PREF, "vlera default");
        final String id_e_makines = Preferencat.getString(Config.ID_E_MAKINES_PREF, "");
        final String pullover_lat = Preferencat.getString(Config.lATITTUDE_MAKINES_PREF, "");
        final String pullover_long = Preferencat.getString(Config.lONGITUDE_MAKINES_PREF, "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equalsIgnoreCase((Config.RESPONSE_SUCCESS))){
                            Log.d("qqq", "u derguan koordinatat e LENIES se klientit");
                            googleMap.clear();
                            finish();    ///////////////////////////// added after live
                            System.exit(0);
                        }else{
                            Log.d("qqq", response);
                            Toast.makeText(ShowClientOnMap.this, "ServerError: lashe_klientin()"+ response , Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponse: " + error.getMessage());
                if(error instanceof NoConnectionError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju nuk keni internet per momentin. Ju lutem provoni perseri pas 5 sekondash" , Toast.LENGTH_SHORT).show();
                }
                else if( error instanceof TimeoutError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureError: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerError: " + error.getMessage());
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkError: " + error.getMessage());
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseError: " + error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(Config.KEY_PULLOVER_CLIENT, pullover_klient);
                params.put(Config.KEY_BOOKING_ID, booking_idl);
                params.put(Config.KEY_VEHICLE_ID, id_e_makines);
                params.put(Config.KEY_LAT_MAKINES, pullover_lat);
                params.put(Config.KEY_LNG_MAKINES, pullover_long);
                Log.d("qqqlashe_klientinlLAT", pullover_lat);
                Log.d("qqqlashe_klientinLNG", pullover_long);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void logout(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AD_logout_style);
        alertDialogBuilder.setMessage("Jeni i sigurte qe doni të dilni nga llogaria?");
        alertDialogBuilder.setPositiveButton("Po",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor set_Prefs = Preferencat.edit();
                        set_Prefs.putBoolean(Config.LOGGEDIN_SHARED_PREF, false);
                        set_Prefs.putString(Config.PASSWORDI_SHARED_PREF, "");
                        set_Prefs.commit();

                        ndaloEkzekutimin_requests =true;                                           // ndalojme ekzekutimin e requesteve
                        handler.post(on_off_get_request_list);
                        Log.d("qqq", "u ndalua requestet");
                        logout_serveri();
                    }
                });
        alertDialogBuilder.setNegativeButton("Jo",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void logout_serveri(){
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        final String logouti_server = "logout";
        final String vehiclee_id = Preferencat.getString(Config.ID_E_MAKINES_PREF,"");
        final String driverr_id =  Preferencat.getString(Config.ID_E_SHOFERIT_PREF, "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase(String.valueOf(Config.RESPONSE_SUCCESS))) {
                            Log.d("qqq", "U dergua LOGOUT NE SERVER");
                            Intent intent = new Intent(ShowClientOnMap.this, LoginActivity.class);
                            startActivity(intent);
                            System.exit(0);
                        } else {
                            Log.d("qqq", "ERRORRI LOGOUT_SERVERI:" + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponse: " + error.getMessage());
                if(error instanceof NoConnectionError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju nuk keni internet per momentin. Ju lutem provoni perseri pas 5 sekondash" , Toast.LENGTH_SHORT).show();
                }
                else if( error instanceof TimeoutError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureError: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerError: " + error.getMessage());
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkError: " + error.getMessage());
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseError: " + error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(Config.KEY_LOGOUT, logouti_server);
                params.put(Config.KEY_VEHICLE_ID, vehiclee_id);
                params.put(Config.KEY_DRIVER_ID, driverr_id);
                Log.d("qqqlogg", logouti_server);
                Log.d("qqqlogg", vehiclee_id);
                Log.d("qqqlogg", driverr_id);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }


        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.BLUE);
            }

            // Drawing polyline in the Google Map for the i-th route
            googleMap.addPolyline(lineOptions);
        }
    }

    public void onBackPressed() {
    }
}
/*   private void shoferi_merr_vete_klientin(){
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        final String driver_request = "DriverCreatesRequest";
        final String id_e_makines = Preferencat.getString(Config.ID_E_MAKINES_PREF, "");
        final String id_e_shoferit =  Preferencat.getString(Config.ID_E_SHOFERIT_PREF, "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response.equalsIgnoreCase((Config.RESPONSE_ERROR))){
                            Toast.makeText(ShowClientOnMap.this, "ServerError: shoferi_merr_vete_klientin()"+ response , Toast.LENGTH_LONG).show();
                        }
                        else if(response.equalsIgnoreCase((Config.RESPONSE_POROSI_EKZISTON))){
                            Toast.makeText(ShowClientOnMap.this, "POROSIA EKZISTON"+ response , Toast.LENGTH_LONG).show();
                        }
                        else{
                            SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor set_Prefs = Preferencat.edit();
                            set_Prefs.putString(Config.BOOKING_ID_PREF, response);
                            Log.d("qqq", response);
                            set_Prefs.commit();
                            ndaloEkzekutimin_requests = true;      // ndalo checkun e requesteve                            handler.post(on_off_get_request_list);
                            Stadi_Makines_Free_Going.setVisibility(View.INVISIBLE);
                            Stadi_Makines_Busy.setVisibility(View.VISIBLE);
                            Gjenjda_Text.setText("I ZENE");
                            Stadi_Makines_Busy.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    lashe_klientin();
                                }
                            });
                            Log.d("qqqbbbbbbbb",  response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("qqq", "ErrorResponse: " + error.getMessage());
                if(error instanceof NoConnectionError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju nuk keni internet per momentin. Ju lutem provoni perseri pas 5 sekondash" , Toast.LENGTH_SHORT).show();
                }
                else if( error instanceof TimeoutError) {
                    Toast.makeText(ShowClientOnMap.this, "Ju keni TIMEOUT. BENI RESTART Internetin" , Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof AuthFailureError) {
                    Log.d("qqq", "AuthFailureError: " + error.getMessage());
                } else if (error instanceof ServerError) {
                    Log.d("qqq", "ServerError: " + error.getMessage());
                } else if (error instanceof NetworkError) {
                    Log.d("qqq", "NetworkError: " + error.getMessage());
                } else if (error instanceof ParseError) {
                    Log.d("qqq", "ParseError: " + error.getMessage());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(Config.KEY_DRIVER_REQUEST, driver_request);
                params.put(Config.KEY_VEHICLE_ID, id_e_makines);
                params.put(Config.KEY_DRIVER_ID, id_e_shoferit);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    */