package com.jeddigital.kerkotaxi_vehicle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import com.jeddigital.kerkotaxi_vehicle.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextKodiUnik;
    private ImageButton buttonLogin;
    private boolean loggedIn = false;
    TextView Nr_makines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        editTextKodiUnik = (EditText) findViewById(R.id.editTextPassword);
        buttonLogin = (ImageButton) findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this);

        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor set_Prefs = Preferencat.edit();
        set_Prefs.putString(Config.ID_E_MAKINES_PREF, "1");  // ****************************************************************  vendos manualisht nr e taxise    ************************************************
        set_Prefs.putString(Config.ID_E_STATUSIT_TE_MAKINES_PREF, "1"); // ***************************************************** vendos manualisht stadin e taxise ************************************************
        set_Prefs.commit();

        Nr_makines = (TextView) findViewById(R.id.nrMakine);
        Nr_makines.setText("MAKINA NR. " + Preferencat.getString(Config.ID_E_MAKINES_PREF, ""));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("qqq", "On Resumeeeeeeeeeeeeee111111");
        //In onresume fetching value from sharedpreference
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        //Fetching the boolean value form sharedpreferences
        loggedIn = Preferencat.getBoolean(Config.LOGGEDIN_SHARED_PREF, false);
        //If we will get true

        if(loggedIn){
            //We will start the Profile Activity
            Intent intent = new Intent(LoginActivity.this, ShowClientOnMap.class);
            startActivity(intent);
        }
    }
    private void login(){
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String login = "login";
        final String password = editTextKodiUnik.getText().toString().trim();
        final String vehicle_id = Preferencat.getString(Config.ID_E_MAKINES_PREF, "") ;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equalsIgnoreCase((Config.LOGIN_NOT_SUCCESS))){
                            Toast.makeText(LoginActivity.this, "KODI JUAJ ESHTE I PAVLEFSHEM", Toast.LENGTH_LONG).show();
                        }
                        else{
                            SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor set_Prefs = Preferencat.edit();
                            set_Prefs.putBoolean(Config.LOGGEDIN_SHARED_PREF, true);
                            set_Prefs.putString(Config.PASSWORDI_SHARED_PREF, password);
                            set_Prefs.putString(Config.ID_E_SHOFERIT_PREF, response);
                            set_Prefs.commit();
                            vehicleStatus();

                            Intent intent = new Intent(LoginActivity.this, ShowClientOnMap.class);
                            startActivity(intent);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("qqq", "ErrorResponseLogin: " + error.getMessage());
                        if(error instanceof NoConnectionError) {
                            Toast.makeText(LoginActivity.this, "Ju nuk keni internet per momentin. Ju lutem provoni perseri pas 5 sekondash" , Toast.LENGTH_SHORT).show();
                        }
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                  params.put(Config.KEY_LOGIN, login);
                  params.put(Config.KEY_PASSWORD, password);
                  params.put(Config.KEY_VEHICLE_ID, vehicle_id);
                 return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void vehicleStatus(){
        SharedPreferences Preferencat = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String vehicle_status = "vehiclestatus";
        final String vehicle_id = Preferencat.getString(Config.ID_E_MAKINES_PREF, "");
        final String vehicle_status_id = Preferencat.getString(Config.ID_E_STATUSIT_TE_MAKINES_PREF, "");
        final String driver_id =  Preferencat.getString(Config.ID_E_SHOFERIT_PREF, "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.WEB_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase(String.valueOf(Config.RESPONSE_SUCCESS))) {
                            Log.d("qqq", "U dergua vehicle status me" + "vehicle_id:  " + vehicle_id + "vehicle_status_id:  " + vehicle_status_id + "driver_id  " + driver_id);
                        } else {
                            Toast.makeText(LoginActivity.this, response , Toast.LENGTH_LONG).show();
                            Log.d("qqq", " erroriiiiiiiiiiiiiiiiiiiiiii vehicle stauts *********:" + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(Config.KEY_VEHICLE_STATUS, vehicle_status);
                params.put(Config.KEY_VEHICLE_ID, vehicle_id);
                params.put(Config.KEY_VEHICLE_STATUS_ID, vehicle_status_id);
                params.put(Config.KEY_DRIVER_ID, driver_id);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public void onClick(View v) {
        login();
    }

}

