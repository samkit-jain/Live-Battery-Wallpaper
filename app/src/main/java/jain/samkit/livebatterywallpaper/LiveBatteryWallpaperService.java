package jain.samkit.livebatterywallpaper;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LiveBatteryWallpaperService extends WallpaperService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new BatteryWallpaperEngine();
    }

    public class BatteryWallpaperEngine extends Engine {

        private final Handler handler = new Handler();
        private final Runnable runner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private boolean visible = true;
        private int width = 0;
        private int height = 0;

        public BatteryWallpaperEngine() {
            //
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;

            if (visible) {
                handler.post(runner);
            } else {
                handler.removeCallbacks(runner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);

            this.visible = false;

            handler.removeCallbacks(runner);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            this.width = width;
            this.height = height;

            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();

                if (canvas != null) {
                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                    int batteryPct = (int)(level * 100.0 / (float)scale);
                    batteryPct = 100 - batteryPct;

                    /*int hexcolor = 0x008000;
                    hexcolor += (batteryPct * (0x00FE80));

                    String str = Integer.toHexString(hexcolor);
                    str = "#" + str;
                    str = str.toUpperCase();*/

                    float hsv[] = {((100 - batteryPct) * 120f / 100f), 1f, 1f};

                    canvas.drawColor(Color.HSVToColor(hsv));
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            handler.removeCallbacks(runner);

            if (visible) {
                handler.postDelayed(runner, 10);
            }
        }
    }
}
