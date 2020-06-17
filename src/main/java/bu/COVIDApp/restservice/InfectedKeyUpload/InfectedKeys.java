package bu.COVIDApp.restservice.InfectedKeyUpload;


/**
 * An object representing one key. This is the information that is stored in the database
 */
public class InfectedKeys {
    private String chirp;
    private String time;

    public InfectedKeys(String chirp, String time) {
        this.chirp = chirp;
        this.time = time;
    }

    public String getChirp() {
        return chirp;
    }

    public void setChirp(String chirp) {
        this.chirp = chirp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
