package org.smartregister.anc.library.activity;

import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.anc.library.AncLibrary;
import org.smartregister.anc.library.BuildConfig;
import org.smartregister.anc.library.R;
import org.smartregister.anc.library.contract.RegisterContract;
import org.smartregister.anc.library.domain.AttentionFlag;
import org.smartregister.anc.library.domain.Contact;
import org.smartregister.anc.library.event.PatientRemovedEvent;
import org.smartregister.anc.library.event.ShowProgressDialogEvent;
import org.smartregister.anc.library.fragment.AdvancedSearchFragment;
import org.smartregister.anc.library.fragment.HomeRegisterFragment;
import org.smartregister.anc.library.fragment.LibraryFragment;
import org.smartregister.anc.library.fragment.MeFragment;
import org.smartregister.anc.library.fragment.SortFilterFragment;
import org.smartregister.anc.library.presenter.RegisterPresenter;
import org.smartregister.anc.library.repository.PatientRepository;
import org.smartregister.anc.library.util.ANCFormUtils;
import org.smartregister.anc.library.util.ANCJsonFormUtils;
import org.smartregister.anc.library.util.ConstantsUtils;
import org.smartregister.anc.library.util.DBConstantsUtils;
import org.smartregister.anc.library.util.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.model.Field;
import org.smartregister.domain.FetchStatus;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import io.fabric.sdk.android.services.concurrency.AsyncTask;
import timber.log.Timber;

/**
 * Created by keyman on 26/06/2018.
 */

public class BaseHomeRegisterActivity extends BaseRegisterActivity implements RegisterContract.View, SyncStatusBroadcastReceiver.SyncStatusListener





{
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    private AlertDialog recordBirthAlertDialog;
    private AlertDialog attentionFlagAlertDialog;
    private View attentionFlagDialogView;
    private boolean isAdvancedSearch = false;
    private boolean isLibrary = false;
    private String advancedSearchQrText = "";
    private HashMap<String, String> advancedSearchFormData = new HashMap<>();

    private RegisterContract.Interactor interactor;

    static String[] JanuaryData = new String[53];
    static String[] FebruaryData = new String[53];
    static String[] MarchData = new String[53];
    static String[] AprilData = new String[53];
    static String[] MayData = new String[53];
    static String[] JuneData = new String[53];
    static String[] JulyData = new String[53];
    static String[] AugustData = new String[53];
    static String[] SeptemberData = new String[53];
    static String[] OctoberData = new String[53];
    static String[] NovemberData = new String[53];
    static String[] DecemberData = new String[53];
    //String[] TotalData = new String[53];
    static int currentMonth = 1;
    public static Context context;

    private static ImageView hia2ReportingImage;
    private static TextView hia2ReportingText;
    private static ProgressBar reportProgress;

    public static String username;

    public static String password;
    public static String code;
    public static String facility;
    public static String name;
    public static String nrc;
    public static String district;
    public static String phone;
    String locationId =  null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordBirthAlertDialog = createAlertDialog();
        createAttentionFlagsAlertDialog();
        this.context = BaseHomeRegisterActivity.this;
        SyncStatusBroadcastReceiver.init(this);
        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(this);

        if (username != null && password != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String code = sp.getString("code", "0000");

            if (!sp.contains("code") || code.equals("0000")) {

                getToken(username, password);

            }

        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

    }


    private void getToken (final String username, final String password) {

        String tag_string_req = "req_login";

        String url = "https://keycloak.zeir.smartregister.org/auth/realms/namai-stage/protocol/openid-connect/token";
        StringRequest
                stringRequest
                = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {


                        String jsonInString = new Gson().toJson(response.toString().trim());
                        try {
                            JSONObject jsonObject = new JSONObject(response.toString().trim());

                            String token  = jsonObject.getString("access_token");

                            getCreds(token);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                error -> {

                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("grant_type","password");
                params.put("username",username);
                params.put("password",password);
                params.put("scope","openid");
                params.put("client_id", BuildConfig.OAUTH_CLIENT_ID);
                params.put("client_secret",BuildConfig.OAUTH_CLIENT_SECRET);
                return params;
            }};

        addToRequestQueue(stringRequest, tag_string_req);

    }


    private void getCreds(String token){

        Log.i("mwamba_Namia_token ", "mwamba_Namia_token" + token);

        String tag_string_creds = "req_creds";

        String url = "https://keycloak.zeir.smartregister.org/auth/realms/namai-stage/protocol/openid-connect/userinfo";
        StringRequest
                stringRequest
                = new StringRequest(
                Request.Method.GET,
                url,
                (Response.Listener<String>) response -> {

                    try {
                        JSONObject jObj = new JSONObject(response);

                        code = jObj.getString("code");
                        facility = jObj.getString("facility");
                        district = jObj.getString("district");
                        name = jObj.getString("name");
                        phone = jObj.getString("phone");
                        nrc = jObj.getString("nrc");

                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseHomeRegisterActivity.this);
                        SharedPreferences.Editor edit = sp.edit();


                        edit.putString("code", code);
                        edit.putString("facility", facility);
                        edit.putString("district", district);
                        edit.putString("name", name);
                        edit.putString("phone", phone);
                        edit.putString("nrc", nrc);

                        edit.commit();
                        finish();
                        startActivity(getIntent());

                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                },
                error -> {

                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }};


        addToRequestQueue(stringRequest, tag_string_creds);

    }

    public void addToRequestQueue(Request<String> request, String tag) {
        RequestQueue requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        if (tag != null) {
            request.setTag(tag);
        }

        requestQueue.add(request);
    }


    public static String getFacilityID(){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String facilityID = sp.getString("code", "anonymous");

        if (facilityID.contains("anonymous")){
            facilityID = code;
        }
        return facilityID;
    }

    public static String getName(){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String nameSP = sp.getString("name", "anonymous");

        if (nameSP.contains("anonymous")){
            nameSP = name;
        }
        return nameSP;

    }

    public static String getPhone(){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String phoneSP = sp.getString("phone", "anonymous");

        if (phoneSP.contains("anonymous")){
            phoneSP = phone;
        }
        return phoneSP;
    }

    public static String getNRC(){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String nrcSP = sp.getString("nrc", "anonymous");

        if (nrcSP.contains("anonymous")){
            nrcSP = code;
        }
        return nrcSP;
    }


    @SuppressLint("ResourceAsColor")
    public static void loadReports()
    {


        //ReportModel1 data = reportitems.get(position);

        for (int month = currentMonth; month <= currentMonth + 11; month++) {
            int adjustedMonth = (month - 1) % 12 + 1;
            int useMonth = month;
            String FILENAME = useMonth + "_" + "monthData.txt";
            String filePath = Utils.getAppPath(context) + FILENAME;
            File file = new File(filePath);
            if (!file.exists()) {
                try {

                    new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected List<String> doInBackground(Void... voids) {

                            List<String> results = new ArrayList<>();

                            String[] monthData = new String[53];

                            for (int i = 0; i < monthData.length; i++) {
                                monthData[i] = null;
                            }

                            final String[] f1first = {null};
                            final String[] f2first = {null};
                            final String[] f3first = {null};
                            final String[] f4first = {null};
                            final String[] f1second = {null};
                            final String[] f2second = {null};
                            final String[] f3second = {null};
                            final String[] f4second = {null};
                            final String[] f1third = {null};
                            final String[] f2third = {null};
                            final String[] f3third = {null};
                            final String[] f4third = {null};
                            String totalfemalesfirst = null;
                            String totalfemalessecond = null;
                            String totalfemalesthird = null;
                            final String[] origin = {null};//null last check
                            final String[] firstc = {null};//null last check
                            final String[] secondc = {null};//null last check
                            final String[] thirdc = {null};//null last check
                            final String[] fourthToSeventhC = {null};
                            final String[] eighthAboveC = {null};
                            final String[] highRiskC = {null};
                            final String[] syphScreened = {null};
                            final String[] syphPositive = {null};
                            final String[] HepBScreened = {null};
                            final String[] HepBPositive = {null};
                            final String[] AnaemiaScreened = {null};
                            final String[] AnaemiaPositive = {null};
                            final String[] IPTP1 = {null};
                            final String[] IPTP2 = {null};
                            final String[] IPTP3 = {null};
                            final String[] IPTP4 = {null};
                            final String[] providedITN = {null};
                            final String[] providedIron = {null};
                            final String[] dewormed = {null};
                            final String[] startedOnPrep = {null};
                            final String[] alreadyOnPrep = {null};//null last check
                            final String[] startedOnART = {null};
                            final String[] alreadyOnART = {null};
                            final String[] followUp = {null};
                            final String[] discordant = {null};
                            final String[] maleStartedART = {null};
                            final String[] maleAlreadyPositive = {null};
                            final String[] malePositive = {null};
                            final String[] maleTested = {null};//null last check
                            final String[] viralLoad = {null};
                            final String[] suppressedVL = {null};//null last check
                            final String[] onART = {null};
                            final String[] testedPositive = {null};
                            final String[] alreadyPositive = {null};//null last check
                            final String[] testedHIV = {null};
                            final String[] ttcvPlusTwo = {null};
                            final String[] referredTB = {null};
                            final String[] contactCount = {null};
                            final String[] screenedTB = {null};

                            System.out.println("Loading data for month :" + useMonth);

                            if (monthData[0] == null) {
                                //Month being loaded
                                monthData[0] = String.valueOf(useMonth);
                                //monthData[0] = String.valueOf(selectedMonth);
                            }

                            results.add(monthData[0]);

                            if (monthData[1] == null) {
                                f1first[0] = ClientDao.getFirstContact("gest_age", "8", "12", monthData[0]);
                                monthData[1] = f1first[0];
                                if (monthData[1] == null) {
                                    monthData[1] = "0";
                                }
                            }
                            results.add(f1first[0]);

                            if (monthData[2] == null) {
                                f2first[0] = ClientDao.getFirstContactAbove15("gest_age", "8", "12", monthData[0]);
                                monthData[2] = f2first[0];
                                if (monthData[2] == null) {
                                    monthData[2] = "0";
                                }
                            }
                            results.add(f2first[0]);

                            if (monthData[3] == null) {
                                f3first[0] = ClientDao.getFirstContactAbove20("gest_age", "8", "12", monthData[0]);
                                monthData[3] = f3first[0];
                            }
                            results.add(f3first[0]);

                            if (monthData[4] == null) {
                                f4first[0] = ClientDao.getFirstContactAbove25("gest_age", "8", "12", monthData[0]);
                                if (f4first[0] == null) {
                                    f4first[0] = "0";
                                }
                                monthData[4] = f4first[0];
                            }
                            results.add(f4first[0]);


                            if (monthData[5] == null) {
                                f1second[0] = ClientDao.getFirstContact("gest_age", "13", "26", monthData[0]);
                                monthData[5] = f1second[0];
                            }
                            results.add(f1second[0]);

                            if (monthData[6] == null) {
                                f2second[0] = ClientDao.getFirstContactAbove15("gest_age", "13", "26", monthData[0]);
                                monthData[6] = f2second[0];
                            }
                            results.add(f2second[0]);

                            if (monthData[7] == null) {
                                f3second[0] = ClientDao.getFirstContactAbove20("gest_age", "13", "26", monthData[0]);
                                monthData[7] = f3second[0];
                            }
                            results.add(f3second[0]);


                            if (monthData[8] == null) {
                                f4second[0] = ClientDao.getFirstContactAbove25("gest_age", "13", "26", monthData[0]);
                                monthData[8] = f4second[0];
                            }
                            results.add(f4second[0]);


                            if (monthData[9] == null) {
                                f1third[0] = ClientDao.getFirstContact("gest_age", "27", "40", monthData[0]);
                                monthData[9] = f1third[0];
                            }
                            results.add(f1third[0]);


                            if (monthData[10] == null) {
                                f2third[0] = ClientDao.getFirstContactAbove15("gest_age", "27", "40", monthData[0]);
                                monthData[10] = f2third[0];
                            }
                            results.add(f2third[0]);


                            if (monthData[11] == null) {
                                f3third[0] = ClientDao.getFirstContactAbove20("gest_age", "27", "40", monthData[0]);
                                monthData[11] = f3third[0];
                            }
                            results.add(f3third[0]);


                            if (monthData[12] == null) {
                                f4third[0] = ClientDao.getFirstContactAbove25("gest_age", "27", "40", monthData[0]);
                                monthData[12] = f4third[0];
                            }
                            results.add(f4third[0]);


                            String originStr = "";
                            if (origin[0] == null) {
                                originStr = String.valueOf(ClientDao.getAllOutside(monthData[0]));
                                origin[0] = originStr;
                            } else {
                                originStr = origin[0];
                            }

                            if (origin[0] != null) {
                                monthData[13] = origin[0];
                            }


                            String firstcStr = "";
                            if (firstc[0] == null) {
                                firstcStr = String.valueOf(ClientDao.getAllFirstContact(monthData[0]));
                                firstc[0] = firstcStr;
                                monthData[14] = firstc[0];
                            } else {
                                firstcStr = firstc[0];
                                monthData[14] = firstc[0];
                            }


                            String secondcStr = "";
                            if (secondc[0] == null) {
                                secondcStr = String.valueOf(ClientDao.getAllSecondContact(monthData[0]));
                                secondc[0] = secondcStr;
                                monthData[15] = secondc[0];
                            } else {
                                secondcStr = secondc[0];
                                monthData[15] = secondc[0];
                            }

                            String thirdcStr = "";
                            if (thirdc[0] == null) {
                                thirdcStr = String.valueOf(ClientDao.getAllThirdContact(monthData[0]));
                                thirdc[0] = thirdcStr;
                                monthData[16] = thirdc[0];
                            } else {
                                thirdcStr = thirdc[0];
                                monthData[16] = thirdc[0];
                            }

                            String fourthToSeventhcStr = "";
                            if (fourthToSeventhC[0] == null) {
                                fourthToSeventhcStr = String.valueOf(ClientDao.getAllFourthToSeventhContact(monthData[0]));
                                fourthToSeventhC[0] = fourthToSeventhcStr;
                                monthData[17] = fourthToSeventhcStr;
                            } else {
                                fourthToSeventhcStr = fourthToSeventhC[0];
                                monthData[17] = fourthToSeventhcStr;
                            }

                            String eighthAbovecStr = "";
                            if (eighthAboveC[0] == null) {
                                eighthAbovecStr = String.valueOf(ClientDao.getAllEighthAboveContact(monthData[0]));
                                eighthAboveC[0] = eighthAbovecStr;
                                monthData[18] = eighthAbovecStr;
                            } else {
                                eighthAbovecStr = eighthAboveC[0];
                                monthData[18] = eighthAbovecStr;
                            }

                            String highRiskStr = "";
                            if (highRiskC[0] == null) {
                                highRiskStr = String.valueOf(ClientDao.getAllHighRiskContact(monthData[0]));
                                highRiskC[0] = highRiskStr;
                                monthData[19] = highRiskStr;
                            } else {
                                highRiskStr = highRiskC[0];
                                monthData[19] = highRiskStr;
                            }

                            String syphScreenedStr = "";
                            if (syphScreened[0] == null) {
                                syphScreenedStr = String.valueOf(ClientDao.getAllSyphScreenedContact(monthData[0]));
                                syphScreened[0] = syphScreenedStr;
                                monthData[20] = syphScreenedStr;
                            } else {
                                syphScreenedStr = syphScreened[0];
                                monthData[20] = syphScreenedStr;
                            }

                            String syphPositiveStr = "";
                            if (syphPositive[0] == null) {
                                syphPositiveStr = String.valueOf(ClientDao.getAllSyphPositiveContact(monthData[0]));
                                syphPositive[0] = syphPositiveStr;
                                monthData[21] = syphPositiveStr;
                            } else {
                                syphPositiveStr = syphPositive[0];
                                monthData[21] = syphPositiveStr;
                            }

                            String HepBScreenedStr = "";
                            if (HepBScreened[0] == null) {
                                HepBScreenedStr = String.valueOf(ClientDao.getAllHepBScreenedContact(monthData[0]));
                                HepBScreened[0] = HepBScreenedStr;
                                monthData[22] = HepBScreenedStr;
                            } else {
                                HepBScreenedStr = HepBScreened[0];
                                monthData[22] = HepBScreenedStr;
                            }

                            String HepBPositiveStr = "";
                            if (HepBPositive[0] == null) {
                                HepBPositiveStr = String.valueOf(ClientDao.getAllHepBPositiveContact(monthData[0]));
                                HepBPositive[0] = HepBPositiveStr;
                                monthData[23] = HepBPositiveStr;
                            } else {
                                HepBPositiveStr = HepBPositive[0];
                                monthData[23] = HepBPositiveStr;
                            }

                            String AnaemiaScreenedStr = "";
                            if (AnaemiaScreened[0] == null) {
                                AnaemiaScreenedStr = String.valueOf(ClientDao.getAllAnaemiaScreenedContact(monthData[0]));
                                AnaemiaScreened[0] = AnaemiaScreenedStr;
                                monthData[24] = AnaemiaScreenedStr;
                            } else {
                                AnaemiaScreenedStr = AnaemiaScreened[0];
                                monthData[24] = AnaemiaScreenedStr;
                            }

                            String AnaemiaPositiveStr = "";
                            if (AnaemiaPositive[0] == null) {
                                AnaemiaPositiveStr = String.valueOf(ClientDao.getAllAnaemiaPositiveContact(monthData[0]));
                                AnaemiaPositive[0] = AnaemiaPositiveStr;
                                monthData[25] = AnaemiaPositiveStr;
                            } else {
                                AnaemiaPositiveStr = AnaemiaPositive[0];
                                monthData[25] = AnaemiaPositiveStr;
                            }

                            String IPTP1Str = "";
                            if (IPTP1[0] == null) {
                                IPTP1Str = String.valueOf(ClientDao.getAllIPTP1Contact(monthData[0]));
                                IPTP1[0] = IPTP1Str;
                                monthData[26] = IPTP1Str;
                            } else {
                                IPTP1Str = IPTP1[0];
                                monthData[26] = IPTP1Str;
                            }

                            String IPTP2Str = "";
                            if (IPTP2[0] == null) {
                                IPTP2Str = String.valueOf(ClientDao.getAllIPTP2Contact(monthData[0]));
                                IPTP2[0] = IPTP2Str;
                                monthData[27] = IPTP2Str;
                            } else {
                                IPTP2Str = IPTP2[0];
                                monthData[27] = IPTP2Str;
                            }

                            String IPTP3Str = "";
                            if (IPTP3[0] == null) {
                                IPTP3Str = String.valueOf(ClientDao.getAllIPTP3Contact(monthData[0]));
                                IPTP3[0] = IPTP3Str;
                                monthData[28] = IPTP3Str;
                            } else {
                                IPTP3Str = IPTP3[0];
                                monthData[28] = IPTP3Str;
                            }

                            String IPTP4Str = "";
                            if (IPTP4[0] == null) {
                                IPTP4Str = String.valueOf(ClientDao.getAllIPTP4Contact(monthData[0]));
                                IPTP4[0] = IPTP4Str;
                                monthData[29] = IPTP4Str;
                            } else {
                                IPTP4Str = IPTP4[0];
                                monthData[29] = IPTP4Str;
                            }

                            String ProvidedITNStr = "";
                            if (providedITN[0] == null) {
                                ProvidedITNStr = String.valueOf(ClientDao.getAllProvidedITNContact(monthData[0]));
                                providedITN[0] = ProvidedITNStr;
                                monthData[30] = ProvidedITNStr;
                            } else {
                                ProvidedITNStr = providedITN[0];
                                monthData[30] = ProvidedITNStr;
                            }

                            String ProvidedIronStr = "";
                            if (providedIron[0] == null) {
                                ProvidedIronStr = String.valueOf(ClientDao.getAllProvidedIronContact(monthData[0]));
                                providedIron[0] = ProvidedIronStr;
                                monthData[31] = ProvidedIronStr;
                            } else {
                                ProvidedIronStr = providedIron[0];
                                monthData[31] = ProvidedIronStr;
                            }

                            String DewormedStr = "";
                            if (dewormed[0] == null) {
                                DewormedStr = String.valueOf(ClientDao.getAllDewormedContact(monthData[0]));
                                dewormed[0] = DewormedStr;
                                monthData[32] = DewormedStr;
                            } else {
                                DewormedStr = dewormed[0];
                                monthData[32] = DewormedStr;
                            }

                            String StartedOnPrepStr = "";
                            if (startedOnPrep[0] == null) {
                                StartedOnPrepStr = String.valueOf(ClientDao.getAllStartedOnPrepContact(monthData[0]));
                                startedOnPrep[0] = StartedOnPrepStr;
                                monthData[33] = StartedOnPrepStr;
                            } else {
                                StartedOnPrepStr = startedOnPrep[0];
                                monthData[33] = StartedOnPrepStr;
                            }

                            String AlreadyOnPrepStr = "";
                            if (alreadyOnPrep[0] == null) {
                                AlreadyOnPrepStr = String.valueOf(ClientDao.getAllAlreadyOnPrepContact(monthData[0]));
                                alreadyOnPrep[0] = AlreadyOnPrepStr;
                                monthData[34] = AlreadyOnPrepStr;
                            } else {
                                AlreadyOnPrepStr = alreadyOnPrep[0];
                                monthData[34] = AlreadyOnPrepStr;
                            }

                            String StartedOnARTStr = "";
                            if (startedOnART[0] == null) {
                                StartedOnARTStr = String.valueOf(ClientDao.getAllStartedARTinANCContact(monthData[0]));
                                startedOnART[0] = StartedOnARTStr;
                                monthData[35] = StartedOnARTStr;
                            } else {
                                StartedOnARTStr = startedOnART[0];
                                monthData[35] = StartedOnARTStr;
                            }

                            String AlreadyOnARTStr = "";
                            if (alreadyOnART[0] == null) {
                                AlreadyOnARTStr = String.valueOf(ClientDao.getAllAlreadyARTinANCContact(monthData[0]));
                                alreadyOnART[0] = AlreadyOnARTStr;
                                monthData[36] = AlreadyOnARTStr;
                            } else {
                                AlreadyOnARTStr = alreadyOnART[0];
                                monthData[36] = AlreadyOnARTStr;
                            }

                            String FollowUpcStr = "";
                            if (followUp[0] == null) {
                                FollowUpcStr = String.valueOf(ClientDao.getAllFollowUpContact(monthData[0]));
                                followUp[0] = FollowUpcStr;
                                monthData[37] = FollowUpcStr;
                            } else {
                                FollowUpcStr = followUp[0];
                                monthData[37] = FollowUpcStr;
                            }

                            String DiscordantcStr = "";
                            if (discordant[0] == null) {
                                DiscordantcStr = String.valueOf(ClientDao.getAllDiscordantContact(monthData[0]));
                                discordant[0] = DiscordantcStr;
                                monthData[38] = DiscordantcStr;
                            } else {
                                DiscordantcStr = discordant[0];
                                monthData[38] = DiscordantcStr;
                            }

                            String MaleStartedARTStr = "";
                            if (maleStartedART[0] == null) {
                                MaleStartedARTStr = String.valueOf(ClientDao.getAllMaleStartedARTinANCContact(monthData[0]));
                                maleStartedART[0] = MaleStartedARTStr;
                                monthData[39] = MaleStartedARTStr;
                            } else {
                                MaleStartedARTStr = maleStartedART[0];
                                monthData[39] = MaleStartedARTStr;
                            }

                            String MaleAlreadyPositiveStr = "";
                            if (maleAlreadyPositive[0] == null) {
                                MaleAlreadyPositiveStr = String.valueOf(ClientDao.getAllMaleAlreadyPositiveContact(monthData[0]));
                                maleAlreadyPositive[0] = MaleAlreadyPositiveStr;
                                monthData[40] = MaleAlreadyPositiveStr;
                            } else {
                                MaleAlreadyPositiveStr = maleAlreadyPositive[0];
                                monthData[40] = MaleAlreadyPositiveStr;
                            }

                            String MalePositiveStr = "";
                            if (malePositive[0] == null) {
                                MalePositiveStr = String.valueOf(ClientDao.getAllMalePositiveFirstContact(monthData[0]));
                                malePositive[0] = MalePositiveStr;
                                monthData[41] = MalePositiveStr;
                            } else {
                                MalePositiveStr = malePositive[0];
                                monthData[41] = MalePositiveStr;
                            }

                            String MaleTestedStr = "";
                            if (maleTested[0] == null) {
                                MaleTestedStr = String.valueOf(ClientDao.getAllMaleTestFirstContact(monthData[0]));
                                maleTested[0] = MaleTestedStr;
                                monthData[42] = MaleTestedStr;
                            } else {
                                MaleTestedStr = maleTested[0];
                                monthData[42] = MaleTestedStr;
                            }

                            String ViralLoadStr = "";
                            if (viralLoad[0] == null) {
                                ViralLoadStr = String.valueOf(ClientDao.getAllViralLoadResultsContact(monthData[0]));
                                viralLoad[0] = ViralLoadStr;
                                monthData[43] = ViralLoadStr;
                            } else {
                                ViralLoadStr = viralLoad[0];
                                monthData[43] = ViralLoadStr;
                            }

                            String SuppressedVLStr = "";
                            if (suppressedVL[0] == null) {
                                SuppressedVLStr = String.valueOf(ClientDao.getAllSuppressedViralLoadResultsContact(monthData[0]));
                                suppressedVL[0] = SuppressedVLStr;
                                monthData[44] = SuppressedVLStr;
                                if (monthData[44] == null) {
                                    monthData[44] = "0";
                                }
                            } else {
                                SuppressedVLStr = suppressedVL[0];
                                monthData[44] = SuppressedVLStr;
                                if (monthData[44] == null) {
                                    monthData[44] = "0";
                                }
                            }

                            String OnARTStr = "";
                            if (onART[0] == null) {
                                OnARTStr = String.valueOf(ClientDao.getAllOnARTContact(monthData[0]));
                                onART[0] = OnARTStr;
                                monthData[45] = OnARTStr;
                                if (monthData[45] == null) {
                                    monthData[45] = "0";
                                }
                            } else {
                                OnARTStr = onART[0];
                                monthData[45] = OnARTStr;
                                if (monthData[45] == null) {
                                    monthData[45] = "0";
                                }
                            }

                            String TestedPositiveStr = "";
                            if (testedPositive[0] == null) {
                                TestedPositiveStr = String.valueOf(ClientDao.getAllTestedPositiveFirstContact(monthData[0]));
                                testedPositive[0] = TestedPositiveStr;
                                monthData[46] = TestedPositiveStr;
                            } else {
                                TestedPositiveStr = testedPositive[0];
                                monthData[46] = TestedPositiveStr;
                            }

                            String AlreadyPositiveStr = "";
                            if (alreadyPositive[0] == null) {
                                AlreadyPositiveStr = String.valueOf(ClientDao.getAllAlreadyPositiveFirstContact(monthData[0]));
                                alreadyPositive[0] = AlreadyPositiveStr;
                                monthData[47] = AlreadyPositiveStr;
                            } else {
                                AlreadyPositiveStr = alreadyPositive[0];
                                monthData[47] = AlreadyPositiveStr;
                            }

                            String TestedHIVStr = "";
                            if (testedHIV[0] == null) {
                                TestedHIVStr = String.valueOf(ClientDao.getAllTestedHIVFirstContact(monthData[0]));
                                testedHIV[0] = TestedHIVStr;
                                monthData[48] = TestedHIVStr;
                            } else {
                                TestedHIVStr = alreadyPositive[0];
                                monthData[48] = TestedHIVStr;
                            }

                            String ScreenedTBStr = "";
                            if (screenedTB[0] == null) {
                                ScreenedTBStr = String.valueOf(ClientDao.getAllScreenedForTBContact(monthData[0]));
                                screenedTB[0] = ScreenedTBStr;
                                monthData[49] = ScreenedTBStr;
                            } else {
                                ScreenedTBStr = screenedTB[0];
                                monthData[49] = ScreenedTBStr;
                            }

                            String TTCVPlusTwoStr = "";
                            if (ttcvPlusTwo[0] == null) {
                                TTCVPlusTwoStr = String.valueOf(ClientDao.getAllTTCVPlusTwoContact(monthData[0]));
                                ttcvPlusTwo[0] = TTCVPlusTwoStr;
                                monthData[50] = TTCVPlusTwoStr;
                            } else {
                                TTCVPlusTwoStr = ttcvPlusTwo[0];
                                monthData[50] = TTCVPlusTwoStr;
                            }

                            String ReferredTBStr = "";
                            if (referredTB[0] == null) {
                                ReferredTBStr = String.valueOf(ClientDao.getAllReferredTBContact(monthData[0]));
                                referredTB[0] = ReferredTBStr;
                                monthData[51] = ReferredTBStr;
                            } else {
                                ReferredTBStr = referredTB[0];
                                monthData[51] = ReferredTBStr;
                            }

                            String ContactCountStr = "";
                            if (contactCount[0] == null) {
                                ContactCountStr = String.valueOf(ClientDao.getAllContactCount(monthData[0]));
                                contactCount[0] = ContactCountStr;
                                monthData[52] = ContactCountStr;
                            } else {
                                ContactCountStr = contactCount[0];
                                monthData[52] = ContactCountStr;
                            }

                            if (monthData[0].contains("1")) {
                                JanuaryData = monthData;
                            } else if (monthData[0].contains("2")) {
                                FebruaryData = monthData;
                            } else if (monthData[0].contains("3")) {
                                MarchData = monthData;
                            } else if (monthData[0].contains("4")) {
                                AprilData = monthData;
                            } else if (monthData[0].contains("5")) {
                                MayData = monthData;
                            } else if (monthData[0].contains("6")) {
                                JuneData = monthData;
                            } else if (monthData[0].contains("7")) {
                                JulyData = monthData;
                            } else if (monthData[0].contains("8")) {
                                AugustData = monthData;
                            } else if (monthData[0].contains("9")) {
                                SeptemberData = monthData;
                            } else if (monthData[0].contains("10")) {
                                OctoberData = monthData;
                            } else if (monthData[0].contains("11")) {
                                NovemberData = monthData;
                            } else if (monthData[0].contains("12")) {
                                DecemberData = monthData;
                            }


                            return results;
                        }

                        @Override
                        protected void onPostExecute(List<String> results) {

                            String[] TotalData = new String[53];
                            if (results.get(0).contains("1")) {
                                TotalData = JanuaryData;
                            } else if (results.get(0).contains("2")) {
                                TotalData = FebruaryData;
                            } else if (results.get(0).contains("3")) {
                                TotalData = MarchData;
                            } else if (results.get(0).contains("4")) {
                                TotalData = AprilData;
                            } else if (results.get(0).contains("5")) {
                                TotalData = MayData;
                            } else if (results.get(0).contains("6")) {
                                TotalData = JuneData;
                            } else if (results.get(0).contains("7")) {
                                TotalData = JulyData;
                            } else if (results.get(0).contains("8")) {
                                TotalData = AugustData;
                            } else if (results.get(0).contains("9")) {
                                TotalData = SeptemberData;
                            } else if (results.get(0).contains("10")) {
                                TotalData = OctoberData;
                            } else if (results.get(0).contains("11")) {
                                TotalData = NovemberData;
                            } else if (results.get(0).contains("12")) {
                                TotalData = DecemberData;
                            }
                            if (isMonthDataComplete(TotalData) && Integer.parseInt(TotalData[0]) < 13) {
                                String FILENAME = TotalData[0] + "_" + "monthData.txt";
                                String filePath = Utils.getAppPath(context) + FILENAME;
                                File file = new File(filePath);
                                if (!file.exists()) {
                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                                        System.out.println("Attempting to save data to file");
                                        if (TotalData != null) {
                                            for (String data : TotalData) {
                                                writer.write(data);
                                                writer.newLine();
                                                System.out.println("Writing data to file");
                                            }
                                        }
                                        System.out.println("Data saved successfully for month: " + TotalData[0]);
                                        Toast.makeText(context, "Data saved successfully for month: " + TotalData[0], Toast.LENGTH_SHORT).show();
                                        if(TotalData[0].contains("12")){
                                            hia2ReportingImage = MeFragment.changedView.findViewById(R.id.hia2_reportingImageView);
                                            hia2ReportingText = MeFragment.changedView.findViewById(R.id.hia2_reporting_text);
                                            reportProgress = MeFragment.changedView.findViewById(R.id.reportCircle);

                                            hia2ReportingText.setText("HIA2 Reporting");
                                            reportProgress.setVisibility(View.GONE);
                                            hia2ReportingImage.setVisibility(View.VISIBLE);
                                            hia2ReportingText.setTextColor(Color.BLACK);
                                            hia2ReportingImage.setImageResource(R.drawable.ic_view_history);

                                        }

                                    } catch (IOException e) {
                                        System.out.println("Failed to save data to file for month: " + TotalData[0]);
                                    }
                                } else {
                                    System.out.println("Unable to save incomplete data for month: " + TotalData[0]);
                                }
                            }

                            //int womenSeen = Integer.parseInt(results.get(0)) + Integer.parseInt(results.get(1)) + Integer.parseInt(results.get(2)) + Integer.parseInt(results.get(3));
                        }
                    }.execute();

                    if (adjustedMonth == 12) {
                        String timestamp = getCurrentTimestamp();
                        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("timestamp", timestamp);
                        editor.apply();
                        //MeFragment.updateView(context);

                        //MeFragment.restartActivity();
                        /*if(context instanceof MeFr){

                        }*/
                        break; // Exit the loop when it reaches the 12th month

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        return timestamp;
    }


    static void reloadQueries(){

        boolean deletionCompleted = false;

        for (int month = 1; month <= 12; month++) {
            String FILENAME = month + "_monthData.txt";
            String filePath = Utils.getAppPath(context) + FILENAME;
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("File deleted successfully for month: " + month);
                } else {
                    System.out.println("Failed to delete file for month: " + month);
                }
            } else {
                System.out.println("File does not exist for month: " + month);
            }
        }

        deletionCompleted = true;

        if (deletionCompleted) {
            System.out.println("Deletion completed");

            loadReports();

            System.out.println("Reports reloading");
        }

    }


    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            if (isMeItemEnabled()) {
                bottomNavigationView.getMenu()
                        .add(Menu.NONE, org.smartregister.R.string.action_me, Menu.NONE, org.smartregister.R.string.me).setIcon(
                                bottomNavigationHelper
                                        .writeOnDrawable(org.smartregister.R.drawable.bottom_bar_initials_background, userInitials,
                                                getResources()));
            }

            bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

            if (!isLibraryItemEnabled()) {
                bottomNavigationView.getMenu().removeItem(R.id.action_library);
            }

            if (!isAdvancedSearchEnabled()) {
                bottomNavigationView.getMenu().removeItem(R.id.action_search);
            }

            BottomNavigationListener bottomNavigationListener = new BottomNavigationListener(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = findFragmentByPosition(currentPage);
        if (fragment instanceof AdvancedSearchFragment) {
            ((AdvancedSearchFragment) fragment).onBackPressed();
            return;
        } else if (fragment instanceof BaseRegisterFragment) {
            setSelectedBottomBarMenuItem(org.smartregister.R.id.action_clients);
            BaseRegisterFragment registerFragment = (BaseRegisterFragment) fragment;
            if (registerFragment.onBackPressed()) {
                return;
            }
        }
        if (currentPage == 0) {
            super.onBackPressed();
        } else {
            switchToBaseFragment();
            setSelectedBottomBarMenuItem(org.smartregister.R.id.action_clients);
        }
    }

    @Override
    protected void initializePresenter() {
        presenter = new RegisterPresenter(this);
    }

    @Override
    public BaseRegisterFragment getRegisterFragment() {
        return new HomeRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        int posCounter = 0;
        if (isAdvancedSearchEnabled()) {
            BaseRegisterActivity.ADVANCED_SEARCH_POSITION = ++posCounter;
        }

        BaseRegisterActivity.SORT_FILTER_POSITION = ++posCounter;

        if (isMeItemEnabled()) {
            BaseRegisterActivity.ME_POSITION = ++posCounter;
        }

        if (isLibraryItemEnabled()) {
            BaseRegisterActivity.LIBRARY_POSITION = ++posCounter;
        }

        Fragment[] fragments = new Fragment[posCounter];

        if (isAdvancedSearchEnabled()) {
            fragments[BaseRegisterActivity.ADVANCED_SEARCH_POSITION - 1] = new AdvancedSearchFragment();
        }

        fragments[BaseRegisterActivity.SORT_FILTER_POSITION - 1] = new SortFilterFragment();

        if (isMeItemEnabled()) {
            fragments[BaseRegisterActivity.ME_POSITION - 1] = new MeFragment();
        }

        if (isLibraryItemEnabled()) {
            fragments[BaseRegisterActivity.LIBRARY_POSITION - 1] = new LibraryFragment();
        }

        return fragments;
    }

    @Override
    public void startFormActivity(String s, String s1, Map<String, String> map) {
        //Todo implementing an abstract class
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {

        String smNumber = entityId + "9";
        try {
            if (mBaseFragment instanceof HomeRegisterFragment) {
                String locationId = AncLibrary.getInstance().getContext().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
                ((RegisterPresenter) presenter).startForm(formName, entityId, metaData, locationId);
            }
        } catch (Exception e) {
            Timber.e(e, "%s --> startFormActivity()", this.getClass().getCanonicalName());
            displayToast(getString(R.string.error_unable_to_start_form));
        }
    }

    @Override
    public void startFormActivity(JSONObject form) {
        Intent intent = new Intent(this, AncRegistrationActivity.class);
        intent.putExtra(ConstantsUtils.JsonFormExtraUtils.JSON, form.toString());
        intent.putExtra(JsonFormConstants.PERFORM_FORM_TRANSLATION, true);
        startActivityForResult(intent, ANCJsonFormUtils.REQUEST_CODE_GET_JSON);
    }

        public void saveKafkaRegistration(JSONObject data){
            String jsonString = data.toString();
        ((RegisterContract.Presenter) presenter).saveRegistrationForm(jsonString, false);
    }

    private JSONObject remoteRegister() {
        new FetchDataTask().execute();
        return null;
    }

    private class FetchDataTask extends AsyncTask<Void, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(Void... voids) {
            try {
                String urlString = "https://kafka.namai.bluecodeltd.com/getRegistrations";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");

                String userCredentials = "admin:nNhwi773#mai";
                String basicAuth = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.getBytes());
                }
                conn.setRequestProperty("Authorization", basicAuth);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                conn.disconnect();

                JSONArray jsonArray = new JSONArray(content.toString());
                JSONArray transformedArray = new JSONArray();

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject clientData = jsonArray.getJSONObject(i);

                    String locationString = clientData.optString("facilityname", "");

                    //if(locationString == locationId){
                        JSONObject transformedData = transformKafkaData(clientData.toString());
                        transformedArray.put(transformedData);
                        return transformedArray;
                    //}

                    /*JSONObject transformedData = transformKafkaData(clientData.toString());
                    transformedArray.put(transformedData);*/
                }
                /*return transformedArray;*/

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            if (result != null) {
                try {
                    Timber.d("Remote Data JSON Array: %s", result.toString(2));

                    for (int i = 0; i < result.length(); i++) {
                        JSONObject remoteData = result.getJSONObject(i);
                        ((RegisterContract.Presenter) presenter).saveRegistrationForm(remoteData.toString(), false);
                    }
                } catch (JSONException e) {
                    Timber.e(e, "Error parsing remote JSON array.");
                }
            } else {
                Timber.e("Error pulling the data.");
            }
        }
    }




        private JSONObject transformKafkaData(String kafkaJson) {
        try {
            if (StringUtils.isBlank(locationId)) {
                locationId = AncLibrary.getInstance().getContext().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
                Triple<String, String, String> triple = Triple.of("anc_register", "", locationId);
                interactor.getNextUniqueId(triple, (RegisterContract.InteractorCallBack) this);
            }

            JSONObject kafkaData = new JSONObject(kafkaJson);

            String randomStudyId = generateRandomStudyId();

            String randomANCId = generateRandomANCId();

            String formattedDob = "01-01-2000";

            JSONObject step1 = new JSONObject();
            step1.put("title", "ANC Registration");

            JSONArray fields = new JSONArray();

            String dobString = kafkaData.optString("dateofbirth", "");

            DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
            Date dobDate = dateFormat.parse(dobString);

            Calendar dobCal = Calendar.getInstance();
            dobCal.setTime(dobDate);
            Calendar todayCal = Calendar.getInstance();
            int age = todayCal.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (todayCal.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            try {

                DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
                formattedDob = targetFormat.format(dobDate);
                System.out.println("Formatted DOB: " + formattedDob);

            } catch (Exception e) {
                e.printStackTrace();
            }


            fields.put(new JSONObject().put("key", "wom_image").put("type", "choose_image").put("uploadButtonText", "Take a picture of the woman"));
            fields.put(new JSONObject().put("key", "anc_id").put("openmrs_entity_parent", "").put("openmrs_entity", "person_identifier").put("openmrs_entity_id", "ANC_ID").put("type", "edit_text").put("read_only", "false").put("value", code + "/" + randomANCId/* + " SC+"*/));
            fields.put(new JSONObject().put("key", "study_id").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "study_id").put("type", "edit_text").put("read_only", "false").put("hint", "Study ID").put("value", kafkaData.optString("hmiscode", "")));
            fields.put(new JSONObject().put("key", "first_name").put("openmrs_entity_parent", "").put("openmrs_entity", "person").put("openmrs_entity_id", "first_name").put("type", "edit_text").put("hint", "First name").put("edit_type", "name").put("v_regex", new JSONObject().put("value", "[A-Za-z\\s\\.\\-]*").put("err", "Please enter a valid name")).put("v_required", new JSONObject().put("value", true).put("err", "Enter a First name")).put("value", kafkaData.optString("firstname", "")));
            fields.put(new JSONObject().put("key", "maiden_name").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "maiden_name").put("type", "edit_text").put("hint", "Maiden Name").put("edit_type", "name").put("v_regex", new JSONObject().put("value", "[A-Za-z\\s\\.\\-]*").put("err", "Please enter a valid name")).put("value", ""));
            fields.put(new JSONObject().put("key", "last_name").put("openmrs_entity_parent", "").put("openmrs_entity", "person").put("openmrs_entity_id", "last_name").put("type", "edit_text").put("hint", "Last name").put("edit_type", "name").put("v_regex", new JSONObject().put("value", "[A-Za-z\\s\\.\\-]*").put("err", "Please enter a valid name")).put("v_required", new JSONObject().put("value", true).put("err", "Enter a Last name")).put("value", kafkaData.optString("surname", "")));
            fields.put(new JSONObject().put("key", "nrc").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "nrc").put("type", "edit_text").put("hint", "NRC number").put("v_regex", new JSONObject().put("value", "Blank").put("err", "Number must take the Correct format")).put("value", ""));
            fields.put(new JSONObject().put("key", "dob").put("openmrs_entity_parent", "").put("openmrs_entity", "person").put("openmrs_entity_id", "birthdate").put("type", "hidden").put("value", formattedDob).put("calculation", new JSONObject().put("rules-engine", new JSONObject().put("ex-rules", new JSONObject().put("rules-file", "registration_calculation_rules.yml")))));
            fields.put(new JSONObject().put("key", "dob_calculated").put("openmrs_entity_parent", "").put("openmrs_entity", "").put("openmrs_entity_id", "").put("type", "hidden").put("value", "0").put("calculation", new JSONObject().put("rules-engine", new JSONObject().put("ex-rules", new JSONObject().put("rules-file", "registration_calculation_rules.yml")))).put("step", "step1").put("is-rule-check", true));
            fields.put(new JSONObject().put("key", "dob_entered").put("openmrs_entity_parent", "").put("openmrs_entity", "").put("openmrs_entity_id", "").put("type", "date_picker").put("hint", "Date of birth (DOB)").put("expanded", false).put("duration", new JSONObject().put("label", "Age")).put("min_date", "today-100y").put("max_date", "today-10y").put("relevance", new JSONObject().put("step1:dob_unknown", new JSONObject().put("ex-checkbox", new JSONArray().put(new JSONObject().put("not", new JSONArray().put("dob_unknown")))))).put("v_required", new JSONObject().put("value", true).put("err", "Specify Age")).put("is_visible", true).put("step", "step1").put("is-rule-check", true).put("value", kafkaData.optString("dateofbirth", "")));
            fields.put(new JSONObject().put("key", "dob_unknown").put("openmrs_entity_parent", "").put("openmrs_entity", "person").put("openmrs_entity_id", "birthdate_estimated").put("type", "check_box").put("options", new JSONArray().put(new JSONObject().put("key", "dob_unknown").put("text", "DOB unknown?").put("text_size", "18px").put("value", "false").put("openmrs_entity_parent", "").put("openmrs_entity", "").put("openmrs_entity_id", ""))).put("is-rule-check", false));
            fields.put(new JSONObject().put("key", "age").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "age").put("type", "hidden").put("value", age).put("calculation", new JSONObject().put("rules-engine", new JSONObject().put("ex-rules", new JSONObject().put("rules-file", "registration_calculation_rules.yml")))));
            fields.put(new JSONObject().put("key", "age_calculated").put("openmrs_entity_parent", "").put("openmrs_entity", "").put("openmrs_entity_id", "").put("type", "hidden").put("value", age).put("calculation", new JSONObject().put("rules-engine", new JSONObject().put("ex-rules", new JSONObject().put("rules-file", "registration_calculation_rules.yml")))));
            fields.put(new JSONObject().put("key", "age_entered").put("openmrs_entity_parent", "").put("openmrs_entity", "person").put("openmrs_entity_id", "age").put("type", "edit_text").put("value", age).put("hint", "Age").put("v_numeric", new JSONObject().put("value", "true").put("err", "Age must be a number")).put("v_min", new JSONObject().put("value", "10").put("err", "Age must be equal to or greater than 10")).put("v_max", new JSONObject().put("value", "49").put("err", "Age must be equal or less than 49")).put("relevance", new JSONObject().put("step1:dob_unknown", new JSONObject().put("ex-checkbox", new JSONArray().put(new JSONObject().put("and", new JSONArray().put("dob_unknown")))))).put("v_required", new JSONObject().put("value", true).put("err", "Enter the Woman Age")).put("is_visible", false).put("step", "step1").put("is-rule-check", true));
            fields.put(new JSONObject().put("key", "gender").put("openmrs_entity_parent", "").put("openmrs_entity", "person").put("openmrs_entity_id", "gender").put("type", "hidden").put("value", "F"));
            fields.put(new JSONObject().put("key", "educ_level").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "educ_level").put("type", "native_radio").put("label", "Level of education").put("options", new JSONArray().put(new JSONObject().put("key", "none").put("openmrs_entity_parent", "").put("openmrs_entity", "concept").put("openmrs_entity_id", "none").put("text", "None")).put(new JSONObject().put("key", "primary").put("text", "Primary").put("openmrs_entity_parent", "").put("openmrs_entity", "concept").put("openmrs_entity_id", "primary")).put(new JSONObject().put("key", "secondary").put("text", "Secondary").put("openmrs_entity_parent", "").put("openmrs_entity", "concept").put("openmrs_entity_id", "secondary")).put(new JSONObject().put("key", "tertiary").put("text", "Tertiary").put("openmrs_entity_parent", "").put("openmrs_entity", "concept").put("openmrs_entity_id", "tertiary")).put(new JSONObject().put("key", "adult").put("text", "Adult literacy").put("openmrs_entity_parent", "").put("openmrs_entity", "concept").put("openmrs_entity_id", "adult"))).put("v_required", new JSONObject().put("value", true).put("err", "Select a level of education")).put("value", kafkaData.optString("educationallevel", "").toLowerCase()));
            fields.put(new JSONObject().put("key", "religion").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "religion").put("type", "native_radio").put("label", "Religion").put("options", new JSONArray().put(new JSONObject().put("key", "christianity").put("text", "Christianity").put("openmrs_entity_parent", "").put("openmrs_entity", "concept").put("openmrs_entity_id", "christianity")).put(new JSONObject().put("key", "muslim").put("text", "Muslim").put("openmrs_entity_parent", "").put("openmrs_entity", "concept").put("openmrs_entity_id", "muslim")).put(new JSONObject().put("key", "other").put("text", "Other").put("openmrs_entity_parent", "").put("openmrs_entity", "concept").put("openmrs_entity_id", "other"))).put("v_required", new JSONObject().put("value", true).put("err", "Select Religion")).put("value", ""));
            fields.put(new JSONObject().put("key", "phone_number").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "phone_number").put("type", "edit_text").put("hint", "Phone Number").put("v_numeric", new JSONObject().put("value", "true").put("err", "Phone Number must be numeric")).put("v_required", new JSONObject().put("value", true).put("err", "Enter the Phone Number")).put("value", kafkaData.optString("mobilephonenumber", "")));
            fields.put(new JSONObject().put("key", "phone_number_alternative").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "phone_number_alt").put("type", "edit_text").put("hint", "Phone Number (alternative)").put("v_numeric", new JSONObject().put("value", "true").put("err", "Phone Number must be numeric")).put("value", "2"));
            fields.put(new JSONObject().put("key", "patient_identifier").put("openmrs_entity_parent", "").put("openmrs_entity", "person_identifier").put("openmrs_entity_id", "zeir_id").put("type", "hidden").put("value", "ZEIR ID"));
            fields.put(new JSONObject().put("key", "mother_guardian_number").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "mother_guardian_number").put("type", "hidden").put("value", ""));
            fields.put(new JSONObject().put("key", "same_as_primary_phone_number").put("openmrs_entity_parent", "").put("openmrs_entity", "").put("openmrs_entity_id", "").put("type", "check_box").put("label", "Same as primary phone number").put("options", new JSONArray().put(new JSONObject().put("key", "same_as_primary_phone_number").put("text", "").put("openmrs_entity_parent", "").put("openmrs_entity", "").put("openmrs_entity_id", "")).put(new JSONObject().put("key", "v_required").put("value", true).put("err", "Select if the same as primary phone number"))));
            fields.put(new JSONObject().put("key", "relationship_to_child").put("openmrs_entity_parent", "").put("openmrs_entity", "relationship").put("openmrs_entity_id", "relationship").put("type", "hidden").put("value", "Mother").put("v_required", new JSONObject().put("value", true).put("err", "Select the Relationship to child")).put("is-rule-check", true));
            fields.put(new JSONObject().put("key", "death_date").put("openmrs_entity_parent", "").put("openmrs_entity", "person").put("openmrs_entity_id", "death_date").put("type", "hidden").put("value", "0").put("v_required", new JSONObject().put("value", true).put("err", "Specify death date")).put("is-rule-check", true));
            fields.put(new JSONObject().put("key", "death_date_unknown").put("openmrs_entity_parent", "").put("openmrs_entity", "").put("openmrs_entity_id", "").put("type", "check_box").put("options", new JSONArray().put(new JSONObject().put("key", "death_date_unknown").put("text", "Death date unknown?").put("openmrs_entity_parent", "").put("openmrs_entity", "").put("openmrs_entity_id", "")).put(new JSONObject().put("key", "v_required").put("value", true).put("err", "Specify if death date is unknown"))));
            fields.put(new JSONObject().put("key", "hiv_status").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "hiv_status").put("type", "hidden").put("value", ""));
            fields.put(new JSONObject().put("key", "next_contact").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "next_contact").put("type", "date_picker").put("hint", "Next Contact").put("value", ""));
            fields.put(new JSONObject().put("key", "next_contact_date").put("openmrs_entity_parent", "").put("openmrs_entity", "person_attribute").put("openmrs_entity_id", "next_contact_date").put("type", "date_picker").put("hint", "Next Contact Date").put("value", ""));
            fields.put(new JSONObject().put("key", "cohabitants").put("openmrs_entity_parent", "").put("openmrs_entity", "").put("openmrs_entity_id", "").put("type", "check_box").put("label", "cohabitants").put("options", new JSONArray().put(new JSONObject().put("key", "cohabitants").put("text", "").put("openmrs_entity_parent", "").put("openmrs_entity", "").put("openmrs_entity_id", "")).put(new JSONObject().put("key", "v_required").put("value", true).put("err", "cohabitants"))));
            fields.put(new JSONObject().put("key", "sc_id").put("openmrs_entity_parent", "").put("openmrs_entity", "person_identifier").put("openmrs_entity_id", "sc_id").put("type", "edit_text").put("hint", "SmartCare Plus ID").put("value", kafkaData.optString("clientuuid", "")));


            step1.put("fields", fields);

            JSONObject metadata = new JSONObject();
            metadata.put("start", new JSONObject().put("value", getCurrentTimestamp()));
            metadata.put("end", new JSONObject().put("value", getCurrentTimestamp()));
            metadata.put("today", new JSONObject().put("value", getCurrentDate()));
            metadata.put("encounter_location", locationId);

            JSONObject mainData = new JSONObject();
            mainData.put("count", "1");
            mainData.put("encounter_type", "ANC Registration");
            mainData.put("entity_id", kafkaData.optString("clientuuid", ""));
            mainData.put("metadata", metadata);
            mainData.put("step1", step1);

            return mainData;
        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultData();
        }
    }

    private String generateRandomStudyId() {
        Random random = new Random();
        return String.valueOf(1000000000 + random.nextInt(900000000));
    }

    private String generateRandomANCId() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    /*private String getUniqueIdForANC() {
        String locationId = AncLibrary.getInstance().getContext().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
        //JSONObject metadata = jsonString.getJSONObject(ANCJsonFormUtils.METADATA);
        Triple<String, String, String> triple = Triple.of("anc_registration", "", locationId);
        String uniqueId = interactor.getNextUniqueId(triple, (RegisterContract.InteractorCallBack) this);
        return uniqueId;
    }*/



    private JSONObject createDefaultData() {
        JSONObject defaultData = new JSONObject();

        try {
            defaultData.put("count", "1");
            defaultData.put("encounter_type", "ANC Registration");
            defaultData.put("entity_id", "");
            defaultData.put("metadata", new JSONObject());
            defaultData.put("step1", new JSONObject().put("fields", new JSONArray()));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return defaultData;
    }



    private String mapEducationLevel(String level) {
        switch(level.toLowerCase()) {
            case "certificate graduate": return "tertiary";
            case "junior secondary": return "secondary";
            case "senior secondary": return "secondary";
            case "none": return "none";
            default: return "none";
        }
    }

    private String mapOccupation(String occupation, String educationLevel) {
        if (educationLevel.equalsIgnoreCase("none")) {
            switch(occupation.toLowerCase()) {
                case "unemployed": return "unemployed_redacted";
                case "formal employment": return "formal_employment_redacted";
                case "informal employment": return "informal_employment_other_redacted";
                default: return "unemployed_redacted";
            }
        } else {
            switch(occupation.toLowerCase()) {
                case "unemployed": return "unemployed";
                case "teacher": return "formal_employment";
                case "other": return "informal_employment_other";
                default: return "unemployed";
            }
        }
    }


    private String getCurrentDate() {
        return new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AllConstants.BARCODE.BARCODE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Barcode barcode = data.getParcelableExtra(AllConstants.BARCODE.BARCODE_KEY);
                if (barcode != null) {
                    Timber.d(barcode.displayValue);

                    Fragment fragment = findFragmentByPosition(currentPage);
                    if (fragment instanceof AdvancedSearchFragment) {
                        advancedSearchQrText = barcode.displayValue;
                    } else {
                        mBaseFragment.onQRCodeSucessfullyScanned(barcode.displayValue);
                        mBaseFragment.setSearchTerm(barcode.displayValue);
                    }
                }
            } else {
                Timber.i("NO RESULT FOR QR CODE");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {
        if (requestCode == ANCJsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == Activity.RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(ConstantsUtils.JsonFormExtraUtils.JSON);
                Timber.d(jsonString);
                if (StringUtils.isNotBlank(jsonString)) {
                    JSONObject form = new JSONObject(jsonString);
                    switch (form.getString(ANCJsonFormUtils.ENCOUNTER_TYPE)) {
                        case ConstantsUtils.EventTypeUtils.REGISTRATION:

                            ((RegisterContract.Presenter) presenter).saveRegistrationForm(jsonString, false);


                            String tag_string_req = "req_login";
                            String url = "https://textit.com/api/v2/contacts.json";

                            JSONObject jsonBody = new JSONObject();
                            try {
                                JSONObject fname = getFieldJSONObject(form.getJSONObject("step1").getJSONArray("fields"),"first_name");
                                JSONObject lname = getFieldJSONObject(form.getJSONObject("step1").getJSONArray("fields"),"last_name");
                                JSONObject pnum = getFieldJSONObject(form.getJSONObject("step1").getJSONArray("fields"),"phone_number");

                                String firstName = fname.getString(ANCJsonFormUtils.VALUE);
                                String lastName = lname.getString(ANCJsonFormUtils.VALUE);
                                String phoneNumber = "+260" + pnum.getString(ANCJsonFormUtils.VALUE);

                                jsonBody.put("uuid", firstName + " " + lastName);


                                jsonBody.put("name", firstName + " " + lastName);
                                jsonBody.put("language", "eng");
                                JSONArray urnsArray = new JSONArray();
                                urnsArray.put("tel:" + phoneNumber);
                                jsonBody.put("urns", urnsArray);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                    Request.Method.POST,
                                    url,
                                    jsonBody,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Log.e("TextItAPIResponse", "Response Received: " + response.toString());
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            if (error.networkResponse != null) {
                                                Log.e("TextItAPIError", "Error response code: " + error.networkResponse.statusCode);
                                            }
                                        }
                                    }
                            ) {
                                @Override
                                public byte[] getBody() {
                                    return jsonBody.toString().getBytes();
                                }

                                @Override
                                public String getBodyContentType() {
                                    return "application/json";
                                }

                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    String apiKey = BuildConfig.TEXTIT_API_KEY;
                                    HashMap<String, String> headers = new HashMap<String, String>();
                                    if(apiKey != null) {
                                        headers.put("Authorization", apiKey);
                                    }
                                    return headers;
                                }
                            };

                            addToJSONRequestQueue(jsonObjectRequest, tag_string_req);
                            break;
                        case ConstantsUtils.EventTypeUtils.CLOSE:
                            ((RegisterContract.Presenter) presenter).closeAncRecord(jsonString);
                            break;
                        case ConstantsUtils.EventTypeUtils.QUICK_CHECK:
                            Contact contact = new Contact();
                            contact.setContactNumber(getIntent().getIntExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO, 0));
                            ANCFormUtils
                                    .persistPartial(getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID), contact);
                            PatientRepository
                                    .updateContactVisitStartDate(getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID),
                                            Utils.getDBDateToday());

                            Date currentTime = Calendar.getInstance().getTime();
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                Timber.e(e, "%s --> onActivityResultExtended()", this.getClass().getCanonicalName());
            }

        }
    }

    public static void addToJSONRequestQueue(Request<JSONObject> request, String tag) {
        Context context = AncLibrary.getInstance().getApplicationContext();
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        if (tag != null) {
            request.setTag(tag);
        }

        requestQueue.add(request);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        if (isAdvancedSearchEnabled()) {
            switchToAdvancedSearchFromBarcode();
        }
        if (isLibrary()) {
            switchToFragment(BaseRegisterActivity.LIBRARY_POSITION);
            setSelectedBottomBarMenuItem(org.smartregister.R.id.action_library);
        }

        //if (getFacilityID() == formatedLocation.getText().toString()) {}
    }

    @Override
    public List<String> getViewIdentifiers() {
        return Arrays.asList(ConstantsUtils.ConfigurationUtils.HOME_REGISTER);
    }

    @Override
    public void updateInitialsText(String initials) {
        this.userInitials = initials;
    }

    public void switchToBaseFragment() {
        switchToFragment(BaseRegisterActivity.BASE_REG_POSITION);
    }

    public void setSelectedBottomBarMenuItem(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);
    }

    /**
     * Forces the Home register activity to open the the Advanced search fragment after the barcode activity is closed (as
     * long as it was opened from the advanced search page)
     */
    private void switchToAdvancedSearchFromBarcode() {
        if (isAdvancedSearch) {
            switchToFragment(BaseRegisterActivity.ADVANCED_SEARCH_POSITION);
            setSelectedBottomBarMenuItem(org.smartregister.R.id.action_search);
            setAdvancedFragmentSearchTerm(advancedSearchQrText);
            setFormData(advancedSearchFormData);
            advancedSearchQrText = "";
            isAdvancedSearch = false;
            advancedSearchFormData = new HashMap<>();
        }
    }

    public boolean isLibrary() {
        return isLibrary;
    }

    private void setAdvancedFragmentSearchTerm(String searchTerm) {
        mBaseFragment.setUniqueID(searchTerm);
    }

    private void setFormData(HashMap<String, String> formData) {
        mBaseFragment.setAdvancedSearchFormData(formData);
    }

    public void setLibrary(boolean library) {
        isLibrary = library;
    }

    public boolean isMeItemEnabled() {
        return true;
    }

    public boolean isLibraryItemEnabled() {
        return true;
    }

    public boolean isAdvancedSearchEnabled() {
        return true;
    }

    @NonNull
    protected AlertDialog createAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.record_birth) + "?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(),
                (dialog, which) -> dialog.dismiss());
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.record_birth).toUpperCase(),
                (dialog, which) -> ANCJsonFormUtils.launchANCCloseForm(BaseHomeRegisterActivity.this));
        return alertDialog;
    }

    @NonNull
    protected void createAttentionFlagsAlertDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        attentionFlagDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_attention_flag, null);
        dialogBuilder.setView(attentionFlagDialogView);

        attentionFlagDialogView.findViewById(R.id.closeButton).setOnClickListener(view -> attentionFlagAlertDialog.dismiss());
        attentionFlagAlertDialog = dialogBuilder.create();
        setAttentionFlagAlertDialog(attentionFlagAlertDialog);
    }

    public void updateSortAndFilter(List<Field> filterList, Field sortField) {
        ((HomeRegisterFragment) mBaseFragment).updateSortAndFilter(filterList, sortField);
        switchToBaseFragment();
    }

    public void startAdvancedSearch() {
        if (isAdvancedSearchEnabled()) {
            try {
                mPager.setCurrentItem(BaseRegisterActivity.ADVANCED_SEARCH_POSITION, false);
            } catch (Exception e) {
                Timber.e(e, "%s --> startAdvancedSearch()", this.getClass().getCanonicalName());
            }
        }

    }

    @Override
    public void showLanguageDialog(final List<String> displayValues) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
                displayValues.toArray(new String[displayValues.size()])) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                ConfigurableViewsLibrary.getInstance();
                view.setTextColor(ConfigurableViewsLibrary.getContext().getColorResource(R.color.customAppThemeBlue));

                return view;
            }
        };

        AlertDialog languageDialog = createLanguageDialog(adapter, displayValues);
        languageDialog.show();
    }

    public AlertDialog createLanguageDialog(ArrayAdapter<String> adapter, final List<String> displayValues) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.select_language));
        builder.setSingleChoiceItems(adapter, 0, (dialog, which) -> {
            String selectedItem = displayValues.get(which);
            ((RegisterContract.Presenter) presenter).saveLanguage(selectedItem);
            dialog.dismiss();
        });

        return builder.create();
    }

    @Override
    public void showAttentionFlagsDialog(List<AttentionFlag> attentionFlags) {
        ViewGroup redFlagsContainer = attentionFlagDialogView.findViewById(R.id.red_flags_container);
        ViewGroup yellowFlagsContainer = attentionFlagDialogView.findViewById(R.id.yellow_flags_container);

        redFlagsContainer.removeAllViews();
        yellowFlagsContainer.removeAllViews();

        int yellowFlagCount = 0;
        int redFlagCount = 0;

        for (AttentionFlag flag : attentionFlags) {
            if (flag.isRedFlag()) {
                LinearLayout redRow = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.alert_dialog_attention_flag_row_red, redFlagsContainer, false);
                ((TextView) redRow.getChildAt(1)).setText(flag.getTitle());
                redFlagsContainer.addView(redRow);
                redFlagCount += 1;
            } else {

                LinearLayout yellowRow = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.alert_dialog_attention_flag_row_yellow, yellowFlagsContainer, false);
                ((TextView) yellowRow.getChildAt(1)).setText(flag.getTitle());
                yellowFlagsContainer.addView(yellowRow);
                yellowFlagCount += 1;
            }
        }

        ((View) redFlagsContainer.getParent()).setVisibility(redFlagCount > 0 ? View.VISIBLE : View.GONE);
        ((View) yellowFlagsContainer.getParent()).setVisibility(yellowFlagCount > 0 ? View.VISIBLE : View.GONE);

        getAttentionFlagAlertDialog().show();
    }

    public AlertDialog getAttentionFlagAlertDialog() {
        return attentionFlagAlertDialog;
    }

    public void setAttentionFlagAlertDialog(AlertDialog attentionFlagAlertDialog) {
        this.attentionFlagAlertDialog = attentionFlagAlertDialog;
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showProgressDialogHandler(ShowProgressDialogEvent showProgressDialogEvent) {
        if (showProgressDialogEvent != null) {
            showProgressDialog(R.string.saving_dialog_title);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void removePatientHandler(PatientRemovedEvent event) {
        if (event != null) {
            Utils.removeStickyEvent(event);
            refreshList(FetchStatus.fetched);
            hideProgressDialog();
        }
    }

    @Override
    public void startRegistration() {
        startFormActivity(ConstantsUtils.JsonFormUtils.ANC_REGISTER, null, "");
    }

    public void showRecordBirthPopUp(CommonPersonObjectClient client) {
        //This is required
        getIntent()
                .putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, client.getColumnmaps().get(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID));

        recordBirthAlertDialog.setMessage(String.format(this.getString(R.string.record_birth_popup_message),
                Utils.getGestationAgeFromEDDate(client.getColumnmaps().get(DBConstantsUtils.KeyUtils.EDD)),
                Utils.convertDateFormat(Utils.dobStringToDate(client.getColumnmaps().get(DBConstantsUtils.KeyUtils.EDD)),
                        dateFormatter), Utils.getDuration(client.getColumnmaps().get(DBConstantsUtils.KeyUtils.EDD)),
                client.getColumnmaps().get(DBConstantsUtils.KeyUtils.FIRST_NAME)));
        recordBirthAlertDialog.show();
    }

    public void setAdvancedSearch(boolean advancedSearch) {
        isAdvancedSearch = advancedSearch;
    }

    public void setAdvancedSearchFormData(HashMap<String, String> advancedSearchFormData) {
        this.advancedSearchFormData = advancedSearchFormData;
    }

    /*private void saveMonthDataToFile() throws IOException {
        if (isMonthDataComplete(monthData)) {
            String FILENAME = currentMonth + "_" + "monthData.txt";
            String filePath = Utils.getAppPath(context) + FILENAME;
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    System.out.println("Attempting to save data to file");
                    if (monthData != null) {
                        for (String data : monthData) {
                            writer.write(data);
                            writer.newLine();
                            System.out.println("Writing data to file");
                        }
                    }
                    System.out.println("Data saved successfully");
                } catch (IOException e) {
                    System.out.println("Failed to save data to file");
                }
            } else {
                System.out.println("Unable to save incomplete month data");
            }
        }

    }*/


    private static boolean isMonthDataComplete(String[] TotalData) {
        for (String data : TotalData) {
            if (data == null || data.isEmpty()) {
                System.out.println("Data collection is not complete");
                return false;  // Found an empty or null element
            }
        }
        System.out.println("Data collection has been completed");
        return true;  // All elements are filled with data
    }

    @Override
    public void onSyncStart() {

    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {

    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        Toast.makeText(context, "Attempting to save monthly reports data", Toast.LENGTH_SHORT).show();
        loadReports();
        JSONObject remoteData = remoteRegister();
        if (remoteData != null) {
            String remoteString = remoteData.toString();
            Timber.d("Remote Data JSON String: %s", remoteString);

            if (StringUtils.isNotBlank(remoteString)) {
                try {
                    JSONObject remoteJson = new JSONObject(remoteString);
                    Timber.d("Remote JSON Structure: %s", remoteJson.toString(2));

                    ((RegisterContract.Presenter) presenter).saveRegistrationForm(remoteJson.toString(), false);
                } catch (JSONException e) {
                    Timber.e(e, "Error parsing remote JSON string.");
                }
            } else {
                Timber.e("Remote data string is blank.");
            }
        } else {
            Timber.e("Remote data is null.");
        }
    }
}


