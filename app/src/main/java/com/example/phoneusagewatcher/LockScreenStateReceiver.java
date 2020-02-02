package com.example.phoneusagewatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class LockScreenStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // screen is turn off
            //("Screen locked");
        } else {
            //Handle resuming events if user is present/screen is unlocked
            //count++;
            //textView.setText(""+count);
            //("Screen unlocked");
        }
    }
}
