package cn.nbmly.bookkeep.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        pieChart = findViewById(R.id.pie_chart);
        barChart = findViewById(R.id.bar_chart);
        spinnerStatisticsType = findViewById(R.id.spinner_statistics_type);

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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Initial display
        displayCategoryStatistics();
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

        // 设置柱状图组的宽度和间距
        float groupSpace = 0.06f; // 组间距
        float barSpace = 0.02f; // 每组内柱状条间距
        float barWidth = 0.45f; // 每条柱状条的宽度
        // (barWidth + barSpace) * 2 + groupSpace = 1.00 -> 0.45*2 + 0.02*2 + 0.06 =
        // 0.90 + 0.04 + 0.06 = 1.00
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        billDao.close();
    }
}
