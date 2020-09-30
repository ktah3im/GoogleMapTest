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
    private EditText edtName;
    private EditText edtInfo;
    private ExampleDialogListener listener;

    Double latitude;
    Double longitude;

    static final String db_name="testDB";
    static final String tb_name="test";
    SQLiteDatabase db;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);
        edtName = view.findViewById(R.id.edtName);
        edtInfo = view.findViewById(R.id.edtInfo);

        Bundle bundle = getArguments();
        latitude = bundle.getDouble("latitude");
        longitude= bundle.getDouble("longitude");;

        db = getActivity().openOrCreateDatabase(db_name,  Context.MODE_PRIVATE, null);

        builder.setView(view)
                .setTitle("新增地標")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = edtName.getText().toString();
                        String info = edtInfo.getText().toString();
                        listener.applyTexts(name, info);

                        ContentValues cv = new ContentValues(4);
                        cv.put("name", name);
                        cv.put("latitude", latitude);
                        cv.put("longitude", longitude);
                        cv.put("info", info);
                        db.insert(tb_name, null, cv);

                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
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
