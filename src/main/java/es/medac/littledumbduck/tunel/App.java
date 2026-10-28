package es.medac.littledumbduck.tunel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * Clase principal de la Sala 4: El Espejo (Túnel del Terror).
 * Desarrollo de Interfaces - MEDAC
 */
public class App extends Application {

    // Constante PROPIETARIO requerida por la rúbrica
    public static final String PROPIETARIO = "littledumbduck";

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sala.fxml"));
        Parent root = loader.load();
        SalaController controller = loader.getController();

        Scene scene = new Scene(root);

        // Teclas de control rápido:
        // - F12: reinicio manual inmediato entre grupos de visitantes
        // - F9: salto directo al jumpscare para betatesting rápido
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F12) {
                if (controller != null) {
                    controller.reiniciarSala();
                }
            } else if (event.getCode() == KeyCode.F9) {
                if (controller != null) {
                    controller.ejecutarSustoRepentino();
                }
            }
        });

        // Título de la ventana con la constante PROPIETARIO
        primaryStage.setTitle("Túnel del Terror - Sala 4: El Espejo | " + PROPIETARIO);
        primaryStage.setScene(scene);

        // Requisitos de pantalla completa de la rúbrica
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
