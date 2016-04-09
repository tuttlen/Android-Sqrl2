package com.tuttlen.android_sqrl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.abstractj.kalium.Sodium;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends Activity {
    public ArrayList<String> resultSet = new ArrayList<String>();
    private ListView listOfBT;
    BluetoothAdapter bAdapter;
    private TextView textView1 = null;
    private TextView idName = null;
    private EditText signatureText = null;
    private Button confbutton = null;
    private Button scanButton = null;
    private Button exportKey =null;
    private Button btnLogout =null;
    private static AuthorizationRequest authReq = null; // Contains all the info for the web page you are trying to authenticate with
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

        //TODO move out BT to another activity this activity will be split into the handling activity and there will be two new identities btooth and IPC
        listOfBT = (ListView) findViewById(R.id.listView);
        textView1 = (TextView) findViewById(R.id.textView1);
        idName = (TextView) findViewById(R.id.idName);

        signatureText = (EditText) findViewById(R.id.signatureText);
        //exportPublicKeys
        exportKey = (Button) findViewById(R.id.exportPublicKeys);
        confbutton = (Button) findViewById(R.id.confbutton);
        scanButton = (Button) findViewById(R.id.scan);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        bAdapter = BluetoothAdapter.getDefaultAdapter();

        if(authReq == null) {
            confbutton.setEnabled(false);
        }

        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ScanQrcCode();

            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                current_sqrl_identity.Clear();
                Intent loginAct = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(loginAct, 54321);

            }
        });


        confbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confbutton.setEnabled(false);

                if(!authReq.isValidBluetooth) {
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
        if (current_identity == null && current_sqrl_identity == null) {
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
        signatureText.setText("");
        idName.setText("");
        confbutton.setEnabled(true);
        //authReq = null;
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
                this.idName.setText(current_sqrl_identity.name);
            }
        }

        // Jumps here when QR is scanned
        if (resultCode == RESULT_OK && requestCode != 54321)
        {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            scanned =  result.getContents();
            //TODO this is where we will determine the activity for btooth
            authReq = new AuthorizationRequest(scanned);
            confbutton.setEnabled(true);
            ShouldAuthenticate(authReq.domain,authReq.SFN);
            textView1.setText("Authenticate to " + authReq.getDomain());
        }
    }

    protected void ShouldAuthenticate(String domain, String sfn)
    {
        //AuthAlertFragment fragment = new AuthAlertFragment();
        //Bundle args =new Bundle();
        //args.putString("domain",domain);
        //args.putString("sfn",sfn);
        //fragment.setArguments(args);
        //fragment.show(getFragmentManager(), "Auth");
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Authenticate To");
        dialog.setContentView(R.layout.activity_authenticate);

        TextView tvDomain = (TextView)dialog.findViewById(R.id.tvDomain);
        TextView tvSFN = (TextView)dialog.findViewById(R.id.tvSFN);
        tvDomain.setText(domain);
        try {
            tvSFN.setText(new String(Helper.urlDecode(sfn), "UTF-8"));
            Button btnConfirm = (Button) dialog.findViewById(R.id.btnConfirm);
            Button btnReject = (Button) dialog.findViewById(R.id.btnReject);
            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!authReq.isValidBluetooth) {
                        new createSignature().execute(authReq.getURL());
                    } else {
                        new createSignature().execute(authReq.getBlueToothURL());
                    }
                    dialog.dismiss();

                }
            });
            btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        } catch (Exception e) {

        }
        dialog.show();
        /*
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        try {
            builder.setTitle("Authenticate To");
            builder.setMessage(String.format("Authenticate to %s known as %s", domain, Helper.urlDecode(sfn)));

            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!authReq.isValidBluetooth) {
                        new createSignature().execute(authReq.getURL());
                    } else {
                        new createSignature().execute(authReq.getBlueToothURL());
                    }
                }
            });

            builder.setNegativeButton("Reject", null);
            builder.setView(inflater.inflate(R.layout.activity_authenticate, null));

            AlertDialog dialog = builder.create();
            dialog.setContentView(R.layout.activity_authenticate);

            TextView tvDomain = (TextView)dialog.findViewById(R.id.tvDomain);
            TextView tvSFN = (TextView)dialog.findViewById(R.id.tvSFN);
            tvDomain.setText(domain);
            tvSFN.setText(sfn);

            dialog.show();

        } catch (Exception e)
        {
            //builder.setMessage(e.getMessage());
            //AlertDialog dialog = builder.create();
            //dialog.show();
        }
        */
    }

    private void ScanQrcCode() {

        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }

    public class createSignature extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            try {
                String URL = params[0];
                byte[] privateKey = Helper.CreatePrivateHMAC(authReq.getDomain(), current_sqrl_identity.getMasterKey());

                byte[] publicKey = null;
                byte[] signature = null;

                try {
                    publicKey = Helper.PublicKeyFromPrivateKey(privateKey);
                    signature = Helper.Sign(authReq.CalledUrl.getBytes(), privateKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String publicKey_s = Helper.urlEncode(publicKey);
                String sign_s = Helper.urlEncode(signature);
                //It's either verified or not
                boolean result = false;
                if (authReq.isValidBluetooth) {
                    //String message, String signature, String publicKey)
                    MessageFormat msgFormat = new MessageFormat("{0}|{1}|{2}");
                    //TODO assert device address we have selected matches
                    String bAddressAndNonce = authReq.getURL();
                    if (ValidateBluetoothAddress(selectedDevice.getAddress(), bAddressAndNonce)) {
                        //TODO need to refactor this to return boolean of validation also this function should return an object
                        String results = RunFindService_client(msgFormat.format(new Object[]{bAddressAndNonce, sign_s, publicKey_s}), true);
                        results = results.split(":")[0]; //TODO thrownaway the result for now but we may use later
                        if (results == "y") {
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
                    //the server may or may not handle SQRL schemes this probably should go away because they should expect http(s)
                    authReq.isConnectionPicky=true;

                    //Even though the nut is encoded in the variable some servers expect to see it in the URL as well
                    authReq.fullNut=true;
                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    try {
                        nameValuePairs =GenerateSQRLCall(authReq.CalledUrl, authReq.getURL(), "query", publicKey_s, privateKey);
                    } catch(UnsupportedEncodingException e){}


                    result = web_post3(nameValuePairs, authReq);
                    //if query result is successful
                    if(result) {
                        try {
                            String parseResult = resultSet.get(resultSet.size() - 1);
                            SqrlResponse response = new SqrlResponse(parseResult);
                            if((Integer.parseInt(response.tifHex,16) & 1) == 0) {
                                //server does not recognize so send an SUK/VUk pair
                                AuthorizationRequest newNutResponse =authReq.getNewNut(response.nut);
                                newNutResponse.isConnectionPicky=true;
                                newNutResponse.fullNut=true;
                                nameValuePairs = GenerateSQRLCall2(newNutResponse.CalledUrl, authReq.getURL(), "ident", publicKey_s, privateKey);
                                result = web_post3(nameValuePairs,newNutResponse);
                            }
                        } catch(UnsupportedEncodingException e)
                        {}
                    }

                }
                if (result) {
                    return new String[]{publicKey_s, sign_s, "Verified"};
                } else {
                    return new String[]{publicKey_s, sign_s, "Failed"};
                }
            } catch(SecurityException e)
            {
                Toast.makeText(getApplicationContext(),"SecuritException",Toast.LENGTH_LONG);
            }
            return new String[]{"", "", "Failed"};
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
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            try {

                String parseResult = resultSet.get(resultSet.size() - 1);
                SqrlResponse response = new SqrlResponse(parseResult);
                builder.setTitle("Server response");
                builder.setMessage(response.tifReuslt);

                AlertDialog dialog = builder.create();
                dialog.show();
            } catch (UnsupportedEncodingException e)
            {
                builder.setMessage("The server did not properly format the response!");
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            if (result[2].compareTo("Verified") == 0)
            {
                Toast.makeText(context, "Verified", Toast.LENGTH_LONG).show(); // show the user
            } else {
                Toast.makeText(context, "Failed to verify", Toast.LENGTH_LONG).show(); // show the user
            }
            //TODO this is good for debugging but the user does not need to see this

            signatureText.setText(sign);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean web_post3(ArrayList<NameValuePair> nameValuePairs, AuthorizationRequest theRequest )
    {
        try
        {
            HttpClient httpClient = new DefaultHttpClient();
            theRequest.isConnectionPicky =true;
            theRequest.fullNut =true;
            HttpPost httppost = new HttpPost(theRequest.getReturnURL());
            httppost.addHeader("User-Agent","SQRL/1");
            httppost.addHeader("Content-type", "application/x-www-form-urlencoded");


            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpClient.execute(httppost); // Execute HTTP Post Request

            int status = response.getStatusLine().getStatusCode();

            if (status == HttpStatus.SC_OK) {
                ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                response.getEntity().writeTo(ostream);

                String out = ostream.toString();
                //String result = new String(Helper.urlDecode(out),"UTF-8");

                resultSet.add(out);

                Log.v("web", out);
                // See if the page returned "Verified"
                //if (out.contains("Verified")) {
                //    return true; // return true if verified
                // }
                //according to spec if we have a status of OK that means it worked any other and it fails,
                //TODO I am not sure if other 200 class codes are also acceptable
                return true;
            }  else
            {
                //TODO turn off this logginglog output to
                ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                response.getEntity().writeTo(ostream);
                String out = ostream.toString();
                //Add a window for verbose transaction watching
                Log.v("web", out);
                Log.v("web", "Connection not ok");
            }
        } catch (ClientProtocolException e) {
            Log.e("web", "error");
            //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("web", "error");
            //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return false; // Return false if query did not return verification


    }

    /**
     *
     * @param URL - The URL to make the call to this would usually start with http(s)
     * @param ids - This is the signature of the URL and does not seem to be used anymore
     * @param cmd - The command as defined ehre (https://www.grc.com/sqrl/semantics.htm)
     * @param publicKey - The ed25519 public key
     * @param sK - The private key to sign the client server combination
     * @return nameValuePairs - The query parameters
     * @throws UnsupportedEncodingException
     */
    private  ArrayList<NameValuePair>  GenerateSQRLCall(String URL, String ids,String cmd, String publicKey, byte[] sK) throws UnsupportedEncodingException {
        ArrayList<NameValuePair> nameValuePairs= new ArrayList<NameValuePair>(4);
        String client = String.format("ver=%s\ncmd=%s\nidk=%s",AuthorizationRequest.CurrentAuthVersion,cmd,publicKey);
        client = Helper.urlEncode(client.getBytes());
        String server = Helper.urlEncode(URL.getBytes());
        String signature2 = client+server;
        nameValuePairs.add(new BasicNameValuePair("client",client));
        //we do not use the incoming parameter
        String ids2 = Helper.urlEncode(Helper.Sign(signature2.getBytes(), sK));
        nameValuePairs.add(new BasicNameValuePair("ids",ids2 ));
        nameValuePairs.add(new BasicNameValuePair("server",server));

        return nameValuePairs;
    }

    /**
     *
     * @param URL - The URL to make the call to this would usually start with http(s)
     * @param ids - This is the signature of the URL and does not seem to be used anymore
     * @param cmd - The command as defined ehre (https://www.grc.com/sqrl/semantics.htm)
     * @param publicKey - The ed25519 public key
     * @param sK - The private key to sign the client server combination
     * @return nameValuePairs - The query parameters
     * @throws UnsupportedEncodingException
     */
    private ArrayList<NameValuePair>  GenerateSQRLCall2(String URL, String ids,String cmd, String publicKey, byte[] sK) throws UnsupportedEncodingException {
        ArrayList<NameValuePair> nameValuePairs= new ArrayList<NameValuePair>(5);
        byte[] RandomLock = Helper.CreatePrivateKeyFromSeed(Helper.CreateRandom(32));
        byte[] ServerUnlock =Helper.PublicKeyFromPrivateKey(RandomLock);
        byte[] VerifyUnlock = new byte[32];
        byte[] EmtprySk = new byte[64];
        Sodium.crypto_sign_seed_keypair(VerifyUnlock, EmtprySk, Helper.DHKA3(current_sqrl_identity.identitylockkey, RandomLock));
        String client = String.format("ver=%s\ncmd=%s\nidk=%s\nsuk=%s\nvuk=%s\n",
                AuthorizationRequest.CurrentAuthVersion,cmd,publicKey,Helper.urlEncode(ServerUnlock),Helper.urlEncode(VerifyUnlock));
        client = Helper.urlEncode(client.getBytes());
        String server = Helper.urlEncode(URL.getBytes());
        String signature2 = client+server;
        nameValuePairs.add(new BasicNameValuePair("client",client));
        nameValuePairs.add(new BasicNameValuePair("server", server));
        nameValuePairs.add(new BasicNameValuePair("ids", Helper.urlEncode(Helper.Sign(signature2.getBytes(), sK))));
        return nameValuePairs;
    }

    /**
     *
     *
     * @param URL
     * @param message
     * @param signature
     * @param publicKey
     * @return
     */
    private boolean web_post(String URL, String message, String signature, String publicKey)
    {
        try {
            String client = String.format("ver=%s\ncmd=%s\nidk=%s\n", 1, "login", publicKey);
            String server = String.format("%s", URL);
            String ids = signature;
            client = String.format("client=%s&",Helper.urlEncode(client.getBytes()));
            server = String.format("server=%s&",Helper.urlEncode(server.getBytes()));
            ids = String.format("ids=%s",signature);
            java.net.URL openUrl = new java.net.URL(URL);
            HttpURLConnection urlConnection = (HttpURLConnection) openUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("User-Agent","SQRL/1");
            int length = client.getBytes().length+server.getBytes().length+ids.getBytes().length;
            urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(length));
            urlConnection.setRequestProperty("Content-Language", "en-US");

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

            out.write(client.getBytes());
            out.write(server.getBytes());
            out.write(ids.getBytes());
            int code = urlConnection.getResponseCode();
            String respMesage = urlConnection.getResponseMessage();
        } catch(IOException e) {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG);
        }

        return true;
    }
    // Send signature and pubkey to server
    private boolean web_post2(String URL, String message, String signature, String publicKey) throws MalformedURLException
    {
        HttpClientBuilder builder;
        HttpPostHC4 httppost;
        CloseableHttpClient httpClient;


        try
        {
            builder = HttpClientBuilder.create();
            //builder.setSSLSocketFactory( new SSLConnectionSocketFactory( SSLContext.getDefault()));
            httpClient = builder.build();
            httppost = new HttpPostHC4(URL);

            //httppost.addHeader("message",message);
            //httppost.addHeader("signature",signature);
            //httppost.addHeader("publicKey",publicKey);
            String client = String.format("ver=%s\ncmd=%s\nidk=%s",1,"login",publicKey);
            String server = String.format("%s",URL);
            String ids = signature;
            httppost.addHeader("client",Helper.urlEncode(client.getBytes()));
            httppost.addHeader("server",Helper.urlEncode(server.getBytes()));
            httppost.addHeader("ids",ids);

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
