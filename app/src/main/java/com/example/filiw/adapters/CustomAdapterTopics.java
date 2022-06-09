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
    List<ItemTopic> listTopics;
    LayoutInflater inflater;

    public CustomAdapterTopics(Context applicationContext, List<ItemTopic> topics){
        this.context = applicationContext;
        this.listTopics = topics;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return listTopics!=null ? listTopics.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return listTopics.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // inflate the layout for each list row
        if (view == null) {
            view = inflater.inflate(R.layout.list_view_item_topic, viewGroup, false);
        }

        TextView topicName = (TextView)view.findViewById(R.id.item_show_topics_topic_title);
        TextView lastMessage = (TextView)view.findViewById(R.id.item_show_topics_topic_last_mes);

        ItemTopic currentItem = (ItemTopic) getItem(i);

        topicName.setText(currentItem.getTopicName());
        lastMessage.setText(currentItem.getLastMessage());

        return view;
    }
}
