package edu.umass.ckc.wo.admin;

import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorParam;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;



/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Dec 19, 2007
 * Time: 4:22:53 PM    kk
 */
public class PedagogyParser {
    private String defaultClasspath;
    private List<Pedagogy> pedagogies;

    public static final String DEFAULT_PROBLEM_SELECTOR = "BaseTopicProblemSelector";
    public static final String DEFAULT_HINT_SELECTOR = "PercentageHintSelector";
    public static final String DEFAULT_PEDAGOGICAL_MODEL = "BasePedagogicalModel";
    public static final String DEFAULT_STUDENT_MODEL = "BaseStudentModel";
    public static final String DEFAULT_REVIEW_MODE_PROBLEM_SELECTOR = "ReviewModeProblemSelector";
    public static final String DEFAULT_CHALLENGE_MODE_PROBLEM_SELECTOR = "ChallengeModeProblemSelector";

    public PedagogyParser(InputStream str) throws ClassNotFoundException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, DataConversionException {
//        File f = new File(filename);
        Document d = makeDocument(str);
        pedagogies = readPedagogies(d);
    }



    /**
     *
     * @return  a List of Pedagogy objects created from the pedagogies.xml file
     */
    public List<Pedagogy> getPedagogies() {
        return pedagogies;
    }


    public List<Pedagogy> readPedagogies (Document xmlDoc) throws NoSuchMethodException, DataConversionException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException {
        Element root = xmlDoc.getRootElement();
        List<Pedagogy> result= new ArrayList<Pedagogy>();
        List children = root.getChildren("pedagogy");
        for (int i = 0; i < children.size(); i++) {
            Element element = (Element) children.get(i);
            result.add(readPed(element));
        }
        return result;
        }



    private Pedagogy readPed(Element pedElt) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, DataConversionException {
        Element e;
        Pedagogy p = new Pedagogy();
        boolean isDefault=false;
        Attribute defaultAttr = pedElt.getAttribute("default");
        if (defaultAttr != null)
            p.setDefault(defaultAttr.getBooleanValue());
        e = pedElt.getChild("pedagogicalModelClass");
        // by default we build a BasePedagogicalModel unless a class is provided.
        String pedModClass = DEFAULT_PEDAGOGICAL_MODEL;
        if (e != null)
            pedModClass = e.getValue();
        p.setPedagogicalModelClass(pedModClass);

        e = pedElt.getChild("id");
        String id = e.getValue();
        p.setId(id);

        e = pedElt.getChild("name");
        String name = e.getValue();
        p.setName(name);

        e = pedElt.getChild("comment");
        if (e != null)  {
            String comment = e.getValue();
            p.setComment(comment);
        }

        e = pedElt.getChild("studentModelClass");
        if (e != null) {
            String smClass = e.getValue();
            p.setStudentModelClass(smClass);
        }
        else p.setStudentModelClass(DEFAULT_STUDENT_MODEL);

        e = pedElt.getChild("problemSelectorClass");
        if (e != null) {
            String psClass = e.getValue();
            p.setProblemSelectorClass(psClass);
        }
        else p.setProblemSelectorClass(DEFAULT_PROBLEM_SELECTOR);

        e = pedElt.getChild("reviewModeProblemSelectorClass");
        if (e != null) {
            String psClass = e.getValue();
            p.setReviewModeProblemSelectorClass(psClass);
        }
        else p.setReviewModeProblemSelectorClass(DEFAULT_REVIEW_MODE_PROBLEM_SELECTOR);

        e = pedElt.getChild("challengeModeProblemSelectorClass");
        if (e != null) {
            String psClass = e.getValue();
            p.setChallengeModeProblemSelectorClass(psClass);
        }
        else p.setChallengeModeProblemSelectorClass(DEFAULT_CHALLENGE_MODE_PROBLEM_SELECTOR);

        e = pedElt.getChild("hintSelectorClass");
        if (e != null) {
            String hClass = e.getValue();
            p.setHintSelectorClass(hClass);
        }
        else p.setHintSelectorClass(DEFAULT_HINT_SELECTOR);
        e = pedElt.getChild("interventions");
        if (e != null) {
            p.setInterventionsElement(e);
        }
        e = pedElt.getChild("lessonControl");
        if (e != null) {
            p.setLessonControlElement(e);
        }

        e = pedElt.getChild("nextProblemInterventionSelector");
        if (e != null)
            readNextProblemInterventionSelectors(p,e);
        e = pedElt.getChild("attemptInterventionSelector");
        if (e != null)
            readAttemptInterventionSelectors(p,e);
        e = pedElt.getChild("controlParameters");
        // get the default params
        PedagogicalModelParameters params = new PedagogicalModelParameters();
        // if user provided some, overwrite the individual settings
        if (e != null)
            readControlParams(params, e);
        p.setParams(params);
        e = pedElt.getChild("package");
        String packg = null ;
        if ( e != null )
            packg = e.getValue();
        p.setPackg(packg);

        e = pedElt.getChild("learningCompanionClass");
        if ( e != null )
            p.setLearningCompanionClass(e.getValue());

        return p;

    }

    // Given a set of pm params that have default settings this reads the XML config and
    // overwrites any that are provided.
    private void readControlParams(PedagogicalModelParameters params, Element p) {

        Element c = p.getChild("maxTimeInTopicSecs");
        String s;
        if (c != null) {
            s = c.getValue();
            int maxTimeSecs = Integer.parseInt(s);
            params.setMaxTimeInTopicSecs(maxTimeSecs);
        }

        c = p.getChild("lessonStyle");
        if (c != null) {
            s = c.getValue();
            params.setLessonStyle(s);
        }

        c = p.getChild("contentFailureThreshold");
        if (c != null) {
            s = c.getValue();
            int contentFailureThreshold = Integer.parseInt(s);
            params.setContentFailureThreshold(contentFailureThreshold);
        }

        c = p.getChild("topicMastery");
        if (c != null) {
            s = c.getValue();
            double topicMastery = Double.parseDouble(s);
            params.setTopicMastery(topicMastery);
        }

        c = p.getChild("minNumberProbs");
        if (c != null) {
            s = c.getValue();
            int minNumberProbs = Integer.parseInt(s);
            params.setMinNumberProbs(minNumberProbs);
        }

        c = p.getChild("maxNumberProbs");
        if (c != null) {
            s = c.getValue();
            int maxNumberProbs = Integer.parseInt(s);
            params.setMaxNumberProbs(maxNumberProbs);
        }

        c = p.getChild("minTimeInTopicSecs");
        if (c != null) {
            s = c.getValue();
            int minTimeInTopicSecs = Integer.parseInt(s);
            params.setMinTimeInTopicSecs(minTimeInTopicSecs);
        }

        c = p.getChild("difficultyRate");
        if (c != null) {
            s = c.getValue();
            int difficultyRate = Integer.parseInt(s);
            params.setDifficultyRate(difficultyRate);
        }

        c = p.getChild("externalActivityTimeThresholdMins");
        if (c != null) {
            s = c.getValue();
            int externalActivityTimeThresholdMins = Integer.parseInt(s);
            params.setExternalActivityTimeThreshold(externalActivityTimeThresholdMins);
        }

        c = p.getChild("showTopicIntro");
        if (c != null) {
            s = c.getValue();
            boolean showTopicIntro = Boolean.parseBoolean(s);
            params.setShowTopicIntro(showTopicIntro);
        }

        c = p.getChild("showExampleFirst");
        if (c != null) {
            s = c.getValue();
            boolean showExampleFirst = Boolean.parseBoolean(s);
            params.setShowExampleFirst(showExampleFirst);
        }
        // this will replace showTopicIntro
        c = p.getChild("topicIntroFrequency");
        if (c != null) {
            s = c.getValue();
            params.setTopicIntroFrequency(s);
        }

        // this will replace showExampleFirst
        c = p.getChild("topicExampleFrequency");
        if (c != null) {
            s = c.getValue();
            params.setTopicExampleFrequency(s);
        }
        c = p.getChild("problemReuseIntervalSessions");
        if (c != null) {
            s = c.getValue();
            params.setProblemReuseIntervalSessions(s);
        }
        c = p.getChild("problemReuseIntervalDays");
        if (c != null) {
            s = c.getValue();
            params.setProblemReuseIntervalDays(s);
        }
        c = p.getChild("displayMyProgressPage");
        if (c != null) {
            s = c.getValue();
            boolean showMpp = Boolean.parseBoolean(s);
            params.setShowMPP(showMpp);
        }

    }

    private void readAttemptInterventionSelectors(Pedagogy p, Element e) {
        InterventionSelectorSpec spec = parseSelector(e);
        p.setAttemptInterventionSelector(spec);
        List<Element> subSelectors = e.getChildren("attemptInterventionSelector");
        List<InterventionSelectorSpec> subs = new ArrayList<InterventionSelectorSpec>();
        for (Element elt : subSelectors) {
            subs.add(parseSelector(elt));
        }
        p.setSubAttemptInterventionSelectors(subs);

    }

    private void readNextProblemInterventionSelectors(Pedagogy p, Element e) {
        InterventionSelectorSpec spec = parseSelector(e);
        p.setNextProblemInterventionSelector(spec);
        List<Element> subSelectors = e.getChildren("nextProblemInterventionSelector");
        List<InterventionSelectorSpec> subs = new ArrayList<InterventionSelectorSpec>();
        for (Element elt : subSelectors) {
            subs.add(parseSelector(elt));
        }
        p.setSubNextProblemInterventionSelectors(subs);
    }

    private InterventionSelectorSpec parseSelector (Element elt) {
        String className = elt.getAttributeValue("class");
        String selectProblem = elt.getAttributeValue("selectProblem");
        List<InterventionSelectorParam> paramSpecs = new ArrayList<InterventionSelectorParam>();
        List<Element> params = elt.getChildren("param");
        for (Element param: params) {
            String name = param.getAttributeValue("name");
            String value = param.getValue();
            InterventionSelectorParam pSpec = new InterventionSelectorParam(name,value);
            paramSpecs.add(pSpec);
        }
        // We want this intervention to show at the same time as a problem, so the selectProblem flag tells the PM
        // to return a problem that contains the intervention
        InterventionSelectorParam selectParam = new InterventionSelectorParam("selectProblem", selectProblem);
        paramSpecs.add(selectParam);
        Element config = elt.getChild("config");
        InterventionSelectorSpec spec  = new  InterventionSelectorSpec(className,paramSpecs, config);
        return spec;
    }


    /**
     * Make a JDOM Document out of the file.
     * @param str
     * @return  JDOM Document
     */
    public Document makeDocument (InputStream str) {

        SAXBuilder parser = new SAXBuilder();
        try {
            Document doc = parser.build(str);
            Element root = doc.getRootElement();
            this.defaultClasspath = root.getAttribute("defaultClasspath").getValue();
            return doc;
        } catch (JDOMException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

}
