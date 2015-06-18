package com.jesus.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView txtRegister = (TextView)findViewById(R.id.txtRegister);
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private static final String SENDER_ID = "912215069822";
        private static final String TAG = "Login Activity";
        private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        private static final String PROPERTY_REG_ID = "registration_id";
        private static final String PROPERTY_APP_VERSION = "appVersion";
        private static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
        private static final String PROPERTY_USER = "user";
        private static final long EXPIRATION_TIME_MS = 1000 * 3600 * 24 * 7;
        private final String mEmail;
        private final String mPassword;
        String regId;
        Context context = getApplicationContext();

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            //Obtenemos el Registration ID guardado
            regId = getRegistrationId(context);
            boolean autenticado = false;
            try {
                autenticado = autenticar(mEmail,mPassword);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (autenticado){
                SharedPreferences prefs = context.getSharedPreferences(
                        "AppData",
                        Context.MODE_PRIVATE);

                int appVersion = getAppVersion(context);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PROPERTY_USER, mEmail);
                editor.putString(PROPERTY_REG_ID, regId);
                editor.putInt(PROPERTY_APP_VERSION, appVersion);
                editor.putLong(PROPERTY_EXPIRATION_TIME,
                        System.currentTimeMillis() + EXPIRATION_TIME_MS);

                editor.commit();
            }

            // TODO: register the new account here.
            return true;
        }

        private boolean autenticar(String usuario, String password) throws UnsupportedEncodingException {
            HttpClient httpclient = new DefaultHttpClient();
            // URL del servicio que almacenara la imagen
            HttpPost httppost = new HttpPost("http://192.168.1.73/movil/services.php?login=1");

            // Creamos los parámetros de la petición
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("usuario", usuario));
            nameValuePairs.add(new BasicNameValuePair("passwd", password));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            try {
                HttpResponse response = httpclient.execute(httppost);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200){
                    HttpEntity httpEntity = response.getEntity();
                    String retSrc = EntityUtils.toString(httpEntity);
                    JSONObject result = new JSONObject(retSrc);
                    JSONArray tokenList = result.getJSONArray("0");
                    JSONObject oj = tokenList.getJSONObject(0);
                    String reg = oj.getString("codigoGCM");
                    String msg;
                    if (regId.equals("")) {
                        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                        regId = gcm.register(SENDER_ID);
                    }
                    if (reg != regId) {

                        msg = updateGCM(usuario, regId);
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                    }
                    Log.d(TAG, EntityUtils.toString(httpEntity));
                    return true;
                }else{
                    Log.d(TAG,"statusCode: "+statusCode);
                    return false;
                }
            }catch (Exception e){
                Log.d(TAG,e.getLocalizedMessage());
                return false;
            }
        }

        private String updateGCM(String usu, String regId) throws UnsupportedEncodingException {
            HttpClient httpclient = new DefaultHttpClient();
            // URL del servicio que almacenara la imagen
            HttpPost httppost = new HttpPost("http://192.168.1.73/movil/services.php?updateGCM=1");

            // Creamos los parámetros de la petición
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("usuario", usu));
            nameValuePairs.add(new BasicNameValuePair("codigoGCM", regId));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            String msg = "Error al actualizar codigo GCM";

            try {
                HttpResponse response = httpclient.execute(httppost);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity httpEntity = response.getEntity();
                    String retSrc = EntityUtils.toString(httpEntity);
                    JSONObject result = new JSONObject(retSrc);
                    JSONArray tokenList = result.getJSONArray("0");
                    JSONObject oj = tokenList.getJSONObject(0);
                    msg = oj.getString("msg");
                    Log.d(TAG, EntityUtils.toString(httpEntity));
                } else
                    Log.d(TAG, "statusCode: " + statusCode);
                return msg;
            }catch (Exception e){
                Log.d(TAG,e.getLocalizedMessage());
            }
            return msg;
        }

        private String getRegistrationId(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(
                    "AppData",
                    Context.MODE_PRIVATE);
            String registrationId = prefs.getString(PROPERTY_REG_ID, "");
            if (registrationId.length() == 0) {
                Log.d(TAG, "Registro GCM no encontrado.");
                return "";
            }
            String registeredUser = prefs.getString(PROPERTY_USER, "user");
            int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
            long expirationTime = prefs.getLong(PROPERTY_EXPIRATION_TIME, -1);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String expirationDate = sdf.format(new Date(expirationTime));
            Log.d(TAG, "Registro GCM encontrado (usuario=" + registeredUser +
                    ", version=" + registeredVersion +
                    ", expira=" + expirationDate + ")");
            int currentVersion = getAppVersion(context);
            if (registeredVersion != currentVersion) {
                Log.d(TAG, "Nueva versión de la aplicación.");
                return "";
            }else if (System.currentTimeMillis() > expirationTime) {
                Log.d(TAG, "Registro GCM expirado.");
                return "";
            }
            return registrationId;
        }

        private int getAppVersion(Context context) {
            try {
                PackageInfo packageInfo = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0);
                return packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e){
                throw new RuntimeException("Error al obtener versión: " + e);
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

