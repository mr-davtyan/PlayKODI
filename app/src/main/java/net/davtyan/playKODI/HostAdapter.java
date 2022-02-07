package net.davtyan.playKODI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

        TextView titleText = (TextView) rowView.findViewById(R.id.name);
        Button colorButton = (Button) rowView.findViewById(R.id.icon);
        TextView subtitleText = (TextView) rowView.findViewById(R.id.host);

        titleText.setText(nickName[position]);
        subtitleText.setText(host[position]);
        try {
            colorButton.setBackgroundColor(Integer.decode(colorCode[position]));
        } catch (NumberFormatException e) {
            colorButton.setBackgroundColor(-11889757);
        }

        return rowView;

    }
}