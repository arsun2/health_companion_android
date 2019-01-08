package com.example.health_companion_uiuc_mobile;

/**
 * Created by sunaustin8 on 5/5/18.
 * Sets default values of filtering options for labels
 */

public class InfoStore {
    public static boolean walkingTrue = false;
    public static boolean runningTrue = false;

    public void setWalkingTrue(boolean walkingTrue) {
        this.walkingTrue = walkingTrue;
    }

    public void setRunningTrue(boolean runningTrue) {
        this.runningTrue = runningTrue;
    }

    public boolean isWalkingTrue() {
        return walkingTrue;
    }

    public boolean isRunningTrue() {
        return runningTrue;
    }
}
