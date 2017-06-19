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
