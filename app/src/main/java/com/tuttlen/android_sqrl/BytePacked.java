package com.tuttlen.android_sqrl;

/**
 * Created by nathan on 3/9/16.
 */
public class BytePacked {
    public int Order;
    public int Length;
    public String Typepacked;

    public static byte[] unPack(int start, int length, byte[] packedArray)
    {
        byte[] returnArray = new byte[length];
        for (int i = start; i < length ; i++) {
            returnArray[i-start] = packedArray[i];
        }
        return returnArray;
    }
    public BytePacked(int order,int length, String type)
    {
        this.Order = order;
        this.Length =length;
        this.Typepacked = type;
    }
}
