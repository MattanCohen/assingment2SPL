package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService  {
	private int spd;
	private int drt;
	private int tickCount;

	public TimeService(int speed,int duration){
		super("TimeService");
		spd=speed;
		drt=duration;
		tickCount = 0;
	}

	@Override
	protected void initialize() {
		// register timeService so that it can send TickBroadcasts
		MessageBusImpl.getInstance().register(this);
		// how to make sure TickBroadcast is sent?
		// do we need to create an event tick?
	}

}
