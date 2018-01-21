import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class SNCF {

    public static final String EX = "http://www.example.com/";
    public static final String GEO = "http://www.w3.org/2003/01/GEO/wgs84_pos#";
    public static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";

    public static void main(String arg[]) throws java.io.IOException{


        /*
        *
        * PARSING FILE stops_ter.txt
        *
        * */

        //Creating a new model
        Model model = ModelFactory.createDefaultModel();

        //Creating associated resources and properties
        Resource spatialThing = model.createResource(GEO + "SpatialThing");
        Property label = model.createProperty(RDFS + "label");
        Property lat = model.createProperty(GEO + "lat");
        Property lon = model.createProperty(GEO + "long");


        //Read the file
        String fileNameStops = "src/main/resources/export-ter-gtfs-last/stops_ter.txt";
        Scanner sc = new Scanner(new File(fileNameStops));
        sc.nextLine(); //Don't parse the header line

        //Process each line
        while(sc.hasNextLine()){
            processStops(sc.nextLine(), model, EX, spatialThing, label, lat, lon);
        }



        // Add the prefixes
        model.setNsPrefix("ex", EX);
        model.setNsPrefix("GEO", GEO);
        model.setNsPrefix("RDFS", RDFS);
        model.write(System.out, "Turtle");

        // now write the model in Turtle form to a file
        File file = new File("SNCF.ttl");
        try {
            FileOutputStream out = new FileOutputStream(file);
            model.write(out, "Turtle");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        /*
        *
        * PARSING FILE stop_times_ter.txt
        *
        * */

        //create a second empty Model
        Model model2 = ModelFactory.createDefaultModel();

        //Creating associated properties
        Property stop_id = model2.createProperty(EX + "stopid");
        Property stop_sequence = model2.createProperty(EX + "stop_sequence");
        Property arrival_time = model2.createProperty(EX + "arrivalTime");
        Property departure_time = model2.createProperty(EX + "departureTime");
        Property has_step = model2.createProperty(EX + "hasstep");

        //Read the file
        String fileNameTrips = "src/main/resources/export-ter-gtfs-last/stop_times_ter.txt";
        Scanner sc2 = new Scanner(new File(fileNameTrips));




        processStopTimes(sc2, model2, stop_id, stop_sequence, arrival_time, departure_time, has_step);




        // Add the prefixes
        model2.setNsPrefix("ex", EX);
        model2.setNsPrefix("RDFS", RDFS);
        model2.write(System.out, "Turtle");

        // now write the model in Turtle form to a file
        File file2 = new File("SNCF_times.ttl");
        try {
            FileOutputStream out = new FileOutputStream(file2);
            model2.write(out, "Turtle");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void processStops(String lineString, Model model, String baseURI, Resource spatialThing, Property label, Property lat, Property lon ){
        String[] line = lineString.split(",");


        Resource area = model.createResource(baseURI + line[0].replace(":", "_").replace(" ", "_"));

        model.add( area, RDF.type, spatialThing);
        model.add( area, label, line[1].replace("\"", ""));
        model.add( area, lat, line[3], XSDDatatype.XSDdecimal);
        model.add( area, lon, line[4], XSDDatatype.XSDdecimal);

    }


    public static void processStopTimes(Scanner sc, Model model, Property stop_id, Property stop_sequence, Property arrival_time, Property departure_time, Property has_step){
        String file_trip_id = null;
        String[] line;
        ArrayList<Etape> etapeList = new ArrayList<>();

        sc.nextLine(); //don't take the header

        //Initialization of file_trip_id  and first step
        line = sc.nextLine().split(","); //first line
        file_trip_id = line[0];
        etapeList.add(new Etape(line[1],line[2],line[3],line[4]));

        //For each line of the file
        while (sc.hasNextLine()){
            //we get the line
            line = sc.nextLine().split(",");

            //if the current line trip_id matches the last trip_id
            if (line[0] == file_trip_id){

                //We add this line as a step of the trip
                etapeList.add(new Etape(line[1],line[2],line[3],line[4]));
                // file_arrival_time,  file_departure_time,  file_stop_id,  file_stop_sequence

            // if the current line trip_id doesn't match the last trip_id we should create the whole trip for the last trip_id
            } else {

                //We create the resource of the trip
                Resource trip = model.createResource(EX + file_trip_id.replace(":","_").replace(" ", "_"));

                // Add to this trip all the steps from the arrayList and clean it
                for (Etape etape : etapeList){
                    //Each step is a blank node
                    Resource etapeBlankNode = model.createResource();
                    //refering to a particular stop which is a resource
                    Resource stop = model.createResource( EX + etape.file_stop_id.replace(":","_").replace(" ","_"));

                    model.add(etapeBlankNode, stop_id, stop);
                    model.add(etapeBlankNode, stop_sequence, etape.file_stop_sequence);
                    model.add(etapeBlankNode, arrival_time, etape.file_arrival_time, XSDDatatype.XSDtime);
                    model.add(etapeBlankNode, departure_time, etape.file_departure_time, XSDDatatype.XSDtime);

                    model.add(trip, has_step, etapeBlankNode);
                }
                //we clear the etapeList for the next trip
                etapeList.clear();


                file_trip_id = line[0]; //We change file_trip_id to the next trip id

                //add the current line to etapelist
                etapeList.add(new Etape(line[1],line[2],line[3],line[4]));
            }
        }

    }

}
