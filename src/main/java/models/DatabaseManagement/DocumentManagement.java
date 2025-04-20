package models.DatabaseManagement;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONObject;
import org.json.JSONArray;
import app.Document;

public class DocumentManagement {
    private static final String OPEN_LIBRARY_API = "https://openlibrary.org/api/books?bibkeys=ISBN:%s&format=json&jscmd=data";
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=isbn:%s";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static Document fetchBookInfo(String isbn) throws Exception {
        JSONObject bookData = fetchFromOpenLibrary(isbn);

        if (bookData == null) {
            System.out.println("Không tìm thấy trên Open Library, thử Google Books...");
            bookData = fetchFromGoogleBooks(isbn);
        }

        if (bookData != null) {
            return parseToDocument(bookData);
        }

        return null;
    }

    private static JSONObject fetchFromOpenLibrary(String isbn) throws Exception {
        String url = String.format(OPEN_LIBRARY_API, isbn);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        JSONObject jsonResponse = new JSONObject(response.body());
        String bookKey = "ISBN:" + isbn;

        if (!jsonResponse.has(bookKey)) {
            return null;
        }

        return jsonResponse.getJSONObject(bookKey);
    }

    private static JSONObject fetchFromGoogleBooks(String isbn) throws Exception {
        String url = String.format(GOOGLE_BOOKS_API, isbn);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 || response.body().contains("\"totalItems\":0")) {
            return null;
        }

        JSONObject jsonResponse = new JSONObject(response.body());
        return jsonResponse.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo");
    }

    private static Document parseToDocument(JSONObject bookData) {
        String isbs = "";
        String title = "";
        String[] authors = new String[0];
        String publisher = "Không rõ";
        String publishedDate = "Không rõ";
        String description = "Không có mô tả";

        // Xử lý tiêu đề
        if (bookData.has("title")) {
            title = bookData.getString("title");
        }

        // Xử lý tác giả
        if (bookData.has("authors")) {
            JSONArray authorsArray = bookData.getJSONArray("authors");
            authors = new String[authorsArray.length()];

            for (int i = 0; i < authorsArray.length(); i++) {
                // Xử lý cả 2 định dạng (Open Library và Google Books)
                if (authorsArray.get(i) instanceof JSONObject) {
                    authors[i] = authorsArray.getJSONObject(i).getString("name");
                } else {
                    authors[i] = authorsArray.getString(i);
                }
            }
        }


        if (bookData.has("publishers")) {
            publisher = bookData.getString("publishers");
        } else if (bookData.has("publisher")) {
            publisher = bookData.getString("publisher");
        }


        if (bookData.has("publish_date")) {
            publishedDate = bookData.getString("publish_date");
        } else if (bookData.has("publishedDate")) {
            publishedDate = bookData.getString("publishedDate");
        }


        if (bookData.has("notes")) {
            description = bookData.getString("notes");
        } else if (bookData.has("description")) {
            description = bookData.getString("description");
        }

        return new Document(isbs, title, authors, publisher, publishedDate, description);
    }
}