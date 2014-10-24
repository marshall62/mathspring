package edu.umass.ckc.wo.cache;

import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.content.CCStandard;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.content.Hint;
import edu.umass.ckc.wo.content.ProblemParameters;
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

    private static List<Problem> allProblems;
    private static Map<Integer,ArrayList<Problem>> probsByTopic;
    private static Map<Integer,Set<CCStandard>> stdsByTopic;
    private static List<Topic> allTopics;
    private static ExampleSelector exSel;
    private static VideoSelector vidSel;
    private static int[] topicIds;
    private static boolean loaded=false;

    public ProblemMgr(ExampleSelector exampleSelector, VideoSelector videoSelector) {
        if (!loaded) {
            this.exSel = exampleSelector;
            this.vidSel = videoSelector;
            this.allProblems = new ArrayList<Problem>();
            this.allTopics = new ArrayList<Topic>();
        }

    }

    public static boolean isLoaded () {
        return loaded;
    }


    public static synchronized void loadProbs (Connection conn) throws Exception {
        if (!loaded) {
            loaded = true;
            allProblems = new ArrayList<Problem>();
            probsByTopic = new HashMap<Integer,ArrayList<Problem>>();
            stdsByTopic = new HashMap<Integer,Set<CCStandard>>();
            loadTopics(conn);
            loadAllProblems(conn);
            fillTopicProblemMap(conn);
            fillTopicStandardMap(conn);
        }
    }

    public static void dumpCache () {
        loaded = false;
    }

//
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

    // This could probably be written better since it's just a single row
    public static String getVarDomain(int id, Connection conn) throws SQLException {
       String s = "select probId, varDomain from ProblemVarDomain where probId="+Integer.toString(id);
        PreparedStatement ps = conn.prepareStatement(s);
        ResultSet rs = ps.executeQuery();
        String vars = null;
        try {
            while (rs.next()) {
                vars = rs.getString("varDomain");
            }
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
        }
        return vars;
    }

    private static void loadAllProblems (Connection conn) throws Exception {
        String s = "select p.id, answer, animationResource,p.name,nickname,strategicHintExists,hasVars,screenShotURL"+
                ", diff_level, form, statementHTML, metainfo, isExternalActivity, type, video, example, p.status"  +
                " from Problem p, OverallProbDifficulty o" +
                " where p.id=o.problemid and (status='Ready' or status='ready' or status='testable') order by p.id";    // and p.id=v.problemid
        PreparedStatement ps = conn.prepareStatement(s);
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                int id = rs.getInt(1);
                String answer = rs.getString(2);
                String resource = rs.getString(3);

                String name = rs.getString(Problem.NAME);
                boolean stratHint = rs.getBoolean(Problem.HAS_STRATEGIC_HINT);
                boolean hasVars = rs.getBoolean(Problem.HAS_VARS);
                String pname = name;
                String nname = rs.getString(Problem.NICKNAME);
                String form = rs.getString(Problem.FORM);
                String instructions = rs.getString(Problem.INSTRUCTIONS) ;
                String type = rs.getString(Problem.TYPE) ;
                String metainfo = rs.getString(Problem.META_INFO);
                boolean isExternal = rs.getBoolean(Problem.IS_EXTERNAL_ACTIVITY);
                double diff = rs.getDouble("diff_level") ;
                String video = rs.getString("video");
                if (rs.wasNull())
                    video = null;
                int exampleId = rs.getInt("example");
                if (rs.wasNull())
                    exampleId = -1;
                String status = rs.getString("status");
                String vars = null;
                if (hasVars) {
                    vars = getVarDomain(id, conn);
                }
                String ssURL = rs.getString("screenShotURL");
                if (rs.wasNull())
                    ssURL = null;
                //                Problem p = new Problem(id, resource, answer, diff, name, nname,form,instructions,type);
                Problem p = new Problem(id, resource,answer,name,nname,stratHint,diff,null,form,instructions,type, status, vars, ssURL);

                // mark the problem as formality or external if it is not a regular wayang problem
                if (form != null && form.equalsIgnoreCase("formality"))
                    p.setFormalityProb(true);
                p.setExternalActivity(isExternal);
                List<Hint> hints = DbHint.getHintsForProblem(conn,id);
                p.setHasStrategicHint(stratHint);
                p.setHints(hints);
                List<CCStandard> standards = DbProblem.getProblemStandards(conn,id);
                p.setStandards(standards);
                List<Topic> topics = DbProblem.getProblemTopics(conn,id);
                p.setTopics(topics);
                allProblems.add(p );
                if (exampleId == -1)
                    exampleId = (exSel != null) ? exSel.selectProblem(conn,id) : -1;
                p.setExample(exampleId);
                if (video == null)
                    video = (vidSel != null) ? vidSel.selectVideo(conn,id) : "";
                p.setVideo(video);
                logger.debug("Problem id="+p.getId() + " name=" + p.getName() + " video="+ p.getVideo() + " example=" + p.getExample());
            }
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
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
        Set<Integer> topicIDs = probsByTopic.keySet();
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

    // For each topic get all its problems in order of difficulty and insert them into the probsByTopic
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
            ArrayList l = probsByTopic.get(topicId);
            if (probsByTopic.get(topicId) == null) {
                l = new ArrayList<Problem>();
                l.add(getProblem(probId));
                probsByTopic.put(topicId,l);
            }
            else l.add(getProblem(probId));
        }
        saveTopics();
    }

    /**
     * After all the problems are loaded we extract the topics (from the keyset of the map) and put them in topic
     * array sorted by id.
     */
    public static void saveTopics () {
        Set<Integer> topics = probsByTopic.keySet(); // no order guaranteed
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
    public static List<Problem> getTopicProblems (int topicId) {
        return (List<Problem>) probsByTopic.get(topicId).clone();
    }

    public static List<Integer> getTopicProblemIds (int topicId) {
        if (probsByTopic.get(topicId) == null)
            return new ArrayList<Integer>();
        List<Integer> l = new ArrayList<Integer>(probsByTopic.get(topicId).size());
        for (Problem p : probsByTopic.get(topicId))
            l.add(p.getId());
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


    // Problems were stored in the allProblems arrayList in order of ID (ascending) so that
    // we can do a lookup in log time with this.
    public static Problem getProblem (int id) throws SQLException {
        if (allProblems.size() == 0)
            throw new SQLException("The ProblemMgr has no loaded problems.  Make sure the correct servlet is being wrong so that it gets loaded");
        Problem temp = new Problem(id);
        int ix = Collections.binarySearch(allProblems,temp,
                new Comparator<Problem>() {
                    public final int compare( Problem p1, Problem p2 )
                    {
                        if (p1.getId() == p2.getId())
                            return 0;
                        else if (p1.getId() < p2.getId())
                            return -1;
                        else return 1;
                    }
                }
        );
        if (ix >= 0)
            return allProblems.get(ix);  // this better not be linear time...  since its ArrayList I assume not
       else return null;
    }

    public static List<Problem> getAllProblems() {
        List<Problem> temp = new ArrayList<Problem>(allProblems.size());
        for (Problem p: allProblems)
            temp.add(p);
        return temp;
    }

    public static Problem getProblemByName(String pname) {
        for (Problem p: allProblems) {
            if (p.getName().equals(pname))
                return p;
        }
        return null;
    }

    private static boolean removeProblemFromList (List<Problem> l, int probId, String probName) {
        Iterator itr = l.iterator();
        while (itr.hasNext()) {
            Problem problem = (Problem) itr.next();
            if (probId == problem.getId() || (probName != null && probName.equals(problem.getName()))) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    /** A request to deactivate a problem has come from an administrator.   Remove the problem from
     * the allProblems cache and from the topic map.   Also set the problem to status=deactivated in the db. */
    public static void deactivateProblem(int probId, String probName, Connection conn) throws SQLException {
        removeProblemFromList(allProblems,probId,probName);
        Collection<ArrayList<Problem>> vals= probsByTopic.values();
        // search all topics (a problem can be in more than one) and remove it.
        for (List<Problem> l: vals) {
            removeProblemFromList(l,probId,probName); // get it out of the topic's bucket.
        }
        new DbProblem().deactivateProblem(conn,probId,probName);     


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


    public static boolean isTestProb(int pid) throws SQLException {
        Problem p = getProblem(pid);
        if (p != null)
            return p.isTestProblem();
        else return false;
    }
}
