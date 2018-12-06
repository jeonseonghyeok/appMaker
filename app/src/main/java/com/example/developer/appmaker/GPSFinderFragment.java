package com.example.developer.appmaker;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
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
    private Marker[] m;
    private float cameraZoomSize;
    private Bundle extra;
    Marker position;
    public interface GPSListener {
        void onReceivedLatLng(LatLng position);
       // void selectedRestaurant(int position);
    }

    private GPSListener mOnMyListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() != null && getActivity() instanceof GPSListener) {
            mOnMyListener = (GPSListener) getActivity();
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
        cameraZoomSize= 15f;
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
        extra = getArguments();//액티비티에서 전송한 데이터를 받아오는 객체
        mMap = googleMap;
        //정문위치 lat 37.8663 lng127.7385
        mMap.setMaxZoomPreference(17f);
        mMap.setMinZoomPreference(13f);
        //final Marker[] m= new Marker[1];
        gpsPosition = new LatLng(extra.getDouble("GPSLat"), extra.getDouble("GPSLng"));
        position=mMap.addMarker(new MarkerOptions().position(gpsPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.android)));
        searchResult();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gpsPosition, (float) extra.getDouble("csize")));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mOnMyListener.onReceivedLatLng(latLng); //맵이동에 따른 메인액티비티 위치부분 텍스트 변환]
                movePosition(latLng);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d("asd", "id: "+marker.getId()+"  index:  "+marker.getId().substring(1)+"   rcode:"+getArguments().getInt(marker.getId()));
                if(marker.getTitle()!=null)//위치좌표 또는 오류가게좌표 가 아닐때 이동동작
                    moveToRestaurant(Integer.parseInt(marker.getId().substring(1))-1);
               return false;//false 해주어야 마크에 대한 정보가 뜸
            }
        });
    }

    /**
     *검색결과를 지도에 뿌려주는 메소드;
     */
    private  void searchResult(){
        int size = extra.getInt("size");//검색된 수 만큼 좌표와 정보가져오기
        //stores=new StoreInfo[size];
        m= new Marker[size];

        for(int i=1;i<=size;i++){
            m[i-1]=mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(extra.getDouble("lat"+i),extra.getDouble("lng"+i)))
                    .title(extra.getString("n"+i))
                    .alpha((extra.getFloat("g"+i)+2)/7f)//투명도
                    .snippet("평점 "+extra.getFloat("g"+i))
                    .icon(BitmapDescriptorFactory.defaultMarker(15*(5-extra.getFloat("g"+i)))));
        }
    }

    /**
     * 현재 맵의 좌표를 이동시켜주는 메소드
     * @param latLng
     */
    private void movePosition(LatLng latLng){//맵의 좌표로 이동, 가게정보를 숨김
        ((MainActivity)getActivity()).RestaurantInfoClose();
        gpsPosition=latLng;
        position.remove();
        position=mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.android)));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /**
     * 액티비티에서 위치검색시 이동을 위한 메소드
     * @param latLng
     */
    public void changedPosition(LatLng latLng){
        gpsPosition=latLng;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gpsPosition));
        movePosition(latLng);

    }
    //인덱스를 통해 가게로 이동
    public void goToRestaurant(int index){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gpsPosition));

    }
    //지도에서 가게로 이동
    public void moveToRestaurant(int index){
        mMap.animateCamera(CameraUpdateFactory.newLatLng(m[index].getPosition()));
        ((MainActivity)getActivity()).RestaurantInfoOpen(index);
    }
    public float getMapSize(){
        return mMap.getCameraPosition().zoom;
    }
    public void setMapSize(float mapSize){
       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gpsPosition,mapSize));
    }
}