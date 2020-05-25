package project.baonq.dto;

import java.util.Date;

import project.baonq.model.User;

public class UserDto {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Date birthday;
    private Date insertDate = new Date();
    private Date lastUpdate = new Date();

    public User user() {
        User rs = new User();
        rs.setUsername(username);
        rs.setFirstName(firstName);
        rs.setLastName(lastName);
        rs.setBirthday(birthday);
        rs.setInsertDate(insertDate);
        rs.setLastUpdate(lastUpdate);
        return rs;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Date insertDate) {
        this.insertDate = insertDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
