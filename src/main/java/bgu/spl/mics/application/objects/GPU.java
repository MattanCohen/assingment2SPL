package bgu.spl.mics.application.objects;

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
    private Model model;
    final private Cluster cluster;

    /**
     * @param _type GPU type
     * @param _cluster GPU is linked to
     * @post model is unassigned
     */
    public GPU(Type _type, Cluster _cluster){
        type=_type;
        cluster=_cluster;
    }

    /**receive a model (change newModel's status to training)
    use cpu to process the data and then
    train it and change newModel's status to trained

     * @pre model.getStatus() = Status.PreTrained
     * @inv model.getStatus() = Status.Training
     * @post model.getStatus() = Status.Trained
     */
    public Model trainModel(Model newModel){
        model=newModel;
        //split data to batches (of 1000)
        return model;
    }

    /**receive a model, test it, change newModel's status to tested and return the result
     * @pre model.getStatus() = Status.Trained
     * @post model.getStatus() = Status.Tested
     * */
    public Model.Result testModel(Model newModel){
        model=newModel;
        return model.getResult();
    }

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

    /**
     * @return model
     */
    public Model getModel() {
        return model;
    }
}

