package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    final int cores;
    Queue<DataBatch> data;
    final private Cluster cluster;
    // number of ticks required to clear queue
    private int ticksToClearQueue;
    /**
     *
     * @param _cores
     * @param _cluster
     * @post data==null
     */
    public CPU(int _cores, Cluster _cluster){
        cores=_cores;
        cluster=_cluster;
        ticksToClearQueue = 0;
        data = new LinkedList<>();
    }

    /**
     * @return cores
     */
    public int getCores() {
        return cores;
    }

    /**
     * add data to the collection
     * @param data
     * @pre data is not in queue this.data
     * @post data is in queue this.data
     */
    public void addData(DataBatch data){
        // add batch to queue
        this.data.add(data);
        // calculate and add num of ticks needed to process batch
        Data.Type dType =data.getData().getType();
        int tickMultiplier = 1;
        // based on data type we ticks needed to process data change
        if (dType==Data.Type.Images) {tickMultiplier=4;}
        else if (dType==Data.Type.Tabular) {tickMultiplier=2;}

        ticksToClearQueue+= (32/cores)*tickMultiplier;

    }

    /**
     * get number of dataBatches in cpu atm
     * @return data.size
     */
    //get number of batches cpu is handling
    public int getNumOfBatches(){
        return data.size();
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
    public DataBatch processData(){
        return null;

        // update ticksToClearQueue each time a tick passes
    }

    /**
     * return the number of ticks until CPU finishes with all batches
     * */
    public int getTicksToClearQueue(){return ticksToClearQueue;}

}
