package cn.nbmly.bookkeep.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.db.BillDao;
import cn.nbmly.bookkeep.models.Bill;

public class StatisticsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private Spinner spinnerStatisticsType;
    private BillDao billDao;
    private int loggedInUserId;
    private TextView tvCardMonth, tvCardWeek, tvCardYear;
    private TextView tvNetIncome, tvMaxExpense, tvMaxIncome, tvAvgExpense, tvAvgIncome, tvTopExpenseCategory,
            tvTopIncomeCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        pieChart = findViewById(R.id.pie_chart);
        barChart = findViewById(R.id.bar_chart);
        spinnerStatisticsType = findViewById(R.id.spinner_statistics_type);

        // 新增统计卡片和信息区View初始化
        tvCardMonth = findViewById(R.id.tv_card_month_value);
        tvCardWeek = findViewById(R.id.tv_card_week_value);
        tvCardYear = findViewById(R.id.tv_card_year_value);
        tvNetIncome = findViewById(R.id.tv_net_income);
        tvMaxExpense = findViewById(R.id.tv_max_expense);
        tvMaxIncome = findViewById(R.id.tv_max_income);
        tvAvgExpense = findViewById(R.id.tv_avg_expense);
        tvAvgIncome = findViewById(R.id.tv_avg_income);
        tvTopExpenseCategory = findViewById(R.id.tv_top_expense_category);
        tvTopIncomeCategory = findViewById(R.id.tv_top_income_category);

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        loggedInUserId = sharedPref.getInt("loggedInUserId", -1);

        billDao = new BillDao(this);
        billDao.open();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.statistics_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatisticsType.setAdapter(adapter);

        spinnerStatisticsType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // 按类别统计
                    displayCategoryStatistics();
                } else { // 按月份统计
                    displayMonthlyStatistics();
                }
                updateStatisticsInfo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Initial display
        displayCategoryStatistics();
        updateStatisticsInfo();
    }

    private void displayCategoryStatistics() {
        pieChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);

        List<Bill> bills = billDao.getAllBillsByUserId(loggedInUserId);
        float totalExpenses = 0f;
        float totalIncomes = 0f;

        for (Bill bill : bills) {
            if (bill.getType() == 0) { // 支出
                totalExpenses += (float) bill.getAmount();
            } else { // 收入
                totalIncomes += (float) bill.getAmount();
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        if (totalExpenses > 0) {
            entries.add(new PieEntry(totalExpenses, "总支出"));
        }
        if (totalIncomes > 0) {
            entries.add(new PieEntry(totalIncomes, "总收入"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "总收支概览");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void displayMonthlyStatistics() {
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);

        List<Bill> bills = billDao.getAllBillsByUserId(loggedInUserId);
        Map<String, Float> monthlyExpenses = new HashMap<>();
        Map<String, Float> monthlyIncomes = new HashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        for (Bill bill : bills) {
            String month = monthFormat.format(bill.getCreateTime());
            float amount = (float) bill.getAmount();
            if (bill.getType() == 0) { // 支出
                monthlyExpenses.put(month, monthlyExpenses.getOrDefault(month, 0f) + amount);
            } else { // 收入
                monthlyIncomes.put(month, monthlyIncomes.getOrDefault(month, 0f) + amount);
            }
        }

        List<String> months = new ArrayList<>(monthlyExpenses.keySet());
        for (String incomeMonth : monthlyIncomes.keySet()) {
            if (!months.contains(incomeMonth)) {
                months.add(incomeMonth);
            }
        }
        months.sort(String::compareTo);

        List<BarEntry> expenseEntries = new ArrayList<>();
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < months.size(); i++) {
            String month = months.get(i);
            float expense = monthlyExpenses.getOrDefault(month, 0f);
            float income = monthlyIncomes.getOrDefault(month, 0f);
            expenseEntries.add(new BarEntry(i, expense));
            incomeEntries.add(new BarEntry(i, income));
            labels.add(month);
        }

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "支出");
        expenseDataSet.setColor(Color.RED); // 支出颜色
        expenseDataSet.setValueTextSize(10f);

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "收入");
        incomeDataSet.setColor(Color.GREEN); // 收入颜色
        incomeDataSet.setValueTextSize(10f);

        BarData data = new BarData(expenseDataSet, incomeDataSet);

        float groupSpace = 0.06f;
        float barSpace = 0.02f;
        float barWidth = 0.45f;

        data.setBarWidth(barWidth);
        barChart.setData(data);
        barChart.groupBars(-0.5f, groupSpace, barSpace);

        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(labels.size());
        xAxis.setCenterAxisLabels(true); // 使标签居中于组

        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    private String getCategoryText(int type, int category) {
        String[] categories;
        if (type == 0) { // 支出
            categories = getResources().getStringArray(R.array.expense_categories);
        } else { // 收入
            categories = getResources().getStringArray(R.array.income_categories);
        }
        return category >= 0 && category < categories.length ? categories[category] : "未知";
    }

    /**
     * 统计并刷新顶部卡片和统计信息区
     */
    private void updateStatisticsInfo() {
        List<Bill> bills = billDao.getAllBillsByUserId(loggedInUserId);
        // 时间相关
        Calendar now = Calendar.getInstance();
        int curYear = now.get(Calendar.YEAR);
        int curMonth = now.get(Calendar.MONTH);
        int curWeek = now.get(Calendar.WEEK_OF_YEAR);
        float monthExpense = 0, monthIncome = 0;
        float weekExpense = 0, weekIncome = 0;
        float yearExpense = 0, yearIncome = 0;
        float totalExpense = 0, totalIncome = 0;
        float maxExpense = 0, maxIncome = 0;
        float sumExpense = 0, sumIncome = 0;
        int countExpense = 0, countIncome = 0;
        Map<Integer, Float> expenseCategoryMap = new HashMap<>();
        Map<Integer, Float> incomeCategoryMap = new HashMap<>();
        for (Bill bill : bills) {
            Calendar c = Calendar.getInstance();
            c.setTime(bill.getCreateTime());
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int week = c.get(Calendar.WEEK_OF_YEAR);
            float amount = (float) bill.getAmount();
            if (bill.getType() == 0) { // 支出
                totalExpense += amount;
                sumExpense += amount;
                countExpense++;
                if (amount > maxExpense)
                    maxExpense = amount;
                if (year == curYear)
                    yearExpense += amount;
                if (year == curYear && month == curMonth)
                    monthExpense += amount;
                if (year == curYear && week == curWeek)
                    weekExpense += amount;
                // 类别统计
                int cat = bill.getCategory();
                expenseCategoryMap.put(cat, expenseCategoryMap.getOrDefault(cat, 0f) + amount);
            } else { // 收入
                totalIncome += amount;
                sumIncome += amount;
                countIncome++;
                if (amount > maxIncome)
                    maxIncome = amount;
                if (year == curYear)
                    yearIncome += amount;
                if (year == curYear && month == curMonth)
                    monthIncome += amount;
                if (year == curYear && week == curWeek)
                    weekIncome += amount;
                // 类别统计
                int cat = bill.getCategory();
                incomeCategoryMap.put(cat, incomeCategoryMap.getOrDefault(cat, 0f) + amount);
            }
        }
        // 顶部卡片
        tvCardMonth.setText(String.format("%.2f/%.2f", monthIncome, monthExpense));
        tvCardWeek.setText(String.format("%.2f/%.2f", weekIncome, weekExpense));
        tvCardYear.setText(String.format("%.2f/%.2f", yearIncome, yearExpense));
        // 统计信息区
        tvNetIncome.setText("净收入：" + String.format("%.2f", totalIncome - totalExpense));
        tvMaxExpense.setText("最大支出：" + (maxExpense > 0 ? String.format("%.2f", maxExpense) : "-"));
        tvMaxIncome.setText("最大收入：" + (maxIncome > 0 ? String.format("%.2f", maxIncome) : "-"));
        tvAvgExpense.setText("平均支出：" + (countExpense > 0 ? String.format("%.2f", sumExpense / countExpense) : "-"));
        tvAvgIncome.setText("平均收入：" + (countIncome > 0 ? String.format("%.2f", sumIncome / countIncome) : "-"));
        tvTopExpenseCategory.setText("支出Top3类别：" + getTopCategoriesString(expenseCategoryMap, 0));
        tvTopIncomeCategory.setText("收入Top3类别：" + getTopCategoriesString(incomeCategoryMap, 1));
    }

    /**
     * 获取Top3类别字符串
     */
    private String getTopCategoriesString(Map<Integer, Float> map, int type) {
        if (map.isEmpty())
            return "-";
        List<Map.Entry<Integer, Float>> list = new ArrayList<>(map.entrySet());
        list.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));
        String[] categories = type == 0 ? getResources().getStringArray(R.array.expense_categories)
                : getResources().getStringArray(R.array.income_categories);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(3, list.size()); i++) {
            int cat = list.get(i).getKey();
            float amt = list.get(i).getValue();
            sb.append(categories[cat]).append("(").append(String.format("%.2f", amt)).append(")");
            if (i < Math.min(3, list.size()) - 1)
                sb.append("，");
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        billDao.close();
    }
}
