package si.um.feri.sloventure.data.crowd;

import java.util.Date;
import java.text.SimpleDateFormat;

public class CrowdData {
    public int numOfPeople;
    public long timestamp;
    public double latitude;
    public double longitude;

    public CrowdData(int numOfPeople, long timestamp, double latitude, double longitude) {
        this.numOfPeople = numOfPeople;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");

        return sdf.format(date);
    }

    public String toString() {
        return "Number of people: " + numOfPeople + " - Time: " + formatTimestamp(timestamp);
    }
}
