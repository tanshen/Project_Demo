package com.iems5722.group3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {
    public static final String BASE_URL = "http://52.196.31.83/iems5722";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private static final String TAG2 = "Parameters";
    public static ArrayList<String> sidList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private boolean isReceiverRegistered;
    private TextView textView;
    private ListView listView;
    private SimpleAdapter adapter;
    private ArrayList<HashMap<String, Object>> list;
    private Socket socket;
    {
        try {
            socket = IO.socket("http://52.196.31.83:8000");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendUser(String token){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("sid", token);

//        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        PostMessageTask postMessageTask = new PostMessageTask(paramsMap);
        postMessageTask.execute(BASE_URL + "/send_user");


//        if (networkInfo != null && networkInfo.isConnected()){
//            postMessageTask.execute(BASE_URL + "/send_gobang");
//        }
//        else {
//            Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
//        }
    }

//    private Emitter.Listener onTextUpdate = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//            try {
//                JSONObject data = (JSONObject) args[0];
//                final String text = data.getString("text");
//                final String init = data.getString("init");
//                final String onlineCnt = data.getString("onlineCnt");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d(TAG, "From: " + text);
//                        Log.d(TAG, "init: " + init);
//                        Log.d(TAG, "onlineCnt: " + onlineCnt);
//                        if (sidList.contains(RegistrationIntentService.token)){
////                            GobangView.listenFlag = Integer.parseInt(init);
//                            GobangView.mCampTurn = sidList.indexOf(RegistrationIntentService.token)+1;
////                            GobangView.localNum = Integer.parseInt(onlineCnt);
//                        } else {
//                            GobangView.mCampTurn = 0;
//                            Toast.makeText(getApplicationContext(), "Already two players, you are watching now!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
////                        Log.d(TAG, "localNum: " + GobangView.localNum);
////                        Log.d(TAG, "GobangView.listenFlag: " + GobangView.listenFlag);
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    };


//    private Emitter.Listener getGobangUser = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//            runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                JSONObject data = null;
//                try {
//                    data = new JSONObject((String) args[0]);
////                    JSONObject data = (JSONObject) args[0];
//                    final String sid = data.getString("sid");
////                    runOnUiThread(new Runnable() {
//
//                    Log.d(TAG, "listensid: " + sid);
//                    if (sidList.contains(RegistrationIntentService.token)) {
////                            GobangView.listenFlag = Integer.parseInt(init);
////                            GobangView.localNum = Integer.parseInt(onlineCnt);
//                        GobangView.mCampTurn = sidList.indexOf(RegistrationIntentService.token) + 1;
//
//                    } else {
//                        GobangView.mCampTurn = 0;
////                            GobangView.listenFlag = 11;
//                        Toast.makeText(getApplicationContext(), "Already two players, you are watching now!", Toast.LENGTH_SHORT).show();
//                    }
//                    Log.e(TAG, "mCampTurn: " + GobangView.mCampTurn);
//                    Log.d(TAG, "GobangView.listenFlag: " + GobangView.listenFlag);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            });
//        }
//    };
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        socket.disconnect();
////        socket.off("update", onTextUpdate);
//        socket.off("get_gobang_user", getGobangUser);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);
        // Registering BroadcastReceiver
        registerReceiver();
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        list = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.cuhk48);
        toolbar.setTitle(getResources().getString(R.string.main_label));
        textView = (TextView)findViewById(R.id.tvText);
        // listView = (ListView)findViewById(R.id.chatroom_list);
        adapter = new SimpleAdapter(this, list, R.layout.content_chatrooms,
                new String[]{"name","id"},
                new int[]{R.id.chatroom_name});

        sidList = new ArrayList<>();
//        sendUser(RegistrationIntentService.token);
        Button bnt_gobang = (Button) findViewById(R.id.gobang);
        bnt_gobang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sendUser(RegistrationIntentService.token);
//                getUser();
                if (sidList.contains(RegistrationIntentService.token)) {
                    GobangView.mCampTurn = sidList.indexOf(RegistrationIntentService.token) + 1;

                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, GobangActivity.class);
                    startActivity(intent);

                } else {
                    GobangView.mCampTurn = 0;
                    Toast.makeText(getApplicationContext(), "Already two players, you are watching now!", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG2, "mCampTurn: " +  GobangView.mCampTurn);
                Log.d(TAG2, "MaincurrentTurn: " +  GobangView.currentTurn);

                // MainActivity.this.finish();
            }
        });

//        socket.on("updateComing", onTextUpdate);
//        socket.on("get_gobang_user", getGobangUser);
//        socket.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void myClickHandler() {
        // Gets the URL from the UI's text field.
        //String stringUrl = urlText.getText().toString();
        String stringUrl = "http://52.196.31.83/iems5722/get_chatrooms";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetChatroomTask().execute(stringUrl);
        } else {
            textView.setText("No network connection available.");
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

    public void getUser() {
        String stringUrl = BASE_URL + "/send_user";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetMsgTask().execute(stringUrl);
        } else {
            Toast.makeText(getApplicationContext(),
                    "No network connection available.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private static class PostMessageTask extends AsyncTask<String, Void, String> {
        private Map<String, String> paramsMap;

        public PostMessageTask(Map<String, String> paramsMap){
            this.paramsMap = paramsMap;
        }

        @Override
        protected String doInBackground(String... urls){
            try {
                return uploadUrl(urls[0], paramsMap);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        protected void onPostExecute(String result){
            try {
                JSONObject json = new JSONObject(result);
                String data = json.getString("data");
                JSONObject json_data = new JSONObject(data);
                JSONArray array = json_data.getJSONArray("sid");
                for (int i = 0; i < array.length() ; i++){
                    String sid = array.getJSONObject(i).getString("sid");
                    sidList.add(sid);
                }
//               if (status.equals("ERROR")){
////                    Toast.makeText(getApplicationContext(), "Cannot post the message!", Toast.LENGTH_SHORT).show();
//                }
                Log.d(TAG, "sidList: " + sidList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String uploadUrl(String myurl, final Map<String, String> paramsMap) throws IOException{
            InputStream is = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                Uri.Builder builder = new Uri.Builder();
                for (String key : paramsMap.keySet()){
                    builder.appendQueryParameter(key, paramsMap.get(key));
                }
                String query = builder.build().getEncodedQuery();

                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                String results = "";
                is = conn.getInputStream();
                if (responseCode == HttpURLConnection.HTTP_OK){
                    String line;
                    BufferedReader br = new BufferedReader( new InputStreamReader(is));
                    while ((line = br.readLine()) != null) {
                        results += line;
                    }
                }
                return results;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            } finally {
                if (is != null){
                    is.close();
                }
            }
        }
    }

    private class GetChatroomTask extends AsyncTask <String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            // textView.setText(result);
            JSONObject json = null;
            try {
                json = new JSONObject(result);
                String status = json.getString("status");
                JSONArray array = json.getJSONArray("data");
                if (status.equals("OK")) {
                    list.clear();
                    for (int i = 0; i < array.length(); i++) {
                        HashMap<String, Object> tmp = new HashMap<>();
                        String id = array.getJSONObject(i).getString("id");
                        String name = array.getJSONObject(i).getString("name");
                        tmp.put("name", name);
                        tmp.put("id", id);
                        list.add(tmp);
                    }
                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                // Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String data = "";
                String results="";
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    results += line;
                }
                return results;
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    private class GetMsgTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            JSONObject json = null;
            try {
                json = new JSONObject(result);
                JSONObject data = json.getJSONObject("data");
                JSONArray array = data.getJSONArray("sid");
                for (int i = 0; i < array.length() ; i++){
                    String sid = array.getJSONObject(i).getString("sid");
                    sidList.add(sid);
                }
//                if (status.equals("ERROR")){
////                    Toast.makeText(getApplicationContext(), "Cannot post the message!", Toast.LENGTH_SHORT).show();
//                }
                Log.d(TAG, "sidList: " + sidList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();
                // Convert the InputStream into a string
                String results="";
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    results += line;
                }
                return results;
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }
}
