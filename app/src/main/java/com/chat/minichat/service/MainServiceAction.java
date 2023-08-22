package com.chat.minichat.service;

public enum MainServiceAction {
    START_SERVICE("START_SERVICE"),
    SETUP_VIEWS("SETUP_VIEWS"),
    TOGGLE_CAMERA("TOGGLE_CAMERA"),
    SWITCH_CAMERA("SWITCH_CAMERA"),
    TOGGLE_AUDIO("TOGGLE_AUDIO"),
    TOGGLE_AUDIO_DEVICE("TOGGLE_AUDIO_DEVICE"),
    STOP_SERVICE("STOP_SERVICE"),
    END_CALL("END_CALL");
    private final String name;

    MainServiceAction(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

}
