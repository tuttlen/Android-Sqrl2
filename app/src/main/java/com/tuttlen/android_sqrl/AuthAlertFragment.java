package com.tuttlen.android_sqrl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by nathan on 4/7/16.
 */
public class AuthAlertFragment extends DialogFragment{

    public AlertDialog.Builder theBuilder;
    protected String authDomain;
    protected String authSFN;

    protected  AuthorizationRequest authorizationRequest;
    protected Activity theActivity;

    public AuthAlertFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void setArguments(Bundle args)
    {
        this.authDomain =args.getString("domain");
        this.authSFN = args.getString("sfn");
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        theBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog dialog = theBuilder.create();
        //theBuilder.setView(inflater.inflate(R.layout.activity_authenticate, null));


        dialog.setContentView(R.layout.activity_authenticate);
        try {
            theBuilder.setTitle("Authenticate To");
            //builder.setMessage(String.format("Authenticate to %s known as %s", domain, Helper.urlDecode(sfn)));
            TextView tvDomain = (TextView) dialog.findViewById(R.id.tvDomain);
            TextView tvSFN = (TextView) dialog.findViewById(R.id.tvSFN);
            tvDomain.setText(authDomain);
            tvSFN.setText(authSFN);
        }
        catch(Exception e)
        {

        }
        return dialog;

    }


}
