package com.example.googlemaptest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class ExampleDialog extends AppCompatDialogFragment {
    private EditText editTextName;
    private EditText editTextInfo;
    private ExampleDialogListener listener;

    static final String db_name="testDB";
    static final String tb_name="test";
    SQLiteDatabase db;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        editTextName = view.findViewById(R.id.editTextName);
        editTextInfo = view.findViewById(R.id.editTextInfo);

        db = getActivity().openOrCreateDatabase(db_name,  Context.MODE_PRIVATE, null);
        //https://stackoverflow.com/questions/24511031/openorcreatedatabase-undefined-in-the-fragment-class

        String createTable="CREATE TABLE IF NOT EXISTS " +
                tb_name +			// 資料表名稱
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + //索引欄位
                "name VARCHAR(32), " +	//名稱欄位
                //"coordinate VARCHAR(32), " +	//座標欄位
                "info VARCHAR(64))";	//資訊欄位
        db.execSQL(createTable);	// 建立資料表

        builder.setView(view)
                .setTitle("新增地標")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editTextName.getText().toString();
                        String info = editTextInfo.getText().toString();
                        listener.applyTexts(name, info);
                        ContentValues cv = new ContentValues(2); //3>2
                        cv.put("name", name);
                        //cv.put("coordinate", );
                        cv.put("info", info);

                        db.insert(tb_name, null, cv);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        //db.close();
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ExampleDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+"must implement ExampleDialogListener");
        }
    }

    public interface ExampleDialogListener{
        void applyTexts(String name, String info);
    }
}
