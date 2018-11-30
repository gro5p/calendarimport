package com.calendar.group5.calendarimport;



import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import java.io.Serializable;
import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.URL;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.net.CookieManager;
import java.net.CookieStore;

import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes.Name;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText username;
    private EditText password;
    private Button btnSubmit;
    private String user_name;
    private String pw;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        addListenerOnButton();

    }

    public void addListenerOnButton() {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        btnSubmit = (Button) findViewById(R.id.GetClassSchedule);

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                user_name=username.getText().toString();
                pw=password.getText().toString();
                Intent i = new Intent(MainActivity.this, googleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("s_id",user_name );
                bundle.putString("pw", pw);
                i.putExtras(bundle);
                startActivity(i);
            }
                
                
                //Intent intent = new Intent(MainActivity.this, GoogleActivty.class);
                
                
                //intent.putExtra("objects_list",arr);
                
                //startActivity(intent);
                //this will store the output

                //Stores the cookies necessary to reach the Soar Calendar


                

            });
        };
    
    
    
    

    
}

/*
class SoarObject{
    public String name;
    public String units;
    public String grades;
    public String classNumber;
    public String days;
    public String startTime;
    public String endTime;
    public String rooms;
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
        
        this.rooms=f;
        this.instructor=g;
        this.startDate=h;
        this.endDate=i;

    }
}

class SoarRequest extends AsyncTask<Void,Void,ArrayList<SoarObject>>
{
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

    public interface AsyncResponse {
        public void processFinish(ArrayList<SoarObject> result);

    }

    public AsyncResponse delegate = null;

    public SoarRequest(String user, String password){
        this.delegate=delegate;
        this.user_name = user;
        this.pw = password;
    }
    
    @Override
    protected ArrayList<SoarObject> doInBackground(Void... args){
    {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        ArrayList<SoarObject> arr= new ArrayList<>();
        try{

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
            if (inputLine.contains("MTG_DATES"))
            {
                String dates=inputLine.replace("</span>", "").replaceAll(".*>", "");
                String[] parts = dates.split(" - ");
                startDateList.add(parts[0]);
                endDatesList.add(parts[1]); 
            }
        }
        in.close();

        for (int i = 0; i < namesList.size(); i++) {
            
            arr.add(new SoarObject(namesList.get(i),unitsList.get(i),
            gradesList.get(i), classNumbersList.get(i), timesList.get(i),
            roomsList.get(i), instructorsList.get(i), startDateList.get(i),
            endDatesList.get(i)));
            Log.d("Tag",arr.get(i).name);

            */
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
            Log.d(TAG,"Start Date: " + datesList.get(i));*//*

        }
        //writer.close();
        Log.d("Fuck","here");
        
        
        }
        catch (MalformedURLException e)
        {
            // Replace this with your exception handling
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // Replace this with your exception handling
            e.printStackTrace();
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
        return arr;
        }
    }

    public void onPostExecute(ArrayList<SoarObject> result) {

    }
}
*/
