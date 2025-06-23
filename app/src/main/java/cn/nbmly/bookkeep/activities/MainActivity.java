package cn.nbmly.bookkeep.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.adapters.BillAdapter;
import cn.nbmly.bookkeep.db.BillDao;
import cn.nbmly.bookkeep.db.UserDao;
import cn.nbmly.bookkeep.fragments.AboutFragment;
import cn.nbmly.bookkeep.fragments.ProfileFragment;
import cn.nbmly.bookkeep.models.Bill;
import cn.nbmly.bookkeep.models.User;
import cn.nbmly.bookkeep.services.NotificationService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private RecyclerView recyclerView;
    private BillAdapter billAdapter;
    private BillDao billDao;
    private UserDao userDao;
    private int loggedInUserId;
    private ActivityResultLauncher<Intent> addBillLauncher;
    private ActivityResultLauncher<Intent> editBillLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 创建通知频道
        NotificationService.createNotificationChannels(this);

        // 初始化 ActivityResultLauncher
        addBillLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshBillList();
                    }
                });

        editBillLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshBillList();
                    }
                });

        // 检查用户是否已登录
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        loggedInUserId = sharedPref.getInt("loggedInUserId", -1);
        if (loggedInUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 初始化数据库
        billDao = new BillDao(this);
        billDao.open();
        userDao = new UserDao(this);
        userDao.open();

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置导航抽屉
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // 更新导航抽屉头部信息
        updateNavHeader();

        // 初始化视图
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化适配器
        List<Bill> billList = billDao.getAllBillsByUserId(loggedInUserId);
        billAdapter = new BillAdapter(this, billList);
        recyclerView.setAdapter(billAdapter);

        // 设置点击事件
        billAdapter.setOnItemClickListener(bill -> {
            Intent intent = new Intent(MainActivity.this, BillDetailActivity.class);
            intent.putExtra("bill_id", bill.getId());
            intent.putExtra("bill_amount", bill.getAmount());
            intent.putExtra("bill_type", bill.getType());
            intent.putExtra("bill_category", bill.getCategory());
            intent.putExtra("bill_note", bill.getNote());
            intent.putExtra("bill_create_time", bill.getCreateTime().getTime());
            editBillLauncher.launch(intent);
        });

        // 设置长按事件
        billAdapter.setOnItemLongClickListener(bill -> {
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("删除账单")
                    .setMessage("确定要删除这条账单记录吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        billDao.deleteBill(bill.getId());
                        refreshBillList();
                        Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        // 设置添加按钮
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddBillActivity.class);
            addBillLauncher.launch(intent);
        });

        // 处理返回按钮
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
    }

    private void updateNavHeader() {
        if (userDao == null)
            return;

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.nav_header_name);
        TextView tvEmail = headerView.findViewById(R.id.nav_header_email);

        User user = userDao.getUserById(loggedInUserId);
        if (user != null) {
            tvName.setText(user.getUsername());
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        }
    }

    private void refreshBillList() {
        if (billDao != null) {
            List<Bill> updatedList = billDao.getAllBillsByUserId(loggedInUserId);
            billAdapter.setBillList(updatedList);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment selectedFragment = null;

        androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (id == R.id.nav_home) {
            if (currentFragment != null) {
                fragmentManager.beginTransaction().remove(currentFragment).commit();
            }
            recyclerView.setVisibility(View.VISIBLE);
            refreshBillList();
        } else if (id == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else if (id == R.id.nav_statistics) {
            startActivity(new Intent(this, StatisticsActivity.class));
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_notifications) {
            startActivity(new Intent(this, NotificationSettingsActivity.class));
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_about) {
            selectedFragment = new AboutFragment();
        }

        if (selectedFragment != null) {
            recyclerView.setVisibility(View.GONE);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            // 清除登录状态
            SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            sharedPref.edit().remove("loggedInUserId").apply();

            // 跳转到登录页面
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billDao != null) {
            billDao.close();
        }
        if (userDao != null) {
            userDao.close();
        }
    }
}
