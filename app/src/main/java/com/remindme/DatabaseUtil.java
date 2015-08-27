package com.remindme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


//DatabaseUtil handles reading and writing data to the SQLiteDatabase
public class DatabaseUtil {
	private static final String FIELD_ROWID = "_id";  //need to use this specific field name so it will work with CursorAdapter
	private static final String FIELD_REMINDER = "reminder";
	private static final String FIELD_FREQUENCY = "frequency";
    private static final String FIELD_TIME_FROM = "time_from";
    private static final String FIELD_TIME_TO = "time_to";
    private static final String FIELD_MONDAY = "monday";
    private static final String FIELD_TUESDAY = "tuesday";
    private static final String FIELD_WEDNESDAY = "wednesday";
    private static final String FIELD_THURSDAY = "thursday";
    private static final String FIELD_FRIDAY = "friday";
    private static final String FIELD_SATURDAY = "saturday";
    private static final String FIELD_SUNDAY = "sunday";
    private static final String FIELD_RECURRING = "recurring";
    private static final String FIELD_NOTIFICATION_TYPE = "notification_type";
    private static final String FIELD_MESSAGE = "message_id";
    public static final String FIELD_ACTIVE = "active";
    public static final String FIELD_ALARM_TIME = "alarm_time";
	private static final String[] ALL_FIELDS = new String[] {FIELD_ROWID, FIELD_REMINDER, FIELD_FREQUENCY, FIELD_TIME_FROM, FIELD_TIME_TO, FIELD_MONDAY, FIELD_TUESDAY, FIELD_WEDNESDAY, FIELD_THURSDAY, FIELD_FRIDAY,
        FIELD_SATURDAY, FIELD_SUNDAY, FIELD_RECURRING, FIELD_NOTIFICATION_TYPE, FIELD_MESSAGE, FIELD_ACTIVE, FIELD_ALARM_TIME};

    private static final String FIELD_CURRENT_ID = "current_id";
    private static final String[] ALL_CURRENT_FIELDS = new String[] {FIELD_ROWID, FIELD_CURRENT_ID};
	
	//declare column indexes for each field
	public static final int COLUMN_ROWID = 0;
	public static final int COLUMN_REMINDER = 1;
	public static final int COLUMN_FREQUENCY = 2;
    public static final int COLUMN_CURRENT = 3;
    public static final int COLUMN_TIME_FROM = 3;
    public static final int COLUMN_TIME_TO = 4;
    public static final int COLUMN_MONDAY = 5;
    public static final int COLUMN_TUESDAY = 6;
    public static final int COLUMN_WEDNESDAY = 7;
    public static final int COLUMN_THURSDAY = 8;
    public static final int COLUMN_FRIDAY = 9;
    public static final int COLUMN_SATURDAY = 10;
    public static final int COLUMN_SUNDAY = 11;
    public static final int COLUMN_RECURRING = 12;
    public static final int COLUMN_NOTIFICATION_TYPE = 13;
    public static final int COLUMN_MESSAGE = 14;
    public static final int COLUMN_ACTIVE = 15;
    public static final int COLUMN_ALARM_TIME = 16;
    public static final int COLUMN_CURRENT_ID = 1;

	//set up database information
	private static final String DATABASE_NAME = "dbRemindMe";
	private static final String DATABASE_TABLE = "mainReminders";
    private static final String DATABASE_TABLE_CURRENT = "currentReminder";
	private static final int DATABASE_VERSION = 14; // The version number must be incremented each time a change to DB structure occurs.
		
	//set up SQL statement for creating database table
	private static final String DATABASE_CREATE_SQL = 
			"CREATE TABLE " + DATABASE_TABLE 
			+ " (" + FIELD_ROWID + " INTEGER PRIMARY KEY, "
			+ FIELD_REMINDER + " TEXT, "
			+ FIELD_FREQUENCY + " TEXT, "
            + FIELD_TIME_FROM + " REAL, "  //use milliseconds for database
            + FIELD_TIME_TO + " REAL, "
            + FIELD_MONDAY + " BOOLEAN, "
            + FIELD_TUESDAY + " BOOLEAN, "
            + FIELD_WEDNESDAY + " BOOLEAN, "
            + FIELD_THURSDAY + " BOOLEAN, "
            + FIELD_FRIDAY + " BOOLEAN, "
            + FIELD_SATURDAY + " BOOLEAN, "
            + FIELD_SUNDAY + " BOOLEAN, "
            + FIELD_RECURRING + " BOOLEAN, "
            + FIELD_NOTIFICATION_TYPE + " BOOLEAN, "
            + FIELD_MESSAGE + " INTEGER, "
            + FIELD_ACTIVE + " BOOLEAN, "
            + FIELD_ALARM_TIME + " INTEGER"
			+ ")";

    private static final String DATABASE_CREATE_CURRENT = "CREATE TABLE " + DATABASE_TABLE_CURRENT
            + " (" + FIELD_ROWID + " INTEGER PRIMARY KEY, "
            + FIELD_CURRENT_ID + " INTEGER)";
	
	private static Context mContext;
	private DatabaseHelper myDBHelper;
	private SQLiteDatabase myDb;

    //first constructor used by onCreate in main activity to set application context
    public DatabaseUtil(Context context) {
        mContext = context;
        myDBHelper = new DatabaseHelper(mContext);
    }

    //second constructor used by other classes without contexts
	public DatabaseUtil() {
		myDBHelper = new DatabaseHelper(mContext);
	}

	//open the database connection
	public DatabaseUtil open() {
		myDb = myDBHelper.getWritableDatabase();
		return this;
	}
	
	//close the database connection
	public void close() {
        myDBHelper.close();
	}

    //method to check if context has been set
    public static boolean hasContext() {
        if (mContext != null) {
            return true;
        } else {
            return false;
        }
    }

	//insert a new set of values into the main table
	public long insertRow(String reminder, String frequency, float timeFrom, float timeTo,
                          boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean sat, boolean sun,
                          boolean useType, boolean notificationType, int messageId, boolean active, long alarmTime) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(FIELD_REMINDER, reminder);
		initialValues.put(FIELD_FREQUENCY, frequency);
        initialValues.put(FIELD_TIME_FROM, timeFrom);
        initialValues.put(FIELD_TIME_TO, timeTo);
        initialValues.put(FIELD_MONDAY, mon);
        initialValues.put(FIELD_TUESDAY, tue);
        initialValues.put(FIELD_WEDNESDAY, wed);
        initialValues.put(FIELD_THURSDAY, thu);
        initialValues.put(FIELD_FRIDAY, fri);
        initialValues.put(FIELD_SATURDAY, sat);
        initialValues.put(FIELD_SUNDAY, sun);
        initialValues.put(FIELD_RECURRING, useType);
        initialValues.put(FIELD_NOTIFICATION_TYPE, notificationType);
        initialValues.put(FIELD_MESSAGE, messageId);
        initialValues.put(FIELD_ACTIVE, active);
        initialValues.put(FIELD_ALARM_TIME, alarmTime);

        //insert the data into the table
		return myDb.insert(DATABASE_TABLE, null, initialValues);
	}

    //update row in database with all parameters passed to method through Reminder class
    public int updateRow(Reminder reminder) {
        boolean days[] = reminder.getDays();
        ContentValues updateValues = new ContentValues();
        String where = FIELD_ROWID + "=" + reminder.getRowId();
        updateValues.put(FIELD_REMINDER, reminder.getReminder());
        updateValues.put(FIELD_FREQUENCY, reminder.getFrequency());
        updateValues.put(FIELD_TIME_FROM, reminder.getTimeFrom());
        updateValues.put(FIELD_TIME_TO, reminder.getTimeTo());
        updateValues.put(FIELD_MONDAY, days[0]);
        updateValues.put(FIELD_TUESDAY, days[1]);
        updateValues.put(FIELD_WEDNESDAY, days[2]);
        updateValues.put(FIELD_THURSDAY, days[3]);
        updateValues.put(FIELD_FRIDAY, days[4]);
        updateValues.put(FIELD_SATURDAY, days[5]);
        updateValues.put(FIELD_SUNDAY, days[6]);
        updateValues.put(FIELD_RECURRING, reminder.getRecurring());
        updateValues.put(FIELD_NOTIFICATION_TYPE, reminder.getNotificationType());
        updateValues.put(FIELD_MESSAGE, reminder.getMessageId());
        updateValues.put(FIELD_ACTIVE, reminder.isActive());
        updateValues.put(FIELD_ALARM_TIME, reminder.getAlarmTime());

        //update the data in the table
        return myDb.update(DATABASE_TABLE,updateValues,where,null);
    }

    public void updateSelectRow(String fieldName, long rowId, String newValue) {
        String sql = "UPDATE " + DATABASE_TABLE +
                " SET " + fieldName + " = '" + newValue +
                "' WHERE " + FIELD_ROWID + " = " + rowId;
        myDb.execSQL(sql);
    }

	//remove a row from the database using the rowId
	public boolean deleteRow(long rowId) {
		String where = FIELD_ROWID + "=" + rowId;
		return myDb.delete(DATABASE_TABLE, where, null) != 0;
	}

	//retrieve all the data in the main table
	public Cursor getAllRows() {
		Cursor cursor = myDb.query(true, DATABASE_TABLE, ALL_FIELDS, null, null, null, null, null, null);
		if (cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
		return cursor;
	}

    //set up override methods of DatabaseHelper class
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL);
            _db.execSQL(DATABASE_CREATE_CURRENT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			//remove old database tables
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CURRENT);

			//create new database
			onCreate(_db);
		}
	}


    //get row id of the row tagged as current
    public long getCurrentRowId(){
        long id = 0;

        String where = FIELD_ROWID + " = 1";
        Cursor cursor = myDb.query(true, DATABASE_TABLE_CURRENT, ALL_CURRENT_FIELDS,
                where, null, null, null, null, null);
        if (!(cursor.getCount() > 0)) {
            ContentValues insertValues = new ContentValues();
            where = FIELD_ROWID + " = 1";
            insertValues.put(FIELD_CURRENT_ID, "1");
            myDb.insert(DATABASE_TABLE_CURRENT, null, insertValues);
            cursor = myDb.query(true, DATABASE_TABLE_CURRENT, ALL_CURRENT_FIELDS, where, null, null, null, null, null);
        }
        cursor.moveToFirst();
        id = cursor.getLong(COLUMN_CURRENT_ID);
        cursor.close();
        return id;
    }



//end of DatabaseUtil class
}

