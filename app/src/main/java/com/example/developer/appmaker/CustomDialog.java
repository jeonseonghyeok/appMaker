package com.example.developer.appmaker;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by Administrator on 2017-08-07.
 */
public class CustomDialog {
    private Context context;
    public CustomDialog(Context context){
        this.context = context;
    }
    // 호출할 다이얼로그 함수를 정의한다.
    public void reviewInsert(final String user_id,final int curRtCode, final float curRtReviewGrade,final LinearLayout strt_info) {//가게코드,가게평가점수,가게리뷰버튼를 받아옴
        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //요청이 이 다이어로그를 종료할 수 없게 지정
        dlg.setCancelable(false);
        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.dialog_review);

        // 커스텀 다이얼로그를 노출한다.
        dlg.show();

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final EditText tvTag = (EditText) dlg.findViewById(R.id.reviewTag);
        final EditText tvContent = (EditText) dlg.findViewById(R.id.reviewContent);
        final Button okButton = (Button) dlg.findViewById(R.id.bt_reviewConfirm);//확인버튼
        final Button cancelButton = (Button) dlg.findViewById(R.id.bt_reviewCancel);//취소버튼

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //strt_info.setVisibility(View.GONE);//가게정보를 내린다...?
                // 커스텀 다이얼로그를 종료한다.
                String rcode = curRtCode+"";
                String tag = String.valueOf(tvTag.getText());
                // Log.d("logwhat", String.valueOf(tvTag.getText()));
                String content = String.valueOf(tvContent.getText());
                if(tag.length()<2) {
                    Toast.makeText(context, "메뉴명을 입력해주세요.(2자 이상)", Toast.LENGTH_SHORT).show();
                }
                else if(content.length()<3){
                    Toast.makeText(context, "리뷰를 입력해주세요.(3자 이상)", Toast.LENGTH_SHORT).show();
                }
                else {
                    String userID = user_id;
                    String grade = curRtReviewGrade + "";
                    WriteReview task = new WriteReview();
                    // InsertData task = new InsertData();
                    task.execute("http://" + "210.115.48.131" + "/postFirstReview.php", rcode, tag, content, grade, userID);
                    Toast.makeText(context, "리뷰를 등록하였습니다.", Toast.LENGTH_SHORT).show();
                    strt_info.setVisibility(View.GONE);
                    dlg.dismiss();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "취소 했습니다.", Toast.LENGTH_SHORT).show();
                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });
    }
    public void reviewUpdate(final String user_id, final int curRtCode, final double curRtReviewGrade,final String rvTag,final String rvContent,final LinearLayout strt_info) {//가게코드,가게평가점수,가게리뷰버튼(수정필요)를 받아옴
        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.dialog_review);

        // 커스텀 다이얼로그를 노출한다.
        dlg.show();

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final EditText tvTag = (EditText) dlg.findViewById(R.id.reviewTag);
        tvTag.setText(rvTag);
        final EditText tvContent = (EditText) dlg.findViewById(R.id.reviewContent);
        tvContent.setText(rvContent);
        final Button okButton = (Button) dlg.findViewById(R.id.bt_reviewConfirm);//수정버튼
        okButton.setText("수정");
        final Button cancelButton = (Button) dlg.findViewById(R.id.bt_reviewCancel);//취소버튼

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //strt_info.setVisibility(View.GONE);//가게정보를 내린다...?
                // 커스텀 다이얼로그를 종료한다.
                String rcode = curRtCode+"";
                String tag = String.valueOf(tvTag.getText());
                // Log.d("logwhat", String.valueOf(tvTag.getText()));
                String content = String.valueOf(tvContent.getText());
                if(tag.length()<2) {
                    Toast.makeText(context, "메뉴명을 입력해주세요.(2자 이상)", Toast.LENGTH_SHORT).show();
                }
                else if(content.length()<3){
                    Toast.makeText(context, "리뷰를 입력해주세요.(3자 이상)", Toast.LENGTH_SHORT).show();
                }
                else {
                    String userID = user_id;
                    String grade = curRtReviewGrade + "";
                    WriteReview task = new WriteReview();
                    // InsertData task = new InsertData();
                    task.execute("http://" + "210.115.48.131" + "/postUpdateReview.php", rcode, tag, content, grade, userID);
                    //bt_review.setBackgroundResource(R.drawable.bt_review_recurring);
                    Toast.makeText(context, "리뷰를 수정하였습니다.", Toast.LENGTH_SHORT).show();
                    strt_info.setVisibility(View.GONE);
                    dlg.dismiss();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "취소 했습니다.", Toast.LENGTH_SHORT).show();
                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });
    }
    public void signUp(final long userKakaoIdCode) {//가게코드,가게평가점수,가게리뷰버튼를 받아옴
        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //요청이 이 다이어로그를 종료할 수 없게 지정
        dlg.setCancelable(false);
        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.dialog_sign_up);

        // 커스텀 다이얼로그를 노출한다.
        dlg.show();

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final Button okButton = (Button) dlg.findViewById(R.id.bt_reviewConfirm);//확인버튼

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //String content = String.valueOf(tvContent.getText());

            }
        });
    }
}
