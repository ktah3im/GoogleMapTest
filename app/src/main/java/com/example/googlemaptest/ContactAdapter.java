package com.example.googlemaptest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends ArrayAdapter {

    List list = new ArrayList();


    public ContactAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public void add(Contacts object){
        super.add(object);
        list.add(object);
    }

    @Override
    public int getCount(){
        return list.size();
    }

    @Override
    public Object getItem(int position){
        return list.get(position);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row;
        row = convertView;
        ContactHolder contactHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater)this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.device_item, parent, false);
            contactHolder = new ContactHolder();
            contactHolder.name = (TextView) row.findViewById(R.id.name);
            contactHolder.dev_id = (TextView) row.findViewById(R.id.dev_id);
            contactHolder.x = (TextView) row.findViewById(R.id.x);
            contactHolder.y = (TextView) row.findViewById(R.id.y);
            contactHolder.spd = (TextView) row.findViewById(R.id.spd);

            row.setTag(contactHolder);
        }
        else{
            contactHolder = (ContactHolder) row.getTag();
        }

        Contacts contacts = (Contacts) this.getItem(position);
        contactHolder.name.setText(contacts.getName());
        String dev_id = Long.toString(contacts.getDev_id());
        contactHolder.dev_id.setText(dev_id);
        String x = Double.toString(contacts.getX());
        contactHolder.x.setText(x);
        String y = Double.toString(contacts.getY());
        contactHolder.y.setText(y);
        String spd = Integer.toString(contacts.getSpd());
        contactHolder.spd.setText(spd);

        return row;
    }

    static class ContactHolder{
        TextView name, dev_id, x, y, spd;
    }
}
