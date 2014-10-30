package edu.umass.ckc.wo.log;

import edu.umass.ckc.wo.event.NavigationEvent;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.tutor.response.HintResponse;
import edu.umass.ckc.wo.tutor.response.ProblemResponse;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutor.response.InterventionResponse;
import edu.umass.ckc.wo.tutor.response.AttemptResponse;
import edu.umass.ckc.wo.tutor.response.ExampleResponse;
import edu.umass.ckc.wo.tutor.response.HintSequenceResponse;
import edu.umass.ckc.wo.content.Hint;

import java.sql.*;
import java.util.List;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Jan 29, 2009
 * Time: 1:56:20 PM
 */
public class TutorLogger {
    private SessionManager smgr;
    Connection conn;
    public static final String EventLog = "EventLog";
    public static final String OldEventLog = "EpisodicData2";

    public static final String[] TUTOR_HUT_EVENTS = {  "BeginProblem", "NextProblem", "EndProblem", "BeginIntervention",
            "EndIntervention", "InputResponse", "Attempt",


    };





    public TutorLogger (SessionManager smgr) {
        this.smgr = smgr;
        this.conn = smgr.getConnection();

    }

    private int getTopic () {
        if (smgr.getStudentState().getStudentSelectedTopic() != -1)
            return  smgr.getStudentState().getStudentSelectedTopic();
        else return smgr.getStudentState().getCurTopic();
    }


    public int clearSessionLog (int sessNum, String eventTable) throws SQLException {
        return clearSessionLog(this.conn,sessNum,eventTable);
    }

    public static int clearSessionLog (Connection conn, int sessNum, String eventTable) throws SQLException {
        String q = "delete from "+eventTable+" where sessNum=?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(q);
            ps.setInt(1,sessNum);
            int n = ps.executeUpdate();
            return n;
        } finally {
            if (ps != null)
                ps.close();
        }
    }

    public int clearSessionLog (int sessNum) throws SQLException {
        return clearSessionLog(sessNum,EventLog) +
            clearSessionLog(sessNum,OldEventLog);
    }

    public int insertLogEntry(String action, String userInput, boolean isCorrect, long elapsedTime,
                              long probElapsed, String hintStep, int hintId, String emotion,
                              String activityName, int auxId, String auxTable, int curTopicId) throws Exception {

        return insertLogEntryWorker(smgr.getStudentId(), smgr.getSessionNum(), action, userInput, isCorrect, elapsedTime, probElapsed,
                smgr.getStudentState().getCurProblem(), hintStep, hintId, emotion, activityName, auxId, auxTable, curTopicId);
    }

    public int insertLogEntry(String action, String userInput, boolean isCorrect, long elapsedTime,
                              long probElapsed, String hintStep, int hintId, String emotion, String activityName, int curTopicId) throws Exception {

        return insertLogEntryWorker(smgr.getStudentId(), smgr.getSessionNum(), action, userInput, isCorrect, elapsedTime, probElapsed,
                smgr.getStudentState().getCurProblem(), hintStep, hintId, emotion, activityName, -1, null, curTopicId);
    }

    public int insertLogEntry(String action, int probId, String userInput, boolean isCorrect, long elapsedTime,
                              long probElapsed, String hintStep, int hintId, String emotion, String activityName, int curTopicId) throws Exception {

        return insertLogEntryWorker(smgr.getStudentId(), smgr.getSessionNum(), action, userInput, isCorrect, elapsedTime, probElapsed,
                probId, hintStep, hintId, emotion, activityName, -1, null, curTopicId);
    }

    private int getDummyProbId() throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select id from problem where form='dummy'";
            stmt = conn.prepareStatement(q);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int c= rs.getInt(1);
                return c;
            }
            throw new SQLException("The dummy problem is missing from the database.   There must be a problem with form=dummy to support events that do not reference a legal problem.");
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }

    public void newSession (int studId, int sessNum, long elapsedTime) throws SQLException {
        this.insertLogEntryWorker(studId, sessNum, "NewSession", "", false, elapsedTime, 0, -1, null, -1, null, null, -1, null, -1);
    }


    public int insertLogEntryWorker(int studId, int sessNum, String action, String userInput, boolean isCorrect, long elapsedTime,
                                    long probElapsed, int probId, String hintStep, int hintId, String emotion, String activityName,
                                    int auxId, String auxTable, int curTopicId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (probId < 1)
                probId = getDummyProbId();
            String q = "insert into " + EventLog + " (studId, sessNum, action, userInput, isCorrect, elapsedTime, probElapsed, problemId, hintStep, " +
                    "hintid, emotion, activityName, auxId, auxTable,time,curTopicId) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, studId);
            ps.setInt(2, sessNum);
            ps.setString(3, action);

            if (userInput == null)
                ps.setNull(4,Types.VARCHAR);
            else ps.setString(4,userInput.substring(0,Math.min(userInput.length(),1500))); // column can only store 1500 chars
            ps.setBoolean(5,isCorrect);
            ps.setLong(6,elapsedTime);
            ps.setLong(7,probElapsed);
            ps.setInt(8,probId);
            if (hintStep == null)
                ps.setNull(9,Types.VARCHAR);
            else ps.setString(9,hintStep);
            if (hintId == -1)
                ps.setNull(10,Types.INTEGER);
            else ps.setInt(10,hintId);
            if (emotion == null)
                ps.setNull(11, Types.VARCHAR);
            else ps.setString(11,emotion);
            if (activityName == null)
                ps.setNull(12,Types.VARCHAR);
            else ps.setString(12,activityName);
            if (auxId == -1)
                ps.setNull(13, Types.INTEGER);
            else ps.setInt(13,auxId);
            if (auxTable == null)
                ps.setNull(14,Types.VARCHAR);
            else ps.setString(14,auxTable);
            ps.setTimestamp(15,new Timestamp(System.currentTimeMillis()));
            if (curTopicId > 0)
                ps.setInt(16,curTopicId);
            else ps.setNull(16,Types.INTEGER);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            int newId = rs.getInt(1);
            return newId;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        finally {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        }
    }

    // Log My Progress Page entries
    public void logMPP (NavigationEvent e) throws Exception {
        insertLogEntry(RequestActions.MPP,null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),null,-1,null,"mpp", getTopic());
    }

    public void logHintRequest(IntraProblemEvent e, HintResponse hr) throws Exception {
        insertLogEntry(RequestActions.HINT,null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),
                hr.getHint().getLabel(),hr.getHint().getId(),hr.getCharacterControl(),null,getTopic());
    }



    public void logHintRequestIntervention (IntraProblemEvent e, InterventionResponse r) throws Exception {
        insertLogEntry(RequestActions.HINT,null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),
                null,-1,r.getCharacterControl(),r.logEventName(),getTopic());
    }


    private void logHelpRequest(IntraProblemEvent e, HintResponse hr, String requestType) throws Exception {
        insertLogEntry(requestType,null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),
                hr.getHint().getLabel(),hr.getHint().getId(),hr.getCharacterControl(),hr.logEventName(), getTopic());
    }


    public void logShowExample(ShowExampleEvent e, ExampleResponse r) throws Exception {
        insertLogEntry(RequestActions.SHOW_EXAMPLE,null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),
                e.getProbElapsedTime(),
                null,-1,r.getCharacterControl(),r.logEventName(),getTopic());
    }

    public void logShowVideoTransaction(ShowVideoEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.SHOW_VIDEO,null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),
                e.getProbElapsedTime(),
                null,-1,r.getCharacterControl(),r.logEventName(),getTopic());
    }

    public void logShowSolveProblem(ShowSolveProblemEvent e, HintSequenceResponse hr) throws Exception {
        String hintNamesCSV = hr.getHintNamesCSV();
        List<Hint> hints = hr.getHintSequence();
        int hintId = -1;
        if (hints.size() > 0)
                hintId = hints.get(0).getId();
        // TODO EntryLog hintId column only permits a single integer.  Here we have multiple ids for each hint, so we just store first.
        insertLogEntry(RequestActions.SHOW_SOLVE_PROBLEM,null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),
                hintNamesCSV,hintId,hr.getCharacterControl(),hr.logEventName(),getTopic());
    }

    public void logNextProblem(NextProblemEvent e, ProblemResponse r, int topicId) throws Exception {
        int probId;
        // when it's the first problem in the session, we want the nextProblem event to have no prob id
        // so we put in -1.
        if (smgr.getStudentState().getNumProblemsThisTutorSession() > 1)
            insertLogEntry(RequestActions.NEXT_PROBLEM,smgr.getStudentState().getCurProblem(), null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),
                null,-1,r.getCharacterControl(),r.logEventName(), topicId);
    }

    public void logNextProblem(NextProblemEvent e, String lcClip) throws Exception {

        insertLogEntry(RequestActions.NEXT_PROBLEM,smgr.getStudentState().getCurProblem(), null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),
                null,-1,lcClip,null, smgr.getStudentState().getCurTopic());
    }

    public void logNextProblemIntervention (NextProblemEvent e, InterventionResponse r) throws Exception {
        insertLogEntry(RequestActions.NEXT_PROBLEM,null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),
                null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }

    public void logAttempt(AttemptEvent e, AttemptResponse r) throws Exception {
        String curHint = smgr.getStudentState().getCurHint();
        insertLogEntry(RequestActions.ATTEMPT,e.getUserInput(),r.isCorrect(),e.getElapsedTime(),e.getProbElapsedTime(),
                (curHint!= null && curHint.equals("0")) ? null : curHint ,smgr.getStudentState().getCurHintId(),r.getCharacterControl(),null, getTopic());
    }


    public void logAttemptIntervention(AttemptEvent e, InterventionResponse r) throws Exception {
        insertLogEntry(RequestActions.ATTEMPT,e.getUserInput(),smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),
                smgr.getStudentState().getCurHint(),smgr.getStudentState().getCurHintId(),r.getCharacterControl(),r.logEventName(), getTopic());
    }

    public void logBeginProblem(BeginProblemEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.BEGIN_PROBLEM,null,false,e.getElapsedTime(),0,null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }

    public void logEndProblem(EndProblemEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.END_PROBLEM,smgr.getStudentState().getCurProblem(),null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());


        // If the EndProblem event is coming after a NextProblem event that selected an intervention,
        // we want to use the curProb ID (bc a new problem has not yet been selected); o/w we use the 
        // last prob Id since a new problem Id has been selected
//        if (smgr.getStudentState().getInBtwProbIntervention())
//            insertLogEntryWorker(RequestActions.END_PROBLEM,smgr.getStudentState().getCurProblem(),null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName());
//        else
//            insertLogEntryWorker(RequestActions.END_PROBLEM,smgr.getStudentState().getLastProblem(),null,smgr.getStudentState().isProblemSolved(),e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName());
    }

    public void logContinue(ContinueEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.CONTINUE,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }
    public void logContinueNextProblemIntervention (ContinueNextProblemInterventionEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.CONTINUE,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }
    public void logContinueAttemptIntervention(ContinueAttemptInterventionEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.CONTINUE,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }

    public void logInputResponse(InputResponseEvent e, Response r) throws Exception {
        AuxilaryEventLogger auxLogger = e.getAuxInfo();

        int auxId= -1;
        String auxTable=null;
        if (auxLogger != null) {
            auxId=auxLogger.logEntry(conn);
            auxTable = auxLogger.getAuxTable();
        }
        insertLogEntry(RequestActions.INPUT_RESPONSE,e.getUserInput(),false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,
                r.getCharacterControl(),r.logEventName(),auxId,auxTable, getTopic());
    }

    public void logInputResponseNextProblemIntervention(InputResponseNextProblemInterventionEvent e, Response r) throws Exception {
        logInputResponse(e,r);
    }
    public void logInputResponseAttemptIntervention(InputResponseAttemptInterventionEvent e, Response r) throws Exception {
        logInputResponse(e,r);
    }

    public void logClickCharacter(ClickCharacterEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.CLICK_CHARACTER,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }

    public void logEliminateCharacter(EliminateCharacterEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.ELIMINATE_CHARACTER,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }
    public void logShowCharacter(ShowCharacterEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.SHOW_CHARACTER,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(),getTopic());
    }

    public void logMuteCharacter(MuteCharacterEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.MUTE_CHARACTER,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }
    public void logUnMuteCharacter(UnMuteCharacterEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.UN_MUTE_CHARACTER,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }

    public void logReadProblem(ReadProblemEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.READ_PROBLEM,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }

    public void logShowIntervention(BeginInterventionEvent e, Response r, String intervention) throws Exception {
        insertLogEntry(RequestActions.SHOW_INTERVENTION,null,false,e.getElapsedTime(),0,null,-1,r.getCharacterControl(),intervention, getTopic());
    }

    public void logEndIntervention(EndInterventionEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.END_INTERVENTION,null,false,e.getElapsedTime(),0,null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }

    public void logBeginExample(BeginExampleEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.BEGIN_EXAMPLE,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }

    public void logEndExample(EndExampleEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.END_EXAMPLE,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,r.getCharacterControl(),r.logEventName(), getTopic());
    }






    public void logBeginExternalActivity(BeginExternalActivityEvent e, Response r) throws Exception {
        if (!hasOpenBegin(conn, e))
            insertLogEntry(RequestActions.BEGIN_XACT,e.getXactId(),null,false,e.getElapsedTime(),0,null,-1,null,r.logEventName(), getTopic());
    }

    /**
     * In the external activity page, the user can go to the My Progress Page and then use the "back" button to return to
     * the external activity page.   Unfortunately, this runs the JQuery document.ready function which then sends BeginExternalActivity
     * which is a duplicate of the one that was sent prior to going to MPP.  So this only logs BeginExternalActivity events if there
     * isn't a similar one already in the log with an associated EndExternalActivity after it.
     * @param conn
     * @param e
     * @return
     */
    private boolean hasOpenBegin(Connection conn, BeginExternalActivityEvent e) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select action from eventlog where (action='EndExternalActivity' or action='BeginExternalActivity') " +
                    "and problemId=? and sessNum=? order by id desc";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,e.getXactId());
            stmt.setInt(2,e.getSessionId());
            rs = stmt.executeQuery();
            // if we hit an End before a begin for the current problem, then things are correct ; o/w we already have an open Begin
            while (rs.next()) {
                String act = rs.getString(1);
                if (act.equals("EndExternalActivity"))
                    return false;
                else return true;
            }
            return false;
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }

    public void logEndExternalActivity(EndExternalActivityEvent e, Response r) throws Exception {
        insertLogEntry(RequestActions.END_XACT,e.getXactId(),null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,null,r.logEventName(), getTopic());

    }

    public void logMPPEvent(MPPTopicEvent e, int probId) throws Exception {
        String action = e.getAction();
        insertLogEntryWorker(smgr.getStudentId(),smgr.getSessionNum(),action,e.getUserInput(),false,e.getElapsedTime(),0,
                probId,null,-1,null,null, -1, null, e.getTopicId());

    }

    public void logReportedError(ReportErrorEvent e) throws Exception {
        String action = e.getAction();
        insertLogEntry(action,e.getMessage(),false,e.getElapsedTime(),0,null,-1,null,null,getTopic());
    }

    public void logShowInstructions(ShowInstructionsEvent e, Response r) throws Exception {
        insertLogEntry("ShowInstructions",null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,null,null,smgr.getStudentState().getCurTopic());
    }

    public void logIntraProblemEvent (IntraProblemEvent e, String action, Response r) throws Exception {
        insertLogEntry(action,null,false,e.getElapsedTime(),e.getProbElapsedTime(),null,-1,null,null,smgr.getStudentState().getCurTopic());
    }

    public void logHomeEvent(HomeEvent e) throws Exception {
        insertLogEntry(RequestActions.HOME,null,false,e.getElapsedTime(),0,null,-1,null,null,-1,null,-1);
    }
}
