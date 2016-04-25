package hk.edu.cuhk.ie.iems5722.a2_1155066083;

import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by tanshen on 2016/4/23.
 */
public class GobangActivity extends AppCompatActivity {
    private static final String TAG = "ClientSocketIO";
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
                        int tmp = Integer.parseInt(id);
                        final int x = (tmp-1) %  9;
                        final int y = (tmp-1) / 9;

                        if (GobangView.mCampTurn == GobangView.CAMP_HERO) {
                            GobangView.mGameMap[y][x] = GobangView.CAMP_HERO;
                            if (GobangView.CheckPiecesMeet(GobangView.CAMP_HERO)){//HERO win
                                GobangView.mCampWinner = R.string.Role_black;
//                                GobangView.setGameState(GobangView.GS_END);
                                GobangView.mGameState = GobangView.GS_END;
                                GobangView.mCampWinner = GobangView.CAMP_HERO;
                            }else {
                                GobangView.mCampTurn = GobangView.CAMP_ENEMY;
                            }
                        }
                        else{
                            GobangView.mGameMap[y][x] = GobangView.CAMP_ENEMY;
                            if (GobangView.CheckPiecesMeet(GobangView.CAMP_ENEMY)){
                                GobangView.mCampWinner = R.string.Role_white;
//                                GobangView.setGameState(GobangView.GS_END);
                                GobangView.mGameState = GobangView.GS_END;
                                GobangView.mCampWinner = GobangView.CAMP_ENEMY;
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "From: " + text);
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
        try {
            JSONObject json = new JSONObject();
            json.put("text", "Socket from client!");
            socket.emit("text", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        gobangView = GobangView.getInstance();
        setContentView(gobangView);
//        setContentView(R.layout.activity_main);

        socket.on(Socket.EVENT_CONNECT, onConnectSuccess);
        socket.on("get_gobang", getGobangListener);
        socket.on("get_gobang_clear", getGobangClearListener);
        socket.on("update", onTextUpdate);
        socket.connect();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
