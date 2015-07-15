package edu.umass.ckc.wo.tutor;

import edu.umass.ckc.wo.config.LessonXML;
import edu.umass.ckc.wo.config.LoginXML;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import org.jdom.Element;

import java.util.List;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Dec 18, 2007
 * Time: 6:35:10 PM
 */
public class Pedagogy implements Comparable {

    public static final String defaultClasspath = "edu.umass.ckc.wo.tutor";
    private String problemSelectorClass;
    private String hintSelectorClass;
    private String studentModelClass;
    private String learningCompanionClass ;
    private String pedagogicalModelClass ;
    private String id;
    private String name;
    private String comment;
    private String packg;
    private boolean isDefault=false;
    private PedagogicalModelParameters params;
    // the main nextProblem intervention selector - the coordinator if subs provided
    private InterventionSelectorSpec nextProblemInterventionSelector;
    // sub nextProblem intervention selectors
    private List<InterventionSelectorSpec> subNextProblemInterventionSelectors;
    // the main attempt intervention selector - the coordinator of subs
    private InterventionSelectorSpec attemptInterventionSelector;
    // sub attempt intervention selectors
    private List<InterventionSelectorSpec> subAttemptInterventionSelectors;
    private String reviewModeProblemSelectorClass;
    private String challengeModeProblemSelectorClass;

    private Element interventionsElement;
    private String lessonName;
    private String loginXMLName;
    private LessonXML lessonXML;
    private LoginXML loginXML;
    private String simpleConfigName;


    public Pedagogy() {
    }

    public Pedagogy(String problemSelector, String hintSelector,
                    String studentModel, String learningCompanion, String pedModelClassname, String id,
                    String name, String comment, String defaultClasspath, boolean isDefault) {
        this.problemSelectorClass = getFullyQualifiedClassname(defaultClasspath+".probSel",problemSelector);
        this.hintSelectorClass = getFullyQualifiedClassname(defaultClasspath+".hintSel",hintSelector);
        this.studentModelClass = getFullyQualifiedClassname(defaultClasspath,studentModel);
        this.learningCompanionClass = getFullyQualifiedClassname(defaultClasspath+".agent",learningCompanion);
        this.pedagogicalModelClass = getFullyQualifiedClassname(defaultClasspath+".pedModel",pedModelClassname);
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.isDefault = isDefault;

    }

    public Pedagogy(String pedModClass, String id, String name, String comment, String defaultClasspath, boolean isDefault, String packg) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.isDefault = isDefault;
        this.pedagogicalModelClass = getFullyQualifiedClassname(defaultClasspath+"."+packg,pedModClass);
    }

    public String getPedagogicalModelClass() {
        return pedagogicalModelClass;
    }

    public void setPedagogicalModelClass(String pedagogicalModelClass) {
        this.pedagogicalModelClass = getFullyQualifiedClassname(defaultClasspath+".pedModel",pedagogicalModelClass);
    }

    public String getProblemSelectorClass() {
        return problemSelectorClass;
    }


    public void setProblemSelectorClass(String problemSelectorClass) {
        this.problemSelectorClass = getFullyQualifiedClassname(defaultClasspath+".probSel",problemSelectorClass);
    }



    public String getLearningCompanionClass() {
        return learningCompanionClass ;
    }

    public void setLearningCompanionClass(String learningCompanionClass) {
        this.learningCompanionClass = getFullyQualifiedClassname(defaultClasspath+".agent",learningCompanionClass);
    }

    public String getHintSelectorClass() {
        return hintSelectorClass;
    }

    public void setHintSelectorClass(String hintSelectorClass) {
        this.hintSelectorClass = getFullyQualifiedClassname(defaultClasspath+".hintSel",hintSelectorClass);
    }

    public String getStudentModelClass() {
        return studentModelClass;
    }

    public void setStudentModelClass(String studentModelClass) {
        this.studentModelClass = getFullyQualifiedClassname(defaultClasspath+".studmod",studentModelClass);
    }

    public boolean isDefault () {
        return isDefault;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }



    public String toString() {
        return id + ": " + name ;
    }

    /**
     * If the classname does not contain package paths, then prepend it with the classpath
     * Note:  This assumes that no one uses inner classes as the classname because this is simply
     * testing for the presence of a '.' to see if classname is a pure class name or a classpath.
     * @param defaultClasspath
     * @param classname
     * @return  the fully qualified classname
     */
    public static String getFullyQualifiedClassname (String defaultClasspath, String classname) {
        if (classname == null)
            return null;
        if (classname.indexOf('.') == -1)
            return defaultClasspath + "." + classname;
        else return classname;
    }




    public InterventionSelectorSpec getAttemptInterventionSelector() {
        return attemptInterventionSelector;
    }

    public void setAttemptInterventionSelector(InterventionSelectorSpec attemptInterventionSelector) {
        String className =   attemptInterventionSelector.getFullyQualifiedClassname();
        attemptInterventionSelector.setClassName(className);
        this.attemptInterventionSelector = attemptInterventionSelector;
    }

    public InterventionSelectorSpec getNextProblemInterventionSelector() {
        return nextProblemInterventionSelector;
    }

    public void setNextProblemInterventionSelector(InterventionSelectorSpec nextProblemInterventionSelector) {
        String className =  nextProblemInterventionSelector.getFullyQualifiedClassname();
        nextProblemInterventionSelector.setClassName(className);
        this.nextProblemInterventionSelector =  nextProblemInterventionSelector;
    }

    public List<InterventionSelectorSpec> getSubNextProblemInterventionSelectors() {
        return subNextProblemInterventionSelectors;
    }

    public void setSubNextProblemInterventionSelectors (List<InterventionSelectorSpec> selectorSpecs) {
        for (InterventionSelectorSpec spec: selectorSpecs) {
            String className =   getFullyQualifiedClassname(defaultClasspath + ".intervSel2", spec.getClassName());
            spec.setClassName(className);
        }
        this.subNextProblemInterventionSelectors = selectorSpecs;
    }

    public List<InterventionSelectorSpec> getSubAttemptInterventionSelectors() {
        return subAttemptInterventionSelectors;
    }

    public void setSubAttemptInterventionSelectors( List<InterventionSelectorSpec> selectorSpecs) {
        for (InterventionSelectorSpec spec: selectorSpecs) {
            String className =   getFullyQualifiedClassname(defaultClasspath + ".intervSel2", spec.getClassName());
            spec.setClassName(className);}
        this.subAttemptInterventionSelectors = selectorSpecs;
    }



    public PedagogicalModelParameters getParams() {
        return params;
    }

    public void setParams(PedagogicalModelParameters params) {
        this.params = params;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public void setPackg(String packg) {
        this.packg = packg;
    }

    public int compareTo(Object o) {
        Pedagogy p2 = (Pedagogy) o;
        if (Integer.parseInt(this.getId()) < Integer.parseInt(p2.getId()))
            return -1;
        else if  (Integer.parseInt(this.getId()) > Integer.parseInt(p2.getId()))
            return 1;
        else return 0;
    }

    public void setReviewModeProblemSelectorClass(String reviewModeProblemSelectorClass) {
        this.reviewModeProblemSelectorClass = getFullyQualifiedClassname(defaultClasspath+".probSel",reviewModeProblemSelectorClass);
    }

    public String getReviewModeProblemSelectorClass() {
        return reviewModeProblemSelectorClass;
    }

    public void setChallengeModeProblemSelectorClass(String challengeModeProblemSelectorClass) {
        this.challengeModeProblemSelectorClass = getFullyQualifiedClassname(defaultClasspath+".probSel",challengeModeProblemSelectorClass);
    }

    public String getChallengeModeProblemSelectorClass() {
        return challengeModeProblemSelectorClass;
    }

    /* An XML element of the form
    *        <lessonControl>
            <interventions>
                <interventionSelector onEvent="EndOfTopic" class="TopicSwitchAskIS">
                    <param name="ask">false</param>
                </interventionSelector>
            </interventions>
        </lessonControl>
     */


    public Element getInterventionsElement() {
        return interventionsElement;
    }

    public void setInterventionsElement(Element interventionsElement) {
        this.interventionsElement = interventionsElement;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLoginXMLName(String loginXMLName) {
        this.loginXMLName = loginXMLName;
    }

    public String getLoginXMLName() {
        return loginXMLName;
    }

    public LessonXML getLessonXML() {
        return lessonXML;
    }


    public void setLessonXML(LessonXML lessonXML) {
        this.lessonXML = lessonXML;
    }

    public LoginXML getLoginXML() {
        return loginXML;
    }

    public void setLoginXML(LoginXML loginXML) {
        this.loginXML = loginXML;
    }

    public void setSimpleConfigName(String simpleConfigName) {
        this.simpleConfigName = simpleConfigName;
    }

    public String getSimpleConfigName() {
        return simpleConfigName;
    }
}
