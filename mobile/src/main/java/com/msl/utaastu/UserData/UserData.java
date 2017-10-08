package com.msl.utaastu.UserData;

import android.support.annotation.Keep;

import com.msl.utaastu.Application.MyApplication;

/**
 * Created by Malek Shefat on 8/3/2017.
 */

@Keep
public class UserData {

    private String Name = "", Birthday = "", Phone = "", Email = "", Photo = "", Department = "";

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public String getDepartment() {
        return Department;
    }

    public void setDepartment(String department) {
        Department = department;
        MyApplication.storeDepartment(department);
    }

    public String getBirthday() {
        return Birthday;
    }

    public void setBirthday(String birthday) {
        Birthday = birthday;
    }
}

