package Controller;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class BookCardControllerTest {

    private BookCardController controller;
    private VBox bookCard;
    private ImageView imageView;
    private Label titleLabel;
    private Label returnLabel;

    @BeforeAll
    public static void setupJavaFX() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        new JFXPanel();
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Không thể khởi tạo JavaFX");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        FxToolkit.registerPrimaryStage();

        controller = new BookCardController();
        bookCard = new VBox();
        imageView = new ImageView();
        titleLabel = new Label();
        returnLabel = new Label();

        // Inject các thành phần vào controller bằng reflection
        setField(controller, "bookCard", bookCard);
        setField(controller, "imageView", imageView);
        setField(controller, "titleLabel", titleLabel);
        setField(controller, "returnLabel", returnLabel);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testSetBookInfo_UpdatesUIComponents() {
        String title = "Test Book";
        String imagePath = "http://example.com/image.jpg";
        Runnable callback = () -> {};

        controller.setBookInfo(title, imagePath, callback);

        assertEquals(title, titleLabel.getText());
        assertNotNull(imageView.getImage());
        assertEquals(imagePath, imageView.getImage().getUrl());
    }

    @Test
    public void testSetBookInfo_NullCallback_DoesNotThrowException(FxRobot robot) {
        String title = "Test Book";
        String imagePath = "http://example.com/image.jpg";

        controller.setBookInfo(title, imagePath, null);

        // Kiểm tra UI được cập nhật
        assertEquals(title, titleLabel.getText());
        assertNotNull(imageView.getImage());
        assertEquals(imagePath, imageView.getImage().getUrl());

        // Nhấn vào returnLabel, không nên có lỗi
        robot.clickOn(returnLabel, MouseButton.PRIMARY);
    }

    @Test
    public void testSetBookInfo_OnReturnCallbackTriggered(FxRobot robot) throws Exception {
        String title = "Test Book";
        String imagePath = "http://example.com/image.jpg";
        final boolean[] callbackTriggered = {false};
        Runnable callback = () -> callbackTriggered[0] = true;

        controller.setBookInfo(title, imagePath, callback);

        // Nhấn vào returnLabel để kích hoạt callback
        robot.clickOn(returnLabel, MouseButton.PRIMARY);

        assertTrue(callbackTriggered[0], "Callback trả sách nên được kích hoạt");
    }

    @Test
    public void testGetCard_ReturnsCorrectVBox() {
        VBox returnedCard = controller.getCard();
        assertEquals(bookCard, returnedCard);
    }

    @Test
    public void testSetBookInfo_InvalidImagePath_DoesNotThrowException() {
        String title = "Test Book";
        String invalidImagePath = "invalid://url";
        Runnable callback = () -> {};

        // Không nên ném ngoại lệ, Image sẽ không tải được nhưng vẫn chạy
        controller.setBookInfo(title, invalidImagePath, callback);

        assertEquals(title, titleLabel.getText());
        assertNotNull(imageView.getImage()); // Image vẫn được tạo nhưng không tải được
    }
}