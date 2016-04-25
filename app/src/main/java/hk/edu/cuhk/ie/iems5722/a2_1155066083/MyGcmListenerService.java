package hk.edu.cuhk.ie.iems5722.a2_1155066083;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import hk.edu.cuhk.ie.iems5722.a2_1155066083.GobangView;

/**
 * Created by tanshen on 2016/4/5.
 */

public class MyGcmListenerService extends GcmListenerService {

    public static android.os.Handler UIHandler = new android.os.Handler(Looper.getMainLooper());

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String id = data.getString("id");
        String var = data.getString("var");
        Log.d(TAG, "From: " + id);
        Log.d(TAG, "Message: " + var);
//        int tmp = Integer.parseInt(id);
//        final int x = (tmp-1) %  9;
//        final int y = (tmp-1) / 9;

//        UIHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (GobangView.mCampTurn == GobangView.CAMP_HERO) {
//                    GobangView.mGameMap[y][x] = GobangView.CAMP_HERO;
//                    if (GobangView.CheckPiecesMeet(GobangView.CAMP_HERO)){
//                        GobangView.mCampWinner = R.string.Role_black;
//                        GobangView.setGameState(GobangView.GS_END);
//                    }else {
//                        GobangView.mCampTurn = GobangView.CAMP_ENEMY;
//                    }
//                }
//                else{
//                    GobangView.mGameMap[y][x] = GobangView.CAMP_ENEMY;
//                    if (GobangView.CheckPiecesMeet(GobangView.CAMP_ENEMY)){
//                        GobangView.mCampWinner = R.string.Role_white;
//                        GobangView.setGameState(GobangView.GS_END);
//                    }else {
//                        GobangView.mCampTurn = GobangView.CAMP_HERO;
//                    }
//                }
//
//            }
//        });

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
//        sendNotification(id,var);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message,String title) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
