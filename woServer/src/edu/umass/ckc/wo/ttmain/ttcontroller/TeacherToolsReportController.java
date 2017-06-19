package edu.umass.ckc.wo.ttmain.ttcontroller;

import edu.umass.ckc.wo.ttmain.ttmodel.ClassStudents;
import edu.umass.ckc.wo.ttmain.ttservice.classservice.TTReportService;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nsmenon on 5/19/2017.
 */
@Controller
public class TeacherToolsReportController {

    @Autowired
    private TTReportService reportService;


    @RequestMapping(value = "/tt/getTeacherReports", method = RequestMethod.POST)
    public @ResponseBody
    String getTeacherReport(ModelMap map, @RequestParam("teacherId") String teacherId, @RequestParam("classId") String classId, @RequestParam("reportType") String reportType) {
        String response = reportService.generateTeacherReport(teacherId, classId, reportType);
        return reportService.generateTeacherReport(teacherId, classId, reportType);
    }

    @RequestMapping(value = "/tt/downLoadPerStudentReport", method = RequestMethod.GET)
       public ModelAndView downLoadPerStudentReport(ModelMap map, @RequestParam("teacherId") String teacherId, @RequestParam("classId") String classId) {
        List<ClassStudents> classStudents =  reportService.generateClassReportPerStudent(teacherId, classId);
        map.addAttribute("classId", classId);
        map.addAttribute("teacherId", teacherId);
        map.addAttribute("levelOneData",classStudents );
        Map<String,String> studentIdMap = classStudents.stream().collect( Collectors.toMap(studMap -> studMap.getStudentId(), studMap -> studMap.getNoOfProblems()));
        map.addAttribute("levelTwoData",reportService.generateEfortMapValues(studentIdMap,classId));
        map.addAttribute("reportType", "perStudentReportDownload");
        return new ModelAndView("teachersReport", map);
    }


}
