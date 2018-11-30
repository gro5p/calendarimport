package com.calendar.group5.calendarimport;


import com.google.android.gms.common.ConnectionResult;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.calendar.group5.calendarimport.SoarObject;

import javax.net.ssl.HttpsURLConnection;


public class googleActivity extends Activity {

    com.google.api.services.calendar.Calendar mService;

    GoogleAccountCredential credential;
    private TextView mStatusText;
    private TextView mResultsText;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };
    String username;
    String password;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);
        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mStatusText = new TextView(this);
        mStatusText.setLayoutParams(tlp);
        mStatusText.setTypeface(null, Typeface.BOLD);
        mStatusText.setText("Retrieving data...");
        activityLayout.addView(mStatusText);

        mResultsText = new TextView(this);
        mResultsText.setLayoutParams(tlp);
        mResultsText.setPadding(16, 16, 16, 16);
        mResultsText.setVerticalScrollBarEnabled(true);
        mResultsText.setMovementMethod(new ScrollingMovementMethod());
        activityLayout.addView(mResultsText);

        setContentView(activityLayout);


        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("CalendarImport")
                .build();

        Bundle bundle = getIntent().getExtras();
        username=bundle.getString("s_id");
        password = bundle.getString("pw");
        Log.d("user",username);

        
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            mStatusText.setText("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }
    }


    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        refreshResults();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    mStatusText.setText("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void refreshResults() {


        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new ApiAsyncTask(this,username,password).execute();
            } else {
                mStatusText.setText("No network connection available.");
            }
        }
    }


    public void clearResultsText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText("Retrieving data…");
                mResultsText.setText("");
            }
        });
    }


    public void updateResultsText(final List<String> dataStrings) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dataStrings == null) {
                    mStatusText.setText("Error retrieving data!");
                } else if (dataStrings.size() == 0) {
                    mStatusText.setText("No data found.");
                } else {
                    mStatusText.setText("Schedule to be added to Calendar");
                    mResultsText.setText(TextUtils.join("\n\n", dataStrings));
                }
            }
        });
    }


    public void updateStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText(message);
            }
        });
    }


    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }


    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }


    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        googleActivity.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

}

class ApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private googleActivity mActivity;
    private String user_name;
    private String pw;
    private List<String> namesList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> statusList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> unitsList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> gradingsList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> gradesList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> classNumbersList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> sectionsList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> componentsList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> timesList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> roomsList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> instructorsList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> startDateList = Stream.of(new String[]{}).collect(Collectors.toList());
    private List<String> endDatesList = Stream.of(new String[]{}).collect(Collectors.toList());
    private String Semester_ID = "4191";
    private static SimpleDateFormat inSDF = new SimpleDateFormat("MM/dd/yyyy");
    private static SimpleDateFormat outSDate = new SimpleDateFormat("yyyy-MM-dd");


    ApiAsyncTask(googleActivity activity, String user, String password) {
        this.mActivity = activity;
        this.user_name = user;
        this.pw = password;
    }


    @Override
    protected Void doInBackground(Void... params) {
        try {
            mActivity.clearResultsText();
            mActivity.updateResultsText(getDataFromApi());

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());

        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    googleActivity.REQUEST_AUTHORIZATION);

        } catch (IOException e) {
            mActivity.updateStatus("The following error occurred: " +
                    e.getMessage());
        }
        return null;
    }


    private List<String> getDataFromApi() throws IOException {

        List<String> eventStrings = new ArrayList<String>();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        ArrayList<SoarObject> arr = new ArrayList<>();
        try {

            String e_pass = URLEncoder.encode(this.pw, StandardCharsets.UTF_8.name());
            String send_this = ("userid=" + this.user_name + "&pwd=" + e_pass);
            URL login_URL = new URL("https://soar.usm.edu/psc/saprd90/EMPLOYEE/SA/c/NUI_FRAMEWORK.PT_LANDINGPAGE.GBL?&cmd=login&languageCd=ENG");

            HttpsURLConnection con = (HttpsURLConnection) login_URL.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");

            con.getOutputStream().write(send_this.getBytes("UTF-8"));
            con.getContent();


            CookieStore cookieStore = cookieManager.getCookieStore();
            //the cookies need to be passed in a particular Order
            //so I'm using a map to pass them in that order
            Map<String, String> cookieMap = (cookieStore.getCookies()).stream()
                    .filter(cookie -> cookie.getName().equals("PS_TOKEN") ||
                            cookie.getName().contains("18011") ||
                            cookie.getName().equals("BIGipServersoar")).collect(
                            Collectors.toMap(cookie -> cookie.getName(), cookie -> cookie.getValue()));

            String tricky_bastard = "";

            //the name of the second cookie often changes...
            //this is here to store it in a string
            for (String key : cookieMap.keySet()) {

                if (key.contains("18011")) {
                    tricky_bastard = key;
                }
            }


            String cookie_header = "PS_TOKEN=" + cookieMap.get("PS_TOKEN") + "; "
                    + tricky_bastard + "=" + cookieMap.get(tricky_bastard) + "; BIGipServersoar="
                    + cookieMap.get("BIGipServersoar");

            String Calendar_URL = "https://soar.usm.edu/psc/saprd90/EMPLOYEE/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_LIST.GBL?ACAD_CAREER=UGRD&INSTITUTION=USM01&STRM="
                    + Semester_ID + "&&";

            //Log.i(Tag,Calendar_URL);
            URL services_URL = new URL(Calendar_URL);

            //Log.d(TAG,cookie_header);


            con.disconnect();
            con = (HttpsURLConnection) services_URL.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Cookie", cookie_header);
            con.getContent();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));

            //BufferedWriter writer = new BufferedWriter(new FileWriter("output"));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("PAGROUPDIVIDER"))
                    namesList.add(inputLine.replace("</td></tr>", "").replaceAll(".*>", "").replace("&amp;", "and"));
                if (inputLine.contains("STATUS"))
                    statusList.add(inputLine.replace("</span>", "").replaceAll(".*>", ""));
                if (inputLine.contains("DERIVED_REGFRM1_UNT_TAKEN"))
                    unitsList.add(inputLine.replace("</span>", "").replaceAll(".*>", ""));
                if (inputLine.contains("GB_DESCR"))
                    gradingsList.add(inputLine.replace("</span>", "").replaceAll(".*>", ""));
                if (inputLine.contains("DERIVED_REGFRM1_CRSE_GRADE_OFF"))
                    gradesList.add(inputLine.replace("</span>", "").replaceAll(".*>", "").replace("&nbsp;", "Not Available"));
                if (inputLine.contains("DERIVED_CLS_DTL_CLASS_NBR"))
                    classNumbersList.add(inputLine.replace("</span>", "").replaceAll(".*>", ""));
                if (inputLine.contains("MTG_SECTION"))
                    sectionsList.add(inputLine.replace("</a></span>", "").replaceAll(".*>", ""));
                if (inputLine.contains("MTG_COMP"))
                    componentsList.add(inputLine.replace("</span>", "").replaceAll(".*>", ""));
                if (inputLine.contains("MTG_SCHED"))
                    timesList.add(inputLine.replace("</span>", "").replaceAll(".*>", ""));
                if (inputLine.contains("MTG_LOC"))
                    roomsList.add(inputLine.replace("</span>", "").replaceAll(".*>", ""));
                if (inputLine.contains("DERIVED_CLS_DTL_SSR_INSTR_LONG"))
                    instructorsList.add(inputLine.replace("</span>", "").replaceAll(".*>", ""));
                if (inputLine.contains("MTG_DATES")) {
                    String dates = inputLine.replace("</span>", "").replaceAll(".*>", "");
                    String[] parts = dates.split(" - ");
                    startDateList.add(parts[0]);
                    endDatesList.add(parts[1]);
                }
            }
            in.close();
            Log.d("tag", namesList.size()+"");
            for (int i = 0; i < namesList.size(); i++) {

                arr.add(new SoarObject(namesList.get(i), unitsList.get(i),
                        gradesList.get(i), classNumbersList.get(i), timesList.get(i),
                        roomsList.get(i), instructorsList.get(i), startDateList.get(i),
                        endDatesList.get(i)));


                eventStrings.add(String.format("%s\n, %s , %s\n, %s: %s-, %s\n, %s\n, %s-, %s\n,",
                        arr.get(i).name,arr.get(i).building, arr.get(i).room,arr.get(i).days,arr.get(i).startTime,
                        arr.get(i).endTime,arr.get(i).instructor,arr.get(i).startDate,arr.get(i).endDate));

            /*Log.d(TAG,"Course: " + namesList.get(i));
            Log.d(TAG,"Status: " + statusList.get(i));
            Log.d(TAG,"Units: " + unitsList.get(i));
            Log.d(TAG,"Grading Scale: " + gradingsList.get(i));
            Log.d(TAG,"Grade: " + gradesList.get(i));
            Log.d(TAG,"Course Code: " + classNumbersList.get(i));
            Log.d(TAG,"Section: " + sectionsList.get(i));
            Log.d(TAG,"Component: " + componentsList.get(i));
            Log.d(TAG,"Times: " + timesList.get(i));
            Log.d(TAG,"Location: " + roomsList.get(i));
            Log.d(TAG,"Instructor: " + instructorsList.get(i));
            Log.d(TAG,"Start Date: " + datesList.get(i));*/
            }
            //writer.close();



        } catch (MalformedURLException e) {
            // Replace this with your exception handling
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // Replace this with your exception handling
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
                
            




        DateTime now = new DateTime(System.currentTimeMillis());
        com.google.api.services.calendar.Calendar service= mActivity.mService;

        for (int i=0; i < arr.size(); i++){
            Event event = new Event();
            try {
                event.setSummary(arr.get(i).name);
                event.setLocation(arr.get(i).building + " " + arr.get(i).room);
                event.setDescription("Instructor: "+arr.get(i).instructor);
                Date sDate = new SimpleDateFormat("MM/dd/yyyy' 'hh:mmaa").parse(arr.get(i).startDate+" "+arr.get(i).startTime);
                Date eDate =new SimpleDateFormat("MM/dd/yyyy' 'hh:mmaa").parse(arr.get(i).startDate+" "+arr.get(i).endTime);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

                String startDate =new String(df.format(sDate)+":00-00:00");
                String endDate = new String(df.format(eDate)+":00-00:00");
                String[] splitEndDate = arr.get(i).endDate.split("/");
                String end_date=splitEndDate[2]+splitEndDate[0]+splitEndDate[1]+"T115900Z";
                

               // if (splitStartTime[2]=="P"){
                    //splitStartTime[0]=(Integer.toString((Integer.parseInt(splitStartTime[0]))+12)));
               // }
                //Log.d("newtag2",splitStartTime[0]);
               // Date s_date=new Date.parseRfc3339()
                
                DateTime start = DateTime.parseRfc3339(startDate);
                DateTime end = DateTime.parseRfc3339(endDate);
                Log.d(arr.get(i).name+" start",start.toString());
                event.setStart(new EventDateTime().setDateTime(start).setTimeZone("America/Chicago"));
                event.setEnd(new EventDateTime().setDateTime(end).setTimeZone("America/Chicago"));
                String by_day = arr.get(i).days.replaceAll("(\\p{Ll})(\\p{Lu})","$1,$2").toUpperCase();


                /*for (int j = 0; j < days.length; j++)
                {
                    Log.d("loop contents: ", days[i]);
                    by_day = days[j].toUpperCase() + ",";
                }*/

                Log.d(arr.get(i).name, by_day);
                Log.d("endDate: ", end_date);
                Log.d(arr.get(i).name+": rule","RRULE:FREQ=WEEKLY;BYDAY=" + by_day + ";UNTIL="
                        + end_date);
                event.setRecurrence(Arrays.asList("RRULE:FREQ=WEEKLY;BYDAY=" + by_day + ";UNTIL="
                        + end_date));

                service.events().insert("primary", event).execute();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
            return eventStrings;
        }

    }






class SoarObject{
    public String name;//
    public String units;//omit
    public String grades;//omit
    public String classNumber;//omit
    public String days;//
    public String startTime;//
    public String endTime;//
    public String building;//
    public String room;//
    public String instructor;
    public String startDate;
    public String endDate;

    public SoarObject(String a,String b,String c,String d,String e,String f,String g,String h,String i){
        this.name=a;
        this.units=b;
        this.grades=c;
        this.classNumber=d;
        String[] parts = e.split(" - ");
        String[] front_parts=parts[0].split(" ");
        this.days=front_parts[0];
        this.startTime=front_parts[1];    
        this.endTime=parts[1];
        String[] location_parts = f.split(" ");
        this.building=location_parts[0];
        this.room=location_parts[1];
        this.instructor=g;
        this.startDate=h;
        this.endDate=i;

    }
}
