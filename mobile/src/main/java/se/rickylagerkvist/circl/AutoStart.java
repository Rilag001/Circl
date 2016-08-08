package se.rickylagerkvist.circl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStart extends BroadcastReceiver {
    public AutoStart() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startGeoFireService = new Intent(context, GeoFireService.class);
        context.startService(startGeoFireService);
        Log.i("Autostart", "started");
    }
}
