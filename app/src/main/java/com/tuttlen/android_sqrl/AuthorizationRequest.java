package com.tuttlen.android_sqrl;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by in805 on 2/7/2016.
 */
public class AuthorizationRequest implements IAuthorizationRequest{

    public static int CurrentAuthVersion =1;
    public boolean fullNut =true;
    protected String CalledUrl ="";
    protected String webProtocol ="http";
    protected String domain ="";
    protected String nonce ="";
    protected boolean isValidBluetooth =false;
    protected boolean isValid =false;
    //This is formatted without colons
    protected  String bluetoothAddress ="";
    protected  boolean isSecure = false;
    protected Pattern regSQRLPattern = Pattern.compile("(http|https|sqrl|qrl):\\/\\/(.*?)\\/(.*)");
    protected Pattern bToothSQRLPattern  = Pattern.compile("(([0-9A-Fa-f]{2}[:-])+([0-9A-Fa-f]{2}))");
    protected  boolean isConnectionPicky =true;
    public String SFN ="";

    @Override
    public boolean isHTTPS() {
        return isSecure;
    }

    @Override
    public String getURL() {
        if(this.isValid) {
            return this.domain;
        } else {
            return this.bluetoothAddress;
        }
    }

    public String getSigningURL()
    {
        this.isConnectionPicky=false;
        this.fullNut=true;

        return getReturnURL();
    }

    public AuthorizationRequest getNewNut(String nut)
    {
        this.isConnectionPicky=false;
        this.fullNut=false;

        return new AuthorizationRequest(String.format("%s?nut=%s",this.getReturnURL() ,nut));
    }

    public String getFullUrl()
    {
        return this.CalledUrl;
    }

    //This may be different then Id I haven't decided yet
    @Override
    public String getBlueToothURL() {
        return this.bluetoothAddress;
    }

    //The called URL up until the query stinrg
    @Override
    public String getReturnURL() {
        int urlLength = fullNut ? this.CalledUrl.length():this.CalledUrl.indexOf("?") ;
        if(isConnectionPicky) {
            if (this.CalledUrl.contains("sqrl://")) {
                return this.CalledUrl.substring(0, urlLength).replace("sqrl://", "https://");
            } else if (this.CalledUrl.contains("qrl://")) {
                return this.CalledUrl.substring(0, urlLength).replace("qrl://", "http://");
            }
        }
        return this.CalledUrl.substring(0, urlLength);
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
        this.isValid =MatchRegularSQRL2(url);
        //try bluetooth maybe use the same pattern for the initial
        this.isValidBluetooth = MatchLocalSQRL(this.CalledUrl);

    }

    public boolean MatchRegularSQRL2(String URL)
    {
        HashMap<String,String> nvPairs = new HashMap<String,String>();
        //Did not no what I was thinking. Actually overthinking to be precise
        //I need to simplify this, it actually can be simplified very easily and
        //works just as well. SImply use split instructions
        //this.VersionOneParsing(url);
        //Split scheme from url "://"
        String[] schemeUrl = URL.split("://");
        this.webProtocol =schemeUrl[0];
        if (schemeUrl.length != 2)
        {
            return false;
        }

        //split domain portion from address portion "/"
        String[] urlAddress = schemeUrl[1].split("/");
        if (urlAddress.length <= 1) {
            return false;
        }
        this.domain =urlAddress[0];
        //We can process the domain portion later
        //Find last address portion and split the question mark to separate the query separator from the action
        // the name value pair into dictionary
        String[] actionAndparameters = urlAddress[urlAddress.length-1].split("\\?");
        String[] nameValueParis = actionAndparameters[1].split("&");

        for (String nv: nameValueParis
                )
        {
            String[] NandV = nv.split("=");
            if(NandV.length == 2) {
                if (!nvPairs.containsKey(NandV[0])) {
                    nvPairs.put(NandV[0], NandV[1]);
                } else {
                    //multiple definitions of nv paris are not allowed
                    return false;
                }
            } else {
                //There is a possibility of the nut being assumed and therefore no other parameters
                //in this case the only value expected is the nonce
                nvPairs.put("nut", NandV[0]);
            }
        }
        if (!nvPairs.containsKey("nut")) {
            return false;
        }

        this.nonce =nvPairs.get("nut");

        if(nvPairs.containsKey("sfn")) {
            this.SFN = nvPairs.get("sfn");
        }
        return true;
    }

    protected void VersionOneParsing(String url)
    {
        Matcher matchRegular = regSQRLPattern.matcher(url);
        Matcher bMatch = bToothSQRLPattern.matcher(url);

        if(matchRegular.matches()) {
            this.isValid= MatchRegularSQRL(matchRegular);

        } else {this.isValid =false; }

        //try bluetooth maybe use the same pattern for the initial
        this.isValidBluetooth = MatchLocalSQRL(this.CalledUrl);

        //if both are invalid then try again by adding http://
        if(!this.isValid && !this.isValidBluetooth)
        {
            //TODO Fix this might jsut remove it makes little sense to be this forgiving.
            //try one more time by adding a protocol prefix.
            //TODO decide whether we need to trim off any leading backslashes
            String newUrl = "http://"+ this.CalledUrl;
            Matcher matches2nd =regSQRLPattern.matcher(newUrl);
            if(matches2nd.matches()) {
                this.isValid= MatchRegularSQRL(matches2nd);
                if(this.isValid) {
                    this.CalledUrl = newUrl;
                }
            } else {
                //todo fault here
                this.isValid =false;
            }
        }

    }

    private boolean MatchLocalSQRL(String url ) {
        String entityPortion =  url.split("/")[0];
        String[] bAddress = entityPortion.split(":");
        if(bAddress.length >6 && bAddress.length <=8 ) {
            //the best way to tell if it is bluetooth so far
            //it is unfortunate that it looks exactly like a MAC address
            //we will need to do validation of this in the future to verify if we trust it
            this.isValidBluetooth=true;
            /*
            for (int i = 0; i < bAddress.length; i++) {
                //we need to do it this way in order to construct the packet, I think I will move this code out and then base64 encode it
                bluetoothAddress+= bAddress[i];
            }
            */

            bluetoothAddress =url.substring(0,url.indexOf("/"));
        }
        return ValidateBTooth(bluetoothAddress);
    }

    private boolean MatchRegularSQRL(Matcher matchRegular) {

        if (matchRegular.group(1).toLowerCase().startsWith("sqrl")) {
            this.isSecure = true;
            this.webProtocol = "https";
        }else if (matchRegular.group(1).toLowerCase().startsWith("qrl")) {
            this.isSecure = false;
            this.webProtocol = "http";
        }else if (matchRegular.group(1).toLowerCase().startsWith("https")) {
            this.isSecure = true;
            this.webProtocol = "https";
        } else if (!matchRegular.group(1).toLowerCase().startsWith("http")) {
            //TODO we need to throw an error do some other stuff to tell the user that this is an invalid URL
        } else if (matchRegular.group(1).toLowerCase().startsWith("http")) {
            this.isSecure = false;
            this.webProtocol = "http";
        } else {
           //TODO fault here
            return false;
        }
        this.domain = matchRegular.group(2);
        String queryString = matchRegular.group(3);
        String[] queryStringParse = Pattern.compile("sqrl\\?|nut=(.*?)").split(queryString);

        //need to update versioning here
        if (queryString.length() > 1) {
            this.nonce = queryStringParse[1];
        }
        //TODO do validation here
        return ValidateUrl(this.domain);
    }


}