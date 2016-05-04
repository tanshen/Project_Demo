package com.iems5722.group3;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;



/**
 * Created by tanshen on 2016/2/23.
 */
public class ChatroomActivity extends AppCompatActivity {
    private ListView list_view;
    private EditText edit_text;
    private ImageButton btn_send;
    private ArrayList<ChatContent> ChatMsgArray = new ArrayList<ChatContent>();
    private ChatAdapter adapter;
    private String chatroomId;
    private String chatroomName;
    private int total_pages;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        chatroomId = getIntent().getExtras().getString("id");
        chatroomName = getIntent().getExtras().getString("name");
        toolbar.setTitle(chatroomName);
        Log.e("name", chatroomName);
        list_view = (ListView) findViewById(R.id.chat_list);
        edit_text = (EditText) findViewById(R.id.editText);
        btn_send = (ImageButton) findViewById(R.id.imageButton);

        GetPageContent(1);
        list_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int firstVisibleItem;
            private int page = 1;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState== AbsListView.OnScrollListener.SCROLL_STATE_IDLE && firstVisibleItem==0 && page < total_pages) {
                    page++;
                    GetPageContent(page);
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.firstVisibleItem=firstVisibleItem;
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });
        adapter = new ChatAdapter(this, ChatMsgArray);
        list_view.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_refresh:
                ChatMsgArray.clear();
                GetPageContent(1);
                Toast.makeText(getApplicationContext(),
                        "Message Refreshed!", Toast.LENGTH_SHORT).show();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
    private void send()
    {
        String name = "Tan Shen";
        String SId="1155066083";
        String context = edit_text.getText().toString();
        context = context.trim();
        HashMap<String, String> sendMsg = new HashMap<>();
        if (context.length() > 0) {
            ChatContent ChatMsg = new ChatContent();
            ChatMsg.setMessage(context);
            ChatMsg.setTimestamp(getTimestamp());
            ChatMsg.setName(name);
            ChatMsg.setId(SId);
            ChatMsgArray.add(ChatMsg);
            adapter.notifyDataSetChanged();
            edit_text.setText("");
            list_view.setSelection(list_view.getCount() - 1);
            sendMsg.put("chatroom_id", chatroomId);
            sendMsg.put("user_id", SId);
            sendMsg.put("name", name);
            sendMsg.put("message", context);
            new SendMsgTask(sendMsg).execute("http://52.196.31.83/iems5722/send_message");
        }
    }
    protected String getTimestamp(){
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return df.format(date);
    }

    public void GetPageContent(int page) {
        // Gets the URL from the UI's text field.
        //String stringUrl = urlText.getText().toString();
        String stringUrl = "http://52.196.31.83/iems5722/get_messages?chatroom_id="
                +chatroomId + "&page=" + page;
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
            // textView.setText(result);
            JSONObject json = null;
            try {
                json = new JSONObject(result);
                JSONObject data = json.getJSONObject("data");
                int current_page = data.getInt("current_page");
                JSONArray messages = data.getJSONArray("messages");
                total_pages = data.getInt("total_pages");
                String status = json.getString("status");
                if (status.equals("OK")) {
                    Log.e("chat", result);

                    for (int i = 0; i < messages.length(); i++) {
                        String message = messages.getJSONObject(i).getString("message");
                        String id = messages.getJSONObject(i).getString("user_id");
                        String name = messages.getJSONObject(i).getString("name");
                        String timestamp = messages.getJSONObject(i).getString("timestamp");
                        Log.e("chat1", "message= " + message);
                        ChatContent ChatMsg = new ChatContent();
                        ChatMsg.setMessage(message);
                        ChatMsg.setTimestamp(timestamp);
                        ChatMsg.setName(name);
                        ChatMsg.setId(id);
                        ChatMsgArray.add(0,ChatMsg);
                        list_view.setSelection(list_view.getCount() - 1);
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
    private class SendMsgTask extends AsyncTask<String, Void, String> {
        private HashMap<String, String> sendMsg;

        public SendMsgTask (HashMap<String, String> sendMsg){
            this.sendMsg = sendMsg;
        }
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return postmsg(urls[0],sendMsg);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
        }
        private String postmsg(String url,HashMap<String, String> sendMsg) throws MalformedURLException {
            try {
                URL url_object = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) url_object.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                Uri.Builder builder = new Uri.Builder();
                for (String str : sendMsg.keySet()) {
                    builder.appendQueryParameter(str, sendMsg.get(str));
                }
                String query = builder.build().getEncodedQuery();
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "OK";
        }
    }
}