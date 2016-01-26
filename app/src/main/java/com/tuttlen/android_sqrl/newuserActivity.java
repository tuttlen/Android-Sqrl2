package com.tuttlen.android_sqrl;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class newuserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newuser);

        final EditText userEdit = (EditText) findViewById(R.id.editText1);
        final EditText passEdit = (EditText) findViewById(R.id.editText2);
        final EditText repassEdit = (EditText) findViewById(R.id.editText3);

        final Button button1 = (Button) findViewById(R.id.button1);
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
    }

}
