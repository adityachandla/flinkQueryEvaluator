package org.tue.thesis.parser;

public enum Direction {
    INCOMING,OUTGOING,BOTH;

    public static Direction parseDirection(String input) {
        switch (input) {
            case "<>":
                return BOTH;
            case ">":
                return OUTGOING;
            case "<":
                return INCOMING;
        }
        throw new IllegalArgumentException("Not able to parse to direction. Input: " + input);
    }
}
