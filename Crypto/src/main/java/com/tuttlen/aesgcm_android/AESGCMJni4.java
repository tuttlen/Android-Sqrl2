package com.tuttlen.aesgcm_android;

/**
 * Created by nathan on 3/10/16.
 */
public class AESGCMJni4 {
    static {
        System.loadLibrary("aesgcm4"); // Load native library at runtime
        // hello.dll (Windows) or libhello.so (Unixes)
    }

    public native String doEncryption(byte[] key, byte[] IV, byte[] AAD, byte[] tag, byte[] plainText);
    public native String doDecryption(byte[] key, byte[] IV, byte[] AAD, byte[] tag, byte[] cipherText);
    //verify_gcm_decryption( key, key_len, iv, iv_len,  aad, aad_len, pt, ct, ct_len, tag, tag_len);

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
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    // Test Driver
    public static void main(String[] args)
    {
        String outvalue2 ="";
        if(args.length == 0)
        {
            System.out.println(" key, IV, AAD, TAG, ciphertext, in hex");
        } else {
            AESGCMJni4 encryption = new AESGCMJni4();
            if(args[0].toUpperCase().startsWith("DECRYPT"))
            {
                String key= args[1];
                String IV = args[2];
                String aad = args[3];
                String tag =args[4];

                String cipherText=args[5];

                outvalue2= encryption.doDecryption(
                        hexStringToByteArray(key),
                        hexStringToByteArray(IV),
                        hexStringToByteArray(aad),
                        hexStringToByteArray(tag),
                        hexStringToByteArray(cipherText));

            } else {
                String key= args[1];
                String IV = args[2];
                String aad = args[3];
                String tag =args[4];

                String plaintext=args[5];

                outvalue2= encryption.doEncryption(
                        hexStringToByteArray(key),
                        hexStringToByteArray(IV),
                        hexStringToByteArray(aad),
                        hexStringToByteArray(tag),
                        hexStringToByteArray(plaintext));

            }
            System.out.println("-----------------------------------");
            System.out.println(outvalue2);
            System.out.println("-----------------------------------");
        }
    }
}