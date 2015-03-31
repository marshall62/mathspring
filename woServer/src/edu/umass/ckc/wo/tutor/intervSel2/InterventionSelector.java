package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.event.tutorhut.AttemptEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutormeta.Intervention;
import edu.umass.ckc.wo.tutormeta.StudentModel;
import org.jdom.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 11/14/13
 * Time: 12:11 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class InterventionSelector {

    protected SessionManager smgr;
    protected Connection conn;
    protected StudentState studentState;
    protected StudentModel studentModel;
    protected PedagogicalModel pedagogicalModel;

    protected List<InterventionSelectorParam> params;
    protected Element configXML;

    protected String userInputXML;


    public InterventionSelector(SessionManager smgr) {
//        init(smgr, pedagogicalModel);
        this.smgr = smgr;
        this.conn = smgr.getConnection();
        this.studentState = smgr.getStudentState();
        this.studentModel = smgr.getStudentModel();
    }

    public Intervention selectIntervention(SessionEvent e) throws Exception {
        if (this instanceof NextProblemInterventionSelector)          {
            //  e could be a continueNextProblemInterventionEvent or InputResponseNextProb...
            if (! (e instanceof NextProblemEvent)) {
                e = new NextProblemEvent(e.getServletParams());
                e.setSessionId(e.getSessionId());
            }
            return ((NextProblemInterventionSelector) this).selectIntervention((NextProblemEvent) e);
        }
        else if (this instanceof AttemptInterventionSelector)
            return ((AttemptInterventionSelector) this).selectIntervention((AttemptEvent) e);
        else return null;
    }

    /**
     * The init method of the InterventionSelector is called at a time later than the constructor.  This is necessary because
     * some of the objects necessary to the InterventinoSelector are not available at the time of its construction (e.g. the PedagogicalModel).
     * So we wait until just before we really need the InterventionSelector and then call its init method passing it the stuff it has to have at that point
     *
     * @param smgr
     * @param pedagogicalModel
     */
    public abstract void init(SessionManager smgr, PedagogicalModel pedagogicalModel);

    /**
     * Returns a JDOM XML Element that is the <config> ... </config> for the intervention selector
     * @return
     */
    public Element getConfigXML() {
        return configXML;
    }

    public void setConfigXML(Element configXML) {
        this.configXML = configXML;
    }

    public void setParams(List<InterventionSelectorParam> params) {
        this.params = params;
    }

    public List<InterventionSelectorParam> getParams() {
        return params;
    }

    /**
     * Each intervention selector must have a unique name (a string) that the tutoring engine can use
     * as a way of keeping state.  It must know which intervention selector produced the last intervention
     * so that when a response comes back from the student it can then forward this response to the
     * correct intervention selector.   The name of the class is sufficient to uniquely identify it.
     *
     * @return
     */
    public String getUniqueName() {
        return this.getClass().getName();
    }

    public void rememberInterventionSelector (InterventionSelector is) throws SQLException {
        smgr.getStudentState().setLastIntervention(is.getUniqueName());

    }

    public InterventionSelector getInterventionSelectorThatGeneratedIntervention () throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String classname = smgr.getStudentState().getLastIntervention();
        Class c = Class.forName(classname);
        Constructor constructor = c.getConstructor(SessionManager.class,PedagogicalModel.class);
        InterventionSelector is =(InterventionSelector) constructor.newInstance(smgr,pedagogicalModel);
        return is;
    }

    protected String getParameter (String name, List<InterventionSelectorParam> params) {
        if (params == null)
            return null;
        for (InterventionSelectorParam param: params) {
            if (param.getName().equals(name))
                return param.getValue();
        }
        return null;

    }


    protected String getConfigParameter (String name) {
        return configXML.getChild(name).getTextTrim();
    }

    protected List<String> getParameters (String name, List<InterventionSelectorParam> params) {
        List<String> results = new ArrayList<String>();
        for (InterventionSelectorParam param: params) {
            if (param.getName().equals(name))
                results.add(param.getValue());
        }
        return results;

    }

    protected void setUserInput(InterventionSelector cl, String userInputXML, InputResponseEvent e) {
        String clname = cl.getClass().getSimpleName();
        String ui = String.format("<interventionInput class=\"%s\">%s</interventionInput>",clname,userInputXML);
        this.userInputXML = ui;
    }


}
