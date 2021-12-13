package bgu.spl.mics.application;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.*;
import java.io.FileReader;
import java.io.FileNotFoundException;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\Celestite\\IdeaProjects\\TestGson\\src\\main\\java\\example_input.json";

        JsonObject inputJson = readJsonFile(inputFilePath);
        Student[] students = extractStudentList(inputJson);
        // gets list of models for corresponding student index
        Model[][] modelMatrix = extractModelMatrix(inputJson,students);
        /*
         * create for each model a "train difficulty" estimate,
         * and based on it we could assign the model to easier cpu's(?)
         * */
        Cluster cluster = Cluster.getInstance();

        GPU[] gpus = extractGPUList(inputJson, cluster);
        CPU[] cpus = extractCPUList(inputJson,cluster);

        cluster.setCPUs(cpus);
        cluster.setGPUs(gpus);

        /*
         * calculate the max gpu type and max cpu core to create for
         * each model the min/max amount of time for training the model
         * this way we can put the students to sleep for a specific amount of time that is always required
         * */

        ConfrenceInformation[] conferences = extractConferenceList(inputJson);
        // global variables relevant for time service
        int tickTime = inputJson.get("TickTime").getAsInt();
        int systemDuration = inputJson.get("Duration").getAsInt();

        MessageBusImpl messageBus = MessageBusImpl.getInstance();


    }

    /******************* Functions to Extract objects from Input File **************************************
     /**
     * gets a input json path and returns a Json object
     * */
    private static JsonObject readJsonFile(String inputFilePath) {
        try {

            JsonParser parser = new JsonParser();
            FileReader jsonReader = new FileReader(inputFilePath);
            JsonObject inputJson = parser.parse(jsonReader).getAsJsonObject();

            return inputJson;
        } catch (FileNotFoundException e) {System.out.print("File not found"); }

        return null;
    }

    /**
     * creates a list of Students given a Json array of Students (models are built at later Stage
     * */
    private static Student[] extractStudentList(JsonObject inputJson){
        JsonArray studentJArray = inputJson.getAsJsonArray("Students");
        Student[] students = new Student[studentJArray.size()];
        for( int i=0; i<studentJArray.size(); i++) {
            JsonObject studentJson = studentJArray.get(i).getAsJsonObject();
            // extract Student fields
            String name = studentJson.get("name").getAsString();
            String department = studentJson.get("department").getAsString();
            Student.Degree status = Student.Degree.valueOf(studentJson.get("status").getAsString());
            // construct object and add to array
            students[i] = new Student(name,department,status);
        }
        return students;
    }

    /**
     * create model list of a single student
     * */
    private static Model[] extractModelList(JsonArray modelJArray, Student linkedStudent){
        Model[] modelList = new Model[modelJArray.size()];
        for(int i=0; i<modelJArray.size();i++) {
            JsonObject modelJson = modelJArray.get(i).getAsJsonObject();
            // create data object linked with Model
            Data modelData = new Data(Data.Type.valueOf(modelJson.get("type").getAsString()),modelJson.get("size").getAsInt());
            modelList[i] = new Model(modelJson.get("name").getAsString(),modelData,linkedStudent);
        }

        return modelList;
    }
    /**
     * create matrix of models based on student list and input json
     * */
    private static Model[][] extractModelMatrix(JsonObject inputJson, Student[] students) {
        Model[][] modelMatrix = new Model[students.length][];

        for(int i=0; i<students.length; i++) {
            JsonArray modelJArray = inputJson.get("Students").getAsJsonArray().get(i).getAsJsonObject().get("models").getAsJsonArray();
            Model[] studentModels = extractModelList(modelJArray,students[i]);
            modelMatrix[i] = studentModels;
        }
        return modelMatrix;

    }

    /**
     * creates a list of GPU's given a Json array of gpuTypes, and Cluster object
     * */
    private static GPU[] extractGPUList(JsonObject inputJson, Cluster cluster){
        JsonArray gpuJArray = inputJson.getAsJsonArray("GPUS");
        GPU[] gpus = new GPU[gpuJArray.size()];
        for(int i=0;i<gpuJArray.size();i++) {
            // need to convert GPUType to enum
            String GPUType = gpuJArray.get(i).getAsString();
            gpus[i] = new GPU(GPU.Type.valueOf(GPUType),cluster);
        }
        return gpus;
    }

    /**
     * creates a list of CPU's given a Json array of cpuCores, and Cluster object
     * */
    private static CPU[] extractCPUList(JsonObject inputJson, Cluster cluster){
        JsonArray cpuCoreJArray = inputJson.getAsJsonArray("CPUS");
        /*
         * Here we can convert the jsonArray to a regular array and sort the values
         * if we want the cpu's to be organized by number of cores
         * */

        CPU[] cpus = new CPU[cpuCoreJArray.size()];
        for(int i=0; i<cpuCoreJArray.size();i++) {
            // jsonElement only contains number of cores
            cpus[i] = new CPU(cpuCoreJArray.get(i).getAsInt(),cluster);
        }
        return cpus;
    }

    /**
     * creates a list of ConferenceInformation given a Json array of conference information
     * */
    private static ConfrenceInformation[] extractConferenceList(JsonObject inputJson) {
        JsonArray conferenceJArray = inputJson.getAsJsonArray("Conferences");
        ConfrenceInformation[] conferences = new ConfrenceInformation[conferenceJArray.size()];
        for(int i=0; i<conferenceJArray.size(); i++) {
            // each array element contains a name and a date
            JsonObject conferenceJson = conferenceJArray.get(i).getAsJsonObject();
            // convert name & date to String and int accordingly
            ConfrenceInformation conference = new ConfrenceInformation(conferenceJson.get("name").getAsString(),conferenceJson.get("date").getAsInt());
            conferences[i] = conference;
        }
        return conferences;
    }
/****************************************************************************************************/

}