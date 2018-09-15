package com.example.developer.appmaker;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GPSFinderFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView = null;
    private GoogleMap mMap;
    private LatLng gpsPosition;
    private Button saveButton;
    private TextView positionName;
    Marker position;
    public interface OnMyListener {
        void onReceivedLatLng(LatLng position);
        void onReceivedSavePosition(TextView positionName);
    }

    private OnMyListener mOnMyListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() != null && getActivity() instanceof OnMyListener) {
            mOnMyListener = (OnMyListener) getActivity();
        }
    }


    public GPSFinderFragment() {
        // required
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_gpsfinder, container, false);

        mapView = (MapView) layout.findViewById(R.id.map);
        mapView.getMapAsync(this);
        saveButton = (Button) layout.findViewById(R.id.saveButton);
        positionName = (TextView) layout.findViewById(R.id.positionName);
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
        //정문위치 lat 37.8663 lng127.7385

        //final Marker[] m= new Marker[1];
        gpsPosition = new LatLng(getArguments().getDouble("GPSLat"), getArguments().getDouble("GPSLng"));
        position=mMap.addMarker(new MarkerOptions().position(gpsPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.android)));
        searchResult();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gpsPosition, 15.5f));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mOnMyListener.onReceivedLatLng(latLng); //맵이동에 따른 메인액티비티 위치부분 텍스트 변환]
                movePosition(latLng);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!positionName.getText().toString().isEmpty()) {
                    mOnMyListener.onReceivedSavePosition(positionName);
                }
            }
        });

    }

    /**
     *검색결과를 지도에 뿌려주는 메소드;
     */
    private  void searchResult(){
        final Marker[] m;
        Bundle extra = getArguments();//액티비티에서 전송한 데이터를 받아오는 객체
        int size = extra.getInt("size");//검색된 수 만큼 좌표와 정보가져오기
        //stores=new StoreInfo[size];
        m= new Marker[size];

        for(int i=0;i<size;i++){
            m[i]=mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(extra.getDouble("lat"+i),extra.getDouble("lng"+i)))
                    .title(extra.getString("n"+i))
                    .alpha((extra.getFloat("g"+i)+2)/7f)//투명도
                    .snippet(extra.getFloat("g"+i)+"점")
                    .icon(BitmapDescriptorFactory.defaultMarker(15*(5-extra.getFloat("g"+i)))));
        }
    }

    /**
     * 현재 맵의 좌표를 이동시켜주는 메소드
     * @param latLng
     */
    private void movePosition(LatLng latLng){//맵의 좌표를 이동
         position.remove();
        position=mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.android)));
           /*  mMap.clear();
        mOnMyListener.onReceivedLatLng(latLng);
        position.position(latLng);
        mMap.addMarker(position);
*/
    }

    /**
     * 액티비티에서 위치검색시 이동을 위한 메소드
     * @param latLng
     */
    public void changedPosition(LatLng latLng){
        gpsPosition=latLng;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gpsPosition, 15.5f));
        movePosition(latLng);
    }

}