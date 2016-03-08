package com.tuttlen.android_sqrl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class newuserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newuser);

        final EditText userEdit = (EditText) findViewById(R.id.publicKeyText);
        final EditText passEdit = (EditText) findViewById(R.id.signatureText);
        final EditText repassEdit = (EditText) findViewById(R.id.editText3);

        final Button button1 = (Button) findViewById(R.id.button1);
        final Button googleDrive = (Button) findViewById(R.id.driveButton);

        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String user = userEdit.getText().toString();
                String pass = passEdit.getText().toString();
                String repass = repassEdit.getText().toString();

                if (pass.compareTo(repass) == 0) { // Check that password and retype are the same
                    identity id = new identity();
                    id.createKeys();
                    id.makeVerificationKey(pass);
                    id.save(getApplicationContext());
                    finish();
                }

            }  });

        googleDrive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent a = new Intent(newuserActivity.this, GoogleDriveActivity.class);
                startActivity(a);

            }  });

    }

}
