package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.DataPreProcessEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;
import org.junit.Test;
import sun.awt.image.ImageWatched;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        //register to msgbus
        MessageBusImpl.getInstance().register(this);
        //sub to events with those callbacks \/
        subscribeEvent(TrainModelEvent.class,(e)-> {
            //register to msg bug
            //prepare model's data and add it to gpu's current models worked
            Model toTrain = e.getModel();
            gpu.addModel(toTrain);
            toTrain.setStatus(Model.Status.Training);
            DataBatch[] unprocessedData = splitData(toTrain.getData());
            //train model via gpu
            boolean notTrained = true;
            int i=0;
            while (notTrained) {
                //for each unprocessed data batch
                MicroPair<Boolean, AtomicReference<LinkedList<DataBatch>>> batchPair=gpu.processData(toTrain, unprocessedData[i]);
                //if the batch has been added to the cluster, add the next batch
                if (batchPair.first()){
                    i++;
                    unprocessedData[i]=null;
                }
                //get the list of data our GPU is supposed to train in the current state (every none processed batch sent to cluster's handle)
                LinkedList<DataBatch> toProcess = batchPair.second().get();
                //if everything was processed and no new data batch needs to be processed, stop the callback/do complete
                if (toProcess.size() == 0) {
                    // all batches have been trained and removed
                    notTrained = false;
                } else {
                    //check on all data we're supposed to train
                    for (int j = 0; j < toProcess.size(); j++) {
                        //if batch j was processed by some cpu sometime
                        if (toProcess.get(j).isProcessed()) {
                            //find how long gpu supposed to wait
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
                            //get the current time of the system (general ticks counter)
                            int currTime = time.get();
                            //try to wait for as long as needed:
                            while (time.get() < currTime + toAdd){
                                //since this was initialized, try to wait 1 message
                                try{
                                    run();
                                }
                                //if the message was interrupted by IAE, run() will send IllegalArgumentException
                                //thus making us continue the while loop in this, and checking if interruption
                                //was by tickBroadcast
                                catch (IllegalArgumentException exc){}
                                //if run() threw ISE, microservice isn't registered anymore to msgBUS so unregister it
                                catch (IllegalStateException excp){
                                    MessageBusImpl.getInstance().unregister(this);
                                }
                            }
                            //that while loop made GPU work toAdd time, so make sure it remembers
                            gpu.incrementTick(toAdd);
                            //batch was processed so remove it from data to process (in gpu) and from data
                            //unprocessed in here
                            gpu.removeBatch(toProcess.get(j));
                        }
                    }
                }
            }
            //finish working on model
            toTrain.setStatus(Model.Status.Trained);
            gpu.removeModel(toTrain);
            MessageBusImpl.getInstance().complete(e,true);
        });
        subscribeEvent(TestModelEvent.class,e->{
            //set model's result to bad if it exists
            String result="Bad";
            if (e.getModel()!=null)
                e.getModel().setResult(Model.Result.Bad);
            //create random number 0-99
            int random=(int)(100*Math.random());
            //in case student is phd, prob is 0.2 to change to good
            if (e.getModel().getStudent().getStatus()== Student.Degree.PhD){
                if (random<20){
                    e.getModel().setResult(Model.Result.Good);
                    result="Good";
                }
            }
            //prob is 0.1
            else{
                if (random<10){
                    e.getModel().setResult(Model.Result.Good);
                    result="Good";
                }
            }
            e.getModel().setStatus(Model.Status.Tested);
            MessageBusImpl.getInstance().complete(e,result);
        });
        subscribeBroadcast(TickBroadcast.class,b->{
            int f=time.get();
            while (time.compareAndSet(f, f+1))
                f=time.get();
            notifyAll();
        });
    }

}