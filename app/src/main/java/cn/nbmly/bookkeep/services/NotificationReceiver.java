package cn.nbmly.bookkeep.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;
import java.util.List;

import cn.nbmly.bookkeep.db.BillDao;
import cn.nbmly.bookkeep.models.Bill;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null)
            return;

        switch (action) {
            case NotificationScheduler.ACTION_DAILY_REMINDER:
                handleDailyReminder(context);
                break;
            case NotificationScheduler.ACTION_WEEKLY_REPORT:
                handleWeeklyReport(context);
                break;
            case NotificationScheduler.ACTION_BUDGET_CHECK:
                handleBudgetCheck(context);
                break;
        }
    }

    private void handleDailyReminder(Context context) {
        // 检查是否启用了每日提醒
        SharedPreferences prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("daily_reminder_enabled", false);

        if (enabled) {
            NotificationService.showReminderNotification(context);

            // 重新设置明天的提醒
            int hour = prefs.getInt("daily_reminder_hour", 20);
            int minute = prefs.getInt("daily_reminder_minute", 0);

            NotificationScheduler notificationScheduler = new NotificationScheduler(context);
            notificationScheduler.scheduleDailyReminder(hour, minute);
            notificationScheduler.close();
        }
    }

    private void handleWeeklyReport(Context context) {
        // 检查是否启用了每周报告
        SharedPreferences prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("weekly_report_enabled", false);

        if (enabled) {
            generateWeeklyReport(context);

            // 重新设置下周的报告
            int dayOfWeek = prefs.getInt("weekly_report_day", Calendar.SUNDAY);
            int hour = prefs.getInt("weekly_report_hour", 9);
            int minute = prefs.getInt("weekly_report_minute", 0);

            NotificationScheduler notificationScheduler = new NotificationScheduler(context);
            notificationScheduler.scheduleWeeklyReport(dayOfWeek, hour, minute);
            notificationScheduler.close();
        }
    }

    private void handleBudgetCheck(Context context) {
        // 获取用户ID和预算设置
        SharedPreferences userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = userPrefs.getInt("loggedInUserId", -1);

        SharedPreferences budgetPrefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE);
        double monthlyBudget = budgetPrefs.getFloat("monthly_budget", 0);

        if (userId != -1 && monthlyBudget > 0) {
            NotificationScheduler notificationScheduler = new NotificationScheduler(context);
            notificationScheduler.checkBudgetAndNotify(userId, monthlyBudget);
            notificationScheduler.close();
        }
    }

    private void generateWeeklyReport(Context context) {
        // 获取用户ID
        SharedPreferences userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = userPrefs.getInt("loggedInUserId", -1);

        if (userId == -1)
            return;

        // 获取本周的账单数据
        Calendar startOfWeek = Calendar.getInstance();
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0);
        startOfWeek.set(Calendar.MINUTE, 0);
        startOfWeek.set(Calendar.SECOND, 0);

        Calendar endOfWeek = Calendar.getInstance();
        endOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek() + 6);
        endOfWeek.set(Calendar.HOUR_OF_DAY, 23);
        endOfWeek.set(Calendar.MINUTE, 59);
        endOfWeek.set(Calendar.SECOND, 59);

        BillDao billDao = new BillDao(context);
        billDao.open();

        List<Bill> weeklyBills = billDao.getBillsByDateRange(userId, startOfWeek.getTime(), endOfWeek.getTime());

        double totalExpense = 0;
        double totalIncome = 0;
        int expenseCount = 0;
        int incomeCount = 0;

        for (Bill bill : weeklyBills) {
            if (bill.getType() == 0) { // 支出类型
                totalExpense += bill.getAmount();
                expenseCount++;
            } else if (bill.getType() == 1) { // 收入类型
                totalIncome += bill.getAmount();
                incomeCount++;
            }
        }

        billDao.close();

        // 生成报告消息
        StringBuilder message = new StringBuilder();
        message.append("本周支出：").append(String.format("%.2f", totalExpense)).append("元");
        message.append("，收入：").append(String.format("%.2f", totalIncome)).append("元");
        message.append("，净收入：").append(String.format("%.2f", totalIncome - totalExpense)).append("元");

        if (expenseCount > 0) {
            message.append("，平均每次支出：").append(String.format("%.2f", totalExpense / expenseCount)).append("元");
        }

        NotificationService.showReportNotification(context, message.toString());
    }
}