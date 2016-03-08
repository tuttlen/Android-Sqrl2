package com.tuttlen.android_sqrl;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by nathan on 3/8/16.
 */
public class IdentityData implements Serializable{
    public String name;
    public String Id;
    public boolean checked;
    public String mimeType;
    public byte[] idContents;
    public IdentityData(String name, String id, String MimeType, boolean selected) {
        this.name =name;
        this.Id = id;
        this.checked =selected;
        this.mimeType =MimeType;
    }

    public static IdentityData selectIdentity(ArrayList<IdentityData> items, String user) {
        for(IdentityData item : items) {
            if(item.name.equals(user))
            {
                //only one can be equal
                return item;
            }
        }
        return null;
    }

    public static void save(Context con, ArrayList<IdentityData> identities) {
        //TODO first need to convert outputstream to hex

        try {
            try (
                    OutputStream file = con.getApplicationContext().openFileOutput("androidsqrl2.dat",Context.MODE_PRIVATE);
                    OutputStream buffer = new BufferedOutputStream(file);
                    ObjectOutput output = new ObjectOutputStream(buffer);
            ){
                output.writeObject(identities);
            }
            catch(IOException ex){
                Log.d("SAVE",String.format("Cannot perform output. %1$s", ex.getMessage()));
            }
        }
        catch (Exception e) {
            Log.d("SAVE",String.format("Cannot instantiate. %1$s", e.getMessage()));
        }
    }

    public static ArrayList<IdentityData> load(Context con)
    {
        ArrayList<IdentityData> readSqrlDataBlocks =null;
        try(
                InputStream file = con.getApplicationContext().openFileInput("androidsqrl2.dat");
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer);
        ){
            //deserialize the List
            readSqrlDataBlocks = (ArrayList<IdentityData>)input.readObject();
            //display its data
            for(IdentityData id: readSqrlDataBlocks){
                System.out.println("Loaded Name " + id.name);
            }
        }
        catch(ClassNotFoundException ex){
            Log.d("LOAD", String.format("Cannot perform input. Class not found: %1$s", ex.getMessage()));
        }
        catch(IOException ex){
            Log.d("LOAD", String.format("Cannot perform input. IO: %1$s ", ex.getMessage()));
        }
        return readSqrlDataBlocks;
    }

}