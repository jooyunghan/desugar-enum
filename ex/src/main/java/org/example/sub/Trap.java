package org.example.sub;

public class Trap {
    Action a;
    void handle(Action action) {
        if (a != action)
            a = action;
        switch (a) {
            case ACTION1:
                break;
            default:
                break;
        }
    }
}
