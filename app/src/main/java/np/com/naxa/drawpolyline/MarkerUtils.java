package np.com.naxa.drawpolyline;

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.Marker;

/**
 * Created on 8/23/17
 * by nishon.tan@gmail.com
 */

public class MarkerUtils {


    public void setMarkerBounce(final Marker marker) {
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();
        final long duration = 2000;
        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = Math.max(1 - interpolator.getInterpolation((float) elapsed/duration), 0);
                marker.setAnchor(0.5f, 1.0f +  t);

                if (t > 0.0) {
                    handler.postDelayed(this, 16);
                } else {
                    setMarkerBounce(marker);
                }
            }
        });
    }
}
