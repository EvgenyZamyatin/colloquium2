package ru.ifmo.md.colloquium2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Женя on 11.11.2014.
 */
public class CandidateManager {
    private static CandidateManager sManager;
    private Context mContext;
    private Uri mStateUri = Uri.parse("content://ru.ifmo.md.colloquium2.providers.provider/state");
    private Uri mCandidateUri = Uri.parse("content://ru.ifmo.md.colloquium2.providers.provider/candidate");

    private CandidateManager(Context context) {
        this.mContext = context;
    }

    public static CandidateManager get(Context context) {
        if (sManager == null) {
            sManager = new CandidateManager(context.getApplicationContext());
        }
        return sManager;
    }

    //--------------
    // Channels
    //--------------

    public long insertCandidate(String name) {
        ContentValues cv = new ContentValues();
        cv.put(CandidateContentProvider.COLUMN_CANDIDATE_NAME, name);
        long id = Long.parseLong(mContext.getContentResolver().insert(mCandidateUri, cv).getLastPathSegment());
        return id;
    }

    public int queryState() {
        Log.d("Manager", mContext.getContentResolver() + "");
        Cursor cursor = mContext.getContentResolver().query(mStateUri, null, "id = 0", null, null);
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndex(CandidateContentProvider.COLUMN_STATE));
    }

    public int queryStateCount() {
        Cursor cursor = mContext.getContentResolver().query(mStateUri, null, "id = 0", null, null);
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndex(CandidateContentProvider.COLUMN_STATE_NUMBER_VOTERS));
    }

    public CandidateContentProvider.CandidateCursor queryCandidates() {
        Cursor cursor = mContext.getContentResolver().query(mCandidateUri, null, null, null, null);
        return new CandidateContentProvider.CandidateCursor(cursor, mContext);
    }

    public CandidateContentProvider.CandidateCursor querySortedCandidates() {
        Cursor cursor = mContext.getContentResolver().query(mCandidateUri, null, null, null, CandidateContentProvider.COLUMN_CANDIDATE_COUNT + " desc");
        return new CandidateContentProvider.CandidateCursor(cursor, mContext);
    }


    public void updateCandidate(long id, int cnt) {
        Uri uri = ContentUris.withAppendedId(mCandidateUri, id);
        ContentValues cv = new ContentValues();
        cv.put(CandidateContentProvider.COLUMN_CANDIDATE_COUNT, cnt);
        mContext.getContentResolver().update(uri, cv, null, null);
    }

    public void setState(int state) {
        ContentValues cv = new ContentValues();
        cv.put(CandidateContentProvider.COLUMN_STATE, state);
        mContext.getContentResolver().update(mStateUri, cv, "id = 0", null);
    }

    public void setStateCount(int count) {
        ContentValues cv = new ContentValues();
        cv.put(CandidateContentProvider.COLUMN_STATE_NUMBER_VOTERS, count);
        mContext.getContentResolver().update(mStateUri, cv, "id = 0", null);
    }

    public void removeCandidates() {
        mContext.getContentResolver().delete(mCandidateUri
                , null
                , null);
    }



}
