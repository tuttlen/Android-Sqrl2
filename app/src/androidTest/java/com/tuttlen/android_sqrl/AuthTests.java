package com.tuttlen.android_sqrl;

import android.test.InstrumentationTestCase;

/**
 * Created by nathan on 3/31/16.
 */
public class AuthTests extends InstrumentationTestCase {

    public void testAuth() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("https://localhost/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals("localhost", testReuslt);
        req.fullNut =false;
        assertEquals( "https://localhost/sqrl",req.getReturnURL());
    }
    public void testAuth2() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("http://localhost/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals(testReuslt, "localhost");
        req.fullNut =false;
        assertEquals( "http://localhost/sqrl",req.getReturnURL());
    }

    public void testAuth3() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("https://example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals("example.com",testReuslt);
        req.fullNut =false;
        assertEquals("https://example.com/sqrl",req.getReturnURL());
    }

    public void testAuth3_wqithsqrl() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("sqrl://example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals("example.com",testReuslt);
        //if we understand how to construct a sqrl then we should expect to understand how to handle the protocol
        req.isConnectionPicky=false;
        req.fullNut =false;
        assertEquals("sqrl://example.com/sqrl",req.getReturnURL());
    }

    public void testAuth3_wqithsqrl_picky() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("sqrl://example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals("example.com",testReuslt);
        //if we understand how to construct a sqrl then we should expect to understand how to handle the protocol
        req.isConnectionPicky=true;
        req.fullNut =false;
        assertEquals("https://example.com/sqrl",req.getReturnURL());
    }

    public void testAuth3_nonce() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("sqrl://example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getNonce();
        assertEquals("4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4",testReuslt);
    }

    public void testAuth_malformed_url() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("example.com/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        //There is no need to be this forgiving. Noone would expect it and I see no benefit in leaving it out
        assertEquals(false,req.IsValid());
    }

    public void testAuth_malformed_url2() throws Exception{

        AuthorizationRequest req = new AuthorizationRequest("10.0.0.2/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a");
        //There is no need to be this forgiving. Noone would expect it and I see no benefit in leaving it out
        assertEquals(false,req.IsValid());
    }

    public void testAuth_malformed_url3() throws Exception
    {
        AuthorizationRequest req = new AuthorizationRequest("10.0.0.2.5/sqrl?972764a6021a2649e9bbecfd52c36f13b30a260dbc5c373a53e9d7ae502d0c3a");
        //There is no need to be this forgiving. Noone would expect it and I see no benefit in leaving it out
        assertEquals(false,req.IsValid());
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

    public void testSqrlWithSFN()
    {
        String sqrlAddress ="sqrl://www.grc.com/sqrl?nut=uogovZVHZtJPMorOw4RHgw&sfn=R1JD";
        AuthorizationRequest aReq = new AuthorizationRequest(sqrlAddress);
        assertEquals("uogovZVHZtJPMorOw4RHgw",aReq.nonce);

    }

    public void testQRlAddressSignatureSend()
    {
        //qrl://10.0.0.27/login/sqrlauth.php?nut=5f7d471e26450c1539fe73b7867a789abb0c7de6f4246f1e719d7b2830e73de2
        String qrlAddress ="qrl://10.0.0.27/login/sqrlauth.php?nut=393cbc323070c8281e05bd8554f8d8d409cd9c64267f358cac41c121b1720299";
        AuthorizationRequest req = new AuthorizationRequest(qrlAddress);
        assertEquals(true, req.isValid);
        assertEquals("10.0.0.27",req.domain);
        assertEquals("393cbc323070c8281e05bd8554f8d8d409cd9c64267f358cac41c121b1720299", req.getNonce());
        assertTrue(req.getReturnURL().startsWith("http"));
        assertTrue(req.getReturnURL().endsWith("nut=393cbc323070c8281e05bd8554f8d8d409cd9c64267f358cac41c121b1720299"));
    }

    public void testQRLAddressNewNut()
    {
        //qrl://10.0.0.27/login/sqrlauth.php?nut=5f7d471e26450c1539fe73b7867a789abb0c7de6f4246f1e719d7b2830e73de2
        String qrlAddress ="qrl://10.0.0.27/login/sqrlauth.php?nut=393cbc323070c8281e05bd8554f8d8d409cd9c64267f358cac41c121b1720299";
        AuthorizationRequest req = new AuthorizationRequest(qrlAddress);
        assertEquals(true, req.isValid);
        assertEquals("10.0.0.27", req.domain);
        assertEquals("393cbc323070c8281e05bd8554f8d8d409cd9c64267f358cac41c121b1720299", req.getNonce());
        assertTrue(req.getReturnURL().startsWith("http"));
        assertTrue(req.getReturnURL().endsWith("nut=393cbc323070c8281e05bd8554f8d8d409cd9c64267f358cac41c121b1720299"));
        String newNut = Helper.urlEncode(Helper.CreateRandom(32));
        AuthorizationRequest newnutaddress = req.getNewNut(newNut);
        assertTrue(newnutaddress.getReturnURL().startsWith("http"));
        assertTrue(newnutaddress.getReturnURL().endsWith(newNut));
        assertEquals(String.format("http://10.0.0.27/login/sqrlauth.php?nut=%s",newNut),newnutaddress.getReturnURL());
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
}
