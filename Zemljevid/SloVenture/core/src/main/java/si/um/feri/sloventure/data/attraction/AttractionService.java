package si.um.feri.sloventure.data.attraction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.Net.HttpResponseListener;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

import si.um.feri.sloventure.data.crowd.CrowdData;

public class AttractionService {
    private static final String URL = "http://localhost:3001/attractions"; // kasneje menjano s strežniškim URL

    public interface Callback {
        void onSuccess(List<AttractionData> attractions);
        void onFailure(int status, String message);
    }

    // pridobitev VSEH znamenitosti
    public static void fetchAllAttractions(Callback callback) {
        sendRequest(URL, callback);
    }

    // filtriranje po klasifikaciji
    public static void fetchByClassification(String classification, Callback callback) {
        try {
            String encodedClassification = java.net.URLEncoder.encode(classification, "UTF-8").replace("+", "%20"); // moram zakodirat, ker so prisotni šumniki ter presledki
            String url = URL + "/classification/" + encodedClassification;
            sendRequest(url, callback);
        }
        catch (Exception e) {
            callback.onFailure(-1, "Encoding failed: " + e.getMessage());
        }
    }

    // filtriranje po regiji
    public static void fetchByRegion(String regionId, Callback callback) {
        String url = URL + "/region/" + regionId;
        sendRequest(url, callback);
    }

    // filtriranje po tipu lokacije
    public static void fetchByLocationType(String locationType, Callback callback) {
        try {
            String encodedLocationType = java.net.URLEncoder.encode(locationType, "UTF-8").replace("+", "%20"); // moram zakodirat, ker so prisotni šumniki ter presledki
            String url = URL + "/type/" + encodedLocationType;
            sendRequest(url, callback);
        }
        catch (Exception e) {
            callback.onFailure(-1, "Encoding failed: " + e.getMessage());
        }
    }

    private static List<AttractionData> parse(String json) {
        List<AttractionData> attractions = new ArrayList<>();
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(json);

        for (JsonValue entry : root) {
            // *** ATTRACTION ***
            JsonValue attraction = entry.get("attraction");

            String id = attraction.getString("_id", "");
            String name = attraction.getString("name", "");

            // *** REGION ***
            JsonValue region = attraction.get("regionId");

            String regionId = region.getString("_id", "");
            String regionName = region.getString("name", "");

            // *** LOCATION ***
            JsonValue location = attraction.get("location");

            float lat = location.getFloat("lat", 0);
            float lon = location.getFloat("lon", 0);

            // *** ADDRESS ***
            JsonValue address = attraction.get("address");

            String street = address.getString("street", "");
            String city = address.getString("city", "");
            String postalCode = address.getString("postalCode", "");
            String country = address.getString("country", "");

            // *** DESCRIPTION ***
            String description = attraction.getString("description", "");

            // *** CLASSIFICATION ***
            String classification = attraction.getString("classification", "");

            // *** LOCATION TYPE ***
            String locationType = attraction.getString("locationType", "");

            // *** ELEVATION ***
            int elevation = attraction.getInt("elevation", 0);

            // *** RATING ***
            float rating = attraction.getFloat("rating", 0);

            // *** IMAGES ***
            JsonValue imagesJson = entry.get("images");
            List<AttractionImage> images = new ArrayList<>();

            if (imagesJson != null) {
                for (JsonValue img : imagesJson) {
                    String imageId = img.getString("_id", "");
                    String attractionId = img.getString("attractionId", "");
                    String url = img.getString("url", "");

                    images.add(new AttractionImage(imageId, attractionId, url));
                }
            }

            // *** CROWD ***
            List<CrowdData> crowd = new ArrayList<>(); // trenutno je prazno, dodam kasneje ko se generira

            attractions.add(new AttractionData(
                id, name,
                regionId, regionName,
                lat, lon,
                street, city, postalCode, country,
                description,
                classification,
                locationType,
                elevation,
                rating,
                images,
                crowd
            ));
        }
        return attractions;
    }

    private static void sendRequest(String url, Callback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        Net.HttpRequest request = requestBuilder.newRequest()
            .method(Net.HttpMethods.GET)
            .url(url)
            .timeout(5000)
            .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int status = httpResponse.getStatus().getStatusCode();
                //String json = httpResponse.getResultAsString();
                byte[] bytes = httpResponse.getResult();
                String json = new String(bytes, StandardCharsets.UTF_8);

                System.out.println("HTTP STATUS: " + status);

                // uspešno
                if (status == 200 && json != null && !json.isEmpty()) {
                    try {
                        System.out.println(json);
                        callback.onSuccess(parse(json));
                    } catch (Exception e) {
                        callback.onFailure(status, "JSON parse failed: " + e.getMessage());
                    }
                } else
                    callback.onFailure(status, "Invalid response body");
            }

            // preklic ali neuspeh
            @Override
            public void cancelled() {
                callback.onFailure(-1, "Request cancelled");
            }

            @Override
            public void failed(Throwable t) {
                callback.onFailure(-1, t.getMessage());
            }
        });
    }
}
