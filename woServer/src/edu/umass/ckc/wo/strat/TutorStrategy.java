package edu.umass.ckc.wo.strat;

import edu.umass.ckc.wo.tutor.Pedagogy;

/**
 * Created by marshall on 6/14/17.
 * Making it be a subclass of Pedagogy so that all places where Pedagogy is used can accept this class.
 */
public class TutorStrategy extends Pedagogy {

    private int id;
    private String name;
    private String className;
    private ClassStrategyComponent login_sc;
    private ClassStrategyComponent lesson_sc;
    private ClassStrategyComponent tutor_sc;

    public int getStratId () {
        return this.id;
    }

    public void setStratId (int id) {
        this.id = id;
    }

    public ClassStrategyComponent getLogin_sc() {
        return login_sc;
    }

    public void setLogin_sc(ClassStrategyComponent login_sc) {
        this.login_sc = login_sc;
    }

    public ClassStrategyComponent getLesson_sc() {
        return lesson_sc;
    }

    public void setLesson_sc(ClassStrategyComponent lesson_sc) {
        this.lesson_sc = lesson_sc;
    }

    public ClassStrategyComponent getTutor_sc() {
        return tutor_sc;
    }

    public void setTutor_sc(ClassStrategyComponent tutor_sc) {
        this.tutor_sc = tutor_sc;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String toString(){
        return "TutorStrategy:" + id + ": " + name + "\n" +
                "\t" + login_sc.toString() + "\n" +
                "\t" + lesson_sc.toString() + "\n" +
                "\t" + tutor_sc.toString();
    }
}
