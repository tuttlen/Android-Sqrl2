package com.tuttlen.android_sqrl;

import android.test.InstrumentationTestCase;

import com.tuttlen.aesgcm_android.AESGCMJni4;

import org.abstractj.kalium.Sodium;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Created by tuttlen on 1/23/2016.
 */
public class CryptoTests extends InstrumentationTestCase {

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

        byte[] IMK = Helper.CreateRandom(32);
        byte[] IuK = Helper.CreateRandom(32);
        parsed.sqrlStorage.ScryptIteration =2;
        parsed.sqrlStorage.IV = Helper.CreateRandom(12);
        parsed.sqrlStorage.ScryptSalt =Helper.CreateRandom(16);
        parsed.sqrlStorage.nFactor =9;

        byte[] newSqrlData = SqrlData.WriteSqrlData(parsed);
        SqrlData parsed2 = SqrlData.ExtractSqrlData(newSqrlData);

        byte[] scryptPassword = Helper.PK(password.getBytes(),parsed.sqrlStorage.ScryptSalt,parsed.sqrlStorage.ScryptIteration,new byte[]{},1<<parsed.sqrlStorage.nFactor);
        android.util.Log.d("test", String.format("UNEncrypted Shareable IMK: %s", Helper.bytesToHex(IMK)));
        byte[] ILK = new byte[32];
        byte[] rP = new byte[64];

        ILK =Helper.CreatePrivateKeyFromSeed(IuK);
        //we require a 64 bit private key but we have to start from a random 32 byte value
        //so we generate this from a 32 byte seed this value is IuK
        ILK =Helper.PublicKeyFromPrivateKey(ILK);

        String cryptoResult =aescrypto.doEncryption(scryptPassword, parsed.sqrlStorage.IV, parsed2.type1aad, new byte[16], Helper.CombineBytes(IMK,ILK));
        //TODO there is probably a less messy way of doing this
        JSONObject object = new JSONObject(cryptoResult);
        byte[] combinedPlaintext =Helper.hexStringToByteArray(object.getString("CipherText"));
        ArrayList<Integer> sizeList = new ArrayList<Integer>();
        sizeList.add(32);
        sizeList.add(32);
        ArrayList<byte[]> values = Helper.UnCombineBytes(combinedPlaintext, sizeList);
        parsed2.sqrlStorage.IDMK= values.get(0);
        parsed2.sqrlStorage.IDLK= values.get(1);
        parsed2.sqrlStorage.tag = Helper.hexStringToByteArray(object.getString("Tag"));

        android.util.Log.d("test",String.format("Scrypt Password: %s",Helper.bytesToHex(scryptPassword)));
        android.util.Log.d("test",String.format("Encrypted Shareable IMK: %s",object.getString("CipherText")));
        android.util.Log.d("test",String.format("Tag: %s",object.getString("Tag")));
        android.util.Log.d("test",String.format("Iv: %s",Helper.bytesToHex(parsed.sqrlStorage.IV)));
        android.util.Log.d("test", String.format("Salt: %s", Helper.bytesToHex(parsed.sqrlStorage.ScryptSalt)));

        android.util.Log.d("test", SqrlData.ExportSqrlData(parsed2));

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
        android.util.Log.d("test", String.format("hashed: %s", hashed_result));
        assertFalse(hashed_result.contains(vTag));
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