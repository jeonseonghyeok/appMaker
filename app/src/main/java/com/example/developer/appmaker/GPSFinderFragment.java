package com.example.developer.appmaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
    private int currentMarkerIndex;
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
        currentMarkerIndex=-1;
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
                Log.d("logd", "id: "+marker.getId()+"  index:  "+marker.getId().substring(1)+"   rcode:"+getArguments().getInt(marker.getId()));
                if(marker.getTitle()!=null) {//위치좌표 또는 오류가게좌표 가 아닐때 이동동작 //본인의 좌표마커클릭시 오류를 방지
                    int markerIndex=Integer.parseInt(marker.getId().substring(1)) - 1;
                    moveToRestaurant(markerIndex);
                }
               return true;//false 해주어야 마크에 대한 정보가 뜸 리스트에서 선택시에 정보가 뜨지않는 문제로 true로 바꿈
            }
        });

    }

    public LatLng getCameraPosition(){
        return mMap.getCameraPosition().target;
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
                    .icon(getMarker(extra.getFloat("g"+i))));
                    //.icon(BitmapDescriptorFactory.defaultMarker(15*(5-extra.getFloat("g"+i)))));
        }
    }
//수정시 https://stackoverflow.com/questions/14851641/change-marker-size-in-google-maps-api-v2 참고
//기본마커
    private BitmapDescriptor getMarker(float grade){
        if(grade>=4)
            return BitmapDescriptorFactory.fromResource(R.drawable.map_marker_red);
        else if(grade>=2.5)
            return BitmapDescriptorFactory.fromResource(R.drawable.map_marker_pink);
        else if(grade>=1)
            return BitmapDescriptorFactory.fromResource(R.drawable.map_marker_yellow);
        else
            return BitmapDescriptorFactory.defaultMarker();
    }
    //클릭하였을때 나오는 큰마커
    private BitmapDescriptor getBigMarker(float grade){
        int markerSize = 150;
        BitmapDrawable bitmapdraw = null;
        if(grade>=4)
            bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.map_marker_red);
        else if(grade>=2.5)
            bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.map_marker_pink);
        else if(grade>=1)
            bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.map_marker_yellow);
        else
            return BitmapDescriptorFactory.defaultMarker();
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap BigMarker = Bitmap.createScaledBitmap(b, markerSize, markerSize, false);
        return  BitmapDescriptorFactory.fromBitmap((Bitmap) BigMarker);
    }
    /**
     * 현재 맵의 좌표를 이동시켜주는 메소드
     * @param latLng
     */
    private void movePosition(LatLng latLng){//맵의 좌표로 이동, 가게정보를 숨김
        if(currentMarkerIndex>=0){//이전에 선택된 마크가 있으면 사이즈를 되돌림
            m[currentMarkerIndex].setIcon(getMarker(extra.getFloat("g"+(currentMarkerIndex+1))));
            currentMarkerIndex=-1;
        }
        if(((MainActivity)getActivity()).ly_RestaurantInfo.getVisibility()==View.VISIBLE) {//가게정보가 켜져있을때
            ((MainActivity)getActivity()).showBasicButtonlayout();
        }
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
        //gpsPosition=latLng;
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(gpsPosition));
        movePosition(latLng);

    }
    //인덱스를 통해 가게로 이동
    public void goToRestaurant(int index){
        if(currentMarkerIndex>=0){//이전에 선택된 마크가 있으면 사이즈를 되돌림
            m[currentMarkerIndex].setIcon(getMarker(extra.getFloat("g"+(currentMarkerIndex+1))));
        }
        currentMarkerIndex=index;
        Log.d("logd", "인덱스 가게이동"+index);
        m[index].setIcon(getBigMarker(extra.getFloat("g"+(index+1))));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(m[index].getPosition()));

    }
    //지도에서 가게로 이동
    public void moveToRestaurant(int index){
        if(currentMarkerIndex>=0){//이전에 선택된 마크가 있으면 사이즈를 되돌림
            m[currentMarkerIndex].setIcon(getMarker(extra.getFloat("g"+(currentMarkerIndex+1))));
        }
        currentMarkerIndex=index;
        m[index].setIcon(getBigMarker(extra.getFloat("g"+(index+1))));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(m[index].getPosition()));
        ((MainActivity)getActivity()).RestaurantInfoOpen(index);//메인액티비티에서 가게의 정보를 띄움
    }
    public float getMapSize(){
        return mMap.getCameraPosition().zoom;
    }
}