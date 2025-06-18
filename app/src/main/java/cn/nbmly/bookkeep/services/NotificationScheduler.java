package cn.nbmly.bookkeep.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;
import java.util.List;

import cn.nbmly.bookkeep.db.BillDao;
import cn.nbmly.bookkeep.models.Bill;

public class NotificationScheduler {

    public static final String ACTION_DAILY_REMINDER = "cn.nbmly.bookkeep.DAILY_REMINDER";
    public static final String ACTION_WEEKLY_REPORT = "cn.nbmly.bookkeep.WEEKLY_REPORT";
    public static final String ACTION_BUDGET_CHECK = "cn.nbmly.bookkeep.BUDGET_CHECK";

    private static final int REQUEST_CODE_DAILY = 1001;
    private static final int REQUEST_CODE_WEEKLY = 1002;
    private static final int REQUEST_CODE_BUDGET = 1003;

    private Context context;
    private AlarmManager alarmManager;
    private BillDao billDao;

    public NotificationScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.billDao = new BillDao(context);
        this.billDao.open();
    }

    private PendingIntent createPendingIntent(Context context, Intent intent, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(context, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getBroadcast(context, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    public void scheduleDailyReminder(int hour, int minute) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_DAILY_REMINDER);

        PendingIntent pendingIntent = createPendingIntent(context, intent, REQUEST_CODE_DAILY);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // 如果时间已经过了，设置为明天
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent);
        }

        // 保存设置
        SharedPreferences prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("daily_reminder_enabled", true)
                .putInt("daily_reminder_hour", hour)
                .putInt("daily_reminder_minute", minute)
                .apply();
    }

    public void scheduleWeeklyReport(int dayOfWeek, int hour, int minute) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_WEEKLY_REPORT);

        PendingIntent pendingIntent = createPendingIntent(context, intent, REQUEST_CODE_WEEKLY);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // 如果时间已经过了，设置为下周
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent);
        }

        // 保存设置
        SharedPreferences prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("weekly_report_enabled", true)
                .putInt("weekly_report_day", dayOfWeek)
                .putInt("weekly_report_hour", hour)
                .putInt("weekly_report_minute", minute)
                .apply();
    }

    public void checkBudgetAndNotify(int userId, double monthlyBudget) {
        if (monthlyBudget <= 0)
            return;

        // 获取本月支出
        Calendar startOfMonth = Calendar.getInstance();
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);

        Calendar endOfMonth = Calendar.getInstance();
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonth.set(Calendar.MINUTE, 59);
        endOfMonth.set(Calendar.SECOND, 59);

        List<Bill> monthlyBills = billDao.getBillsByDateRange(userId, startOfMonth.getTime(), endOfMonth.getTime());

        double totalExpense = 0;
        for (Bill bill : monthlyBills) {
            if (bill.getType() == 0) { // 支出类型
                totalExpense += bill.getAmount();
            }
        }

        // 检查预算
        double remainingBudget = monthlyBudget - totalExpense;
        double percentageUsed = (totalExpense / monthlyBudget) * 100;

        if (percentageUsed >= 90) {
            String message = String.format("本月预算已使用 %.1f%%，剩余 %.2f 元", percentageUsed, remainingBudget);
            NotificationService.showBudgetNotification(context, message);
        } else if (remainingBudget <= 0) {
            String message = String.format("本月预算已超支 %.2f 元", Math.abs(remainingBudget));
            NotificationService.showBudgetNotification(context, message);
        }
    }

    public void cancelDailyReminder() {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_DAILY_REMINDER);

        PendingIntent pendingIntent = createPendingIntent(context, intent, REQUEST_CODE_DAILY);

        alarmManager.cancel(pendingIntent);

        SharedPreferences prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("daily_reminder_enabled", false).apply();
    }

    public void cancelWeeklyReport() {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_WEEKLY_REPORT);

        PendingIntent pendingIntent = createPendingIntent(context, intent, REQUEST_CODE_WEEKLY);

        alarmManager.cancel(pendingIntent);

        SharedPreferences prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("weekly_report_enabled", false).apply();
    }

    public void close() {
        if (billDao != null) {
            billDao.close();
        }
    }
}