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

/**
 * Created by tanshen on 2016/4/23.
 */
public class GobangView extends SurfaceView implements Params,
        SurfaceHolder.Callback, Runnable {

    public static final String BASE_URL = "http://52.196.31.83/iems5722";
    public static Paint sPaint = null;
    public static Canvas sCanvas = null;
    public static Resources sResources = null;
    static GobangView sInstance = null;
    public static int[][] mGameMap = null;
    public int[][] GameMapDB = null;
    public static int mCampTurn = 0;
    public int mCampWinner = 0;
    // 控制循环
    boolean mbLoop = false;
    // 定义SurfaceHolder对象
    SurfaceHolder mSurfaceHolder = null;
    Bitmap bitmapBg = null;
    Bitmap mBlack = null;
    Bitmap mWhite = null;
    Context mContext = null;
    private int mGameState = 0;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mMapHeightLengh = 0;
    private int mMapWidthLengh = 0;
    public static int mMapIndexX = 0;
    public static int mMapIndexY = 0;
    private float mTitleSpace = 0;
    private int mTitleHeight = 0;
    private float mTitleIndex_x = 0;
    private float mTitleIndex_y = 0;

    public GobangView(Activity activity, int screenWidth, int screenHeight) {
        super(activity);
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
        bitmapBg = CreatMatrixBitmap(R.drawable.status, mScreenWidth,
                mScreenHeight);
        mBlack = BitmapFactory.decodeResource(GobangView.sResources,
                R.drawable.ai);
        mWhite = BitmapFactory.decodeResource(GobangView.sResources,
                R.drawable.human);
        mTitleSpace = (float) mScreenWidth / CHESS_WIDTH;
        mTitleHeight = mScreenHeight / 3;
        mTitleIndex_x = (float) (mTitleSpace / 2);
        mTitleIndex_y = (float) (mTitleSpace / 2);
        setGameState(GS_GAME);
    }

    public static void init(Activity mActivity, int screenWidth,
                            int screenHeight) {
        sInstance = new GobangView(mActivity, screenWidth, screenHeight);
    }

    public static GobangView getInstance() {
        return sInstance;
    }

    public void setGameState(int newState) {
        mGameState = newState;
        switch (mGameState) {
            case GS_GAME:
                mGameMap = new int[CHESS_HEIGHT][CHESS_WIDTH];
                GameMapDB = new int[CHESS_HEIGHT][CHESS_WIDTH];
                mMapHeightLengh = mGameMap.length;
                mMapWidthLengh = mGameMap[0].length;
                mCampTurn = CAMP_HERO;
                break;
        }
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
                DrawRect(Color.RED, 0, 0, mScreenWidth, mScreenHeight);
                DrawString(Color.WHITE, sResources.getString(mCampWinner)
                        + "胜利 点击继续游戏", 50, 50);
                break;
        }

    }

    private void RenderMap() {
        int i, j;
        DrawImage(bitmapBg, 0, 0, 0);

        for (i = 0; i < mMapHeightLengh; i++) {
            for (j = 0; j < mMapWidthLengh; j++) {
                int CampID = mGameMap[i][j];
                float x = (j * mTitleSpace) + mTitleIndex_x;
                float y = (i * mTitleSpace) + mTitleHeight + mTitleIndex_y;
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

    public boolean CheckPiecesMeet(int Camp) {
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

    private void UpdateTouchEvent(int x, int y) {
        switch (mGameState) {
            case GS_GAME:

                if (x > 0 && y > mTitleHeight) {
                    mMapIndexX = (int) (x / mTitleSpace);
                    mMapIndexY = (int) ((y - mTitleHeight) / mTitleSpace);

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
                    ItemSend item = new ItemSend(mMapIndexX+"", mMapIndexY+"", mCampTurn+"");
                    sendMessage(item);
//                    if (mGameMap[mMapIndexY][mMapIndexX] == CAMP_DEFAULT) {
//
//                        if (mCampTurn == CAMP_HERO) {
//                            mGameMap[mMapIndexY][mMapIndexX] = CAMP_HERO;
//                            if (CheckPiecesMeet(CAMP_HERO)) {
//                                mCampWinner = R.string.Role_black;
//                                setGameState(GS_END);
//                            } else {
//                                mCampTurn = CAMP_ENEMY;
//                            }
//
//                        } else {
//                            mGameMap[mMapIndexY][mMapIndexX] = CAMP_ENEMY;
//                            if (CheckPiecesMeet(CAMP_ENEMY)) {
//                                mCampWinner = R.string.Role_white;
//                                setGameState(GS_END);
//                            } else {
//                                mCampTurn = CAMP_HERO;
//                            }
//                        }
//                    }
//                    getMessage();
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

    public void sendMessage(ItemSend item){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("x", item.x);
        paramsMap.put("y", item.y);
        paramsMap.put("campTurn", item.campTure);

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

    public void getMessage() {
        // Gets the URL from the UI's text field.
        //String stringUrl = urlText.getText().toString();
        String stringUrl = (BASE_URL + "/get_gobang");
        new GetMsgTask().execute(stringUrl);
    }

    private class PostMessageTask extends AsyncTask<String, Void, String> {
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
                json = new JSONObject(result);;
                JSONArray messages = json.getJSONArray("data");
                String status = json.getString("status");
                if (status.equals("OK")) {
                    Log.e("chat", result);

                    for (int i = 0; i < messages.length(); i++) {
                        String id = messages.getJSONObject(i).getString("id");
                        String name = messages.getJSONObject(i).getString("var");
                        int tmp = Integer.parseInt(id);
                        int var = Integer.parseInt(name);
                        int x,y;
                        x = (tmp-1) %  9;
                        y = (tmp-1) / 9;
                        mGameMap[y][x]=var;
                    }
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
}
