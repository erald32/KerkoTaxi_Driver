package com.jeddigital.kerkotaxi_vehicle;
public class Config {
    //URL to web app file (app.php)
    public static final String WEB_APP_URL = "http://www.1upinteractive.com/vodafonetour/taxi/services/vehicles/vehicles.php";

    //Keys for login code  as defined in our $_POST['key'] in app.php
    public static final String KEY_LOGIN = "login";
    public static final String KEY_LOGOUT = "logout";
    public static final String KEY_PASSWORD = "password";

    public static final String KEY_VEHICLE_STATUS = "vehiclestatus";
    public static final String KEY_VEHICLE_ID = "vehicle_id";
    public static final String KEY_VEHICLE_STATUS_ID = "status_id";
    public static final String KEY_DRIVER_ID = "driver_id";

    public static final String KEY_VEHICLE_LOCATION = "vehicleLocation";
    public static final String KEY_LAT_MAKINES = "latitude";
    public static final String KEY_LNG_MAKINES = "longitude";

    public static final String KEY_GET_REQUEST_LIST = "getrequestlist";
    public static final String KEY_ACCEPT_REQUEST = "acceptrequest";
    public static final String KEY_GET_STATUS_REQUEST = "getrequeststatus";
    public static final String KEY_GET_BOOKING_STATUS = "bookingstatus";
    public static final String KEY_DENY_REQUEST = "cancelrequest";
    public static final String KEY_DENY_REQUEST_ON_WAITING = "cancelrequest_waiting_client";
    public static final String KEY_BOOKING_ID= "booking_id";
    public static final String KEY_PICKUP_CLIENT = "pickupClient";
    public static final String KEY_ARRIVED_TO_CLIENT = "arrived_to_client";
    public static final String KEY_PULLOVER_CLIENT = "pullOverClient";
    public static final String KEY_DRIVER_REQUEST = "DriverCreatesRequest";

    //If server response is equal to:
    public static final String LOGIN_NOT_SUCCESS = "0";
    public static final String LOGIN_EXISTS = "driver_logged_in";
    public static final String RESPONSE_ERROR = "error";
    public static final String RESPONSE_SUCCESS = "Success";
    public static final String RESPONSE_POROSI_EKZISTON = "PorosiaEkziston";
    public static final String BOOKING_NOT_VALID = "booking_not_valid";
    public static final String BOOKING_NOT_VALID_CLIENT_REFUSE = "booking_not_valid_client_refuse";
    public static final String BOOKING_NOT_VALID_SYSTEM_REFUSE = "booking_not_valid_system_refuse";

    //Keys for Sharedpreferences
    //This would be the name of our shared preferences
    public static final String SHARED_PREF_NAME = "myloginapp";

    public static final String LOGGEDIN_SHARED_PREF = "boolean true false";
    public static final String PASSWORDI_SHARED_PREF = "kodi_unik_identifikues";

    public static final String ID_E_MAKINES_PREF = "id_e_makines_qe_vendoset_kur_instalohet_apliakcioni";
    public static final String ID_E_STATUSIT_TE_MAKINES_PREF = "1";
    public static final String ID_E_SHOFERIT_PREF = "id_e_shoferit_qe_vjen_si_pergjigje_ne_login_nga_serveri";

    public static final String EMRI_SHOFERIT_PREF = "emri shoferit_qe_vjen_si_pergjigje_ne_login_nga_serveri";

    public static final String BOOKING_ID_PREF = "BookingIdnedatabase";
    public static final String ADRESA_KLIENTIT_PREF = "Adresa e klientit qe vjen nga serveri";
    public static final String EMRI_KLIENTIT_PREF = "Emri i klientit qe vjen nga serveri";
    public static final String NR_CEL_KLIENTIT_PREF = "nr i klientit qe vjen nga serveri";
    public static final String REQUESTED_TIME_E_KLIENTIT_PREF = "Koha e kerkuar nga klienti per ti ardeh taksia";
    public static final String NOTES_E_KLIENTIT_PREF = "Shenime te ndryshme rreth klientit";

    public static final String LATITUDE_KLIENTIT_PREF = "latitude klientit qe vjen nga serveri";
    public static final String lONGITUDE_KLIENTIT_PREF = "longitude klientit qe vjen nga serveri";

    public static final String lATITTUDE_MAKINES_PREF = "latitude makines";
    public static final String lONGITUDE_MAKINES_PREF = "longitude makines";
}