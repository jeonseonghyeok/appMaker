package com.example.developer.appmaker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements GPSFinderFragment.OnMyListener{
    ViewPager vp;
    Button viewChangeButton,gpsFindButton;
    String[] languages;
    LatLng position;
    AutoCompleteTextView gpsSearch;
    Bundle bundle;
    EditText tag;
    String myJSON;

    private static final String TAG_RESULTS = "result";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_ADD = "address";
    JSONArray peoples = null;

    private GpsInfo gps;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;

    public void onReceivedLatLng(LatLng position){
        this.position=position;
        if(isChunCheon(position.latitude,position.longitude))
            gpsSearch.setText("지정좌표: "+position.latitude+"  "+position.longitude);
        else
            gpsSearch.setText("조회불가지역");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        gpsSearch= (AutoCompleteTextView) findViewById(R.id.gpsSearch);
        gps = new GpsInfo(MainActivity.this);
        tag = (EditText) findViewById(R.id.tagSearch);
        vp = (ViewPager) findViewById(R.id.vp);//프래그먼트 보는 화면

        vp.setAdapter(new gpsPagerAdapter(getSupportFragmentManager()));
        gpsFindButton=  (Button)findViewById(R.id.gpsSearchButton);
        // gps 권한 요청을 해야 함
            callPermission();
        languages =new String[7];

        languages[0]="서버:강원대 정문";
        languages[1]="서버:남춘천";
        languages[2]="서버:강원대 후문";
        languages[3]="서버:명동";
        languages[4]="개인:후평동";
        languages[5]="개인:스무숲";
        languages[6]="개인:스무숲2";

        gpsSearch.setThreshold(1);//문자 개수를 매개변수값 이상 입력하여야 실행됨(매개변수를 0이하로 주어도 1자 이상)
        gpsSearch.setAdapter(new ArrayAdapter<String>(this,android.R.layout.select_dialog_item, languages));
        gpsSearch.setSingleLine();
        gpsSearch.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(gpsSearch.getText().length()>0)
                    gpsSearch.setText("");
                else
                    gpsSearch.showDropDown();
                return false;
            }
        });
        gpsSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {//검색결과에서 선택시
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(index);//결과리스트 순서에서의 포지션
                Toast.makeText(getApplicationContext(), item, Toast.LENGTH_LONG).show();
                gpsMove(37.867071,127.742678);
            }
        });
        gpsFindButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                bundle = new Bundle();//액티비티에서 프래그먼트로 데이터전달을 위한 객체
                gps = new GpsInfo(MainActivity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation() && isChunCheon(gps.getLatitude(),gps.getLongitude())) {//사용가능할때&&춘천일때
                    gpsMove(gps.getLatitude(),gps.getLongitude());
                    gpsSearch.setText("현재 위치");
                } else {
                    gpsSearch.setText("직접설정(GPS사용불가)");
                    gpsMove(37.869071,127.742778);
                    // GPS 를 사용할수 없으므로 기본위치에서
                }
            }
        });
        tag.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH://엔터(검색)치면
                        bundle = new Bundle();//액티비티에서 프래그먼트로 데이터전달을 위한 객체

                         //listSearch(tag.getText().toString());
                      //  getData("http://210.115.48.131/getjson.php");
                        break;
                }
                return true;
            }
        });
    }
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            gpsSearch.setText("GPS권한없음(직접설정)");
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        }
        else//이전에 gps권한을 받았을때
        gpsFirstStart();
    }

    private  boolean isChunCheon(double lat,double lng){
        if((37.84<lat && lat<37.96)&&(127.70<lng && lng<127.79))
            return true;
        else
            return false;
    }


    private class searchResultPagerAdapter extends FragmentStatePagerAdapter {
        public searchResultPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            Fragment rFragment;
            switch (position) {
                case 0: {
                    rFragment = new FirstFragment();
                    rFragment.setArguments(bundle);
                    return rFragment;
                }
                case 1: {
                    rFragment = new SecondFragment();
                    rFragment.setArguments(bundle);
                    return rFragment;
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
    private class gpsPagerAdapter extends FragmentStatePagerAdapter {
        public gpsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            Fragment rFragment;
            switch (position) {
                case 0: {
                    rFragment = new GPSFinderFragment();
                    rFragment.setArguments(bundle);
                    return rFragment;
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    }
    private void gpsFirstStart()   {
        bundle = new Bundle();//액티비티에서 프래그먼트로 데이터전달을 위한 객체
        gps = new GpsInfo(MainActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation() && isChunCheon(gps.getLatitude(),gps.getLongitude())) {//사용가능할때&&춘천일때
            gpsMove(gps.getLatitude(),gps.getLongitude());
            gpsSearch.setText("현재 위치");
        } else {
            gpsSearch.setText("GPS 꺼짐(직접설정)");
            gpsMove(37.869071,127.742778);
            // GPS 를 사용할수 없으므로 기본위치에서
        }
    }
    private void listSearch(String tag) {

        StoreInfo stores[] = new StoreInfo[5];
        stores[0] = new StoreInfo(new LatLng(37.8730, 127.7445), tag + "정문", 5.0f);
        stores[1] = new StoreInfo(new LatLng(37.8730, 127.7450), tag + "돌돌삼겹살", 4.0f);
        stores[2] = new StoreInfo(new LatLng(37.8730, 127.7455), tag + "곱돌", 3.0f);
        stores[3] = new StoreInfo(new LatLng(37.8730, 127.7460), tag + "여진소", 2.0f);
        stores[4] = new StoreInfo(new LatLng(37.8730, 127.7465), tag + "손문", 1.0f);
        bundle.putInt("size", stores.length);
        for (int i = 0; i < stores.length; i++) {
            bundle.putFloat("g" + i, stores[i].grade);
            bundle.putString("n" + i, stores[i].name);
            bundle.putDouble("lat" + i, stores[i].gpsPosition.latitude);
            bundle.putDouble("lng" + i, stores[i].gpsPosition.longitude);
        }
        vp.setAdapter(new searchResultPagerAdapter(getSupportFragmentManager()));
    }

    protected void showList() {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);
            //  Toast.makeText(getApplicationContext(), "asd", Toast.LENGTH_SHORT).show();

            bundle.putInt("size", peoples.length());
            for (int i = 0; i < peoples.length(); i++) {
                JSONObject c = peoples.getJSONObject(i);
                //String id = c.getString(TAG_ID);
                bundle.putFloat("g" + i, 4.0f);
                bundle.putString("n" + i, c.getString(TAG_NAME));
                bundle.putDouble("lat" + i, 37.8735+0.0005*i);
                bundle.putDouble("lng" + i, 127.7465);


                // String address = c.getString(TAG_ADD);
                //HashMap<String, String> persons = new HashMap<String, String>();

            }
            vp.setAdapter(new searchResultPagerAdapter(getSupportFragmentManager()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void getData(String url) {

        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                myJSON = result;
                showList();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);

    }
    private void gpsMove(double lat,double lng){
        position=new LatLng(37.867071,127.742678);
        bundle.putDouble("GPSLat", lat);
        bundle.putDouble("GPSLng", lng);
        vp.setAdapter(new gpsPagerAdapter(getSupportFragmentManager()));
    }
}


