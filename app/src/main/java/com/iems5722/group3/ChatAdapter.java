package com.iems5722.group3;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import com.iems5722.group3.R;

/**
 * Created by tanshen on 2016/2/23.
 */
public class ChatAdapter  extends ArrayAdapter<ChatContent> {
    public ChatAdapter(Context context,ArrayList<ChatContent> chatcontent){
        super(context, 0, chatcontent);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ChatContent chatcontent = getItem(position);
        ViewHolder viewholder = null;

        String usrId = chatcontent.id;
        Log.e("view",usrId);
        if (usrId.equals("1155066083")){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_chat_to, parent, false);
        }
        else{
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_chat_from, parent, false);
        }
        viewholder = new ViewHolder();
        LayoutInflater inflater =LayoutInflater.from(getContext());
        //convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_chat_to, parent, false);
        viewholder.name = (TextView) convertView.findViewById(R.id.name);
        viewholder.message = (TextView) convertView.findViewById(R.id.message);
        viewholder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
        convertView.setTag(viewholder);

        viewholder.name.setText("User: " + chatcontent.name);
        viewholder.message.setText(chatcontent.message);
        viewholder.timestamp.setText(chatcontent.timestamp);

        return convertView;
    }

    private static class ViewHolder {
        TextView message;
        TextView timestamp;
        TextView name;
    }
}
