package bgu.spl.mics.application.messages;
import bgu.spl.mics.Broadcast;

/*
    -sent by conference (at a set time according to time ticks) to msgBus
    -msgBus broadcasts all collected results to all students
    -unregister conference
 */


public class PublishConferenceBroadcast implements Broadcast {
}
