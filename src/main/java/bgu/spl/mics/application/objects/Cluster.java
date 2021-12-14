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



}
