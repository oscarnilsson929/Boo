package com.boo.app.api;

import android.util.Log;

import com.boo.app.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connect implements HttpUrlManager, AppConfig{
    private static Connect mSharedInstance = null;

    public static Connect getInstance() {

        if (mSharedInstance == null) {
            mSharedInstance = new Connect();
        }
        return mSharedInstance;
    }

    public JSONObject getJSONObject(String strUrl, JSONObject jsonParam) {
        JSONObject result = null;
        HttpURLConnection connection;

        Log.d(APP_NAME, "URL : " + strUrl);
        Log.d(APP_NAME, "Request : " + jsonParam.toString());

        try {
            URL url = new URL(strUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
            connection.setRequestProperty("api-key", API_KEY);
            connection.connect();

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(jsonParam.toString());
            wr.flush();
            wr.close();

            InputStream ins = connection.getInputStream();

            if(ins != null) {
                result = convertInputStreamToJSON(ins);
                ins.close();
            }

            connection.disconnect();
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        Log.d(APP_NAME, result == null ? "Response : NULL" : "Response : " + result.toString());

        return result;
    }

    private static JSONObject convertInputStreamToJSON(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();

        JSONObject jObject= null;

        try {
            jObject = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject;
    }
}
