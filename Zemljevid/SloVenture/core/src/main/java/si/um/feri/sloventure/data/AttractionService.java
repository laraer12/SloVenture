package si.um.feri.sloventure.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.Net.HttpResponseListener;

public class AttractionService {
    private static final String URL = "http://localhost:3001/attractions"; // kasneje menjano s strežniškim URL

    public interface Callback {
        void onSuccess(String json);
        void onFailure(int status, String message);
    }

    public static void fetchAllAttractions(Callback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        Net.HttpRequest request = requestBuilder.newRequest()
            .method(Net.HttpMethods.GET)
            .url(URL)
            .timeout(5000)
            .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int status = httpResponse.getStatus().getStatusCode();
                String json = httpResponse.getResultAsString();

                System.out.println("HTTP STATUS: " + status);

                // uspešno
                if (status == 200 && json != null && !json.isEmpty())
                    callback.onSuccess(json);

                else
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
