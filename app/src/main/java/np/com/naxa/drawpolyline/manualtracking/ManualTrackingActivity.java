package np.com.naxa.drawpolyline.manualtracking;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;
import com.thetechnocafe.gurleensethi.liteutils.ToastUtilsKt;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import np.com.naxa.drawpolyline.R;

public class ManualTrackingActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener, OnTabSelectListener, OnTabReselectListener {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_content)
    RelativeLayout mainContent;

    BottomBar bottomBar;


    private ArrayList<Marker> markersPresentOnMap = new ArrayList<>();


    private ArrayList<LatLng> points = new ArrayList<>();
    private Polyline polyline;
    private Bundle savedInstanceState;
    private Polygon polygon;
    private DrawingOption.DrawingType selectedDrawingType;
    private GoogleMap googleMap;

    private final String TAG = this.getClass().getSimpleName();
    private BottomSheet.Builder bottomSheet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initMap();
        setToolbar();
        initUI();
        showDrawOptionOnStart(savedInstanceState);

    }

    private void initUI() {
        selectedDrawingType = DrawingOption.DrawingType.POLYGON;

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(this);
        bottomBar.setOnTabReselectListener(
                this);
    }


    private void restoreMapDrawingState(Bundle savedInstanceState) {

        try {

            if (savedInstanceState == null) return;

            points = savedInstanceState.getParcelableArrayList("points");
            selectedDrawingType = (DrawingOption.DrawingType) savedInstanceState.getSerializable("selected_drawing");

            for (LatLng point : points) {
                putMarkerOnMap(point);

            }

            drawChosenDrawing();

        } catch (NullPointerException e) {
            showMsg("Failed to restore state");
        }
    }


    private void showDrawOptionOnStart(Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey("hide_bottom_sheet") && savedInstanceState.getBoolean("hide_bottom_sheet")) {
            //don't show dialog
        } else {
            showDrawOptions();
        }
    }


    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setToolbar() {

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Manual Measuring Mode");


    }

    @Override
    public boolean onMarkerClick(Marker marker) {


        return false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMarkerDragListener(this);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);

        initMapsUIElements(googleMap);
        restoreMapDrawingState(savedInstanceState);
    }

    @SuppressWarnings({"MissingPermission"})
    private void initMapsUIElements(GoogleMap googleMap) {

        googleMap.setMyLocationEnabled(true);

        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setPadding(0, 120, 0, 120);//to stop UI buttons to being overlapped and hidden

    }


    @Override
    public void onMapClick(LatLng point) {

        moveCamera(googleMap, point);
        putMarkerOnMap(point);

        addLatLon(point);
        drawChosenDrawing();

    }

    private void drawChosenDrawing() {

        if (points.size() == 0) return;


        if (selectedDrawingType == DrawingOption.DrawingType.POLYGON) {
            drawPolygon(points);
        } else if (selectedDrawingType == DrawingOption.DrawingType.POLYLINE) {
            drawPolyLine(points);
        }
    }

    private void putMarkerOnMap(LatLng point) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.draggable(true);
        markerOptions.title(String.valueOf(markersPresentOnMap.size() + 1));
        Marker marker = googleMap.addMarker(markerOptions);
        marker.setTag(markersPresentOnMap.size());
        markersPresentOnMap.add(marker);


    }

    private void drawPolyLine(ArrayList<LatLng> points) {


        if (polyline != null) {
            polyline.remove();
        }


        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLACK);
        polylineOptions.width(5);
        polylineOptions.addAll(points);
        polyline = googleMap.addPolyline(polylineOptions);


    }

    private void drawPolygon(List<LatLng> latLngList) {
        if (polygon != null) {
            polygon.remove();
        }

        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.fillColor(Color.GREEN);
        polygonOptions.strokeColor(Color.BLACK);
        polygonOptions.strokeWidth(5);
        polygonOptions.addAll(latLngList);
        polygon = googleMap.addPolygon(polygonOptions);
    }


    private void undoMarkerAdd() {

        if (points.size() == 0) return;

        int matchingIndex = (points.size() - 1);

        removeMarker(matchingIndex);
        removeLatLan(matchingIndex);
        drawChosenDrawing();

        moveCameraTopMaker(matchingIndex);

    }

    private void moveCameraTopMaker(int matchingIndex) {

        if (points.size() == 0) return;

        LatLng cameraPostion = points.get(matchingIndex - 1);
        moveCamera(googleMap, cameraPostion);
    }


    private void removeLatLan(int matchingIndex) {
        points.remove(matchingIndex);
    }

    private void addLatLon(LatLng point) {
        points.add(point);
    }

    private void addMarkerToList(Marker marker) {

    }

    private void removeMarker(int matchingIndex) {
        Marker marker = markersPresentOnMap.get(matchingIndex);
        marker.remove();

        markersPresentOnMap.remove(matchingIndex);


    }


    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng dragPosition = marker.getPosition();
        int markerIndex = (int) marker.getTag();

        handleMarkerDrag(markerIndex, dragPosition);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        LatLng dragPosition = marker.getPosition();
        int markerIndex = (int) marker.getTag();

        handleMarkerDrag(markerIndex, dragPosition);

    }

    private void handleMarkerDrag(int markerIndex, LatLng dragPosition) {


        points.set(markerIndex, dragPosition);

        drawChosenDrawing();

    }


    private void moveCamera(GoogleMap googleMap, LatLng latLng) {

        CameraPosition cameraPositon = CameraPosition.builder()
                .target(latLng)
                .zoom(googleMap.getCameraPosition().zoom)
                .bearing(googleMap.getCameraPosition().bearing)
                .tilt(googleMap.getCameraPosition().tilt)
                .build();

        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPositon), null);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {


        outState.putBoolean("hide_bottom_sheet", true);
        outState.putSerializable("selected_drawing", selectedDrawingType);
        outState.putParcelableArrayList("points", points);
        super.onSaveInstanceState(outState);
    }


    private void showMsg(String msg) {
        ToastUtilsKt.coloredLongToast(getApplicationContext(), msg, android.R.color.background_dark, android.R.color.white);

    }


    public boolean clearAllDrawings() {

        try {
            googleMap.clear();
            points.clear();
            markersPresentOnMap.clear();

            showMsg("Drawings cleared");
        } catch (NullPointerException e) {

        }

        return true;
    }

    @Override
    public void onTabSelected(@IdRes int tabId) {
        switch (tabId) {
            case R.id.tab_undo_drawing:
                undoMarkerAdd();
                break;

            case R.id.tab_clear_drawing:
                clearAllDrawings();
                break;

            case R.id.tab_save_drawing:
                showMsg("Coming soon");
                break;

            case R.id.tab_draw_options:
                showDrawOptions();
                break;
        }
    }

    private void showDrawOptions() {

        if (bottomSheet != null) {
            bottomSheet.show();
            return;
        }

        bottomSheet = new BottomSheet.Builder(this).title("Draw").sheet(R.menu.menu_draw_options).listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case R.id.action_draw_polygons:
                        selectedDrawingType = DrawingOption.DrawingType.POLYGON;
                        break;
                    case R.id.action_draw_polylines:
                        selectedDrawingType = DrawingOption.DrawingType.POLYLINE;
                        break;
                    case R.id.action_use_dark_map:
                        loadMapStyle(R.raw.dark_gmaps);
                        break;
                    case R.id.action_use_light_map:
                        loadMapStyle(R.raw.light_gmaps);
                        break;
                }
            }
        });

        bottomSheet.show();
    }

    private void loadMapStyle(int resource) {
        try {
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, resource));

        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onTabReSelected(@IdRes int tabId) {
        switch (tabId) {
            case R.id.tab_undo_drawing:
                undoMarkerAdd();
                break;
            case R.id.tab_clear_drawing:
                clearAllDrawings();
                break;
            case R.id.tab_draw_options:
                showDrawOptions();
                break;
        }
    }


}
