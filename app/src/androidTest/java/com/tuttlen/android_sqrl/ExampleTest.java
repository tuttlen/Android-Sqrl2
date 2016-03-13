package com.tuttlen.android_sqrl;

import android.support.annotation.NonNull;
import android.test.InstrumentationTestCase;
import android.util.Base64;

import com.github.dazoe.android.Ed25519;
import com.igormaznitsa.jbbp.JBBPParser;
import com.igormaznitsa.jbbp.mapper.Bin;
import com.igormaznitsa.jbbp.mapper.BinType;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayByte;
import com.igormaznitsa.jbbp.model.JBBPFieldStruct;
import com.tuttlen.aesgcm_android.AESGCMJni4;

import org.abstractj.kalium.Sodium;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import eu.artemisc.stodium.Stodium;

/**
 * Created by tuttlen on 1/23/2016.
 */
public class ExampleTest extends InstrumentationTestCase {

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
        byte[] publicKey_check = Helper.hexStringToByteArray("0b387c5669ca6fa137a2a383521f15b6243979f741b828fc0851e98fcfeb026c");
        byte[] privateKey = Helper.hexStringToByteArray("8385e4536a0c29d1c2c6d8befe741feb28d01a5722596362bb4487a7fd2f03120b387c5669ca6fa137a2a383521f15b6243979f741b828fc0851e98fcfeb026c");
        byte[] publicKey = Ed25519.PublicKeyFromPrivateKey(privateKey);

        assertEquals(Helper.bytesToHex(publicKey_check), Helper.bytesToHex(publicKey));
    }


    public void testSalsa()  throws GeneralSecurityException
    {
        //Sodium.sodium_init();
        Stodium.StodiumInit();
        String saltStr = "62007dc8478a69339b611b7046d6887a";

        String expectedReuslt = "5ada4327f5975b10e1667a2b4844576cb85f41a5d16e2163e440cb9bc8d9317a";
        String passwd = "tttttttttttttttttttttttt";
        byte[] random = new byte[]{};
        byte[] outBytes = new byte[]{};
        byte[] password = passwd.getBytes();
        byte[] salt = Helper.hexStringToByteArray(saltStr);
        int N = 512;
        int r = 256;
        int p = 1;
        int iterations = 85;

        String result2 = Helper.bytesToHex(Helper.PK(password, Helper.hexStringToByteArray(saltStr), iterations, new byte[]{}, N, r));


        assertEquals(expectedReuslt, result2);
    }

    public void testSalsa2()  throws GeneralSecurityException
    {
        //Sodium.sodium_init();
        Stodium.StodiumInit();
        String saltStr = "62007dc8478a69339b611b7046d6887a";

        String expectedReuslt = "2f30b9d4e5c48056177ff90a6cc9da04b648a7e8451dfa60da56c148187f6a7d";
        String expectedResult1 ="532bcc911c16df81996258158de460b2e59d9a86531d59661da5fbeb69f7cd54";
        String passwd = "tttttttttttttttttttttttt";
        byte[] random = new byte[]{};
        byte[] outBytes = new byte[]{};
        byte[] password = passwd.getBytes();
        byte[] salt = Helper.hexStringToByteArray(saltStr);
        int N = 512;
        int r = 256;
        int p = 1;
        int iterations = 2;

        //Sodium.randombytes_buf(random, 32);
        //Sodium.crypto_pwhash_scryptsalsa208sha256_ll(password,password.length,salt,salt.length,N,r,p,outBytes,outBytes.length);
        android.util.Log.d("test",String.format("Pasword: %s",Helper.bytesToHex(password)));
        android.util.Log.d("test",String.format("salt: %s",Helper.bytesToHex(salt)));
        for (int i = 0; i < 16; i++) {
            saltStr = saltStr.substring(2);
            android.util.Log.d("test",String.format("Salt: %s",saltStr));
            String result2 = Helper.bytesToHex(Helper.PK(password, Helper.hexStringToByteArray(saltStr), iterations, new byte[]{}, N, r));
            android.util.Log.d("test",String.format("Pasword: %s",result2));
        }


        //assertEquals(expectedReuslt, result2);
    }

    public void testByteUnPackSQRLData() throws IOException,GeneralSecurityException
    {
        Stodium.StodiumInit();
        String password= "tttttttttttttttttttttttt";

        String sqrlData ="SQRLDATAnQABAC0AjAIFnNpAdZDUjrMFYgB9yEeKaTObYRtwRtaIeglVAAAA8QAEBQ8AZKlrEUYZ1CxIBjW-pRpmbCY3P4H9v99j16WrXI262DFZIP4kMGhqK7N05g6gQzcQdgiD72cqj5qHmKiiP88Thf0RSJD6aAvRcP3XNdpSglh4l1Fb-1nb-A4TiH3Tk0zR0bE0ZcqhUaj4M4ILu86KmEkAAgAqponTFyavyjhYUCECOHqSCU0AAAAt_s6hM4nMEk4xdmyQmd1Juojslag8I6cVb2ma4B3CpIBlLnDCVd066kaB9GjptRE";
        String sqrlBase = sqrlData.substring(8);
        byte[] hexResult = Helper.urlDecode(sqrlBase);

        SqrlData parsed = SqrlData.ExtractSqrlData(hexResult);

        String scrpytPassword ="5ada4327f5975b10e1667a2b4844576cb85f41a5d16e2163e440cb9bc8d9317a";
        String IV = "8c02059cda407590d48eb305";
        String tag ="4452243e9a02f45c3f75cd7694a0961e";
        int nfactor  = 512;
        int iteration = 85;
        String aad = "9d0001002d008c02059cda407590d48eb30562007dc8478a69339b611b7046d6887a0955000000f10004050f00";
        String Identity_MasterKey ="64a96b114619d42c480635a946999b098dcfe07f6ff7d8f5e96ad7236eb60c56";
        String Identity_LockKey ="483f890c1a1a8aecdd3983a810cdc41d8220fbd9caa3e6a1e62a288ff3c4e17f";
        String scryptSalt ="62007dc8478a69339b611b7046d6887a";

        assertEquals(IV,Helper.bytesToHex(parsed.sqrlStorage.IV));
        assertEquals(scryptSalt,Helper.bytesToHex(parsed.sqrlStorage.ScryptSalt));

        assertEquals(Identity_LockKey,Helper.bytesToHex(parsed.sqrlStorage.IDLK));
        assertEquals(Identity_MasterKey,Helper.bytesToHex(parsed.sqrlStorage.IDMK));
        assertEquals(tag,Helper.bytesToHex(parsed.sqrlStorage.tag));
        assertEquals(aad,Helper.bytesToHex(parsed.aad));
        assertEquals(nfactor, 1 << parsed.sqrlStorage.nFactor);
        assertEquals(iteration,parsed.sqrlStorage.ScryptIteration);
        assertEquals(Helper.hexStringToByteArray(scryptSalt).length,16);
        android.util.Log.d("test", String.format("Password: %s", Helper.bytesToHex(password.getBytes())));
        android.util.Log.d("test", String.format("Iterations: %d",parsed.sqrlStorage.ScryptIteration));
        byte[] scrypekey = Helper.PK(password.getBytes(), parsed.sqrlStorage.ScryptSalt, parsed.sqrlStorage.ScryptIteration, new byte[]{}, 1 << parsed.sqrlStorage.nFactor, 256);
        android.util.Log.d("test", String.format("Result: %s",Helper.bytesToHex(scrypekey)));
        assertEquals(scrpytPassword,Helper.bytesToHex(scrypekey));

    }

    public void testByteUnPackSQRLData_Unencrypt() throws IOException,GeneralSecurityException
    {
        Stodium.StodiumInit();
        String password= "tttttttttttttttttttttttt";

        String sqrlData ="SQRLDATAnQABAC0AjAIFnNpAdZDUjrMFYgB9yEeKaTObYRtwRtaIeglVAAAA8QAEBQ8AZKlrEUYZ1CxIBjW-pRpmbCY3P4H9v99j16WrXI262DFZIP4kMGhqK7N05g6gQzcQdgiD72cqj5qHmKiiP88Thf0RSJD6aAvRcP3XNdpSglh4l1Fb-1nb-A4TiH3Tk0zR0bE0ZcqhUaj4M4ILu86KmEkAAgAqponTFyavyjhYUCECOHqSCU0AAAAt_s6hM4nMEk4xdmyQmd1Juojslag8I6cVb2ma4B3CpIBlLnDCVd066kaB9GjptRE";
        String sqrlBase = sqrlData.substring(8);
        byte[] hexResult = Helper.urlDecode(sqrlBase);

        SqrlData parsed = SqrlData.ExtractSqrlData(hexResult);

        String scrpytPassword ="5ada4327f5975b10e1667a2b4844576cb85f41a5d16e2163e440cb9bc8d9317a";
        String IV = "8c02059cda407590d48eb305";
        String tag ="4452243e9a02f45c3f75cd7694a0961e";
        int nfactor  = 512;
        int iteration = 85;
        String aad = "9d0001002d008c02059cda407590d48eb30562007dc8478a69339b611b7046d6887a0955000000f10004050f00";
        String Identity_MasterKey ="64a96b114619d42c480635a946999b098dcfe07f6ff7d8f5e96ad7236eb60c56";
        String Identity_LockKey ="483f890c1a1a8aecdd3983a810cdc41d8220fbd9caa3e6a1e62a288ff3c4e17f";
        String scryptSalt ="62007dc8478a69339b611b7046d6887a";

        assertEquals(IV,Helper.bytesToHex(parsed.sqrlStorage.IV));
        assertEquals(scryptSalt,Helper.bytesToHex(parsed.sqrlStorage.ScryptSalt));

        assertEquals(Identity_LockKey,Helper.bytesToHex(parsed.sqrlStorage.IDLK));
        assertEquals(Identity_MasterKey,Helper.bytesToHex(parsed.sqrlStorage.IDMK));
        assertEquals(tag,Helper.bytesToHex(parsed.sqrlStorage.tag));
        assertEquals(aad, Helper.bytesToHex(parsed.aad));
        assertEquals(nfactor, 1 << parsed.sqrlStorage.nFactor);
        assertEquals(iteration, parsed.sqrlStorage.ScryptIteration);
        assertEquals(Helper.hexStringToByteArray(scryptSalt).length, 16);
        android.util.Log.d("PK", String.format("Password: %s", Helper.bytesToHex(password.getBytes())));
        android.util.Log.d("PK", String.format("Iterations: %d", parsed.sqrlStorage.ScryptIteration));
        byte[] scrypekey = Helper.PK(password.getBytes(), parsed.sqrlStorage.ScryptSalt, parsed.sqrlStorage.ScryptIteration, new byte[]{}, 1 << parsed.sqrlStorage.nFactor, 256);
        android.util.Log.d("PK", String.format("Result: %s", Helper.bytesToHex(scrypekey)));

        AESGCMJni4 crypto = new AESGCMJni4();
        String result = crypto.doDecryption(scrypekey, parsed.sqrlStorage.IV, parsed.aad, parsed.sqrlStorage.tag, parsed.sqrlStorage.IDLK);
        android.util.Log.d("test", String.format("ResultDecryption: %s", result));

        assertEquals(scrpytPassword, Helper.bytesToHex(scrypekey));

    }

    public void testEncryptAADUnencrypt() throws JSONException {
        Stodium.StodiumInit();
        AESGCMJni4 crypto = new AESGCMJni4();
        String scrpytPassword ="5ada4327f5975b10e1667a2b4844576cb85f41a5d16e2163e440cb9bc8d9317a";
        String IV = "8c02059cda407590d48eb305";
        String aad = "9d0001002d008c02059cda407590d48eb30562007dc8478a69339b611b7046d6887a0955000000f10004050f00";
        byte[] randomKey = new byte[32];
        Sodium.randombytes_buf(randomKey, 32);
        android.util.Log.d("test", String.format("RandomKey: %s", AESGCMJni4.bytesToHex(randomKey)));
        String result = crypto.doEncryption(AESGCMJni4.hexStringToByteArray(scrpytPassword),AESGCMJni4.hexStringToByteArray(IV),AESGCMJni4.hexStringToByteArray(aad),new byte[]{},randomKey);
        JSONObject object = new JSONObject(result);

        String decryptResult = crypto.doDecryption(AESGCMJni4.hexStringToByteArray(scrpytPassword),
                                AESGCMJni4.hexStringToByteArray(IV),
                                AESGCMJni4.hexStringToByteArray(aad),
                                AESGCMJni4.hexStringToByteArray(object.getString("Tag")),
                                AESGCMJni4.hexStringToByteArray(object.getString("CipherText")));

        android.util.Log.d("test", String.format("ResultDecryption: %s", decryptResult));
        assertEquals(AESGCMJni4.bytesToHex(randomKey),decryptResult.toUpperCase());

    }

    public void testEd25519ToSodoium() throws Exception
    {
        Stodium.StodiumInit();
        byte[] randomBytes = new byte[64];
        Sodium.randombytes_buf(randomBytes,64);

        System.out.println(Helper.bytesToHex(randomBytes));

        byte[] edKey = Ed25519.PublicKeyFromPrivateKey(randomBytes);
        byte[] edkey2 = Helper.PublicKeyFromPrivateKey(randomBytes);
        String first = Helper.bytesToHex(edkey2);
        String libKey = Helper.bytesToHex(edKey);

        assertEquals(first,libKey);

        //9U0eUkrV18ObhG+n7M/DqFlxPaSqytkHwL4RLuXtlkbF8JB2XUxF4Lxj0qpe0SI3aLErphECKU6P+1eKBfqYlw==
    }

    public void testEd25519ToSodoium_fromKnown() throws Exception
    {
        Stodium.StodiumInit();
        String privateKey ="9U0eUkrV18ObhG+n7M/DqFlxPaSqytkHwL4RLuXtlkbF8JB2XUxF4Lxj0qpe0SI3aLErphECKU6P+1eKBfqYlw==";
        String publicKey = "xfCQdl1MReC8Y9KqXtEiN2ixK6YRAilOj/tXigX6mJc=";
        byte[] randomPrivate = Base64.decode(privateKey, Base64.DEFAULT);

        byte[] edKey = Ed25519.PublicKeyFromPrivateKey(randomPrivate);
        byte[] edkey2 = Helper.PublicKeyFromPrivateKey(randomPrivate);
        String first = Helper.bytesToHex(edkey2);
        String libKey = Helper.bytesToHex(edKey);

        assertEquals(first,libKey);

        assertEquals(publicKey, Base64.encodeToString(edKey, Base64.DEFAULT | Base64.NO_WRAP));
        assertEquals(publicKey, Base64.encodeToString(edkey2, Base64.DEFAULT | Base64.NO_WRAP));
    }

    public void testEd25519ToSodoium_Sign() throws Exception
    {
        Stodium.StodiumInit();
        String privateKey ="9U0eUkrV18ObhG+n7M/DqFlxPaSqytkHwL4RLuXtlkbF8JB2XUxF4Lxj0qpe0SI3aLErphECKU6P+1eKBfqYlw==";
        String publicKey = "xfCQdl1MReC8Y9KqXtEiN2ixK6YRAilOj/tXigX6mJc=";
        byte[] randomBuf = new byte[128];
        Sodium.randombytes_buf(randomBuf,128);
        byte[] randomPrivate = Base64.decode(privateKey, Base64.DEFAULT);

        byte[] edKey = Ed25519.PublicKeyFromPrivateKey(randomPrivate);
        byte[] edkey2 = Helper.PublicKeyFromPrivateKey(randomPrivate);


        byte[] sodium_result = Helper.Sign(randomBuf, randomPrivate);
        byte[] ed2_result = Ed25519.Sign(randomBuf, randomPrivate);

        assertEquals(Helper.bytesToHex(sodium_result), Helper.bytesToHex(ed2_result));

    }

}