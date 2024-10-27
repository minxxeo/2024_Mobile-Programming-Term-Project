package org.techtown.healtea;

public class MyUser {
    private String userId;
    private String name;
    private String birth;
    private String phone;

    public MyUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public MyUser(String userId, String name, String birth, String phone) {
        this.userId = userId;
        this.name = name;
        this.birth = birth;
        this.phone = phone;
    }

    // 각 필드에 대한 getter 및 setter 메서드

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}



