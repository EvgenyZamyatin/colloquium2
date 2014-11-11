package ru.ifmo.md.colloquium2;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * Created by Женя on 11.11.2014.
 */
public class VoteActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return VoteFragment.newInstance();
    }
}
