package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {

    //the data the batch belongs to
    private Data data;
    //index of first sample in batch
    private int start_index;

    public DataBatch (Data _data, int _start_index){
        data=_data;
        start_index=_start_index;
    }

    public Data getData() {
        return data;
    }

    public int getStart_index() {
        return start_index;
    }
}
