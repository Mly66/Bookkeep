package cn.nbmly.bookkeep.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Date;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.db.BillDao;
import cn.nbmly.bookkeep.models.Bill;

public class AddBillActivity extends AppCompatActivity {

    private EditText etAmount;
    private Spinner spinnerType;
    private Spinner spinnerCategory;
    private EditText etNote;
    private MaterialButton btnSave;
    private BillDao billDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        // 初始化视图
        initViews();
        
        // 初始化数据库
        billDao = new BillDao(this);
        billDao.open();

        // 设置下拉框
        setupSpinners();
    }

    private void initViews() {
        etAmount = findViewById(R.id.et_amount);
        spinnerType = findViewById(R.id.spinner_type);
        spinnerCategory = findViewById(R.id.spinner_category);
        etNote = findViewById(R.id.et_note);
        btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> saveBill());
    }

    private void setupSpinners() {
        // 设置类型下拉框
        String[] types = getResources().getStringArray(R.array.bill_types);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // 设置初始分类下拉框
        updateCategorySpinner(0);

        // 监听类型选择变化
        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updateCategorySpinner(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void updateCategorySpinner(int typePosition) {
        // 根据类型更新分类选项
        int categoriesArrayId;
        if (typePosition == 0) { // 支出
            categoriesArrayId = R.array.expense_categories;
        } else { // 收入
            categoriesArrayId = R.array.income_categories;
        }

        String[] categories = getResources().getStringArray(categoriesArrayId);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void saveBill() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            int type = spinnerType.getSelectedItemPosition();
            int category = spinnerCategory.getSelectedItemPosition();
            String note = etNote.getText().toString().trim();

            // 获取当前登录用户ID
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            int userId = prefs.getInt("loggedInUserId", -1);
            if (userId == -1) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                // 跳转到登录界面
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            // 创建新账单
            Bill newBill = new Bill();
            newBill.setUserId((int)userId);
            newBill.setAmount(amount);
            newBill.setType(type);
            newBill.setCategory(category);
            newBill.setNote(note);
            newBill.setCreateTime(new Date());
            newBill.setUpdateTime(new Date());

            long result = billDao.insertBill(newBill);
            if (result != -1) {
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的金额", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billDao != null) {
            billDao.close();
            billDao = null;
        }
    }
} 