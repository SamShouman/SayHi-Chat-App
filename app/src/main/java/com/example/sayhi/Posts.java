package com.example.sayhi;

public class Posts {
   private String Caption;
   private String Image;
   private int Likes;
   private long Time;
   private String From;
   private String Name;
   private int Dislikes;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Posts() {
    }

    public int getDislikes() {
        return Dislikes;
    }

    public void setDislikes(int dislikes) {
        Dislikes = dislikes;
    }

    public Posts(String caption, String image, int likes, long time, String from, String name, int dislikes) {
        Caption = caption;
        Image = image;
        Likes = likes;
        Time = time;
        From = from;
        Name = name;
        Dislikes = dislikes;
    }

    public String getCaption() {
        return Caption;
    }

    public void setCaption(String caption) {
        Caption = caption;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public int getLikes() {
        return Likes;
    }

    public void setLikes(int likes) {
        Likes = likes;
    }

    public long getTime() {
        return Time;
    }

    public void setTime(long time) {
        Time = time;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }
}
