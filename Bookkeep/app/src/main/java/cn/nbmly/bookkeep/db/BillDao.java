package cn.nbmly.bookkeep.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cn.nbmly.bookkeep.models.Bill;

public class BillDao {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public BillDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addBill(Bill bill) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BILL_USER_ID, bill.getUserId());
        values.put(DatabaseHelper.COLUMN_AMOUNT, bill.getAmount());
        values.put(DatabaseHelper.COLUMN_TYPE, bill.getType());
        values.put(DatabaseHelper.COLUMN_CATEGORY, bill.getCategory());
        values.put(DatabaseHelper.COLUMN_DATE, bill.getDate());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, bill.getDescription());
        return database.insert(DatabaseHelper.TABLE_BILLS, null, values);
    }

    public int updateBill(Bill bill) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BILL_USER_ID, bill.getUserId());
        values.put(DatabaseHelper.COLUMN_AMOUNT, bill.getAmount());
        values.put(DatabaseHelper.COLUMN_TYPE, bill.getType());
        values.put(DatabaseHelper.COLUMN_CATEGORY, bill.getCategory());
        values.put(DatabaseHelper.COLUMN_DATE, bill.getDate());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, bill.getDescription());
        return database.update(DatabaseHelper.TABLE_BILLS, values,
                DatabaseHelper.COLUMN_BILL_ID + " = ?",
                new String[]{String.valueOf(bill.getId())});
    }

    public void deleteBill(int billId) {
        database.delete(DatabaseHelper.TABLE_BILLS,
                DatabaseHelper.COLUMN_BILL_ID + " = ?",
                new String[]{String.valueOf(billId)});
    }

    public List<Bill> getAllBillsByUserId(int userId) {
        List<Bill> bills = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_BILLS,
                null,
                DatabaseHelper.COLUMN_BILL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, DatabaseHelper.COLUMN_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Bill bill = new Bill(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BILL_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BILL_USER_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION))
                );
                bills.add(bill);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return bills;
    }

    public Bill getBillById(int billId) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_BILLS,
                null,
                DatabaseHelper.COLUMN_BILL_ID + " = ?",
                new String[]{String.valueOf(billId)},
                null, null, null
        );

        Bill bill = null;
        if (cursor != null && cursor.moveToFirst()) {
            bill = new Bill(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BILL_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BILL_USER_ID)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION))
            );
            cursor.close();
        }
        return bill;
    }
}

