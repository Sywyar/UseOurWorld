package com.sywyar.reuseourworld.event;

public class StringUpdate {
    StringListener stringListener;
    private String str = "";
    public void setPercentListener(StringListener stringListener){
        this.stringListener = stringListener;
    }
    public void setString(String str){
        this.str=str;
        stringListener.updateEvent(str);
    }
    public String getString(){
        return str;
    }

    public void updateString(String str){
        this.str=this.str+"<br>"+str;
        stringListener.updateEvent(this.str);
    }
}
