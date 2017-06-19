package edu.umass.ckc.wo.ttmain.ttservice.classservice;

import edu.umass.ckc.wo.ttmain.ttmodel.ClassStudents;

import java.util.List;
import java.util.Map;

/**
 * Created by nsmenon on 5/19/2017.
 */

public interface TTReportService {
    public String generateTeacherReport(String teacherId, String classId, String reportType);

    Map<String,Map<String, List<String>>> generateEfortMapValues(Map<String, String> studentIds, String classId);

    List<ClassStudents> generateClassReportPerStudent(String teacherId, String classId);
}
