package es.medac.littledumbduck.tunel;

import java.io.InputStream;
import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Controlador para la Sala 4: El Espejo (Túnel del Terror).
 * Reto 3: Animaciones visuales con Transition/Timeline, susto controlado
 * (escala y aparición sin destellos estroboscópicos por seguridad de fotosensibilidad),
 * música ambiente en bucle con MediaPlayer y efecto de impacto con AudioClip.
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
    private FadeTransition animacionNiebla;
    private ScaleTransition animacionLatidoLetra;
    
    // Elementos de audio requeridos por la rúbrica
    private MediaPlayer reproductorAmbiente;
    private AudioClip audioClipSusto;

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
        configurarAnimacionesAmbiente();
        inicializarAudio();
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
            System.err.println("Aviso: No se pudo cargar la fuente personalizada: " + e.getMessage());
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
            System.err.println("Aviso al cargar imágenes: " + e.getMessage());
        }
    }

    /**
     * Configura el bucle de sonido ambiental con MediaPlayer y el efecto de impacto con AudioClip.
     */
    private void inicializarAudio() {
        // Carga de sonido ambiente (busca mp3 y luego wav)
        try {
            URL ambienteUrl = getClass().getResource("sounds/ambiente.mp3");
            if (ambienteUrl == null) {
                ambienteUrl = getClass().getResource("sounds/ambiente.wav");
            }
            if (ambienteUrl != null) {
                Media mediaAmbiente = new Media(ambienteUrl.toExternalForm());
                reproductorAmbiente = new MediaPlayer(mediaAmbiente);
                reproductorAmbiente.setCycleCount(MediaPlayer.INDEFINITE);
                reproductorAmbiente.setVolume(0.55);
                reproductorAmbiente.play();
            } else {
                System.out.println("Nota: Coloque 'ambiente.mp3' en sounds/ para activar el audio de fondo.");
            }
        } catch (Exception e) {
            System.err.println("Aviso al iniciar MediaPlayer de ambiente: " + e.getMessage());
        }

        // Carga de efecto de susto para AudioClip
        try {
            URL sustoUrl = getClass().getResource("sounds/susto.mp3");
            if (sustoUrl == null) {
                sustoUrl = getClass().getResource("sounds/susto.wav");
            }
            if (sustoUrl != null) {
                audioClipSusto = new AudioClip(sustoUrl.toExternalForm());
                audioClipSusto.setVolume(1.0);
            } else {
                System.out.println("Nota: Coloque 'susto.mp3' o 'susto.wav' en sounds/ para el efecto sonoro.");
            }
        } catch (Exception e) {
            System.err.println("Aviso al preparar AudioClip de susto: " + e.getMessage());
        }
    }

    /**
     * Animaciones visuales ambientales continuas para generar tensión antes del susto.
     */
    private void configurarAnimacionesAmbiente() {
        animacionNiebla = new FadeTransition(Duration.seconds(3.5), capaNiebla);
        animacionNiebla.setFromValue(0.15);
        animacionNiebla.setToValue(0.45);
        animacionNiebla.setAutoReverse(true);
        animacionNiebla.setCycleCount(Timeline.INDEFINITE);
        animacionNiebla.play();
    }

    /**
     * Configuración de eventos de interacción.
     */
    private void configurarInteraccionBase() {
        // Tocar el cristal del espejo directamente desencadena el susto
        contenedorEspejo.setOnMouseClicked(event -> onInteractuar());

        // Pulsar Enter en el campo de texto desencadena la interacción
        txtEntrada.setOnAction(event -> onInteractuar());
    }

    /**
     * Temporizador de piedad de 60 segundos requerido por la rúbrica.
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
     * Lógica de susto controlado con Transition y entrega de la letra D con efecto visual.
     * Seguro para fotosensibilidad (sin destellos estroboscópicos).
     */
    public void ejecutarSecuenciaSustoYEntregaLetra(boolean porPiedad) {
        resuelto = true;
        detenerTemporizadores();

        // Reproducir efecto sonoro de susto
        if (audioClipSusto != null) {
            audioClipSusto.play();
        }

        // Ocultar caja de interacción
        cajaInteraccion.setVisible(false);
        txtEntrada.setDisable(true);
        btnInteractuar.setDisable(true);

        if (!porPiedad) {
            lblMensajeEstado.setText("¡EL REFLEJO TE HA RECLAMADO!");
        }

        // Susto controlado: Escala y Fade in suave del rostro espectral
        imgEspectro.setScaleX(0.7);
        imgEspectro.setScaleY(0.7);
        imgEspectro.setOpacity(0.0);

        FadeTransition aparicionEspectro = new FadeTransition(Duration.millis(350), imgEspectro);
        aparicionEspectro.setFromValue(0.0);
        aparicionEspectro.setToValue(1.0);

        ScaleTransition zoomEspectro = new ScaleTransition(Duration.millis(450), imgEspectro);
        zoomEspectro.setFromValue(0.7);
        zoomEspectro.setToValue(1.15);

        ParallelTransition sustoAnimacion = new ParallelTransition(aparicionEspectro, zoomEspectro);

        // Al terminar el impacto del susto, revelamos la letra 'D'
        sustoAnimacion.setOnFinished(e -> {
            // Disminuir ligeramente la opacidad del espectro para resaltar la letra
            FadeTransition atenuarEspectro = new FadeTransition(Duration.millis(600), imgEspectro);
            atenuarEspectro.setToValue(0.5);
            atenuarEspectro.play();

            revelarLetraDConEfecto();
        });

        sustoAnimacion.play();
    }

    /**
     * Revela la letra D con efectos de animación (Fade, Scale y pulso continuo).
     */
    private void revelarLetraDConEfecto() {
        lblLetraD.setScaleX(0.4);
        lblLetraD.setScaleY(0.4);
        lblLetraD.setOpacity(0.0);
        lblSubLetra.setOpacity(0.0);

        FadeTransition fadeLetra = new FadeTransition(Duration.millis(700), lblLetraD);
        fadeLetra.setFromValue(0.0);
        fadeLetra.setToValue(1.0);

        ScaleTransition scaleLetra = new ScaleTransition(Duration.millis(700), lblLetraD);
        scaleLetra.setFromValue(0.4);
        scaleLetra.setToValue(1.0);

        FadeTransition fadeSubLetra = new FadeTransition(Duration.millis(800), lblSubLetra);
        fadeSubLetra.setFromValue(0.0);
        fadeSubLetra.setToValue(1.0);

        ParallelTransition transicionRevelacion = new ParallelTransition(fadeLetra, scaleLetra, fadeSubLetra);
        transicionRevelacion.setOnFinished(ev -> {
            // Efecto de pulso/latido espectral en la letra D
            animacionLatidoLetra = new ScaleTransition(Duration.seconds(1.2), lblLetraD);
            animacionLatidoLetra.setFromValue(1.0);
            animacionLatidoLetra.setToValue(1.08);
            animacionLatidoLetra.setAutoReverse(true);
            animacionLatidoLetra.setCycleCount(Timeline.INDEFINITE);
            animacionLatidoLetra.play();

            programarAutorreinicio();
        });

        transicionRevelacion.play();
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

        if (animacionLatidoLetra != null) {
            animacionLatidoLetra.stop();
        }

        // Restaurar estado visual inicial
        imgEspectro.setOpacity(0.0);
        imgEspectro.setScaleX(1.0);
        imgEspectro.setScaleY(1.0);

        lblLetraD.setOpacity(0.0);
        lblLetraD.setScaleX(1.0);
        lblLetraD.setScaleY(1.0);

        lblSubLetra.setOpacity(0.0);
        lblMensajeEstado.setText("");

        txtEntrada.clear();
        txtEntrada.setDisable(false);
        btnInteractuar.setDisable(false);
        cajaInteraccion.setVisible(true);

        // Volver a iniciar el temporizador de 60 segundos
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
