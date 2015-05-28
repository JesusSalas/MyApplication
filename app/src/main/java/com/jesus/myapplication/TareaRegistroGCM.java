package com.jesus.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JesúsHumberto on 19/05/2015.
 */
public class TareaRegistroGCM extends AsyncTask<String,Integer,String> {
    private static final String SENDER_ID = "912215069822";
    static final String TAG = "Register Activity";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
    private static final String PROPERTY_USER = "user";
    private static final long EXPIRATION_TIME_MS = 1000 * 3600 * 24 * 7;
    GoogleCloudMessaging gcm;
    Context context;
    String regId;
    @Override
    protected String doInBackground(String... params) {
        String msg = "";
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            //Nos registramos en los servidores de GCM
            regId = gcm.register(SENDER_ID);
            Log.d(TAG, "Registrado en GCM: registration_id=" + regId);
            //Nos registramos en nuestro servidor
            boolean registrado = registroServidor(params[0],params[1],params[2], regId);

            //Guardamos los datos del registro
            if(registrado) {
                setRegistrationId(context, params[0], regId);
            }
        }catch (IOException ex) {
            Log.d(TAG, "Error registro en GCM:" + ex.getMessage());
        }
        return msg;
    }

    private void setRegistrationId(Context context, String usuario, String regId) {
        SharedPreferences prefs = context.getSharedPreferences(
                MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);

        int appVersion = getAppVersion(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_USER, usuario);
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.putLong(PROPERTY_EXPIRATION_TIME,
                System.currentTimeMillis() + EXPIRATION_TIME_MS);

        editor.commit();
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

    private boolean registroServidor(String usuario,String email,String passwd, String regId) {

        try{
            HttpClient httpclient = new DefaultHttpClient();
            // URL del servicio que almacenara la imagen
            HttpPost httppost = new HttpPost("http://192.168.1.73/movil/services.php?registro=1");

            // Creamos los parámetros de la petición
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("usuario", usuario));
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("passwd", passwd));
            nameValuePairs.add(new BasicNameValuePair("regGCM", regId));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Ejecutamos la petición
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity ent = response.getEntity();
            Toast.makeText(context, EntityUtils.toString(ent),Toast.LENGTH_SHORT).show();

            return true;
        }catch (Exception e){
            Log.d(TAG, "Error en petición HTTP:"+e);
            return false;
        }
    }
}
