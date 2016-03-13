package com.tuttlen.android_sqrl;

import com.igormaznitsa.jbbp.JBBPParser;
import com.igormaznitsa.jbbp.mapper.Bin;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayByte;
import com.igormaznitsa.jbbp.model.JBBPFieldStruct;

import java.io.IOException;

/**
 * Created by nathan tuttle on 3/9/16.
 */
public class SqrlData {
    public byte [] aad;
    public Parsed sqrlStorage;

    @Bin
    class Parsed {
        short Entire_length;
        short Initial_type;
        short PT_length;
        byte[] IV;
        byte[] ScryptSalt;
        byte nFactor;
        int ScryptIteration;
        byte[] Options;
        byte HintLength;
        byte PWVerifySeconds;
        byte[] Timeout;
        byte[] IDMK;
        byte[] IDLK;
        byte[] tag;
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
}