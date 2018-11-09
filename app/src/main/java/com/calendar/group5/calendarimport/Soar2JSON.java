package com.calendar.group5.calendarimport;


import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.URL;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.CookieManager;
import javax.net.ssl.HttpsURLConnection;

public class Soar2JSON {
    private String user_name="w10016423";
    private String Semester_ID="4191";
    private String password="12345";

    private String e_pass=URLEncoder.encode(password, StandardCharsets.UTF_8.name());

    private String send_this=("userid="+user_name+"&pwd="+e_pass);

    public static void main(String [] args) throws IOException, MalformedURLException, URISyntaxException
    {

        CookieManager cookieManager = new CookieManager();

        CookieHandler.setDefault(cookieManager);
        try

        {

            URL login_URL = new URL("https://soar.usm.edu/psc/saprd90/EMPLOYEE/SA/c/NUI_FRAMEWORK.PT_LANDINGPAGE.GBL?&cmd=login&languageCd=ENG");

            HttpsURLConnection con = (HttpsURLConnection) login_URL.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");

            con.getOutputStream().write(send_this.getBytes("UTF-8"));
            //and this is where I'm at. the next part of the code sends the login information
            //to soar and then outputs the cookies to
        }
        catch (MalformedURLException e)
        {
            // Replace this with your exception handling
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

    }
}
