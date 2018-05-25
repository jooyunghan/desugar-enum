package org.example;

import java.util.Arrays;
import java.util.List;

import static org.example.Action.PLAY;
import static org.example.Action.STOP;
import static org.example.State.*;


public class ActionMain {
    State state = IDLE;
    Action last = null;

    public static void main(String[] args) {
        ActionMain t = new ActionMain();
        List<Action> actions = Arrays.asList(new Action[]{PLAY, PLAY, STOP, Action.PAUSE, Action.valueOf("PLAY")});
        for (Action a : actions) {
            t.handle(a);
        }
        for (Action a : Action.values()) {
            t.handle(a);
        }
        System.out.println(t);
    }

    void handle(Action action) {
        /** 주석 */
        last = action;
        switch (state) {
            case IDLE:
                switch (action) {
                    case PLAY:
                        this.state = PLAYING;
                        break;
                    default:
                        break;
                }
                break;
            case PLAYING:
                if (action == Action.PAUSE) {
                    this.state = PAUSED;
                } else if (action.equals(STOP)) {
                    this.state = IDLE;
                } else {

                }
                break;
//            case PLAYING:
//                if (action == Action.PAUSE) {
//                    this.state = PAUSED;
//                } else if (action.equals(STOP)) {
//                    this.state = IDLE;
//                } else {
//
//                }
//                break;
            default:
                if (state == PAUSED && action == PLAY) {
                    this.state = PLAYING;
                } else if (action == Action.STOP) {
                    this.state = IDLE;
                }
                break;
        }
    }
}

