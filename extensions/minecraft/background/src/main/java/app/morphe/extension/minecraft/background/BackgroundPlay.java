package app.morphe.extension.minecraft.background;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;


@SuppressWarnings("unused")
public final class BackgroundPlay {
    private static final int REQ_POST_NOTIFICATIONS = 0x4250;

    private BackgroundPlay() {}

    public static void onActivityCreate(Activity activity) {
        try {
            if (Build.VERSION.SDK_INT >= 33
                    && activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
            }
            Intent intent = new Intent(activity, BackgroundPlayService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                activity.startForegroundService(intent);
            } else {
                activity.startService(intent);
            }
        } catch (Throwable t) {
            android.util.Log.e("BackgroundPlay", "failed to start service", t);
        }
    }
    public static void skipNative(Object activity, long handle) {
    }
}
