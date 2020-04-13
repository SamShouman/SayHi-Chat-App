package com.example.sayhi;

public class Conversations {

    private long TimeStamp;
    private String Seen;

    public Conversations() {
    }

    public Conversations(long timeStamp, String seen) {
        TimeStamp = timeStamp;
        Seen = seen;
    }

    public long getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        TimeStamp = timeStamp;
    }

    public String getSeen() {
        return Seen;
    }

    public void setSeen(String seen) {
        Seen = seen;
    }
}
