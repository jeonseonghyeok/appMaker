package com.example.developer.appmaker;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WriteReview extends AsyncTask<String, Void, String> {
    //ProgressDialog progressDialog;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //  progressDialog = ProgressDialog.show(MainActivity.this,
        //          "Please Wait", null, true, true);
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // progressDialog.dismiss();
        Log.d("TAGPHP ", "POST response  - " + result);
    }


    @Override
    protected String doInBackground(String... params) {

        //int rcode = Integer.parseInt((String)params[1]);
        String rcode = (String)params[1];
        String tag = (String)params[2];
        String content =(String)params[3];
        String grade =(String)params[4];
       // float grade = (float) (Integer.parseInt((String)params[4])/10.0);
        String user_id =(String)params[5];



        String serverURL = (String)params[0];
        String postParameters =
                "rcode=" + rcode +
                "&tag=" + tag +
                "&content=" + content +
                "&grade=" + grade +
                "&user_id=" + user_id;

        try {
            Log.d("phpTest",postParameters);
            URL url = new URL(serverURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.connect();


            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(postParameters.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();


            int responseStatusCode = httpURLConnection.getResponseCode();

            InputStream inputStream;
            if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
            }
            else{
                inputStream = httpURLConnection.getErrorStream();
            }


            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }


            bufferedReader.close();

            return sb.toString();


        } catch (Exception e) {


            return new String("Error: " + e.getMessage());
        }

    }
}
