package bgu.spl.mics.application.services;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DataPreProcessEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

import java.util.concurrent.atomic.AtomicInteger;

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
    AtomicInteger time;

    public CPUService(String name, CPU _cpu){
        super(name);
        cpu=_cpu;
    }



    @Override
    protected void initialize() {
        // Add message+callback to subscriptions
        // DataPreProcessEvent should be managed by cluster??
        subscribeBroadcast(TickBroadcast.class, b->{
            time.compareAndSet(time.get(),time.get()+1);
            cpu.processData();
        });
        // register CPU
        MessageBusImpl.getInstance().register(this);
        // subscribe to relevant messages in MessageBus
        MessageBusImpl.getInstance().subscribeBroadcast(TickBroadcast.class,this);
    }
}
