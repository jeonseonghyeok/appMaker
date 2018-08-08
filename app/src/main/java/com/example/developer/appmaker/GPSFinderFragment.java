package com.example.developer.appmaker;

import android.content.Context;
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

public class GPSFinderFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView = null;
    private GoogleMap mMap;
    private boolean isPermission = false;
    private LatLng gpsPosition;
    public interface OnMyListener{
        void onReceivedLatLng(LatLng position);
    }
    private OnMyListener mOnMyListener;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(getActivity() != null && getActivity() instanceof OnMyListener){
            mOnMyListener = (OnMyListener) getActivity();
        }
    }
    {
        if(mOnMyListener !=null){
            mOnMyListener.onReceivedLatLng(gpsPosition);
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

        //final Marker[] m= new Marker[1];
        gpsPosition = new LatLng(getArguments().getDouble("GPSLat"), getArguments().getDouble("GPSLng"));
        final MarkerOptions a=new MarkerOptions().position(gpsPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.android));
        mMap.addMarker(a);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gpsPosition, 15.5f));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                    mOnMyListener.onReceivedLatLng(latLng);
                    mMap.clear();
                    a.position(latLng);
                    mMap.addMarker(a);
                }
        });


    }
}

