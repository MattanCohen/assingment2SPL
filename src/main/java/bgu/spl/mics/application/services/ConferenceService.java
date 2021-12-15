package bgu.spl.mics.application.services;

import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroPair;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    ConfrenceInformation conference;
    int tickTime;
    LinkedList<Model> modelsToPublish;

    public ConferenceService(ConfrenceInformation other){
        super("ConferenceService");
        conference=other;
        modelsToPublish = new LinkedList<>();
        tickTime = 0;
    }

    @Override
    protected void initialize() {
        //register to msgbus
        MessageBusImpl.getInstance().register(this);
        //sub to events with those callbacks \/
        subscribeEvent(PublishResultsEvent.class,(e)-> {
            Model modelToPublish = e.getModel();
            // add good model to conference
            if(modelToPublish.getResult()== Model.Result.Good)
                {modelsToPublish.add(modelToPublish);}
        });
        subscribeBroadcast(TickBroadcast.class, (b)->{
            tickTime++;
            // if we reach conference date, public the conference
            if (tickTime==conference.getDate()) {
                PublishConferenceBroadcast pubC = new PublishConferenceBroadcast(modelsToPublish);
                sendBroadcast(pubC);
                // after conference is published we want to unregister
                MessageBusImpl.getInstance().unregister(this);
            }
        });
    }
}
