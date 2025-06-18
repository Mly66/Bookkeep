package cn.nbmly.bookkeep.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.activities.MainActivity;

public class NotificationService extends Service {

        public static final String CHANNEL_ID_REMINDER = "bookkeep_reminder";
        public static final String CHANNEL_ID_BUDGET = "bookkeep_budget";
        public static final String CHANNEL_ID_REPORT = "bookkeep_report";

        public static final int NOTIFICATION_ID_REMINDER = 1001;
        public static final int NOTIFICATION_ID_BUDGET = 1002;
        public static final int NOTIFICATION_ID_REPORT = 1003;

        @Override
        public void onCreate() {
                super.onCreate();
                createNotificationChannels();
        }

        @Override
        public IBinder onBind(Intent intent) {
                return null;
        }

        private void createNotificationChannels() {
                createNotificationChannels(this);
        }

        public static void createNotificationChannels(Context context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

                        // 记账提醒频道
                        NotificationChannel reminderChannel = new NotificationChannel(
                                        CHANNEL_ID_REMINDER,
                                        "记账提醒",
                                        NotificationManager.IMPORTANCE_DEFAULT);
                        reminderChannel.setDescription("提醒您记录日常支出");

                        // 预算提醒频道
                        NotificationChannel budgetChannel = new NotificationChannel(
                                        CHANNEL_ID_BUDGET,
                                        "预算提醒",
                                        NotificationManager.IMPORTANCE_HIGH);
                        budgetChannel.setDescription("预算超支提醒");

                        // 统计报告频道
                        NotificationChannel reportChannel = new NotificationChannel(
                                        CHANNEL_ID_REPORT,
                                        "统计报告",
                                        NotificationManager.IMPORTANCE_LOW);
                        reportChannel.setDescription("定期支出统计报告");

                        notificationManager.createNotificationChannel(reminderChannel);
                        notificationManager.createNotificationChannel(budgetChannel);
                        notificationManager.createNotificationChannel(reportChannel);
                }
        }

        private static PendingIntent createPendingIntent(Context context, Intent intent) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        return PendingIntent.getActivity(context, 0, intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                } else {
                        return PendingIntent.getActivity(context, 0, intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);
                }
        }

        public static void showReminderNotification(Context context) {
                // 确保通知频道已创建
                createNotificationChannels(context);

                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent pendingIntent = createPendingIntent(context, intent);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle("记账提醒")
                                .setContentText("记得记录今天的支出哦！")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID_REMINDER, builder.build());
        }

        public static void showBudgetNotification(Context context, String message) {
                // 确保通知频道已创建
                createNotificationChannels(context);

                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent pendingIntent = createPendingIntent(context, intent);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_BUDGET)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle("预算提醒")
                                .setContentText(message)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID_BUDGET, builder.build());
        }

        public static void showReportNotification(Context context, String message) {
                // 确保通知频道已创建
                createNotificationChannels(context);

                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent pendingIntent = createPendingIntent(context, intent);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REPORT)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle("支出统计")
                                .setContentText(message)
                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID_REPORT, builder.build());
        }
}