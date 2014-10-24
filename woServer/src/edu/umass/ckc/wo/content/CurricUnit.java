package edu.umass.ckc.wo.content;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 8/1/14
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class CurricUnit {
    public enum type {cluster, standard, problems}
    private int id;
    private type t;
    private String stdId;
    private int clustId;
    private int lessonId;
    private int position;  // its position in the lesson (which implies that a curric unit exists only for one lesson)
    private List<Integer> probIds;

    private CCCluster cluster;
    private CCStandard standard;
    private List<Problem> problems;

    public CurricUnit(int id, String typ, int lessonId, int position) {
        this.id=id;
        this.t=type.problems;
        this.lessonId=lessonId;
        this.position=position;
        this.probIds = new ArrayList<Integer>();
    }

    public CurricUnit(int id, String typ, String stdId, int lessonId, int position) {
        this.id=id;
        this.t=type.standard;
        this.stdId=stdId;
        this.lessonId=lessonId;
        this.position=position;
    }

    public CurricUnit(int id, String typ, int clustId, int lessonId, int position) {
        this.id=id;
        this.t=type.cluster;
        this.clustId=clustId;
        this.lessonId=lessonId;
        this.position=position;
    }

    public void setCluster(CCCluster cluster) {
        this.cluster = cluster;
    }

    public void setStandard(CCStandard standard) {
        this.standard = standard;
    }

    public void setProbIds (List<Integer> probIds) {
        this.probIds = probIds;
    }

    public List<Integer> getProbIds () {
        return this.probIds;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems;
    }

    public int getId() {
        return id;
    }

    public int getLessonId() {
        return lessonId;
    }

    public CCCluster getCluster() {
        return cluster;
    }

    public CCStandard getStandard() {
        return standard;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public int getPosition() {
        return position;
    }

    public String getStdId() {
        return stdId;
    }

    public int getClustId() {
        return clustId;
    }
}
