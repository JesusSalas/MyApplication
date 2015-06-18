package com.jesus.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jesus.myapplication.servicios.MiServicio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.View.OnClickListener;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        private static final String PROPERTY_REG_ID = "registration_id";
        private static final String PROPERTY_APP_VERSION = "appVersion";
        private static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
        private static final String PROPERTY_USER = "user";
        Button reg;
        EditText txtEmail,txtPasswd,txtPasswdConf;
        TextView txtLogin;
        String regId;
        GoogleCloudMessaging gcm;
        Context context;
        static final String TAG = "Register Activity";

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("AppData", Context.MODE_PRIVATE);
            String registrationId = sharedPreferences.getString(PROPERTY_REG_ID, "");
            String usu = sharedPreferences.getString(PROPERTY_USER, "");
            if (!registrationId.equals("") || !usu.equals("")){
                Intent intent = new Intent(getActivity().getApplicationContext(),IndexActivity.class);
                startActivity(intent);
            }

            MiServicio servicio = new MiServicio(getActivity().getApplicationContext());
            //servicio.setView(rootView.findViewById(R.id.textoUbicacion));

            txtEmail = (EditText)rootView.findViewById(R.id.txtEmail);
            txtPasswd = (EditText)rootView.findViewById(R.id.txtPasswd);
            txtPasswdConf = (EditText)rootView.findViewById(R.id.txtPasswdConf);
            txtLogin = (TextView)rootView.findViewById(R.id.txtLogin);
            reg = (Button)rootView.findViewById(R.id.btnRegistrar);

            txtLogin.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity().getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                }
            });

            reg.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    context = getActivity().getApplicationContext();
                    if(TextUtils.isEmpty(txtEmail.getText().toString())){
                        Toast.makeText(context,"Favor de proporcionar un email",Toast.LENGTH_SHORT).show();
                    }else if(TextUtils.isEmpty(txtPasswd.getText().toString())){
                        Toast.makeText(context,"Favor de escoger un password",Toast.LENGTH_SHORT).show();
                    }else if(TextUtils.isEmpty(txtPasswdConf.getText().toString())){
                        Toast.makeText(context,"Favor de confirmar el password",Toast.LENGTH_SHORT).show();
                    }else if(!txtPasswd.getText().toString().equals(txtPasswdConf.getText().toString())) {
                        Toast.makeText(context,"Los passwords no coinciden",Toast.LENGTH_SHORT).show();
                    }else{

                        //Chequemos si está instalado Google Play Services
                        if (checkPlayServices()) {
                            gcm = GoogleCloudMessaging.getInstance(context);

                            //Obtenemos el Registration ID guardado
                            regId = getRegistrationId(context);

                            //Si no disponemos de Registration ID comenzamos el registro
                            if (regId.equals("")) {
                                TareaRegistroGCM tarea = new TareaRegistroGCM(context);
                                tarea.execute(txtEmail.getText().toString(), txtPasswd.getText().toString());
                            }
                            Intent intent = new Intent(context,IndexActivity.class);
                            startActivity(intent);
                        } else {
                            Log.i(TAG, "No se ha encontrado Google Play Services.");
                        }
                    }
                }

            });

            return rootView;
        }
        private boolean checkPlayServices() {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity().getApplicationContext());
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                }else{
                    Log.i(TAG, "Dispositivo no soportado.");
                    getActivity().finish();
                }
                return false;
            }
            return true;
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
    }

}
