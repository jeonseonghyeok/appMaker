package com.example.developer.appmaker;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class SignUpActivity extends AppCompatActivity {
    private long userKakaoIdCode;
    private LoginButton btn_kakao_login;
    private LinearLayout signUpLayout;
    private Button btn_signUpConfirm;
    private EditText pt_nickname;
    SessionCallback callback;
    private String myJSON;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userKakaoIdCode=0;
        requestMe();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btn_kakao_login=(LoginButton) findViewById(R.id.btn_kakao_login);
        signUpLayout=(LinearLayout) findViewById(R.id.signUpLayout);
        btn_signUpConfirm=(Button)findViewById(R.id.bt_signUpConfirm);
        pt_nickname=(EditText)findViewById(R.id.pt_nickname);

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);

        btn_signUpConfirm.setOnClickListener(signUpConfirmListener);

    }
    private class SessionCallback implements ISessionCallback {

        // 로그인에 성공한 상태
        @Override
        public void onSessionOpened() {
            Log.d("logd", "SessionOpend:로그인 성공콜백 ");
            requestMe();
        }

        // 로그인에 실패한 상태
        @Override
        public void onSessionOpenFailed(KakaoException exception) {

            Log.d("logd", "SessionOpend:로그인 실패콜백 ");
            //getIsSignedId("http://210.115.48.131/getIsSignedId.php?k_id_code=98938641");

        }


    }
    // 사용자 정보 요청
    private void requestMe() {
        Log.d("logd", "요청!");
        // 사용자정보 요청 결과에 대한 Callback
        UserManagement.getInstance().requestMe(new MeResponseCallback() {
            // 세션 오픈 실패. 세션이 삭제된 경우,
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                btn_kakao_login.setVisibility(View.VISIBLE);
                Log.e("logd Session", "onSessionClosed : " + errorResult.getErrorMessage());
            }

            // 회원이 아닌 경우,
            @Override
            public void onNotSignedUp() {
                Log.e("logd Session", "onNotSignedUp");
            }

            // 사용자정보 요청에 성공한 경우,
            @Override
            public void onSuccess(UserProfile userProfile) {

                Log.e("logd Session", "onSuccess");
                if (userKakaoIdCode == 0) {
                    userKakaoIdCode = userProfile.getId();
                    Log.d("logd", "요청 성공 카카오아이디:"+userKakaoIdCode);
                    getIsSignedId("http://210.115.48.131/getIsSignedId.php?k_id_code="+userKakaoIdCode);
                }
            }

            // 사용자 정보 요청 실패
            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.e("logd Session", "onFailure : " + errorResult.getErrorMessage());
            }
        });
    }
    View.OnClickListener signUpConfirmListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String nickname=String.valueOf(pt_nickname.getText());
            if(nickname.length()>=2){
               if(userKakaoIdCode!=0)//혹시모를일에 대비
                getSignUp("http://210.115.48.131/getIsUsedNickname.php?k_id_code="+userKakaoIdCode+"&nickname="+nickname);
               else
                Log.d("logd", "오류 133line");
            }
            else{
                Toast.makeText(SignUpActivity.this, "2자 이상 입력해주세요", Toast.LENGTH_SHORT).show();
            }
        }
    };
    /**
     * 서버에 이미 등록된 아이디가 있는지 확인하는 메소드(등록되어있다면 닉네임을 반환)
     */

    public void getIsSignedId(String url) {

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
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray types = jsonObj.getJSONArray("result");//데이터집합의 이름 없을시 catch로 넘어감
                    moveToMain();
                } catch (JSONException e) {
                    btn_kakao_login.setVisibility(View.INVISIBLE);
                    signUpLayout.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }
            }
        }

        GetDataJSON g = new GetDataJSON();
        g.execute(url);

    }
    /**
     * 사용중인 닉네임이 아니라면 가입을 진행한다.
     */

    public void getSignUp(String url) {

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
                    Log.d("logd", "문제발생3");
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray types = jsonObj.getJSONArray("result");//데이터집합의 이름 없을시 catch로 넘어감
                    Toast.makeText(SignUpActivity.this, "이미 존재하는 닉네임입니다.", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    moveToMain();
                    e.printStackTrace();
                }
            }
        }

        GetDataJSON g = new GetDataJSON();
        g.execute(url);

    }
    private void moveToMain(){
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.putExtra("userKakaoIdCode", (long) userKakaoIdCode);
        startActivity(intent);
        finish();
    }
}
