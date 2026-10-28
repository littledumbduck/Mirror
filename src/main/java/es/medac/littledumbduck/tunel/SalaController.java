package es.medac.littledumbduck.tunel;

import java.io.InputStream;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Controlador para la Sala 4: El Espejo (Túnel del Terror).
 * Reto 2: Lógica de interacción, entrega de la letra D,
 * temporizador de piedad (60s) y autorreinicio (automático y con tecla F12).
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
    private Timeline temporizadorPiedad;
    private PauseTransition temporizadorReinicio;
    private boolean resuelto = false;

    // Constante de tiempo de piedad requerida por la rúbrica (60 segundos)
    private static final int SEGUNDOS_PIEDAD = 60;
    // Tiempo de exhibición de la letra antes del autorreinicio (12 segundos)
    private static final int SEGUNDOS_AUTORREINICIO = 12;

    @FXML
    public void initialize() {
        cargarFuentePersonalizada();
        cargarImagenes();
        configurarInteraccionBase();
        iniciarTemporizadorPiedad();
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
        // Tocar el cristal del espejo directamente desencadena el susto
        contenedorEspejo.setOnMouseClicked(event -> onInteractuar());

        // Pulsar Enter en el campo de texto desencadena la interacción
        txtEntrada.setOnAction(event -> onInteractuar());
    }

    /**
     * Temporizador de piedad de 60 segundos requerido por la rúbrica:
     * Si no interactúan en 60 segundos, la sala se apiada y entrega la letra.
     */
    private void iniciarTemporizadorPiedad() {
        detenerTemporizadores();
        temporizadorPiedad = new Timeline(new KeyFrame(Duration.seconds(SEGUNDOS_PIEDAD), event -> {
            if (!resuelto) {
                lblMensajeEstado.setText("El espejo se apiada de tu terror... contempladlo.");
                ejecutarSecuenciaSustoYEntregaLetra(true);
            }
        }));
        temporizadorPiedad.setCycleCount(1);
        temporizadorPiedad.play();
    }

    @FXML
    public void onInteractuar() {
        if (resuelto) {
            return;
        }
        ejecutarSecuenciaSustoYEntregaLetra(false);
    }

    /**
     * Lógica de susto y entrega de la letra D.
     */
    public void ejecutarSecuenciaSustoYEntregaLetra(boolean porPiedad) {
        resuelto = true;
        detenerTemporizadores();

        // Ocultar caja de controles para centrar el foco en el espejo
        cajaInteraccion.setVisible(false);
        txtEntrada.setDisable(true);
        btnInteractuar.setDisable(true);

        if (!porPiedad) {
            lblMensajeEstado.setText("¡EL REFLEJO TE HA RECLAMADO!");
        }

        // Mostrar la aparición y la letra 'D'
        imgEspectro.setOpacity(1.0);
        lblLetraD.setOpacity(1.0);
        lblSubLetra.setOpacity(1.0);

        programarAutorreinicio();
    }

    /**
     * Programa el autorreinicio automático tras mostrar la letra para el siguiente grupo.
     */
    private void programarAutorreinicio() {
        temporizadorReinicio = new PauseTransition(Duration.seconds(SEGUNDOS_AUTORREINICIO));
        temporizadorReinicio.setOnFinished(event -> reiniciarSala());
        temporizadorReinicio.play();
    }

    /**
     * Reinicia la sala al estado inicial (puede invocarse automáticamente o con la tecla F12).
     */
    public void reiniciarSala() {
        detenerTemporizadores();
        resuelto = false;

        // Restaurar visibilidad y opacidades iniciales
        imgEspectro.setOpacity(0.0);
        lblLetraD.setOpacity(0.0);
        lblSubLetra.setOpacity(0.0);
        lblMensajeEstado.setText("");
        txtEntrada.clear();
        txtEntrada.setDisable(false);
        btnInteractuar.setDisable(false);
        cajaInteraccion.setVisible(true);

        // Volver a iniciar el temporizador de 60 segundos para el siguiente grupo
        iniciarTemporizadorPiedad();
    }

    private void detenerTemporizadores() {
        if (temporizadorPiedad != null) {
            temporizadorPiedad.stop();
        }
        if (temporizadorReinicio != null) {
            temporizadorReinicio.stop();
        }
    }
}
