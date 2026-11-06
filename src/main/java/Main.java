import javafx.application.Application;
import javafx.stage.Stage;

import com.example.temperatureconverter.Controller.TemperatureController;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        new TemperatureController().start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
