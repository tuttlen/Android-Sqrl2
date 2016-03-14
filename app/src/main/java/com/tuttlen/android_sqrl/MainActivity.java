package com.tuttlen.android_sqrl;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends Activity {
    private ListView listOfBT;
    BluetoothAdapter bAdapter;
    private TextView textView1 = null;
    private EditText publicKeyText = null;
    private EditText signatureText = null;
    private Button confbutton = null;
    private Button scanButton = null;
    private Button exportKey =null;
    private static authRequest authReq = null; // Contains all the info for the web page you are trying to authenticate with
    private static identity current_identity = null; // The currently logged in identity
    private static IdentityData current_sqrl_identity = null; // The currently logged in identity

    private String pubKey = "";
    private String sign = "";
    private String scanned="";
    BluetoothDevice selectedDevice;
    String uuid_sqrlService = "1e0ca4ea-299d-4335-93eb-27fcfe7fa848";
    Map<String, BluetoothDevice> pairedDevices = new HashMap<String, BluetoothDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        listOfBT = (ListView) findViewById(R.id.listView);
        textView1 = (TextView) findViewById(R.id.textView1);
        publicKeyText = (EditText) findViewById(R.id.publicKeyText);
        signatureText = (EditText) findViewById(R.id.signatureText);
        //exportPublicKeys
        exportKey = (Button) findViewById(R.id.exportPublicKeys);
        confbutton = (Button) findViewById(R.id.confbutton);
        scanButton = (Button) findViewById(R.id.scan);
        bAdapter = BluetoothAdapter.getDefaultAdapter();

        if(authReq == null) {
            confbutton.setEnabled(false);
        }

        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ScanQrcCode();

            }
        });


        confbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                publicKeyText.setText("Please wait this will take time");
                confbutton.setEnabled(false);

                if(!authReq.isBlueTooth) {
                    new createSignature().execute(authReq.getURL());
                } else {
                    new createSignature().execute(authReq.getBlueToothURL());
                }
            }
        });

        exportKey.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //validate public key
                ExportKeyViaBtooth(pubKey);

            }
        });

        // Delete identity button (just for testing/debugging)
        final Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (current_identity != null)
                    current_identity.deleteIdentityFile(getApplicationContext());
            }
        });
        if(bAdapter != null) {
            GetPaired();
        } else
        {
            //TODO check to see if bluetooth is off and update this text. Otherwise scream at the user
            Toast.makeText(getApplicationContext(), "Bluetooth is not emabled or is not working, the app will not function peer to peer", Toast.LENGTH_LONG).show(); // show the user
        }
    }

    /*
        TODO checkvalue meets constraints
    */
    public String RunFindService_client(String sendValue,boolean listen) {
        String result = "n";
        BluetoothSocket socket =null;
        if(selectedDevice ==null) {
            Toast.makeText(getApplicationContext(), "Must select a receiver", Toast.LENGTH_LONG).show(); // show the user
        } else {
            try {
                socket = selectedDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid_sqrlService));
                socket.connect();
                OutputStream stream = socket.getOutputStream();
                InputStream streamin = socket.getInputStream();
                stream.write(sendValue.getBytes());
                if(listen) {
                    //now we wait for response
                    byte[] inBytes = new byte[100];
                    streamin.read(inBytes);
                    result = new String(inBytes, StandardCharsets.UTF_8);
                }
                //and then we may need to sing back results
                //based on response we may then want to send an unlock password or something

            } catch (Exception e) {
                result = "n:"+e.getMessage();
            }
        }
        return result;
    }

    //TODO remove this
    public void RunFindService(String sendValue) {

        try {

            bAdapter = BluetoothAdapter.getDefaultAdapter();
            //BluetoothDevice dvice = bAdapter.getRemoteDevice(uuid_sqrlService);
            BluetoothServerSocket srvrSocket = bAdapter.listenUsingRfcommWithServiceRecord("helloService", UUID.fromString(uuid_sqrlService));
            BluetoothSocket socket = srvrSocket.accept(30);
            socket.connect();
            OutputStream stream = socket.getOutputStream();
            stream.write(sendValue.getBytes());

            if(socket.isConnected()) {
                socket.close();
                srvrSocket.close();
            }

        } catch (Exception e) {
            textView1.setText(e.getMessage());
        }
    }

    private void ExportKeyViaBtooth(String pubKey) {

        RunFindService_client(pubKey, false);
    }

    private void GetPaired()
    {
        //pariedDeviceName
        //ListAdapter adapter = listOfBT.getAdapter();
        ArrayList<String> listOfstring = new ArrayList<String>();

        for (BluetoothDevice pairedDevice : bAdapter.getBondedDevices()) {
            String name = pairedDevice.getName();
            listOfstring.add(name);
            pairedDevices.put(name, pairedDevice);
        }
        //in C# this is smatter


        ListAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, listOfstring);

        listOfBT.setAdapter(adapter);
        listOfBT.setOnItemClickListener(onItemClickListener);

    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectedDevice = pairedDevices.get(parent.getItemAtPosition(position));
            //execute.setEnabled(true);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (current_identity == null) {
            // Show login form
            Intent loginAct = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(loginAct, 54321);
        }
        //TODO remove this
        /*
        else
        {
            if (authReq == null) {
                //ScanQrcCode();
                IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
                //intentIntegrator.addExtra("Results","ScannedAuth");

                AlertDialog dialog = intentIntegrator.initiateScan();
                dialog.setOwnerActivity(this);
            }
        }
        */

    }

    // The activity is no longer visible
    @Override
    protected void onPause() {
        super.onPause();
        // Reset for new scan
        textView1.setText("");
        publicKeyText.setText("");
        signatureText.setText("");
        confbutton.setEnabled(true);
        authReq = null;
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        // From loginAct
        if (resultCode == RESULT_OK && requestCode == 54321) {
            //sqrlid
            if(data.getSerializableExtra("id") !=null)
            {
                current_identity = (identity) data.getSerializableExtra("id");
            } else if(data.getSerializableExtra("sqrlid") !=null)
            {
                current_sqrl_identity = (IdentityData) data.getSerializableExtra("sqrlid");
            }
        }

        // Jumps here when QR is scanned
        if (resultCode == RESULT_OK && requestCode != 54321)
        {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            scanned =  result.getContents();
            try {
                authReq = new authRequest(scanned);
            } catch (URISyntaxException e)
            {

            }
            confbutton.setEnabled(true);
            textView1.setText("Authenticate to " + authReq.getDomain());
        }
    }

    private void ScanQrcCode() {

        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }

    private class createSignature extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String URL = params[0];
            byte[] privateKey = Helper.CreatePrivateKey(authReq.getDomain(), current_identity.getMasterKey());

            byte[] publicKey=null;
            byte[] signature=null;

            try {
                publicKey = Helper.PublicKeyFromPrivateKey(privateKey);
                signature = Helper.Sign(URL.getBytes(), privateKey);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String publicKey_s = Helper.urlEncode(publicKey);
            String sign_s = Helper.urlEncode(signature);
            //It's either verified or not
            boolean result = false;
            if(authReq.isBlueTooth) {
                //String message, String signature, String publicKey)
                MessageFormat msgFormat = new MessageFormat("{0}|{1}|{2}");
                //TODO assert device address we have selected matches
                String bAddressAndNonce = authReq.getURL();
                if(ValidateBluetoothAddress(selectedDevice.getAddress(), bAddressAndNonce)) {
                    //TODO need to refactor this to return boolean of validation also this function should return an object
                    String results = RunFindService_client(msgFormat.format(new Object[]{bAddressAndNonce, sign_s, publicKey_s}), true);
                    results = results.split(":")[0]; //TODO thrownaway the result for now but we may use later
                    if(results == "y") {
                        result = true;
                    } else {
                        result = false;
                    }
                } else {
                    //for bluetooth this is somewhat important if we want to send or receive passwords
                    Toast.makeText(getApplicationContext(), "Receiver does not match! Please select the right recipient.",
                            Toast.LENGTH_LONG).show(); // show the user
                    result = false;
                }

            } else {
                result =web_post(authReq.getReturnURL(), authReq.getURL(), sign_s, publicKey_s);
            }
            if(result) {
                return new String[]{publicKey_s, sign_s, "Verified"};
            } else {
                return new String[]{publicKey_s, sign_s, "Failed"};
            }
        }

        private boolean ValidateBluetoothAddress(String address, String bAddressAndNonce) {

            //TODO verify address endianness. THis is tricky we may need to ask because
            //there may be a security vulnerabilty introduced if we allow both
            return bAddressAndNonce.startsWith(address);
        }

        @Override
        protected void onPostExecute(String[]result) {
            pubKey = result[0];
            sign = result[1];

            Context context = getApplicationContext();
            if (result[2].compareTo("Verified") == 0)
            {
                Toast.makeText(context, "Verified", Toast.LENGTH_LONG).show(); // show the user
            } else {
                Toast.makeText(context, "Failed to verify", Toast.LENGTH_LONG).show(); // show the user
            }
            //TODO this is good for debugging but the user does not need to see this
            publicKeyText.setText(pubKey);
            signatureText.setText(sign);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Send signature and pubkey to server
    private boolean web_post(String URL, String message, String signature, String publicKey) {


        HttpClientBuilder builder;
        HttpPostHC4 httppost;
        CloseableHttpClient httpClient;

        try
        {
            builder = HttpClientBuilder.create();
            //builder.setSSLSocketFactory( new SSLConnectionSocketFactory( SSLContext.getDefault()));
            httpClient = builder.build();
            httppost = new HttpPostHC4(URL);

            httppost.addHeader("message",message);
            httppost.addHeader("signature",signature);
            httppost.addHeader("publicKey",publicKey);
            CloseableHttpResponse response = httpClient.execute(httppost); // Execute HTTP Post Request

            int status = response.getStatusLine().getStatusCode();

            if (status == HttpStatus.SC_OK) {
                ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                response.getEntity().writeTo(ostream);

                String out = ostream.toString();
                Log.v("web", out);
                // See if the page returned "Verified"
                //if (out.contains("Verified")) {
                //    return true; // return true if verified
               // }
                //according to spec if we have a status of OK that means it worked any other and it fails,
                //TODO I am not sure if other 200 class codes are also acceptable
                return true;
            }  else {Log.v("web", "Connection not ok");}
        } catch (ClientProtocolException e) {
            Log.e("web", "error");
            //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("web", "error");
            //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return false; // Return false if query did not return verification
    }

}
