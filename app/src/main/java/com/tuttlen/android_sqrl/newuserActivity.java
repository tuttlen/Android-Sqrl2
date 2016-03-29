package com.tuttlen.android_sqrl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class newuserActivity extends Activity {
    private ArrayList<IdentityData> userIdentities=  new ArrayList<IdentityData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newuser);
        final Button cancelButton = (Button) findViewById(R.id.btnCancel);
        final EditText userEdit = (EditText) findViewById(R.id.publicKeyText);
        final EditText passEdit = (EditText) findViewById(R.id.signatureText);
        final EditText repassEdit = (EditText) findViewById(R.id.editText3);

        final Button button1 = (Button) findViewById(R.id.button1);
        final Button googleDrive = (Button) findViewById(R.id.driveButton);

        //TODO update this to use the new class
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String user = userEdit.getText().toString();
                String pass = passEdit.getText().toString();
                String repass = repassEdit.getText().toString();

                if (pass.compareTo(repass) == 0) { // Check that password and retype are the same
                    //identity id = new identity();
                    //id.createKeys();
                    //id.makeVerificationKey(pass);
                    //id.save(getApplicationContext());
                    finish();
                }

            }  });
        userIdentities = IdentityData.load(getApplicationContext());
        if(userIdentities == null || userIdentities.size() == 0)
        {
            cancelButton.setEnabled(false);

        } else {
            //There are already identites created so we can allow the user to cancel
            //There may possibly be a condition where the identity filed is corrupted
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent a = new Intent(newuserActivity.this, LoginActivity.class);
                    //startActivity(a);
                    Intent output = new Intent();
                    //output.putExtra("sqrlid",id);
                    setResult(Activity.RESULT_CANCELED, output);
                    finish();
                }
            });
        }

        googleDrive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent a = new Intent(newuserActivity.this, GoogleDriveActivity.class);
                startActivity(a);

            }  });

    }

}
