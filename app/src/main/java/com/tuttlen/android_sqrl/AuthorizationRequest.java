package com.tuttlen.android_sqrl;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by in805 on 2/7/2016.
 */
public class AuthorizationRequest implements IAuthorizationRequest{

    protected String CalledUrl ="";
    protected String webProtocol ="http";
    protected String domain ="";
    protected String nonce ="";
    protected boolean isValidBluetooth =false;
    protected boolean isValid =false;
    //This is formatted without colons
    protected  String bluetoothAddress ="";
    protected  boolean isSecure = false;

    @Override
    public boolean isHTTPS() {
        return isSecure;
    }

    @Override
    public String getURL() {
        return this.domain;
    }

    //This may be different then Id I haven't decided yet
    @Override
    public String getBlueToothURL() {
        return this.bluetoothAddress;
    }

    //The called URL up until the query stinrg
    @Override
    public String getReturnURL() {
        return this.CalledUrl.substring(0,this.CalledUrl.indexOf("?"));
    }

    @Override
    public String getNonce() {
        return this.nonce;
    }

    @Override
    public String getDomain() {
        return this.domain;
    }

    @Override
    public boolean IsValid() {
        return isValid;
    }

    @Override
    public String getBluetoothId() {
        return this.bluetoothAddress;

    }

    @Override
    public boolean IsValidBluetooth(String address) {
        return isValidBluetooth;
    }

    @Override
    public String removeScheme(String URL) throws URISyntaxException {
        return null;
    }

    @Override
    public boolean ValidateUrl(String URL) {
        //for now return true until we get this in place
        //TODO
        if(URL == "") return false;
        return true;
    }

    @Override
    public boolean ValidateBTooth(String address) {
        //for now return true until we get this in place
        //TODO
        if(address =="") return false;
        return true;
    }


    public AuthorizationRequest(String url)
    {
        this.CalledUrl = url;
        Pattern reg1 = Pattern.compile("(http|https|sqrl):\\/\\/(.*?)\\/(.*)");
        Pattern bTooth = Pattern.compile("(([0-9A-Fa-f]{2}[:-])+([0-9A-Fa-f]{2}))");

        Matcher matchRegular = reg1.matcher(url);
        Matcher bMatch = bTooth.matcher(url);

        if(matchRegular.matches()) {
            if(matchRegular.group(1).toLowerCase().startsWith("sqrl"))
            {
                this.isSecure=true;
                this.webProtocol="sqrl";
            } else if(matchRegular.group(1).toLowerCase().startsWith("https")) {
                this.isSecure=true;
                this.webProtocol ="https";
            }
            this.domain = matchRegular.group(2);
            String queryString =matchRegular.group(3);
            String[] queryStringParse =Pattern.compile("sqrl\\?|nut=(.*?)").split(queryString);

            //need to update versioning here
            if(queryString.length() >1) {
                this.nonce = queryStringParse[1];
            }
            //TODO do validation here
            ValidateUrl(this.domain);
        } else {isValid =false;}

        if(!matchRegular.matches()) {
            String entityPortion =  url.split("/")[0];
            String[] bAddress = entityPortion.split(":");
            if(bAddress.length >6) {
                //the best way to tell if it is bluetooth so far
                //it is unfortunate that it looks exactly like a MAC address
                //we will need to do validation of this in the future to verify if we trust it
                this.isValidBluetooth=true;
                for (int i = 0; i < bAddress.length; i++) {
                    bluetoothAddress+= bAddress[i];
                }
            }
            ValidateBTooth(bluetoothAddress);
        } else{this.isValidBluetooth = false;}

    }
}
