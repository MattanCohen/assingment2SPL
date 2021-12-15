package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.DataPreProcessEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import org.junit.Test;
import sun.awt.image.ImageWatched;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static bgu.spl.mics.application.objects.GPU.Type.*;

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
    AtomicInteger time;

    public GPUService(String name){
        super(name);
    }

    private DataBatch[] splitData(Data data){
        DataBatch[] ans=new DataBatch[(int)(data.getSize()/1000)];
        for (int i=0; i<ans.length; i+=1)
            ans[i]=new DataBatch(data,i);
        return ans;
    }

    @Override
    protected void initialize() {
        // *****************Add message+callback to subscriptions
        /*need to update callbacks*/
        subscribeEvent(TrainModelEvent.class,(e)-> {
            Model toTrain = e.getModel();
            DataBatch[] unprocessedData = splitData(toTrain.getData());
            boolean notTrained = true;
            int i=0;
            while (notTrained) {
                MicroPair<Boolean,LinkedList<DataBatch>> batchPair=gpu.processData(toTrain, unprocessedData[i]);
                if (batchPair.first())
                    i++;
                LinkedList<DataBatch> toProcess = batchPair.second();
                if (toProcess.size() == 0) {
                    // all batches have been trained and removed
                    notTrained = false;
                } else {
                    for (int j = 0; j < toProcess.size(); j++) {
                        if (toProcess.get(j).isProcessed()) {
                            int toAdd = 0;
                            switch (gpu.getType()) {
                                case RTX3090:
                                    toAdd = 1;
                                    break;
                                case RTX2080:
                                    toAdd = 2;
                                    break;
                                case GTX1080:
                                    toAdd = 4;
                                    break;
                            }
                            int currTime = time.get();
                            while (time.get() < currTime + toAdd){
                                run();
                            }
                            gpu.incrementTick(toAdd);
                            gpu.removeBatch(toProcess.get(j));
                            unprocessedData[i]=null;
                        }
                    }
                }
            }
            MessageBusImpl.getInstance().complete(e,true);
        });

        subscribeEvent(TestModelEvent.class,e->{

        });
        subscribeBroadcast(TickBroadcast.class,b->{
            time.compareAndSet(time.get(), time.get()+1);
            notifyAll();
        });
        // register to message bus
        MessageBusImpl.getInstance().register(this);
        // subscribe to relevant messages in MessageBus
        MessageBusImpl.getInstance().subscribeEvent(TrainModelEvent.class,this);
        MessageBusImpl.getInstance().subscribeEvent(TestModelEvent.class,this);
        MessageBusImpl.getInstance().subscribeBroadcast(TickBroadcast.class,this);
    }

}