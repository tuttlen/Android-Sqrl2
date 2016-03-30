package com.tuttlen.android_sqrl;

import com.igormaznitsa.jbbp.JBBPParser;
import com.igormaznitsa.jbbp.io.JBBPByteOrder;
import com.igormaznitsa.jbbp.io.JBBPOut;
import com.igormaznitsa.jbbp.mapper.Bin;
import com.igormaznitsa.jbbp.mapper.BinType;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayByte;
import com.igormaznitsa.jbbp.model.JBBPFieldStruct;

import java.io.IOException;

/**
 * Created by nathan tuttle on 3/9/16.
 */
public class SqrlData {
    public byte [] aad;
    public type1 sqrlStorage;
    public type2 type2SqrlData;
    public type3 type3SqrlData;

    @Bin
    class type1 {
        @Bin(outOrder = 0, type = BinType.SHORT) short Entire_length;
        @Bin(outOrder = 1, type = BinType.SHORT) short Initial_type;
        @Bin(outOrder = 2, type = BinType.SHORT) short PT_length;
        @Bin(outOrder = 3, type = BinType.BYTE_ARRAY) byte[] IV;
        @Bin(outOrder = 4, type = BinType.BYTE_ARRAY) byte[] ScryptSalt;
        @Bin(outOrder = 5, type = BinType.BYTE) byte nFactor;
        @Bin(outOrder = 6, type = BinType.INT) int ScryptIteration;
        @Bin(outOrder = 7, type = BinType.BYTE_ARRAY) byte[] Options;
        @Bin(outOrder = 8, type = BinType.BYTE) byte HintLength;
        @Bin(outOrder = 9, type = BinType.BYTE) byte PWVerifySeconds;
        @Bin(outOrder = 10,type = BinType.BYTE_ARRAY) byte[] Timeout;
        @Bin(outOrder = 11,type = BinType.BYTE_ARRAY) byte[] IDMK;
        @Bin(outOrder = 12,type = BinType.BYTE_ARRAY) byte[] IDLK;
        @Bin(outOrder = 13,type = BinType.BYTE_ARRAY) byte[] tag;
    }

    @Bin
    class sqrlType {
        @Bin(outOrder = 0, type = BinType.SHORT) short Length;
        @Bin(outOrder = 1, type = BinType.SHORT) short TypeCode;
    }

    class type2 extends sqrlType
    {
        @Bin(outOrder = 2, type = BinType.BYTE_ARRAY) byte[] ScryptSalt;
        @Bin(outOrder = 3, type = BinType.BYTE) byte nFactor;
        @Bin(outOrder = 4, type = BinType.INT) int ScryptIteration;
        @Bin(outOrder = 5,type = BinType.BYTE_ARRAY) byte[] IDUK;
        @Bin(outOrder = 6,type = BinType.BYTE_ARRAY) byte[] tag;
    }


    class type3 extends sqrlType
    {
        @Bin(outOrder = 2,type = BinType.BYTE_ARRAY) byte[] encryptedpIUK;
        @Bin(outOrder = 3,type = BinType.BYTE_ARRAY) byte[] encryptedNOIUK;
        @Bin(outOrder = 4,type = BinType.BYTE_ARRAY) byte[] encryptedNNOIUK;
        @Bin(outOrder = 5,type = BinType.BYTE_ARRAY) byte[] encryptedOPIUK;
        @Bin(outOrder = 6,type = BinType.BYTE_ARRAY) byte[] tag;

    }

    public static SqrlData ExtractSqrlData(byte[] hex) throws IOException
    {
        SqrlData dataHolder = new SqrlData();
        type1 type1sqrlData = JBBPParser.prepare("<short Entire_length; <short Initial_type; <short PT_length; byte[12] IV; byte[16] ScryptSalt; byte nFactor; <int ScryptIteration; byte[2] Options; byte HintLength; byte PWVerifySeconds; byte[2] Timeout; byte[32] IDMK; byte[32] IDLK; byte[16] tag;").parse(hex).mapTo(type1.class);
        JBBPParser parseAAD = JBBPParser.prepare("byte[" + type1sqrlData.PT_length + "] aad;");
        JBBPFieldStruct result = parseAAD.parse(hex);
        byte[] parsedaad = result.findFieldForNameAndType("aad", JBBPFieldArrayByte.class).getArray();
        dataHolder.aad =parsedaad;
        dataHolder.sqrlStorage = type1sqrlData;
        int endLength = type1sqrlData.Entire_length;
        dataHolder = getNextType(dataHolder,endLength,hex);
        return dataHolder;
    }

    public static SqrlData getNextType(SqrlData dataHolder, int endLength, byte[] hex) throws IOException
    {
        int lastLength =  hex.length - endLength;
        if(lastLength > 0) {
            byte[] nextHex = new byte[lastLength];
            System.arraycopy(hex, endLength, nextHex, 0, lastLength);
            sqrlType nextType = JBBPParser.prepare("<short Length; <short TypeCode;").parse(nextHex).mapTo(sqrlType.class);

            if(nextType.Length > nextHex.length) return dataHolder;

            if(nextType.TypeCode == 2)
            {
                dataHolder.type2SqrlData = JBBPParser.prepare("<short Length; <short TypeCode;  byte[16] ScryptSalt; byte nFactor; <int ScryptIteration; byte[32] IDUK; byte[16] tag;").parse(nextHex).mapTo(type2.class);
                dataHolder = getNextType(dataHolder,dataHolder.type2SqrlData.Length,nextHex);
            }

            if(nextType.TypeCode == 3)
            {
                dataHolder.type3SqrlData = JBBPParser.prepare("<short Length; <short TypeCode; byte[32] encryptedpIUK; byte[32] encryptedNOIUK; byte[32] encryptedNNOIUK; byte[32] encryptedOPIUK; byte[16] tag;").parse(nextHex).mapTo(type3.class);
                dataHolder = getNextType(dataHolder,dataHolder.type3SqrlData.Length,nextHex);
            }

        }

        return  dataHolder;
    }

    public static byte[]  WriteSqrlData(SqrlData data) throws IOException {

        byte[] bytesOut = JBBPOut.BeginBin(JBBPByteOrder.LITTLE_ENDIAN).Bin(data.sqrlStorage).End().toByteArray();
        //TODO soon there will be multiple types at that point we would have an array of types and output the entire array
        //into this structure
        return bytesOut;
    }

    public static String ExportSqrlData(SqrlData data) throws IOException {

        byte[] bytesOut = JBBPOut.BeginBin(JBBPByteOrder.LITTLE_ENDIAN).Bin(data.sqrlStorage).End().toByteArray();
        //TODO soon there will be multiple types at that point we would have an array of types and output the entire array
        //into this structure
        //TODO add type
        String rawCode = "SQRLDATA"+Helper.urlEncode(bytesOut);
        return rawCode;
    }
}