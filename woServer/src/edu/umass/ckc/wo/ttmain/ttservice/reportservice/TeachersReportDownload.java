package edu.umass.ckc.wo.ttmain.ttservice.reportservice;

import edu.umass.ckc.wo.ttmain.ttmodel.ClassStudents;
import edu.umass.ckc.wo.ttmain.ttservice.classservice.TTReportService;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

            //SheetOne
            Sheet sheet = workbook.createSheet(classId);
            // create header row

            Font headerFont = workbook.createFont();
            headerFont.setFontHeightInPoints((short)20);
            headerFont.setFontName("Angsana New");

            CellStyle cellHeaderRowStyle = workbook.createCellStyle();
            cellHeaderRowStyle.setBorderLeft(BorderStyle.THIN);
            cellHeaderRowStyle.setBorderRight(BorderStyle.THIN);
            cellHeaderRowStyle.setBorderBottom(BorderStyle.THIN);
            cellHeaderRowStyle.setBorderTop(BorderStyle.THIN);
            cellHeaderRowStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            cellHeaderRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellHeaderRowStyle.setFont(headerFont);

            Row header = sheet.createRow(3);
            Cell studentNameCell = header.createCell(3);
            Cell studentUsernameCell = header.createCell(4);
            Cell studentProblemsAttemptedCell = header.createCell(5);

            studentNameCell.setCellValue("Student Name");
            studentNameCell.setCellStyle(cellHeaderRowStyle);

            studentUsernameCell.setCellValue("Username");
            studentUsernameCell.setCellStyle(cellHeaderRowStyle);

            studentProblemsAttemptedCell.setCellValue("No of problems attempted");
            studentProblemsAttemptedCell.setCellStyle(cellHeaderRowStyle);


            AtomicInteger atomicInteger = new AtomicInteger(4);

            CellStyle cellDataStyle = workbook.createCellStyle();
            cellDataStyle.setBorderLeft(BorderStyle.THIN);
            cellDataStyle.setBorderRight(BorderStyle.THIN);
            cellDataStyle.setBorderBottom(BorderStyle.THIN);
            cellDataStyle.setBorderTop(BorderStyle.THIN);
            cellDataStyle.setFont(headerFont);


            (dataMap).forEach(levelD -> {

                Row initialData = sheet.createRow(atomicInteger.getAndIncrement());


                Cell studentNameCellChild = initialData.createCell(3);
                studentNameCellChild.setCellValue(levelD.getStudentName());
                studentNameCellChild.setCellStyle(cellDataStyle);
                sheet.autoSizeColumn(studentNameCellChild.getColumnIndex());

                Cell studentIDCell = initialData.createCell(4);
                studentIDCell.setCellValue(levelD.getUserName());
                studentIDCell.setCellStyle(cellDataStyle);
                sheet.autoSizeColumn(studentIDCell.getColumnIndex());


                Cell hyperLinkCell = initialData.createCell(5);
                CellStyle hlink_style = workbook.createCellStyle();

                Font hlink_font = workbook.createFont();
                hlink_font.setUnderline(Font.U_SINGLE);
                hlink_font.setColor(IndexedColors.BLUE.getIndex());
                hlink_font.setFontHeightInPoints((short)20);
                hlink_font.setFontName("Angsana New");
                hlink_style.setFont(hlink_font);



                hlink_style.setBorderLeft(BorderStyle.THIN);
                hlink_style.setBorderRight(BorderStyle.THIN);
                hlink_style.setBorderBottom(BorderStyle.THIN);
                hlink_style.setBorderTop(BorderStyle.THIN);
                hlink_style.setAlignment(HorizontalAlignment.CENTER);

                Hyperlink linkDetail = helper.createHyperlink(HyperlinkType.DOCUMENT);
                linkDetail.setAddress("'" + levelD.getStudentId() + "'!A1");
                hyperLinkCell.setHyperlink(linkDetail);
                hyperLinkCell.setCellValue(levelD.getNoOfProblems());
                hyperLinkCell.setCellStyle(hlink_style);
                sheet.autoSizeColumn(hyperLinkCell.getColumnIndex());

            });


            //Detail Sheets
            detailDataMap.forEach((key, dataObject) -> {
                if ("effortMap".equals(key))
                    return;

                //SheetOne
                Sheet childSheets = workbook.createSheet(key);
                // create header row
                Row childHeaders = childSheets.createRow(3);

                Cell childCellHeaderProblemId = childHeaders.createCell(3);
                childCellHeaderProblemId.setCellValue("Problem Id");
                childCellHeaderProblemId.setCellStyle(cellHeaderRowStyle);
                childSheets.autoSizeColumn(childCellHeaderProblemId.getColumnIndex());

                Cell childCellHeaderProblemNickName = childHeaders.createCell(4);
                childCellHeaderProblemNickName.setCellValue("Problem Nickname");
                childCellHeaderProblemNickName.setCellStyle(cellHeaderRowStyle);
                childSheets.autoSizeColumn(childCellHeaderProblemNickName.getColumnIndex());

                Cell childCellHeaderProblemFinishedOn = childHeaders.createCell(5);
                childCellHeaderProblemFinishedOn.setCellValue("Problem finished on");
                childCellHeaderProblemFinishedOn.setCellStyle(cellHeaderRowStyle);
                childSheets.autoSizeColumn(childCellHeaderProblemFinishedOn.getColumnIndex());

                Cell childCellHeaderProblemDescription = childHeaders.createCell(6);
                childCellHeaderProblemDescription.setCellValue("Problem Description");
                childCellHeaderProblemDescription.setCellStyle(cellHeaderRowStyle);
                childSheets.autoSizeColumn(childCellHeaderProblemDescription.getColumnIndex());


                Cell childCellHeaderProblemURL  = childHeaders.createCell(7);
                childCellHeaderProblemURL.setCellValue("Problem URL");
                childCellHeaderProblemURL.setCellStyle(cellHeaderRowStyle);
                childSheets.autoSizeColumn(childCellHeaderProblemURL.getColumnIndex());

                Cell childCellHeaderSolvedCorrectly  = childHeaders.createCell(8);
                childCellHeaderSolvedCorrectly.setCellValue("Solved Correctly");
                childCellHeaderSolvedCorrectly.setCellStyle(cellHeaderRowStyle);
                childSheets.autoSizeColumn(childCellHeaderSolvedCorrectly.getColumnIndex());

                Cell childCellHeadermistakesMade  = childHeaders.createCell(9);
                childCellHeadermistakesMade.setCellValue("# of mistakes made");
                childCellHeadermistakesMade.setCellStyle(cellHeaderRowStyle);
                childSheets.autoSizeColumn(childCellHeadermistakesMade.getColumnIndex());

                Cell childCellHedarHintsSeen  = childHeaders.createCell(10);
                childCellHedarHintsSeen.setCellValue("# of hints seen");
                childCellHedarHintsSeen.setCellStyle(cellHeaderRowStyle);
                childSheets.autoSizeColumn(childCellHedarHintsSeen.getColumnIndex());

                Cell childCellHeaderAttemptsMade  = childHeaders.createCell(11);
                childCellHeaderAttemptsMade.setCellValue("# of attempts made");
                childCellHeaderAttemptsMade.setCellStyle(cellHeaderRowStyle);
                childSheets.autoSizeColumn(childCellHeaderAttemptsMade.getColumnIndex());

                Cell childCellHeaderEffort  = childHeaders.createCell(12);
                childCellHeaderEffort.setCellValue("Effort");
                childCellHeaderEffort.setCellStyle(cellHeaderRowStyle);
                childSheets.autoSizeColumn(childCellHeaderEffort.getColumnIndex());

                // Create data cells
                AtomicInteger atomicIntegerDetail = new AtomicInteger(4);
                dataObject.forEach((studentId, studentVal) -> {
                    Row childCells = childSheets.createRow(atomicIntegerDetail.getAndIncrement());


                    Cell childCellProblemId = childCells.createCell(3);
                    childCellProblemId.setCellValue(studentVal.get(0));
                    childCellProblemId.setCellStyle(cellDataStyle);
                    childSheets.autoSizeColumn(childCellProblemId.getColumnIndex());

                    Cell childCellProblemNickName = childCells.createCell(4);
                    childCellProblemNickName.setCellValue(studentVal.get(1));
                    childCellProblemNickName.setCellStyle(cellDataStyle);
                    childSheets.autoSizeColumn(childCellHeaderProblemId.getColumnIndex());

                    Cell childCelProblemFinishedOn = childCells.createCell(5);
                    childCelProblemFinishedOn.setCellValue(studentVal.get(10));
                    childCelProblemFinishedOn.setCellStyle(cellDataStyle);
                    childSheets.autoSizeColumn(childCelProblemFinishedOn.getColumnIndex());

                    Cell childCellProblemDescription = childCells.createCell(6);
                    childCellProblemDescription.setCellValue(studentVal.get(2));
                    childCellProblemDescription.setCellStyle(cellDataStyle);
                    childSheets.autoSizeColumn(childCellProblemDescription.getColumnIndex());


                    Cell childCellProblemURL  = childCells.createCell(7);
                    childCellProblemURL.setCellValue(studentVal.get(3));
                    childCellProblemURL.setCellStyle(cellDataStyle);
                    childSheets.autoSizeColumn(childCellProblemURL.getColumnIndex());

                    Cell childCellSolvedCorrectly  = childCells.createCell(8);
                    childCellSolvedCorrectly.setCellValue(studentVal.get(4));
                    childCellSolvedCorrectly.setCellStyle(cellDataStyle);
                    childSheets.autoSizeColumn(childCellSolvedCorrectly.getColumnIndex());

                    Cell childCellmistakesMade  = childCells.createCell(9);
                    childCellmistakesMade.setCellValue(studentVal.get(5));
                    childCellmistakesMade.setCellStyle(cellDataStyle);
                    childSheets.autoSizeColumn(childCellmistakesMade.getColumnIndex());

                    Cell childCellHintsSeen  = childCells.createCell(10);
                    childCellHintsSeen.setCellValue(studentVal.get(6));
                    childCellHintsSeen.setCellStyle(cellDataStyle);
                    childSheets.autoSizeColumn(childCellHintsSeen.getColumnIndex());

                    Cell childCellAttemptsMade  = childCells.createCell(11);
                    childCellAttemptsMade.setCellValue(studentVal.get(7));
                    childCellAttemptsMade.setCellStyle(cellDataStyle);
                    childSheets.autoSizeColumn(childCellAttemptsMade.getColumnIndex());

                    Cell childCellEffort  = childCells.createCell(12);
                    childCellEffort.setCellValue(studentVal.get(8));
                    childCellEffort.setCellStyle(cellDataStyle);
                    childSheets.autoSizeColumn(childCellEffort.getColumnIndex());

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
