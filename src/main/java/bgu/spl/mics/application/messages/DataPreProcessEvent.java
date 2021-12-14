package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;

public class DataPreProcessEvent  implements Event<Boolean> {

    //set result=true when batch of this event was processed in CPU
    Future<Boolean> result;
}
