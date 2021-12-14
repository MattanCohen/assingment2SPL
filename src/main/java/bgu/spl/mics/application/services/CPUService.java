package bgu.spl.mics.application.services;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DataPreProcessEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
/*
 *sole responsibility is to update the time for the CPU
 */

public class CPUService extends MicroService {

    private CPU cpu;

    public CPUService(String name){
        super(name);

    }

    @Override
    protected void initialize() {
        // Add message+callback to subscriptions
        subscribeEvent(DataPreProcessEvent.class,e->{});
        subscribeBroadcast(TickBroadcast.class, b->{});
        // register CPU
        MessageBusImpl.getInstance().register(this);
        // subscribe to relevant messages in MessageBus
        MessageBusImpl.getInstance().subscribeEvent(DataPreProcessEvent.class,this);
        MessageBusImpl.getInstance().subscribeBroadcast(TickBroadcast.class,this);
    }
}
