package models.services;

import com.google.zxing.WriterException;
import models.dao.DocumentDAO;
import models.data.DatabaseConnection;
import models.entities.Document;
import models.entities.User;
import org.json.JSONArray;
import org.json.JSONException;
import utils.QRCodeGenerator;
import utils.QRCodeReader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class cung cấp các chức năng xử lý logic nghiệp vụ liên quan đến tài liệu (sách),
 * bao gồm tìm kiếm, thêm, cập nhật, xóa, điều chỉnh số lượng, mã QR và tương tác với API Google Books.
 */
public class DocumentService {
    private final DocumentDAO documentDAO;

    /**
     * Service class cung cấp các chức năng xử lý logic nghiệp vụ liên quan đến tài liệu (sách),
     * bao gồm tìm kiếm, thêm, cập nhật, xóa, điều chỉnh số lượng, mã QR và tương tác với API Google Books.
     */
    public DocumentService() {
        this.documentDAO = new DocumentDAO();
    }


    /**
     * Điều chỉnh số lượng sách hiện có trong kho.
     *
     * @param isbn         Mã ISBN của sách.
     * @param changeAmount Số lượng thay đổi (dương để tăng, âm để giảm).
     * @return true nếu cập nhật thành công.
     * @throws SQLException             nếu có lỗi khi truy cập cơ sở dữ liệu.
     * @throws IllegalArgumentException nếu số lượng sau cập nhật bị âm.
     */

    public static boolean adjustBookQuantity(String isbn, int changeAmount) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int currentQty = DocumentDAO.getQuantityByIsbn(conn, isbn);
            if (currentQty + changeAmount < 0) {
                throw new IllegalArgumentException("Số lượng sau khi cập nhật không được âm.");
            }
            DocumentDAO.updateBookQuantity(conn, isbn, changeAmount);
            return true;
        }
    }

    /**
     * Tìm kiếm sách theo ISBN. Nếu người dùng là admin, có thể lấy thông tin từ Google Books nếu không có trong DB.
     *
     * @param isbn        Mã ISBN của sách cần tìm.
     * @param currentUser Người dùng hiện tại đang tìm kiếm.
     * @return Đối tượng Document nếu tìm thấy, ngược lại trả về null.
     * @throws Exception nếu có lỗi xảy ra trong quá trình tìm kiếm.
     */
    public Document searchBook(String isbn, User currentUser) throws SQLException, Exception {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }

        boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
        Document fetchedDoc = null;

        // Luôn lấy từ cơ sở dữ liệu trước
        fetchedDoc = documentDAO.getBookByIsbn(isbn);

        // Nếu là admin và sách không tồn tại trong DB, thử Google Books API
        if (isAdmin && fetchedDoc == null) {
            try {
                fetchedDoc = GoogleBooksAPIService.fetchBookInfo(isbn);
                if (fetchedDoc != null) {
                    // Thiết lập Google Books URL nếu cần
                    if (fetchedDoc.getGoogleBooksUrl() == null) {
                        fetchedDoc.setGoogleBooksUrl("https://books.google.com/books?isbn=" + isbn);
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi Google Books API: " + e.getMessage());
            }
        } else if (fetchedDoc != null && isAdmin) {
            // Nếu sách tồn tại trong DB và là admin, cập nhật thông tin từ Google Books API
            try {
                Document apiDoc = GoogleBooksAPIService.fetchBookInfo(isbn);
                if (apiDoc != null) {
                    fetchedDoc.setTitle(apiDoc.getTitle() != null ? apiDoc.getTitle() : fetchedDoc.getTitle());
                    fetchedDoc.setAuthors(apiDoc.getAuthors() != null ? apiDoc.getAuthors() : fetchedDoc.getAuthors());
                    fetchedDoc.setPublisher(apiDoc.getPublisher() != null ? apiDoc.getPublisher() : fetchedDoc.getPublisher());
                    fetchedDoc.setPublishedDate(apiDoc.getPublishedDate() != null ? apiDoc.getPublishedDate() : fetchedDoc.getPublishedDate());
                    fetchedDoc.setDescription(apiDoc.getDescription() != null ? apiDoc.getDescription() : fetchedDoc.getDescription());
                    fetchedDoc.setThumbnailUrl(apiDoc.getThumbnailUrl() != null ? apiDoc.getThumbnailUrl() : fetchedDoc.getThumbnailUrl());
                    fetchedDoc.setGoogleBooksUrl(apiDoc.getGoogleBooksUrl() != null ? apiDoc.getGoogleBooksUrl() : "https://books.google.com/books?isbn=" + isbn);
                }
            } catch (Exception e) {
                System.err.println("Lỗi Google Books API: " + e.getMessage());
            }
        }

        System.out.println("Tìm kiếm ISBN '" + isbn + "' -> Kết quả: " + (fetchedDoc != null ? fetchedDoc.getTitle() : "null"));
        return fetchedDoc;
    }

    /**
     * Kiểm tra sách có tồn tại trong cơ sở dữ liệu hay không.
     *
     * @param isbn Mã ISBN của sách.
     * @return true nếu sách tồn tại, ngược lại false.
     * @throws SQLException nếu có lỗi khi truy cập DB.
     */
    public boolean bookExists(String isbn) throws SQLException {
        return documentDAO.bookExists(isbn);
    }


    /**
     * Thêm sách mới vào hệ thống (chỉ admin). Đồng thời tạo mã QR và lưu đường dẫn.
     *
     * @param document    Đối tượng sách cần thêm.
     * @param currentUser Người dùng hiện tại.
     * @return true nếu thêm thành công, ngược lại false.
     * @throws SQLException    nếu lỗi truy cập DB.
     * @throws WriterException nếu lỗi tạo mã QR.
     * @throws IOException     nếu lỗi ghi tệp QR code.
     */
    public boolean addBook(Document document, User currentUser) throws SQLException, WriterException, IOException {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            System.err.println("Thêm sách thất bại: người dùng không có quyền admin.");
            return false;
        }
        if (document == null || document.getIsbn() == null) {
            System.err.println("Thêm sách thất bại: thông tin sách không hợp lệ.");
            return false;
        }
        if (documentDAO.bookExists(document.getIsbn())) {
            System.err.println("Thêm sách thất bại: ISBN đã tồn tại.");
            return false;
        }

        // Fetch book info from Google Books API if needed
        if (document.getGoogleBooksUrl() == null || document.getGoogleBooksUrl().isEmpty()) {
            try {
                Document apiDoc = GoogleBooksAPIService.fetchBookInfo(document.getIsbn());
                if (apiDoc != null && apiDoc.getGoogleBooksUrl() != null) {
                    document.setGoogleBooksUrl(apiDoc.getGoogleBooksUrl());
                } else {
                    document.setGoogleBooksUrl("https://books.google.com/books?isbn=" + document.getIsbn());
                }
            } catch (Exception e) {
                System.err.println("Lỗi Google Books API: " + e.getMessage());
                document.setGoogleBooksUrl("https://books.google.com/books?isbn=" + document.getIsbn());
            }
        }

        // Generate and save QR code
        try {
            String googleBooksUrl = document.getGoogleBooksUrl();
            BufferedImage qrCodeImage = QRCodeGenerator.generateQRCodeImage(googleBooksUrl, 250, 250);
            String qrCodeFilePath = "qr_codes/qr_code_" + document.getIsbn() + ".png";
            QRCodeGenerator.saveQRCode(qrCodeImage, qrCodeFilePath);
            document.setQrCodePath(qrCodeFilePath);
        } catch (WriterException | IOException e) {
            System.err.println("Lỗi tạo mã QR: " + e.getMessage());
            throw e;
        }

        Document standardizedDoc = standardizeDocument(document);
        return documentDAO.addBook(standardizedDoc);
    }

    /**
     * Cập nhật thông tin sách (chỉ admin).
     *
     * @param document    Thông tin sách cần cập nhật.
     * @param currentUser Người dùng hiện tại.
     * @return true nếu cập nhật thành công, ngược lại false.
     * @throws SQLException nếu lỗi DB.
     */
    public boolean updateBook(Document document, User currentUser) throws SQLException {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            System.err.println("Cập nhật sách thất bại: người dùng không có quyền admin.");
            return false;
        }
        if (document == null || document.getIsbn() == null) {
            System.err.println("Cập nhật sách thất bại: thông tin sách không hợp lệ.");
            return false;
        }
        if (!documentDAO.bookExists(document.getIsbn())) {
            System.err.println("Cập nhật sách thất bại: sách không tồn tại trong DB.");
            return false;
        }
        Document standardizedDoc = standardizeDocument(document);
        return documentDAO.updateBook(standardizedDoc);
    }

    /**
     * Xóa sách khỏi hệ thống (chỉ admin).
     *
     * @param isbn        Mã ISBN của sách cần xóa.
     * @param currentUser Người dùng hiện tại.
     * @return true nếu xóa thành công, ngược lại false.
     * @throws SQLException nếu lỗi DB.
     */
    public boolean deleteBook(String isbn, User currentUser) throws SQLException {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            System.err.println("Xóa sách thất bại: người dùng không có quyền admin.");
            return false;
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            System.err.println("Xóa sách thất bại: ISBN không hợp lệ.");
            return false;
        }
        if (!documentDAO.bookExists(isbn)) {
            System.err.println("Xóa sách thất bại: sách không tồn tại trong DB.");
            return false;
        }
        return documentDAO.deleteBook(isbn);
    }


    /**
     * Chuẩn hóa thông tin sách trước khi lưu trữ, đặc biệt là định dạng ngày xuất bản.
     *
     * @param doc Tài liệu cần chuẩn hóa.
     * @return Document đã chuẩn hóa.
     */
    private Document standardizeDocument(Document doc) {
        String publishDate = doc.getPublishedDate();
        if (publishDate != null && publishDate.matches("\\d{4}")) {
            publishDate = publishDate + "-01-01";
        } else if (publishDate == null || publishDate.trim().isEmpty() || "Không rõ".equalsIgnoreCase(publishDate)) {
            publishDate = null;
        }

        String googleBooksUrl = doc.getGoogleBooksUrl();
        if (googleBooksUrl == null || googleBooksUrl.isEmpty()) {
            googleBooksUrl = "https://books.google.com/books?isbn=" + doc.getIsbn();
        }

        return new Document(doc.getIsbn(), doc.getTitle(), doc.getAuthors(), doc.getPublisher(),
                publishDate, doc.getDescription(), doc.getThumbnailUrl(), doc.getQrCodePath(), googleBooksUrl);
    }


    /**
     * Tìm kiếm sách theo tiêu đề, tác giả, hoặc ngày xuất bản.
     * Hỗ trợ lọc theo tác giả phía client nếu chuỗi tìm kiếm quá ngắn.
     *
     * @param title       Tên sách (có thể null).
     * @param author      Tác giả (có thể null).
     * @param publishDate Ngày xuất bản (có thể null).
     * @return Danh sách các sách phù hợp với tiêu chí tìm kiếm.
     * @throws SQLException nếu lỗi DB.
     */
    public List<Document> searchBooks(String title, String author, String publishDate) throws SQLException {
        List<Document> results = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();
        boolean performClientSideAuthorFilter = false;
        String clientSideAuthorQuery = null;

        if (title != null && !title.trim().isEmpty()) {
            sqlBuilder.append(" AND LOWER(title) LIKE LOWER(?)");
            params.add("%" + title.trim() + "%");
        }

        if (author != null && !author.trim().isEmpty()) {
            String trimmedAuthor = author.trim().toLowerCase();
            if (trimmedAuthor.length() <= 3) {
                performClientSideAuthorFilter = true;
                clientSideAuthorQuery = trimmedAuthor;
            } else {
                sqlBuilder.append(" AND LOWER(authors) LIKE LOWER(?)");
                params.add("%" + trimmedAuthor + "%");
            }
        }

        if (publishDate != null && !publishDate.trim().isEmpty()) {
            sqlBuilder.append(" AND publish_date LIKE ?");
            params.add(publishDate.trim() + "%");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String authorsJson = rs.getString("authors");
                String[] authorsArray;
                if (authorsJson != null && !authorsJson.isEmpty()) {
                    try {
                        JSONArray jsonAuthors = new JSONArray(authorsJson);
                        authorsArray = new String[jsonAuthors.length()];
                        for (int i = 0; i < jsonAuthors.length(); i++) {
                            authorsArray[i] = jsonAuthors.getString(i);
                        }
                    } catch (JSONException e) {
                        authorsArray = new String[]{authorsJson};
                        System.err.println("Lỗi parse JSON cho authors: " + authorsJson + " - " + e.getMessage());
                    }
                } else {
                    authorsArray = new String[0];
                }

                Document doc = new Document(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        authorsArray,
                        rs.getString("publisher"),
                        rs.getString("publish_date"),
                        rs.getString("description"),
                        rs.getString("thumbnail_url"),
                        rs.getString("qr_code_path")
                );

                if (performClientSideAuthorFilter) {
                    boolean match = false;
                    if (doc.getAuthors() != null) {
                        for (String anAuthor : doc.getAuthors()) {
                            if (anAuthor != null && anAuthor.toLowerCase().startsWith(clientSideAuthorQuery)) {
                                match = true;
                                break;
                            }
                        }
                    }
                    if (match) {
                        results.add(doc);
                    }
                } else {
                    results.add(doc);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi tìm kiếm sách: " + e.getMessage());
            throw e;
        }
        return results;
    }

    /**
     * Tạo lại mã QR cho sách với đường dẫn đến Google Books URL.
     *
     * @param document    Sách cần tạo lại QR.
     * @param currentUser Người dùng hiện tại.
     * @return Đường dẫn đến tệp QR code mới.
     * @throws Exception nếu lỗi xảy ra trong quá trình tạo hoặc xác minh QR.
     */
    public String regenerateQRCode(Document document, User currentUser) throws Exception {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            throw new SecurityException("Chỉ admin mới có thể tạo lại mã QR.");
        }
        if (document == null || document.getIsbn() == null) {
            throw new IllegalArgumentException("Thông tin sách không hợp lệ.");
        }
        if (!documentDAO.bookExists(document.getIsbn())) {
            throw new IllegalArgumentException("Sách không tồn tại trong cơ sở dữ liệu.");
        }

        String googleBooksUrl = document.getGoogleBooksUrl();
        if (googleBooksUrl == null || googleBooksUrl.isEmpty()) {
            Document apiDoc = GoogleBooksAPIService.fetchBookInfo(document.getIsbn());
            googleBooksUrl = apiDoc != null && apiDoc.getGoogleBooksUrl() != null
                    ? apiDoc.getGoogleBooksUrl()
                    : "https://books.google.com/books?isbn=" + document.getIsbn();
        }
        BufferedImage qrCodeImage = QRCodeGenerator.generateQRCodeImage(googleBooksUrl, 250, 250);
        String qrCodeFilePath = "qr_codes/qr_code_" + document.getIsbn() + ".png";
        QRCodeGenerator.saveQRCode(qrCodeImage, qrCodeFilePath);

        String qrContent = QRCodeReader.readQRCode(qrCodeFilePath);
        if (!qrContent.equals(googleBooksUrl)) {
            throw new IOException("QR code verification failed: content does not match URL.");
        }

        String sql = "UPDATE books SET qr_code_path = ?, google_books_url = ? WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, qrCodeFilePath);
            stmt.setString(2, googleBooksUrl);
            stmt.setString(3, document.getIsbn());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Không thể cập nhật qr_code_path và google_books_url cho ISBN: " + document.getIsbn());
            }
        }

        document.setGoogleBooksUrl(googleBooksUrl);
        return qrCodeFilePath;
    }
}