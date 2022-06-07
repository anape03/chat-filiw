package com.example.filiw.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.filiw.R;

import java.util.List;

public class CustomAdapterChat extends BaseAdapter {
    Context context;
    List<String> listSenderNames;
    List<String> listMessages;
    LayoutInflater inflater;

    public CustomAdapterChat(Context applicationContext, List<String> senderNames, List<String> messages){
        this.context = applicationContext;
        this.listSenderNames = senderNames;
        this.listMessages = messages;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return listMessages!=null ? listMessages.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.list_view_item_message, null);

        TextView senderName = (TextView)view.findViewById(R.id.show_chat_sender);
        TextView message = (TextView)view.findViewById(R.id.show_chat_message_sent);

        senderName.setText(listSenderNames.get(i));
        message.setText(listMessages.get(i));

        return view;
    }
}
