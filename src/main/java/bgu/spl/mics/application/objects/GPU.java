package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroPair;
import sun.awt.image.ImageWatched;
import sun.jvm.hotspot.oops.CompressedOops;
import sun.management.MonitorInfoCompositeData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
    private AtomicInteger currentSpace;
    private HashMap<Model, AtomicReference<LinkedList<DataBatch>>> processedData;
    // list of models GPU needs to train (according to round robin)
    // tickCounter = number of ticks counted needed to train model
    AtomicInteger tickCounter;
    private AtomicReference<LinkedList<Model>> modelsToTrain;


    /**
     * @param _type GPU type
     * @param _cluster GPU is linked to
     * @post model is unassigned
     */
    public GPU(Type _type, Cluster _cluster){
        type=_type;
        cluster=_cluster;
        processedData=new HashMap<>();
        modelsToTrain = new AtomicReference<LinkedList<Model>>();
        tickCounter = new AtomicInteger(0);
        if (type==Type.RTX3090)
            currentSpace =new AtomicInteger(32);
        else if (type==Type.RTX2080)
            currentSpace =new AtomicInteger(16);
        else if (type==Type.GTX1080)
            currentSpace =new AtomicInteger(8);
    }

    public int getCurrentSpace() {
        return currentSpace.get();
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
        for (int i=0; i<modelsToTrain.get().size() & !removed; i++){
            if (processedData.get(modelsToTrain.get().get(i)).get().contains(trainedBatch)){
                //create copy list and remove unporcessedData to change to
                LinkedList<DataBatch> with= processedData.get(modelsToTrain.get().get(i)).get();
                LinkedList<DataBatch> without=processedData.get(modelsToTrain.get().get(i)).get();
                without.remove(trainedBatch);
                //try to atomically remove data:
                while (processedData.get(modelsToTrain.get().get(i)).compareAndSet(with,without)){
                    with= processedData.get(modelsToTrain.get().get(i)).get();
                    without= processedData.get(modelsToTrain.get().get(i)).get();
                    without.remove(trainedBatch);
                }
                //data was removed from model's list
                removed=true;
            }
        }
        //batch was removed, we got 1 extra space add it atomically
        int f=currentSpace.get();
        while (currentSpace.compareAndSet(f, currentSpace.get() + 1))
            f=currentSpace.get();
    }

    public void addModel(Model t){
        //create copy list and add t to change to
        LinkedList<Model> without=modelsToTrain.get();
        LinkedList<Model> with= modelsToTrain.get();
        with.add(t);
        //try to atomically add model:
        while (modelsToTrain.compareAndSet(without,with)){
            without=modelsToTrain.get();
            with= modelsToTrain.get();
            with.add(t);
        }
    }
    public void removeModel(Model t) {
        //create copy list and remove unporcessedData to change to
        LinkedList<Model> with= modelsToTrain.get();
        LinkedList<Model> without=modelsToTrain.get();
        without.remove(t);
        //try to atomically remove model:
        while (modelsToTrain.compareAndSet(with,without)){
            with= modelsToTrain.get();
            without=modelsToTrain.get();
            without.remove(t);
        }
    }
    public MicroPair<Boolean,AtomicReference<LinkedList<DataBatch>>> processData (Model model, DataBatch unprocessedData){
        if(unprocessedData==null)
            return null;
        //store unproccesed data on gpu
        //if theres room for new batch add it accordingly
        boolean added=false;
        if (currentSpace.get()>0){
            added=true;
            //create copy list with unporcessedData to change to
            LinkedList<DataBatch> without= processedData.get(model).get();
            LinkedList<DataBatch> with=processedData.get(model).get();
            with.add(unprocessedData);
            //try to atomically add data:
            while (processedData.get(model).compareAndSet(without,with)){
                without= processedData.get(model).get();
                with= processedData.get(model).get();
                with.add(unprocessedData);
            }
            //batch was added, we got 1 less space
            int f=currentSpace.get();
            while (currentSpace.compareAndSet(f, currentSpace.get() -1))
                f=currentSpace.get();
            //@post: batch.isProcessed
            Cluster.getInstance().addBatchToProcess(unprocessedData);
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

