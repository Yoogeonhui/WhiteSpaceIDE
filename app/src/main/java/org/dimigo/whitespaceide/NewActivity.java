package org.dimigo.whitespaceide;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NewActivity extends AppCompatActivity {

    DBManager dbManager;
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        dbManager =  DBManager.getInstance(this);
        editText = findViewById(R.id.editText);
    }


    public void onButtonClicked(View view) {
        String title = editText.getText().toString();
        if(title.equals("")){
            Toast.makeText(this, "이름이 비어있습니다.",Toast.LENGTH_LONG).show();
        }else{
            ContentValues contentValues = new ContentValues();
            contentValues.put("title", title);
            contentValues.put("code", "");
            long result = dbManager.insertSQL(contentValues);
            if(result!=-1){
                Intent intent = new Intent(this, CodeEditActivity.class);
                intent.putExtra("id", result);
                intent.putExtra("title", title);
                intent.putExtra("code", "");
                startActivity(intent);
            }else{
                Toast.makeText(this, "INSERT 오류", Toast.LENGTH_LONG).show();
            }
        }
    }
}
