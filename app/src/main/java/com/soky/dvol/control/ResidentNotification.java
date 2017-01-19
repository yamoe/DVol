package com.soky.dvol.control;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.soky.dvol.R;

/**
 * 항상 상주하는 알림(Notification) 관리
 */
public class ResidentNotification {
    public final String TAG = this.getClass().getSimpleName();

    private static final int NOTIFY_ID = 111;

    private static final String INTENT_ACTION_EXIT_BUTTON = "com.soky.dvl.exit.btn";
    private static final String INTENT_ACTION_START_BUTTON = "com.soky.dvl.start.btn";

    private static boolean sInitialized = false;    // 중복 start 되는 경우 BroadcastReceiver가 중복 등록됨을 방지
    private final static boolean USE_BIG_CONTENT = false;  // 임의로 막아둠

    private Class<?> mClass;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mBigContentView;
    private RemoteViews mSmallContentView;

    public ResidentNotification(Class<?> cls) {
        mClass = cls;
    }

    // 상주 알림창 생성
    public void start() {
        if (sInitialized) return;
        sInitialized = true;

        Context context = Controller.getApplication().getApplicationContext();

        mBuilder = new NotificationCompat.Builder(context);

        // Lollipop ( Android 5.0 ) 부터는 Notification 의 Icon 의 Color 값을 전부 무시하며, Non-Alpha Channel 은 모두 White 로 바꾸어 버린다.
        // 즉, Alpha 가 있는 부분은 transparent 로, Alpha 가 없는 부분은 모두 흰색으로 표시한다. (  Material Design 의 영향 )
        mBuilder.setSmallIcon(R.drawable.dvol_icon_noti);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.dvol_icon_noti));
        mBuilder.setTicker("Notify");            // 알림이 발생될 때 잠시 보이는 글씨
        mBuilder.setContentText("Title");        // 알림창에서의 제목
        mBuilder.setContentText("Contents");    // 알림창에서의 글씨
        mBuilder.setAutoCancel(false);           // 클릭하면 자동으로 알림 삭제
        mBuilder.setOngoing(true);               // 클릭해도 알림이 사라지지 않도록 유지

        // 알림 클릭시 실행될 activity 설정
        Intent intent = new Intent(context, mClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pending);

        /*
        Notification의 RemoteViews 에서 사용할 수 있는 widget은 제한적임. 아래 URL의 widget만 사용가능.
        https://developer.android.com/guide/topics/appwidgets/index.html#CreatingLayout
         */

        // 기본 알림 크기 (높이 64dp)
        mSmallContentView = new RemoteViews(context.getPackageName(), R.layout.resident_notification_small);
        mBuilder.setCustomContentView(mSmallContentView);

        // 종료 버튼
        mSmallContentView.setOnClickPendingIntent(
                R.id.noti_small_exit_button,
                PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_EXIT_BUTTON), PendingIntent.FLAG_UPDATE_CURRENT)
        );

        // 시작,중지 버튼
        mSmallContentView.setOnClickPendingIntent(
                R.id.noti_small_start_button,
                PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_START_BUTTON), PendingIntent.FLAG_UPDATE_CURRENT)
        );

        // 확장 알림 크기 (높이 256dp)
        if (Build.VERSION.SDK_INT >= 16 && USE_BIG_CONTENT) {
            mBigContentView = new RemoteViews(context.getPackageName(), R.layout.resident_notification_big);
            mBuilder.setCustomBigContentView(mBigContentView);

            // 종료 버튼
            mBigContentView.setOnClickPendingIntent(
                    R.id.noti_big_exit_button,
                    PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_EXIT_BUTTON), PendingIntent.FLAG_UPDATE_CURRENT)
            );

            // 시작,중지 버튼
            mBigContentView.setOnClickPendingIntent(
                    R.id.noti_big_start_button,
                    PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_START_BUTTON), PendingIntent.FLAG_UPDATE_CURRENT)
            );

        }

        BroadcastReceiver button_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                if (action.equals(INTENT_ACTION_EXIT_BUTTON)) {
                    Controller.exit();
                } else if (action.equals(INTENT_ACTION_START_BUTTON)) {
                    Controller.switchAutoVolume();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_START_BUTTON);
        intentFilter.addAction(INTENT_ACTION_EXIT_BUTTON);
        context.registerReceiver(button_receiver, intentFilter);

        // 알림 실행
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFY_ID, mBuilder.build());
    }

    // 상주 알림  종료
    public void cancel() {
        sInitialized = false;

        Context context = Controller.getApplication().getApplicationContext();
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFY_ID);
    }

    public void changeStarted() {
        if (!sInitialized) return;

        mSmallContentView.setImageViewResource(R.id.noti_small_start_button, R.drawable.img_stop);
        if (Build.VERSION.SDK_INT >= 16 && USE_BIG_CONTENT) {
            mBigContentView.setImageViewResource(R.id.noti_big_start_button, R.drawable.img_stop);
        }

        updateNotification();
    }

    public void changeStopped() {
        if (!sInitialized) return;

        mSmallContentView.setImageViewResource(R.id.noti_small_start_button, R.drawable.img_start);
        if (Build.VERSION.SDK_INT >= 16 && USE_BIG_CONTENT) {
            mBigContentView.setImageViewResource(R.id.noti_big_start_button, R.drawable.img_start);
        }

        updateNotification();
    }

    private void updateNotification() {
        Context context = Controller.getApplication().getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFY_ID, mBuilder.build());
    }

}
