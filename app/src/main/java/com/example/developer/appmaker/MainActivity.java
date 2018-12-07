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


public class MainActivity extends AppCompatActivity implements GPSFinderFragment.GPSListener, FirstFragment.OnMyListener {
    BackPressCloseHandler backPressCloseHandler;
    ViewPager vp;
    LinearLayout ll,ly_savePosition,ly_basicButtons;
    Button gpsFindButton, bt_downGrade, bt_upGrade, bt_reviewConfirm, bt_reviewCancel, bt_review, bt_savePosition;
    ImageButton bt_showSPL,bt_reSearch;
    RatingBar ratingBar;
    String[] tagList_Array, gpsList_Array;
    LatLng position;//현재위치를 가지고있는 객체
    AutoCompleteTextView tagSearch, gpsSearch;
    Bundle bundle;
    EditText tag;
    String myJSON;
    TextView tab_list, tab_map, positionName;
    LinearLayout strt_info;//가게정보를 띄워주는 화면//초기에 평가만 올리면 상세정보
    Dialog dialog;//리뷰를 위한 다이얼로그
    TextView strt_name;//선택된(selected)가게(restaurant) 이름(name)
    InputMethodManager inputMethodManager;//키보드 사용유무를 관리하는 매니저

    String user_id = "admin";
    boolean isEmptyList;//검색결과가 없는지 확인
    String searchedWord;//검색된 단어(새로운 단어가 검색되기 전까지 변경X)
    boolean isFirstReview = true;//첫리뷰인가(한 가게대상)
    float mapSize = 15f;
    String rvTag = "";
    String rvContent = "";
    int curRtCode;
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


        backPressCloseHandler = new BackPressCloseHandler(this);
        gpsSearch = (AutoCompleteTextView) findViewById(R.id.gpsSearch);
        tagSearch = (AutoCompleteTextView) findViewById(R.id.tagSearch);
        gps = new GpsInfo(MainActivity.this);
        vp = (ViewPager) findViewById(R.id.vp);//프래그먼트 보는 화면
        ll = (LinearLayout) findViewById(R.id.ll);
        strt_info = (LinearLayout) findViewById(R.id.strt_info);
        strt_name = (TextView) findViewById(R.id.strt_name);
        bundle = new Bundle();//액티비티에서 프래그먼트로 데이터전달을 위한 객체

        callPermission();// gps 권한 요청을 해야 함
        gpsMove(37.869071, 127.742778);
        isEmptyList = true;//검색이 실패로 가정
        searchedWord = "";
        vp.setAdapter(new searchResultPagerAdapter(getSupportFragmentManager()));
        gpsFindButton = (Button) findViewById(R.id.gpsSearchButton);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);//키보드를 관리하는 매니저
        tab_map = (TextView) findViewById(R.id.tab_map);
        tab_list = (TextView) findViewById(R.id.tab_list);
        positionName = (TextView) findViewById(R.id.positionName);
        tab_list.setOnClickListener(movePageListener);
        tab_list.setTag(0);
        tab_map.setOnClickListener(movePageListener);
        tab_map.setTag(1);
        tab_map.setSelected(true);
        tab_map.callOnClick();
        bt_upGrade = (Button) findViewById(R.id.bt_upGrade);
        bt_downGrade = (Button) findViewById(R.id.bt_downGrade);
        bt_review = (Button) findViewById(R.id.bt_review);
        bt_savePosition= (Button) findViewById(R.id.bt_savePosition);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        bt_showSPL = (ImageButton)findViewById(R.id.bt_showSPL);
        bt_reSearch =  (ImageButton)findViewById(R.id.bt_reSearch);
        ly_savePosition = (LinearLayout)findViewById(R.id.savePositionLayout);
        ly_basicButtons =  (LinearLayout)findViewById(R.id.basicButtonsLayout);

        //데이터베이스를 생성
        sqliteDB = init_database();
        init_tables();
        getTypes("http://210.115.48.131/getRestaurantType.php");//주소로 부터 가게대분류(타입)을 가져옴
        showPositionList();//gpsSearch의 기능을 만들어줌 리스트목록을 넣어 선택가능하도록
        curRtCode = 0;
        bundle.putDouble("csize",mapSize);


        vp.addOnPageChangeListener(pageChangeListener);
        bt_upGrade.setOnClickListener(upGradeListener);
        dialog = new Dialog(this);
        bt_downGrade.setOnClickListener(downGradeListener);
        bt_review.setOnClickListener(reviewBTListener);
        bt_showSPL.setOnClickListener(showSavePositionLayoutListener);
        bt_reSearch.setOnClickListener(reSearchListener);
        bt_savePosition.setOnClickListener(savePositionListener);
    }

    //뒤고가기 버튼을 제어함
    public void onBackPressed() {//
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
    /**
     * 다시검색하는 리스너
     */
    OnClickListener reSearchListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!searchedWord.isEmpty())
                getData("http://210.115.48.131/getSearchResult.php?type="+searchedWord+"&map_size="+mapSize+"&lat="+position.latitude+"&lng="+position.longitude);
        }
    };
    /**
     * 위치를 저장하는 기능의 리스너
     */
    OnClickListener savePositionListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            save_Position(positionName);
        }
    };
    /**
     * 위치를 저장하는 레이아웃을 노출시키도록 하는 리스너
     */
    OnClickListener showSavePositionLayoutListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            strt_info.setVisibility(View.GONE);
            ly_savePosition.setVisibility(View.VISIBLE);
            ly_basicButtons.setVisibility(View.GONE);
        }
    };

    /**
     * 리뷰를 등록하는 다이얼로그를 띄워주는 메소드
     */
    OnClickListener reviewBTListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            ReviewDialog reviewDialog = new ReviewDialog(MainActivity.this);//리뷰다이얼로그를 생성한다.
            float curRtReviewGrade = ratingBar.getRating();
            if (isFirstReview)
                reviewDialog.callFunction(user_id, curRtCode, curRtReviewGrade, strt_info);
            else
                reviewDialog.reviewUpdate(user_id, curRtCode, curRtReviewGrade, rvTag, rvContent, strt_info);
        }
    };

    /**
     * 점수를 올려주는 기능의 리스터
     */
    OnClickListener upGradeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ratingBar.setRating(ratingBar.getRating() + 0.5f);
        }
    };
    /**
     * 점수를 내려주는 기능의 리스터
     */
    OnClickListener downGradeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ratingBar.setRating(ratingBar.getRating() - 0.5f);
        }
    };
    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
           // Log.d("logd", "odd"+positionOffsetPixels);
        }


        @Override
        public void onPageSelected(int tag) {
            int i = 0;
            if (!isEmptyList) {//검색결과가 없을때(로딩후 처음)가 아니라면 페이지이동을 도움
                while (i < 2) {
                    if (tag == i) {
                        ll.findViewWithTag(i).setSelected(true);

                    } else {
                        ll.findViewWithTag(i).setSelected(false);
                    }
                    i++;
                }

            }
            if (tag == 0) {
                closeLayoutForMap();
            }
            else if(tag ==1){
                showBasicButtonlayout();
            }
            Log.d("logd", "태그:"+tag);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
    /**
     * 탭 클릭에 대해 화면변환
     */
    OnClickListener movePageListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int tag = (int) v.getTag();

            int i = 0;
            if (!isEmptyList) {
                while (i < 2) {
                    if (tag == i) {
                        ll.findViewWithTag(i).setSelected(true);
                    } else {
                        ll.findViewWithTag(i).setSelected(false);
                    }
                    i++;
                }

                vp.setCurrentItem(tag);

            }

        }
    };

    public void onReceivedLatLng(LatLng position) {//GPSFinderFragment에서 좌표를 선택할 때
        inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(), 0);
        if (isChunCheon(position.latitude, position.longitude)) {
            gpsSearch.setText("지정위치");
            gpsMove(position.latitude, position.longitude);
        } else
            gpsSearch.setText("조회불가지역");
    }

    private void save_Position(final TextView positionName) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final String _positionName = positionName.getText().toString();
        String sqlSelect = "SELECT * " +
                "FROM position_t " +
                "WHERE NAME = '" + _positionName + "'";
        Cursor c = sqliteDB.rawQuery(sqlSelect, null);
        if (!isChunCheon(position.latitude, position.longitude)) {//춘천이 아니면 저장불가
            alertDialogBuilder.setMessage("이 장소는 지원하지않는 장소입니다.");
            alertDialogBuilder.setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            positionName.setText(null);
                        }
                    });
        } else if (c.getCount() != 0) {//이미 존재하는 이름이면 저장불가
            alertDialogBuilder.setMessage("장소 '" + _positionName + "'는 이미 존재하는 주소명입니다.");
            alertDialogBuilder.setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            positionName.setText(null);
                        }
                    });

        } else {
            alertDialogBuilder.setMessage("장소 '" + _positionName + "'을(를) 저장하시겠습니까?");
            alertDialogBuilder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {



                            String sqlInsert = "INSERT INTO position_t " +
                                    "(NAME, LAT, LNG) VALUES ('" +
                                    _positionName + "'," +
                                    position.latitude + "," +
                                    position.longitude +
                                    ")";

                            sqliteDB.execSQL(sqlInsert);
                            Toast.makeText(MainActivity.this, "장소 '" + _positionName + "'(이)가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                            positionName.setText(null);
                            showPositionList();//장소리스트를 다시만들어 어댑터로 연결
                            inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(), 0);
                        }
                    });

            AlertDialog.Builder builder = alertDialogBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    sqliteDB.execSQL("DELETE FROM position_t") ; //위치데이터 모두 지우기
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
            Cursor cursor = sqliteDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='position_t'" , null);
            cursor.moveToFirst();
            Log.d("logd 카운트값", cursor.getCount()+"");
           if(cursor.getCount()==0){//테이블이 없으면
               String sqlCreateTbl = "CREATE TABLE position_t (" +
                       "NAME " + "TEXT," +
                       "LAT " + "REAL," +
                       "LNG " + "REAL" +
                       ")";
               sqliteDB.execSQL(sqlCreateTbl);
               positionData positiondata=new positionData();
               String[] Insertsql =  positiondata.getPositionInsertSql();
               //   "INSERT INTO position_t(NAME, LAT, LNG) VALUES ('남역', 37.872535,127.744873)";
               for(int i=0;i<Insertsql.length;i++){
                   String sql="INSERT INTO position_t(NAME, LAT, LNG) VALUES ("+Insertsql[i]+")";
                   sqliteDB.execSQL(sql);
                   Log.d("logd", i+": "+sql);
               }
            }
            //System.out.println(sqlCreateTbl);




            // sqliteDB.execSQL("DROP TABLE IF EXISTS position_t") ;//테이블 삭제코드
        }
    }


    private SQLiteDatabase init_database() {
        SQLiteDatabase db = null;
        File file = new File(getFilesDir(), "contact.db");
        // System.out.println("PATH : " + file.toString()) ;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(file, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (db == null) {
            //  System.out.println("DB creation failed. " + file.getAbsolutePath()) ;
        }
        return db;
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
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else//이전에 gps권한을 받았을때
            gpsFirstStart();
    }

    private boolean isChunCheon(double lat, double lng) {
        if ((37.68 < lat && lat < 38.05) && (127.50 < lng && lng < 128.00))
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
            if (!isEmptyList) {
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
            } else {
                switch (select) {
                    case 0: {
                        rFragment = new GPSFinderFragment();
                        rFragment.setArguments(bundle);
                        return rFragment;
                    }
                    default:
                        return null;
                }
            }
        }

        @Override
        public int getCount() {
            if (!isEmptyList) {
                return 2;
            } else
                return 1;
        }
    }

    private void gpsFirstStart()   {

        gps = new GpsInfo(MainActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {//사용가능할때&&춘천일때
            gpsMove(gps.getLatitude(),gps.getLongitude());
            gpsSearch.setText("현재 위치");
        } else {
            gpsSearch.setText("GPS 꺼짐(직접설정)");
            gpsMove(37.869071,127.742778);
            // GPS 를 사용할수 없으므로 기본위치에서
        }
    }

    protected void showPositionList(){
        Cursor cursor = sqliteDB.rawQuery("select NAME from position_t",null);
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
                        "FROM position_t " +
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
                    con.disconnect();
                    return sb.toString().trim();
                } catch (Exception e) {
                    Log.d("updatTest", "문제발생3");
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
                    searchRestaurant(tagSearch.getText().toString());
                    /*
                    String searchWord = (String) listView.getItemAtPosition(index);//결과리스트 순서에서의 포지션
                    searchRestaurant(searchWord);
                    GPSFinderFragment gpsFragment = (GPSFinderFragment)getSupportFragmentManager().findFragmentById(R.id.vp);
                    mapSize=gpsFragment.getMapSize();
                    getData("http://210.115.48.131/getSearchResult.php?type="+searchWord+"&map_size="+mapSize+"&lat="+position.latitude+"&lng="+position.longitude);
                    inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(),0);
                    */

                }
            });
            tagSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    switch (actionId) {
                        case EditorInfo.IME_ACTION_SEARCH://태그엔터(검색)치면
                            searchRestaurant(tagSearch.getText().toString());
                           /*
                            searchWord=""+tagSearch.getText();
                            GPSFinderFragment gpsFragment = (GPSFinderFragment)getSupportFragmentManager().findFragmentById(R.id.vp);
                            mapSize=gpsFragment.getMapSize();
                            getData("http://210.115.48.131/getSearchResult.php?type="+searchWord+"&map_size="+mapSize+"&lat="+position.latitude+"&lng="+position.longitude);
                            inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(), 0);
                            */
                            break;
                    }
                    return true;
                }
            });
        } catch (JSONException e) {

            e.printStackTrace();
        }

    }
    public void searchRestaurant(String searchWord){
        GPSFinderFragment gpsFragment = (GPSFinderFragment)getSupportFragmentManager().findFragmentById(R.id.vp);
        mapSize=gpsFragment.getMapSize();
        getData("http://210.115.48.131/getSearchResult.php?type="+searchWord+"&map_size="+mapSize+"&lat="+position.latitude+"&lng="+position.longitude);
        inputMethodManager.hideSoftInputFromWindow(gpsSearch.getWindowToken(), 0);
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
                    Log.d("updatTest", "getdata문제발생");
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

            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);

    }
    protected void showRestaurantList() {
        try {
            bundle.putDouble("csize",mapSize);
            if(!myJSON.isEmpty()){
                Log.d("logd", "검색어 "+tagSearch.getText().toString());
                isEmptyList=false;//검색동작으로 리스트생성됨을 알림
                JSONObject jsonObj = new JSONObject(myJSON);
                restaurants = jsonObj.getJSONArray("result");//데이터집합의 이름
                int searchResult= restaurants.length();
                bundle.putInt("size", searchResult);

                //   Log.d("logcatch", "showList: 4011line");
                for (int i = 1; i <= restaurants.length(); i++) {
                    JSONObject c = restaurants.getJSONObject(i-1);
                    //String id = c.getString(TAG_ID);
                    bundle.putInt("m"+i,c.getInt("rcode"));//맵의 마커와 검색결과의 code를 매칭
                    bundle.putString("n" + i, c.getString("rname"));
                    bundle.putDouble("lat" + i, c.getDouble("rgps_lat"));
                    bundle.putDouble("lng" + i, c.getDouble("rgps_lng"));
                    bundle.putFloat("g" + i, (float) c.getDouble("rag"));
                }
                vp.setAdapter(new searchResultPagerAdapter(getSupportFragmentManager()));
                if(!searchedWord.equals(tagSearch.getText().toString())) {
                    tab_list.callOnClick();
                    closeLayoutForMap();
                    searchedWord=tagSearch.getText().toString();
                }
                else{
                    tab_map.callOnClick();
                }
            }
            else{
                isEmptyList=true;//검색이 실패
                vp.setAdapter(new searchResultPagerAdapter(getSupportFragmentManager()));
                tab_map.callOnClick();
                bundle.putInt("size", 0);
                Toast.makeText(MainActivity.this, "정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.d("updatTest", "문제발생1");
            e.printStackTrace();
        }

    }

    /**
     * 리뷰작성(수정)
     * 선택한 가게에 대한 리뷰를 제공 만일 기존에 작성한 것이 있다면 수정할 수 있도록 한다.
     * @param url
     */

    public void isUpdateReview(String url) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.setConnectTimeout(5000);

                    StringBuilder sb = new StringBuilder();
                    InputStreamReader ist=new InputStreamReader(httpURLConnection.getInputStream());

                    bufferedReader = new BufferedReader(ist);
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
              //  Log.d("updatTest", result);
                myJSON = result;
                PrevReview();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();

            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);

    }
    protected void PrevReview() {
        try {
            isFirstReview=true;//첫 리뷰로 가정
            ratingBar.setRating(0);
            if(!myJSON.isEmpty()) {
                JSONObject jsonObj = new JSONObject(myJSON);
                JSONArray review = jsonObj.getJSONArray("result");//데이터집합의 이름

                JSONObject c = review.getJSONObject(0);
               // Log.d("updatTest", c.getDouble("rv_grade")+"");
                isFirstReview=false;//첫 리뷰가 아님
                bt_review.setBackgroundResource(R.drawable.bt_review_recurring);
                ratingBar.setRating((float) c.getDouble("rv_grade"));
                rvTag=c.getString("rv_tag");
                rvContent=c.getString("rv_content");
                Log.d("updatTest", c.getString("rv_content"));
                }
            else{//이전의 데이터가 없는경우
                bt_review.setBackgroundResource(R.drawable.bt_review);
                Log.d("updatTest", "첫리뷰");
            }


        } catch (JSONException e) {
            Log.d("updatTest", "PrevReview문제발생");
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
        strt_name.setText(bundle.getString("n"+(index+1))+" "+bundle.getFloat("g"+(index+1))+"점"+" ");
        curRtCode=bundle.getInt("m"+(index+1));
        Log.d("updatTest", "테스트시작");
        isUpdateReview("http://210.115.48.131/getIsUpdateReview.php?rcode="+curRtCode+"&user_id="+user_id);
        strt_info.setVisibility(View.VISIBLE);
        ly_savePosition.setVisibility(View.GONE);
        ly_basicButtons.setVisibility(View.GONE);
    }
    //기본버튼을 띄우는 메소드
    public void showBasicButtonlayout(){
        strt_info.setVisibility(View.GONE);
        ly_savePosition.setVisibility(View.GONE);
        ly_basicButtons.setVisibility(View.VISIBLE);
    }
    //맵에서 사용되는 레이아웃을 숨김(위치저장,가게등록,기본버튼들)
    public void closeLayoutForMap(){
        strt_info.setVisibility(View.GONE);
        ly_savePosition.setVisibility(View.GONE);
        ly_basicButtons.setVisibility(View.GONE);
    }
}


