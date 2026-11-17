package com.zamcan.codelab;

/**
 * Explicit learner-selected storage mode. This type does not authenticate or upload anything.
 * Guest must remain available even when cloud services are unreachable.
 */
public enum CloudLearningMode {
    GUEST(false, false),
    GOOGLE_APPWRITE(true, true),
    AMAZON_DYNAMODB(true, false);

    private final boolean loginRequired;
    private final boolean launchEnabled;

    CloudLearningMode(boolean loginRequired, boolean launchEnabled) {
        this.loginRequired = loginRequired;
        this.launchEnabled = launchEnabled;
    }

    public boolean requiresLogin() { return loginRequired; }
    public boolean availableAtLaunch() { return this == GUEST || launchEnabled; }

    /** Fail closed for unknown persisted mode values. */
    public static CloudLearningMode fromPersisted(String value) {
        if (value == null) return GUEST;
        for (CloudLearningMode mode : values()) {
            if (mode.name().equals(value)) return mode;
        }
        return GUEST;
    }
}
