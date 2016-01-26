package com.tuttlen.android_sqrl;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.github.dazoe.android.Ed25519;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;

public class MainActivity extends Activity {
    private ListView listOfBT;
    BluetoothAdapter bAdapter;
    private TextView textView1 = null;
    private EditText editText1 = null;
    private EditText editText2 = null;
    private Button confbutton = null;
    private Button scanButton = null;
    private Button exportKey =null;
    private authRequest authReq = null; // Contains all the info for the web page you are trying to authenticate with
    private identity current_identity = null; // The currently logged in identity

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
        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
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
                editText1.setText("Please wait this will take time");
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
        GetPaired();
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

        RunFindService_client(pubKey,false);
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
        editText1.setText("");
        editText2.setText("");
        confbutton.setEnabled(true);
        authReq = null;
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        // From loginAct
        if (resultCode == RESULT_OK && requestCode == 54321) {

            current_identity = (identity)data.getSerializableExtra("id");
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
            byte[] privateKey = CreatePrivateKey(authReq.getDomain(), current_identity.getMasterKey());

            byte[] publicKey=null;
            byte[] signature=null;

            try {
                publicKey = Ed25519.PublicKeyFromPrivateKey(privateKey);
                signature = Ed25519.Sign(URL.getBytes(), privateKey);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String publicKey_s = Base64.encodeToString(publicKey, Base64.DEFAULT);
            String sign_s = Base64.encodeToString(signature, Base64.DEFAULT);
            String result ="";
            if(authReq.isBlueTooth) {
                //String message, String signature, String publicKey)
                MessageFormat msgFormat = new MessageFormat("{0}:{1}:{2}");
                //TODO assert device address we have selected matches
                String bAddressAndNonce = authReq.getURL();
                if(ValidateBluetoothAddress(selectedDevice.getAddress(), bAddressAndNonce)) {
                    result = RunFindService_client(msgFormat.format(new Object[]{bAddressAndNonce, sign_s, publicKey_s}), true);
                    result = result.split(":")[0]; //TODO thrownaway the result for now but we may use later
                } else {
                    //for bluetooth this is somewhat important if we want to send or receive passwords
                    Toast.makeText(getApplicationContext(), "Receiver does not match! Please select the right recipient.",
                            Toast.LENGTH_LONG).show(); // show the user
                    result = "n";
                }

            } else {
                if (web_post(authReq.getReturnURL(), authReq.getURL(), sign_s, publicKey_s)) {
                    // post the result
                    result = "y";
                }
            }

            return new String[] {publicKey_s, sign_s, result};
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
            if (result[2].compareTo("y") == 0)
            {
                Toast.makeText(context, "Verified", Toast.LENGTH_LONG).show(); // show the user
            } else {
                Toast.makeText(context, "Faield to verify", Toast.LENGTH_LONG).show(); // show the user
            }

            editText1.setText(pubKey);
            editText2.setText(sign);
        }
    }

    // Create the private key from URL and secret key
    public static byte[] CreatePrivateKey(String domain, byte[] key) {
        byte[] hmac=null;
        try {
            SecretKeySpec pKey = new SecretKeySpec(key, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(pKey);
            hmac = mac.doFinal(domain.getBytes());
        } catch (Exception e) {
        }

        return hmac;
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
        HttpClient httpClient;
        HttpPost httppost;

        try
        {
            builder = HttpClientBuilder.create();
            //builder.setSSLSocketFactory( new SSLConnectionSocketFactory( SSLContext.getDefault()));
            httpClient = builder.build();
            httppost = new HttpPost(URL);
            // Add data to post
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("message", message));
            nameValuePairs.add(new BasicNameValuePair("signature", signature));
            nameValuePairs.add(new BasicNameValuePair("publicKey", publicKey));

            //TODO
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(httppost); // Execute HTTP Post Request

            int status = response.getStatusLine().getStatusCode();

            if (status == HttpStatus.SC_OK) {
                ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                response.getEntity().writeTo(ostream);

                String out = ostream.toString();
                Log.v("web", out);
                // See if the page returned "Verified"
                if (out.contains("Verified")) {
                    return true; // return true if verified
                }
            }  else {Log.v("web", "Connection not ok");}
        } catch (ClientProtocolException e) {
            Log.e("web", "error");
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("web", "error");
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return false; // Return false if query did not return verification
    }

}
