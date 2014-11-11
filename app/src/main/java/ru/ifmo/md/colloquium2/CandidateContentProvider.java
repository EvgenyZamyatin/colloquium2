package ru.ifmo.md.colloquium2;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;

/**
 * Created by Женя on 09.11.2014.
 */
public class CandidateContentProvider extends ContentProvider {
    private static final String TAG = "CandidateContentProvider";

    //----------------
    //DB constants
    //----------------
    private static final String DB_NAME = "ru.ifmo.md.colloquium2.sqlite";
    private static final int VERSION = 1;

    public static final String TABLE_STATE = "state";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_STATE_NUMBER_VOTERS = "cnt_voters";

    public static final String TABLE_CANDIDATE = "candidate";
    public static final String COLUMN_CANDIDATE_ID = "_id";
    public static final String COLUMN_CANDIDATE_NAME = "name";
    public static final String COLUMN_CANDIDATE_COUNT = "voters_count";

    //----------------
    //Uri constants
    //----------------

    private static final String AUTHORITY = "ru.ifmo.md.colloquium2.providers.provider";
    public static final Uri STATE_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + TABLE_STATE);
    public static final Uri CANDIDATE_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + TABLE_CANDIDATE);
    private static final int URI_STATE = 1;
    private static final int URI_CANDIDATE = 2;
    private static final int URI_CANDIDATE_ID = 3;
    private static final UriMatcher mUriMatcher;
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, TABLE_STATE, URI_STATE);
        mUriMatcher.addURI(AUTHORITY, TABLE_CANDIDATE, URI_CANDIDATE);
        mUriMatcher.addURI(AUTHORITY, TABLE_CANDIDATE + "/#", URI_CANDIDATE_ID);
    }

    private DatabaseHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String table = null;
        Uri contentUri = null;
        switch (mUriMatcher.match(uri)) {
            case URI_STATE:
                table = TABLE_STATE;
                contentUri = STATE_CONTENT_URI;
                break;
            case URI_CANDIDATE:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = COLUMN_CANDIDATE_ID + " desc";
                }
                table = TABLE_CANDIDATE;
                contentUri = CANDIDATE_CONTENT_URI;
                break;
            case URI_CANDIDATE_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_CANDIDATE_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_CANDIDATE_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_CANDIDATE;
                contentUri = CANDIDATE_CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        Log.d(TAG, table);
        Cursor cursor = mHelper.getWritableDatabase().query(table, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), contentUri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        String table = null;
        switch (mUriMatcher.match(uri)) {
            case URI_STATE:
                table = TABLE_STATE;
                break;
            case URI_CANDIDATE:
                table = TABLE_CANDIDATE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        long id = mHelper.getReadableDatabase().insert(table, null, contentValues);
        Uri resultUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table = null;
        switch (mUriMatcher.match(uri)) {
            case URI_STATE:
                table = TABLE_STATE;
                break;
            case URI_CANDIDATE:
                table = TABLE_CANDIDATE;
                break;
            case URI_CANDIDATE_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_CANDIDATE_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_CANDIDATE_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_CANDIDATE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        int cnt = mHelper.getWritableDatabase().delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        String table = null;
        switch (mUriMatcher.match(uri)) {
            case URI_STATE:
                table = TABLE_STATE;
                break;
            case URI_CANDIDATE:
                table = TABLE_CANDIDATE;
                break;
            case URI_CANDIDATE_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_CANDIDATE_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_CANDIDATE_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_CANDIDATE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        int cnt = mHelper.getWritableDatabase().update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }


    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table state (" +
                            "id integer default 0, state integer default 0, cnt_voters integer default 0)"
            );
            db.execSQL("create unique index my_index on state (id)");
            ContentValues cv = new ContentValues();
            cv.put("id", 0);
            cv.put("state", 0);
            cv.put("cnt_voters", 0);
            db.insert("state", null, cv);
            db.execSQL("create table candidate (" +
                            "_id integer primary key autoincrement, " +
                            "name string, voters_count integer default 0)"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        }
    }

    public static class CandidateCursor extends CursorWrapper {
        public CandidateCursor(Cursor cursor, Context context) {
            super(cursor);
            setNotificationUri(context.getContentResolver(), CANDIDATE_CONTENT_URI);
        }
        public Candidate getCandidate() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            Candidate candidate = new Candidate();
            String name = getString(getColumnIndex(COLUMN_CANDIDATE_NAME));
            candidate.setName(name);
            long id = getLong(getColumnIndex(COLUMN_CANDIDATE_ID));
            candidate.setId(id);
            int cnt = getInt(getColumnIndex(COLUMN_CANDIDATE_COUNT));
            candidate.setCount(cnt);
            return candidate;
        }

    }



}
