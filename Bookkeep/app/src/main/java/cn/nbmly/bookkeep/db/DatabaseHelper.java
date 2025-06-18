package cn.nbmly.bookkeep.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookkeep.db";
    private static final int DATABASE_VERSION = 1;

    // User table constants
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Bill table constants
    public static final String TABLE_BILLS = "bills";
    public static final String COLUMN_BILL_ID = "id";
    public static final String COLUMN_BILL_USER_ID = "user_id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DESCRIPTION = "description";

    // SQL statement to create users table
    private static final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_USERNAME + " TEXT NOT NULL UNIQUE," +
            COLUMN_PASSWORD + " TEXT NOT NULL);";

    // SQL statement to create bills table
    private static final String SQL_CREATE_BILLS_TABLE = "CREATE TABLE " + TABLE_BILLS + " (" +
            COLUMN_BILL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_BILL_USER_ID + " INTEGER NOT NULL," +
            COLUMN_AMOUNT + " REAL NOT NULL," +
            COLUMN_TYPE + " TEXT NOT NULL," +
            COLUMN_CATEGORY + " TEXT NOT NULL," +
            COLUMN_DATE + " TEXT NOT NULL," +
            COLUMN_DESCRIPTION + " TEXT," +
            "FOREIGN KEY(" + COLUMN_BILL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_BILLS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // Create tables again
        onCreate(db);
    }
}

