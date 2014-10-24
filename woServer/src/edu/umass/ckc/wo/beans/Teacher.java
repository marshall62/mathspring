package edu.umass.ckc.wo.beans;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.io.Serializable;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 5/28/14
 * Time: 10:07 AM
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "teacher", catalog = "wayangoutpostdb", uniqueConstraints = {
        @UniqueConstraint(columnNames = "ID"),
        @UniqueConstraint(columnNames = "userName") })
public class Teacher implements Serializable {
    private String email;
    private int id;
    private String fname;
    private String lname;
    private String userName;
    private String password;

    public Teacher() {
    }

    public Teacher(String email, int id, String fname, String lname, String userName, String password) {
        this.email = email;
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.userName = userName;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "fname", unique = true, nullable = false, length = 50)
    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    @Column(name = "lname", unique = true, nullable = false, length = 50)
    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    @Column(name = "userName", unique = true, nullable = false, length = 50)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "password", unique = true, nullable = false, length = 30)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
