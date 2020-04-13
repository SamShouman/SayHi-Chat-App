package com.example.sayhi;

public class Comments {

    private String Comment;
    private String From;
    private long Time;

    public Comments() {
    }

    public Comments(String comment, String from, long time) {
        Comment = comment;
        From = from;
        Time = time;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public long getTime() {
        return Time;
    }

    public void setTime(long time) {
        Time = time;
    }




}
