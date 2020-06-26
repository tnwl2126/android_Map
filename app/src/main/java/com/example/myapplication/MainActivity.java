package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.DownloadManager;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ListIterator;

import static android.view.View.INVISIBLE;

public class MainActivity extends AppCompatActivity  implements OnMapReadyCallback {

    private NaverMap myMap;

    //위도경도
    private Double latitude, longitude;

    private Button btn_basic, btn_navi, btn_hybrid, btn_terrain, btn_satellite,
            btn_visible, btn_layout;
    private TableLayout btn_group;

    private Marker marker;
    private CameraUpdate cameraUpdate;
    private Context context = this;
    private InfoWindow infoWindow;
    private List<Address> list;
    private String str = "";

    private URL url;
    private int num;

    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        btn_basic = (Button) findViewById(R.id.basic);
        btn_navi = (Button) findViewById(R.id.navi);
        btn_satellite = (Button) findViewById(R.id.satellite);
        btn_hybrid = (Button) findViewById(R.id.hybrid);
        btn_terrain = (Button) findViewById(R.id.terrain);
        btn_visible = (Button) findViewById(R.id.visible);
        btn_layout = (Button) findViewById(R.id.layoutBtn);

        btn_group = (TableLayout) findViewById(R.id.buttonGroup);
        num = 0;
        marker = new Marker();
        infoWindow = new InfoWindow();
        geocoder = new Geocoder(this);

        try {
            url=new URL("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(context) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return  str; } //"위도 : "+latitude + "   경도 : " + longitude;
        });

        btn_group.setVisibility(INVISIBLE);
        this.listener();
    }

    public void listener() {

        //일반지도
        btn_basic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myMap.setMapType(NaverMap.MapType.Basic);
                btn_group.setVisibility(INVISIBLE);
                btn_visible.setText(btn_basic.getText());
            }
        });
        //위성사진, 도로, 심벌 함께 노출
        btn_hybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myMap.setMapType(NaverMap.MapType.Hybrid);
                btn_group.setVisibility(INVISIBLE);
                btn_visible.setText(btn_hybrid.getText());
            }
        });
        //네비게이션지도
        btn_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myMap.setMapType(NaverMap.MapType.Navi);
                btn_group.setVisibility(INVISIBLE);
                btn_visible.setText(btn_navi.getText());
            }
        });
        //지형도
        btn_terrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myMap.setMapType(NaverMap.MapType.Terrain);
                btn_group.setVisibility(INVISIBLE);
                btn_visible.setText(btn_terrain.getText());
            }
        });
        //위성지도
        btn_satellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myMap.setMapType(NaverMap.MapType.Satellite);
                btn_group.setVisibility(INVISIBLE);
                btn_visible.setText(btn_satellite.getText());
            }
        });
        btn_visible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn_group.getVisibility() == View.VISIBLE) btn_group.setVisibility(INVISIBLE);
                else btn_group.setVisibility(View.VISIBLE);
            }
        });

        btn_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (num == 0) {
                    myMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, true);
                    btn_layout.setText("CADASTRAL");
                } else {
                    myMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, false);
                    num = -1;
                    btn_layout.setText("NONE");
                }
                num++;
            }
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.myMap = naverMap;
        myMap.setMapType(NaverMap.MapType.Basic);

        myMap.setOnMapClickListener((coord, point) -> {
            list = null;
            str = "";
            marker.setPosition(new LatLng(point.latitude, point.longitude));
            latitude = point.latitude;
            longitude = point.longitude;



            try {
                list = geocoder.getFromLocation(
                        latitude, longitude, // 위도,경도
                                10); // 얻어올 값의 개수
                } catch (IOException e) {
                e.printStackTrace();
                Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
                }
            if (list != null) {
                if (list.size() == 0) {
                    str = "해당되는 주소 정보는 없습니다.";
                    } else {
                    str += list.get(0).getAddressLine(0);
                    }
                }

            marker.setMap(myMap);
            cameraUpdate = CameraUpdate.scrollTo(marker.getPosition());
            myMap.moveCamera(cameraUpdate);
            infoWindow.close();
        });

        Overlay.OnClickListener listener = overlay -> {
            marker = (Marker) overlay;

            if (marker.getInfoWindow() == null) {
                infoWindow.open(marker);
            } else {
                infoWindow.close();
            }
            return true;
        };
        marker.setOnClickListener(listener);
    }
}