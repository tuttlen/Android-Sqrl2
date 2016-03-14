package com.tuttlen.android_sqrl;

import android.test.InstrumentationTestCase;
import android.util.Base64;

import org.abstractj.kalium.Sodium;

//removed
//import com.github.dazoe.android.Ed25519;

import eu.artemisc.stodium.Stodium;

/**
 * Created by nathan tuttle on 3/14/16.
 *
 * leaving here for lagacy but these tests existed when ne android ed25519 library existed.
 * Replaced with Stodium
 */
public class Ed25519Tests extends InstrumentationTestCase {

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        Stodium.StodiumInit();
    }
    /*

    public void testEd25519ToSodoium() throws Exception
    {
        byte[] randomBytes = new byte[64];
        Sodium.randombytes_buf(randomBytes, 64);

        System.out.println(Helper.bytesToHex(randomBytes));

        byte[] edKey = Ed25519.PublicKeyFromPrivateKey(randomBytes);
        byte[] edkey2 = Helper.PublicKeyFromPrivateKey(randomBytes);
        String first = Helper.bytesToHex(edkey2);
        String libKey = Helper.bytesToHex(edKey);

        assertEquals(first, libKey);
    }

    public void testEd25519ToSodoium_fromKnown() throws Exception
    {
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
    */
    public void test_ed25519() throws Exception
    {
        //private
        //8385e4536a0c29d1c2c6d8befe741feb28d01a5722596362bb4487a7fd2f03120b387c5669ca6fa137a2a383521f15b6243979f741b828fc0851e98fcfeb026c

        //public
        //0b387c5669ca6fa137a2a383521f15b6243979f741b828fc0851e98fcfeb026c
        byte[] publicKey_check = Helper.hexStringToByteArray("0b387c5669ca6fa137a2a383521f15b6243979f741b828fc0851e98fcfeb026c");
        byte[] privateKey = Helper.hexStringToByteArray("8385e4536a0c29d1c2c6d8befe741feb28d01a5722596362bb4487a7fd2f03120b387c5669ca6fa137a2a383521f15b6243979f741b828fc0851e98fcfeb026c");
        byte[] publicKey = Helper.PublicKeyFromPrivateKey(privateKey);

        assertEquals(Helper.bytesToHex(publicKey_check), Helper.bytesToHex(publicKey));
    }

}
