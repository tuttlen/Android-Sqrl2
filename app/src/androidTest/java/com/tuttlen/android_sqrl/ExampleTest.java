package com.tuttlen.android_sqrl;

import android.test.InstrumentationTestCase;

import com.tuttlen.aesgcm_android.AESGCMJni4;

import org.abstractj.kalium.Sodium;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

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
        req.isConnectionPicky=false;
        assertEquals("sqrl://example.com/sqrl",req.getReturnURL());
    }

    public void testAuth3_wqithsqrl_picky() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("sqrl://example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals("example.com",testReuslt);
        //if we understand how to construct a sqrl then we should expect to understand how to handle the protocol
        req.isConnectionPicky=true;
        assertEquals("https://example.com/sqrl",req.getReturnURL());
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
        assertTrue("Is proper Bluetooth address", req.isValidBluetooth);
        assertEquals(properBtooth,req.getBluetoothId());
    }

    public void test_btooth_proper_URL() throws Exception
    {
        String properBtooth ="B1:B1:0D:B3:10:30:00:00";
        AuthorizationRequest req = new AuthorizationRequest("B1:B1:0D:B3:10:30:00:00/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a");
        assertTrue("Is proper Bluetooth address", req.isValidBluetooth);
        assertEquals("B1:B1:0D:B3:10:30:00:00", req.getURL());
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

    public void testQRlAddress()
    {
        //qrl://10.0.0.27/login/sqrlauth.php?nut=5f7d471e26450c1539fe73b7867a789abb0c7de6f4246f1e719d7b2830e73de2
        String qrlAddress ="qrl://10.0.0.27/login/sqrlauth.php?nut=5f7d471e26450c1539fe73b7867a789abb0c7de6f4246f1e719d7b2830e73de2";
        AuthorizationRequest req = new AuthorizationRequest(qrlAddress);
        assertEquals(true, req.isValid);
        assertEquals("10.0.0.27",req.domain);
        assertEquals("5f7d471e26450c1539fe73b7867a789abb0c7de6f4246f1e719d7b2830e73de2", req.getNonce());
        assertTrue(req.getReturnURL().startsWith("http"));

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


    public void testSalsa()  throws GeneralSecurityException
    {
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

        String result2 = Helper.bytesToHex(Helper.PK(password, Helper.hexStringToByteArray(saltStr), iterations, new byte[]{}, N));


        assertEquals(expectedReuslt, result2);
    }

    public void testSalsa2()  throws GeneralSecurityException
    {
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
            String result2 = Helper.bytesToHex(Helper.PK(password, Helper.hexStringToByteArray(saltStr), iterations, new byte[]{}, N));
            android.util.Log.d("test",String.format("Pasword: %s",result2));
        }


        //assertEquals(expectedReuslt, result2);
    }

    public void testByteUnPackSQRLData() throws IOException,GeneralSecurityException
    {
        String password= "tttttttttttttttttttttttt";

        String sqrlData ="SQRLDATAnQABAC0AjAIFnNpAdZDUjrMFYgB9yEeKaTObYRtwRtaIeglVAAAA8QAEBQ8AZKlrEUYZ1CxIBjW-pRpmbCY3P4H9v99j16WrXI262DFZIP4kMGhqK7N05g6gQzcQdgiD72cqj5qHmKiiP88Thf0RSJD6aAvRcP3XNdpSglh4l1Fb-1nb-A4TiH3Tk0zR0bE0ZcqhUaj4M4ILu86KmEkAAgAqponTFyavyjhYUCECOHqSCU0AAAAt_s6hM4nMEk4xdmyQmd1Juojslag8I6cVb2ma4B3CpIBlLnDCVd066kaB9GjptRE";
        String sqrlBase = sqrlData.substring(8);
        byte[] hexResult = Helper.urlDecode(sqrlBase);

        SqrlData parsed = SqrlData.ExtractSqrlData(hexResult);

        String scrpytPassword ="5ada4327f5975b10e1667a2b4844576cb85f41a5d16e2163e440cb9bc8d9317a";
        String IV = "8c02059cda407590d48eb305";
        String tag ="fd114890fa680bd170fdd735da528258";
        int nfactor  = 512;
        int iteration = 85;
        String aad = "9d0001002d008c02059cda407590d48eb30562007dc8478a69339b611b7046d6887a0955000000f10004050f00";
        String Identity_MasterKey ="64a96b114619d42c480635bea51a666c26373f81fdbfdf63d7a5ab5c8dbad831";
        String Identity_LockKey ="5920fe2430686a2bb374e60ea0433710760883ef672a8f9a8798a8a23fcf1385";
        String scryptSalt ="62007dc8478a69339b611b7046d6887a";

        assertEquals(IV,Helper.bytesToHex(parsed.sqrlStorage.IV));
        assertEquals(scryptSalt,Helper.bytesToHex(parsed.sqrlStorage.ScryptSalt));

        assertEquals(Identity_LockKey,Helper.bytesToHex(parsed.sqrlStorage.IDLK));
        assertEquals(Identity_MasterKey,Helper.bytesToHex(parsed.sqrlStorage.IDMK));
        assertEquals(tag,Helper.bytesToHex(parsed.sqrlStorage.tag));
        assertEquals(aad,Helper.bytesToHex(parsed.type1aad));
        assertEquals(nfactor, 1 << parsed.sqrlStorage.nFactor);
        assertEquals(iteration,parsed.sqrlStorage.ScryptIteration);
        assertEquals(Helper.hexStringToByteArray(scryptSalt).length,16);
        android.util.Log.d("test", String.format("Password: %s", Helper.bytesToHex(password.getBytes())));
        android.util.Log.d("test", String.format("Iterations: %d",parsed.sqrlStorage.ScryptIteration));
        byte[] scrypekey = Helper.PK(password.getBytes(), parsed.sqrlStorage.ScryptSalt, parsed.sqrlStorage.ScryptIteration, new byte[]{}, 1 << parsed.sqrlStorage.nFactor);
        android.util.Log.d("test", String.format("Result: %s",Helper.bytesToHex(scrypekey)));
        assertEquals(scrpytPassword,Helper.bytesToHex(scrypekey));

    }

    //https://www.grc.com/x/news.exe?cmd=article&group=grc.sqrl&item=12636&utag=
    public void testSqrlData2_adam() throws IOException,GeneralSecurityException
    {
        AESGCMJni4 crypto = new AESGCMJni4();
        String thePass ="the password";
        String sqrlData ="SQRLDATAfQABAC0Abouu7IvEI_1qknlaakonT7Lm4PWRwle2tKfXMQkEAAAA8QAE" +
                "AQ8Au7C0-pN8wxqzf-Qx0XUTZMXq3dS7brDwEaBTdwqsRkKi-mE_mV9UVHCe6sj1" +
                "kMTYrqPsDNflap_jD_K5-8_dMPLoB0pnbp9MqPXuzlXEe5dJAAIAUWdkk7EVnS3B" +
                "wXMVJkUU7AnoAAAA6bvMz939Yf_CkJTcd7tPg9qecHjC5n4tcnjO1PP5yqpEHr9C" +
                "7SxCZ0cQ-UcdhAVmlAADAKz22QR9avOQMyOEJ3V6G_f3uhLgtg-T3_DNONeWVlI7" +
                "PZcYwYsY_aDc_ZcpBb4L91Dv5xBtPgN2owc93O9OlSr0Rhs8unMNgDy809SomAnl" +
                "HrTz6oOg6Y-Cz8glP5kRcC8RpTIQugCCr8KkvhzEgydtC4aDMlFn3qqzykp8NkL3" +
                "QJUPoENfba3N4KZA8jbzpw";

        String sqrlBase = sqrlData.substring(8);
        byte[] hexResult = Helper.urlDecode(sqrlBase);

        SqrlData parsed = SqrlData.ExtractSqrlData(hexResult);

        String scrpytPassword ="2a1c f64c 099c 8383 357d 11a6 18f9 5299 26c7 79e0 3b41 2043 bf24 3d67 894c 6b33".replace(" ","");
        assertEquals("6e8baeec8bc423fd6a92795a", Helper.bytesToHex(parsed.sqrlStorage.IV));
        assertEquals("6a4a274fb2e6e0f591c257b6b4a7d731",Helper.bytesToHex(parsed.sqrlStorage.ScryptSalt));
        assertEquals(4, parsed.sqrlStorage.ScryptIteration);
        assertEquals("f2e8 074a 676e 9f4c a8f5 eece 55c4 7b97".replace(" ", ""), Helper.bytesToHex(parsed.sqrlStorage.tag));
        assertEquals("bbb0 b4fa 937c c31a b37f e431 d175 1364c5ea ddd4 bb6e b0f0 11a0 5377 0aac 4642".replace(" ",""),Helper.bytesToHex(parsed.sqrlStorage.IDMK));
        assertEquals("a2fa 613f 995f 5454 709e eac8 f590 c4d8 aea3 ec0c d7e5 6a9f e30f f2b9 fbcf dd30".replace(" ",""), Helper.bytesToHex(parsed.sqrlStorage.IDLK));
        byte[] scrypekey = Helper.PK(thePass.getBytes(), parsed.sqrlStorage.ScryptSalt, parsed.sqrlStorage.ScryptIteration, new byte[]{}, 1 << parsed.sqrlStorage.nFactor);
        assertEquals(scrpytPassword,Helper.bytesToHex(scrypekey));
        String combinedCipher = Helper.bytesToHex(parsed.sqrlStorage.IDMK)+Helper.bytesToHex(parsed.sqrlStorage.IDLK);
        String mk =
                crypto.doDecryption(
                        scrypekey,
                        parsed.sqrlStorage.IV,
                        parsed.type1aad,
                        parsed.sqrlStorage.tag,
                        Helper.hexStringToByteArray(combinedCipher));

        String pt_idmk= "8156 8e9d ab3a 5da2 9633 f9df 4bd3 247b 1d1d 8b55 0850 43cd a707 5d3d 0376 a25d";
        String pt_idlk ="1e14 8232 c33a 0af5 a5d2 19f3 7004 b696 a898 e8db d585 7456 2ef9 8289 e944 7610";
        String pt = pt_idmk+pt_idlk;
        assertEquals(mk, pt.replace(" ", ""));
    }

    public void testByteUnPackSQRLData_forum_adam() throws IOException,GeneralSecurityException
    {
        String password ="the password";

        String sqrlData ="SQRLDATAfQABAC0Abouu7IvEI_1qknlaakonT7Lm4PWRwle2tKfXMQkEAAAA8QAEAQ8Au7C0" +
                "-pN8wxqzf-Qx0XUTZMXq3dS7brDwEaBTdwqsRkKi-mE_mV9UVHCe6sj1kMTYrqPsDNflap_j" +
                "D_K5-8_dMPLoB0pnbp9MqPXuzlXEe5dJAAIAUWdkk7EVnS3BwXMVJkUU7AnoAAAA6bvMz939" +
                "Yf_CkJTcd7tPg9qecHjC5n4tcnjO1PP5yqpEHr9C7SxCZ0cQ-UcdhAVmlAADAKz22QR9avOQ" +
                "MyOEJ3V6G_f3uhLgtg-T3_DNONeWVlI7PZcYwYsY_aDc_ZcpBb4L91Dv5xBtPgN2owc93O9O" +
                "lSr0Rhs8unMNgDy809SomAnlHrTz6oOg6Y-Cz8glP5kRcC8RpTIQugCCr8KkvhzEgydtC4aD" +
                "MlFn3qqzykp8NkL3QJUPoENfba3N4KZA8jbzpw";

        String sqrlBase = sqrlData.substring(8);
        byte[] hexResult = Helper.urlDecode(sqrlBase);

        SqrlData parsed = SqrlData.ExtractSqrlData(hexResult);

        int nfactor  = 512;
        int iteration = 4;
        String pIUK = Helper.bytesToHex(parsed.type3SqrlData.encryptedpIUK);
        String eNOIUK = Helper.bytesToHex(parsed.type3SqrlData.encryptedNOIUK);

        String Identity_MasterKey ="81568e9dab3a5da29633f9df4bd3247b1d1d8b55085043cda7075d3d0376a25d";
        String Identity_LockKey ="1e148232c33a0af5a5d219f37004b696a898e8dbd58574562ef98289e9447610";
        String IUK ="beb9f19c8ca4eb1766bcf7c00876faa0dfa060e162284f686098408debdafa6c";

        assertEquals(nfactor, 1 << parsed.sqrlStorage.nFactor);
        assertEquals(iteration, parsed.sqrlStorage.ScryptIteration);

        android.util.Log.d("test", String.format("Password: %s", Helper.bytesToHex(password.getBytes())));
        android.util.Log.d("test", String.format("Iterations: %d", parsed.sqrlStorage.ScryptIteration));

        byte[] scrypekey =
                Helper.PK(password.getBytes(),
                        parsed.sqrlStorage.ScryptSalt,
                        parsed.sqrlStorage.ScryptIteration,
                        new byte[]{},
                        1 << parsed.sqrlStorage.nFactor);

        AESGCMJni4 crypto = new AESGCMJni4();

        String combinedCipher = Helper.bytesToHex(parsed.sqrlStorage.IDMK)+Helper.bytesToHex(parsed.sqrlStorage.IDLK);
        String mk =
                crypto.doDecryption(
                        scrypekey,
                        parsed.sqrlStorage.IV,
                        parsed.type1aad,
                        parsed.sqrlStorage.tag,
                        Helper.hexStringToByteArray(combinedCipher));
        /*
        byte[] scrypekey_type2 =
                Helper.PK("488778072850674671909820".getBytes(),
                        parsed.type2SqrlData.ScryptSalt,
                        parsed.type2SqrlData.ScryptIteration,
                        new byte[]{},
                        1 << parsed.type2SqrlData.nFactor);

        */
        byte[] scrypekey_type2 =Helper.hexStringToByteArray("ae568d701503d3beadbd92c6c9a158bc65840200b1f28467c5679860d7ccf46e");
        assertEquals("ae56 8d70 1503 d3be adbd 92c6 c9a1 58bc 6584 0200 b1f2 8467 c567 9860 d7cc f46e".replace(" ", ""), Helper.bytesToHex(scrypekey_type2));
        String IDUK =crypto.doDecryption(scrypekey_type2,Helper.hexStringToByteArray("000000000000000000000000"), parsed.type2aad, parsed.type2SqrlData.tag, parsed.type2SqrlData.IDUK);

        android.util.Log.d("test", String.format("uIDUK: %s", IDUK));

        String type3String = Helper.bytesToHex(parsed.type3SqrlData.encryptedpIUK)+
                Helper.bytesToHex(parsed.type3SqrlData.encryptedNOIUK)+
                Helper.bytesToHex(parsed.type3SqrlData.encryptedNNOIUK)+
                Helper.bytesToHex(parsed.type3SqrlData.encryptedOPIUK);
        byte[] uIMK=Helper.hexStringToByteArray(mk.substring(0,64));
        String pKeys =crypto.doDecryption(uIMK,Helper.hexStringToByteArray("000000000000000000000000"),parsed.type3aad, parsed.type3SqrlData.tag,Helper.hexStringToByteArray(type3String));

        assertTrue(Helper.determineAuth(pKeys));
        assertTrue(Helper.determineAuth(IDUK));
        android.util.Log.d("test", String.format("previous keys: %s", pKeys));
    }

    public void testBytePackSQRLData() throws IOException,GeneralSecurityException
    {
        String password= "tttttttttttttttttttttttt";
        String randomExpected =Helper.bytesToHex(Helper.CreateRandom(32));

        String sqrlData ="SQRLDATAnQABAC0AjAIFnNpAdZDUjrMFYgB9yEeKaTObYRtwRtaIeglVAAAA8QAEBQ8AZKlrEUYZ1CxIBjW-pRpmbCY3P4H9v99j16WrXI262DFZIP4kMGhqK7N05g6gQzcQdgiD72cqj5qHmKiiP88Thf0RSJD6aAvRcP3XNdpSglh4l1Fb-1nb-A4TiH3Tk0zR0bE0ZcqhUaj4M4ILu86KmEkAAgAqponTFyavyjhYUCECOHqSCU0AAAAt_s6hM4nMEk4xdmyQmd1Juojslag8I6cVb2ma4B3CpIBlLnDCVd066kaB9GjptRE";
        String sqrlBase = sqrlData.substring(8);
        byte[] hexResult = Helper.urlDecode(sqrlBase);

        SqrlData parsed = SqrlData.ExtractSqrlData(hexResult);

        String result_hex = Helper.bytesToHex(SqrlData.WriteSqrlData(parsed));

        assertTrue("Re-encoded packet does not look the same!",Helper.bytesToHex(hexResult).contains(result_hex));

        parsed.sqrlStorage.IDMK = Helper.hexStringToByteArray(randomExpected);

        result_hex = Helper.bytesToHex(SqrlData.WriteSqrlData(parsed));

        assertFalse("We changed the packet but it is still the same!",Helper.bytesToHex(hexResult).contains(result_hex));

        SqrlData parsed2 = SqrlData.ExtractSqrlData(Helper.hexStringToByteArray(result_hex));

        assertNotSame("Datum are still the same even after changing the IDMK!",parsed.sqrlStorage.IDMK,parsed2.sqrlStorage.IDMK);
        assertEquals("Datum does not contain the updated value!",Helper.bytesToHex(parsed2.sqrlStorage.IDMK),randomExpected);

    }

    public void testEnhash()
    {
        String expectedEnhash=" 8156 8e9d ab3a 5da2 9633 f9df 4bd3 247b 1d1d 8b55 0850 43cd a707 5d3d 0376 a25d".replace(" ","");
        String password ="beb9 f19c 8ca4 eb17 66bc f7c0 0876 faa0 dfa0 60e1 6228 4f68 6098 408d ebda fa6c".replace(" ","");
        byte[] actualHash = Helper.EnHash(Helper.hexStringToByteArray(password),16);
        assertEquals(expectedEnhash,Helper.bytesToHex(actualHash));
    }

    public void testSQRLData_createSimpleIdentity() throws IOException,GeneralSecurityException,JSONException
    {
        AESGCMJni4 aescrypto = new AESGCMJni4();
        String password= "tt";

        String sqrlData ="SQRLDATAnQABAC0AjAIFnNpAdZDUjrMFYgB9yEeKaTObYRtwRtaIeglVAAAA8QAEBQ8AZKlrEUYZ1CxIBjW-pRpmbCY3P4H9v99j16WrXI262DFZIP4kMGhqK7N05g6gQzcQdgiD72cqj5qHmKiiP88Thf0RSJD6aAvRcP3XNdpSglh4l1Fb-1nb-A4TiH3Tk0zR0bE0ZcqhUaj4M4ILu86KmEkAAgAqponTFyavyjhYUCECOHqSCU0AAAAt_s6hM4nMEk4xdmyQmd1Juojslag8I6cVb2ma4B3CpIBlLnDCVd066kaB9GjptRE";
        String sqrlBase = sqrlData.substring(8);
        byte[] hexResult = Helper.urlDecode(sqrlBase);

        SqrlData parsed = SqrlData.ExtractSqrlData(hexResult);

        byte[] randomKey = Helper.CreateRandom(32);
        parsed.sqrlStorage.ScryptIteration =2;
        parsed.sqrlStorage.IV = Helper.CreateRandom(12);
        parsed.sqrlStorage.ScryptSalt =Helper.CreateRandom(16);
        parsed.sqrlStorage.nFactor =9;

        byte[] newSqrlData = SqrlData.WriteSqrlData(parsed);
        SqrlData parsed2 = SqrlData.ExtractSqrlData(newSqrlData);

        byte[] scryptPassword = Helper.PK(password.getBytes(),parsed.sqrlStorage.ScryptSalt,parsed.sqrlStorage.ScryptIteration,new byte[]{},1<<parsed.sqrlStorage.nFactor);
        android.util.Log.d("test",String.format("UNEncrypted Shareable IMK: %s",Helper.bytesToHex(randomKey)));
        String cryptoResult =aescrypto.doEncryption(scryptPassword, parsed.sqrlStorage.IV, parsed2.type1aad, new byte[16], randomKey);
        //TODO there is probably a less messy way of doing this
        JSONObject object = new JSONObject(cryptoResult);

        parsed2.sqrlStorage.IDMK= Helper.hexStringToByteArray(object.getString("CipherText"));
        parsed2.sqrlStorage.tag = Helper.hexStringToByteArray(object.getString("Tag"));

        android.util.Log.d("test",String.format("Scrypt Password: %s",Helper.bytesToHex(scryptPassword)));
        android.util.Log.d("test",String.format("Encrypted Shareable IMK: %s",object.getString("CipherText")));
        android.util.Log.d("test",String.format("Tag: %s",object.getString("Tag")));
        android.util.Log.d("test",String.format("Iv: %s",Helper.bytesToHex(parsed.sqrlStorage.IV)));
        android.util.Log.d("test", String.format("Salt: %s",Helper.bytesToHex(parsed.sqrlStorage.ScryptSalt)));

        android.util.Log.d("test", SqrlData.ExportSqrlData(parsed2));

    }

    public void testBytePackSQRLData_changePassword() throws IOException,GeneralSecurityException
    {
        String password= "tt";

        String sqrlData ="SQRLDATAnQABAC0AjAIFnNpAdZDUjrMFYgB9yEeKaTObYRtwRtaIeglVAAAA8QAEBQ8AZKlrEUYZ1CxIBjW-pRpmbCY3P4H9v99j16WrXI262DFZIP4kMGhqK7N05g6gQzcQdgiD72cqj5qHmKiiP88Thf0RSJD6aAvRcP3XNdpSglh4l1Fb-1nb-A4TiH3Tk0zR0bE0ZcqhUaj4M4ILu86KmEkAAgAqponTFyavyjhYUCECOHqSCU0AAAAt_s6hM4nMEk4xdmyQmd1Juojslag8I6cVb2ma4B3CpIBlLnDCVd066kaB9GjptRE";
        String sqrlBase = sqrlData.substring(8);
        byte[] hexResult = Helper.urlDecode(sqrlBase);

        SqrlData parsed = SqrlData.ExtractSqrlData(hexResult);

        parsed.sqrlStorage.IDMK = Helper.CreateRandom(32);


        android.util.Log.d("test", Helper.bytesToHex(SqrlData.WriteSqrlData(parsed)));

    }


    /*
        Trying different combinations of pk parameters to get packet to decrypt properly
     */
    public void testEncryptAADUnencrypt_1() throws JSONException ,IOException, GeneralSecurityException
    {
        AESGCMJni4 crypto = new AESGCMJni4();
        String password= "tttttttttttttttttttttttt";

        String sqrlData ="SQRLDATAnQABAC0AjAIFnNpAdZDUjrMFYgB9yEeKaTObYRtwRtaIeglVAAAA8QAEBQ8AZKlrEUYZ1CxIBjW-pRpmbCY3P4H9v99j16WrXI262DFZIP4kMGhqK7N05g6gQzcQdgiD72cqj5qHmKiiP88Thf0RSJD6aAvRcP3XNdpSglh4l1Fb-1nb-A4TiH3Tk0zR0bE0ZcqhUaj4M4ILu86KmEkAAgAqponTFyavyjhYUCECOHqSCU0AAAAt_s6hM4nMEk4xdmyQmd1Juojslag8I6cVb2ma4B3CpIBlLnDCVd066kaB9GjptRE";
        String sqrlBase = sqrlData.substring(8);
        byte[] hexResult = Helper.urlDecode(sqrlBase);

        SqrlData parsed = SqrlData.ExtractSqrlData(hexResult);

        String sixteenZeros ="00000000000000000000000000000000";
        String scrpytPassword ="5ada4327f5975b10e1667a2b4844576cb85f41a5d16e2163e440cb9bc8d9317a";

        String decryptResult = crypto.doDecryption(AESGCMJni4.hexStringToByteArray(scrpytPassword),
                parsed.sqrlStorage.IV,
                parsed.type1aad,
                parsed.sqrlStorage.tag,
                parsed.sqrlStorage.IDMK);

        android.util.Log.d("test", String.format("ResultDecryption: %s", decryptResult));
        assertFalse(Helper.determineAuth(decryptResult));

        byte[] anotherResult = Helper.PK(password.getBytes(),
                Helper.hexStringToByteArray(sixteenZeros+Helper.bytesToHex(parsed.sqrlStorage.ScryptSalt)),
                parsed.sqrlStorage.ScryptIteration,
                new byte[]{},
                1 << parsed.sqrlStorage.nFactor);

        decryptResult = crypto.doDecryption(anotherResult,
                parsed.sqrlStorage.IV,
                parsed.type1aad,
                parsed.sqrlStorage.tag,
                parsed.sqrlStorage.IDMK);

        android.util.Log.d("test", String.format("ResultDecryption: %s", decryptResult));
        assertFalse(Helper.determineAuth(decryptResult));

        anotherResult = Helper.PK(password.getBytes(),
                Helper.hexStringToByteArray(Helper.bytesToHex(parsed.sqrlStorage.ScryptSalt)+sixteenZeros),
                parsed.sqrlStorage.ScryptIteration,
                new byte[]{},
                1 << parsed.sqrlStorage.nFactor);

        decryptResult = crypto.doDecryption(anotherResult,
                parsed.sqrlStorage.IV,
                parsed.type1aad,
                parsed.sqrlStorage.tag,
                parsed.sqrlStorage.IDMK);

        android.util.Log.d("test", String.format("ResultDecryption: %s", decryptResult));
        assertFalse(Helper.determineAuth(decryptResult));
    }

    public void testByteUnPackSQRLData_Unencrypt() throws IOException,GeneralSecurityException
    {
        String password= "tttttttttttttttttttttttt";

        String sqrlData ="SQRLDATAnQABAC0AjAIFnNpAdZDUjrMFYgB9yEeKaTObYRtwRtaIeglVAAAA8QAEBQ8AZKlrEUYZ1CxIBjW-pRpmbCY3P4H9v99j16WrXI262DFZIP4kMGhqK7N05g6gQzcQdgiD72cqj5qHmKiiP88Thf0RSJD6aAvRcP3XNdpSglh4l1Fb-1nb-A4TiH3Tk0zR0bE0ZcqhUaj4M4ILu86KmEkAAgAqponTFyavyjhYUCECOHqSCU0AAAAt_s6hM4nMEk4xdmyQmd1Juojslag8I6cVb2ma4B3CpIBlLnDCVd066kaB9GjptRE";
        String sqrlBase = sqrlData.substring(8);
        byte[] hexResult = Helper.urlDecode(sqrlBase);

        SqrlData parsed = SqrlData.ExtractSqrlData(hexResult);

        String scrpytPassword ="5ada4327f5975b10e1667a2b4844576cb85f41a5d16e2163e440cb9bc8d9317a";
        String IV = "8c02059cda407590d48eb305";
        String tag ="fd114890fa680bd170fdd735da528258";
        int nfactor  = 512;
        int iteration = 85;
        String aad = "9d0001002d008c02059cda407590d48eb30562007dc8478a69339b611b7046d6887a0955000000f10004050f00";
        String Identity_MasterKey ="64a96b114619d42c480635bea51a666c26373f81fdbfdf63d7a5ab5c8dbad831";
        String Identity_LockKey ="5920fe2430686a2bb374e60ea0433710760883ef672a8f9a8798a8a23fcf1385";
        String scryptSalt ="62007dc8478a69339b611b7046d6887a";

        assertEquals(IV,Helper.bytesToHex(parsed.sqrlStorage.IV));
        assertEquals(scryptSalt,Helper.bytesToHex(parsed.sqrlStorage.ScryptSalt));

        assertEquals(Identity_LockKey,Helper.bytesToHex(parsed.sqrlStorage.IDLK));
        assertEquals(Identity_MasterKey,Helper.bytesToHex(parsed.sqrlStorage.IDMK));
        assertEquals(tag,Helper.bytesToHex(parsed.sqrlStorage.tag));
        assertEquals(aad, Helper.bytesToHex(parsed.type1aad));
        assertEquals(nfactor, 1 << parsed.sqrlStorage.nFactor);
        assertEquals(iteration, parsed.sqrlStorage.ScryptIteration);
        assertEquals(Helper.hexStringToByteArray(scryptSalt).length, 16);
        android.util.Log.d("PK", String.format("Password: %s", Helper.bytesToHex(password.getBytes())));
        android.util.Log.d("PK", String.format("Iterations: %d", parsed.sqrlStorage.ScryptIteration));
        byte[] scrypekey = Helper.PK(password.getBytes(), parsed.sqrlStorage.ScryptSalt, parsed.sqrlStorage.ScryptIteration, new byte[]{}, 1 << parsed.sqrlStorage.nFactor);
        android.util.Log.d("PK", String.format("Result: %s", Helper.bytesToHex(scrypekey)));

        AESGCMJni4 crypto = new AESGCMJni4();
        String fullPt = Helper.bytesToHex(parsed.sqrlStorage.IDMK) + Helper.bytesToHex(parsed.sqrlStorage.IDLK);
        String result = crypto.doDecryption(scrypekey, parsed.sqrlStorage.IV, parsed.type1aad, parsed.sqrlStorage.tag,Helper.hexStringToByteArray( fullPt));
        android.util.Log.d("test", String.format("ResultDecryption: %s", result));

        assertEquals(scrpytPassword, Helper.bytesToHex(scrypekey));


        //This test  will fail because we still haven't fully understood how to decode a SQRL packet yet :(
        assertTrue(Helper.determineAuth(result));

    }

    public void testEncryptAADUnencrypt() throws JSONException {

        AESGCMJni4 crypto = new AESGCMJni4();
        String scrpytPassword ="5ada4327f5975b10e1667a2b4844576cb85f41a5d16e2163e440cb9bc8d9317a";
        String IV = "8c02059cda407590d48eb305";
        String aad = "9d0001002d008c02059cda407590d48eb30562007dc8478a69339b611b7046d6887a0955000000f10004050f00";
        byte[] randomKey = Helper.CreateRandom(32);
        android.util.Log.d("test", String.format("RandomKey: %s", AESGCMJni4.bytesToHex(randomKey)));
        String result = crypto.doEncryption(AESGCMJni4.hexStringToByteArray(scrpytPassword), AESGCMJni4.hexStringToByteArray(IV), AESGCMJni4.hexStringToByteArray(aad), new byte[]{}, randomKey);
        JSONObject object = new JSONObject(result);

        String decryptResult = crypto.doDecryption(AESGCMJni4.hexStringToByteArray(scrpytPassword),
                AESGCMJni4.hexStringToByteArray(IV),
                AESGCMJni4.hexStringToByteArray(aad),
                AESGCMJni4.hexStringToByteArray(object.getString("Tag")),
                AESGCMJni4.hexStringToByteArray(object.getString("CipherText")));

        android.util.Log.d("test", String.format("ResultDecryption: %s", decryptResult));
        assertEquals(AESGCMJni4.bytesToHex(randomKey),decryptResult.toUpperCase());

    }

    public void testHMAC()
    {
        String domain ="www.example.com";
        byte[] randomKey = new byte[32];
        Sodium.randombytes_buf(randomKey, 32);
        String current = Helper.bytesToHex(Helper.CreatePrivateHMAC(domain, randomKey));
        String sodium2 = Helper.bytesToHex(Helper.CreatePrivateKey(domain, randomKey));

        assertEquals(current,sodium2);
    }

    public void testSHA256()
    {
        byte[] randomKey = Helper.CreateRandom(32);

        String current = Helper.bytesToHex(crypto.sha256(randomKey));
        String sodium2 = Helper.bytesToHex(Helper.SHA256(randomKey));

        assertEquals(current, sodium2);

    }
    /*
        This test will fail when we decrypt a packet.
     */
    public void testFindRightParms() throws JSONException
    {
        AESGCMJni4 aescrypto = new AESGCMJni4();
        String scrpytPassword ="1b10108c15591493aad2bf363877ff7b079361ff0551ba3eb87e17106f59861e";
        String IV = "c32b48ad60f81a5256a977bb";
        String aad = "9d0001002d00c32b48ad60f81a5256a977bbbb010194d66d890a372dd9f2a0a6f460094d000000f10004050f00";
        String eIDMK ="c0120b09e3b1f383bcb2326f99710701156434383b614ed2b3a17b03a5f50ba2";
        String uIDMK =aescrypto.doDecryption(
                Helper.hexStringToByteArray(scrpytPassword),
                Helper.hexStringToByteArray(IV),
                Helper.hexStringToByteArray(aad),
                new byte[]{}, //no tag
                Helper.hexStringToByteArray(eIDMK));

        //Is the verification tag the tail of the hash or the AES tag?
        String vTag ="f48476adda8e21290383ed5100b705c9";

        String result = Helper.massXOR("1b10108c15591493aad2bf363877ff7b079361ff0551ba3eb87e17106f59861e",eIDMK);
        byte[] hashed =Helper.SHA256(Helper.hexStringToByteArray(result));
        String hashed_result =Helper.bytesToHex(hashed);
        android.util.Log.d("test",String.format("hashed: %s",hashed_result));
        assertFalse(hashed_result.contains(vTag));

        result = Helper.massXOR("1b10108c15591493aad2bf363877ff7b079361ff0551ba3eb87e17106f59861e",uIDMK);
        hashed =Helper.SHA256(Helper.hexStringToByteArray(result));
        hashed_result =Helper.bytesToHex(hashed);
        android.util.Log.d("test",String.format("hashed: %s",hashed_result));
        assertFalse(hashed_result.contains(vTag));
    }

    public void testSqrlData() throws IOException
    {
        String aad = "9d0001002d008c02059cda407590d48eb30562007dc8478a69339b611b7046d6887a0955000000f10004050f00";
        String Identity_MasterKey ="64a96b114619d42c480635bea51a666c26373f81fdbfdf63d7a5ab5c8dbad831";
        String sqrlData ="SQRLDATAnQABAC0AjAIFnNpAdZDUjrMFYgB9yEeKaTObYRtwRtaIeglVAAAA8QAEBQ8AZKlrEUYZ1CxIBjW-pRpmbCY3P4H9v99j16WrXI262DFZIP4kMGhqK7N05g6gQzcQdgiD72cqj5qHmKiiP88Thf0RSJD6aAvRcP3XNdpSglh4l1Fb-1nb-A4TiH3Tk0zR0bE0ZcqhUaj4M4ILu86KmEkAAgAqponTFyavyjhYUCECOHqSCU0AAAAt_s6hM4nMEk4xdmyQmd1Juojslag8I6cVb2ma4B3CpIBlLnDCVd066kaB9GjptRE";
        IdentityData data = new IdentityData("test","0000","",true);
        data.idContents = sqrlData.getBytes();

        SqrlData datapacket= IdentityData.LoadSqrlData(data);

        assertEquals(Helper.bytesToHex(datapacket.sqrlStorage.IDMK), Identity_MasterKey);
        assertEquals(Helper.bytesToHex(datapacket.type1aad), aad);

    }

    public void testSqrlData2_rawdata() throws IOException
    {
        String aad = "9d0001002d008c02059cda407590d48eb30562007dc8478a69339b611b7046d6887a0955000000f10004050f00";
        String Identity_MasterKey ="64a96b114619d42c480635bea51a666c26373f81fdbfdf63d7a5ab5c8dbad831";
        String sqrlData ="c3FybGRhdGGdAAEALQCMAgWc2kB1kNSOswViAH3IR4ppM5thG3BG1oh6CVUAAADxAAQFDwBkqWsRRhnULEgGNb6lGmZsJjc/gf2/32PXpatcjbrYMVkg/iQwaGors3TmDqBDNxB2CIPvZyqPmoeYqKI/zxOF/RFIkPpoC9Fw/dc12lKCWHiXUVv7Wdv4DhOIfdOTTNHRsTRlyqFRqPgzggu7zoqYSQACACqmidMXJq/KOFhQIQI4epIJTQAAAC3+zqEzicwSTjF2bJCZ3Um6iOyVqDwjpxVvaZrgHcKkgGUucMJV3TrqRoH0aOm1EQ==";
        IdentityData data = new IdentityData("test","0000","",true);
        data.idContents = Helper.urlDecode(sqrlData);

        SqrlData datapacket= IdentityData.LoadSqrlData(data);

        assertEquals(Helper.bytesToHex(datapacket.sqrlStorage.IDMK),Identity_MasterKey);
        assertEquals(Helper.bytesToHex(datapacket.type1aad),aad);

    }

    public void testUrlEncode() {

        String urlOneEqul2 = "aa";
        String urlOneEqul1 = "aaa";
        String urlOneEqul0 = "aaaa";
        String test1 = Helper.bytesToHex(Helper.urlDecode(urlOneEqul2));
        String test2 = Helper.bytesToHex(Helper.urlDecode(urlOneEqul1));
        String test3 = Helper.bytesToHex(Helper.urlDecode(urlOneEqul0));

        assertEquals("69",test1);
        assertEquals("69a6",test2);
        assertEquals("69a69a",test3);
    }

}