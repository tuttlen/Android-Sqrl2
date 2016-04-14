package com.tuttlen.android_sqrl;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

//import org.apache.http.NameValuePair;

import com.android.internal.util.Predicate;
import com.tuttlen.aesgcm_android.AESGCMJni4;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {
    private String TAG = "loginAct";
    private List<String> users = new ArrayList<String>(); // List of usernames
    private ArrayList<IdentityData> userIdentities=  new ArrayList<IdentityData>();
    private Spinner username;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final AESGCMJni4 aesCrypto = new AESGCMJni4();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (Spinner) findViewById(R.id.userSpinner);
        final EditText passEdit = (EditText) findViewById(R.id.publicKeyText);

        // Add listener on loginbutton
        final Button loginButton = (Button) findViewById(R.id.button1);


        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String user = users.get(username.getSelectedItemPosition());
                final String pass = passEdit.getText().toString();
                final IdentityData id = IdentityData.selectIdentity(userIdentities,user);

                try {
                    final SqrlData data = IdentityData.LoadSqrlData(id);
                    final ProgressDialog pDialog = new ProgressDialog(v.getContext());
                    //final ProgressBar pBar = new ProgressBar(v.getContext());
                    //pBar.setMax(data.sqrlStorage.ScryptIteration);
                    pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pDialog.setMax(data.sqrlStorage.ScryptIteration);


                    final Predicate<Integer> runthis = new Predicate<Integer>() {
                        @Override
                        public boolean apply(Integer integer) {
                            pDialog.setProgress(integer);
                            return true;
                        }
                    };

                    final byte[]  keyresult = new byte[32];
                    Thread runScrypt = new Thread() {
                        @Override
                        public void run() {
                            try {
                                byte[] scryptResult = Helper.PK(pass.getBytes(), data.sqrlStorage.ScryptSalt, data.sqrlStorage.ScryptIteration, new byte[]{}, 1 << data.sqrlStorage.nFactor, runthis);
                                //TODO another way to do this that won't take all this manipulation, need to create another extraction element
                                String ciphertext = Helper.bytesToHex(data.sqrlStorage.IDMK)+Helper.bytesToHex(data.sqrlStorage.IDLK);
                                String unencryptedResult = aesCrypto.doDecryption(scryptResult, data.sqrlStorage.IV, data.type1aad, data.sqrlStorage.tag, Helper.hexStringToByteArray(ciphertext));

                                if (!Helper.determineAuth(unencryptedResult) && false) { //for debug purposes
                                    Toast.makeText(getApplicationContext(), "Wrong password", Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    //id.sqrlIdentity =data;
                                    id.unecryptedMasterKey = Helper.hexStringToByteArray(unencryptedResult.substring(0,64));
                                    id.identitylockkey = Helper.hexStringToByteArray(unencryptedResult.substring(64,128));
                                    // Send object back to parent
                                    Intent output = new Intent();
                                    output.putExtra("sqrlid",id);
                                    setResult(Activity.RESULT_OK, output);
                                    finish();
                                    //Intent a = new Intent(LoginActivity.this, MainActivity.class);
                                    //startActivity(a);
                                }
                                pDialog.dismiss();
                            } catch(GeneralSecurityException ex)
                            {
                                Toast.makeText(getApplicationContext(), "Security failed", Toast.LENGTH_LONG).show();
                            }
                        }

                    };
                    pDialog.show(LoginActivity.this,"Unencrypting","Unencrypting...");

                    runScrypt.start();


                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "Failed to load SQRLdata", Toast.LENGTH_LONG).show();
                }


            }  });

        // Add listener on new user button button
        final Button regButton = (Button) findViewById(R.id.button2);
        regButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Open new activity to register new user
                Intent a = new Intent(LoginActivity.this, newuserActivity.class);
                startActivity(a);
            }  });


        // Check if an identity is created
        //identity id = new identity();
        userIdentities = IdentityData.load(getApplicationContext());
        if(userIdentities == null)
        {
            // If not open newidActivity
            Intent a = new Intent(LoginActivity.this, newuserActivity.class);
            startActivity(a);
        } else {

            addUsersToSpinner(userIdentities);

        }
    }

    private identity loadIdentity(String user, String passwd) {
        identity id = new identity();

        // TODO: Check if that user exists

        // load the identity
        id.load(this.getApplicationContext());
        if (id.deriveMasterKey(passwd)) {
            return id;
        }
        else
        {
            return null;
        }
    }

    // adds items to username list (spinner)
    public void addUsersToSpinner() {
        users.add("User 1");
        users.add("User 2");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, users);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        username.setAdapter(dataAdapter);
    }

    // adds items to username list (spinner)
    public void addUsersToSpinner(ArrayList<IdentityData> id) {

        if(id == null || id.size() == 0)
        {
            Intent a = new Intent(LoginActivity.this, newuserActivity.class);
            startActivity(a);

        } else {

            for (IdentityData item : id) {
                users.add(item.name);
            }


            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, users);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            username.setAdapter(dataAdapter);
        }
    }
}

