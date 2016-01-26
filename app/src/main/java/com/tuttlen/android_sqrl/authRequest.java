package com.tuttlen.android_sqrl;

import android.util.Log;


import java.net.URISyntaxException;

// Copyright © 2013 geir54
//Thanks geir54 updating for some exception handling
//Contains all the info for the web page you are trying to authenticate with 
public class authRequest {
    private boolean isHTTPS; //
    // Is it using SSL or not
    private String URL; // contains everything except scheme
    private String bAddress;
    private  boolean isMalformed =false;
    public String domain ="";
    public  boolean isBlueTooth=false;
    public authRequest(String URL) throws URISyntaxException{
        this.URL = removeScheme(URL);
    }

    public boolean isHTTPS() {
        return isHTTPS;
    }

    // The part thet should be signed
    public String getURL() {
        return URL;
    }

    public String getBlueToothURL() {
        String retVal ="";
        String firstPart = URL.substring(0,'/');
        String lastPart = URL.substring('/');
        String[] array = firstPart.split(":");
        for (int i=0;i< array.length;i++   ) {
            retVal+=array[i];
        }
        return retVal+lastPart;
    }

    public String getReturnURL()
    {
        String retURL="";

        if(isMalformed) {
            retURL = URL.substring(0, URL.indexOf("?"));
        } else {
            retURL = URL.substring(1, URL.indexOf("?"));
        }

        if (isHTTPS)
        {
            retURL =  "https://" + retURL;
        } else {
            retURL = "http://" + retURL;
        }
        Log.v("web", retURL);
        return retURL;
    }

    public String getNonce()
    {
        String retURL = URL.substring(URL.indexOf("?")+1);

        return retURL;
    }

    // get domain form URL
    public String getDomain() {
        if(domain =="") {
            domain = URL.substring(1, URL.indexOf("/", 1));
        }
        return domain;
    }

    public boolean isValid()
    {
        //TODO resolve dependency conflict with commons-logging and org.apache.commons.validator
        return true;
    }

    public String getBluetoothId() {
        return   bAddress;
    }

    public boolean isValidBlueTooth(String address)
    {
       //TODO
        String bTooth = address.substring(0);
        String[] array = bTooth.split(":");

        return array.length ==8;
    }

    // remove the sqrl:// part from the URL and set isHTTPS
    private String removeScheme(String URL) throws URISyntaxException {
        if (URL.toLowerCase().startsWith("https"))
        {
            URL = URL.substring(7);
            isHTTPS = true;
        }
        else if(URL.toLowerCase().startsWith("http"))
        {
            URL = URL.substring(6);
            isHTTPS = false;
        }
        else if(URL.toLowerCase().startsWith("sqrl"))
        {
            //sqrl is always secure
            URL = URL.substring(6);
            isHTTPS = true;
        } else {
            //it may not have an http designation
            //let's try it anyway
            isHTTPS =false;
            bAddress = URL.substring(0,URL.indexOf('/'));
            if(this.isValidBlueTooth(bAddress)) {

                isBlueTooth = true;
            } else {
                isMalformed =true;
            }
            domain = URL.substring(0,URL.indexOf('/'));
            //URL = URL.substring(URL.indexOf('/'));

        }


        return URL;
    }

}
