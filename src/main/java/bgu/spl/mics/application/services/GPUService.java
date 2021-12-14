package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import org.junit.Test;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;

    public GPUService(String name){
        super(name);
    }

    @Override
    protected void initialize() {
        // register to message bus
        MessageBusImpl.getInstance().register(this);
        // add TrainModel + TestModel callbacks
        /*need to update callbacks*/
        /*only when in training model we want to subscribe to tick model?*/
        subscribeEvent(TrainModelEvent.class,e->{});
        subscribeEvent(TestModelEvent.class,e->{});
        // subscribe to TrainModel+TestModel in messageBus
        MessageBusImpl.getInstance().subscribeEvent(TrainModelEvent.class,this);
        MessageBusImpl.getInstance().subscribeEvent(TestModelEvent.class,this);
    }

}