package com.example.michael.yell2;

import android.util.Log;

import com.kinvey.android.push.KinveyGCMService;

/**
 * Created by Michael on 8/1/16.
 */
public class GCMService extends KinveyGCMService {

    @Override
    public void onMessage(String message) {
        MainActivity.backend.onMessage(message);
    }
    @Override
    public void onError(String error) {
        displayNotification(error);
    }
    @Override
    public void onDelete(String deleted) {
        displayNotification(deleted);
    }
    @Override
    public void onRegistered(String gcmID) {
        displayNotification(gcmID);
    }
    @Override
    public void onUnregistered(String oldID) {
        displayNotification(oldID);
    }
    //This method will return the WakefulBroadcastReceiver class you define in the next step
    public Class getReceiver() {
        return GCMReceiver.class;
    }
    private void displayNotification(String message){

        Log.v("app_gcm", message);
    }
}
