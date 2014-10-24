package edu.umass.ckc.wo.beans;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 5/28/14
 * Time: 1:28 PM
 * To change this template use File | Settings | File Templates.
 */
@javax.persistence.Table(name = "teacher", schema = "", catalog = "wayangoutpostdb")
@Entity
public class TeacherEntity {
    private String email;
    private int id;
    private String fname;
    private String lname;
    private String userName;
    private String password;

    @javax.persistence.Column(name = "email")
    @Basic
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @javax.persistence.Column(name = "ID")
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @javax.persistence.Column(name = "fname")
    @Basic
    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    @javax.persistence.Column(name = "lname")
    @Basic
    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    @javax.persistence.Column(name = "userName")
    @Basic
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @javax.persistence.Column(name = "password")
    @Basic
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeacherEntity that = (TeacherEntity) o;

        if (id != that.id) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (fname != null ? !fname.equals(that.fname) : that.fname != null) return false;
        if (lname != null ? !lname.equals(that.lname) : that.lname != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = email != null ? email.hashCode() : 0;
        result = 31 * result + id;
        result = 31 * result + (fname != null ? fname.hashCode() : 0);
        result = 31 * result + (lname != null ? lname.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
