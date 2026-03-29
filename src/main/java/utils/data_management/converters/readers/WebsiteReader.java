package utils.data_management.converters.readers;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.writers.Writer;
import utils.data_management.parsing.StringParserException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class WebsiteReader<T extends CustomSerializable> extends StringReader<T> {

    // ─── Constructors ─── //

    public WebsiteReader(Writer<T> writer) {
        super(writer);
    }

    // ─── Utility methods ─── //

    protected String scrap(String url) throws IOException, InterruptedException, StringParserException, URISyntaxException {
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        return response.body();
    }

}
