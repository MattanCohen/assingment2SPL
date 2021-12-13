package java;
import bgu.spl.mics.application.objects.*;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

public class GPUTest {
    GPU gpuTest;
    Data d;
    Student s;
    Model model;
    Cluster cluster;

    @Before
    public void setTest() {
        cluster=Cluster.getInstance();
        gpuTest = new GPU(GPU.Type.RTX2080, cluster);
        d = new Data(Data.Type.Images, 1000);
        s = new Student();
        model = new Model("testModel", d, s);
    }

    @Test
    public void trainModel() {
        // make sure trainModel isn't trained before and after training is updated
        assertEquals(Model.Status.PreTrained, model.getStatus());
        gpuTest.trainModel(model);
        assertEquals(Model.Status.Trained, model.getStatus());
    }

    @Test
    public void testModel() {
        // model has been tested
        gpuTest.trainModel(model);
        gpuTest.testModel(model);
        assertEquals(Model.Status.Tested, model.getStatus());
    }

    @Test
    public void getCluster() {
        assertEquals(cluster, gpuTest.getCluster());
    }
}
