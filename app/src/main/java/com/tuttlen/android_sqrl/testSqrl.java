package com.tuttlen.android_sqrl;
import com.tuttlen.android_sqrl.*;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

/**
 * Created by nathan on 3/10/16.
 */
public class testSqrl {
/*
    public static void main(String[] args)
   {
	String publicKey=args[1];
	String serverString ="";
	String signature ="";

	AuthorizationRequest authReq = new AuthorizationRequest(args[0]);
	SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("sqrl", SSLSocketFactory.getSocketFactory(), 443));
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(URL);
	String client = String.format("ver=%s\ncmd=%s\nidk=%s",1,"login",publicKey);
        String server = String.format("%s",serverString);
        String ids = signature;
        httppost.addHeader("client",Helper.urlEncode(client.getBytes()));
        httppost.addHeader("server",Helper.urlEncode(server.getBytes()));
        httppost.addHeader("ids",ids);        
	HttpResponse response = httpClient.execute(httppost); // Execute HTTP Post Request
	System.out.println(response.getStatusLine());
            
	
   }
   */
}
