package cn.nbmly.bookkeep.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookkeep.db";
    private static final int DATABASE_VERSION = 4;

    // User table constants
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_CREATE_TIME = "create_time";
    public static final String COLUMN_UPDATE_TIME = "update_time";

    // Bill table constants
    public static final String TABLE_BILLS = "bills";
    public static final String COLUMN_BILL_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_BILL_CREATE_TIME = "create_time";
    public static final String COLUMN_BILL_UPDATE_TIME = "update_time";

    // SQL statement to create users table
    private static final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_USERNAME + " TEXT NOT NULL UNIQUE," +
            COLUMN_PASSWORD + " TEXT NOT NULL," +
            COLUMN_EMAIL + " TEXT," +
            COLUMN_CREATE_TIME + " INTEGER NOT NULL," +
            COLUMN_UPDATE_TIME + " INTEGER NOT NULL" +
            ")";

    // SQL statement to create bills table
    private static final String SQL_CREATE_BILLS_TABLE = "CREATE TABLE " + TABLE_BILLS + " (" +
            COLUMN_BILL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_USER_ID + " INTEGER NOT NULL," +
            COLUMN_AMOUNT + " REAL NOT NULL," +
            COLUMN_TYPE + " INTEGER NOT NULL," +
            COLUMN_CATEGORY + " INTEGER NOT NULL," +
            COLUMN_NOTE + " TEXT," +
            COLUMN_BILL_CREATE_TIME + " INTEGER NOT NULL," +
            COLUMN_BILL_UPDATE_TIME + " INTEGER NOT NULL," +
            "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")" +
            ")";

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
        if (oldVersion < 2) {
            // Drop older tables if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
            // Create tables again
            db.execSQL(SQL_CREATE_BILLS_TABLE);
        }

        if (oldVersion < 3) {
            // 如果是从版本2升级到版本3，需要将description列重命名为note
            try {
                db.execSQL("ALTER TABLE " + TABLE_BILLS + " RENAME COLUMN description TO note");
            } catch (Exception e) {
                // 如果重命名失败（可能是因为列不存在），则删除表并重新创建
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
                db.execSQL(SQL_CREATE_BILLS_TABLE);
            }
        }

        if (oldVersion < 4) {
            // 添加email字段
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_EMAIL + " TEXT");
        }
    }
}
