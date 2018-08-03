package com.example.developer.appmaker;

import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;


public class FirstFragment extends Fragment {
    public FirstFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*  View view = inflater.inflate(R.layout.fragment_first, null) ;
        final String[] LIST_MENU = {"LIST1", "LIST2", "LaIST3"} ;

        ArrayAdapter Adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1,LIST_MENU) ;

        ListView listview = (ListView) view.findViewById(R.id.ListView) ;
        listview.setAdapter(Adapter) ;
*/

        //RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_first, container, false);//기존 리턴레이아웃 현재 view가 대신하고있음
        ListView listview;
        ListViewAdapter adapter;
        // Adapter 생성
        adapter = new ListViewAdapter();
        View view = inflater.inflate(R.layout.fragment_first, null);//레이아웃을 view로 변환하고 view를 이용하여 findVIewByld를 호출
        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) view.findViewById(R.id.ListView);
        listview.setAdapter(adapter);

        Bundle extra = getArguments();//액티비티에서 전송한 데이터를 받아오는 객체
        int size = extra.getInt("size");//검색된 수 만큼 리스트를 생성//너무많을시에 어떻게할지 구상할것
        for (int i = 0; i < size; i++) {
            adapter.addItem(null, extra.getString("n"+i) , extra.getFloat("g"+i)+ "점");
        }

        return view;

    }
}
