package bgu.spl.mics.application.objects;

import bgu.spl.mics.MicroPair;
import sun.awt.image.DataBufferNative;
import sun.awt.image.ImageWatched;
import sun.tools.jconsole.InternalDialog;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    final int cores;
    AtomicReference<Queue<MicroPair<Integer,DataBatch>>> data;
    final private Cluster cluster;
    // number of ticks required to clear queue
    private AtomicInteger ticksToClearQueue;
    /**
     *
     * @param _cores
     * @param _cluster
     * @post data==null
     */
    public CPU(int _cores, Cluster _cluster){
        cores=_cores;
        cluster=_cluster;
        ticksToClearQueue = new AtomicInteger(0);
        data = new AtomicReference<Queue<MicroPair<Integer,DataBatch>>>();
    }

    /**
     * @return cores
     */
    public int getCores() {
        return cores;
    }

    public DataBatch removeFirstData() {
        //remove first element in without
        Queue<MicroPair<Integer, DataBatch>> with = data.get();
        Queue<MicroPair<Integer, DataBatch>> without = data.get();
        MicroPair<Integer,DataBatch> removed=without.remove();
        while (data.compareAndSet(without, with)) {
            with = data.get();
            without = data.get();
            removed=without.remove();
        }
        //substract ticks to clear queue accordingly
        int before=ticksToClearQueue.get();
        while (ticksToClearQueue.compareAndSet(before,before-removed.first()))
            before=ticksToClearQueue.get();
        //change removed pair's databatch to processed
        removed.second().finishProcessing();
        //return dataBatch removed
        return removed.second();
    }


    /**
     * add data to the collection
     * @param newData
     * @pre data is not in queue this.data
     * @post data is in queue this.data
     */
    public void addData(DataBatch newData){
        //create copy queue with newData to change to
        Queue<MicroPair<Integer,DataBatch>> without= data.get();
        Queue<MicroPair<Integer,DataBatch>> with= data.get();
        //get time needed to calculate data batch
        int timeNeeded=newData.getTimeToDoBatch(this);
        //add pair with amount of time to calculate and the same dataBatch
        with.add(new MicroPair<Integer, DataBatch>(timeNeeded,newData));
        //try to atomically add data and time needed to calculate it:
        while (data.compareAndSet(without,with)){
            without= data.get();
            with= data.get();
            with.add(new MicroPair<Integer, DataBatch>(timeNeeded,newData));
        }
        // add ticks atomically to clear entire queue
        int before=ticksToClearQueue.get();
        while(ticksToClearQueue.compareAndSet(before,before+timeNeeded))
            before=ticksToClearQueue.get();
    }

    /**
     * get number of dataBatches in cpu atm
     * @return data.size
     */
    //get number of batches cpu is handling
    public int getNumOfBatches(){
        return data.get().size();
    }

    /**
     * @return cluster
     */
    //get cluster
    public Cluster getCluster() {
        return cluster;
    }

    /** process the first data for calculated time milliseconds and return processed data
     * @return the data processed or null if getNumOfBatches()==0
     * if getNumOfBatches()>0: @post.getNumOfBatches() = @pre.getNumOfBatches() - 1
     * if getNumOfBatches()>0: @post first batch in queue this.data is removed
     */
    synchronized public DataBatch processData(){
        //if theres elements
        if (data.get().size()>0){
            //down 1 tick for first element in data queue
            data.get().peek().setFirst(data.get().peek().first()-1);
            //if first batch was running for enough ticks remove it from data queue and set as processed
            if (data.get().peek().first()==0){
                return removeFirstData();
            }
        }
        //if theres no elements
        return null;
    }

    /**
     * return the number of ticks until CPU finishes with all batches
     * */
    public int getTicksToClearQueue(){return ticksToClearQueue.get();}

}
