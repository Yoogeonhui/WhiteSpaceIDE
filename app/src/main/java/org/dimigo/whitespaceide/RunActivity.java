package org.dimigo.whitespaceide;

import android.content.Intent;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.EmptyStackException;
import java.util.Stack;

public class RunActivity extends AppCompatActivity {
	String code;
	Compiler compiler = null;
	EditText stdinText;
	TextView stdoutText;

	public void onRunClickListener(View view) {
		if(compiler== null || !compiler.isAlive()){
			stdoutText.setText("");
			String stringStdIn = stdinText.getText().toString();
			compiler = new Compiler(code, stringStdIn, this);
			compiler.start();
		}else{
			Toast.makeText(this,"벌써 컴파일러가 돌아가고 있습니다.",Toast.LENGTH_LONG).show();
		}
	}

	public void onStopClickListener(View view) {
		if(compiler!=null && compiler.isAlive()){
			compiler.kill();
			compiler = null;
		}else{
			Toast.makeText(this,"컴파일러가 꺼져있습니다.",Toast.LENGTH_LONG).show();
		}
	}



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        stdinText = findViewById(R.id.stdIn);
        stdoutText = findViewById(R.id.stdOut);
        stdoutText.setMovementMethod(new ScrollingMovementMethod());
        Intent intent = getIntent();
		this.code = intent.getStringExtra("code");
		Log.d("TAG", "Code on Run: "+code);
    }





}
