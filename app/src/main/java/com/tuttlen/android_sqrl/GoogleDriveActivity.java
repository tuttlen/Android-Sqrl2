package com.tuttlen.android_sqrl;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.drive.DriveScopes;

import com.google.api.services.drive.model.*;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoogleDriveActivity extends Activity {
    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private ListView identityList;
    private IdentitiesAdapter adapter;
    private Button btnAddIds;
    private Button btnCancelIds;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE_READONLY };

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);


        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        activityLayout.addView(mOutputText);
        identityList = new ListView(this);
        identityList.setLayoutParams(tlp);
        identityList.setPadding(16, 16, 32, 32);
        activityLayout.addView(identityList);

        btnAddIds = new Button(this);
        btnAddIds.setLayoutParams(tlp);
        btnAddIds.setEnabled(false);
        btnAddIds.setText("Import identities");
        btnAddIds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                com.google.api.services.drive.Drive mService = new com.google.api.services.drive.Drive.Builder(
                        transport, jsonFactory, mCredential)
                        .setApplicationName("Google Drive")
                        .build();

                //todo execute
                ExportRequestTask newTask = new ExportRequestTask(mService);
                newTask.execute();
            }
        });
        btnCancelIds = new Button(this);
        btnCancelIds.setLayoutParams(tlp);
        btnCancelIds.setEnabled(true);
        btnCancelIds.setText("Cancel");
        btnCancelIds.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent a = new Intent(GoogleDriveActivity.this, LoginActivity.class);
                                                startActivity(a);
                                            }
                                        }
        );
        activityLayout.addView(btnAddIds);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Drive API ...");

        setContentView(activityLayout);
        //setContentView(R.layout.activity_google_drive);

        //identityList = (ListView)findViewById(R.id.lstIdentityList);
/*
        identityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("UI", String.format("onItemClick: %1$d,%2$s", position,adapter.getItem(position)));
                adapter.setItem(position,view,parent);
                adapter.notifyDataSetChanged();
            }
        });
        */
        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    public class IdentitiesAdapter extends ArrayAdapter<IdentityData> {
        public ArrayList<IdentityData> currentIds =null;
        public IdentitiesAdapter(Context context, ArrayList<IdentityData> identity) {
            super(context, 0, identity);
            this.currentIds = identity;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            IdentityData identity = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }
            // Lookup view for data population
            //TextView idName = (TextView) convertView.findViewById(R.id.IdentityName);
            CheckBox bUse = (CheckBox) convertView.findViewById(R.id.useIdentity);
            // Populate the data into the template view using the data object

            bUse.setText(identity.name);
            bUse.setChecked(identity.checked);
            bUse.setTextColor(Color.rgb(92, 179, 255));
            convertView.setTag(bUse);
            bUse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    IdentityData identity = (IdentityData) cb.getTag();
                    //identity.setSelected(cb.isChecked());
                    identity.checked = !identity.checked;
                    cb.setChecked(identity.checked);

                }
            });
            bUse.setTag(identity);
            // Return the completed view to render on screen
            return convertView;
        }

        public void setItem(int position, View convertView, ViewGroup parent) {
            IdentityData identity = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }
            CheckBox bUse = (CheckBox) convertView.findViewById(R.id.useIdentity);
            if(bUse.isChecked())
            {
                bUse.setChecked(false);
            } else {
                bUse.setChecked(true);
            }
        }
    }


    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            mOutputText.setText("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    mOutputText.setText("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Drive API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(mCredential).execute();
            } else {
                mOutputText.setText("No network connection available.");
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                GoogleDriveActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class ExportRequestTask extends AsyncTask<Void, Void,ArrayList<IdentityData>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;
        private ArrayList<IdentityData> idsToImport =new ArrayList<IdentityData>();

        public ExportRequestTask(com.google.api.services.drive.Drive theService)
        {
            mService =theService;
        }

        /**
         * Background task to call Drive API. For file export
         * @param params no parameters needed for this task.
         */
        @Override
        protected ArrayList<IdentityData> doInBackground(Void... params) {
            try {

                for (IdentityData id:  adapter.currentIds) {
                    if(id.checked) {
                        //Toast.makeText(getApplicationContext(),String.format("Selected: %1$s:( %2$s )",id.name,id.Id),Toast.LENGTH_LONG );
                        try {
                            ByteArrayOutputStream hold = new ByteArrayOutputStream();

                            mService.files().get(id.Id).executeMediaAndDownloadTo(hold);
                            id.idContents = hold.toByteArray();
                            //mService.files().export(id.Id, id.mimeType)
                             //       .executeMediaAndDownloadTo(id.idContents);
                        } catch(Exception e)
                        {
                            Toast.makeText(getApplicationContext(),String.format("Selected: %1$s:( %2$s ) FAILED!",id.name,id.Id),Toast.LENGTH_LONG );
                        }
                    }
                }
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
            return idsToImport;
        }

        @Override
        protected void onPostExecute(ArrayList<IdentityData> identities) {
            Log.i("DRIVE", "Processing and saving ..." + identities.size());
            //TODO why is identities empty
            ArrayList<IdentityData> saveIds = new ArrayList<IdentityData>();
            mProgress.setMessage("Saving imported identities to local ...");
            mProgress.show();
            //TODO: collecting, I would like to use a lambda but just not enough java knowledge plus extra complexity
            for (IdentityData data: adapter.currentIds ) {
                if(data.checked && (data.idContents !=null || data.idContents.length ==0)) {
                    saveIds.add(data);
                } else if(data.checked && (data.idContents == null || data.idContents.length ==0)) {
                    Log.i("DRIVE", String.format("Id %1$s could not be saved!",data.name));
                }
            }

            IdentityData.save(getApplicationContext(),saveIds);
            mProgress.hide();
            Intent a = new Intent(GoogleDriveActivity.this, LoginActivity.class);
            startActivity(a);

        }

    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void,ArrayList<IdentityData>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            //HttpTransport transport = AndroidHttp.newCompatibleTransport();
            //JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Drive")
                    .build();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected ArrayList<IdentityData> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private ArrayList<IdentityData> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            //setFields("nextPageToken, items(id, title)")
            //List<String> fileInfo = new ArrayList<String>();
            ArrayList<IdentityData> ids = new ArrayList<IdentityData>();
            FileList result = mService.files().list()
                    .setQ("fileExtension= 'sqrl'")
                    .setFields("nextPageToken,files(id,name)").execute();


            //.setFields("nextPageToken, items(id, name)")
            List<File> files = result.getFiles();

            if (files != null) {
                for (File file : files) {
                    //fileInfo.add(String.format("%s (%s)\n", file.getName(), file.getId()));
                    ids.add(new IdentityData(file.getName(), file.getId(),file.getMimeType(),false));
                }
            }
            return ids;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(ArrayList<IdentityData> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                //output.add(0, "Data retrieved using the Drive API:");
                //mOutputText.setText(TextUtils.join("\n", output));
                 /*adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_expandable_list_item_1, output);
                        */
                //ArrayList<IdentityData> foundIdentities= new ArrayList<IdentityData>();
                adapter = new IdentitiesAdapter(getApplicationContext(),output);

                identityList.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                if(adapter.currentIds.size() > 0) {
                    btnAddIds.setEnabled(true);
                }
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GoogleDriveActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }
}