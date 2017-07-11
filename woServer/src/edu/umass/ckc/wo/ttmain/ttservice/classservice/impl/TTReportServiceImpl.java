package edu.umass.ckc.wo.ttmain.ttservice.classservice.impl;

import edu.umass.ckc.wo.ttmain.ttconfiguration.TTConfiguration;
import edu.umass.ckc.wo.ttmain.ttmodel.ClassStudents;
import edu.umass.ckc.wo.ttmain.ttmodel.datamapper.ClassStudentsMapper;
import edu.umass.ckc.wo.ttmain.ttservice.classservice.TTReportService;
import edu.umass.ckc.wo.ttmain.ttservice.util.TTUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

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
    
    @Override
    public String generateTeacherReport(String teacherId, String classId, String reportType) {
        try {
            switch (reportType) {
                case "perStudentReport":
                    List<ClassStudents> classStudents =  generateClassReportPerStudent(teacherId, classId);
                    String[][] levelOneData = classStudents.stream().map(classStudents1 -> new String[]{classStudents1.getStudentId(),classStudents1.getStudentName(), classStudents1.getUserName(), classStudents1.getNoOfProblems()}).toArray(String[][]::new);
                    Map<String,String> studentIdMap = classStudents.stream().collect( Collectors.toMap( studMap -> studMap.getStudentId(), studMap -> studMap.getNoOfProblems()));
                    Map<String,Map<String, List<String>>> effortValues =  generateEfortMapValues(studentIdMap,classId);
                    ObjectMapper objMapper = new ObjectMapper();
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("levelOneData",levelOneData);
                    dataMap.put("effortChartValues",effortValues.get("effortMap"));
                    dataMap.put("eachStudentDataValues",effortValues);
                    return objMapper.writeValueAsString(dataMap);

                case "perProblemReport":
                    break;

                case "commonCoreClusterReport":
                    break;

                case "gainsReport":
                    break;

                case "perStudentPerProblemSetReport":
                    break;

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String,Map<String, List<String>>> generateEfortMapValues(Map<String, String> studentIds, String classId) {
        Map<String,Map<String, List<String>>> completeDataMap = new LinkedHashMap<>();
        Map<String,List<String>> effortValues= new LinkedHashMap<String,List<String>>();
        studentIds.forEach((studentId,noOfProblems) -> {
            String[] effortvalues = new String[9];
            Integer noOfProb = Integer.valueOf(noOfProblems.trim());
            int SKIP=0,NOTR=0 ,GIVEUP=0,SOF=0 ,SHINT=0 ,SHELP=0,ATT=0 ,GUESS=0 ,NODATA = 0;
            Map<String, String> selectParams = new LinkedHashMap<String, String>();
            selectParams.put("classId", classId);
            selectParams.put("studId", studentId);
            List<String> studentEfforts = namedParameterJdbcTemplate.query(TTUtil.PER_STUDENT_QUERY_SECOND, selectParams, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet resultSet, int i) throws SQLException {
                    if("".equals(resultSet.getString("effort")) || resultSet.getString("effort") == null)
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

                    if("".equals(mappedRow.getString("statementHTML")) || mappedRow.getString("statementHTML") == null)
                        studentRecordValues.add("The problem does not have a description");
                    else
                        studentRecordValues.add(mappedRow.getString("statementHTML"));

                    studentRecordValues.add(mappedRow.getString("screenShotURL"));
                    studentRecordValues.add(mappedRow.getString("isSolved"));
                    studentRecordValues.add(mappedRow.getString("numMistakes"));
                    studentRecordValues.add(mappedRow.getString("numHints"));
                    studentRecordValues.add(mappedRow.getString("numAttemptsToSolve"));

                    if("".equals(mappedRow.getString("effort")) ||  "unknown".equals(mappedRow.getString("effort")) || mappedRow.getString("effort") == null)
                    studentRecordValues.add("NO DATA");
                    else
                     studentRecordValues.add(mappedRow.getString("effort"));


                    studentRecordValues.add(mappedRow.getString("description"));

                    studentData.put(mappedRow.getString("id"), studentRecordValues);

                    if("".equals(mappedRow.getString("problemEndTime"))  || mappedRow.getString("problemEndTime") == null)
                        studentRecordValues.add("Problem was not completed");
                    else
                        studentRecordValues.add(mappedRow.getString("problemEndTime"));

                }

                return studentData;

            });

            // Calculate Effort Percentages
            for(String effortVal : studentEfforts){
                switch (effortVal) {
                    case "SKIP": SKIP++; break;
                    case "NOTR": NOTR++; break;
                    case "GIVEUP":GIVEUP++; break;
                    case "SOF":SOF++; break;
                    case "ATT":ATT++; break;
                    case "GUESS":GUESS++; break;
                    case "SHINT":SHINT++; break;
                    case "SHELP":SHELP++; break;
                    case "NODATA":NODATA++; break;
                    default:NODATA++; break;
                }
            }
            effortvalues[0] =  Double.toString((double)((SKIP * 100)/noOfProb));
            effortvalues[1] =  Double.toString((double)((NOTR * 100)/noOfProb));
            effortvalues[2] =  Double.toString((double)((GIVEUP * 100)/noOfProb));
            effortvalues[3] =  Double.toString((double)((SOF * 100)/noOfProb));
            effortvalues[4] =  Double.toString((double)((ATT * 100)/noOfProb));
            effortvalues[5] =  Double.toString((double)((GUESS * 100)/noOfProb));
            effortvalues[6] =  Double.toString((double)((SHINT * 100)/noOfProb));
            effortvalues[7] =  Double.toString((double)((SHELP * 100)/noOfProb));
            effortvalues[8] =  Double.toString((double)((NODATA * 100)/noOfProb));

            effortValues.put(studentId, Arrays.asList(effortvalues));
            completeDataMap.put("effortMap",effortValues);
            completeDataMap.put(studentId,perstudentRecords);

        });

        return completeDataMap;
    }



    @Override
    public List<ClassStudents> generateClassReportPerStudent(String teacherId, String classId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource("classId", classId);
        List<ClassStudents> classStudents = (List)namedParameterJdbcTemplate.query(TTUtil.PER_STUDENT_QUERY_FIRST, namedParameters, new ClassStudentsMapper());
        return classStudents;
    }
}
