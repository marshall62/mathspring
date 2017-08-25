package edu.umass.ckc.wo.ttmain.ttservice.util;

import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.db.DbTopics;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Neeraj on 4/5/2017.
 */
public class TTUtil {
    private static TTUtil util = new TTUtil();

    /**
     * SQL Queries to Reorder Problem Sets as well as deactivate problem sets. The Passed problemset ID's are first removed and then inserted back as per their new Sequence position. For deactivate, I am clearing the plan and inserting only the ones that are left unchecked
     */
    public static final String DELETE_CLASS_PLAN = "DELETE FROM classlessonplan where seqPos IN (:seqPos) and classId=(:classId)";
    public static final String INSERT_ON_CLASS_PLAN = "INSERT INTO classlessonplan (classId, seqPos, probGroupId, isDefault) values (:classId, :seqPos, :probGroupId, :isDefault)";
    public static final String ACTIVATE_ON_CLASS_PLAN = "SELECT MAX(seqPos)+1 from classLessonPlan where classId=?";
    public static final String REMOVE_FROM_CLASSOMITTED_PROBLEMS =  "DELETE FROM ClassOmittedProblems where classId=(:classId) and topicId=(:problemSetId)";
    public static final String INSERT_ON_CLASSOMITTED_PROBLEMS = "INSERT INTO ClassOmittedProblems (classId,topicId,probId) values (:classId, :topicId, :probId)";
    public static final String UPDATE_PASSWORD_FOR_STUDENT = "UPDATE STUDENT SET password=(:resetPassword) where id=(:studentId)";
    public static final String UPDATE_STUDENT_INFO = "UPDATE STUDENT SET fname=(:fname),lname=(:lname),userName=(:uname) where id=(:studentId)";
    public static final String PASSWORD_TOKEN = "M8$tek@12";

    /** SQL Queries For Reports **/
    public static final String PER_STUDENT_QUERY_FIRST ="Select studId AS studentId,concat(s.fname,' ',s.lname) As studentName, s.userName As userName,count(problemId) AS noOfProblems  from student s,studentproblemhistory sh where s.id=sh.studId and s.classId=(:classId) and sh.mode != 'demo' GROUP BY studId order by studId ; ";
    public static final String PER_STUDENT_QUERY_SECOND ="select sh.id,sh.problemId, pg.description,sh.problemEndTime,pr.name,pr.nickname, pr.statementHTML,pr.screenShotURL,sh.isSolved,sh.numMistakes,sh.numHints,sh.numAttemptsToSolve,sh.effort from studentproblemhistory sh, problem pr, problemgroup pg where sh.studId in ( select id from student where classId=(:classId)) and sh.studId=(:studId) and sh.mode != 'demo'and sh.problemId = pr.id and sh.topicId=pg.id order by sh.problemEndTime desc;";
    public static final String PER_TOPIC_QUERY_FIRST = "select studId AS studentId,concat(s.fname,' ',s.lname) As studentName, s.userName As userName,sh.topicId,pg.description,CAST(MAX(sh.mastery) AS DECIMAL(16,2)) AS mastery from student s,studentproblemhistory sh ,problemgroup pg where s.id=sh.studId and s.classId=(:classId) and  sh.topicId = pg.id  and sh.mode != 'demo' group by sh.topicId,studId order by studId";
    public static final String PER_TOPIC_QUERY_SECOND = "select sh.topicId,sh.problemId,CAST(sh.mastery AS DECIMAL(16,2)) AS mastery,sh.effort,sh.problemEndTime,pr.name,pr.nickname, pr.statementHTML,pr.screenShotURL from studentproblemhistory sh,problem pr where sh.studId in ( select id from student where classId=(:classId)) and sh.studId=(:studId) and sh.topicId=(:topicId) and sh.mode != 'demo' and sh.problemId = pr.id order by sh.topicId,problemEndTime asc";

    public static final String PER_TOPIC_QUERY_COMPLETE_MAX = "select sh.topicId,CAST(MAX(sh.mastery) AS DECIMAL(16,2)) AS mastery from student s,studentproblemhistory sh where s.classId=(:classId) and studId=(:studId) and sh.mode != 'demo' group by sh.topicId";
    public static final String PER_TOPIC_QUERY_COMPLETE_AVG = "select sh.topicId,CAST(AVG(sh.mastery) AS DECIMAL(16,2)) AS mastery from student s,studentproblemhistory sh where s.classId=(:classId) and studId=(:studId) and sh.mode != 'demo' group by sh.topicId";
    public static final String PER_TOPIC_QUERY_COMPLETE_LATEST = "select sh.topicId, CAST(sh.mastery AS DECIMAL(16,2)) AS mastery from studentproblemhistory sh where sh.id IN (SELECT MAX(shs.id) from studentproblemhistory shs where studId=(:studId) and shs.mode != 'demo' group by shs.topicId)";

    public static final String PER_PROBLEM_QUERY_FIRST = "select distinct(e.problemId)as problemID,pr.name,pr.standardID, pr.standardCategoryName,pr.screenShotURL,std.description from eventlog e,problem pr,standard std, student student where student.trialUser=0 and student.classId=(:classId) and probElapsed<600000 and e.action in ('Attempt', 'BeginProblem','EndProblem', 'Hint') and student.id = e.studId and e.problemId = pr.id and pr.standardID=std.id order by student.id, e.id";
    public static final String PER_PROBLEM_QUERY_SECOND = "select e.* from eventlog e, student where student.trialUser=0 and student.classId=(:classId) and  e.problemId=(:problemId) and probElapsed<600000 and e.action in ('Attempt', 'BeginProblem','EndProblem', 'Hint') and student.id = e.studId order by student.id, e.id";

    public static final String PER_STANDARD_QUERY_FIRST = "select distinct(std.clusterId),cc.categoryCode,cc.clusterCCName,cc.displayName,count(distinct(h.problemId)) as noOfProblemsInCluster ,SUM((h.numHints)) as totalHintsViewedPerCluster from studentproblemhistory h, standard std, probstdmap map, cluster cc where studid in (select id from student where classId=(:classId)) and std.clusterID = cc.id and h.mode != 'demo' and std.id=map.stdId and map.probId=h.problemId group by std.clusterID";
    public static final String PER_STANDARD_QUERY_SECOND = "select std.clusterId,count(distinct(h.problemId)) as noOfProblems from studentproblemhistory h, standard std, probstdmap map where studid in (select id from student where classId =(:classId)) and mode='practice' and std.id=map.stdId and map.probId=h.problemId and h.numAttemptsToSolve = 1 group by std.clusterID";
    public static final String PER_STANDARD_QUERY_THIRD = "select distinct(h.problemId),pr.name,pr.standardID, pr.standardCategoryName,pr.screenShotURL,std.description  from studentproblemhistory h, standard std, probstdmap map,problem pr where studid in (select id from student where classId=(:classId)) and std.clusterID=(:clusterID) and mode='practice' and std.id=map.stdId and map.probId=h.problemId and h.problemId = pr.id";

    /* A private Constructor prevents any other
    * class from instantiating.
    */
    private TTUtil(){

    }
    /* Static 'instance' method */
    public static TTUtil getInstance( ) {
        return util;
    }


    public void setNumProblemsForProblemSet(DbProblem probMgr,Integer classId ,Connection conn,List<Topic> problemSets) throws SQLException {
        for (Topic t : problemSets) {
            List<Problem> problems = ProblemMgr.getWorkingProblems(t.getId());
            List<String> ids = probMgr.getClassOmittedTopicProblemIds(conn, classId, t.getId());
            Map<String,Integer> gradewiseProblemMap = new HashMap<String,Integer>();
            int availabProblem = 0;
            if (problems != null) {
                for (Problem p : problems) {
                    if (!ids.contains("" + p.getId())) {
                        availabProblem++;
                        if(gradewiseProblemMap.containsKey(p.getStandards().get(0).getGrade())){
                            int numProb = gradewiseProblemMap.get(p.getStandards().get(0).getGrade());
                            numProb++;
                            gradewiseProblemMap.put(p.getStandards().get(0).getGrade(),numProb);
                        }else{
                            gradewiseProblemMap.put(p.getStandards().get(0).getGrade(),1);
                        }
                    }
                }

            }
            t.setNumProbs(availabProblem);
            t.setGradewiseProblemDistribution(gradewiseProblemMap);
        }
    }

    public void resetSequenceNosForTheClass( List<Topic> classActiveTopics,int classId, NamedParameterJdbcTemplate namedParameterJdbcTemplate){
        int resetSeq = 1;
        for(Topic classTopic : classActiveTopics) {
            Map<String, Integer> insertParams = new HashMap<String, Integer>();
            insertParams.put("classId", classId);
            insertParams.put("seqPos", resetSeq);
            insertParams.put("probGroupId", classTopic.getId());
            insertParams.put("isDefault", 0);
            namedParameterJdbcTemplate.update(TTUtil.INSERT_ON_CLASS_PLAN, insertParams);
            resetSeq++;
        }

    }

    public Map<String,Integer> getMaxGradesforClassPerHashMap( Map<String,Integer> gradewisePerProblemMap, Map<String,Integer> headerGradeMap){
        for(Map.Entry<String,Integer> gradeEntry : gradewisePerProblemMap.entrySet())
            headerGradeMap.putIfAbsent(gradeEntry.getKey(),gradeEntry.getValue());
        return headerGradeMap;
    }
}
