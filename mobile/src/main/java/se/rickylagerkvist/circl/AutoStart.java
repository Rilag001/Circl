package se.rickylagerkvist.circl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver {
    public AutoStart() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        /*Intent startGeoFireService = new Intent(context, GeoFireService.class);
        context.startService(startGeoFireService);
        Log.i("Autostart", "started");*/

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, GeoFireService.class);
            context.startService(serviceIntent);
        }

    }
}
