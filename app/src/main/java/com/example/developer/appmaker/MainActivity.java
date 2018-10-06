package com.example.developer.appmaker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements GPSFinderFragment.OnMyListener{
    ViewPager vp;
    Button viewChangeButton,gpsFindButton;
    String[] tagList_Array,gpsList_Array;
    LatLng position;//현재위치를 가지고있는 객체
    AutoCompleteTextView tagSearch,gpsSearch;
    Bundle bundle;
    EditText tag;
    String myJSON;
    InputMethodManager inputMethodManager;//키보드 사용유무를 관리하는 매니저

    private static final String TAG_ID = "id";
    private static final String TAG_ADD = "address";
    JSONArray restaurants = null;

    private GpsInfo gps;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    SQLiteDatabase sqliteDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        gpsSearch= (AutoCompleteTextView) findViewById(R.id.gpsSearch);
        tagSearch = (AutoCompleteTextView) findViewById(R.id.tagSearch);
        gps = new GpsInfo(MainActivity.this);
        vp = (ViewPager) findViewById(R.id.vp);//프래그먼트 보는 화면

        bundle = new Bundle();//액티비티에서 프래그먼트로 데이터전달을 위한 객체
        gpsMove(37.869071,127.742778);
        callPermission();// gps 권한 요청을 해야 함
        vp.setAdapter(new gpsPagerAdapter(getSupportFragmentManager()));
        gpsFindButton=  (Button)findViewById(R.id.gpsSearchButton);
        inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);//키보드를 관리하는 매니저

        //데이터베이스를 생성
        sqliteDB = init_database();
        init_tables() ;
        getTypes("http://210.115.48.131/getRestaurantType.php");//주소로 부터 가게대분류(타입)을 가져옴
        createPositionList();//gpsSearch의 기능을 만들어줌 리스트목록을 넣어 선택가능하도록

        //String sqlCreateTbl = "CREATE TABLE ORDER_T (NAME TEXT)" ;
        //sqliteDB.execSQL(sqlCreateTbl) ;


        /*tagSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH://엔터(검색)치면
                        //listSearch(tag.getText().toString());
                        getData("http://210.115.48.131/store_getData.php");
                        inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(),0);
                        break;
                }
                return true;
            }
        });*/
    }


    public void onReceivedLatLng(LatLng position){//GPSFinderFragment에서 좌표를 선택할 때
        inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(),0);
        if(isChunCheon(position.latitude,position.longitude)) {
            gpsSearch.setText("지정위치");
            gpsMove(position.latitude,position.longitude);
        }
        else
            gpsSearch.setText("조회불가지역");
    }
    public void onReceivedSavePosition(TextView positionName){//GPSFinderFragment에서 saveButton을 클릭하여 TextView가 전달될 때
        save_Position(positionName) ;
    }

    private void save_Position(final TextView positionName) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final String _positionName = positionName.getText().toString();
        String sqlSelect = "SELECT * " +
                "FROM CONTACT_T " +
                "WHERE NAME = '"+_positionName+"'";
        Cursor c=sqliteDB.rawQuery(sqlSelect,null);
       if(!isChunCheon(position.latitude,position.longitude)){
           alertDialogBuilder.setMessage("이 장소는 지원하지않는 장소입니다.");
           alertDialogBuilder.setPositiveButton("확인",
                   new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface arg0, int arg1) {
                           positionName.setText(null);
                       }
                   });
       }
        else if(c.getCount()!=0) {
           alertDialogBuilder.setMessage("장소 '" + _positionName + "'는 이미 존재하는 주소명입니다.");
           alertDialogBuilder.setPositiveButton("확인",
                   new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface arg0, int arg1) {
                           positionName.setText(null);
                       }
                   });

       }
       else {
           alertDialogBuilder.setMessage("장소 '" + _positionName + "'을(를) 저장하시겠습니까?");
           alertDialogBuilder.setPositiveButton("예",
                   new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface arg0, int arg1) {

                           //데이터 모두 지우기
                          // sqliteDB.execSQL("DELETE FROM CONTACT_T") ;
                           String sqlInsert = "INSERT INTO CONTACT_T " +
                                   "(NAME, LAT, LNG) VALUES ('" +
                                   _positionName + "'," +
                                   position.latitude + "," +
                                   position.longitude +
                                   ")";

                           System.out.println("log " + sqlInsert);
                           sqliteDB.execSQL(sqlInsert);
                           Toast.makeText(MainActivity.this, "장소 '" + _positionName + "'(이)가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                           positionName.setText(null);
                           createPositionList();//리스트를 다시만들어 어댑터로 연결
                           inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(),0);
                       }
                   });

           AlertDialog.Builder builder = alertDialogBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                   positionName.setText(null);
                   // finish();0
               }
           });
       }
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();



    }
    private void createPositionList(){
        Cursor cursor = sqliteDB.rawQuery("select NAME from CONTACT_T",null);
        gpsList_Array =new String[4+cursor.getCount()];

        gpsList_Array[0]="서버:강원대 정문";
        gpsList_Array[1]="서버:남춘천";
        gpsList_Array[2]="서버:강원대 후문";
        gpsList_Array[3]="서버:명동";
        int i=4;
        while( cursor.moveToNext() ) {
            gpsList_Array[i++]=cursor.getString(0);
        }
        gpsSearch.setThreshold(1);//문자 개수를 매개변수값 이상 입력하여야 실행됨(매개변수를 0이하로 주어도 1자 이상)
        gpsSearch.setAdapter(new ArrayAdapter<String>(this,android.R.layout.select_dialog_item, gpsList_Array));
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
        gpsSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {//위치검색결과에서 선택시
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(index);//결과리스트 순서에서의 포지션
                //Toast.makeText(getApplicationContext(), item, Toast.LENGTH_LONG).show();
                String sqlSelect = "SELECT * " +
                        "FROM CONTACT_T " +
                        "WHERE NAME = '"+item+"'";
                Cursor c=sqliteDB.rawQuery(sqlSelect,null);
                if(c.moveToFirst()) {//커서를 처음으로 이동시켜야 한다.
                    gpsMove(c.getDouble(1),c.getDouble(2));
                    GPSFinderFragment gpsFragment = (GPSFinderFragment)getSupportFragmentManager().findFragmentById(R.id.vp);
                    gpsFragment.changedPosition(position);
                    vp.setCurrentItem(1);
                    //vp.setAdapter(new gpsPagerAdapter(getSupportFragmentManager()));//맵을 새로 세팅
                }
                inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(),0);
            }
        });
        //현재위치찾기 버튼을 눌렀을때
        gpsFindButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                bundle = new Bundle();//액티비티에서 프래그먼트로 데이터전달을 위한 객체
                gps = new GpsInfo(MainActivity.this);
                // GPS 사용유무 가져오기
                GPSFinderFragment gpsFragment = (GPSFinderFragment)getSupportFragmentManager().findFragmentById(R.id.vp);
                if (gps.isGetLocation() && isChunCheon(gps.getLatitude(),gps.getLongitude())) {//사용가능할때&&춘천일때
                    gpsMove(gps.getLatitude(),gps.getLongitude());
                    gpsSearch.setText("현재 위치");
                    vp.setCurrentItem(1);
                } else {
                    gpsSearch.setText("직접설정(GPS사용불가)");
                    gpsMove(37.869071,127.742778);
                    // GPS 를 사용할수 없으므로 기본위치에서
                }
                gpsFragment.changedPosition(position);
                inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(),0);
            }
        });

    }
    private void init_tables() {
        if (sqliteDB != null) {
        String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS CONTACT_T (" +
                "NAME " + "TEXT," +
                "LAT " + "REAL," +
                "LNG " + "REAL" +
                ")" ;
            //System.out.println(sqlCreateTbl);
            sqliteDB.execSQL(sqlCreateTbl) ;
           // sqliteDB.execSQL("DROP TABLE IF EXISTS CONTACT_T") ;//테이블 삭제코드
        }
    }


    private SQLiteDatabase init_database() {
        SQLiteDatabase db = null ;
        // File file = getDatabasePath("contact.db") ;
        File file = new File(getFilesDir(), "contact.db") ;
       // System.out.println("PATH : " + file.toString()) ;
        try { db = SQLiteDatabase.openOrCreateDatabase(file, null) ;
        } catch (SQLiteException e) { e.printStackTrace() ;
        }
        if (db == null) {
          //  System.out.println("DB creation failed. " + file.getAbsolutePath()) ;
        }
        return db ;
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
        if((37.68<lat && lat<38.05)&&(127.50<lng && lng<128.00))
            return true;
        else
            return false;


    }


    private class searchResultPagerAdapter extends FragmentStatePagerAdapter {

        public searchResultPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int select) {
            Fragment rFragment;
            switch (select) {
                case 0: {
                    rFragment = new FirstFragment();
                    rFragment.setArguments(bundle);
                    return rFragment;
                }
                case 1: {
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
            return 2;
        }
    }
    private class gpsPagerAdapter extends FragmentStatePagerAdapter {
        public gpsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int select) {
            switch (select) {
                case 0: {
                    Fragment rFragment = new GPSFinderFragment();
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

    /**
     * 검색리스트정보를 보내주는 메소드
     */
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
    protected void showList() {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            restaurants = jsonObj.getJSONArray("result");//데이터집합의 이름
            bundle.putInt("size", restaurants.length());
            //   Log.d("logcatch", "showList: 4011line");
            for (int i = 0; i < restaurants.length(); i++) {
                JSONObject c = restaurants.getJSONObject(i);
                //String id = c.getString(TAG_ID);
                bundle.putFloat("g" + i, 4.0f);
                bundle.putString("n" + i, c.getString("rname"));
                bundle.putDouble("lat" + i, c.getDouble("rgps_lat"));
                bundle.putDouble("lng" + i,c.getDouble( "rgps_lng"));

            }
            vp.setAdapter(new searchResultPagerAdapter(getSupportFragmentManager()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * 검색리스트정보를 보내주는 메소드
     */

    public void getTypes(String url) {

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
                showTypes();
            }
        }

        GetDataJSON g = new GetDataJSON();
        g.execute(url);

    }
    protected void showTypes() {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            JSONArray types = jsonObj.getJSONArray("result");//데이터집합의 이름
            //Log.d("logcatch", types.length()+"showList: 4011line");
            tagList_Array=new String[types.length()];
            for (int i = 0; i < types.length(); i++) {
                JSONObject c = types.getJSONObject(i);
                tagList_Array[i]=c.getString("rtype");
            }
            tagSearch.setThreshold(1);//문자 개수를 매개변수값 이상 입력하여야 실행됨(매개변수를 0이하로 주어도 1자 이상)
            tagSearch.setAdapter(new ArrayAdapter<String>(this,android.R.layout.select_dialog_item, tagList_Array));
            tagSearch.setSingleLine();
            tagSearch.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event){
                    if(tagSearch.getText().length()>0)
                        tagSearch.setText("");
                    else
                        tagSearch.showDropDown();
                    return false;
                }
            });
            tagSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {//태그검색결과에서 선택시
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                    ListView listView = (ListView) parent;
                    String rtype = (String) listView.getItemAtPosition(index);//결과리스트 순서에서의 포지션
                    getData("http://210.115.48.131/store_getData.php");
                    inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(),0);
                }
            });
            tagSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    switch (actionId) {
                        case EditorInfo.IME_ACTION_SEARCH://태그엔터(검색)치면
                            getData("http://210.115.48.131/store_getData.php");
                            inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(), 0);
                            break;
                    }
                    return true;
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //현재위치를 확인하여 프래그먼트가 처음만들어질때 위치 알려주기위한 메소드
    private void gpsMove(double lat,double lng){
        bundle.putDouble("GPSLat", lat);
        bundle.putDouble("GPSLng", lng);
        position=new LatLng(lat,lng);
    }
}


