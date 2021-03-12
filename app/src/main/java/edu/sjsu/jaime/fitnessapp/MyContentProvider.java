package edu.sjsu.jaime.fitnessapp;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

public class MyContentProvider extends ContentProvider {
    public MyContentProvider() {
    }

    static final String PROVIDER = "com.wearable.myprovider";
    static final String URL = "content://" + PROVIDER + "/workouts";
    static final Uri URI = Uri.parse(URL);

    static final String _ID = "_id";
    static final String TIME = "time";
    static final String CALORIES = "calories";
    static final String DISTANCE = "distance";

    Context mContext;

    private static HashMap<String, String> WORKOUTS_PROJECTION_MAP;

    static final int WORKOUTS = 1;
    static final int WORKOUTS_ID = 2;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher =  new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER, "workouts", WORKOUTS);
        uriMatcher.addURI(PROVIDER, "workouts/#", WORKOUTS_ID);
    }

    private void notifyChange(Uri uri)
    {
        ContentResolver resolver = mContext.getContentResolver();
        if (resolver != null)
        {
            resolver.notifyChange(uri, null);
        }
    }

    private int getMatchedID(Uri uri) {
        int matchedID = uriMatcher.match(uri);
        if (!(matchedID == WORKOUTS || matchedID == WORKOUTS_ID))
        {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return matchedID;

    }

    private String getIdString(Uri uri)
    {
        return (_ID + " = " + uri.getPathSegments().get(1));
    }

    private String getSelectionWithID(Uri uri, String selection)
    {
        String sel_str = getIdString(uri);
        if (!TextUtils.isEmpty(selection))
        {
            sel_str +=" AND (" + selection + ")";
        }
        return sel_str;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        String sel_str = (getMatchedID(uri) == WORKOUTS_ID) ?
                getSelectionWithID(uri, selection) : selection;

        count = db.delete(
                WORKOUT_TABLE_NAME,
                sel_str,
                selectionArgs);

        notifyChange(uri);
        return count;

    }

    @Override
    public String getType(Uri uri) {
        if (getMatchedID(uri) == WORKOUTS)
        {
            return "vnd.android.cursor.dir/vnd.wearable.workouts";
        }
        else
        {
            return "vnd.android.cursor.item/vnd.wearable.workouts";
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = db.insert(WORKOUT_TABLE_NAME, "", values);

        if (row > 0)
        {
            Uri _uri = ContentUris.withAppendedId(URI, row);
            notifyChange(_uri);
            return uri;
        }

        throw new SQLException("Failed to add a record into " + uri);

    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        Log.d("cont", "content provider: onCreatet()");

        mContext = getContext();
        if (mContext == null)
        {
            return false;
        }
        DB dbHelper = new DB(mContext);
        db = dbHelper.getWritableDatabase();
        if (db == null){
            return false;
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(WORKOUT_TABLE_NAME);

        if (getMatchedID(uri) == WORKOUTS)
        {
            sqLiteQueryBuilder.setProjectionMap(WORKOUTS_PROJECTION_MAP);
        }
        else
        {
            sqLiteQueryBuilder.appendWhere(getIdString(uri));
        }

        if (sortOrder == null || sortOrder == "")
        {
            sortOrder = _ID;
        }

        Cursor c = sqLiteQueryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(mContext.getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        int matchedID = getMatchedID(uri);

        String sel_str = (matchedID == WORKOUTS_ID) ?
                getSelectionWithID(uri, selection) : selection;

        count = db.update(
                WORKOUT_TABLE_NAME,
                values,
                sel_str,
                selectionArgs);

        notifyChange(uri);
        return count;
    }

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "myprovider";
    static final String WORKOUT_TABLE_NAME = "workouts";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + WORKOUT_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " time INTEGER NOT NULL, " +
                    " calories INTEGER NOT NULL, " +
                    " distance FLOAT NOT NULL);";

    private static class DB extends SQLiteOpenHelper
    {
        DB(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS " + WORKOUT_TABLE_NAME);
            onCreate(db);
        }
    }
}
