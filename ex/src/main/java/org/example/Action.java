package org.example;

public enum Action {
    NONE,
    PLAY,
    // STOP_COMMENT,
    STOP,
    PAUSE,
    USER_DEFINED;

    static final String initialString;
    static {
        initialString = NONE.toString();
    }

    private String userDefined;

    public static String getInitialString() {
        return initialString;
    }

    public static Action getEnumByString(String a) {
        Action returnType;
        if (a == null || a.equals("")) {
            return NONE;
        }

        try {
            returnType = valueOf(Action.class, a.toUpperCase());
        } catch (Exception ex) {
            returnType = NONE;
        }

        return returnType;
    }

    @Override
    public String toString() {
        if (this.equals(Action.USER_DEFINED)) {
            return getUserDefined().toLowerCase();
        }
        String stringVal = super.toString();

        return stringVal.toLowerCase();
    }

    public String getUserDefined() {
        return userDefined;
    }

    public void setUserDefined(String userDefined) {
        this.userDefined = userDefined;
    }

}
