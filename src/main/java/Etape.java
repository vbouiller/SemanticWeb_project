public class Etape {

    String file_arrival_time ;
    String file_departure_time ;
    String file_stop_id ;
    String file_stop_sequence;


    public Etape(String file_arrival_time, String file_departure_time, String file_stop_id, String file_stop_sequence) {
        this.file_arrival_time = file_arrival_time;
        this.file_departure_time = file_departure_time;
        this.file_stop_id = file_stop_id;
        this.file_stop_sequence = file_stop_sequence;
    }
}
