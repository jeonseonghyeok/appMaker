package com.example.developer.appmaker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;


public class SignUpActivity extends AppCompatActivity {
    private LoginButton btn_kakao_login;
    SessionCallback callback;
    private long userKakaoIdCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestMe();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btn_kakao_login=(LoginButton) findViewById(R.id.btn_kakao_login);
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);

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

                    Log.d("logd", "request Success:정보요청 성공");
                    userKakaoIdCode = userProfile.getId();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.putExtra("userKakaoIdCode", (long) userKakaoIdCode);
                    startActivity(intent);
                    finish();
                }
                Log.e("logd Profile : ", userKakaoIdCode + "");
            }

            // 사용자 정보 요청 실패
            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.e("logd Session", "onFailure : " + errorResult.getErrorMessage());
            }
        });
    }
}
