package net.davtyan.playKODI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class HostAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] nickName;
    private final String[] host;
    private final String[] colorCode;

    public HostAdapter(Activity context, String[] nickName, String[] host, String[] colorCode) {
        super(context, R.layout.host_list_item, nickName);
        this.context = context;
        this.nickName = nickName;
        this.host = host;
        this.colorCode = colorCode;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.host_list_item, parent, false);

        TextView titleText = rowView.findViewById(R.id.name);
        ImageView icon = rowView.findViewById(R.id.icon);
        TextView subtitleText = rowView.findViewById(R.id.host);

        if (nickName[position].equalsIgnoreCase("")){
            titleText.setText(host[position]);
            subtitleText.setVisibility(View.GONE);
        }else{
            titleText.setText(nickName[position]);
            subtitleText.setText(host[position]);
        }

        try {
            icon.setColorFilter(Integer.decode(colorCode[position]));
        } catch (NumberFormatException e) {
            icon.setColorFilter(-11889757);
        }

        return rowView;

    }
}