package es.medac.littledumbduck.tunel;

import java.io.InputStream;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

/**
 * Controlador para la Sala 4: El Espejo (Túnel del Terror).
 * Maneja la vista FXML, el tema oscuro CSS, la carga de la fuente gótica
 * y la estructura inicial de la interfaz.
 */
public class SalaController {

    @FXML private StackPane raiz;
    @FXML private StackPane capaAmbiente;
    @FXML private StackPane contenedorEspejo;
    @FXML private ImageView imgEspejo;
    @FXML private ImageView imgEspectro;
    @FXML private StackPane capaNiebla;
    @FXML private Label lblTitulo;
    @FXML private Label lblInstruccion;
    @FXML private Label lblLetraD;
    @FXML private Label lblSubLetra;
    @FXML private HBox cajaInteraccion;
    @FXML private TextField txtEntrada;
    @FXML private Button btnInteractuar;
    @FXML private Label lblMensajeEstado;

    private Font fuenteTerror;

    @FXML
    public void initialize() {
        cargarFuentePersonalizada();
        cargarImagenes();
        configurarInteraccionBase();
    }

    /**
     * Carga de fuente gótica/terrorífica mediante Font.loadFont como exige la rúbrica.
     */
    private void cargarFuentePersonalizada() {
        try {
            InputStream fontStream = getClass().getResourceAsStream("fonts/terror.ttf");
            if (fontStream != null) {
                fuenteTerror = Font.loadFont(fontStream, 60);
                if (fuenteTerror != null) {
                    lblTitulo.setFont(Font.font(fuenteTerror.getName(), 54));
                    lblLetraD.setFont(Font.font(fuenteTerror.getName(), 160));
                }
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la fuente personalizada: " + e.getMessage());
        }
    }

    /**
     * Carga las imágenes del espejo gótico y el espectro del susto.
     */
    private void cargarImagenes() {
        try {
            InputStream espejoStream = getClass().getResourceAsStream("images/espejo.png");
            if (espejoStream != null) {
                imgEspejo.setImage(new Image(espejoStream));
            }

            InputStream espectroStream = getClass().getResourceAsStream("images/reflejo_espectro.png");
            if (espectroStream != null) {
                imgEspectro.setImage(new Image(espectroStream));
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imágenes: " + e.getMessage());
        }
    }

    /**
     * Configuración visual y eventos base de interacción.
     */
    private void configurarInteraccionBase() {
        // Tocar el marco del espejo directamente también invoca la interacción
        contenedorEspejo.setOnMouseClicked(event -> onInteractuar());

        // Pulsar Enter en el campo de texto invoca la interacción
        txtEntrada.setOnAction(event -> onInteractuar());
    }

    @FXML
    public void onInteractuar() {
        // Marcador para la lógica de validación y susto
    }

    public void reiniciarSala() {
        // Marcador para el reinicio
    }
}
