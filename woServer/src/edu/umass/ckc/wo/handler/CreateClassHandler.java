package edu.umass.ckc.wo.handler;


import edu.umass.ckc.wo.beans.Classes;
import edu.umass.ckc.wo.db.DbClassPedagogies;
import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.beans.PretestPool;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbPrePost;
import edu.umass.ckc.wo.event.admin.*;
import ckc.servlet.servbase.ServletEvent;
import ckc.servlet.servbase.View;
//import edu.umass.ckc.wo.handler.ClassAdminHelper;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class CreateClassHandler  {
    private String teacherId;
    private HttpSession sess;

    public static final String JSP = "/teacherTools/createClass.jsp";
    public static final String NOCLASS_JSP="/teacherTools/noClassCreateClass.jsp";
    public static final String SELECT_PEDAGOGIES_JSP = "/teacherTools/selectPedagogies.jsp";
    public static final String SELECT_PRETEST_POOL_JSP = "/teacherTools/selectPretest.jsp";
    public static final String ACTIVATE_HUTS_JSP = "/teacherTools/activatehuts.jsp";
    public static final String CLASS_INFO_JSP = "/teacherTools/classInfo.jsp";
    public static final String EDIT_SURVEYS_JSP = "/teacherTools/editSurveys.jsp";
    public static final String MAINPAGE_JSP ="/teacherTools/wayangMain.jsp";




    public CreateClassHandler() {
    }
    public View handleEvent(ServletContext sc, Connection conn, ServletEvent e, HttpServletRequest req, HttpServletResponse resp) throws Exception {

        // State 1: Process the event for a new class.
        // Generate createClass.jsp to get basic info about the class.
        if (e instanceof AdminCreateNewClassEvent) { // START state
            req.setAttribute("action","AdminCreateNewClass");
            req.setAttribute("message","");
            req.setAttribute("teacherId",((AdminCreateNewClassEvent) e).getTeacherId());
            req.getRequestDispatcher(JSP).forward(req,resp);
            return null;
        }
        // State 1A: Process the event for a new class without any previous class
        else if (e instanceof AdminNoClassCreateNewClassEvent){
            req.setAttribute("action","AdminCreateNewClass");
            req.setAttribute("message","");
            req.setAttribute("teacherId",((AdminNoClassCreateNewClassEvent) e).getTeacherId());
            req.getRequestDispatcher(NOCLASS_JSP).forward(req,resp);
            return null;

        }
        // State 2: Process submit-event generated from the createClass.jsp form that requests class name, town, year.
        // Creates the class in the db.
        //  Generates the next page as selectPedagogies.jsp
        else if (e instanceof AdminSubmitClassFormEvent) // class created/  Now show select pedagogies page




            return processClass(conn, (AdminSubmitClassFormEvent) e, req, resp);
        // State 3: Process submitted form generated from the selectPedagogies.jsp page.   Check selections and regenerate
        // pedagogy selection page if there are errors.  If no errors errors, save selections and
        // Generate the next page as selectPretest.jsp (n.b. this JSP page requires that we set
        // its form submission event so we pass AdminSubmitSelectedPretest)
        else if (e instanceof AdminNoClassSubmitClassFormEvent) {
            req.setAttribute("teacherId", ((AdminNoClassSubmitClassFormEvent) e).getTeacherId());
            return processNoClass(conn, (AdminNoClassSubmitClassFormEvent) e, req, resp);
        }
        else if (e instanceof AdminSubmitSelectedPedagogiesEvent) {
            AdminSubmitSelectedPedagogiesEvent e2 = (AdminSubmitSelectedPedagogiesEvent) e;
            if (!ClassAdminHelper.errorCheckSelectedPedagogySubmission(e2.getClassId(),e2.getPedagogyIds(),req,resp,
                    "AdminSubmitSelectedPedagogies", e2.getTeacherId(), conn)) {
                ClassAdminHelper.saveSelectedPedagogies(conn,e2.getClassId(),e2.getPedagogyIds());

                ClassInfo info = DbClass.getClass(conn,((AdminSubmitSelectedPedagogiesEvent) e).getClassId());
                ClassInfo[] classes = DbClass.getClasses(conn, ((AdminSubmitSelectedPedagogiesEvent) e).getTeacherId());
                Classes bean = new Classes(classes);
                req.setAttribute("bean",bean);
                req.setAttribute("classInfo",info);
                req.setAttribute("classId", ((AdminSubmitSelectedPedagogiesEvent) e).getClassId());
                req.setAttribute("teacherId", ((AdminSubmitSelectedPedagogiesEvent) e).getTeacherId());
                req.setAttribute("action","AdminAlterClassPedagogies");


                generateValidPedagogySubmissionNextPage(conn,e2.getClassId(),req,resp);
            }
            return null;
        }
        // State 4: process submitted form with selected pretest pool.  Then show class info
        // using classInfo.jsp
        else if (e instanceof AdminSubmitSelectedPretestEvent) {
            AdminSubmitSelectedPretestEvent e2 = (AdminSubmitSelectedPretestEvent) e;
            ClassInfo info = DbClass.getClass(conn,((AdminSubmitSelectedPretestEvent) e).getClassId());
            ClassInfo[] classes = DbClass.getClasses(conn, ((AdminSubmitSelectedPretestEvent) e).getTeacherId());
            Classes bean = new Classes(classes);
            req.setAttribute("action", "AdminUpdateClassId");
            req.setAttribute("bean",bean);
            req.setAttribute("classInfo",info);
            req.setAttribute("classId", e2.getClassId());
            req.setAttribute("teacherId", e2.getTeacherId());

            ClassAdminHelper.processSelectedPretestSubmission(conn, e2.getClassId(),e2.getPoolId(),
                    req,resp,MAINPAGE_JSP, e2.isGivePretest());
            return null;
        }
        // next state will be to select adventures stuff


        // for debugging only
         else if (e instanceof AdminCreateClassTestEvent) {
            PretestPool apool = DbPrePost.getPretestPool(conn,((AdminCreateClassTestEvent) e).getClassId());
            ClassInfo info = DbClass.getClass(conn,((AdminCreateClassTestEvent) e).getClassId());
            req.setAttribute("action","AdminCreateClassTest");
            req.setAttribute("pools",DbPrePost.getAllPretestPools(conn));
            req.setAttribute("classId",((AdminCreateClassTestEvent) e).getClassId());
            req.setAttribute("classInfo",info);
            ClassInfo c2 = new ClassInfo("a",100,"hi","ch","s",161,3,"dkd",4,1,1, 0, 7, 0, 7);
            PretestPool ppp = new PretestPool(1,"pool10");
            req.setAttribute("ccc",c2);
            req.setAttribute("aaa",ppp);
            req.getRequestDispatcher("/teacherTools/test.jsp").forward(req,resp);
            return null;
        }
        else return null;
    }


    /**
     * Given the fields describing the class.   This will add a row to the class table.
     * @param conn
     * @param e
     * @param req
     * @param resp
     * @return
     * @throws Exception
     */
    private View processClass(Connection conn, AdminSubmitClassFormEvent e, HttpServletRequest req,
                              HttpServletResponse resp) throws Exception {
        String className = e.getClassName();
        String school = e.getSchool();
        String schoolYear = e.getSchoolYear();
        String town = e.getTown();
        String section = e.getSection();
        int newid;
        if (className.trim().equals("") || school.trim().equals("") || schoolYear.trim().equals("")
                || town.trim().equals("")) {
            req.setAttribute("message","You must correctly fill out all required fields in the form.");
            req.setAttribute("&teacherId",e.getTeacherId());
            req.getRequestDispatcher(JSP).forward(req,resp);
            return null;
        }
        else if (!validateYear(schoolYear)) {
            req.setAttribute("&teacherId",e.getTeacherId());
            req.setAttribute("message","That year is invalid. Please enter year as 2XXX");
            req.getRequestDispatcher(JSP).forward(req,resp);
            return null;
        }
        else {
            int defaultPropGroup = DbClass.getPropGroupWithName(conn,"default");
            String tid = Integer.toString(e.getTeacherId());
            // the prop group is a set of questions that we want to ask the user (gender, race, etc).
            // When a class is first created, we use the default prop group.
            // We'll allow it to be altered in the page that allows the user to edit the class fields.
            newid = DbClass.insertClass(conn,className, school, schoolYear, town, section,tid,
                    defaultPropGroup, 0);
            if (newid != -1) {

                ClassInfo info = DbClass.getClass(conn,newid);
                ClassInfo[] classes = DbClass.getClasses(conn,e.getTeacherId());
                Classes bean = new Classes(classes);

                req.setAttribute("formSubmissionEvent","AdminSubmitSelectedPedagogies");
                req.setAttribute("pedagogies", DbClassPedagogies.getClassPedagogyBeans(conn,newid));
                req.setAttribute("bean",bean);
                req.setAttribute("classInfo",info);
                req.setAttribute("classId", newid);
                req.setAttribute("teacherId",e.getTeacherId());
                req.getRequestDispatcher(SELECT_PEDAGOGIES_JSP).forward(req,resp);
                return null;
            }
            else {
                req.setAttribute("&teacherId",e.getTeacherId());
                req.setAttribute("message","Failed to add class.  That class already exists");
                req.getRequestDispatcher(JSP).forward(req,resp);
                return null;
            }
        }
    }

    private View processNoClass(Connection conn, AdminNoClassSubmitClassFormEvent e, HttpServletRequest req,
                              HttpServletResponse resp) throws Exception {
        String className = e.getClassName();
        String school = e.getSchool();
        String schoolYear = e.getSchoolYear();
        String town = e.getTown();
        String section = e.getSection();
        int newid;
        if (className.trim().equals("") || school.trim().equals("") || schoolYear.trim().equals("")
                || town.trim().equals("")) {
            req.setAttribute("message","You must correctly fill out all required fields in the form.");
            req.setAttribute("&teacherId",e.getTeacherId());
            req.getRequestDispatcher(NOCLASS_JSP).forward(req,resp);
            return null;
        }
        else if (!validateYear(schoolYear)) {
            req.setAttribute("&teacherId",e.getTeacherId());
            req.setAttribute("message","That year is invalid. Please enter year as 2XXX");
            req.getRequestDispatcher(NOCLASS_JSP).forward(req,resp);
            return null;
        }
        else {
            int defaultPropGroup = DbClass.getPropGroupWithName(conn,"default");
            String tid = Integer.toString(e.getTeacherId());
            // the prop group is a set of questions that we want to ask the user (gender, race, etc).
            // When a class is first created, we use the default prop group.
            // We'll allow it to be altered in the page that allows the user to edit the class fields.
            newid = DbClass.insertClass(conn,className, school, schoolYear, town, section,tid,
                    defaultPropGroup, 0);
            if (newid != -1) {
                req.setAttribute("formSubmissionEvent","AdminSubmitSelectedPedagogies");
                req.setAttribute("pedagogies", DbClassPedagogies.getClassPedagogyBeans(conn,newid));
                req.setAttribute("classId",newid);
                req.setAttribute("teacherId",e.getTeacherId());
                ClassInfo info = DbClass.getClass(conn,newid);
                ClassInfo[] classes = DbClass.getClasses(conn,info.getTeachid());
                Classes bean = new Classes(classes);
                req.setAttribute("bean", bean);
                req.setAttribute("classInfo", info);
                
                req.getRequestDispatcher(SELECT_PEDAGOGIES_JSP).forward(req,resp);
                return null;
            }
            else {
                req.setAttribute("&teacherId",e.getTeacherId());
                req.setAttribute("message","Failed to add class.  That class already exists");
                req.getRequestDispatcher(NOCLASS_JSP).forward(req,resp);
                return null;
            }
        }
    }

    private boolean validateYear (String yr) {
        try {
            int y = Integer.parseInt(yr);
            if (y < 2000 || y > 3000)      // optimistic software lifetime
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    // generates the next page after the pedagogy selection page (selectPretest.jsp)
    public static void generateValidPedagogySubmissionNextPage (Connection conn, int classId,
                                                           HttpServletRequest req,
                                                           HttpServletResponse resp) throws SQLException, IOException, ServletException {
            ClassInfo info = DbClass.getClass(conn,classId);
            req.setAttribute("formSubmissionEvent","AdminSubmitSelectedPretest");
            req.setAttribute("classInfo",info);
            req.setAttribute("classId",classId);
            ClassInfo[] classes = DbClass.getClasses(conn,info.getTeachid());
            Classes bean = new Classes(classes);
            req.setAttribute("bean", bean);
            req.setAttribute("teacherId",info.getTeachid());
            req.setAttribute("selectedPool",-1); // on new class no pool is selected
            List<PretestPool> pools = DbPrePost.getAllPretestPools(conn);
            req.setAttribute("pools", pools);
            req.getRequestDispatcher(SELECT_PRETEST_POOL_JSP).forward(req,resp);
       }

 

}