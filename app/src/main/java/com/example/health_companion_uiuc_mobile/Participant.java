package com.example.health_companion_uiuc_mobile;

import java.util.ArrayList;

/**
 * Created by sunaustin8 on 4/16/17.
 */

public class Participant {

    private String name;
    private String message;

    public Participant() { this.name = name; }

    public Participant(String name) {
        this.name = name;
    }

    public void setName(String name){
        this.name = name;
    }

    public Participant(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getMessage() { return message;}

}