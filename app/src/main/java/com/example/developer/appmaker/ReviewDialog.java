package com.example.developer.appmaker;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Administrator on 2017-08-07.
 */
public class ReviewDialog{
    private Context context;
    public ReviewDialog(Context context){
        this.context = context;
    }
    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction(final LinearLayout strt_info) {

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
        final EditText tvContent = (EditText) dlg.findViewById(R.id.reviewContent);
        final Button okButton = (Button) dlg.findViewById(R.id.bt_reviewConfirm);//확인버튼
        final Button cancelButton = (Button) dlg.findViewById(R.id.bt_reviewCancel);//취소버튼

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //strt_info.setVisibility(View.GONE);//가게정보를 내린다...?
                // 커스텀 다이얼로그를 종료한다.
                String rcode = "1000";
                String tag = String.valueOf(tvTag.getText());
               // Log.d("logwhat", String.valueOf(tvTag.getText()));
                String content = String.valueOf(tvContent.getText());
                String userID="qoq10";
                float grade=4.5f;
               WriteReview task = new WriteReview();
                // InsertData task = new InsertData();
                task.execute("http://" + "210.115.48.131" + "/postTest2.php", rcode , tag , content ,grade+"", userID);
                Toast.makeText(context,  "리뷰를 등록하였습니다.", Toast.LENGTH_SHORT).show();


                dlg.dismiss();
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
}
