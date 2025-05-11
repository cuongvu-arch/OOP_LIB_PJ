package models.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.json.JSONObject;
import org.json.JSONArray;

import models.entities.Document;

public class GoogleBooksAPIService {
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=isbn:%s";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static Document fetchBookInfo(String isbn) throws Exception {
        JSONObject bookData = fetchFromGoogleBooks(isbn);

        if (bookData != null) {
            return parseToDocument(isbn, bookData);
        }

        return null;
    }

    private static JSONObject fetchFromGoogleBooks(String isbn) throws Exception {
        String url = String.format(GOOGLE_BOOKS_API, isbn);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("API request failed with status code: " + response.statusCode());
            return null;
        }

        JSONObject jsonResponse = new JSONObject(response.body());
        if (jsonResponse.has("totalItems") && jsonResponse.getInt("totalItems") == 0) {
            System.out.println("No books found for ISBN: " + isbn);
            return null;
        }

        if (!jsonResponse.has("items")) {
            System.err.println("Response does not contain 'items' key.");
            return null;
        }

        return jsonResponse.getJSONArray("items").getJSONObject(0);
    }

    private static Document parseToDocument(String isbn, JSONObject bookItem) {
        JSONObject bookData = bookItem.getJSONObject("volumeInfo");
        String title = "";
        String[] authors = new String[0];
        String publisher = "Không rõ";
        String publishedDate = "Không rõ";
        String description = "Không có mô tả";
        String thumbnailUrl = null;
        // Sử dụng canonicalVolumeLink hoặc id cho googleBooksUrl
        String googleBooksUrl = bookItem.optString("canonicalVolumeLink", null);
        if (googleBooksUrl == null && bookItem.has("id")) {
            googleBooksUrl = "https://books.google.com/books?id=" + bookItem.getString("id");
        }
        if (googleBooksUrl == null) {
            googleBooksUrl = "https://books.google.com/books?isbn=" + isbn;
        }

        // Tiêu đề
        if (bookData.has("title")) {
            title = bookData.getString("title");
        }

        // Tác giả
        if (bookData.has("authors")) {
            JSONArray authorsArray = bookData.getJSONArray("authors");
            authors = new String[authorsArray.length()];
            for (int i = 0; i < authorsArray.length(); i++) {
                authors[i] = authorsArray.optString(i, "Không rõ");
            }
        }

        // Nhà xuất bản
        if (bookData.has("publisher")) {
            publisher = bookData.optString("publisher", "Không rõ");
        }

        // Ngày xuất bản
        if (bookData.has("publishedDate")) {
            publishedDate = bookData.optString("publishedDate", "Không rõ");
        }

        // Mô tả
        if (bookData.has("description")) {
            Object descObj = bookData.get("description");
            if (descObj instanceof String) {
                description = (String) descObj;
            } else if (descObj instanceof JSONObject) {
                description = ((JSONObject) descObj).optString("text", "Không có mô tả");
            }
        }

        // Thumbnail
        if (bookData.has("imageLinks")) {
            JSONObject imageLinks = bookData.getJSONObject("imageLinks");
            thumbnailUrl = imageLinks.optString("thumbnail", null);
            if (thumbnailUrl != null) {
                thumbnailUrl = thumbnailUrl.replace("http://", "https://");
            }
        }

        // Sử dụng constructor 7 tham số và setter cho googleBooksUrl
        Document doc = new Document(isbn, title, authors, publisher, publishedDate, description, thumbnailUrl);
        doc.setGoogleBooksUrl(googleBooksUrl);
        return doc;
    }
}