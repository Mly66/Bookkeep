package cn.nbmly.bookkeep.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.db.BillDao;
import cn.nbmly.bookkeep.models.Bill;

public class BillDetailActivity extends AppCompatActivity {

    private EditText etAmount;
    private Spinner spinnerType;
    private Spinner spinnerCategory;
    private EditText etNote;
    private TextView tvTime;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private BillDao billDao;
    private Bill currentBill;
    private SimpleDateFormat dateFormat;
    private long billId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        
        // 初始化视图
        initViews();
        
        // 初始化数据库
        billDao = new BillDao(this);
        billDao.open();

        // 设置下拉框
        setupSpinners();

        // 获取传递的数据
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            billId = extras.getLong("bill_id", -1);
            if (billId != -1) {
                loadBill(billId);
            } else {
                Toast.makeText(this, "无法获取账单数据", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "无法获取账单数据", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        etAmount = findViewById(R.id.et_amount);
        spinnerType = findViewById(R.id.spinner_type);
        spinnerCategory = findViewById(R.id.spinner_category);
        etNote = findViewById(R.id.et_note);
        tvTime = findViewById(R.id.tv_time);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        btnSave.setOnClickListener(v -> saveBill());
        btnCancel.setOnClickListener(v -> finish());
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

    private void loadBill(long billId) {
        currentBill = billDao.getBillById(billId);
        if (currentBill != null) {
            etAmount.setText(String.valueOf(currentBill.getAmount()));
            spinnerType.setSelection(currentBill.getType());
            updateCategorySpinner(currentBill.getType());
            spinnerCategory.setSelection(currentBill.getCategory());
            etNote.setText(currentBill.getNote());
            tvTime.setText(String.format("创建时间: %s", dateFormat.format(currentBill.getCreateTime())));
        }
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

            if (currentBill != null) {
                // 更新现有账单
                currentBill.setAmount(amount);
                currentBill.setType(type);
                currentBill.setCategory(category);
                currentBill.setNote(note);
                currentBill.setUpdateTime(new Date());

                int result = billDao.updateBill(currentBill);
                if (result > 0) {
                    Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
                }
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