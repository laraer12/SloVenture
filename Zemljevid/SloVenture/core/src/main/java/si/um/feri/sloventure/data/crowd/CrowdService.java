package si.um.feri.sloventure.data.crowd;

import java.util.List;
import java.util.ArrayList;

public class CrowdService {
    public interface Callback {
        void onSuccess(List<CrowdData> crowdList);
        void onFailure(String message);
    }

    // trenutno še fake vrednosti
    public static void fetchCrowdDataForAttraction(double lat, double lon, Callback callback) {
        try {
            List<CrowdData> crowdList = new ArrayList<>();

            // generiram 3 fake podatke o gneči pri določeni lokaciji
            for (int i = 0; i < 3; i++) {
                long timestamp = System.currentTimeMillis() / 1000 - i * 3600;
                int numOfPeople = (int)(Math.random() * 100); // do 100 ljudi

                crowdList.add(new CrowdData(numOfPeople, timestamp, lat, lon));
            }
            callback.onSuccess(crowdList);
        }
        catch (Exception e) {
            callback.onFailure("Failed to generate crowd data: " + e.getMessage());
        }
    }
}
