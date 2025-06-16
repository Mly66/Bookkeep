package cn.nbmly.bookkeep.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import cn.nbmly.bookkeep.models.User;

public class UserDao {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addUser(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
        long currentTime = new Date().getTime();
        values.put(DatabaseHelper.COLUMN_CREATE_TIME, currentTime);
        values.put(DatabaseHelper.COLUMN_UPDATE_TIME, currentTime);
        return database.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    public int updateUser(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_UPDATE_TIME, new Date().getTime());

        return database.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(user.getId()) });
    }

    public User getUserById(int userId) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[] {
                        DatabaseHelper.COLUMN_ID,
                        DatabaseHelper.COLUMN_USERNAME,
                        DatabaseHelper.COLUMN_PASSWORD,
                        DatabaseHelper.COLUMN_EMAIL,
                        DatabaseHelper.COLUMN_CREATE_TIME,
                        DatabaseHelper.COLUMN_UPDATE_TIME
                },
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(userId) },
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
            user.setCreateTime(
                    new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME))));
            user.setUpdateTime(
                    new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATE_TIME))));
            cursor.close();
        }
        return user;
    }

    public User getUserByUsername(String username) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[] {
                        DatabaseHelper.COLUMN_ID,
                        DatabaseHelper.COLUMN_USERNAME,
                        DatabaseHelper.COLUMN_PASSWORD,
                        DatabaseHelper.COLUMN_EMAIL,
                        DatabaseHelper.COLUMN_CREATE_TIME,
                        DatabaseHelper.COLUMN_UPDATE_TIME
                },
                DatabaseHelper.COLUMN_USERNAME + " = ?",
                new String[] { username },
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
            user.setCreateTime(
                    new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME))));
            user.setUpdateTime(
                    new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATE_TIME))));
            cursor.close();
        }
        return user;
    }

    public boolean checkUser(String username, String password) {
        User user = getUserByUsername(username);
        return user != null && user.getPassword().equals(password);
    }

    public int getUserId(String username) {
        User user = getUserByUsername(username);
        return user != null ? user.getId() : -1;
    }
}
