package hk.edu.cuhk.ie.iems5722.a2_1155066083;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import android.os.Handler;
import android.os.Message;

import hk.edu.cuhk.ie.iems5722.a2_1155066083.GobangActivity;

/**
 * Created by tanshen on 2016/4/23.
 */
public class GobangView extends SurfaceView implements Params,
        SurfaceHolder.Callback, Runnable {

    public static final String BASE_URL = "http://52.196.31.83/iems5722";
//    private Socket socket;
//    {
//        try {
//            socket = IO.socket("http://52.196.31.83");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    private static final String TAG = "ClientSocketIO";
    private static final String TAG2 = "Parameters";
    public static Paint sPaint = null;
    public static Canvas sCanvas = null;
    public static Resources sResources = null;
    public static int[][] mGameMap = null;
    public static int mCampTurn = 0;
    public static int mGameState = 0;
    public static int mCampWinner = 0;
    public static int mMapIndexX = 0;
    public static int mMapIndexY = 0;
    public static android.os.Handler UIHandler = new android.os.Handler(Looper.getMainLooper());
    public static int mMapHeightLengh = CHESS_HEIGHT;
    public static int mMapWidthLengh = CHESS_WIDTH;
    public static int[] flag = new int[1];
    public static int listenFlag;
    public static ArrayList<Integer> addToFlag = new ArrayList<>();
    static GobangView sInstance = null;
    // 控制循环
    boolean mbLoop = false;
    // 定义SurfaceHolder对象
    SurfaceHolder mSurfaceHolder = null;
    Bitmap bitmapBg = null;
    Bitmap bitmapWin = null;
    Bitmap mBlack = null;
    Bitmap mWhite = null;
    Context mContext = null;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private float mTitleSpaceX = 0;
    private float mTitleSpaceY = 0;
    private int mTitleHeight = 0;
    private float mTitleIndex_x = 0;
    private float mTitleIndex_y = 0;
    public static int localNum = 0;
    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "From: " + "connected!");
                }
            });
        }
    };

    public GobangView(Activity activity, int screenWidth, int screenHeight) {
        super(activity);

//        socket.on(Socket.EVENT_CONNECT, onConnectSuccess);
//        socket.on("get_gobang", getGobangListener);
//        socket.connect();

        sPaint = new Paint();
        sPaint.setAntiAlias(true);
        sResources = getResources();
        mContext = activity;
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        setFocusable(true);
        mbLoop = true;
        bitmapBg = CreatMatrixBitmap(R.drawable.status, mScreenWidth, mScreenHeight);
        bitmapWin = CreatMatrixBitmap(R.drawable.gameover, 2*mScreenWidth/3, mScreenHeight/4);
        mBlack = BitmapFactory.decodeResource(GobangView.sResources,
                R.drawable.ai);
        mWhite = BitmapFactory.decodeResource(GobangView.sResources,
                R.drawable.human);
        mTitleHeight = mScreenHeight / 3;
        mTitleSpaceX = (float) mScreenWidth / CHESS_WIDTH;
        mTitleSpaceY = (float) 2* mTitleHeight / CHESS_HEIGHT;
        mTitleIndex_x = (float) (mTitleSpaceX / 2);
        mTitleIndex_y = (float) (mTitleSpaceY / 2);

    }

    public static void init(Activity mActivity, int screenWidth,
                            int screenHeight) {
        sInstance = new GobangView(mActivity, screenWidth, screenHeight);
    }

    public static GobangView getInstance() {
        return sInstance;
    }

    public static void setGameState(int newState) {
        mGameState = newState;
        switch (mGameState) {
            case GS_GAME:
                if (GobangView.listenFlag != 11){
                    mGameMap = new int[CHESS_HEIGHT][CHESS_WIDTH];
                    mCampTurn = CAMP_HERO;
                    ItemClear item = new ItemClear(mCampWinner + "");
                    sendClear(item);
//                listenFlag = 0;
//                addToFlag.clear();
                    ItemClear itemState = new ItemClear(mGameState + "");
                    sendState(itemState);
                }
                break;
        }
    }

    public static boolean CheckPiecesMeet(int Camp) {
        int MeetCount = 0;
        // 横向
        for (int i = 0; i < CALU_ALL_COUNT; i++) {
            int index = mMapIndexX - CALU_SINGLE_COUNT + i;
            if (index < 0 || index >= mMapWidthLengh) {
                if (MeetCount == CALU_SINGLE_COUNT) {
                    return true;
                }
                MeetCount = 0;
                continue;
            }
            if (mGameMap[mMapIndexY][index] == Camp) {
                MeetCount++;
                if (MeetCount == CALU_SINGLE_COUNT) {
                    return true;
                }
            } else {
                MeetCount = 0;
            }
        }
        // 纵向
        MeetCount = 0;
        for (int i = 0; i < CALU_ALL_COUNT; i++) {
            int index = mMapIndexY - CALU_SINGLE_COUNT + i;
            if (index < 0 || index >= mMapHeightLengh) {
                if (MeetCount == CALU_SINGLE_COUNT) {
                    return true;
                }
                MeetCount = 0;
                continue;
            }
            if (mGameMap[index][mMapIndexX] == Camp) {
                MeetCount++;
                if (MeetCount == CALU_SINGLE_COUNT) {
                    return true;
                }
            } else {
                MeetCount = 0;
            }
        }

        // 右斜
        MeetCount = 0;
        for (int i = 0; i < CALU_ALL_COUNT; i++) {
            int indexX = mMapIndexX - CALU_SINGLE_COUNT + i;
            int indexY = mMapIndexY - CALU_SINGLE_COUNT + i;
            if ((indexX < 0 || indexX >= mMapWidthLengh)
                    || (indexY < 0 || indexY >= mMapHeightLengh)) {
                if (MeetCount == CALU_SINGLE_COUNT) {
                    return true;
                }
                MeetCount = 0;
                continue;
            }
            if (mGameMap[indexY][indexX] == Camp) {
                MeetCount++;
                if (MeetCount == CALU_SINGLE_COUNT) {
                    return true;
                }
            } else {
                MeetCount = 0;
            }
        }

        // 左斜
        MeetCount = 0;
        for (int i = 0; i < CALU_ALL_COUNT; i++) {
            int indexX = mMapIndexX - CALU_SINGLE_COUNT + i;
            int indexY = mMapIndexY + CALU_SINGLE_COUNT - i;
            if ((indexX < 0 || indexX >= mMapWidthLengh)
                    || (indexY < 0 || indexY >= mMapHeightLengh)) {
                if (MeetCount == CALU_SINGLE_COUNT) {
                    return true;
                }
                MeetCount = 0;
                continue;
            }
            if (mGameMap[indexY][indexX] == Camp) {
                MeetCount++;
                if (MeetCount == CALU_SINGLE_COUNT) {
                    return true;
                }
            } else {
                MeetCount = 0;
            }
        }
        return false;
    }

    public static void sendClear(ItemClear item){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("winner", item.winner);
        PostMessageTask postClearTask = new PostMessageTask(paramsMap);
        postClearTask.execute(BASE_URL + "/send_clear");

    }

    public static void sendWinner(ItemClear item) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("winner", item.winner);
        PostMessageTask postClearTask = new PostMessageTask(paramsMap);
        postClearTask.execute(BASE_URL + "/send_winner");
    }

    public static void sendState(ItemClear item){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("state", item.winner);
        PostMessageTask postClearTask = new PostMessageTask(paramsMap);
        postClearTask.execute(BASE_URL + "/send_state");
    }

    protected void Draw() {
        sCanvas = mSurfaceHolder.lockCanvas();
        if (mSurfaceHolder == null || sCanvas == null) {
            return;
        }
        RenderGame();
        mSurfaceHolder.unlockCanvasAndPost(sCanvas);
    }

    private void RenderGame() {
        switch (mGameState) {
            case GS_GAME:
                DrawRect(Color.WHITE, 0, 0, mScreenWidth, mScreenHeight);
                RenderMap();
                break;
            case GS_END:
                RenderMap();
                DrawImage(bitmapWin, mScreenWidth / 6, mScreenHeight / 3, 0);
                Log.d(TAG,"winner"+ mCampWinner);

                break;
        }

    }

    private void RenderMap() {
        int i, j;
        DrawImage(bitmapBg, 0, 0, 0);

        for (i = 0; i < mMapHeightLengh; i++) {
            for (j = 0; j < mMapWidthLengh; j++) {
                int CampID = mGameMap[i][j];
                float x = (j * mTitleSpaceX) + mTitleIndex_x;
                float y = (i * mTitleSpaceY) + mTitleHeight + mTitleIndex_y;
                if (CampID == CAMP_HERO) {
                    DrawImage(mBlack, x, y, ALIGN_VCENTER | ALIGN_HCENTER);
                } else if (CampID == CAMP_ENEMY) {
                    DrawImage(mWhite, x, y, ALIGN_VCENTER | ALIGN_HCENTER);
                }
            }
        }

    }

    private void DrawRect(int color, int x, int y, int width, int height) {
        sPaint.setColor(color);
        sCanvas.clipRect(x, y, width, height);
        sCanvas.drawRect(x, y, width, height, sPaint);
    }

    private void DrawString(int color, String str, int x, int y) {
        sPaint.setColor(color);
        sCanvas.drawText(str, x, y, sPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                UpdateTouchEvent(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void UpdateTouchEvent(int x, int y) {
        switch (mGameState) {
            case GS_GAME:
                if (x > 0 && y > mTitleHeight) {
                    mMapIndexX = (int) (x / mTitleSpaceX);
                    mMapIndexY = (int) ((y - mTitleHeight) / mTitleSpaceY);

                    if (mMapIndexX > mMapWidthLengh) {
                        mMapIndexX = mMapWidthLengh;
                    }
                    if (mMapIndexX < 0) {
                        mMapIndexX = 0;
                    }

                    if (mMapIndexY > mMapHeightLengh) {
                        mMapIndexY = mMapHeightLengh;
                    }

                    if (mMapIndexY < 0) {
                        mMapIndexY = 0;
                    }

                    if (mGameMap[mMapIndexY][mMapIndexX] == CAMP_DEFAULT) {
                        ItemSend item = new ItemSend(mMapIndexX+"", mMapIndexY+"", mCampTurn+"");
                        addToFlag.add(mCampTurn-1);
                        flag[0] = addToFlag.get(0);
                        Log.d(TAG2, "listenFlag: " + listenFlag);
                        Log.d(TAG2, "GobangView.flag[0]: " + GobangView.flag[0]);
                        if (listenFlag == GobangView.flag[0]){
                            sendMessage(item, flag[0]);
                        }
                    }
                }
                break;
            case GS_END:
                setGameState(GS_GAME);
                break;
        }

    }

    public boolean isCheckInvite(String body) {
        if (body.indexOf("invite") >= 0) {
            if (mGameState != GS_INVITING && mGameState != GS_COMFIRE
                    && mGameState != GS_GAME) {
                return true;
            }
        }
        return false;
    }

    private Bitmap CreatMatrixBitmap(int resourcesID, float scr_width,
                                     float res_height) {
        Bitmap bitMap = null;
        bitMap = BitmapFactory.decodeResource(sResources, resourcesID);
        int bitWidth = bitMap.getWidth();
        int bitHeight = bitMap.getHeight();
        float scaleWidth = scr_width / (float) bitWidth;
        float scaleHeight = res_height / (float) bitHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        bitMap = Bitmap.createBitmap(bitMap, 0, 0, bitWidth, bitHeight, matrix,
                true);
        return bitMap;
    }

    private void DrawString(int color, String text, int x, int y, int anchor) {
        Rect rect = new Rect();
        sPaint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        int tx = 0;
        int ty = 0;
        if ((anchor & ALIGN_RIGHT) != 0) {
            tx = x - w;
        } else if ((anchor & ALIGN_HCENTER) != 0) {
            tx = x - (w >> 1);
        } else {
            tx = x;
        }
        if ((anchor & ALIGN_TOP) != 0) {
            ty = y + h;
        } else if ((anchor & ALIGN_VCENTER) != 0) {
            ty = y + (h >> 1);
        } else {
            ty = y;
        }
        sPaint.setColor(color);
        sCanvas.drawText(text, tx, ty, sPaint);
    }

    private void DrawImage(Bitmap bitmap, float x, float y, int anchor) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float tx = 0;
        float ty = 0;
        if ((anchor & ALIGN_RIGHT) != 0) {
            tx = x - w;
        } else if ((anchor & ALIGN_HCENTER) != 0) {
            tx = x - (w >> 1);
        } else {
            tx = x;
        }
        if ((anchor & ALIGN_TOP) != 0) {
            ty = y + h;
        } else if ((anchor & ALIGN_VCENTER) != 0) {
            ty = y - (h >> 1);
        } else if ((anchor & ALIGN_BOTTOM) != 0) {
            ty = y - h;
        } else {
            ty = y;
        }

        sCanvas.drawBitmap(bitmap, tx, ty, sPaint);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        mbLoop = false;
    }

    @Override
    public void run() {
        while (mbLoop) {
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
            synchronized (mSurfaceHolder) {
                Draw();
            }
        }
    }

    public void sendMessage(ItemSend item, int flag){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("x", item.x);
        paramsMap.put("y", item.y);
        paramsMap.put("campTurn", item.campTurn);
        paramsMap.put("flag", String.valueOf(flag));

//        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        PostMessageTask postMessageTask = new PostMessageTask(paramsMap);
        postMessageTask.execute(BASE_URL + "/send_gobang");


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
