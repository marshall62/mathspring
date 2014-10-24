package edu.umass.ckc.wo.content;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/23/13
 * Time: 9:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class CCStandard implements Comparable<CCStandard> {
    private String id;
    private String code;
    private String description;
    private String category;
    private String idABC;
    private String clusName;
    private String grade; // K-8, H
    private CCCluster cluster;  // pointer to cluster that contains this standard
    private int clustId;
    private String clustName;
    private List<Problem> problems; // problems in this standard


    public CCStandard () {
        problems = new ArrayList<Problem>();
    }

    public CCStandard(String code, String description, String category) {
        this();
        this.code = code;
        this.description = description;
        this.category = category;

    }

    public CCStandard(String id, String descr, String grade, String cat, String clustName, int clustId,
                      String idABC, String adminFlag, String authFlag, CCCluster cluster) {
        this();
        this.id=id;
        this.description=descr;
        this.grade=grade;
        this.category=cat;
        this.idABC=idABC;
        this.clustId=clustId;
        this.clustName=clustName;
        this.cluster=cluster;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getClustId() {
        return clustId;
    }

    public void setClustId(int clustId) {
        this.clustId = clustId;
    }

    public CCCluster getCluster() {
        return cluster;
    }

    public void setCluster(CCCluster cluster) {
        this.cluster = cluster;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems;
    }

    public void addProblem(Problem p) {
        this.problems.add(p);
    }

    // needed because these are put in Sets and duplicates are checked.
    public boolean equals (CCStandard std) {
        return std.getCode().equals(this.getCode());
    }

    @Override
    public int compareTo(CCStandard o) {
        return this.code.compareTo(o.getCode());
    }


}
