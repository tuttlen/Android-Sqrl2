package com.tuttlen.android_sqrl;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

//import org.apache.http.NameValuePair;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (Spinner) findViewById(R.id.userSpinner);
        final EditText passEdit = (EditText) findViewById(R.id.publicKeyText);

        // Add listener on loginbutton
        final Button loginButton = (Button) findViewById(R.id.button1);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String user = users.get(username.getSelectedItemPosition());
                String pass = passEdit.getText().toString();

                //identity id = loadIdentity(user, pass);
                //this will be part of the password verification process which calls Scrypt
                IdentityData id = IdentityData.selectIdentity(userIdentities,user);

                if (id == null) {
                    Toast.makeText(getApplicationContext(), "Wrong password", Toast.LENGTH_LONG).show();
                }
                else
                {
                    // Send object back to parent
                    Intent output = new Intent();
                    output.putExtra("sqrlid",id);
                    setResult(Activity.RESULT_OK, output);
                    finish();
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
        }
        /*
        //TODO this will be a screen that calls an API to get SQRL data
        if (!id.isIdentityCreated(this.getApplicationContext())) {
            // If not open newidActivity
            Intent a = new Intent(LoginActivity.this, newuserActivity.class);
            startActivity(a);
        }
        */
        addUsersToSpinner(userIdentities);
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
        for(IdentityData item : id) {
            users.add(item.name);
        }


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, users);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        username.setAdapter(dataAdapter);
    }
}

