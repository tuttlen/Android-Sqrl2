package com.tuttlen.android_sqrl;

import android.test.InstrumentationTestCase;

import com.tuttlen.aesgcm_android.AESGCMJni4;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by nathan on 3/31/16.
 */
public class SqrlDataTests extends InstrumentationTestCase {

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
        String pKeys =crypto.doDecryption(uIMK, Helper.hexStringToByteArray("000000000000000000000000"), parsed.type3aad, parsed.type3SqrlData.tag, Helper.hexStringToByteArray(type3String));
        assertEquals(mk.substring(0,64),Helper.EnHash(Helper.hexStringToByteArray(IDUK),16));
        assertTrue(Helper.determineAuth(pKeys));
        assertTrue(Helper.determineAuth(IDUK));
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

        assertTrue("Re-encoded packet does not look the same!", Helper.bytesToHex(hexResult).contains(result_hex));

        parsed.sqrlStorage.IDMK = Helper.hexStringToByteArray(randomExpected);

        result_hex = Helper.bytesToHex(SqrlData.WriteSqrlData(parsed));

        assertFalse("We changed the packet but it is still the same!", Helper.bytesToHex(hexResult).contains(result_hex));

        SqrlData parsed2 = SqrlData.ExtractSqrlData(Helper.hexStringToByteArray(result_hex));

        assertNotSame("Datum are still the same even after changing the IDMK!",parsed.sqrlStorage.IDMK,parsed2.sqrlStorage.IDMK);
        assertEquals("Datum does not contain the updated value!",Helper.bytesToHex(parsed2.sqrlStorage.IDMK),randomExpected);

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

        assertEquals(Helper.bytesToHex(datapacket.sqrlStorage.IDMK), Identity_MasterKey);
        assertEquals(Helper.bytesToHex(datapacket.type1aad),aad);

    }
}
