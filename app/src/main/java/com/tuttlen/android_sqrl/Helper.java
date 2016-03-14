package com.tuttlen.android_sqrl;

import android.util.Base64;

import com.igormaznitsa.jbbp.JBBPParser;
import com.igormaznitsa.jbbp.mapper.Bin;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayByte;
import com.igormaznitsa.jbbp.model.JBBPFieldStruct;

import org.abstractj.kalium.Sodium;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import eu.artemisc.stodium.Stodium;

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
        target = target.replace("+", "/").replace("_", "-");
        return Base64.decode(target, Base64.DEFAULT);
    }

    public static String urlEncode(byte[] target) {
        String base64 = Base64.encodeToString(target, Base64.DEFAULT);
        return base64.replace("/", "+").replace("-", "_");

    }

    //PBKDF(2)
    public static byte[] PK(byte[] password, byte[] salt, int iterations, byte[] previosResult, int N, int r) throws GeneralSecurityException {
        int p = 1;

        byte[] result = new byte[32];
        Sodium.crypto_pwhash_scryptsalsa208sha256_ll(password, password.length, salt, salt.length, N, r, p, result, result.length);
        iterations--;
        android.util.Log.d("PK", String.format("On iteration %d: %s", iterations, Helper.bytesToHex(result)));
        if (iterations == 0 && previosResult.length == 0) return result;
        if (iterations == 0) return Helper.massXOR(result, previosResult);
        if (previosResult.length == 0) {
            return PK(password, result, iterations, result, N, r);
        } else {
            return PK(password, result, iterations, Helper.massXOR(previosResult, result), N, r);
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

    public static byte[] CreatePrivateHMAC(String domain, byte[] key)
    {
        byte[] messageBytes = domain.getBytes();
        byte[] out = new byte[32];
        Sodium.crypto_auth_hmacsha256(out, messageBytes, messageBytes.length, key);
        return out;
    }

    public static byte[] CreateRandom(int length) {
        byte[] randomValues = new byte[length];
        Sodium.randombytes_buf(randomValues, length);
        return  randomValues;
    }

    public static byte[] PublicKeyFromPrivateKey(byte[] privateKey) {
        byte[] publicKey = new byte[32];
        Sodium.crypto_sign_ed25519_sk_to_pk(publicKey,privateKey);
        return publicKey;
    }

    public static byte[] Sign(byte[] message, byte[] privateKey) {
        byte[] signedMessage = new byte[64];
        Sodium.crypto_sign_ed25519(signedMessage,new int[]{message.length},message,message.length,privateKey);
        return signedMessage;
    }

    public static byte[] SHA256(byte[] value)
    {
        byte[] outHash = new byte[32];
        Sodium.crypto_hash_sha256(outHash,value,32);
        return  outHash;
    }
}
