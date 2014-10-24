package edu.umass.ckc.wo.event.admin;

import ckc.servlet.servbase.ServletParams;


/**
 *
 */
public class AdminAlterClassCreateStudentsEvent extends AdminClassEvent {
    private String prefix;
    private String testUserPrefix;
    private String password;
    private int beginNum;
    private int endNum;


    public AdminAlterClassCreateStudentsEvent(ServletParams p) throws Exception {
        super(p);
        this.prefix = p.getString("prefix");
        this.testUserPrefix = p.getString("testUserPrefix");
        this.password = p.getString("password");
        this.beginNum = p.getInt("beginNumber");
        this.endNum = p.getInt("endNumber");
    }

    public String getPrefix() {
        return prefix;
    }

    public String getTestUserPrefix() {
        return testUserPrefix;
    }

    public String getPassword() {
        return password;
    }

    public int getBeginNum() {
        return beginNum;
    }

    public int getEndNum() {
        return endNum;
    }
}