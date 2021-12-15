package bgu.spl.mics.application.objects;
import bgu.spl.mics.MessageBusImpl;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private GPU[] gpus;
	private CPU[] cpus;
	// make sure GPU updates cluster each time a model is trained
	private LinkedList<String> trainedModelsNames;
	// total number of data batches
	private int dataCPU;
	private int timeCPU;
	private int timeGPU;

//	public void statistics(){
//		timeCPU=
//	}

	private static class SingletonHolder{
		private static Cluster instance=new Cluster();
	}
	/**
	 * Retrieves the single instance of this class.
	 */
	synchronized public static Cluster getInstance(){return Cluster.SingletonHolder.instance;}

	private Cluster(){}
	public void setGPUs(GPU[]_gpus){gpus=_gpus;}
	public void setCPUs(CPU[]_cpus){cpus=_cpus;}

	//add to optimal cpu the data. it will be processed with ticks
	synchronized public void addBatchToProcess(DataBatch toProcess){
		CPU work=getOptimalCPU(toProcess.getData().getType());
		work.addData(toProcess);
	}

	/**
	 * based on the batch data type,
	 * find the best CPU to add to batch list
	 * */
	public CPU getOptimalCPU(Data.Type dType) {
		int tickMultiplier = 1;
		// based on data type we ticks needed to process data change
		if (dType==Data.Type.Images) {tickMultiplier=4;}
		else if (dType==Data.Type.Tabular) {tickMultiplier=2;}

		int minIndex = 0;
		int cores = cpus[0].getCores();
		int minNewTicksToClearQueue = cpus[0].getTicksToClearQueue()+(32/cores)*tickMultiplier;

		// get the core that will need the least ticks to clear queue with new batch
		for(int i=1; i<cpus.length; i++) {
			cores = cpus[i].getCores();
			// calculate number of ticks with new batch added
			int newTicksToClearQueue = cpus[i].getTicksToClearQueue()+(32/cores)*tickMultiplier;
			// update fields with better CPU
			if (newTicksToClearQueue<minNewTicksToClearQueue) {
				minIndex=i;
				minNewTicksToClearQueue=newTicksToClearQueue;
			}
		}
		// return the optimal CPU for the job
		return cpus[minIndex];
	}



}
