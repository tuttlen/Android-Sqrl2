package com.tuttlen.android_sqrl;

import android.test.InstrumentationTestCase;

import com.github.dazoe.android.Ed25519;

import org.abstractj.kalium.Sodium;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import eu.artemisc.stodium.Stodium;

/**
 * Created by tuttlen on 1/23/2016.
 */
public class ExampleTest extends InstrumentationTestCase {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //TODO move this to a helper class
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static byte[] massXOR(byte[] first, byte[] second)
    {
        byte[] resultByte = new byte[first.length];
        for (int i = 0; i < first.length; i++)
        {
            resultByte[i] = (byte)(first[i] ^ second[i]);
        }
        return resultByte;
    }

    //PBKDF(2)
    public static byte[] PK(byte[] password, byte[] salt, int iterations, byte[] previosResult, int N, int r) throws GeneralSecurityException
    {
        int p=1;

        byte[] result = new byte[32];
        Sodium.crypto_pwhash_scryptsalsa208sha256_ll(password,password.length,salt,salt.length,N,r,p,result,result.length);
        iterations--;
        android.util.Log.d("PK",String.format("On iteration %d: %s",iterations,bytesToHex(result)));
        if(iterations == 0 && previosResult.length == 0) return result;
        if(iterations == 0) return massXOR(result,previosResult);
        if(previosResult.length == 0)
        {
            return PK(password,result,iterations,result,N,r);
        } else {
            return PK(password,result,iterations,massXOR(previosResult,result),N,r);
        }
    }

    public void testAuth() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("https://localhost/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals("localhost", testReuslt);
        assertEquals( "https://localhost/sqrl",req.getReturnURL());
    }
    public void testAuth2() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("http://localhost/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals(testReuslt, "localhost");
        assertEquals( "http://localhost/sqrl",req.getReturnURL());
    }

    public void testAuth3() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("https://example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals("example.com",testReuslt);
        assertEquals("https://example.com/sqrl",req.getReturnURL());
    }

    public void testAuth3_wqithsqrl() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("sqrl://example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals("example.com",testReuslt);
        //if we understand how to construct a sqrl then we should expect to understand how to handle the protocol
        assertEquals("sqrl://example.com/sqrl",req.getReturnURL());
    }

    public void testAuth3_nonce() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("sqrl://example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getNonce();
        assertEquals("4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4",testReuslt);
    }

    public void testAuth_malformed_url() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        //For empty protocol lets assume http
        assertEquals(true,req.IsValid());
        assertEquals("example.com",req.getDomain());
        assertEquals("http://example.com/sqrl",req.getReturnURL());
    }

    public void testAuth_malformed_url2() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("10.0.0.2/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a");
        //Changed an empty protocol to default to http this may chagne
        assertEquals(true,req.IsValid());
        assertEquals("10.0.0.2",req.getDomain());
        assertEquals("http://10.0.0.2/sqrl",req.getReturnURL());
    }

    public void testAuth_malformed_url3() throws Exception{
        //this test just changed, I decided to make this format invalid
        AuthorizationRequest req = new AuthorizationRequest("10.0.0.2.5/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a");
        assertEquals(req.IsValid(),true);
        assertEquals("10.0.0.2.5",req.getDomain());
        assertEquals("http://10.0.0.2.5/sqrl",req.getReturnURL());
    }

    public void test_http()
    {
        String URL = "http://example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4";
        HttpClientBuilder builder;
        HttpClient httpClient;
        HttpPost httppost;

        builder = HttpClientBuilder.create();
        //builder.setSSLSocketFactory( new SSLConnectionSocketFactory( SSLContext.getDefault()));
        httpClient = builder.build();
        httppost = new HttpPost(URL);
    }

    public void test_http_malformed()
    {
        String URL = "example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4";
        HttpClientBuilder builder;
        HttpClient httpClient;
        HttpPost httppost;


        builder = HttpClientBuilder.create();
        //builder.setSSLSocketFactory( new SSLConnectionSocketFactory( SSLContext.getDefault()));
        httpClient = builder.build();
        httppost = new HttpPost(URL);
    }

    public void test_btooth_proper() throws Exception
    {
        //since
        String properBtooth ="B1:B1:0D:B3:10:30:00:00";
        AuthorizationRequest req = new AuthorizationRequest("B1:B1:0D:B3:10:30:00:00/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a");
        assertTrue("Is proper Bluetooth address",req.isValidBluetooth);
        assertEquals(properBtooth,req.getBluetoothId());
    }

    public void test_btooth_proper_URL() throws Exception
    {
        String properBtooth ="B1:B1:0D:B3:10:30:00:00";
        AuthorizationRequest req = new AuthorizationRequest("B1:B1:0D:B3:10:30:00:00/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a");
        assertTrue("Is proper Bluetooth address",req.isValidBluetooth);
        assertEquals("B1:B1:0D:B3:10:30:00:00",req.getURL());
    }

    public void test_btooth_improper_URL() throws Exception
    {
        String improperBtooth ="B1:B1:0D:B3:10:30:00:00:00";
        AuthorizationRequest req = new AuthorizationRequest("B1:B1:0D:B3:10:30:00:00:00/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a");
        assertFalse("Is proper Blue" +
                "tooth address", req.isValidBluetooth);
    }

    public void test_btooth_malformed() throws Exception
    {
        String imProperBtooth ="B1:B1:0D:B3:10:30:00:00:00";
        AuthorizationRequest req = new AuthorizationRequest("B1:B1:0D:B3:10:30:00:00:00/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a");
        assertFalse(req.isValidBluetooth);
    }

    public void test_NewAuthRequest() throws Exception
    {
        String imProperBtooth ="B1:B1:0D:B3:10:30:00:00:00";
        String s1 ="https://www.example.com/sqrl?2342349234234234234234";
        String s2 = "sqrl://www.example.com/sqrl?2342349234234234234234";
        String s3 = "sqrl://www.example.twist/login/sqrl.ext?nut=1000";
        String s4 = "http://www.example.com/sqrl?2342349234234234234234";
        String s5 = "B1:B1:0D:B3:10:30:00:00:00/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a";
        String s6 = "B1:B1:0D:B3:10:30:00:00:00'";
        AuthorizationRequest req1 = new AuthorizationRequest(s1);
        AuthorizationRequest req2 = new AuthorizationRequest(s2);
        AuthorizationRequest req3 = new AuthorizationRequest(s3);
        AuthorizationRequest req4 = new AuthorizationRequest(s4);
        AuthorizationRequest req5 = new AuthorizationRequest(s5);
        AuthorizationRequest req6 = new AuthorizationRequest(s6);

    }


    public void test_NewAuthRequestBtooth() throws Exception
    {

        String s5 = "B1:B1:0D:B3:10:30:00:00:00/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a";
        String s6 = "B1:B1:0D:B3:10:30:00:00:00'";
        AuthorizationRequest req5 = new AuthorizationRequest(s5);
        AuthorizationRequest req6 = new AuthorizationRequest(s6);

    }

    public void test_ed25519() throws Exception
    {
        //private
        //8385e4536a0c29d1c2c6d8befe741feb28d01a5722596362bb4487a7fd2f03120b387c5669ca6fa137a2a383521f15b6243979f741b828fc0851e98fcfeb026c

        //public
        //0b387c5669ca6fa137a2a383521f15b6243979f741b828fc0851e98fcfeb026c
        byte[] publicKey_check = hexStringToByteArray("0b387c5669ca6fa137a2a383521f15b6243979f741b828fc0851e98fcfeb026c");
        byte[] privateKey = hexStringToByteArray("8385e4536a0c29d1c2c6d8befe741feb28d01a5722596362bb4487a7fd2f03120b387c5669ca6fa137a2a383521f15b6243979f741b828fc0851e98fcfeb026c");
        byte[] publicKey = Ed25519.PublicKeyFromPrivateKey(privateKey);

        assertEquals(bytesToHex(publicKey_check), bytesToHex(publicKey));
    }


    public void testSalsa()  throws GeneralSecurityException
    {
        //Sodium.sodium_init();
        Stodium.StodiumInit();
        String saltStr = "0000000000000000000000000000000000000000000000000000000000000000";
        String expectedReuslt = "2f30b9d4e5c48056177ff90a6cc9da04b648a7e8451dfa60da56c148187f6a7d";
        String expectedResult1 ="532bcc911c16df81996258158de460b2e59d9a86531d59661da5fbeb69f7cd54";
        String passwd = "password";
        byte[] random = new byte[]{};
        byte[] outBytes = new byte[]{};
        byte[] password = passwd.getBytes();
        byte[] salt = hexStringToByteArray(saltStr);
        int N = 512;
        int r = 256;
        int p = 1;
        int iterations = 123;

        //Sodium.randombytes_buf(random, 32);
        //Sodium.crypto_pwhash_scryptsalsa208sha256_ll(password,password.length,salt,salt.length,N,r,p,outBytes,outBytes.length);
        android.util.Log.d("PK",String.format("Pasword: %s",bytesToHex(password)));
        android.util.Log.d("PK",String.format("salt: %s",bytesToHex(salt)));
        String result2 = bytesToHex(PK(password,salt,iterations,new byte[]{},N,r));

        assertEquals(expectedReuslt.toUpperCase(), result2);
    }

    public void testByteUnPackSQRLData()
    {
        Map<String,BytePacked> packedResource = new HashMap<String,BytePacked>();
        String sqrlData ="SQRLDATAnQABAC0AjAIFnNpAdZDUjrMFYgB9yEeKaTObYRtwRtaIeglVAAAA8QAEBQ8AZKlrEUYZ1CxIBjW-pRpmbCY3P4H9v99j16WrXI262DFZIP4kMGhqK7N05g6gQzcQdgiD72cqj5qHmKiiP88Thf0RSJD6aAvRcP3XNdpSglh4l1Fb-1nb-A4TiH3Tk0zR0bE0ZcqhUaj4M4ILu86KmEkAAgAqponTFyavyjhYUCECOHqSCU0AAAAt_s6hM4nMEk4xdmyQmd1Juojslag8I6cVb2ma4B3CpIBlLnDCVd066kaB9GjptRE";
        String sqrlBase = sqrlData.substring(8);


    }

}