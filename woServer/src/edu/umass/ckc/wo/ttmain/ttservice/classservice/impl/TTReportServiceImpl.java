package edu.umass.ckc.wo.ttmain.ttservice.classservice.impl;

import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.ttmain.ttconfiguration.TTConfiguration;
import edu.umass.ckc.wo.ttmain.ttconfiguration.errorCodes.ErrorCodeMessageConstants;
import edu.umass.ckc.wo.ttmain.ttconfiguration.errorCodes.TTCustomException;
import edu.umass.ckc.wo.ttmain.ttmodel.ClassStudents;
import edu.umass.ckc.wo.ttmain.ttmodel.PerClusterObjectBean;
import edu.umass.ckc.wo.ttmain.ttmodel.PerProblemReportBean;
import edu.umass.ckc.wo.ttmain.ttmodel.ProblemsView;
import edu.umass.ckc.wo.ttmain.ttmodel.datamapper.ClassStudentsMapper;
import edu.umass.ckc.wo.ttmain.ttservice.classservice.TTProblemsViewService;
import edu.umass.ckc.wo.ttmain.ttservice.classservice.TTReportService;
import edu.umass.ckc.wo.ttmain.ttservice.util.TTUtil;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutor.studmod.StudentProblemHistory;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nsmenon on 5/19/2017.
 */

@Service
public class TTReportServiceImpl implements TTReportService {
    @Autowired
    private TTConfiguration connection;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static Logger logger = Logger.getLogger(TTReportServiceImpl.class);


    @Override
    public String generateTeacherReport(String teacherId, String classId, String reportType) throws TTCustomException {
        try {
            switch (reportType) {
                case "perStudentReport":
                    List<ClassStudents> classStudents = generateClassReportPerStudent(teacherId, classId);
                    String[][] levelOneData = classStudents.stream().map(classStudents1 -> new String[]{classStudents1.getStudentId(), classStudents1.getStudentName(), classStudents1.getUserName(), classStudents1.getNoOfProblems()}).toArray(String[][]::new);
                    Map<String, String> studentIdMap = classStudents.stream().collect(Collectors.toMap(studMap -> studMap.getStudentId(), studMap -> studMap.getNoOfProblems()));
                    Map<String, Map<String, List<String>>> effortValues = generateEfortMapValues(studentIdMap, classId);
                    ObjectMapper objMapper = new ObjectMapper();
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("levelOneData", levelOneData);
                    dataMap.put("effortChartValues", effortValues.get("effortMap"));
                    dataMap.put("eachStudentDataValues", effortValues);
                    return objMapper.writeValueAsString(dataMap);

                case "perProblemReport":
                    Map<String, PerProblemReportBean> resultBean = generatePerProblemReportForClass(classId);
                    ObjectMapper perStudentPerProblemReportMapper = new ObjectMapper();
                    Map<String, Object> dataMapper = new HashMap<>();
                    dataMapper.put("levelOneDataPerProblem", resultBean);
                    return perStudentPerProblemReportMapper.writeValueAsString(dataMapper);

                case "commonCoreClusterReport":
                    Map<String, PerClusterObjectBean> resultsPerStandard = generatePerCommonCoreClusterReport(classId);
                    ObjectMapper perStudentPerProblemSetReportMapper = new ObjectMapper();
                    return perStudentPerProblemSetReportMapper.writeValueAsString(resultsPerStandard);

                case "perStudentPerProblemSetReport":
                    Map<String, Object> result = generateClassReportPerStudentPerProblemSet(teacherId, classId);
                    ObjectMapper perClusterReportMapper = new ObjectMapper();
                    return perClusterReportMapper.writeValueAsString(result);
            }
        } catch (IOException e) {
           logger.error(e.getMessage());
           throw new TTCustomException(ErrorCodeMessageConstants.DATABASE_CONNECTION_FAILED);
        }
        return null;
    }

    @Override
    public Map<String,PerClusterObjectBean> generatePerCommonCoreClusterReport(String classId) {
        Map<String,PerClusterObjectBean> completeDataMap = new LinkedHashMap<>();
        SqlParameterSource namedParameters = new MapSqlParameterSource("classId", classId);
        Map<String,PerClusterObjectBean> resultantValues = namedParameterJdbcTemplate.query(TTUtil.PER_STANDARD_QUERY_FIRST, namedParameters, (ResultSet mappedrow) -> {
            while (mappedrow.next()) {
                String clusterID = mappedrow.getString("clusterId");
                int noOfProblemsInCluster = mappedrow.getInt("noOfProblemsInCluster");
                int totalHintsViewedPerCluster = mappedrow.getInt("totalHintsViewedPerCluster");
                double avgtotalHintsViewedPerCluster =  Math.round(((double)totalHintsViewedPerCluster/(double)noOfProblemsInCluster)*100.0)/100.0;
                String categoryCode = mappedrow.getString("categoryCode");
                String clusterCCName = mappedrow.getString("clusterCCName");
                String displayName = mappedrow.getString("displayName");
                PerClusterObjectBean resultObj = new PerClusterObjectBean(clusterID,noOfProblemsInCluster,avgtotalHintsViewedPerCluster);
                resultObj.setClusterCCName(clusterCCName);
                resultObj.setCategoryCodeAndDisplayCode(categoryCode+" { "+displayName+" } ");
                completeDataMap.put(clusterID,resultObj);
             }
          return completeDataMap;
        });
        Map<String,PerClusterObjectBean> resultFirstAttemptValues = namedParameterJdbcTemplate.query(TTUtil.PER_STANDARD_QUERY_SECOND, namedParameters, (ResultSet mappedrow) -> {
            while (mappedrow.next()) {
                String clusterID = mappedrow.getString("clusterId");
                int noOfFirstAttemptProblems = mappedrow.getInt("noOfProblems");
                PerClusterObjectBean result = completeDataMap.get(clusterID);
                int firstAttempt = (int)Math.round(100.0 / result.getNoOfProblemsInCluster() * noOfFirstAttemptProblems);
                result.setNoOfProblemsonFirstAttempt(firstAttempt);
            }
            return completeDataMap;
        });
        return completeDataMap;
    }

    @Override
    public Map<String, Map<String, List<String>>> generateEfortMapValues(Map<String, String> studentIds, String classId) {
        Map<String, Map<String, List<String>>> completeDataMap = new LinkedHashMap<>();
        Map<String, List<String>> effortValues = new LinkedHashMap<String, List<String>>();
        studentIds.forEach((studentId, noOfProblems) -> {
            String[] effortvalues = new String[9];
            Integer noOfProb = Integer.valueOf(noOfProblems.trim());
            int SKIP = 0, NOTR = 0, GIVEUP = 0, SOF = 0, SHINT = 0, SHELP = 0, ATT = 0, GUESS = 0, NODATA = 0;
            Map<String, String> selectParams = new LinkedHashMap<String, String>();
            selectParams.put("classId", classId);
            selectParams.put("studId", studentId);
            List<String> studentEfforts = namedParameterJdbcTemplate.query(TTUtil.PER_STUDENT_QUERY_SECOND, selectParams, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet resultSet, int i) throws SQLException {
                    if ("".equals(resultSet.getString("effort")) || resultSet.getString("effort") == null)
                        return "";
                    return resultSet.getString("effort");
                }
            });

            Map<String, List<String>> perstudentRecords = namedParameterJdbcTemplate.query(TTUtil.PER_STUDENT_QUERY_SECOND, selectParams, (ResultSet mappedRow) -> {
                Map<String, List<String>> studentData = new LinkedHashMap<>();
                while (mappedRow.next()) {
                    List<String> studentRecordValues = new ArrayList<>();
                    studentRecordValues.add(mappedRow.getString("name"));
                    studentRecordValues.add(mappedRow.getString("nickname"));

                    if ("".equals(mappedRow.getString("statementHTML")) || mappedRow.getString("statementHTML") == null)
                        studentRecordValues.add("The problem does not have a description");
                    else
                        studentRecordValues.add(mappedRow.getString("statementHTML"));

                    studentRecordValues.add(mappedRow.getString("screenShotURL"));
                    studentRecordValues.add(mappedRow.getString("isSolved"));
                    studentRecordValues.add(mappedRow.getString("numMistakes"));
                    studentRecordValues.add(mappedRow.getString("numHints"));
                    studentRecordValues.add(mappedRow.getString("numAttemptsToSolve"));

                    if ("".equals(mappedRow.getString("effort")) || "unknown".equals(mappedRow.getString("effort")) || mappedRow.getString("effort") == null)
                        studentRecordValues.add("NO DATA");
                    else
                        studentRecordValues.add(mappedRow.getString("effort"));


                    studentRecordValues.add(mappedRow.getString("description"));

                    studentData.put(mappedRow.getString("id"), studentRecordValues);

                    if ("".equals(mappedRow.getString("problemEndTime")) || mappedRow.getString("problemEndTime") == null)
                        studentRecordValues.add("Problem was not completed");
                    else
                        studentRecordValues.add(mappedRow.getString("problemEndTime"));

                }

                return studentData;

            });

            // Calculate Effort Percentages
            for (String effortVal : studentEfforts) {
                switch (effortVal) {
                    case "SKIP":
                        SKIP++;
                        break;
                    case "NOTR":
                        NOTR++;
                        break;
                    case "GIVEUP":
                        GIVEUP++;
                        break;
                    case "SOF":
                        SOF++;
                        break;
                    case "ATT":
                        ATT++;
                        break;
                    case "GUESS":
                        GUESS++;
                        break;
                    case "SHINT":
                        SHINT++;
                        break;
                    case "SHELP":
                        SHELP++;
                        break;
                    case "NODATA":
                        NODATA++;
                        break;
                    default:
                        NODATA++;
                        break;
                }
            }
            effortvalues[0] = Double.toString((double) ((SKIP * 100) / noOfProb));
            effortvalues[1] = Double.toString((double) ((NOTR * 100) / noOfProb));
            effortvalues[2] = Double.toString((double) ((GIVEUP * 100) / noOfProb));
            effortvalues[3] = Double.toString((double) ((SOF * 100) / noOfProb));
            effortvalues[4] = Double.toString((double) ((ATT * 100) / noOfProb));
            effortvalues[5] = Double.toString((double) ((GUESS * 100) / noOfProb));
            effortvalues[6] = Double.toString((double) ((SHINT * 100) / noOfProb));
            effortvalues[7] = Double.toString((double) ((SHELP * 100) / noOfProb));
            effortvalues[8] = Double.toString((double) ((NODATA * 100) / noOfProb));

            effortValues.put(studentId, Arrays.asList(effortvalues));
            completeDataMap.put("effortMap", effortValues);
            completeDataMap.put(studentId, perstudentRecords);

        });

        return completeDataMap;
    }


    @Override
    public List<ClassStudents> generateClassReportPerStudent(String teacherId, String classId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource("classId", classId);
        List<ClassStudents> classStudents = (List) namedParameterJdbcTemplate.query(TTUtil.PER_STUDENT_QUERY_FIRST, namedParameters, new ClassStudentsMapper());
        return classStudents;
    }

    @Override
    public Map<String, Object> generateClassReportPerStudentPerProblemSet(String teacherId, String classId) throws TTCustomException {
        SqlParameterSource namedParameters = new MapSqlParameterSource("classId", classId);
        Map<String, List<String>> finalMapLevelOne = new LinkedHashMap<>();
        Map<String, List<String>> finalMapLevelOneTemp = new LinkedHashMap<>();
        Map<String, String> columnNamesMap = new LinkedHashMap<>();
        Map<String, Object> allResult = new HashMap<>();
        Map<String, List<String>> resultantValues = namedParameterJdbcTemplate.query(TTUtil.PER_TOPIC_QUERY_FIRST, namedParameters, (ResultSet mappedrow) -> {
            while (mappedrow.next()) {
                String studentId = mappedrow.getString("studentId");
                Integer problemSetId = mappedrow.getInt("topicId");
                List<String> studentValuesList = null;
                List<String> tempTopicDescriptionList = null;
                StudentProblemHistory shHistory = null;
                try {
                    shHistory = new StudentProblemHistory(connection.getConnection(), Integer.valueOf(studentId));

                } catch (TTCustomException e) {
                    logger.error(e.getErrorMessage());
                }
                List<String> noOfProblemsSolved = shHistory.getTopicProblemsSolved(problemSetId);
                List<String> noOfProblemsSolvedOnFirstAttempt = shHistory.getTopicProblemsSolvedOnFirstAttempt(problemSetId);
                if (finalMapLevelOne.containsKey(studentId)) {
                    studentValuesList = finalMapLevelOne.get(studentId);
                    tempTopicDescriptionList = finalMapLevelOneTemp.get(studentId);
                    studentValuesList.add(mappedrow.getString("description").trim().replace(" ", "") + "~~~" + "[" + noOfProblemsSolvedOnFirstAttempt.size() + "/" + noOfProblemsSolved.size() + "]" + "---" + mappedrow.getString("mastery") + "---" + noOfProblemsSolved.size() + "---" + mappedrow.getString("topicId"));
                    tempTopicDescriptionList.add(mappedrow.getString("description").trim().replace(" ", ""));
                } else {
                    studentValuesList = new ArrayList<>();
                    tempTopicDescriptionList = new ArrayList<>();

                    studentValuesList.add("studentName" + "~~~" + mappedrow.getString("studentName"));
                    studentValuesList.add("userName" + "~~~" + mappedrow.getString("userName"));
                    studentValuesList.add(mappedrow.getString("description").trim().replace(" ", "") + "~~~" + "[" + noOfProblemsSolvedOnFirstAttempt.size() + "/" + noOfProblemsSolved.size() + "]" + "---" + mappedrow.getString("mastery") + "---" + noOfProblemsSolved.size() + "---" + mappedrow.getString("topicId"));
                    tempTopicDescriptionList.add(mappedrow.getString("description").trim().replace(" ", ""));
                }
                columnNamesMap.put(mappedrow.getString("topicId"), mappedrow.getString("description"));
                finalMapLevelOne.put(studentId, studentValuesList);
                finalMapLevelOneTemp.put(studentId, tempTopicDescriptionList);

            }
            return finalMapLevelOne;
        });
        finalMapLevelOne.forEach((studentId, studentDetails) -> {
            Map<String, String> selectParams = new LinkedHashMap<String, String>();
            selectParams.put("classId", classId);
            selectParams.put("studId", studentId);

            List<String> tempTopicDescriptionList = finalMapLevelOneTemp.get(studentId);
            List<String> columnList = new ArrayList<String>(columnNamesMap.values());
            List<String> columnListFinal = new ArrayList<String>();

            for (String columnNames : columnList)
                columnListFinal.add(columnNames.trim().replaceAll(" ", ""));

            if (!tempTopicDescriptionList.isEmpty()) {
                columnListFinal.removeAll(tempTopicDescriptionList);
                for (String columnNames : columnListFinal) {
                    studentDetails.add(columnNames + "~~~" + "");

                }
            }
        });
        allResult.put("levelOneData", finalMapLevelOne);
        allResult.put("columns", columnNamesMap);
        return allResult;
    }


    @Override
    public String getMasterProjectionsForCurrentTopic(String classId, String studentId, String topicID) throws TTCustomException {
        try {
            Map<String, String> selectParams = new LinkedHashMap<String, String>();
            ObjectMapper objMapper = new ObjectMapper();
            selectParams.put("classId", classId);
            selectParams.put("studId", studentId);
            selectParams.put("topicId", topicID);
            List<String[]> eachMasteryStudentPerTopicResults = namedParameterJdbcTemplate.query(TTUtil.PER_TOPIC_QUERY_SECOND, selectParams, (ResultSet mappedrow) -> {
                List<String[]> arrayRecords = new ArrayList<>();
                while (mappedrow.next()) {
                    Map<String, List<String[]>> studentValuesList = null;
                    String[] problemDetails = new String[10];
                    problemDetails[0] = mappedrow.getString("problemId");
                    problemDetails[1] = mappedrow.getString("name");
                    problemDetails[2] = mappedrow.getString("nickname");
                    problemDetails[3] = mappedrow.getString("statementHTML");
                    problemDetails[4] = mappedrow.getString("screenShotURL");
                    problemDetails[5] = mappedrow.getString("mastery");
                    problemDetails[6] = mappedrow.getString("problemEndTime");
                    if ("".equals(mappedrow.getString("effort")) || "unknown".equals(mappedrow.getString("effort")) || mappedrow.getString("effort") == null)
                        problemDetails[7] = "NO DATA";
                    else
                        problemDetails[7] = mappedrow.getString("effort");
                    arrayRecords.add(problemDetails);

                }
                return arrayRecords;
            });
            return objMapper.writeValueAsString(eachMasteryStudentPerTopicResults);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new TTCustomException(ErrorCodeMessageConstants.DATABASE_CONNECTION_FAILED);
        }
    }

    @Override
    public String getCompleteMasteryProjectionForStudent(String classId, String studentId, String chartType) throws TTCustomException{
        try {
            Map<String, String> selectParams = new LinkedHashMap<String, String>();
            ObjectMapper objMapper = new ObjectMapper();

            selectParams.put("classId", classId);
            selectParams.put("studId", studentId);

            String queryType = "";
            if (chartType.equals("avg"))
                queryType = TTUtil.PER_TOPIC_QUERY_COMPLETE_AVG;
            else if (chartType.equals("max"))
                queryType = TTUtil.PER_TOPIC_QUERY_COMPLETE_MAX;
            else
                queryType = TTUtil.PER_TOPIC_QUERY_COMPLETE_LATEST;

            List<String> resultantValues = namedParameterJdbcTemplate.query(queryType, selectParams, (ResultSet mappedrowAvg) -> {
                List<String> fieldValuesList = new ArrayList<>();
                while (mappedrowAvg.next()) {
                    String fieldValue = mappedrowAvg.getString("topicId") + "~~~" + mappedrowAvg.getString("mastery");
                    fieldValuesList.add(fieldValue);
                }
                return fieldValuesList;
            });
            return objMapper.writeValueAsString(resultantValues);
        }catch (Exception ex){
            logger.error(ex.getMessage());
            throw new TTCustomException(ErrorCodeMessageConstants.DATABASE_CONNECTION_FAILED);
        }
    }

    @Override
    public String generateReportForProblemsInCluster(String teacherId, String classId, String clusterId) throws TTCustomException {
        try {
            Map<String, String> selectParams = new LinkedHashMap<String, String>();
            Map<String, String> problemDescriptionMap = new LinkedHashMap<String, String>();
            selectParams.put("classId", classId);
            selectParams.put("clusterID", clusterId);
            List<String> problemIdsList = namedParameterJdbcTemplate.query(TTUtil.PER_STANDARD_QUERY_THIRD, selectParams, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet resultSet, int i) throws SQLException {
                    problemDescriptionMap.put(resultSet.getString("problemID"), resultSet.getString("name")
                            + "~~" + resultSet.getString("screenShotURL") + "~~" + resultSet.getString("standardID") + ":" + resultSet.getString("standardCategoryName"));
                    return resultSet.getString("problemID");
                }
            });
            Map<String, PerProblemReportBean> resultObjectPerCluster = generatePerProblemReportForGivenProblemIDs(classId, problemIdsList, problemDescriptionMap);
            ObjectMapper perStudentPerProblemClusterReportMapper = new ObjectMapper();
            return perStudentPerProblemClusterReportMapper.writeValueAsString(resultObjectPerCluster);
        } catch (IOException excep) {
            logger.error(excep.getMessage());
            throw new TTCustomException(ErrorCodeMessageConstants.FAILED_TO_LOAD_PROBLEMS);
        }
    }

    private Map<String, PerProblemReportBean> generatePerProblemReportForGivenProblemIDs(String classId, List<String> problemIdsList, Map<String, String> problemDescriptionMap)  {
        Map<String, PerProblemReportBean> perProblemReportBeanMap = new LinkedHashMap<String, PerProblemReportBean>();
        Map<String, String> selectParams = new LinkedHashMap<String, String>();
        String URI = Settings.probPreviewerPath;
        String html5ProblemURI = Settings.html5ProblemURI;
        selectParams.put("classId", classId);
        for (String problemId : problemIdsList) {

            selectParams.put("problemId",problemId);
            List<PerProblemReportBean> perProblemReportBean = namedParameterJdbcTemplate.query(TTUtil.PER_PROBLEM_QUERY_SECOND, selectParams, new RowMapper<PerProblemReportBean>() {
                @Override
                public PerProblemReportBean mapRow(ResultSet resultSet, int i) throws SQLException {
                    int studId = resultSet.getInt("e.studid");
                    String problemID = resultSet.getString("e.problemId");

                    PerProblemReportBean perProblemReportBeanObj = null;
                    if (perProblemReportBeanMap.containsKey(problemID))
                        perProblemReportBeanObj = perProblemReportBeanMap.get(problemID);
                    else
                        perProblemReportBeanObj = new PerProblemReportBean();

                    if (studId != perProblemReportBeanObj.lastStud) {
                        // gather stats on last student
                        if (perProblemReportBeanObj.lastStud != -1) {
                            perProblemReportBeanObj.nStudsSeen++;
                            int nStudsRepeated = perProblemReportBeanObj.getPercStudentsRepeated();
                            nStudsRepeated += (perProblemReportBeanObj.studEncounters > 1) ? 1 : 0;
                            int nStudsSkipped = perProblemReportBeanObj.getPercStudentsSkipped();
                            nStudsSkipped += (perProblemReportBeanObj.studEncounters == perProblemReportBeanObj.nSkips) ? 1 : 0;
                            int nStudsGiveUp = perProblemReportBeanObj.getPercStudentsGaveUp();
                            nStudsGiveUp += (perProblemReportBeanObj.studEncounters > perProblemReportBeanObj.nSkips && !perProblemReportBeanObj.solved) ? 1 : 0;
                            int nStudsGiveSolved = perProblemReportBeanObj.getGetPercStudentsSolvedEventually();
                            nStudsGiveSolved += perProblemReportBeanObj.solved ? 1 : 0;

                            perProblemReportBeanObj.setNoStudentsSeenProblem(perProblemReportBeanObj.nStudsSeen);
                            perProblemReportBeanObj.setPercStudentsRepeated(nStudsRepeated);
                            perProblemReportBeanObj.setPercStudentsSkipped(nStudsSkipped);
                            perProblemReportBeanObj.setPercStudentsGaveUp(nStudsGiveUp);
                            perProblemReportBeanObj.setGetPercStudentsSolvedEventually(nStudsGiveSolved);

                            if (perProblemReportBeanObj.nA > perProblemReportBeanObj.nB && perProblemReportBeanObj.nA > perProblemReportBeanObj.nC && perProblemReportBeanObj.nA > perProblemReportBeanObj.nD)
                                perProblemReportBeanObj.setMostIncorrectResponse("A");
                            else if (perProblemReportBeanObj.nB > perProblemReportBeanObj.nA && perProblemReportBeanObj.nB > perProblemReportBeanObj.nC && perProblemReportBeanObj.nB > perProblemReportBeanObj.nD)
                                perProblemReportBeanObj.setMostIncorrectResponse("B");
                            else if (perProblemReportBeanObj.nC > perProblemReportBeanObj.nA && perProblemReportBeanObj.nC > perProblemReportBeanObj.nB && perProblemReportBeanObj.nC > perProblemReportBeanObj.nD)
                                perProblemReportBeanObj.setMostIncorrectResponse("C");
                            else if (perProblemReportBeanObj.nD > perProblemReportBeanObj.nA && perProblemReportBeanObj.nD > perProblemReportBeanObj.nB && perProblemReportBeanObj.nD > perProblemReportBeanObj.nC)
                                perProblemReportBeanObj.setMostIncorrectResponse("D");
                            else perProblemReportBeanObj.setMostIncorrectResponse("-");

                        }

                        perProblemReportBeanObj.begTime = 0;
                        perProblemReportBeanObj.firstActTime = 0;
                        perProblemReportBeanObj.solved = false;
                        perProblemReportBeanObj.lastStud = studId;
                        perProblemReportBeanObj.studEncounters = 0;
                        perProblemReportBeanObj.nSkips = 0;
                        perProblemReportBeanObj.nA = perProblemReportBeanObj.nB = perProblemReportBeanObj.nC = perProblemReportBeanObj.nD = 0;
                    }

                    String action = resultSet.getString("e.action");
                    boolean isCorrect = resultSet.getBoolean("e.isCorrect");
                    long elapsedTime = resultSet.getLong("e.elapsedTime");
                    String userInput = resultSet.getString("e.userInput");
                    String activityName = resultSet.getString("e.activityName"); // on beginProblem will be either practice or demo

                    if (action.equals("BeginProblem") && activityName.equals("practice")) {
                        perProblemReportBeanObj.found = true;
                        perProblemReportBeanObj.isExample = false;
                        perProblemReportBeanObj.begTime = elapsedTime;
                        perProblemReportBeanObj.studEncounters++;
                        perProblemReportBeanObj.attemptIx = 0;
                        perProblemReportBeanObj.correctAttemptIx = 0;
                        perProblemReportBeanObj.nHints = 0;
                        perProblemReportBeanObj.firstActTime = 0;
                    } else if (action.equals("BeginProblem"))
                        perProblemReportBeanObj.isExample = true;
                    else if (action.equals("EndProblem") && !perProblemReportBeanObj.isExample) {
                        perProblemReportBeanObj.probTime = elapsedTime - perProblemReportBeanObj.begTime;
                        if (perProblemReportBeanObj.probTime < 6000 && perProblemReportBeanObj.attemptIx == 0 && perProblemReportBeanObj.nHints == 0)
                            perProblemReportBeanObj.nSkips++;
                    } else if (action.equals("Hint") && !perProblemReportBeanObj.isExample) {
                        perProblemReportBeanObj.nHints++;
                        if (perProblemReportBeanObj.firstActTime == 0)
                            perProblemReportBeanObj.firstActTime = elapsedTime - perProblemReportBeanObj.begTime;
                    } else if (action.equals("Attempt") && !perProblemReportBeanObj.isExample) {
                        perProblemReportBeanObj.attemptIx++;
                        if (perProblemReportBeanObj.firstActTime == 0)
                            perProblemReportBeanObj.firstActTime = elapsedTime - perProblemReportBeanObj.begTime;
                        if (isCorrect) {
                            perProblemReportBeanObj.solved = true;
                            if (perProblemReportBeanObj.correctAttemptIx == 0) {
                                perProblemReportBeanObj.correctAttemptIx = perProblemReportBeanObj.attemptIx;
                            }
                            if (perProblemReportBeanObj.studEncounters == 1) {
                                if (perProblemReportBeanObj.correctAttemptIx == 1) {
                                    perProblemReportBeanObj.getGetPercStudentsSolvedFirstTry++;

                                } else if (perProblemReportBeanObj.correctAttemptIx == 2) {
                                    perProblemReportBeanObj.getGetPercStudentsSolvedSecondTry++;
                                }
                            }
                        } else {
                            if (userInput.equalsIgnoreCase("a"))
                                perProblemReportBeanObj.nA++;
                            else if (userInput.equalsIgnoreCase("b"))
                                perProblemReportBeanObj.nB++;
                            else if (userInput.equalsIgnoreCase("c"))
                                perProblemReportBeanObj.nC++;
                            else if (userInput.equalsIgnoreCase("d"))
                                perProblemReportBeanObj.nD++;
                        }
                    }
                   /* if (action.equals("EndProblem") && perProblemReportBeanObj.isExample) {
                        perProblemReportBeanMap.remove(problemId);
                    } else{*/

                   // }
                    Problem probDetails = ProblemMgr.getProblem(Integer.valueOf(problemID));
                    if(probDetails != null) {
                        if ("flash".equals(probDetails.getType())) {
                            perProblemReportBeanObj.setProblemURLWindow( URI + "?questionNum=" + probDetails.getProbNumber());
                        } else {
                            perProblemReportBeanObj.setProblemURLWindow(  html5ProblemURI + probDetails.getHTMLDir() + "/" + probDetails.getResource());
                        }
                    }else{
                        perProblemReportBeanObj.setProblemURLWindow( html5ProblemURI );
                    }
                    perProblemReportBeanMap.put(problemID, perProblemReportBeanObj);
                    return perProblemReportBeanObj;
                }
            });
        }
        perProblemReportBeanMap.forEach((problemId, perProblemReportBeanObj) -> {

            String[] problemDescriptionValues =  problemDescriptionMap.get(problemId).split("~~");
            perProblemReportBeanObj.setProblemName(problemDescriptionValues[0]);
            perProblemReportBeanObj.setImageURL(problemDescriptionValues[1]);
            perProblemReportBeanObj.setProblemStandardAndDescription(problemDescriptionValues[2]);

            if (perProblemReportBeanObj.found) {
                perProblemReportBeanObj.nStudsSeen++;
                int nStudsRepeated = perProblemReportBeanObj.getPercStudentsRepeated();
                nStudsRepeated += (perProblemReportBeanObj.studEncounters > 1) ? 1 : 0;
                int nStudsSkipped = perProblemReportBeanObj.getPercStudentsSkipped();
                nStudsSkipped += (perProblemReportBeanObj.studEncounters == perProblemReportBeanObj.nSkips) ? 1 : 0;
                int nStudsGiveUp = perProblemReportBeanObj.getPercStudentsGaveUp();
                nStudsGiveUp += (perProblemReportBeanObj.studEncounters > perProblemReportBeanObj.nSkips && !perProblemReportBeanObj.solved) ? 1 : 0;
                int nStudsGiveSolved = perProblemReportBeanObj.getGetPercStudentsSolvedEventually();
                nStudsGiveSolved += perProblemReportBeanObj.solved ? 1 : 0;

                perProblemReportBeanObj.setNoStudentsSeenProblem(perProblemReportBeanObj.nStudsSeen);
                perProblemReportBeanObj.setPercStudentsRepeated(nStudsRepeated);
                perProblemReportBeanObj.setPercStudentsSkipped(nStudsSkipped);
                perProblemReportBeanObj.setPercStudentsGaveUp(nStudsGiveUp);
                perProblemReportBeanObj.setGetPercStudentsSolvedEventually(nStudsGiveSolved);

                if (perProblemReportBeanObj.nA > perProblemReportBeanObj.nB && perProblemReportBeanObj.nA > perProblemReportBeanObj.nC && perProblemReportBeanObj.nA > perProblemReportBeanObj.nD)
                    perProblemReportBeanObj.setMostIncorrectResponse("A");
                else if (perProblemReportBeanObj.nB > perProblemReportBeanObj.nA && perProblemReportBeanObj.nB > perProblemReportBeanObj.nC && perProblemReportBeanObj.nB > perProblemReportBeanObj.nD)
                    perProblemReportBeanObj.setMostIncorrectResponse("B");
                else if (perProblemReportBeanObj.nC > perProblemReportBeanObj.nA && perProblemReportBeanObj.nC > perProblemReportBeanObj.nB && perProblemReportBeanObj.nC > perProblemReportBeanObj.nD)
                    perProblemReportBeanObj.setMostIncorrectResponse("C");
                else if (perProblemReportBeanObj.nD > perProblemReportBeanObj.nA && perProblemReportBeanObj.nD > perProblemReportBeanObj.nB && perProblemReportBeanObj.nD > perProblemReportBeanObj.nC)
                    perProblemReportBeanObj.setMostIncorrectResponse("D");
                else perProblemReportBeanObj.setMostIncorrectResponse("-");
            }
            /* Round while converting to Percentage */
            perProblemReportBeanObj.setPercStudentsRepeated((int)Math.round(100.0 / perProblemReportBeanObj.getNoStudentsSeenProblem() * perProblemReportBeanObj.getPercStudentsRepeated()));
            perProblemReportBeanObj.setPercStudentsSkipped((int)Math.round(100.0 / perProblemReportBeanObj.getNoStudentsSeenProblem() * perProblemReportBeanObj.getPercStudentsSkipped()));
            perProblemReportBeanObj.setPercStudentsGaveUp((int)Math.round(100.0 / perProblemReportBeanObj.getNoStudentsSeenProblem() * perProblemReportBeanObj.getPercStudentsGaveUp()));
            perProblemReportBeanObj.setGetPercStudentsSolvedEventually((int)Math.round(100.0 / perProblemReportBeanObj.getNoStudentsSeenProblem() * perProblemReportBeanObj.getGetPercStudentsSolvedEventually()));
            perProblemReportBeanObj.setGetGetPercStudentsSolvedFirstTry((int)Math.round(100.0 / perProblemReportBeanObj.getNoStudentsSeenProblem() * perProblemReportBeanObj.getGetGetPercStudentsSolvedFirstTry()));

        });
        return perProblemReportBeanMap;
    }
    @Override
    public Map<String, PerProblemReportBean> generatePerProblemReportForClass(String classId) {
        Map<String, String> selectParams = new LinkedHashMap<String, String>();
        Map<String, String> problemDescriptionMap = new LinkedHashMap<String, String>();
        selectParams.put("classId", classId);
        List<String> problemIdsList = namedParameterJdbcTemplate.query(TTUtil.PER_PROBLEM_QUERY_FIRST, selectParams, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                problemDescriptionMap.put(resultSet.getString("problemID"),resultSet.getString("name")
                        +"~~"+resultSet.getString("screenShotURL")+"~~"+resultSet.getString("standardID")+":"+resultSet.getString("standardCategoryName"));
                return resultSet.getString("problemID");
            }
        });
        return  generatePerProblemReportForGivenProblemIDs(classId,problemIdsList,problemDescriptionMap);
    }
}