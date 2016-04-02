package com.tuttlen.android_sqrl;

import com.google.android.gms.common.server.converter.StringToIntConverter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nathan tuttleon 4/2/16.
 */
public class SqrlResponse
{
    public int Version =0;
    public String nut ="";
    public String SFN ="";
    public String qry ="";
    public String tifHex ="";
    public String tifReuslt ="";

    public SqrlResponse(String encodedResponse) throws UnsupportedEncodingException
    {
        byte[] decoded = Helper.urlDecode(encodedResponse);
        String value = new String(decoded,"UTF-8");
        //what if we are using another form of line feed (dos as opposed to unix)
        String[] separatedValues = value.split("\r\n");
        for (String item:
                separatedValues) {
            String[] nameValue = item.split("=");
            if(nameValue[0].trim().equals("ver")) { this.Version = Integer.parseInt(nameValue[1]);}
            if(nameValue[0].trim().equals("nut")) { this.nut = nameValue[1];}
            if(nameValue[0].trim().equals("tif")) {
                this.tifHex =nameValue[1];
                this.tifReuslt = SqrlResponse.ExtractTIF(Integer.parseInt(this.tifHex, 16));}
            if(nameValue[0].trim().equals("sfn")) { this.SFN = nameValue[1];}
            if(nameValue[0].trim().equals("qry")) { this.qry = nameValue[1];}
        }

    }

    public static String ExtractTIF(int tifCode)
    {
        String result ="";
        for(TIF code : TIF.values()) {

            if((tifCode & code.value) > 0) {
                result= String.format("%s%s\r\n",result,code.tifResult() );
            }
        }
        return result;
    }


}
