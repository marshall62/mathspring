package edu.umass.ckc.wo.login;


import edu.umass.ckc.wo.tutormeta.LearningCompanion;

public class LoginResult {
    int sessId;
    String message;
    LearningCompanion learningCompanion;
    int status;

    public static final int  NEW_SESSION = 1;
    public static final int  ALREADY_LOGGED_IN = 2;
    public static final int  ERROR = -1;


    public LoginResult(int sessId, String loginView) {
        this.sessId = sessId;
        this.message = loginView;
        status = NEW_SESSION;
    }

    public LoginResult(int sessId, String loginView, int status) {
        this(sessId,loginView);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // If an error message is created during login, it is placed in message.
    public boolean isFailed() {
        return sessId == -1 && message != null;
    }

    public boolean hasExistingSession () {
        return sessId != -1 && message != null;
    }

    public LearningCompanion getLearningCompanion() {
        return learningCompanion;
    }

    public void setLearningCompanion(LearningCompanion learningCompanion) {
        this.learningCompanion = learningCompanion;
    }

    public String getMessage() {
        return message;
    }

    public int getSessId() {
        return sessId;
    }
}