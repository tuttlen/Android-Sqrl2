package com.tuttlen.aesgcm_android;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void  testAesGcm() {
        AESGCMJni4.main(new String[]{"encrpyt",
                "647e78c420786e149d7e75e877e0e89d4fac498159551e5e2d23dc98f745078f",
                "8c02059cda407590d48eb305",
                "9d0001002d008c02059cda407590d48eb30562007dc8478a69339b611b7046d6887a0955000000f10004050f00",
                "aed31cfac79793881a379fa3709a5ceb",
                "64a96b114619d42c480635a946999b098dcfe07f6ff7d8f5e96ad7236eb60c56"});

        //c6581cc62bca0e38682a2e7c67410a4d8cd1c81628caa7fb79eb5463ad14b077	);
    }

    public void testAesGCm2()
    {
        AESGCMJni4 crypto = new AESGCMJni4();
        byte[] key = AESGCMJni4.hexStringToByteArray("5ada4327f5975b10e1667a2b4844576cb85f41a5d16e2163e440cb9bc8d9317a");
        byte[] IV =AESGCMJni4.hexStringToByteArray("8c02059cda407590d48eb305");
        byte[] aad =AESGCMJni4.hexStringToByteArray("9d0001002d008c02059cda407590d48eb30562007dc8478a69339b611b7046d6887a0955000000f10004050f00");
        byte[] tag =AESGCMJni4.hexStringToByteArray("4452243e9a02f45c3f75cd7694a0961e");
        byte[] cipherText =AESGCMJni4.hexStringToByteArray("64a96b114619d42c480635a946999b098dcfe07f6ff7d8f5e96ad7236eb60c56483f890c1a1a8aecdd3983a810cdc41d8220fbd9caa3e6a1e62a288ff3c4e17f4452243e9a02f45c3f75cd7694a0961e25d456f59db0384e21f74e4d334746c4d1972a8546a3e0ce082eef3a2a6124000800aa9a274c5c9abf28e161408408e1ea482534000000b6cea13389cc124e31766c9099dd49ba88ec95a83c23a7156f699ae01dc2a480652e70c255dd3aea4681f468e9b511");

        String testResult = crypto.doDecryption(key, IV, aad, tag, cipherText);
        boolean isAllZeros = true;
        byte[] result = AESGCMJni4.hexStringToByteArray(testResult);
        for (int i = 0; i <result.length; i++) {
            isAllZeros &= result[i] == 0;
        }
        android.util.Log.d("AES", "testAesGCm2: "+testResult);
        assertTrue("The encrypted message will not be authenticated by this block",isAllZeros);
    }

}