package hk.edu.cuhk.ie.iems5722.a2_1155066083;

/**
 * Created by tanshen on 2016/2/23.
 */
public class ChatContent {
    public String timestamp;
    public String message;
    public String name;
    public String id;

    public void setMessage(String text){
        this.message=text;
    }
    public String getMessage(){
        return message;
    }
    public void setTimestamp(String timestamp){
        this.timestamp=timestamp;
    }
    public String getTimestamp(){
        return timestamp;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
    public void setId(String id){
        this.id=id;
    }
    public String getId(){
        return id;
    }
    public ChatContent(String message,String name, String timestamp,String id){
        this.timestamp = timestamp;
        this.message = message;
        this.name = name;
        this.id=id;
    }
    public ChatContent(){}

}