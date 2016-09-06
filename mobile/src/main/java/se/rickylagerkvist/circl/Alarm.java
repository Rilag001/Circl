package se.rickylagerkvist.circl;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.Toast;

/**
 * Created by Ricky on 2016-09-06.
 * http://stackoverflow.com/questions/4459058/alarm-manager-example
 */
public class Alarm extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        //String mUserUid = PreferenceManager.getDefaultSharedPreferences(context).getString("USERUID", "defaultStringIfNothingFound");

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        // Put here YOUR code.
        //GeoFireService.setOnlineTrue(context);

        //DatabaseReference mOnlineUsers = FirebaseDatabase.getInstance().getReference("onlineUsers");
        //mOnlineUsers.child(mUserUid).setValue(true);

        Intent startGeoFireService = new Intent (context, GeoFireService.class);
        context.startService(startGeoFireService);

        Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_SHORT).show(); // For example

        wl.release();

    }

    public void setAlarm(Context context) {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000 * 60 * 5, pi); // Millisec * Second * Minute 5min

        Toast.makeText(context, "Alarm set", Toast.LENGTH_SHORT).show();
    }

}