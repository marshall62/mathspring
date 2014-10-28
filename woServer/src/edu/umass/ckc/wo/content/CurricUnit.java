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
    private int probId;
//    private List<Integer> probIds;

    private CCCluster cluster;
    private CCStandard standard;
//    private List<Problem> problems;
    private Problem problem;



    public CurricUnit (int id,String stdId, int clustId, int probId, int lessonId, int position) {
        this.id=id;
        this.t=type.standard;
        this.clustId = clustId;
        this.stdId=stdId;
        this.probId = probId;
        this.lessonId=lessonId;
        this.position=position;
    }



    public void setCluster(CCCluster cluster) {
        this.cluster = cluster;
    }

    public void setStandard(CCStandard standard) {
        this.standard = standard;
    }

    public int getProbId() {
        return probId;
    }

    public void setProbId(int probId) {
        this.probId = probId;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
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
