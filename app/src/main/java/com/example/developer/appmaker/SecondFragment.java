package com.example.developer.appmaker;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SecondFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView = null;
    private GoogleMap mMap;
    private boolean isPermission = false;

    public SecondFragment() {
        // required
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_second, container, false);

        mapView = (MapView) layout.findViewById(R.id.map);
        mapView.getMapAsync(this);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //액티비티가 처음 생성될 때 실행되는 함수

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //정문위치 lng127.7385 lat 37.8663
        LatLng gpsPosition = new LatLng(37.869071, 127.742778);
        //StoreInfo stores[];
        final Marker[] m;
        Bundle extra = getArguments();//액티비티에서 전송한 데이터를 받아오는 객체
        int size = extra.getInt("size");//검색된 수 만큼 좌표와 정보가져오기
        //stores=new StoreInfo[size];
        m= new Marker[size+1];

        for(int i=0;i<size;i++){
            m[i+1]=mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(extra.getDouble("lat"+i),extra.getDouble("lng"+i)))
                    .title(extra.getString("n"+i))
                    .alpha((extra.getFloat("g"+i)+2)/7f)//투명도
                    .snippet(extra.getFloat("g"+i)+"점")
                    .icon(BitmapDescriptorFactory.defaultMarker(15*(5-extra.getFloat("g"+i)))));
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override

            public void onMapClick(LatLng latLng) {
                if (m[0] == null) {//마커 없을때
                    m[0] = mMap.addMarker(new MarkerOptions().position(latLng));
                } else {
                    m[0].remove();
                    m[0] = mMap.addMarker(new MarkerOptions().position(latLng));

                }

            }
        });

        mMap.addMarker(new MarkerOptions().position(gpsPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.android)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gpsPosition, 15.5f));
    }
}

