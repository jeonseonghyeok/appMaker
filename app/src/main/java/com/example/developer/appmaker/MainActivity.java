package com.example.developer.appmaker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
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


public class MainActivity extends AppCompatActivity implements GPSFinderFragment.GPSListener, FirstFragment.OnMyListener{
    ViewPager vp;
    LinearLayout ll;
    Button gpsFindButton,bt_downGrade,bt_upGrade,bt_reviewConfirm,bt_reviewCancel;
    ImageButton bt_review;
    RatingBar ratingBar;
    String[] tagList_Array,gpsList_Array;
    LatLng position;//현재위치를 가지고있는 객체
    AutoCompleteTextView tagSearch,gpsSearch;
    Bundle bundle;
    EditText tag;
    String myJSON;
    TextView tab_list;
    TextView tab_map ;
    LinearLayout strt_info;//가게정보를 띄워주는 화면//초기에 평가만 올리면 상세정보
    Dialog dialog;//리뷰를 위한 다이얼로그
    TextView strt_name;//선택된(selected)가게(restaurant) 이름(name)
    InputMethodManager inputMethodManager;//키보드 사용유무를 관리하는 매니저
    boolean isEmptyList;

    private static final String TAG_ID = "id";
    private static final String TAG_ADD = "address";
    JSONArray restaurants = null;

    private GpsInfo gps;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    SQLiteDatabase sqliteDB;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        gpsSearch= (AutoCompleteTextView) findViewById(R.id.gpsSearch);
        tagSearch = (AutoCompleteTextView) findViewById(R.id.tagSearch);
        gps = new GpsInfo(MainActivity.this);
        vp = (ViewPager) findViewById(R.id.vp);//프래그먼트 보는 화면
        ll = (LinearLayout)findViewById(R.id.ll);
        strt_info = (LinearLayout)findViewById(R.id.strt_info);
        strt_name = (TextView)  findViewById(R.id.strt_name);
        bundle = new Bundle();//액티비티에서 프래그먼트로 데이터전달을 위한 객체
        gpsMove(37.869071,127.742778);
        callPermission();// gps 권한 요청을 해야 함
        vp.setAdapter(new gpsPagerAdapter(getSupportFragmentManager()));
        gpsFindButton=  (Button)findViewById(R.id.gpsSearchButton);
        inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);//키보드를 관리하는 매니저
        tab_map=(TextView) findViewById(R.id.tab_map);
        tab_list=(TextView) findViewById(R.id.tab_list);
        tab_list.setOnClickListener(movePageListener);
        tab_list.setTag(0);
        tab_map.setOnClickListener(movePageListener);
        tab_map.setTag(1);
        tab_map.setSelected(true);
        isEmptyList=true;//검색 전 검색결과 리스트 존재하지않음
        bt_upGrade= (Button)findViewById(R.id.bt_upGrade);
        bt_downGrade= (Button)findViewById(R.id.bt_downGrade);
        bt_review=(ImageButton)findViewById(R.id.bt_review);
        ratingBar= (RatingBar)findViewById(R.id.ratingBar);
        //데이터베이스를 생성
        sqliteDB = init_database();
        init_tables() ;
        getTypes("http://210.115.48.131/getRestaurantType.php");//주소로 부터 가게대분류(타입)을 가져옴
        showPositionList();//gpsSearch의 기능을 만들어줌 리스트목록을 넣어 선택가능하도록

        vp.addOnPageChangeListener(pageChangeListener);
        bt_upGrade.setOnClickListener(upGradeListener);
        dialog=new Dialog(this);
        bt_downGrade.setOnClickListener(downGradeListener);
        bt_review.setOnClickListener(reviewBTListener);

    }



    /**
     * 리뷰를 등록하는 다이얼로그를 띄워주는 메소드
     */
    OnClickListener reviewBTListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ReviewDialog reviewDialog = new ReviewDialog(MainActivity.this);//리뷰다이얼로그를 생성한다.
            reviewDialog.callFunction(strt_info);

        }
    };

    /**
     * 점수를 올려주는 기능의 리스터
     */
    OnClickListener upGradeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ratingBar.setRating(ratingBar.getRating()+0.5f);
        }
    };
    /**
     * 점수를 내려주는 기능의 리스터
     */
    OnClickListener downGradeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ratingBar.setRating(ratingBar.getRating()-0.5f);
        }
    };
    ViewPager.OnPageChangeListener pageChangeListener=new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {

        }

        @Override
        public void onPageSelected(int tag)
        {
            int i = 0;
            if(!isEmptyList) {
                while (i < 2) {
                    if (tag == i) {
                        ll.findViewWithTag(i).setSelected(true);
                    } else {
                        ll.findViewWithTag(i).setSelected(false);
                    }
                    i++;
                }
            }
            if(tag==0){
                RestaurantInfoClose();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {

        }
    };
    /**
     * 탭 클릭에 대해 화면변환 리스너
     */
    OnClickListener movePageListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            int tag = (int) v.getTag();

            int i = 0;
            if(!isEmptyList) {
                while (i < 2) {
                    if (tag == i) {
                        ll.findViewWithTag(i).setSelected(true);
                    } else {
                        ll.findViewWithTag(i).setSelected(false);
                    }
                    i++;
                }
            }
            vp.setCurrentItem(tag);
            if(tag==0){
                RestaurantInfoClose();
            }
        }
    };

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
       if(!isChunCheon(position.latitude,position.longitude)){//춘천이 아니면 저장불가
           alertDialogBuilder.setMessage("이 장소는 지원하지않는 장소입니다.");
           alertDialogBuilder.setPositiveButton("확인",
                   new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface arg0, int arg1) {
                           positionName.setText(null);
                       }
                   });
       }
        else if(c.getCount()!=0) {//이미 존재하는 이름이면 저장불가
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
                           showPositionList();//장소리스트를 다시만들어 어댑터로 연결
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
        public Fragment getItem(int select) {
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
        public Fragment getItem(int select) {
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

    protected void showPositionList(){
        Cursor cursor = sqliteDB.rawQuery("select NAME from CONTACT_T",null);
        gpsList_Array =new String[cursor.getCount()];
        int i=0;
        while( cursor.moveToNext() ) {
            gpsList_Array[i++]=cursor.getString(0);
        }
        gpsSearch.setThreshold(1);//문자 개수를 매개변수값 이상 입력하여야 실행됨(매개변수를 0이하로 주어도 1자 이상)
        gpsSearch.setAdapter(new ArrayAdapter<String>(this,android.R.layout.select_dialog_item, gpsList_Array));
        gpsSearch.setSingleLine();
        gpsSearch.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                RestaurantInfoClose();
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
                }
                inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(),0);
            }
        });
        //현재위치찾기 버튼을 눌렀을때
        gpsFindButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
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

    /**
     * 검색제안목록(서버의 데이터)을 제공하는 메소드
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
                showTypeList();
            }
        }

        GetDataJSON g = new GetDataJSON();
        g.execute(url);

    }
    protected void showTypeList() {
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
                    RestaurantInfoClose();
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
                    getData("http://210.115.48.131/getSearchResult.php?search="+rtype);
                    isEmptyList=false;//검색동작으로 리스트생성됨을 알림
                    inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(),0);
                    tab_list.callOnClick();
                }
            });
            tagSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    switch (actionId) {
                        case EditorInfo.IME_ACTION_SEARCH://태그엔터(검색)치면
                            getData("http://210.115.48.131/getSearchResult.php?search="+tagSearch.getText());
                            isEmptyList=false;//검색동작으로 리스트생성됨을 알림
                            inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(), 0);
                            tab_list.callOnClick();
                            break;
                    }
                    return true;
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //좌표이동 메소드
    private void gpsMove(double lat,double lng){
        bundle.putDouble("GPSLat", lat);
        bundle.putDouble("GPSLng", lng);
        position=new LatLng(lat,lng);
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
                showRestaurantList();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Toast.makeText(MainActivity.this, "ddd.", Toast.LENGTH_SHORT).show();

            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);

    }
    protected void showRestaurantList() {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            restaurants = jsonObj.getJSONArray("result");//데이터집합의 이름
            int searchResult= restaurants.length();
            if(searchResult>0) {
                bundle.putInt("size", searchResult);
                //   Log.d("logcatch", "showList: 4011line");
                for (int i = 1; i <= restaurants.length(); i++) {
                    JSONObject c = restaurants.getJSONObject(i-1);
                    //String id = c.getString(TAG_ID);
                    bundle.putInt("m"+i,c.getInt("rcode"));//맵의 마커와 검색결과의 code를 매칭
                    bundle.putString("n" + i, c.getString("rname"));
                    bundle.putDouble("lat" + i, c.getDouble("rgps_lat"));
                    bundle.putDouble("lng" + i, c.getDouble("rgps_lng"));
                    bundle.putFloat("g" + i, (float) c.getDouble("rrga"));
                }

                vp.setAdapter(new searchResultPagerAdapter(getSupportFragmentManager()));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    @Override
    //리스트 프레그먼트에서 가게명을 클릭시 맵프레크먼트에서 동작
    public void selectedRestaurant(int index){
        GPSFinderFragment gpsFragment = (GPSFinderFragment)getSupportFragmentManager().findFragmentById(R.id.vp);
        gpsFragment.goToRestaurant(index);
        vp.setCurrentItem(1);
        RestaurantInfoOpen(index);
        //Log.d("getData", "selectedRestaurant: "+index);
    }
    //가게정보를 띄우는 메소드
    public void RestaurantInfoOpen(int index){
        strt_name.setText(bundle.getString("n"+(index+1))+" "+bundle.getFloat("g"+(index+1))+"점");
      //  strt_info.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,300));
        strt_info.setVisibility(View.VISIBLE);
        ratingBar.setRating(0);
    }
    //가게정보를 숨기는 메소드
    public void RestaurantInfoClose(){
        strt_info.setVisibility(View.GONE);

    }
}


