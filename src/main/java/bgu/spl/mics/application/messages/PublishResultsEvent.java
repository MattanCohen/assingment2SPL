package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;


/*
    -student sends this event
    -msgBus assigns conference to handle
    -conference collects model names of successful models from students
 */


public class PublishResultsEvent implements Event<Integer> {

    //will hold 1 only if event is finished
    Future<Integer> result;
}
