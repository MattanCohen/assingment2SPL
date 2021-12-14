package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;


/*
    diagram:
    -student send test model event
    -some GPU receives the event
    -GPU updates the object
    -msgBus sets future={
                            "Good" with probability of 0.6 for MSc and 0.8 for PhD
                            "Bad" else
                        }
 */
public class TestModelEvent implements Event<String> {
    Future<String> result;
}
