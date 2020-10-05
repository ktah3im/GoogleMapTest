package com.example.googlemaptest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MarkerList extends AppCompatActivity implements AdapterView.OnItemClickListener {

    static final String tb_name="test";
    static final String[] FROM=new String[] {"name", "latitude", "longitude", "info"};

    static final String db_name="testDB";
    SQLiteDatabase db;

    Cursor cur;
    SimpleCursorAdapter adapter;
    EditText edtName,edtInfo;
    Button btnUpdate, btnDelete;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_list);
        edtName=findViewById(R.id.edtName);
        edtInfo=findViewById(R.id.edtInfo);
        btnUpdate =findViewById(R.id.btnUpdate);
        btnDelete =findViewById(R.id.btnDelete);

        db = openOrCreateDatabase(db_name,  Context.MODE_PRIVATE, null);
        //https://stackoverflow.com/questions/24511031/openorcreatedatabase-undefined-in-the-fragment-class

        cur=db.rawQuery("SELECT * FROM "+tb_name, null);

        adapter=new SimpleCursorAdapter(this,
                R.layout.item, cur,
                FROM,
                new int[] {R.id.name,R.id.latitude,R.id.longitude,R.id.info}, 0);

        lv=findViewById(R.id.lv);
        lv.setAdapter(adapter);			 // 設定 Adapter
        lv.setOnItemClickListener(this); // 設定按下事件的監聽器
        requery();	// 呼叫自訂方法, 重新查詢及設定按鈕狀態

    }

    private void update(String name, String info, int id) {
        ContentValues cv=new ContentValues(2);
        cv.put(FROM[0], name);
        cv.put(FROM[3], info);

        db.update(tb_name, cv, "_id="+id, null);	// 更新 id 所指的欄位
    }

    private void requery() {	// 重新查詢的自訂方法
        cur=db.rawQuery("SELECT * FROM "+tb_name, null);
        adapter.changeCursor(cur);	//更改 Adapter的Cursor
        btnUpdate.setEnabled(false);	// 停用更新鈕
        btnDelete.setEnabled(false);	// 停用刪除鈕
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        cur.moveToPosition(position); //	移動 Cursor 至使用者選取的項目

        edtName.setText(cur.getString(
                cur.getColumnIndex(FROM[0])));
        edtInfo.setText(cur.getString(
                cur.getColumnIndex(FROM[3])));

        btnUpdate.setEnabled(true);	// 啟用更新鈕
        btnDelete.setEnabled(true);	// 啟用刪除鈕
    }

    public void onUpdate(View v){
        String nameStr=edtName.getText().toString().trim();
        String infoStr=edtInfo.getText().toString().trim();
        if(nameStr.length()==0) return;

        update(nameStr, infoStr, cur.getInt(0));

        requery();	// 更新 Cursor 內容
    }

    public void onDelete(View v){	// 刪除鈕的On Click事件方法
        db.delete(tb_name, "_id="+cur.getInt(0),null);
        requery();
    }
}