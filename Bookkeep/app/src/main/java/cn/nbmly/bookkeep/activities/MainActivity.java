package cn.nbmly.bookkeep.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.adapters.BillAdapter;
import cn.nbmly.bookkeep.db.BillDao;
import cn.nbmly.bookkeep.models.Bill;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_BILL_REQUEST_CODE = 1;
    private static final int EDIT_BILL_REQUEST_CODE = 2;

    private TextView tvWelcome;
    private Button btnLogout;
    private Button btnAddBill;
    private Button btnStatistics;
    private RecyclerView rvBills;
    private BillAdapter billAdapter;
    private List<Bill> billList;
    private BillDao billDao;
    private int loggedInUserId;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvWelcome = findViewById(R.id.tv_welcome);
        btnLogout = findViewById(R.id.btn_logout);
        btnAddBill = findViewById(R.id.btn_add_bill);
        btnStatistics = findViewById(R.id.btn_statistics);
        rvBills = findViewById(R.id.rv_bills);

        // Check login status
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            // If not logged in, redirect to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        loggedInUserId = sharedPref.getInt("loggedInUserId", -1);
        loggedInUsername = sharedPref.getString("loggedInUsername", "");
        tvWelcome.setText("欢迎, " + loggedInUsername + "!");

        billDao = new BillDao(this);
        billDao.open();

        billList = new ArrayList<>();
        billAdapter = new BillAdapter(billList);
        rvBills.setLayoutManager(new LinearLayoutManager(this));
        rvBills.setAdapter(billAdapter);

        loadBills();

        btnAddBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BillDetailActivity.class);
                startActivityForResult(intent, ADD_BILL_REQUEST_CODE);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        billAdapter.setOnItemClickListener(new BillAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Bill bill) {
                Intent intent = new Intent(MainActivity.this, BillDetailActivity.class);
                intent.putExtra("bill_id", bill.getId());
                startActivityForResult(intent, EDIT_BILL_REQUEST_CODE);
            }
        });

        billAdapter.setOnItemLongClickListener(new BillAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Bill bill) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("删除账单")
                        .setMessage("确定要删除这笔账单吗？")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                billDao.deleteBill(bill.getId());
                                Toast.makeText(MainActivity.this, "账单已删除", Toast.LENGTH_SHORT).show();
                                loadBills(); // Refresh list
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        btnStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadBills() {
        billList.clear();
        billList.addAll(billDao.getAllBillsByUserId(loggedInUserId));
        billAdapter.notifyDataSetChanged();
    }

    private void logoutUser() {
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear(); // Clear all user session data
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_BILL_REQUEST_CODE || requestCode == EDIT_BILL_REQUEST_CODE) {
                loadBills(); // Refresh bills after add/edit
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        billDao.close();
    }
}

