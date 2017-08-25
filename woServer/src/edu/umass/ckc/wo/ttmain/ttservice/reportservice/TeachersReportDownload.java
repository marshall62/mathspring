package edu.umass.ckc.wo.ttmain.ttservice.reportservice;

import edu.umass.ckc.wo.ttmain.ttmodel.ClassStudents;
import edu.umass.ckc.wo.ttmain.ttmodel.PerClusterObjectBean;
import edu.umass.ckc.wo.ttmain.ttmodel.PerProblemReportBean;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.servlet.view.document.AbstractXlsView;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nsmenon on 6/6/2017.
 */

public class TeachersReportDownload extends AbstractXlsView {

    @Override
    protected void buildExcelDocument(Map<String, Object> map, Workbook workbook, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        String reportType = (String) map.get("reportType");
        if (reportType.equals("perStudentReportDownload"))
            buildPerStudentTeacherReport(map, workbook, httpServletRequest, httpServletResponse);
        else if (reportType.equals("perProblmSetReportDownload"))
            buildPerProblemSetTeacherReport(map, workbook, httpServletRequest, httpServletResponse);
        else if (reportType.equals("perProblemReportDownload"))
            buildPerProblemReport(map, workbook, httpServletRequest, httpServletResponse);
        else if (reportType.equals("perClusterReport"))
            buildPerClusterReport(map, workbook, httpServletRequest, httpServletResponse);
    }


    private void buildPerClusterReport(Map<String, Object> map, Workbook workbook, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            String classId = (String) map.get("classId");
            String teacherId = (String) map.get("teacherId");
            CreationHelper helper = workbook.getCreationHelper();
            httpServletResponse.setContentType("application/vnd.ms-excel");
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"per_cluster_problem_report" + classId + ".xls\"");
            Map<String, PerClusterObjectBean> dataForProblemObjects = (Map<String, PerClusterObjectBean>) map.get("dataForProblem");

            Sheet sheet = workbook.createSheet(classId);
            Row header = sheet.createRow(3);
            Cell clusterIdHeader = header.createCell(2);
            clusterIdHeader.setCellValue("Cluster ID");


            Cell categoryCodeAndDisplayCodeHeader = header.createCell(3);
            categoryCodeAndDisplayCodeHeader.setCellValue("Cluster's in Class");

            Cell ClusterDescriptionHeader = header.createCell(4);
            ClusterDescriptionHeader.setCellValue("Cluster Description");


            Cell noOfProblemsInClusterHeader = header.createCell(5);
            noOfProblemsInClusterHeader.setCellValue("# of problems in cluster");

            Cell noOfProblemsonFirstAttemptHeader = header.createCell(6);
            noOfProblemsonFirstAttemptHeader.setCellValue("% solved in the first attempt");


            Cell totalHintsViewedPerClusterHeader = header.createCell(7);
            totalHintsViewedPerClusterHeader.setCellValue("Avg ratio of hint requested");


            AtomicInteger atomicIntegerForHeaderForData = new AtomicInteger(4);
            dataForProblemObjects.forEach((clusterID, clusterObject) -> {
                Row dataRow = sheet.createRow(atomicIntegerForHeaderForData.getAndIncrement());
                Cell clusterId = dataRow.createCell(2);
                clusterId.setCellValue(clusterID);


                Cell categoryCodeAndDisplayCode = dataRow.createCell(3);
                categoryCodeAndDisplayCode.setCellValue(clusterObject.getCategoryCodeAndDisplayCode());


                Cell clusterCCName = dataRow.createCell(4);
                clusterCCName.setCellValue(clusterObject.getClusterCCName());


                Cell noOfProblemsInCluster = dataRow.createCell(5);
                noOfProblemsInCluster.setCellValue(clusterObject.getNoOfProblemsInCluster());


                Cell noOfProblemsonFirstAttempt = dataRow.createCell(6);
                noOfProblemsonFirstAttempt.setCellValue(clusterObject.getNoOfProblemsonFirstAttempt());


                Cell totalHintsViewedPerCluster = dataRow.createCell(7);
                totalHintsViewedPerCluster.setCellValue(clusterObject.getTotalHintsViewedPerCluster());

            });

            workbook.write(httpServletResponse.getOutputStream());
            httpServletResponse.getOutputStream().flush();
            httpServletResponse.getOutputStream().close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    private void buildPerProblemReport(Map<String, Object> map, Workbook workbook, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            String classId = (String) map.get("classId");
            String teacherId = (String) map.get("teacherId");
            CreationHelper helper = workbook.getCreationHelper();
            httpServletResponse.setContentType("application/vnd.ms-excel");
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"problem_report" + classId + ".xls\"");
            Map<String, PerProblemReportBean> dataForProblemObjects = (Map<String, PerProblemReportBean>) map.get("dataForProblem");
            Sheet sheet = workbook.createSheet(classId);
            Row header = sheet.createRow(3);

            Cell problemID = header.createCell(2);
            problemID.setCellValue("Problem ID");


            Cell problemName = header.createCell(3);
            problemName.setCellValue("Problem Name");


            Cell problemStandard = header.createCell(4);
            problemStandard.setCellValue("Problem Standard");


            Cell noStudentsSeenProblem = header.createCell(5);
            noStudentsSeenProblem.setCellValue("# of Students seen the problem");


            Cell getPercStudentsSolvedEventually = header.createCell(6);
            getPercStudentsSolvedEventually.setCellValue("% of Students solved the problem");


            Cell getGetPercStudentsSolvedFirstTry = header.createCell(7);
            getGetPercStudentsSolvedFirstTry.setCellValue("% of Students solved the problem on the first attempt");


            Cell percStudentsRepeated = header.createCell(8);
            percStudentsRepeated.setCellValue("% of Students repeated the problem");


            Cell percStudentsSkipped = header.createCell(9);
            percStudentsSkipped.setCellValue("% of Students skipped the problem");


            Cell percStudentsGaveUp = header.createCell(10);
            percStudentsGaveUp.setCellValue("% of Students gave up");


            Cell mostIncorrectResponse = header.createCell(11);
            mostIncorrectResponse.setCellValue("Most Frequent Incorrect Response");


            AtomicInteger atomicIntegerForHeaderForData = new AtomicInteger(4);
            dataForProblemObjects.forEach((problemId, problemDetails) -> {
                Row dataRow = sheet.createRow(atomicIntegerForHeaderForData.getAndIncrement());

                Cell problemIDData = dataRow.createCell(2);
                problemIDData.setCellValue(problemId);


                Cell problemNameData = dataRow.createCell(3);
                problemNameData.setCellValue(problemDetails.getProblemName());


                Cell problemStandardData = dataRow.createCell(4);
                problemStandardData.setCellValue(problemDetails.getProblemStandardAndDescription());


                Cell noStudentsSeenProblemData = dataRow.createCell(5);
                noStudentsSeenProblemData.setCellValue(problemDetails.getNoStudentsSeenProblem());


                Cell getPercStudentsSolvedEventuallyData = dataRow.createCell(6);
                getPercStudentsSolvedEventuallyData.setCellValue(problemDetails.getGetPercStudentsSolvedEventually());


                Cell getGetPercStudentsSolvedFirstTryData = dataRow.createCell(7);
                getGetPercStudentsSolvedFirstTryData.setCellValue(problemDetails.getGetGetPercStudentsSolvedFirstTry());


                Cell percStudentsRepeatedData = dataRow.createCell(8);
                percStudentsRepeatedData.setCellValue(problemDetails.getPercStudentsRepeated());


                Cell percStudentsSkippedData = dataRow.createCell(9);
                percStudentsSkippedData.setCellValue(problemDetails.getPercStudentsSkipped());


                Cell percStudentsGaveUpData = dataRow.createCell(10);
                percStudentsGaveUpData.setCellValue(problemDetails.getPercStudentsGaveUp());


                Cell mostIncorrectResponseData = dataRow.createCell(11);
                mostIncorrectResponseData.setCellValue(problemDetails.getMostIncorrectResponse());


            });

            workbook.write(httpServletResponse.getOutputStream());
            httpServletResponse.getOutputStream().flush();
            httpServletResponse.getOutputStream().close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void buildPerProblemSetTeacherReport(Map<String, Object> map, Workbook workbook, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        try {
            String classId = (String) map.get("classId");
            String teacherId = (String) map.get("teacherId");
            CreationHelper helper = workbook.getCreationHelper();
            httpServletResponse.setContentType("application/vnd.ms-excel");
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"problemset_report" + classId + ".xls\"");
            Map<String, Object> dataForProblemSet = (Map<String, Object>) map.get("dataForProblemSet");

            Map<String, List<String>> finalMapLevelOne = (Map<String, List<String>>) dataForProblemSet.get("levelOneData");
            Map<String, String> columnNamesMap = (Map<String, String>) dataForProblemSet.get("columns");

            Sheet sheet = workbook.createSheet(classId);

            Row header = sheet.createRow(3);

            Cell studentNameID = header.createCell(2);
            Cell studentNameCell = header.createCell(3);
            Cell studentUsernameCell = header.createCell(4);
            studentNameID.setCellValue("Student ID");


            studentNameCell.setCellValue("Student Name");


            studentUsernameCell.setCellValue("Username");


            AtomicInteger atomicIntegerForHeader = new AtomicInteger(5);
            AtomicInteger atomicIntegerForHeaderForData = new AtomicInteger(4);
            columnNamesMap.forEach((topicID, topicName) -> {
                Cell columnNameHeaderCell = header.createCell(atomicIntegerForHeader.getAndIncrement());
                columnNameHeaderCell.setCellValue(topicName);

            });
            finalMapLevelOne.forEach((studentId, problemSetDetails) -> {

                Row dataRow = sheet.createRow(atomicIntegerForHeaderForData.getAndIncrement());
                Cell columnNameHeaderCellID = dataRow.createCell(2);
                columnNameHeaderCellID.setCellValue(studentId);


                for (String studentDetails : problemSetDetails) {
                    String datadetails[] = studentDetails.split("~~~");
                    if (studentDetails.contains("studentName")) {
                        Cell columnStudentPersonal = dataRow.createCell(3);
                        columnStudentPersonal.setCellValue(datadetails[1]);


                    } else if (studentDetails.contains("userName")) {
                        Cell columnStudentPersonal = dataRow.createCell(4);
                        columnStudentPersonal.setCellValue(datadetails[1]);


                    } else {
                        if (datadetails.length > 1) {
                            String[] masteryDetailsForproblemSet = datadetails[1].split("---");
                            String problemRatio = masteryDetailsForproblemSet[0];
                            String mastery = masteryDetailsForproblemSet[1];
                            String topicName = columnNamesMap.get(masteryDetailsForproblemSet[3]);
                            int cellIndex = 0;
                            for (Cell cell : header) {
                                if (cell.getStringCellValue().equals(topicName))
                                    cellIndex = cell.getColumnIndex();
                            }
                            Cell columnStudentproblemSetDetails = dataRow.createCell(cellIndex);
                            columnStudentproblemSetDetails.setCellValue(problemRatio + mastery);
                        }
                    }
                }
            });

            workbook.write(httpServletResponse.getOutputStream());
            httpServletResponse.getOutputStream().flush();
            httpServletResponse.getOutputStream().close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void buildPerStudentTeacherReport(Map<String, Object> map, Workbook workbook, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            String classId = (String) map.get("classId");
            String teacherId = (String) map.get("teacherId");
            CreationHelper helper = workbook.getCreationHelper();
            httpServletResponse.setContentType("application/vnd.ms-excel");
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"student_report_" + classId + ".xls\"");
            List<ClassStudents> dataMap = (List<ClassStudents>) map.get("levelOneData");
            Map<String, Map<String, List<String>>> detailDataMap = (Map<String, Map<String, List<String>>>) map.get("levelTwoData");
            Sheet sheet = workbook.createSheet(classId);

            Row header = sheet.createRow(3);
            Cell studentNameID = header.createCell(2);
            Cell studentNameCell = header.createCell(3);
            Cell studentUsernameCell = header.createCell(4);
            Cell childCellHeaderProblemId = header.createCell(5);
            Cell childCellHeaderProblemNickName = header.createCell(6);
            Cell childCellHeaderProblemFinishedOn = header.createCell(7);
            Cell childCellHeaderProblemDescription = header.createCell(8);
            Cell childCellHeaderProblemURL = header.createCell(9);
            Cell childCellHeaderSolvedCorrectly = header.createCell(10);
            Cell childCellHeadermistakesMade = header.createCell(11);
            Cell childCellHedarHintsSeen = header.createCell(12);
            Cell childCellHeaderAttemptsMade = header.createCell(13);
            Cell childCellHeaderEffort = header.createCell(14);

            studentNameID.setCellValue("Student ID");
            studentNameCell.setCellValue("Student Name");
            studentUsernameCell.setCellValue("Username");
            childCellHeaderProblemId.setCellValue("Problem Id");
            childCellHeaderProblemNickName.setCellValue("Problem Nickname");
            childCellHeaderProblemFinishedOn.setCellValue("Problem finished on");
            childCellHeaderProblemDescription.setCellValue("Problem Description");
            childCellHeaderProblemURL.setCellValue("Problem URL");
            childCellHeaderSolvedCorrectly.setCellValue("Solved Correctly");
            childCellHeadermistakesMade.setCellValue("# of mistakes made");
            childCellHedarHintsSeen.setCellValue("# of hints seen");
            childCellHeaderAttemptsMade.setCellValue("# of attempts made");
            childCellHeaderEffort.setCellValue("Effort");

            AtomicInteger atomicInteger = new AtomicInteger(4);
            detailDataMap.forEach((key, dataObject) -> {

                if ("effortMap".equals(key))
                    return;

                (dataObject).forEach((problemId, studentVal) -> {
                    Row dataRow = sheet.createRow(atomicInteger.getAndIncrement());
                    Cell studentIDCell = dataRow.createCell(2);
                    studentIDCell.setCellValue(key);
                    dataMap.forEach(leveoneId -> {
                        if (key.equals(leveoneId.getStudentId())) {
                            Cell studentNameCellChild = dataRow.createCell(3);
                            studentNameCellChild.setCellValue(leveoneId.getStudentName());
                            Cell studentUserNameCell = dataRow.createCell(4);
                            studentUserNameCell.setCellValue(leveoneId.getUserName());
                        }
                    });
                    Cell childCellProblemId = dataRow.createCell(5);
                    childCellProblemId.setCellValue(studentVal.get(0));
                    Cell childCellProblemNickName = dataRow.createCell(6);
                    childCellProblemNickName.setCellValue(studentVal.get(1));
                    Cell childCelProblemFinishedOn = dataRow.createCell(7);
                    childCelProblemFinishedOn.setCellValue(studentVal.get(10));
                    Cell childCellProblemDescription = dataRow.createCell(8);
                    childCellProblemDescription.setCellValue(studentVal.get(2));
                    Cell childCellProblemURL = dataRow.createCell(9);
                    childCellProblemURL.setCellValue(studentVal.get(3));
                    Cell childCellSolvedCorrectly = dataRow.createCell(10);
                    childCellSolvedCorrectly.setCellValue(studentVal.get(4));
                    Cell childCellmistakesMade = dataRow.createCell(11);
                    childCellmistakesMade.setCellValue(studentVal.get(5));
                    Cell childCellHintsSeen = dataRow.createCell(12);
                    childCellHintsSeen.setCellValue(studentVal.get(6));
                    Cell childCellAttemptsMade = dataRow.createCell(13);
                    childCellAttemptsMade.setCellValue(studentVal.get(7));
                    Cell childCellEffort = dataRow.createCell(14);
                    childCellEffort.setCellValue(studentVal.get(8));
                });

            });

            workbook.write(httpServletResponse.getOutputStream());
            httpServletResponse.getOutputStream().flush();
            httpServletResponse.getOutputStream().close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
