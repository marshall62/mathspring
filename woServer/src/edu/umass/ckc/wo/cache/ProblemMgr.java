package edu.umass.ckc.wo.cache;

import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.content.*;
import edu.umass.ckc.wo.db.DbVideo;
import edu.umass.ckc.wo.tutormeta.ExampleSelector;
import edu.umass.ckc.wo.tutormeta.VideoSelector;
import edu.umass.ckc.wo.db.DbHint;
import edu.umass.ckc.wo.db.DbUtil;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.tutor.probSel.BaseExampleSelector;
import edu.umass.ckc.wo.tutor.vid.BaseVideoSelector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;


import edu.umass.ckc.wo.util.Pair;
import edu.umass.ckc.wo.util.TwoTuple;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: Sep 24, 2009
 * Time: 2:40:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProblemMgr {

    private static final Logger logger = Logger.getLogger(ProblemMgr.class);

    private static ArrayList<Integer> problemIds;
    private static Map<Integer, Problem> allProblems;
    private static Map<Integer, ArrayList<Integer>> probIdsByTopic;
    private static Map<Integer, Set<CCStandard>> stdsByTopic;
    private static List<Topic> allTopics;
    private static ExampleSelector exSel;
    private static VideoSelector vidSel;
    private static int[] topicIds;
    private static boolean loaded=false;

    //This is very bad; this class is written like an abstract static container,
    // but this is treating it like a normal instanced class... which is using
    // the static members as instance variables???
    //But I am afraid to touch it because it's used in 10 other places...
    public ProblemMgr(ExampleSelector exampleSelector, VideoSelector videoSelector) {
        if (!loaded) {
            exSel = exampleSelector;
            vidSel = videoSelector;
            problemIds = new ArrayList<Integer>();
            allProblems = new HashMap<Integer, Problem>();
            allTopics = new ArrayList<Topic>();
        }
    }

    public static boolean isLoaded () {
        return loaded;
    }

    public static synchronized void loadProbs(Connection conn) throws Exception {
        if (!loaded) {
            loaded = true;
            problemIds = new ArrayList<Integer>();
            allProblems = new HashMap<Integer, Problem>();
            allTopics = new ArrayList<Topic>();
            probIdsByTopic = new HashMap<Integer,ArrayList<Integer>>();
            stdsByTopic = new HashMap<Integer,Set<CCStandard>>();
            loadTopics(conn);
            loadAllProblems(conn);
            fillTopicProblemMap(conn);
            fillTopicStandardMap(conn);
        }
    }

    public static int getTopicProblemCount (int topicId) {
        ArrayList<Integer> probs = probIdsByTopic.get(topicId);
        if (probs != null)
            return probs.size();
        else return -1;
    }

    public static void dumpCache () {
        loaded = false;
    }

//    private List<TopicEntity> loadTopics2 () {
//        Session sess = HibernateUtil.getSessionFactory().openSession();
//        Transaction tx;
//        try {
//            tx = sess.beginTransaction();
//            Query q =  sess.createQuery("from TopicEntity where active=1");
//            List<TopicEntity> l = q.list();
//            return l;
//        }
//        finally {
//            sess.close();
//        }
//    }

    private static void loadTopics(Connection conn) throws SQLException {
//        List<TopicEntity> topicEntities = loadTopics2();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String q = "select id, description, summary from problemgroup where active=1";
            ps = conn.prepareStatement(q);
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String description = rs.getString("description");
                String summary = rs.getString("summary");
                allTopics.add(new Topic(id, description,summary));
            }
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();

        }
    }

    public static Topic getTopic(int id)
    {
        for (Topic t : allTopics)
        {
            if (t.getId() == id)
            {
                return t;
            }
        }
        return null;
    }

    public static HashMap<String, ArrayList<String>> getVarDomain(int id, Connection conn) throws SQLException {
        String s = "select p.name, p.values from ProblemParamSet p where problemID="+Integer.toString(id);
        PreparedStatement ps = conn.prepareStatement(s);
        ResultSet rs = ps.executeQuery();
        HashMap<String, ArrayList<String>> vars = new HashMap<String, ArrayList<String>>();
        String name = null;
        ArrayList<String> vals = null;
        try {
            while (rs.next()) {
                // prepend the variable name with $#.   Later plugging values into the problem will use $# to indicate where
                // variables are located.
                name = "$#"+rs.getString("name");    // DM 6/2/15 changed per Toms request
                vals = new ArrayList<String>(Arrays.asList(rs.getString("values").split(",")));
                vars.put(name, vals);
            }
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
        }
        return vars;
    }

    private static PreparedStatement buildProblemQuery(Connection conn, Integer problemId) throws SQLException {
        String problemFilter = problemId != null ? " and p.id = " + problemId : "";
        String s = "select p.id, answer, animationResource, p.name, nickname," +
                " strategicHintExists, hasVars, screenShotURL, diff_level, form," +
                " isExternalActivity, type, video, example, p.status, p.questType," +
                " statementHTML, imageURL, audioResource, units, problemFormat" +
                " from Problem p, OverallProbDifficulty o" +
                " where p.id=o.problemid" + problemFilter +
                " and (status='Ready' or status='ready' or status='testable')" +
                " order by p.id;";
        PreparedStatement ps = conn.prepareStatement(s);
        return ps;
    }

    private static Problem buildProblem(Connection conn, ResultSet rs) throws Exception {
        int id = rs.getInt(Problem.ID);
        String answer = rs.getString(Problem.ANSWER);
        String resource = rs.getString(Problem.ANIMATION_RESOURCE);
        String name = rs.getString(Problem.NAME);
        boolean stratHint = rs.getBoolean(Problem.HAS_STRATEGIC_HINT);
        boolean hasVars = rs.getBoolean(Problem.HAS_VARS);
        String pname = name;
        String nname = rs.getString(Problem.NICKNAME);
        String form = rs.getString(Problem.FORM);
        String instructions = null ;
        String type = rs.getString(Problem.TYPE) ;
        boolean isExternal = rs.getBoolean(Problem.IS_EXTERNAL_ACTIVITY);
        double diff = rs.getDouble("diff_level") ;
        int video = rs.getInt("video");
        if (rs.wasNull())
            video = -1;
        int exampleId = rs.getInt("example");
        if (rs.wasNull())
            exampleId = -1;
        String status = rs.getString("status");
        String t = rs.getString("questType");
        String statementHTML = rs.getString("statementHTML");
        String imgURL = rs.getString("imageURL");
        String audioRsc = rs.getString("audioResource");
        String units = rs.getString("units");
        String problemFormat = rs.getString("problemFormat");
        Problem.QuestType questType = Problem.parseType(t);
        HashMap<String, ArrayList<String>> vars = null;
        if (hasVars) {
            vars = getVarDomain(id, conn);
        }
        List<ProblemAnswer> answers =null;
        if (form != null && (questType == Problem.QuestType.shortAnswer || form.equals(Problem.QUICK_AUTH))) {
            answers = getAnswerValues(conn,id);
        }
        // perhaps its a short answer problem but not built with quickAuth
        else if (form == null && questType == Problem.QuestType.shortAnswer)
            answers = getAnswerValues(conn,id);

        String ssURL = rs.getString("screenShotURL");
        if (rs.wasNull())
            ssURL = null;
        Problem p = new Problem(id, resource, answer, name, nname, stratHint,
                diff, null, form, instructions, type, status, vars, ssURL,
                questType, statementHTML, imgURL, audioRsc, units, problemFormat);

        p.setExternalActivity(isExternal);
        List<Hint> hints = DbHint.getHintsForProblem(conn,id);
        p.setHasStrategicHint(stratHint);
        p.setHints(hints);
        List<CCStandard> standards = DbProblem.getProblemStandards(conn,id);
        p.setStandards(standards);
        List<Topic> topics = DbProblem.getProblemTopics(conn,id);
        p.setTopics(topics);
        // short answer problems have a list of possible answers.
        if (answers != null)
            p.setAnswers(answers);
        allProblems.put(p.getId(), p);
        if (exampleId == -1)
            exampleId = (exSel != null) ? exSel.selectProblem(conn,id) : -1;
        p.setExample(exampleId);

        String vidURL = null;
        // if video is given, it is the id of a row in the video table.
        if (video == -1)
            vidURL = (vidSel != null) ? vidSel.selectVideo(conn,id) : "";
        else {
            //
            Video v= DbVideo.getVideo(conn, video);
            vidURL = v.getUrl();
        }
        p.setVideo(vidURL);
        logger.debug("Problem id="+p.getId() + " name=" + p.getName() + " video="+ p.getVideo() + " example=" + p.getExample());
        return p;
    }

    private static void loadAllProblems(Connection conn) throws Exception {
        loadDefaultProblemFormat(conn);
        PreparedStatement ps = buildProblemQuery(conn, null); //query all problems
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                Problem p = buildProblem(conn, rs);
                problemIds.add(p.getId());
            }
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
        }
    }

    public static void reloadProblem(Connection conn, int problemId) throws Exception {
        if(!allProblems.containsKey(problemId)) {
            //Allowing for "re"loading an unloaded problem would require special handling
            //For now, just fail in this case rather than introduce subtle bugs
            throw new Exception("Problem " + problemId + " was not loaded in the first place.");
        }
        loadDefaultProblemFormat(conn);
        PreparedStatement ps = buildProblemQuery(conn, problemId);
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()) {
                Problem p = buildProblem(conn, rs);
//                allProblems.put(problemId, p);
            }
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
        }
    }

    private static void loadDefaultProblemFormat(Connection conn) throws SQLException {
        String query = "SELECT problemFormat FROM quickauthformattemplates WHERE id=1;";
        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()) {
                String template = rs.getString("problemFormat");
                Problem.defaultFormat = template;
            }
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
        }
    }

    private static List<ProblemAnswer> getAnswerValues(Connection conn, int id) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select a.val,a.choiceletter,a.bindingPosition,a.order from problemanswers a where a.probid=? order by a.order";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,id);
            rs = stmt.executeQuery();
            List<ProblemAnswer> answers = new ArrayList<ProblemAnswer>();
            while (rs.next()) {
                String v= rs.getString("a.val");
                String l= rs.getString("a.choiceLetter");
                int bn= rs.getInt("a.bindingPosition");
                if (rs.wasNull())
                     bn = -1;
                int order = rs.getInt("a.order");
                answers.add(new ProblemAnswer(v,l,null,true,id, bn, order));
            }
            return answers;
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }

    /**
     * Creates a mapping from topic ids to a list of CCStandards in that topic.
     * Creates this by going through the map of topics->Problem and requesting the standards in each problem.
     * It then puts these standards into a Set (relying on duplicate removal).   The topic is then mapped to this Set.
     * @param conn
     * @throws SQLException
     */
    public static void fillTopicStandardMap(Connection conn) throws SQLException {
        Set<Integer> topicIDs = probIdsByTopic.keySet();
        for (int topicID: topicIDs) {
            Set<CCStandard> topicStandards = new TreeSet<CCStandard>();
            List<Problem> probs = getTopicProblems(topicID);
            for (Problem p: probs) {
                List<CCStandard> probStandards = p.getStandards();
                for (CCStandard probStd: probStandards) {
                    topicStandards.add(probStd);  // counting on Set to remove dupes.
                }
            }
            Topic topic = getTopicFromID(topicID);
            if (topic == null)  {
                System.out.println("Failed to find topic: " + topicID);
                continue;
            }
            topic.setCcStandards(topicStandards);
            stdsByTopic.put(topicID,topicStandards);

        }
    }

    private static Topic getTopicFromID(int topicID) {
        for (Iterator<Topic> iterator = allTopics.iterator(); iterator.hasNext(); ) {
            Topic next = iterator.next();
            if (next.getId() == topicID)
            {
                return next;
            }
        }
        return null;
    }

    public static Set<CCStandard> getTopicStandards (int topicId) {
        return stdsByTopic.get(topicId);
    }

    // For each topic get all its problems in order of difficulty and insert them into the probIdsByTopic
    // Note this correctly takes care of problems that live in more than one topic.
    public static void fillTopicProblemMap(Connection conn) throws Exception {
        String q = "select p.id,t.id from problem p, OverallProbDifficulty d, " +
                "ProbProbGroup m, problemgroup t where p.id = m.probID and t.id=m.pgroupid and t.active=1 and d.problemId = p.id and (p.status='ready' or p.status='testable')" +
                " order by t.id, d.diff_level";
        PreparedStatement ps = conn.prepareStatement(q);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int probId = rs.getInt(1);
            int topicId = rs.getInt(2);
            if (probIdsByTopic.get(topicId) == null) {
                probIdsByTopic.put(topicId,new ArrayList<Integer>());
            }
            probIdsByTopic.get(topicId).add(probId);
        }
        saveTopics();
    }

    /**
     * After all the problems are loaded we extract the topics (from the keyset of the map) and put them in topic
     * array sorted by id.
     */
    public static void saveTopics () {
        Set<Integer> topics = probIdsByTopic.keySet(); // no order guaranteed
        // need to guarantee an order, so we sort it
        topicIds = new int[topics.size()];
        int i=0;
        for (int topicId: topics) {
            topicIds[i++] = topicId;
        }
        Arrays.sort(topicIds);
    }

    public static int[] getTopicIds () {
        return topicIds.clone();
    }

    // returns a clone of the List because the caller may destroy the
    // contents of the list.
    public static List<Integer> getTopicProblemIds (int topicId) {
        List<Integer> l = probIdsByTopic.get(topicId);
        if (l == null)
            return null;
        else
            return (List<Integer>) probIdsByTopic.get(topicId).clone();
    }

    public static List<Problem> getTopicProblems (int topicId) {
        if (probIdsByTopic.get(topicId) == null) return new ArrayList<Problem>();
        List<Problem> l = new ArrayList<Problem>(probIdsByTopic.get(topicId).size());
        for(int i : probIdsByTopic.get(topicId)) l.add(allProblems.get(i));
        return l;
    }

    public static List<Problem> getStandardProblems (Connection conn, String ccss) throws SQLException{
        String[] standards = ccss.split(",");
        List<Problem> problems = new ArrayList<Problem>();
        String q = "select p.id from problem p, OverallProbDifficulty d, " +
                "ProbStdMap std where p.id = std.probID and d.problemId = p.id and p.status='ready' and std.stdId in ";
        String vars = "(";
        //Start at 1 to get the right number of commas
        for(int i = 1; i < standards.length; i++){
            vars = vars + "?,";
        }
        q = q + vars + "?) order by d.diff_level";
        PreparedStatement ps = conn.prepareStatement(q);
        for(int i = 1; i <= standards.length; i++){
            ps.setString(i, standards[i-1]);
        }
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int probId = rs.getInt(1);
            problems.add(getProblem(probId));                        //add to arraylist
        }

        return problems;
    }

    public static int getStandardNumProblems (Connection conn, String ccss) throws SQLException{
        String[] standards = ccss.split(",");
        List<Problem> problems = new ArrayList<Problem>();
        String q = "select count(p.id) from problem p, OverallProbDifficulty d, " +
                "ProbStdMap std where p.id = std.probID and d.problemId = p.id and p.status='ready' and std.stdId in ";
        String vars = "(";
        //Start at 1 to get the right number of commas
        for(int i = 1; i < standards.length; i++){
            vars = vars + "?,";
        }
        q = q + vars + "?) order by d.diff_level";
        PreparedStatement ps = conn.prepareStatement(q);
        for(int i = 1; i <= standards.length; i++){
            ps.setString(i, standards[i-1]);
        }
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            int num = rs.getInt(1);
            return num;                        //add to arraylist
        }

        return 0;
    }

    public static List<Pair<String,Integer>> getAllStandardNumProblems(Connection conn) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            List<Pair<String,Integer>> res = new ArrayList<Pair<String,Integer>>();
            String q = "select m.stdid, count(m.probId) from probstdmap m, problem p where m.probid=p.id and p.status='ready' group by m.stdid";
            stmt = conn.prepareStatement(q);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String std= rs.getString(1);
                int num = rs.getInt(2);
                if (std != null)
                    res.add(new Pair(std,num));
            }
            return res;
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        } 
    }

    public static Problem getProblem(int id) throws SQLException {
        if (allProblems.size() == 0)
            throw new SQLException("The ProblemMgr has no loaded problems.  Make sure the correct servlet is being run so that it gets loaded");
        return allProblems.get(id);
    }

    public static List<Problem> getAllProblems() {
        List<Problem> copy = new ArrayList<Problem>();
        for(int id : problemIds) {
            copy.add(allProblems.get(id));
        }
        return copy;
    }

    public static Problem getProblemByName(String pname) {
        for (Problem p : allProblems.values()) {
            if (p.getName().equals(pname))
                return p;
        }
        return null;
    }

    private static boolean removeProblemFromList(List<Integer> l, int probId, String probName) {
        Iterator itr = l.iterator();
        while (itr.hasNext()) {
            Problem problem = allProblems.get((Integer) itr.next());
            if (probId == problem.getId() || (probName != null && probName.equals(problem.getName()))) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    //#rezecib: supporting the dual id/name indexing is really an antipattern
    // that leads to all sorts of weird scenarios. I believe we should remove that.
    /** A request to deactivate a problem has come from an administrator.   Remove the problem from
     * the allProblems cache and from the topic map.   Also set the problem to status=deactivated in the db. */
    public static void deactivateProblem(int probId, String probName, Connection conn) throws SQLException {
        if(probName != null) {
            Problem p = getProblemByName(probName);
            //previously it iterated through problems in order and removed the first one
            //that had a matching name OR id. So if both are defined we need the min id
            //otherwise, we can just take the defined one
            if(p != null) probId = probId > -1 ? Math.min(probId, p.getId()) : p.getId();
        }
        allProblems.remove(probId);
        Collection<ArrayList<Integer>> vals= probIdsByTopic.values();
        // search all topics (a problem can be in more than one) and remove it.
        for (List<Integer> l : vals) {
            removeProblemFromList(l,probId,probName); // get it out of the topic's bucket.
        }
        DbProblem.deactivateProblem(conn,probId,probName);
    }

    /**
     * Returns all the non-testable problems in a topic
     */
    public static List<Problem> getWorkingProblems (int topicId) {
        List<Problem> all = getTopicProblems(topicId);
        if (all == null || all.size() == 0)
            return new ArrayList<Problem>();
        List<Problem> some = new ArrayList<Problem>(all.size());
        for (Problem p : all) {
            if (p.isTestProblem())
                continue;
            some.add(p);
        }
        return some;
    }

    /**
     * Return whether the topic contains ready problems.   If the  includeTestableProblems is true,
     * it will return true if there are no ready problems but some testables.
     * @param topicId
     * @param includeTestableProblems
     * @return
     * @throws SQLException
     */
    public static boolean isTopicPlayable(int topicId, boolean includeTestableProblems) throws SQLException {
        List<Problem> probs = ProblemMgr.getTopicProblems(topicId);
        if (probs == null)
            return false;
        for (Problem p : probs) {
            // if a problems isn't testable it must be ready.
            if (!p.isTestProblem())
                return true;
            else if (includeTestableProblems && p.isTestProblem())
                return true;
        }
        return false;
    }




    public static void main(String[] args) {
        DbUtil.loadDbDriver();
        try {
            Connection c = DbUtil.getAConnection("localhost");
            ProblemMgr m = new ProblemMgr(new BaseExampleSelector(),new BaseVideoSelector());
            m.loadProbs(c);
            ProblemMgr.getTopicProblemIds(38);
//            System.out.println("Problem " + p);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public static List<Topic> getAllTopics () {
        return allTopics;
    }


    public static boolean isTestProb(int pid) throws SQLException {
        Problem p = getProblem(pid);
        if (p != null)
            return p.isTestProblem();
        else return false;
    }


}
