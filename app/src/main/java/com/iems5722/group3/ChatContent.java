package com.iems5722.group3;

/**
 * Created by tanshen on 2016/2/23.
 */
public class ChatContent {
    public String timestamp;
    public String message;
    public String name;
    public String id;

    public ChatContent(String message,String name, String timestamp,String id){
        this.timestamp = timestamp;
        this.message = message;
        this.name = name;
        this.id=id;
    }
    public ChatContent(){}

    public String getMessage(){
        return message;
    }

    public void setMessage(String text){
        this.message=text;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(String timestamp){
        this.timestamp=timestamp;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name=name;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id=id;
    }

}