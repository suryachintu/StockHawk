package com.udacity.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;

/**
 * Created by Surya on 17-01-2017.
 */

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(QuoteSyncJob.ACTION_DATA_VALID))
            Toast.makeText(context, "Symbol doesn't exists", Toast.LENGTH_SHORT).show();

    }
}
