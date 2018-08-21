package org.dimigo.whitespaceide;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Vector;

public class CodeEditActivity extends AppCompatActivity {

    TextView tvTitle;
    EditText codeEdit;
	DialogInterface.OnClickListener dialogClickListener;
    long codeID;
	int spaceColor1, spaceColor2, tabColor1, tabColor2;
    boolean printedFlag = false, colorFlag = false;
    DBManager dbManager;

    private void setIndividualSpan(SpannableString s, Vector<Integer> indexes, int color1, int color2){
		int cnt = 0, nowColor;
		for (int index : indexes) {
			if(cnt%2==0) nowColor = color1;
				else nowColor = color2;
			s.setSpan(new BackgroundColorSpan(nowColor), index, index+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			cnt++;
		}
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_edit);
        codeEdit= findViewById(R.id.codeEdit);
        tvTitle = findViewById(R.id.title);
        dbManager = DBManager.getInstance(this);
        Intent intent = getIntent();
        String code = intent.getStringExtra("code");
        String title = intent.getStringExtra("title");
        codeID = intent.getLongExtra("id",-1);
        if(codeID==-1)
            finish();
        spaceColor1 = Color.parseColor("#FF9999");
        spaceColor2 = Color.parseColor("#E880B4");
        tabColor1 = Color.parseColor("#9999FF");
        tabColor2 = Color.parseColor("#809BE8");
        tvTitle.setText(title);
		codeEdit.setText(code);

		codeEdit.addTextChangedListener(
				new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {

					}

					@Override
					public void afterTextChanged(Editable s) {
						if(colorFlag){
							colorFlag = false;
							s.clearSpans();
						}
					}
				}
		);

		dialogClickListener = (dialog, which) -> {
			switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					String sql = "DELETE FROM "+ DBManager.TABLE_NAME+" WHERE _id='"+codeID+"'";
					if(dbManager.execSQL(sql))
						finish();
					else
						Toast.makeText(this, "삭제 실패", Toast.LENGTH_LONG).show();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					break;
			}
		};
    }
    public void STButton(View view) {
		String origin = codeEdit.getText().toString();
		origin = origin.replaceAll(" ", "S");
		origin = origin.replaceAll("\t", "T");
		origin = origin.replaceAll("\n", "L");
		codeEdit.setText(origin);
    }

    public void onColorButton(View view) {
		String origin = codeEdit.getText().toString();
		origin = origin.replaceAll("s", " ");
		origin = origin.replaceAll("S", " ");

		origin = origin.replaceAll("t", "\t");
		origin = origin.replaceAll("T", "\t");


		origin = origin.replaceAll("L", "\n");
		origin = origin.replaceAll("l", "\n");
		SpannableString s = new SpannableString(origin);
		String what = s.toString();

		if(what.length()>100000 && !printedFlag){
			Toast.makeText(getApplicationContext(), "너무 코드가 깁니다 컬러링이 불가합니다.", Toast.LENGTH_LONG).show();
			printedFlag = true;
			return;
		}

		printedFlag = false;
		Log.d("TAG", "editText: "+what);
		Vector<Integer> spaceIndexes = new Vector<>();
		Vector<Integer> tabIndexes = new Vector<>();
		char finder[] = what.toCharArray();
		for(int i=0;i<what.length();i++){
			if(finder[i]==' ' || finder[i] =='s' || finder[i]=='S'){
				spaceIndexes.add(i);
			}
			if(finder[i]=='\t' || finder[i]=='t'||finder[i]=='T'){
				tabIndexes.add(i);
			}
		}
		setIndividualSpan(s, spaceIndexes, spaceColor1, spaceColor2);
		setIndividualSpan(s, tabIndexes, tabColor1, tabColor2);
		codeEdit.setText(s);
		colorFlag = true;
    }

    public void onDropButton(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("정말 삭제할건가요?").setPositiveButton("예", dialogClickListener)
				.setNegativeButton("아니요", dialogClickListener).show();
    }

    public void onRun(View view) {
    	String code = codeEdit.getText().toString();
		Intent intent = new Intent(this, RunActivity.class);
		intent.putExtra("code", code);
		startActivity(intent);
    }

    public void onSaveButton(View view) {
    	String saveCode = codeEdit.getText().toString();
		String sql = "UPDATE "+DBManager.TABLE_NAME+" SET code='"+saveCode+"' WHERE _id = '"+codeID+"'";
		if(dbManager.execSQL(sql)){

			Toast.makeText(this, "성공적으로 저장하였습니다.", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(this, "업데이트 실패", Toast.LENGTH_LONG).show();
		}
    }
}
