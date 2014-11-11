package ru.ifmo.md.colloquium2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Женя on 11.11.2014.
 */

public class VoteFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int REQUEST_CANDIDATE = 0;
    private static String DIALOG_CANDIDATE = "dialog_candidate";

    private static final int STATE_STARTED = 0;
    private static final int STATE_FINISHED = 1;
    private int mState;
    private ListView mListView;
    private int mCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mState = CandidateManager.get(getActivity()).queryState();
        mCount = CandidateManager.get(getActivity()).queryStateCount();
        if (mState == STATE_STARTED) {
            Toast.makeText(getActivity(), "Started", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getActivity(), "Finished", Toast.LENGTH_SHORT).show();
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vote_fragment, container, false);
        mListView = (ListView) v.findViewById(R.id.listView);
        Button addButton = (Button) v.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mState == STATE_STARTED) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    CandidatePickerFragment dialog = new CandidatePickerFragment();
                    dialog.setTargetFragment(VoteFragment.this, REQUEST_CANDIDATE);
                    dialog.show(fm, DIALOG_CANDIDATE);
                }
            }
        });

        Button finishButton = (Button) v.findViewById(R.id.finisfButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mState == STATE_STARTED) {
                    mState = STATE_FINISHED;
                    CandidateManager.get(getActivity()).setState(mState);
                }
            }
        });

        Button restartButton = (Button) v.findViewById(R.id.restartButton);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mState = STATE_STARTED;
                mCount = 0;
                CandidateManager.get(getActivity()).setStateCount(mCount);
                CandidateManager.get(getActivity()).setState(mState);
                CandidateManager.get(getActivity()).removeCandidates();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mState == STATE_STARTED) {
                    Candidate c = ((CandidateCursorAdapter) mListView.getAdapter()).get(i);
                    CandidateManager.get(getActivity()).updateCandidate(l, c.getCount() + 1);
                    mCount++;
                    CandidateManager.get(getActivity()).setStateCount(mCount);
                }
            }
        });

        return v;
    }

    public static VoteFragment newInstance() {
        return new VoteFragment();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_CANDIDATE) {
            String name = data.getStringExtra(CandidatePickerFragment.EXTRA_CANDIDATE_NAME);
            CandidateManager.get(getActivity()).insertCandidate(name);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CandidateListCursorLoader loader = new CandidateListCursorLoader(getActivity());
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        CandidateCursorAdapter adapter = new CandidateCursorAdapter(getActivity(), (CandidateContentProvider.CandidateCursor)cursor);
        mListView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mListView.setAdapter(null);
    }

    private static class CandidateListCursorLoader extends SQLiteCursorLoader {
        public CandidateListCursorLoader(Context context) {
            super(context);
        }
        @Override
        protected Cursor loadCursor() {
            return CandidateManager.get(getContext()).queryCandidates();
        }
    }

    private class CandidateCursorAdapter extends CursorAdapter {

        private CandidateContentProvider.CandidateCursor mCursor;

        public CandidateCursorAdapter(Context context, CandidateContentProvider.CandidateCursor cursor) {
            super(context, cursor, true);
            mCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.list_item, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Candidate candidate = mCursor.getCandidate();
            TextView name = (TextView) view.findViewById(R.id.candidate_list_item_name);
            name.setText(candidate.getName());
            TextView info = (TextView) view.findViewById(R.id.candidate_list_item_info);
            info.setText(candidate.getCount() + " from " + mCount);
        }

        public Candidate get(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getCandidate();
        }
    }


}
