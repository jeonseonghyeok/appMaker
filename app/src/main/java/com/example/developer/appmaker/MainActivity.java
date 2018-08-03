package com.example.developer.appmaker;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    ViewPager vp;
    Button viewChangeButton;
    //LinearLayout ll;
    Bundle bundle;
    EditText tag;
    String myJSON;

    private static final String TAG_RESULTS = "result";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_ADD = "address";

    JSONArray peoples = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tag = (EditText) findViewById(R.id.tagSearch);
        vp = (ViewPager) findViewById(R.id.vp);//프래그먼트 보는 화면
        viewChangeButton=(Button) findViewById(R.id.viewChangeButton);
        vp.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        //  Log.d(this.getClass().getName(),"여기");
        vp.setCurrentItem(0);
        viewChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vp.getCurrentItem()==0) {//현재 화면이 리스트이면
                    vp.setCurrentItem(1);//지도를 뛰움
                    viewChangeButton.setText("리스트보기");
                }
                else {//아니라면
                    vp.setCurrentItem(0);//리스트를 뛰움
                    viewChangeButton.setText("지도보기");
                }
            }
        });
        tag.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH://엔터(검색)치면
                         search(tag.getText().toString());
                      //  getData("http://210.115.48.131/getjson.php");
                        break;
                }
                return true;
            }
        });
       // tab_first.setOnClickListener(movePageListener);
       // tab_first.setTag(0);
       // tab_second.setOnClickListener(movePageListener);
        //tab_second.setTag(1);

      //  tab_first.setSelected(true);
        bundle = new Bundle();//액티비티에서 프래그먼트로 데이터전달을 위한 객체
    }

   /* View.OnClickListener movePageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = 0;
            while (i < 2) {
                if (tag == i) {
                    ll.findViewWithTag(i).setSelected(true);
                } else {
                    ll.findViewWithTag(i).setSelected(false);
                }
                i++;
            }
            vp.setCurrentItem(i);
        }
    };*/

    private class pagerAdapter extends FragmentStatePagerAdapter {
        public pagerAdapter(android.support.v4.app.FragmentManager fm) {
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

    private void search(String tag) {

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
        vp.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        viewChangeButton.setText("지도보기");
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
            vp.setAdapter(new pagerAdapter(getSupportFragmentManager()));

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

}


