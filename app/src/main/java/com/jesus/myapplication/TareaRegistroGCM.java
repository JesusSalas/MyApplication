package com.jesus.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

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

    TareaRegistroGCM(Context context){
        this.context = context;
    }

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
            boolean registrado = registroServidor(params[0],params[1], regId);

            //Guardamos los datos del registro
            if(registrado) setRegistrationId(context, params[0], regId);
        }catch (IOException ex) {
            Log.d(TAG, "Error registro en GCM:" + ex.getMessage());
        }
        return msg;
    }

    private void setRegistrationId(Context context, String usuario, String regId) {
        SharedPreferences prefs = context.getSharedPreferences(
                "AppData",
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

    public boolean registroServidor(String email,String passwd, String regId) throws IOException {

        HttpClient httpclient = new DefaultHttpClient();
        // URL del servicio que almacenara la imagen
        HttpPost httppost = new HttpPost("http://192.168.1.73/movil/services.php?registro=1");

        // Creamos los parámetros de la petición
        List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("usuario", email));
        nameValuePairs.add(new BasicNameValuePair("passwd", passwd));
        nameValuePairs.add(new BasicNameValuePair("regGCM", regId));

        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        try {
            HttpResponse response = httpclient.execute(httppost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200){
                HttpEntity httpEntity = response.getEntity();
                //Toast.makeText(context, EntityUtils.toString(httpEntity),Toast.LENGTH_SHORT).show();
                Log.d(TAG,EntityUtils.toString(httpEntity));
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
}
