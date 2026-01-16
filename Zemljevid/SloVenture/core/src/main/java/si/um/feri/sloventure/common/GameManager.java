    package si.um.feri.sloventure.common;

    import com.badlogic.gdx.Gdx;
    import com.badlogic.gdx.files.FileHandle;
    import com.badlogic.gdx.utils.Json;
    import com.badlogic.gdx.utils.JsonWriter;

    import java.util.ArrayList;
    import java.util.List;

    import si.um.feri.sloventure.data.attraction.AttractionData;

    public class GameManager {
        public static final GameManager INSTANCE = new GameManager();
        private static final String ATTRACTIONS_FILE = "attractions.json";
        private final Json json;

        private GameManager() {
            json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            json.setUsePrototypes(false);
        }

        public List<AttractionData> loadAttractions() {
            FileHandle file = Gdx.files.local(ATTRACTIONS_FILE);

            List<AttractionData> attractions = new ArrayList<>();
            if (!file.exists()) {
                saveAttractions(attractions);
                return attractions;
            }

            try {
                AttractionData[] arr = json.fromJson(AttractionData[].class, file);
                attractions = new ArrayList<>(List.of(arr));
            } catch (Exception e) {
                e.printStackTrace();
                attractions = new ArrayList<>();
            }

            return attractions;
        }

        public void saveAttractions(List<AttractionData> attractions) {
            json.setUsePrototypes(false);
            String jsonString = json.toJson(attractions);

            // IZPIS V KONZOLO
            System.out.println("Saving attractions JSON:");
            System.out.println(jsonString);
            FileHandle file = Gdx.files.local(ATTRACTIONS_FILE);
            file.writeString(json.toJson(attractions), false);
        }
    }
