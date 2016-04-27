package hk.edu.cuhk.ie.iems5722.a2_1155066083;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by tanshen on 2016/4/23.
 */
public class GobangActivity extends AppCompatActivity {
    private static final String TAG = "ClientSocketIO";
    public static final String BASE_URL = "http://52.196.31.83/iems5722";
    GobangView gobangView = null;

    private Socket socket;
    private Emitter.Listener getGobangListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = new JSONObject((String) args[0]);
                        String id = data.getString("id");
                        String var = data.getString("var");
                        String flagInt = data.getString("flagInt");

                        GobangView.listenFlag = Integer.parseInt(flagInt);
                        GobangView.addToFlag.add(GobangView.listenFlag);
                        GobangView.flag[0] = GobangView.addToFlag.get(0);

                        int tmp = Integer.parseInt(id);
                        final int x = (tmp-1) %  9;
                        final int y = (tmp-1) / 9;
                        int campTurn = Integer.parseInt(var);
                        if (campTurn == GobangView.CAMP_HERO) {
                            GobangView.mGameMap[y][x] = GobangView.CAMP_HERO;
                            if (GobangView.CheckPiecesMeet(GobangView.CAMP_HERO)){//HERO win
                                GobangView.mCampWinner = R.string.Role_black;
                                GobangView.mGameState = GobangView.GS_END;
                                GobangView.mCampWinner = GobangView.CAMP_HERO;
                                ItemClear itemState = new ItemClear(GobangView.mGameState + "");
                                GobangView.sendState(itemState);
                            }else {
                                GobangView.mCampTurn = GobangView.CAMP_ENEMY;
                            }
                        }
                        else{
                            GobangView.mGameMap[y][x] = GobangView.CAMP_ENEMY;
                            if (GobangView.CheckPiecesMeet(GobangView.CAMP_ENEMY)){
                                GobangView.mCampWinner = R.string.Role_white;
                                GobangView.mGameState = GobangView.GS_END;
                                GobangView.mCampWinner = GobangView.CAMP_ENEMY;
                                ItemClear itemState = new ItemClear(GobangView.mGameState + "");
                                GobangView.sendState(itemState);
                            }else {
                                GobangView.mCampTurn = GobangView.CAMP_HERO;
                            }
                        }

                        if(GobangView.mCampWinner != 0) {
                            ItemClear item = new ItemClear(GobangView.mCampWinner + "");
                            GobangView.sendWinner(item);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener getGobangClearListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = null;
                    try {
                        data = new JSONObject((String) args[0]);
                        String clear_winner = data.getString("clear_winner");
                        GobangView.mCampWinner = Integer.parseInt(clear_winner);
                        GobangView.mGameMap = new int[GobangView.CHESS_HEIGHT][GobangView.CHESS_WIDTH];
                        GobangView.listenFlag = 0;
                        GobangView.addToFlag.clear();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };
    private Emitter.Listener getGobangStateListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = null;
                    try {
                        data = new JSONObject((String) args[0]);
                        String state = data.getString("state");
                        GobangView.mGameState = Integer.parseInt(state);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };
    private Emitter.Listener onTextUpdate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                final String text = data.getString("text");
                final String init = data.getString("init");
                final String onlineCnt = data.getString("onlineCnt");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "From: " + text);
                        Log.d(TAG, "init: " + init);
                        Log.d(TAG, "onlineCnt: " + onlineCnt);
                        if (Integer.parseInt(onlineCnt) < 2){
                            GobangView.listenFlag = Integer.parseInt(init);
                            GobangView.localNum = Integer.parseInt(onlineCnt);
                        } else {
                            GobangView.listenFlag = 11;
                            Toast.makeText(getApplicationContext(), "Already two players, you are watching now!", Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "localNum: " + GobangView.localNum);
                        Log.d(TAG, "GobangView.listenFlag: " + GobangView.listenFlag);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "From: " + "connected!");
                    Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    {
        try {
            socket = IO.socket("http://52.196.31.83:8000");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
       // requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 获取屏幕宽高
        Display display = getWindowManager().getDefaultDisplay();
        // 现实GobangView
        GobangView.init(this, display.getWidth(), display.getHeight());
        gobangView = GobangView.getInstance();
        setContentView(gobangView);

        try {
            JSONObject json = new JSONObject();
            json.put("text", "Socket from client!");
            socket.emit("text", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        getInit();
        if (GobangView.localNum < 2)
            sendMessage(1);
//        setContentView(R.layout.activity_main);

        socket.on(Socket.EVENT_CONNECT, onConnectSuccess);
        socket.on("get_gobang", getGobangListener);
        socket.on("get_gobang_clear", getGobangClearListener);
        socket.on("get_gobang_state", getGobangStateListener);
        socket.on("updateComing", onTextUpdate);
        socket.connect();

        GobangView.setGameState(GobangView.GS_GAME);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnectSuccess);
        socket.off("get_gobang", getGobangListener);
        socket.off("get_gobang_clear", getGobangClearListener);
        socket.off("get_gobang_state", getGobangStateListener);
        socket.off("update", onTextUpdate);
        sendMessage(-1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }


//    public void getInit(){
//        String stringUrl = BASE_URL + "/get_init";
//
//        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//        GetMessageTask getMessageTask = new GetMessageTask();
//        if (networkInfo != null && networkInfo.isConnected()){
//            getMessageTask.execute(stringUrl);
//        } else {
//            Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
//        }
//    }

//    private class GetMessageTask extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... urls){
//            try {
//                return downloadUrl(urls[0]);
//            } catch (IOException e) {
//                return "Unable to retrieve web page. URL may be invalid.";
//            }
//        }
//
//        protected void onPostExecute(String result){
//            try {
//                JSONObject json = new JSONObject(result);
//                String init = json.getString("init");
//                String onlineCnt = json.getString("onlineCnt");
////                Log.d(TAG, "OnlineCnt: " + onlineCnt);
//                if (Integer.parseInt(onlineCnt) < 2){
//                    GobangView.listenFlag = Integer.parseInt(init);
////                    GobangView.localNum = Integer.parseInt(onlineCnt);
//
//                } else {
//                    GobangView.listenFlag = 11;
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
////            text.setText(currentPage + " : " + totalPage);
//        }
//
//        private String downloadUrl(String myurl) throws IOException{
//            InputStream is = null;
//            try {
//                URL url = new URL(myurl);
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setReadTimeout(10000);
//                conn.setConnectTimeout(15000);
//                conn.setRequestMethod("GET");
//                conn.setDoInput(true);
//
//                conn.connect();
//                int responseCode = conn.getResponseCode();
//                String results = "";
//                is = conn.getInputStream();
//                if (responseCode == HttpURLConnection.HTTP_OK){
//                    String line;
//                    BufferedReader br = new BufferedReader( new InputStreamReader(is));
//                    while ((line = br.readLine()) != null) {
//                        results += line;
//                    }
//                }
//                return results;
//            }finally {
//                if (is != null){
//                    is.close();
//                }
//            }
//        }
//    }

    public void sendMessage(int cnt){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("cnt", String.valueOf(cnt));

//        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        PostMessageTask postMessageTask = new PostMessageTask(paramsMap);
        postMessageTask.execute(BASE_URL + "/send_onlinecnt");


//        if (networkInfo != null && networkInfo.isConnected()){
//            postMessageTask.execute(BASE_URL + "/send_gobang");
//        }
//        else {
//            Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
//        }
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
                String status = json.getString("status");
                if (status.equals("ERROR")){
//                    Toast.makeText(getApplicationContext(), "Cannot post the message!", Toast.LENGTH_SHORT).show();
                }
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
}
