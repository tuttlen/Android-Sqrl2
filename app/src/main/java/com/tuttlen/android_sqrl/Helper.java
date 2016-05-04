package com.tuttlen.android_sqrl;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.android.internal.util.Predicate;
import com.igormaznitsa.jbbp.JBBPParser;
import com.igormaznitsa.jbbp.mapper.Bin;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayByte;
import com.igormaznitsa.jbbp.model.JBBPFieldStruct;

import org.abstractj.kalium.Sodium;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import eu.artemisc.stodium.Ed25519;
import eu.artemisc.stodium.GenericHash;
import eu.artemisc.stodium.RandomBytes;
//import eu.artemisc.stodium.ScalarMult;
import eu.artemisc.stodium.Stodium;
import eu.artemisc.stodium.StodiumException;

/**
 * Created by nathan tuttle on 3/9/16.
 *
 * This class will contain a central location for the crypto libraries and general use functions
 */
public class Helper {

    static
    {
        Stodium.StodiumInit();
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }

    //TODO move this to a helper class
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String massXOR(String first, String second) {
        return Helper.bytesToHex(Helper.massXOR(Helper.hexStringToByteArray(first), Helper.hexStringToByteArray(second)));
    }

    public static byte[] massXOR(byte[] first, byte[] second) {
        byte[] resultByte = new byte[first.length];
        for (int i = 0; i < first.length; i++) {
            resultByte[i] = (byte) (first[i] ^ second[i]);
        }
        return resultByte;
    }

    public static byte[] urlDecode(String target) {
        target = target.replace("-", "+").replace("_", "/");
        return Base64.decode(target, Base64.DEFAULT | Base64.NO_WRAP);
    }

    public static String urlEncode(byte[] target) {
        String base64 = Base64.encodeToString(target, Base64.DEFAULT| Base64.NO_WRAP);
        return base64.replace("/", "_").replace("+", "-").replace("=", "");

    }

    public static byte[] PK(byte[] password, byte[] salt, int iterations, byte[] previosResult, int N) throws GeneralSecurityException {
        return PK(password, salt, iterations, previosResult, N, null);
    }

    //PBKDF(2)
    public static byte[] PK(byte[] password, byte[] salt, int iterations, byte[] previosResult, int N, Predicate<Integer> run ) throws GeneralSecurityException {
        int p = 1;
        int r =256;
        byte[] result = new byte[32];
        Sodium.crypto_pwhash_scryptsalsa208sha256_ll(password, password.length, salt, salt.length, N, r, p, result, result.length);
        iterations--;

        if(run != null) run.apply(iterations);

        android.util.Log.d("PK", String.format("On iteration %d: %s", iterations, Helper.bytesToHex(result)));
        if (iterations == 0 && previosResult.length == 0) return result;
        if (iterations == 0) return Helper.massXOR(result, previosResult);
        if (previosResult.length == 0) {
            return PK(password, result, iterations, result, N);
        } else {
            return PK(password, result, iterations, Helper.massXOR(previosResult, result), N);
        }
    }

    // Create the private key from URL and secret key
    public static byte[] CreatePrivateKey(String domain, byte[] key) {
        byte[] hmac = null;
        try {
            SecretKeySpec pKey = new SecretKeySpec(key, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(pKey);
            hmac = mac.doFinal(domain.getBytes());
        } catch (Exception e) {
        }
        return hmac;
    }

    public static byte[] EnHash(byte[] password, int iterations)
    {
        byte[] xorresult = new byte[]{};
        byte[] nextResult = new byte[]{};
        byte[] result = Helper.SHA256(password);
        for (int i = 0; i < iterations ; i++) {
            nextResult = Helper.SHA256(result);
            if(xorresult.length ==0)
            {
                xorresult = Helper.massXOR(result,nextResult);
            } else {
                xorresult = Helper.massXOR(nextResult,xorresult);
            }
            result=nextResult;
        }
        return Helper.massXOR(nextResult, xorresult);
    }

    public static byte[] CreatePrivateHMAC(String domain, byte[] key)
    {
        byte[] messageBytes = domain.getBytes();
        byte[] out = new byte[32];
        byte[] publicKey = new byte[32];
        byte[] privatekey = new byte[64];

        Sodium.crypto_auth_hmacsha256(out, messageBytes, messageBytes.length, key);
        Sodium.crypto_sign_seed_keypair(publicKey, privatekey, out);
        return privatekey;
    }

    public static byte[] CreateRandom(int length) {
        byte[] randomValues = new byte[length];
        Sodium.randombytes_buf(randomValues, length);
        return  randomValues;
    }

    public static byte[] PublicKeyFromPrivateKey(byte[] privateKey)
    {
        byte[] publicKey = new byte[32];
        try {
            Ed25519.publicFromPrivate(publicKey, privateKey);
        } catch(StodiumException e)
        {}
        return publicKey;
    }

    public static byte[] Sign(byte[] message, byte[] privateKey){
        //A bug fixed by creating a signed message that is already 64 bytes larger then message
        byte[] signatureSignedMessage = new byte[64+message.length];
        byte[] signature = new byte[Ed25519.SIGNBYTES];

        //yuk we have to copy out the reuslt
        try {
            int size = Ed25519.signDetached(signature, message, privateKey);
        } catch(StodiumException e){}
        return signature;
    }

    public static byte[] CreatePrivateKeyFromSeed(byte[] random)
    {
        byte[] privateKey = new byte[64];
        byte[] publicKey = new byte[32];
        try {
            Ed25519.keypairSeed(publicKey, privateKey, random);
        } catch(StodiumException e){}
        return privateKey;
    }

    public static boolean Verify(byte[] sMessage,byte[] message,  byte[] publicKey)
    {
        try {
            return Ed25519.verifyDetached(sMessage, message, publicKey);
        } catch (StodiumException e){}
        return false;
    }

    public static byte[] SHA256(byte[] value)
    {
        byte[] outHash = new byte[32];
        Sodium.crypto_hash_sha256(outHash,value,32);
        return  outHash;
    }

    /**
     *
     * @param result - Result of decryption
     * @return - Whether the Encryption was successful
     */
    public static boolean determineAuth(String result) {
        byte[] hexResult = Helper.hexStringToByteArray(result);
        return Helper.OrContents(hexResult) >0 ;
    }

    /**
     * This function is useful for comparing buffers.
     * Use with massXOR and determineAuth
     * @param orCandidate - A set of values
     * @return The candidate orred together
     */
    public static int OrContents(byte[] orCandidate)
    {
        int orredByte =0;

        for (int i = 0; i < orCandidate.length; i++) {
            orredByte |= orCandidate[i];
        }

        return orredByte;

    }

    public static byte[] CombineBytes(byte[] imk, byte[] ilk)
    {
        byte[] returnBytes = new byte[imk.length+ilk.length];
        System.arraycopy(imk,0,returnBytes,0,imk.length);
        System.arraycopy(ilk, 0, returnBytes, imk.length, ilk.length);
        return returnBytes;
    }

    public static ArrayList<byte[]> UnCombineBytes(byte[] combinedValues,ArrayList<Integer> size)
    {
        int index =0;
        ArrayList<byte[]> returnBytes= new ArrayList<byte[]>();
        for (Integer item: size) {
            byte[] values = new byte[item];
            System.arraycopy(combinedValues, index, values, 0, item);
            returnBytes.add(values);
            index+=item;
        }
        return returnBytes;
    }

    /**
     *
     * @param publicKey - Public key
     * @param randomLock - Private key
     * @return - Shared secret
     */
    public static byte[] DHKA(byte[] publicKey, byte[] randomLock)
    {
        byte[] sharedSecret = new byte[32];
        //ScalarMult.scalarMult(sharedSecret,randomLock,publicKey);
        Sodium.crypto_scalarmult(sharedSecret,randomLock,publicKey);
        return sharedSecret;
    }

    /**
     *
     * @param publicKey - Public key
     * @param randomLock - Private key
     * @return - Shared secret
     */
    public static byte[] DHKA3(byte[] publicKey, byte[] randomLock) {
        byte[] publicKey_curve = new byte[32];

        byte[] randomLock_curve = new byte[64];
        byte[] sharedSecret = new byte[32];

        Sodium.crypto_sign_ed25519_pk_to_curve25519(publicKey_curve, publicKey);

        Sodium.crypto_sign_ed25519_sk_to_curve25519(randomLock_curve, randomLock);
        Sodium.crypto_scalarmult(sharedSecret, randomLock_curve, publicKey_curve);
        return sharedSecret;
    }



    /**
     *
     * @param publicKey - Public key
     * @param randomSeed - Private key
     * @return - Shared secret
     */
    public static byte[] DHKA2(byte[] publicKey, byte[] randomSeed)
    {
        byte[] sharedSecret = new byte[32];
        //ScalarMult.scalarMult(sharedSecret,randomLock,publicKey);
        Sodium.crypto_scalarmult_base(publicKey,randomSeed);
        Sodium.crypto_scalarmult(sharedSecret,randomSeed,publicKey);
        return sharedSecret;
    }
}
