package com.chat.minichat.service;

public enum MainServiceAction {
    START_SERVICE("START_SERVICE"),
    SETUP_VIEWS("SETUP_VIEWS");
    private final String name;

    MainServiceAction(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

}
