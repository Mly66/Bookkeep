package cn.nbmly.bookkeep.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.db.UserDao;
import cn.nbmly.bookkeep.models.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsernameReg;
    private EditText etPasswordReg;
    private EditText etConfirmPasswordReg;
    private Button btnRegisterConfirm;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsernameReg = findViewById(R.id.et_username_reg);
        etPasswordReg = findViewById(R.id.et_password_reg);
        etConfirmPasswordReg = findViewById(R.id.et_confirm_password_reg);
        btnRegisterConfirm = findViewById(R.id.btn_register_confirm);

        userDao = new UserDao(this);
        userDao.open();

        btnRegisterConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String username = etUsernameReg.getText().toString().trim();
        String password = etPasswordReg.getText().toString().trim();
        String confirmPassword = etConfirmPasswordReg.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "所有字段都不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userDao.getUserByUsername(username) != null) {
            Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(username, password);
        long result = userDao.addUser(newUser);

        if (result != -1) {
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "注册失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userDao.close();
    }
}

