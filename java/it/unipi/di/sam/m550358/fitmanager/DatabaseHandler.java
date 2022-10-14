package it.unipi.di.sam.m550358.fitmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "FitManagerDatabase";

    private static final String TABLE_MUSIC_NAME = "MUSIC";
    private static final String MUSIC_COLUMN_ID = "ID";
    private static final String MUSIC_COLUMN_TITLE = "Title";
    private static final String MUSIC_COLUMN_DURATION = "Duration";
    private static final String MUSIC_COLUMN_URI = "Uri";

    private static final String TABLE_PLAN_NAME = "PIANIFICATION";
    private static final String PLAN_COLUMN_ID = "ID";
    private static final String PLAN_COLUMN_HOURS = "Hours";
    private static final String PLAN_COLUMN_WORKOUT = "Workout";
    private static final String PLAN_COLUMN_DATE = "Date";
    private static final String PLAN_COLUMN_DONE = "Done";

    private static final String TABLE_WORKOUT_NAME = "WORKOUT";
    private static final String WORKOUT_COLUMN_ID = "ID";
    private static final String WORKOUT_COLUMN_DATE = "Date";
    private static final String WORKOUT_COLUMN_HOURS = "Hours";
    private static final String WORKOUT_COLUMN_DESCRIPTION = "Description";
    private static final String WORKOUT_COLUMN_TIMESEC = "Seconds";
    private static final String WORKOUT_COLUMN_STEPS = "Steps";
    private static final String WORKOUT_COLUMN_STEPS_GOAL = "StepsGoal";
    private static final String WORKOUT_COLUMN_SPEED = "Speed";
    private static final String WORKOUT_COLUMN_IMAGE_ID = "ImageId";

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql;
        Log.d("DatabaseHandler.onCreate", "called");

        // crazione dello schema della tabella di pianificazione
        sql = "CREATE TABLE " + TABLE_PLAN_NAME + " (\n" +
                "    " + PLAN_COLUMN_ID + " INTEGER NOT NULL CONSTRAINT fitmanager_pk PRIMARY KEY AUTOINCREMENT,\n" +
                "    " + PLAN_COLUMN_DATE + " varchar(200) NOT NULL,\n" +
                "    " + PLAN_COLUMN_HOURS + " varchar(200) NOT NULL,\n" +
                "    " + PLAN_COLUMN_WORKOUT + " varchar(200) NOT NULL,\n" +
                "    " + PLAN_COLUMN_DONE + " int\n" +
                ");";
        db.execSQL(sql);

        // creazione dello schema della tabella musicale
        sql = "CREATE TABLE " + TABLE_MUSIC_NAME + " (\n" +
                "    " + MUSIC_COLUMN_ID + " INTEGER NOT NULL CONSTRAINT fitmanager_pk PRIMARY KEY AUTOINCREMENT,\n" +
                "    " + MUSIC_COLUMN_URI + " varchar(200) NOT NULL,\n" +
                "    " + MUSIC_COLUMN_TITLE + " varchar(200) NOT NULL,\n" +
                "    " + MUSIC_COLUMN_DURATION + " int\n" +
                ");";
        db.execSQL(sql);

        // creazione dello schema della tabella degli allenamenti
        sql = "CREATE TABLE " + TABLE_WORKOUT_NAME + " (\n" +
                "    " + WORKOUT_COLUMN_ID + " INTEGER NOT NULL CONSTRAINT fitmanager_pk PRIMARY KEY AUTOINCREMENT,\n" +
                "    " + WORKOUT_COLUMN_DATE + " varchar(200) NOT NULL,\n" +
                "    " + WORKOUT_COLUMN_HOURS + " varchar(200) NOT NULL,\n" +
                "    " + WORKOUT_COLUMN_DESCRIPTION + " varchar(200) NOT NULL,\n" +
                "    " + WORKOUT_COLUMN_TIMESEC + " int,\n" +
                "    " + WORKOUT_COLUMN_STEPS + " int,\n" +
                "    " + WORKOUT_COLUMN_STEPS_GOAL + " int,\n" +
                "    " + WORKOUT_COLUMN_SPEED + " double,\n" +
                "    " + WORKOUT_COLUMN_IMAGE_ID + " int\n" +
                ");";
        db.execSQL(sql);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String sql;
        sql = "DROP TABLE IF EXISTS " + TABLE_PLAN_NAME + ";";
        db.execSQL(sql);
        sql = "DROP TABLE IF EXISTS " + TABLE_MUSIC_NAME + ";";
        db.execSQL(sql);

        onCreate(db);
    }


    public boolean insertMusicRecord(String title, String duration, String dataUri) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(MUSIC_COLUMN_TITLE, title);
        cv.put(MUSIC_COLUMN_DURATION, duration);
        cv.put(MUSIC_COLUMN_URI, dataUri);
        return db.insert(TABLE_MUSIC_NAME,null, cv) != -1;
    }


    public boolean insertPlanRecord(String date, String hours, String workout, int done) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(PLAN_COLUMN_DATE, date);
        cv.put(PLAN_COLUMN_HOURS, hours);
        cv.put(PLAN_COLUMN_WORKOUT, workout);
        cv.put(PLAN_COLUMN_DONE, done);
        return db.insert(TABLE_PLAN_NAME,null, cv) != -1;
    }


    public boolean insertWorkoutRecord(String date, String hours, String description, int time, int steps, int stepsGoal, double speed, int imageId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(WORKOUT_COLUMN_DATE, date);
        cv.put(WORKOUT_COLUMN_HOURS, hours);
        cv.put(WORKOUT_COLUMN_DESCRIPTION, description);
        cv.put(WORKOUT_COLUMN_TIMESEC, time);
        cv.put(WORKOUT_COLUMN_STEPS, steps);
        cv.put(WORKOUT_COLUMN_STEPS_GOAL, stepsGoal);
        cv.put(WORKOUT_COLUMN_SPEED, speed);
        cv.put(WORKOUT_COLUMN_IMAGE_ID, imageId);
        return db.insert(TABLE_WORKOUT_NAME,null, cv) != -1;
    }


    public boolean deletePlanRecord(String date, String hours) {
        return getWritableDatabase().delete(TABLE_PLAN_NAME, PLAN_COLUMN_DATE+"=? AND "+PLAN_COLUMN_HOURS+"=?", new String[]{date,hours}) == 1;
    }


    public boolean deleteMusicRecord(String uri) {
        return getWritableDatabase().delete(TABLE_MUSIC_NAME, MUSIC_COLUMN_URI+"=?", new String[]{uri}) == 1;
    }


    public boolean deleteWorkoutRecord(String date, String hours) {
        return getWritableDatabase().delete(TABLE_WORKOUT_NAME, WORKOUT_COLUMN_DATE+"=? AND "+WORKOUT_COLUMN_HOURS+"=?", new String[]{date,hours}) == 1;
    }


    public Cursor getAllRecordsFromMusicTable() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_MUSIC_NAME, null);
    }


    public Cursor getAllRecordsFromPlanTable() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_PLAN_NAME, null);
    }


    public Cursor getAllRecordsFromWorkoutTable() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_WORKOUT_NAME, null);
    }


    public boolean updatePlanRecord(String date, String hours, String workout, boolean done) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(PLAN_COLUMN_DONE, done?1:0);
        return db.update(
                TABLE_PLAN_NAME,
                cv,
                PLAN_COLUMN_DATE+"=? AND "+PLAN_COLUMN_HOURS+"=?",
                new String[]{date,hours}
        ) != -1;
    }
}
