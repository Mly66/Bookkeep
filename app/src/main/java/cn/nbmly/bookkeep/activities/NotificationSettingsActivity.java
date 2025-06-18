package cn.nbmly.bookkeep.activities;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.services.NotificationScheduler;

public class NotificationSettingsActivity extends AppCompatActivity {

    private Switch switchDailyReminder;
    private Switch switchWeeklyReport;
    private Switch switchBudgetAlert;
    private TextView tvDailyTime;
    private TextView tvWeeklyTime;
    private EditText etMonthlyBudget;
    private Button btnSaveSettings;
    private Button btnTestNotification;

    private NotificationScheduler notificationScheduler;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("通知设置");
        }

        // 初始化视图
        initViews();

        // 初始化通知管理器
        notificationScheduler = new NotificationScheduler(this);

        // 加载设置
        loadSettings();

        // 设置事件监听器
        setupListeners();
    }

    private void initViews() {
        switchDailyReminder = findViewById(R.id.switch_daily_reminder);
        switchWeeklyReport = findViewById(R.id.switch_weekly_report);
        switchBudgetAlert = findViewById(R.id.switch_budget_alert);
        tvDailyTime = findViewById(R.id.tv_daily_time);
        tvWeeklyTime = findViewById(R.id.tv_weekly_time);
        etMonthlyBudget = findViewById(R.id.et_monthly_budget);
        btnSaveSettings = findViewById(R.id.btn_save_settings);
        btnTestNotification = findViewById(R.id.btn_test_notification);
    }

    private void loadSettings() {
        prefs = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        SharedPreferences budgetPrefs = getSharedPreferences("budget_prefs", Context.MODE_PRIVATE);

        // 加载每日提醒设置
        boolean dailyEnabled = prefs.getBoolean("daily_reminder_enabled", false);
        switchDailyReminder.setChecked(dailyEnabled);

        int dailyHour = prefs.getInt("daily_reminder_hour", 20);
        int dailyMinute = prefs.getInt("daily_reminder_minute", 0);
        tvDailyTime.setText(String.format("%02d:%02d", dailyHour, dailyMinute));

        // 加载每周报告设置
        boolean weeklyEnabled = prefs.getBoolean("weekly_report_enabled", false);
        switchWeeklyReport.setChecked(weeklyEnabled);

        int weeklyDay = prefs.getInt("weekly_report_day", Calendar.SUNDAY);
        int weeklyHour = prefs.getInt("weekly_report_hour", 9);
        int weeklyMinute = prefs.getInt("weekly_report_minute", 0);
        tvWeeklyTime.setText(String.format("%s %02d:%02d", getDayName(weeklyDay), weeklyHour, weeklyMinute));

        // 加载预算设置
        boolean budgetEnabled = prefs.getBoolean("budget_alert_enabled", false);
        switchBudgetAlert.setChecked(budgetEnabled);

        float monthlyBudget = budgetPrefs.getFloat("monthly_budget", 0);
        if (monthlyBudget > 0) {
            etMonthlyBudget.setText(String.valueOf(monthlyBudget));
        }
    }

    private void setupListeners() {
        // 每日提醒时间选择
        tvDailyTime.setOnClickListener(v -> showTimePickerDialog(true));

        // 每周报告时间选择
        tvWeeklyTime.setOnClickListener(v -> showTimePickerDialog(false));

        // 保存设置
        btnSaveSettings.setOnClickListener(v -> saveSettings());

        // 测试通知
        btnTestNotification.setOnClickListener(v -> testNotification());
    }

    private void showTimePickerDialog(boolean isDaily) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> {
                    if (isDaily) {
                        tvDailyTime.setText(String.format("%02d:%02d", hourOfDay, minute1));
                    } else {
                        // 对于每周报告，还需要选择星期几
                        showDayPickerDialog(hourOfDay, minute1);
                    }
                },
                hour,
                minute,
                true);
        timePickerDialog.show();
    }

    private void showDayPickerDialog(int hour, int minute) {
        String[] days = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("选择星期")
                .setSingleChoiceItems(days, currentDay, (dialog, which) -> {
                    tvWeeklyTime.setText(String.format("%s %02d:%02d", days[which], hour, minute));
                    dialog.dismiss();
                })
                .show();
    }

    private void saveSettings() {
        // 保存每日提醒设置
        boolean dailyEnabled = switchDailyReminder.isChecked();
        String dailyTime = tvDailyTime.getText().toString();
        String[] dailyTimeParts = dailyTime.split(":");
        int dailyHour = Integer.parseInt(dailyTimeParts[0]);
        int dailyMinute = Integer.parseInt(dailyTimeParts[1]);

        if (dailyEnabled) {
            notificationScheduler.scheduleDailyReminder(dailyHour, dailyMinute);
        } else {
            notificationScheduler.cancelDailyReminder();
        }

        // 保存每周报告设置
        boolean weeklyEnabled = switchWeeklyReport.isChecked();
        String weeklyTime = tvWeeklyTime.getText().toString();
        String[] weeklyTimeParts = weeklyTime.split(" ");
        String dayName = weeklyTimeParts[0];
        String[] timeParts = weeklyTimeParts[1].split(":");
        int weeklyHour = Integer.parseInt(timeParts[0]);
        int weeklyMinute = Integer.parseInt(timeParts[1]);
        int weeklyDay = getDayOfWeek(dayName);

        if (weeklyEnabled) {
            notificationScheduler.scheduleWeeklyReport(weeklyDay, weeklyHour, weeklyMinute);
        } else {
            notificationScheduler.cancelWeeklyReport();
        }

        // 保存预算设置
        boolean budgetEnabled = switchBudgetAlert.isChecked();
        String budgetText = etMonthlyBudget.getText().toString();
        float monthlyBudget = 0;

        if (!budgetText.isEmpty()) {
            try {
                monthlyBudget = Float.parseFloat(budgetText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效的预算金额", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        SharedPreferences budgetPrefs = getSharedPreferences("budget_prefs", Context.MODE_PRIVATE);
        budgetPrefs.edit()
                .putFloat("monthly_budget", monthlyBudget)
                .apply();

        prefs.edit()
                .putBoolean("budget_alert_enabled", budgetEnabled)
                .apply();

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
    }

    private void testNotification() {
        // 发送测试通知
        cn.nbmly.bookkeep.services.NotificationService.showReminderNotification(this);
        Toast.makeText(this, "测试通知已发送", Toast.LENGTH_SHORT).show();
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "周日";
            case Calendar.MONDAY:
                return "周一";
            case Calendar.TUESDAY:
                return "周二";
            case Calendar.WEDNESDAY:
                return "周三";
            case Calendar.THURSDAY:
                return "周四";
            case Calendar.FRIDAY:
                return "周五";
            case Calendar.SATURDAY:
                return "周六";
            default:
                return "周日";
        }
    }

    private int getDayOfWeek(String dayName) {
        switch (dayName) {
            case "周日":
                return Calendar.SUNDAY;
            case "周一":
                return Calendar.MONDAY;
            case "周二":
                return Calendar.TUESDAY;
            case "周三":
                return Calendar.WEDNESDAY;
            case "周四":
                return Calendar.THURSDAY;
            case "周五":
                return Calendar.FRIDAY;
            case "周六":
                return Calendar.SATURDAY;
            default:
                return Calendar.SUNDAY;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationScheduler != null) {
            notificationScheduler.close();
        }
    }
}