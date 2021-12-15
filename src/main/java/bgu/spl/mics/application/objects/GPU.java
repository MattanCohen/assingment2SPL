package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroPair;
import sun.jvm.hotspot.oops.CompressedOops;
import sun.management.MonitorInfoCompositeData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}

    final private Type type;
    final private Cluster cluster;
    private int currentSpace;
    private HashMap<Model,LinkedList<DataBatch>> processedData;
    // list of models GPU needs to train (according to round robin)
    // tickCounter = number of ticks counted needed to train model
    AtomicInteger tickCounter;
    private LinkedList<Model> modelsToTrain;


    /**
     * @param _type GPU type
     * @param _cluster GPU is linked to
     * @post model is unassigned
     */
    public GPU(Type _type, Cluster _cluster){
        type=_type;
        cluster=_cluster;
        processedData=new HashMap<Model,LinkedList<DataBatch>>();
        modelsToTrain = new LinkedList();
        tickCounter = new AtomicInteger(0);
        if (type==Type.RTX3090)
            currentSpace =32;
        else if (type==Type.RTX2080)
            currentSpace =16;
        else if (type==Type.GTX1080)
            currentSpace =8;
    }

    public int getCurrentSpace() {
        return currentSpace;
    }

    /**receive a model (change newModel's status to training)
    use cpu to process the data and then
    train it and change newModel's status to trained

     * @pre model.getStatus() = Status.PreTrained
     * @inv model.getStatus() = Status.Training
     * @post model.getStatus() = Status.Trained
     */

    // remove batch after it's been trained (ticks done in GPUService)
    public void removeBatch(DataBatch trainedBatch){
        boolean removed=false;
        for (int i=0; i<modelsToTrain.size() & !removed; i++){
            if (processedData.get(modelsToTrain.get(i)).contains(trainedBatch)){
                processedData.get(modelsToTrain.get(i)).remove(trainedBatch);
                removed=true;
            }
        }
        currentSpace++;
    }

    public MicroPair<Boolean,LinkedList<DataBatch>> processData (Model model, DataBatch unprocessedData){
        if(unprocessedData==null)
            return null;
        //store unproccesed data on gpu
        //while there is unprocessed data
            //while there is room for more processed data
        boolean added=false;
        if (currentSpace>0){
            added=true;
            processedData.get(model).add(unprocessedData);
            currentSpace--;
            //@post: batch.isProcessed
            Cluster.getInstance().processBatch(unprocessedData);
        }
        return new MicroPair<>(added,processedData.get(model));
    }

    /**receive a model, test it, change newModel's status to tested and return the result
     * @pre model.getStatus() = Status.Trained
     * @post model.getStatus() = Status.Tested
     * */
//    public Model.Result testModel(Model newModel){
//        model=newModel;
//        return model.getResult();
//    }

    /**
     * @return cluster
     */
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @return type
     */
    public Type getType() {
        return type;
    }

    public AtomicInteger getTickCounter() {
        return tickCounter;
    }
    public void incrementTick(int toAdd){
        tickCounter.compareAndSet(tickCounter.get(),tickCounter.get()+toAdd);
    }

    /**
     * @return model
     */
//    public Model getModel() {
//        return model;
//    }
}

