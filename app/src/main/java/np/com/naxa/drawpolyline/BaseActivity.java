package np.com.naxa.drawpolyline;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.google.firebase.analytics.FirebaseAnalytics.Param;

public class BaseActivity extends AppCompatActivity {

    private final int REQUEST_ACCESS_FINE_LOCATION = 123;

    @BindView(R.id.lauch)
    Button lauch;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        ButterKnife.bind(this);


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @OnClick(R.id.lauch)
    public void onViewClicked() {
        if (hasFineLocationPermission()) {


            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Manual Tracking Mode");
            mFirebaseAnalytics.logEvent(Event.VIEW_ITEM, bundle);

            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private boolean hasFineLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                return false;
            }
            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(this, MainActivity.class));

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "2");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Manual Tracking Mode");
                    mFirebaseAnalytics.logEvent(Event.VIEW_ITEM, bundle);

                } else {

                }
                return;
            }


        }
    }
}
