package cn.nbmly.bookkeep.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.activities.LoginActivity;
import cn.nbmly.bookkeep.db.UserDao;
import cn.nbmly.bookkeep.models.User;

public class ProfileFragment extends Fragment {
    private TextInputEditText etUsername;
    private TextInputEditText etEmail;
    private TextInputEditText etOldPassword;
    private TextInputEditText etPassword;
    private Button btnSave;
    private UserDao userDao;
    private int loggedInUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图
        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        etOldPassword = view.findViewById(R.id.et_old_password);
        etPassword = view.findViewById(R.id.et_password);
        btnSave = view.findViewById(R.id.btn_save);

        // 初始化数据库
        userDao = new UserDao(requireContext());
        userDao.open();

        // 获取当前登录用户ID
        SharedPreferences sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        loggedInUserId = sharedPref.getInt("loggedInUserId", -1);

        // 加载用户数据
        loadUserData();

        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        if (loggedInUserId != -1) {
            User user = userDao.getUserById(loggedInUserId);
            if (user != null) {
                etUsername.setText(user.getUsername());
                etEmail.setText(user.getEmail());
                // etPassword.setText(user.getPassword()); // 不在加载时显示密码
            }
        }
    }

    private void saveUserData() {
        String email = etEmail.getText().toString().trim();
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            Toast.makeText(requireContext(), "新密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = userDao.getUserById(loggedInUserId);
        if (user != null) {
            // 验证旧密码
            if (!user.getPassword().equals(oldPassword)) {
                Toast.makeText(requireContext(), "旧密码不正确", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setEmail(email);
            user.setPassword(newPassword);

            int result = userDao.updateUser(user);
            if (result > 0) {
                Toast.makeText(requireContext(), "密码修改成功，请重新登录", Toast.LENGTH_SHORT).show();
                // 清除登录状态并跳转到登录页面
                SharedPreferences sharedPref = requireContext().getSharedPreferences("user_prefs",
                        Context.MODE_PRIVATE);
                sharedPref.edit().remove("isLoggedIn").remove("loggedInUserId").remove("loggedInUsername").apply();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                Toast.makeText(requireContext(), "保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (userDao != null) {
            userDao.close();
        }
    }
}