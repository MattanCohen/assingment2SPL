package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.LinkedList;

/*
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    Student std;

    public StudentService(Student student){
        super("StudentService");
        std=student;
//        subscribeBroadcast(PublishConferenceBroadcast.class, message->{
//            System.out.println("Student "+std.getName()+" has subscribed to conference's broadcast");
//        });
    }

    @Override
    protected void initialize() {
        // Add message+callback to subscriptions
        subscribeBroadcast(PublishConferenceBroadcast.class, b->{
            LinkedList<Model> publishedModels = b.getPublishedModels();
            // each model in the conference changes Student
            for(Model m:publishedModels) {
                // Student has another published paper or read another paper
                if (m.getStudent() == std)
                    std.publicationsIncrement();
                else {
                    std.papersReadIncrement();
                }
            }
        });
        // register to message bus
        MessageBusImpl.getInstance().register(this);
        // subscribe to relevant messages in MessageBus
        MessageBusImpl.getInstance().subscribeBroadcast(PublishConferenceBroadcast.class,this);

        //**send TrainModel events for each model that is related to student**

    }
}
