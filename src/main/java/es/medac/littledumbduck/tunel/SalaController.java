package es.medac.littledumbduck.tunel;

import java.io.InputStream;
import java.net.URL;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Controlador para la Sala 4: El Espejo (Túnel del Terror).
 * - Espejo a gran escala ocupando casi toda la pantalla.
 * - Rostro reflejado con ojos cuyas pupilas siguen el cursor del ratón en tiempo real.
 * - Susto repentino e instantáneo (pantalla completa sin transición) con sonido durante 5 segundos.
 * - Entrega posterior de la letra secreta "D" con animación de latido.
 * - Temporizador de piedad (60s) y autorreinicio (o mediante tecla F12).
 */
public class SalaController {

    // =========================================================================
    // PALABRA DEL DÍA: Halloween
    // =========================================================================

    @FXML private StackPane raiz;

    // Capa 1: Espejo e interactividad
    @FXML private StackPane capaEspejo;
    @FXML private StackPane contenedorEspejo;
    @FXML private ImageView imgEspejo;
    @FXML private StackPane contenedorRostro;
    @FXML private ImageView imgRostroEspejo;
    @FXML private Pane panelOjos;
    @FXML private Circle pupilaIzquierda;
    @FXML private Circle pupilaDerecha;
    @FXML private StackPane capaNiebla;
    @FXML private Label lblTitulo;
    @FXML private Label lblInstruccion;
    @FXML private HBox cajaInteraccion;
    @FXML private TextField txtEntrada;
    @FXML private Button btnInteractuar;
    @FXML private Label lblMensajeEstado;

    // Capa 2: Jumpscare repentino
    @FXML private StackPane capaJumpscare;
    @FXML private ImageView imgJumpscare;

    // Capa 3: Revelación de la letra "D"
    @FXML private StackPane capaLetra;
    @FXML private Label lblTituloLetra;
    @FXML private Label lblLetraD;
    @FXML private Label lblSubLetra;
    @FXML private Label lblReinicioAviso;

    private Font fuenteTerror;
    private Timeline temporizadorPiedad;
    private PauseTransition temporizadorScream;
    private PauseTransition temporizadorReinicio;
    private ScaleTransition animacionLatidoLetra;

    // Reproducción de sonido
    private MediaPlayer reproductorAmbiente;
    private AudioClip audioClipSusto;

    private boolean resuelto = false;

    // Radio máximo de movimiento de la pupila dentro de la cuenca del ojo
    private static final double RADIO_MAX_PUPILA = 7.5;
    // Sensibilidad del seguimiento de la mirada
    private static final double FACTOR_DISTANCIA = 0.025;
    // Duración del scream solicitada (5 segundos)
    private static final int SEGUNDOS_SCREAM = 5;
    // Tiempo de piedad según rúbrica (60 segundos)
    private static final int SEGUNDOS_PIEDAD = 60;
    // Tiempo de exhibición de la letra antes del autorreinicio (12 segundos)
    private static final int SEGUNDOS_AUTORREINICIO = 12;

    @FXML
    public void initialize() {
        cargarFuentePersonalizada();
        cargarImagenes();
        ajustarTamanosPantalla();
        configurarSeguimientoOjos();
        configurarInteraccionBase();
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
                    lblTitulo.setFont(Font.font(fuenteTerror.getName(), 52));
                    lblLetraD.setFont(Font.font(fuenteTerror.getName(), 220));
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso al cargar fuente: " + e.getMessage());
        }
    }

    /**
     * Carga de imágenes: espejo, rostro de reflejo con ojos interactivos, y scream para susto.
     */
    private void cargarImagenes() {
        try {
            InputStream espejoStream = getClass().getResourceAsStream("images/espejo.png");
            if (espejoStream != null) {
                imgEspejo.setImage(new Image(espejoStream));
            }

            InputStream rostroStream = getClass().getResourceAsStream("images/rostro_espejo.png");
            if (rostroStream != null) {
                imgRostroEspejo.setImage(new Image(rostroStream));
            }

            InputStream jumpscareStream = getClass().getResourceAsStream("images/reflejo_espectro.png");
            if (jumpscareStream != null) {
                imgJumpscare.setImage(new Image(jumpscareStream));
            }
        } catch (Exception e) {
            System.err.println("Aviso al cargar imágenes: " + e.getMessage());
        }
    }

    /**
     * Hace que el espejo ocupe casi toda la pantalla y el jumpscare la pantalla completa.
     */
    private void ajustarTamanosPantalla() {
        // El espejo ocupa prácticamente toda la ventana
        imgEspejo.fitWidthProperty().bind(raiz.widthProperty().multiply(0.95));
        imgEspejo.fitHeightProperty().bind(raiz.heightProperty().multiply(0.92));

        // La imagen del jumpscare se estira a pantalla completa total
        imgJumpscare.fitWidthProperty().bind(raiz.widthProperty());
        imgJumpscare.fitHeightProperty().bind(raiz.heightProperty());
    }

    /**
     * Lógica interactiva de seguimiento ocular:
     * Calcula el ángulo entre cada ojo y la posición del cursor del ratón en la ventana,
     * orientando las pupilas en tiempo real hacia donde mira el visitante.
     */
    private void configurarSeguimientoOjos() {
        raiz.setOnMouseMoved(event -> {
            if (resuelto) {
                return;
            }
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();

            actualizarPupila(pupilaIzquierda, mouseX, mouseY);
            actualizarPupila(pupilaDerecha, mouseX, mouseY);
        });
    }

    private void actualizarPupila(Circle pupila, double mouseX, double mouseY) {
        try {
            Point2D centroOjoEscena = pupila.localToScene(pupila.getCenterX(), pupila.getCenterY());
            double dx = mouseX - centroOjoEscena.getX();
            double dy = mouseY - centroOjoEscena.getY();
            double angulo = Math.atan2(dy, dx);
            double distancia = Math.min(RADIO_MAX_PUPILA, Math.hypot(dx, dy) * FACTOR_DISTANCIA);

            pupila.setTranslateX(Math.cos(angulo) * distancia);
            pupila.setTranslateY(Math.sin(angulo) * distancia);
        } catch (Exception ignored) {
            // Protección ante layout inicial
        }
    }

    /**
     * Configuración del audio de ambiente continuo en bucle y del sonido de susto.
     */
    private void inicializarAudio() {
        try {
            URL ambienteUrl = getClass().getResource("sounds/ambiente.mp3");
            if (ambienteUrl == null) {
                ambienteUrl = getClass().getResource("sounds/ambiente.wav");
            }
            if (ambienteUrl != null) {
                Media mediaAmbiente = new Media(ambienteUrl.toExternalForm());
                reproductorAmbiente = new MediaPlayer(mediaAmbiente);
                reproductorAmbiente.setCycleCount(MediaPlayer.INDEFINITE);
                reproductorAmbiente.setVolume(0.60);
                reproductorAmbiente.play();
            }
        } catch (Exception e) {
            System.err.println("Aviso audio ambiente: " + e.getMessage());
        }

        try {
            URL sustoUrl = getClass().getResource("sounds/susto.mp3");
            if (sustoUrl == null) {
                sustoUrl = getClass().getResource("sounds/susto.wav");
            }
            if (sustoUrl != null) {
                audioClipSusto = new AudioClip(sustoUrl.toExternalForm());
                audioClipSusto.setVolume(1.0);
            }
        } catch (Exception e) {
            System.err.println("Aviso audio susto: " + e.getMessage());
        }
    }

    /**
     * Configuración base de eventos.
     */
    private void configurarInteraccionBase() {
        // Tocar el cristal del espejo directamente desencadena el susto de repente
        contenedorEspejo.setOnMouseClicked(event -> onInteractuar());

        // Pulsar Enter en el campo de texto desencadena el susto
        txtEntrada.setOnAction(event -> onInteractuar());
    }

    /**
     * Temporizador de piedad (60s) según rúbrica.
     */
    private void iniciarTemporizadorPiedad() {
        detenerTemporizadores();
        temporizadorPiedad = new Timeline(new KeyFrame(Duration.seconds(SEGUNDOS_PIEDAD), event -> {
            if (!resuelto) {
                ejecutarSustoRepentino();
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
        ejecutarSustoRepentino();
    }

    /**
     * Susto repentino e instantáneo:
     * - CAMBIO DE PANTALLA DE GOLPE SIN TRANSICIÓN a pantalla completa.
     * - Reproducción inmediata del sonido del scream.
     * - Se mantiene durante ~5 segundos antes de mostrar la letra 'D'.
     */
    public void ejecutarSustoRepentino() {
        resuelto = true;
        detenerTemporizadores();

        // 1. Cambio súbito de pantalla (sin transición gradual)
        capaEspejo.setVisible(false);
        capaLetra.setVisible(false);
        capaJumpscare.setVisible(true);

        // 2. Reproducción inmediata del scream
        if (audioClipSusto != null) {
            audioClipSusto.play();
        }

        // 3. Duración de 5 segundos de scream antes de entregar la letra "D"
        temporizadorScream = new PauseTransition(Duration.seconds(SEGUNDOS_SCREAM));
        temporizadorScream.setOnFinished(event -> mostrarPantallaLetraD());
        temporizadorScream.play();
    }

    /**
     * Muestra la pantalla de la letra 'D' con animación de latido espectral.
     */
    private void mostrarPantallaLetraD() {
        capaJumpscare.setVisible(false);
        capaLetra.setVisible(true);

        // Animación de latido espectral requerida por la rúbrica (ScaleTransition en bucle)
        animacionLatidoLetra = new ScaleTransition(Duration.seconds(1.1), lblLetraD);
        animacionLatidoLetra.setFromX(1.0);
        animacionLatidoLetra.setFromY(1.0);
        animacionLatidoLetra.setToX(1.12);
        animacionLatidoLetra.setToY(1.12);
        animacionLatidoLetra.setAutoReverse(true);
        animacionLatidoLetra.setCycleCount(Timeline.INDEFINITE);
        animacionLatidoLetra.play();

        // Programar autorreinicio para el siguiente grupo
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

        // Restaurar visibilidad de las capas
        capaJumpscare.setVisible(false);
        capaLetra.setVisible(false);
        capaEspejo.setVisible(true);

        // Resetear pupilas
        pupilaIzquierda.setTranslateX(0);
        pupilaIzquierda.setTranslateY(0);
        pupilaDerecha.setTranslateX(0);
        pupilaDerecha.setTranslateY(0);

        txtEntrada.clear();
        lblMensajeEstado.setText("");

        // Reiniciar temporizador de piedad
        iniciarTemporizadorPiedad();
    }

    private void detenerTemporizadores() {
        if (temporizadorPiedad != null) {
            temporizadorPiedad.stop();
        }
        if (temporizadorScream != null) {
            temporizadorScream.stop();
        }
        if (temporizadorReinicio != null) {
            temporizadorReinicio.stop();
        }
    }
}
