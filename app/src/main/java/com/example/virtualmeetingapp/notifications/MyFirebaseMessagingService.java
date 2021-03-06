package com.example.virtualmeetingapp.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.virtualmeetingapp.activites.ChatActivity;
import com.example.virtualmeetingapp.models.User;
import com.example.virtualmeetingapp.utils.Global;
import com.example.virtualmeetingapp.utils.TextUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //get current user from shared preferences
        SharedPreferences sp = getSharedPreferences("PREFS", MODE_PRIVATE);
        String savedCurrentUser = sp.getString(TextUtil.PREF_LATEST_USER_ID, "");

        /*Now there are two types of notifications
                      > notificationType="PostNotification"
                      > notificationType="ChatNotification"*/
        String notificationType = remoteMessage.getData().get("notificationType");
        assert notificationType != null;
        if (notificationType.equals("PostNotification")) {
            //post notification
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescription = remoteMessage.getData().get("pDescription");

            //if user is same that has posted don't show notification
//            assert sender != null;
//            if (!sender.equals(savedCurrentUser)) {
//                showPostNotification("" + pId, "" + pTitle, "" + pDescription);
//            }
        } else if ("ChatNotification".equals(notificationType)) {
            //chat notification
            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");
            User currentUser = (User) Global.getCurrentUser();
//            if (currentUser != null && sent.equals(currentUser.getId())) {
            if (currentUser != null && sent.equals(currentUser.getUserEmail())) {
                if (!savedCurrentUser.equals(user)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sendOAndAboveNotification(remoteMessage);
                    } else {
                        sendNormalNotification(remoteMessage);
                    }
                }
            }
        }
    }

//    private void showPostNotification(String pId, String pTitle, String pDescription) {
//        MNotificationManager notificationManager = (MNotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        int notificationID = new Random().nextInt(3000);
//
//
//        /*Apps targeting SDK 26 or above (Android O and above) must implement notification channels
//         and add its notifications to at least one of them
//         Let's add check if version is Oreo or higher then setup notification channel*/
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            setupPostNotificationChannel(notificationManager);
//        }
//
//        //show post detail activity using post id when notification clicked
//        Intent intent = new Intent(this, PostDetailFragment.class);
//        intent.putExtra("postid", pId);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        //LargeIcon
//        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
//
//        //sound for notification
//        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
//                .setSmallIcon(R.drawable.placeholder)
//                .setLargeIcon(largeIcon)
//                .setContentTitle(pTitle)
//                .setContentText(pDescription)
//                .setSound(notificationSoundUri)
//                .setContentIntent(pendingIntent);
//
//        //show notification
//        assert notificationManager != null;
//        notificationManager.notify(notificationID, notificationBuilder.build());
//
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPostNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "New Notification";
        String channelDescription = "Device to device post notification";

        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = "" + remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        assert icon != null;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i > 0) {
            j = i;
        }
        assert notificationManager != null;
        notificationManager.notify(j, builder.build());
    }

    private void sendOAndAboveNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        assert user != null;
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotifications(title, body, pIntent, defSoundUri, Integer.parseInt(icon));

        int j = 0;
        if (i > 0) {
            j = i;
        }
        notification1.getManager().notify(j, builder.build());

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        updateToken(s);
    }

    private void updateToken(String tokenRefresh) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        assert user != null;
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.COLLECTION_USER).child(user.getUid());
//        Map<String, Object> map = new HashMap<>();
//        map.put("token", tokenRefresh);
//        ref.updateChildren(map);
    }
}
