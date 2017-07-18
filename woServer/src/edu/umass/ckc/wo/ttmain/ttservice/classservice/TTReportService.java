package edu.umass.ckc.wo.ttmain.ttservice.classservice;

import edu.umass.ckc.wo.ttmain.ttconfiguration.errorCodes.TTCustomException;
import edu.umass.ckc.wo.ttmain.ttmodel.ClassStudents;
import edu.umass.ckc.wo.ttmain.ttmodel.PerClusterObjectBean;
import edu.umass.ckc.wo.ttmain.ttmodel.PerProblemReportBean;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by nsmenon on 5/19/2017.
 */

public interface TTReportService {
    public String generateTeacherReport(String teacherId, String classId, String reportType);

    Map<String,PerClusterObjectBean> generatePerCommonCoreClusterReport(String classId);

    Map<String,Map<String, List<String>>> generateEfortMapValues(Map<String, String> studentIds, String classId);

    List<ClassStudents> generateClassReportPerStudent(String teacherId, String classId);

    Map<String,Object> generateClassReportPerStudentPerProblemSet(String teacherId, String classId) throws TTCustomException, SQLException;

    public String generateReportForProblemsInCluster(String teacherId, String classId, String clusterId) throws TTCustomException;

    Map<String, PerProblemReportBean> generatePerProblemReportForClass(String classId);
}
