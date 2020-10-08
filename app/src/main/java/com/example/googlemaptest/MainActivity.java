package com.example.googlemaptest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements LocationListener, OnMapReadyCallback, GoogleMap.OnMapLongClickListener
        , ExampleDialog.ExampleDialogListener{

    private GoogleMap map;  //操控地圖的物件
    LatLng currPoint;   //儲存目前的位置
    TextView tv;

    static final int MIN_TIME = 5000000;  //位置更新條件：5000000 毫秒
    static final float MIN_DIST = 0;    //位置更新條件：5 公尺
    LocationManager mgr;    // 定位管理員

    boolean isGPSEnabled;   //GPS定位是否可用
    boolean isNetworkEnabled;   //網路定位是否可用

    static final String db_name = "testDB";
    static final String tb_name = "test";
    SQLiteDatabase db;

    boolean markerSwitch;
    String markerIcon;
    final int SETTINGS_ACTIVITY = 1;

    FusedLocationProviderClient fusedLocationProviderClient;
    Location location;
    LocationRequest locationRequest;
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    private DrawerLayout drawer;
    //String json_string;
    String mJsonString = null;
    NavigationView navigationView;
    ListView lv;
    ContactAdapter contactAdapter;
    JSONObject mJsonObj = null;
    JSONArray jsonArray;
    Button btnMove;
    LatLng mMoveToPoint;
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*-------------------------------------------------------------------*/
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        OkHttpClient client = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .build();
        String url = "http://srv.avema.com.tw:7000/api/dbgGetDevice";
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    final String myResponse = response.body().string();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mJsonString = myResponse;
                            LoadList();
                        }
                    });
                }
            }
        });

        /*-------------------------------------------------------------------*/

        //取得系統服務的LocationManager物件
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        tv = findViewById(R.id.tv);

        //取得佈局上的 map 元件
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);  //註冊 Google Map onMapReady 事件監聽器

        // 檢查若尚未授權, 則向使用者要求定位權限

        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        String createTable = "CREATE TABLE IF NOT EXISTS " +
                tb_name +            // 資料表名稱
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + //索引欄位
                "name VARCHAR(32), " +    //名稱欄位
                "latitude REAL(64), " +    //座標欄位
                "longitude REAL(64), " +    //座標欄位
                "info VARCHAR(64))";    //資訊欄位
        db.execSQL(createTable);    // 建立資料表

        checkPermission();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        checkPermission2();

        //db.close();
    }

    /*-------------------------------------------------------------------*/
    @Override
    public void onBackPressed(){
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else
            super.onBackPressed();
    }

    public void LoadList(){
        lv = (ListView) findViewById(R.id.lv);
        lv.setBackgroundResource(R.drawable.customshape);
        contactAdapter = new ContactAdapter(this, R.layout.device_item);
        lv.setAdapter(contactAdapter);
        try {
            if( mJsonString == null )
                return;

            mJsonObj = new JSONObject(mJsonString);

            jsonArray = mJsonObj.getJSONArray("ids");
            int count=0;
            String name;
            Long dev_id;
            Double x, y;
            int spd;
            while(count<jsonArray.length()){
                JSONObject JO = jsonArray.getJSONObject(count);
                name = JO.getString("name");
                dev_id = JO.getLong("dev_id");
                x = JO.getDouble("x");
                y = JO.getDouble("y");
                spd = JO.getInt("spd");
                Contacts contacts = new Contacts(name, dev_id, x, y , spd);
                contactAdapter.add(contacts);
                count++;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //mMoveToPoint = new LatLng(parent.getAdapter().getItem(position),lv.getY());
                map.animateCamera(CameraUpdateFactory.newLatLng(mMoveToPoint));
            }
        });
    }

    /*-------------------------------------------------------------------*/

    public void LoadMarker(int mt) {
        Cursor c = db.rawQuery("SELECT * FROM " + tb_name, null);

        int[] markerType = new int[5];
        markerType[1] = R.drawable.m1;
        markerType[2] = R.drawable.m2;
        markerType[3] = R.drawable.m3;
        markerType[4] = R.drawable.m4;

        int MarkerCount = c.getCount();
        c.moveToFirst();
        for (int i = 0; i < MarkerCount; i++) {
            String name = c.getString(1);
            double lat = c.getDouble(2);
            double lng = c.getDouble(3);
            String info = c.getString(4);
            LatLng thePoint = new LatLng(lat, lng);
            map.addMarker(new MarkerOptions()
                    .position(thePoint)
                    .title(name)
                    .snippet(info)
                    .icon(BitmapDescriptorFactory.fromResource(markerType[mt])));
            c.moveToNext();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_ACTIVITY) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            markerSwitch = prefs.getBoolean("switch_preference_1", true);
            markerIcon = prefs.getString("list_preference_1", "1");
            if (markerSwitch == true) {
                int i = Integer.parseInt(markerIcon);
                LoadMarker(i);
                switch (markerIcon) {
                    case "1":
                        map.clear();
                        LoadMarker(1);
                        break;
                    case "2":
                        map.clear();
                        LoadMarker(2);
                        break;
                    case "3":
                        map.clear();
                        LoadMarker(3);
                        break;
                    case "4":
                        map.clear();
                        LoadMarker(4);
                        break;
                }
            } else
                map.clear();
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        markerIcon = prefs.getString("list_preference_1", "1");
        int mt = Integer.parseInt(markerIcon);
        int[] markerType = new int[5];
        markerType[1] = R.drawable.m1;
        markerType[2] = R.drawable.m2;
        markerType[3] = R.drawable.m3;
        markerType[4] = R.drawable.m4;
        map.addMarker(new MarkerOptions()
                .position(point)
                .title("新增地標")
                .icon(BitmapDescriptorFactory.fromResource(markerType[mt])));

        Double latitude = point.latitude;
        Double longitude = point.longitude;

        ExampleDialog exampleDialog = new ExampleDialog();
        Bundle bundle = new Bundle();
        bundle.putDouble("latitude", latitude);
        bundle.putDouble("longitude", longitude);
        exampleDialog.setArguments(bundle);

        // 顯示交談窗
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    public void applyTexts(String name, String info) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        enableLocationUpdates(true);    //開啟定位更新功能
    }

    @Override
    protected void onPause() {
        super.onPause();
        enableLocationUpdates(false);   //關閉定位更新功能
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //將功能表載入到 menu 物件
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) { // 依照選項的 id 來處理
            case R.id.markersetting:
                startActivityForResult
                        (new Intent(this, com.example.googlemaptest.Settings.class), SETTINGS_ACTIVITY);
                break;
            case R.id.markerlist:
                Intent it2 = new Intent();
                it2.setClass(this, MarkerList.class);
                startActivity(it2);
                break;
            case R.id.currLoction:
                if (currPoint == null) {
                    Intent i = new Intent( // 利用 Intent 啟動系統的定位服務設定
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(i);
                    break;
                }
                else
                    map.animateCamera( // 將地圖中心點移到目前位置
                            CameraUpdateFactory.newLatLng(currPoint));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //第2.3.4個也是 LocationListener 介面的方法, 未用到
    @Override
    public void onLocationChanged(Location location) {  //取得位置資訊
        if (location != null) { // 如果可以取得座標
            tv.setText(String.format("緯度 %.4f, 經度 %.4f (%s 定位 )",
                    location.getLatitude(),  // 目前緯度
                    location.getLongitude(), // 目前經度
                    location.getProvider()));// 定位方式

            currPoint = new LatLng(          //依照目前經緯度建立LatLng 物件
                    location.getLatitude(), location.getLongitude());
            if (map != null) {              // 如果 Google Map 已經啟動完畢
                map.animateCamera(CameraUpdateFactory.newLatLng(currPoint)); // 將地圖中心點移到目前位置
                map.addMarker(new MarkerOptions().position(currPoint).title("目前位置")); //標記目前位置
            }
        }
        else { // 無法取得座標
            tv.setText("暫時無法取得定位資訊...");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){}

    @Override
    public void onProviderEnabled(String provider){}

    @Override
    public void onProviderDisabled(String provider){}

    //檢查若尚未授權, 則向使用者要求定位權限
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
        }

    }

    private void checkPermission2() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUIValues(location);
                }
            });
        }
        else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location) {
        Toast.makeText(this, String.valueOf(location.getLatitude()), Toast.LENGTH_LONG).show();
        Toast.makeText(this, String.valueOf(location.getLongitude()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200){
            if (grantResults.length >= 1 &&
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {  // 使用者不允許權限
                Toast.makeText(this, "程式需要定位權限才能運作", Toast.LENGTH_LONG).show();
            }
        }
        switch (requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission2();
                }
                else{
                    Toast.makeText
                            (this, "This app requires permission to be granted in order to work properly",
                                    Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void enableLocationUpdates(boolean isTurnOn) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {  // 使用者已經允許定位權限
            if (isTurnOn) {
                //檢查 GPS 與網路定位是否可用
                isGPSEnabled = mgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // 無提供者, 顯示提示訊息
                    Toast.makeText(this, "請確認已開啟定位功能!", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "取得定位資訊中...", Toast.LENGTH_LONG).show();

                    //每次取得新的定位資料, 就通知程式
                    if (isGPSEnabled)
                        mgr.requestLocationUpdates(   //向 GPS 定位提供者註冊位置事件監聽器
                                LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, this);
                    if (isNetworkEnabled)
                        mgr.requestLocationUpdates(   //向網路定位提供者註冊位置事件監聽器
                                LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, this);
                }
            }
            else {
                mgr.removeUpdates(this);    //停止監聽位置事件
            }
            location = mgr.getLastKnownLocation("network");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {  //Google Map 啟動完畢可以使用
        map = googleMap;  //取得 Google Map 物件, 此物件可以操控地圖
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL); //設定地圖為普通街道模式
        map.moveCamera(CameraUpdateFactory.zoomTo(18));  //將地圖縮放級數改為 18
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.setOnMapLongClickListener(this);
        onActivityResult(1, 0, null);
    }

}
