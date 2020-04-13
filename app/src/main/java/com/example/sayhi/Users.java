package com.example.sayhi;

public class Users {
    //have to be same as keys in the firebase and private
    private String Image;
    private String Name;
    private String Status;




    public Users() {
    }

    public Users(String image, String name, String status) {
        Image = image;
        Name = name;
        Status = status;

    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
