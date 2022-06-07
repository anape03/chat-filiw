package com.example.filiw.adapters;

import com.example.filiw.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomAdapterTopics extends BaseAdapter {
    Context context;
    List<Integer> listImages;
    List<String> listTopicNames;
    List<String> listLastMessage;
    LayoutInflater inflater;

    public CustomAdapterTopics(Context applicationContext, List<Integer> images,
                               List<String> topicNames, List<String> lastMessage){
        this.context = applicationContext;
        this.listImages = images;
        this.listTopicNames = topicNames;
        this.listLastMessage = lastMessage;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return listTopicNames!=null ? listTopicNames.size() : 0;
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
        view = inflater.inflate(R.layout.list_view_item_topic, null);

        ImageView image = (ImageView)view.findViewById(R.id.show_topics_topic_icon);
        TextView topicName = (TextView)view.findViewById(R.id.show_topics_topic_title);
        TextView lastMessage = (TextView)view.findViewById(R.id.show_topics_topic_last_mes);

        image.setImageResource(listImages.get(i));
        topicName.setText(listTopicNames.get(i));
        lastMessage.setText(listLastMessage.get(i));

        return view;
    }
}
