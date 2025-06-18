package cn.nbmly.bookkeep.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.nbmly.bookkeep.models.Bill;

public class BillDao {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] allColumns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_USER_ID,
            DatabaseHelper.COLUMN_AMOUNT,
            DatabaseHelper.COLUMN_TYPE,
            DatabaseHelper.COLUMN_CATEGORY,
            DatabaseHelper.COLUMN_NOTE,
            DatabaseHelper.COLUMN_CREATE_TIME,
            DatabaseHelper.COLUMN_UPDATE_TIME
    };

    public BillDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertBill(Bill bill) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, bill.getUserId());
        values.put(DatabaseHelper.COLUMN_AMOUNT, bill.getAmount());
        values.put(DatabaseHelper.COLUMN_TYPE, bill.getType());
        values.put(DatabaseHelper.COLUMN_CATEGORY, bill.getCategory());
        values.put(DatabaseHelper.COLUMN_NOTE, bill.getNote());
        values.put(DatabaseHelper.COLUMN_CREATE_TIME, bill.getCreateTime().getTime());
        values.put(DatabaseHelper.COLUMN_UPDATE_TIME, bill.getUpdateTime().getTime());

        return database.insert(DatabaseHelper.TABLE_BILLS, null, values);
    }

    public int updateBill(Bill bill) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AMOUNT, bill.getAmount());
        values.put(DatabaseHelper.COLUMN_TYPE, bill.getType());
        values.put(DatabaseHelper.COLUMN_CATEGORY, bill.getCategory());
        values.put(DatabaseHelper.COLUMN_NOTE, bill.getNote());
        values.put(DatabaseHelper.COLUMN_UPDATE_TIME, new Date().getTime());

        return database.update(DatabaseHelper.TABLE_BILLS, values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(bill.getId()) });
    }

    public void deleteBill(long billId) {
        database.delete(DatabaseHelper.TABLE_BILLS,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(billId) });
    }

    public Bill getBillById(long billId) {
        Cursor cursor = database.query(DatabaseHelper.TABLE_BILLS,
                allColumns,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(billId) },
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Bill bill = cursorToBill(cursor);
            cursor.close();
            return bill;
        }
        return null;
    }

    public List<Bill> getAllBillsByUserId(int userId) {
        List<Bill> bills = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_BILLS,
                allColumns,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[] { String.valueOf(userId) },
                null, null,
                DatabaseHelper.COLUMN_CREATE_TIME + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                bills.add(cursorToBill(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return bills;
    }

    public List<Bill> getBillsByDateRange(int userId, Date startDate, Date endDate) {
        List<Bill> bills = new ArrayList<>();
        String selection = DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_CREATE_TIME + " >= ? AND " +
                DatabaseHelper.COLUMN_CREATE_TIME + " <= ?";
        String[] selectionArgs = {
                String.valueOf(userId),
                String.valueOf(startDate.getTime()),
                String.valueOf(endDate.getTime())
        };

        Cursor cursor = database.query(DatabaseHelper.TABLE_BILLS,
                allColumns,
                selection,
                selectionArgs,
                null, null,
                DatabaseHelper.COLUMN_CREATE_TIME + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                bills.add(cursorToBill(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return bills;
    }

    private Bill cursorToBill(Cursor cursor) {
        Bill bill = new Bill();
        bill.setId(cursor.getLong(0));
        bill.setUserId(cursor.getInt(1));
        bill.setAmount(cursor.getDouble(2));
        bill.setType(cursor.getInt(3));
        bill.setCategory(cursor.getInt(4));
        bill.setNote(cursor.getString(5));
        bill.setCreateTime(new Date(cursor.getLong(6)));
        bill.setUpdateTime(new Date(cursor.getLong(7)));
        return bill;
    }
}
