package com.tuttlen.android_sqrl;

import com.igormaznitsa.jbbp.JBBPParser;
import com.igormaznitsa.jbbp.io.JBBPBitOrder;
import com.igormaznitsa.jbbp.io.JBBPByteOrder;
import com.igormaznitsa.jbbp.io.JBBPOut;
import com.igormaznitsa.jbbp.mapper.Bin;
import com.igormaznitsa.jbbp.mapper.BinType;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayByte;
import com.igormaznitsa.jbbp.model.JBBPFieldStruct;
import com.igormaznitsa.jbbp.utils.JBBPTextWriter;

import java.io.IOException;

/**
 * Created by nathan tuttle on 3/9/16.
 */
public class SqrlData {
    public byte [] aad;
    public Parsed sqrlStorage;

    @Bin
    class Parsed {
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

    public static SqrlData ExtractSqrlData(byte[] hex) throws IOException
    {
        SqrlData dataHolder = new SqrlData();
        SqrlData.Parsed parsed = JBBPParser.prepare("<short Entire_length; <short Initial_type; <short PT_length; byte[12] IV; byte[16] ScryptSalt; byte nFactor; <int ScryptIteration; byte[2] Options; byte HintLength; byte PWVerifySeconds; byte[2] Timeout; byte[32] IDMK; byte[32] IDLK; byte[16] tag;").parse(hex).mapTo(Parsed.class);
        JBBPParser parseAAD = JBBPParser.prepare("byte[" + parsed.PT_length + "] aad;");
        JBBPFieldStruct result = parseAAD.parse(hex);
        byte[] parsedaad = result.findFieldForNameAndType("aad", JBBPFieldArrayByte.class).getArray();
        dataHolder.aad =parsedaad;
        dataHolder.sqrlStorage =parsed;
        return dataHolder;
    }

    public static byte[]  WriteSqrlData(SqrlData data) throws IOException {

        byte[] bytesOut = JBBPOut.BeginBin(JBBPByteOrder.LITTLE_ENDIAN).Bin(data.sqrlStorage).End().toByteArray();

        return bytesOut;
    }
}