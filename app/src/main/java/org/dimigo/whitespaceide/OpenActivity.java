package org.dimigo.whitespaceide;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class OpenActivity extends AppCompatActivity {


    List<Map<String, String>> list;
    List<String> titles;
    ListView listView;
    DBManager dbManager;

    @Override
    protected void onStart() {
        super.onStart();
        list.clear();
        titles.clear();
        selectAll();
    }

    private void selectAll(){
        String sql = "SELECT _id, title, code"
                +" FROM "+ DBManager.TABLE_NAME
                +" ORDER BY _id DESC";

        Cursor cursor = dbManager.rawQuery(sql);
        int cnt = cursor.getCount();
        for(int i=0;i<cnt;i++){
            cursor.moveToNext();
            Map<String, String> map = new HashMap<>();
            map.put("id", cursor.getInt(0)+"");
            String title = cursor.getString(1);
            map.put("title", title);
            map.put("code", cursor.getString(2));
            list.add(map);
            titles.add(title);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);
        cursor.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        list = new ArrayList<>();
        titles = new ArrayList<>();
        listView = findViewById(R.id.listView);
        dbManager = DBManager.getInstance(this);
        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id)->{
            Intent intent = new Intent(this, CodeEditActivity.class);
            Map<String,String> what = list.get(position);
            long myid = parseInt(what.get("id"));
            intent.putExtra("id", myid);
            intent.putExtra("title", what.get("title"));
            intent.putExtra("code", what.get("code"));
            startActivity(intent);
        });

    }
}
