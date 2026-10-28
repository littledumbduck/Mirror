package es.medac.littledumbduck.tunel;

import java.io.InputStream;
import java.net.URL;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Controlador para la Sala 4: El Espejo (Túnel del Terror).
 * - Pantalla de concentración: "Mira atentamente al punto y síguelo con el ratón".
 * - Botón para iniciar la prueba.
 * - Punto móvil aleatorio (con variaciones continuas de velocidad y distancia por toda la pantalla).
 * - Comprobación continua: si el ratón se sale del punto, se detiene y muestra "Vuelve a intentarlo".
 * - Tras 30 segundos continuos siguiendo el punto, se dispara el scream súbito con audio instantáneo.
 * - Tras 5 segundos de scream, se revela la letra secreta "D" con animación de latido espectral.
 * - Autorreinicio tras 12 segundos y tecla F12 para reinicio manual inmediato.
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
    @FXML private StackPane capaNiebla;
    @FXML private Label lblTitulo;
    @FXML private Label lblInstruccion;
    @FXML private Button btnIniciar;
    @FXML private Label lblMensajeEstado;

    // Elementos del punto móvil
    @FXML private Pane panelPunto;
    @FXML private StackPane nodoPunto;

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
    private boolean juegoEnCurso = false;

    // Posición del ratón en la escena
    private double mouseX = -1000;
    private double mouseY = -1000;

    // Lógica del punto móvil
    private AnimationTimer buclePunto;
    private double puntoX = 0;
    private double puntoY = 0;
    private double objetivoX = 0;
    private double objetivoY = 0;
    private double velocidadActual = 180.0; // px por segundo
    private double segundosSeguidos = 0.0;
    private long ultimoTiempoNano = 0;
    private final Random azar = new Random();

    // Tolerancia del ratón sobre el punto (radio en px)
    private static final double RADIO_TOLERANCIA_RATON = 48.0;
    // Tiempo requerido siguiendo el punto antes del susto (30 segundos)
    private static final double SEGUNDOS_OBJETIVO = 30.0;
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
        configurarDeteccionRaton();
        configurarBucleMovimientoPunto();
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
     * Carga de imágenes: marco del espejo y rostro para el jumpscare.
     */
    private void cargarImagenes() {
        try {
            InputStream espejoStream = getClass().getResourceAsStream("images/espejo.png");
            if (espejoStream != null) {
                imgEspejo.setImage(new Image(espejoStream));
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
     * Ajuste de proporciones para ocupar casi toda la pantalla.
     */
    private void ajustarTamanosPantalla() {
        imgEspejo.fitWidthProperty().bind(raiz.widthProperty().multiply(0.95));
        imgEspejo.fitHeightProperty().bind(raiz.heightProperty().multiply(0.92));

        imgJumpscare.fitWidthProperty().bind(raiz.widthProperty());
        imgJumpscare.fitHeightProperty().bind(raiz.heightProperty());
    }

    /**
     * Rastreo continuo de la posición del ratón.
     */
    private void configurarDeteccionRaton() {
        raiz.setOnMouseMoved(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });
        raiz.setOnMouseDragged(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });
    }

    /**
     * Bucle continuo a 60 FPS con AnimationTimer para mover el punto aleatoriamente
     * y verificar que el usuario mantiene el ratón encima.
     */
    private void configurarBucleMovimientoPunto() {
        buclePunto = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!juegoEnCurso || resuelto) {
                    return;
                }

                if (ultimoTiempoNano == 0) {
                    ultimoTiempoNano = now;
                    return;
                }

                double dt = (now - ultimoTiempoNano) / 1_000_000_000.0;
                ultimoTiempoNano = now;

                // 1. Comprobar si el ratón está encima del punto
                Point2D puntoEscena = nodoPunto.localToScene(nodoPunto.getWidth() / 2.0, nodoPunto.getHeight() / 2.0);
                double distMouse = Math.hypot(mouseX - puntoEscena.getX(), mouseY - puntoEscena.getY());

                if (distMouse > RADIO_TOLERANCIA_RATON) {
                    // El usuario se ha salido del punto -> Detener y mostrar "Vuelve a intentarlo"
                    pausarPorSalidaDelPunto();
                    return;
                }

                // 2. Si el ratón sigue encima, sumar tiempo de concentración
                segundosSeguidos += dt;

                // 3. Si llega a los 30 segundos continuos, ¡DISPARAR EL SUSTO!
                if (segundosSeguidos >= SEGUNDOS_OBJETIVO) {
                    buclePunto.stop();
                    ejecutarSustoRepentino();
                    return;
                }

                // 4. Mover el punto hacia su objetivo aleatorio
                double dx = objetivoX - puntoX;
                double dy = objetivoY - puntoY;
                double distObjetivo = Math.hypot(dx, dy);

                if (distObjetivo < 20.0) {
                    // Llegó al objetivo -> Elegir nuevo rumbo, distancia y velocidad aleatoria
                    elegirNuevoObjetivoYVelocidad();
                } else {
                    double paso = velocidadActual * dt;
                    puntoX += (dx / distObjetivo) * paso;
                    puntoY += (dy / distObjetivo) * paso;

                    nodoPunto.setLayoutX(puntoX);
                    nodoPunto.setLayoutY(puntoY);
                }
            }
        };
    }

    /**
     * Elige una nueva coordenada y velocidad aleatoria por toda la pantalla.
     */
    private void elegirNuevoObjetivoYVelocidad() {
        double ancho = Math.max(800, raiz.getWidth());
        double alto = Math.max(600, raiz.getHeight());

        // Margen para mantenerse dentro de la zona visible del espejo
        double minX = ancho * 0.18;
        double maxX = ancho * 0.82;
        double minY = alto * 0.16;
        double maxY = alto * 0.80;

        // Intervalos de distancia: a veces cerca, a veces lejos
        objetivoX = minX + azar.nextDouble() * (maxX - minX);
        objetivoY = minY + azar.nextDouble() * (maxY - minY);

        // Intervalos de velocidad: más rápido o más lento (entre 120 px/s y 320 px/s)
        velocidadActual = 130.0 + azar.nextDouble() * 190.0;
    }

    /**
     * Inicia la concentración al pulsar el botón.
     */
    @FXML
    public void onIniciarPunto() {
        if (resuelto) {
            return;
        }

        btnIniciar.setVisible(false);
        lblMensajeEstado.setText("");
        lblInstruccion.setText("Mira atentamente al punto y síguelo con el ratón...");

        // Posicionar el punto en el centro del espejo
        double ancho = Math.max(800, raiz.getWidth());
        double alto = Math.max(600, raiz.getHeight());
        puntoX = ancho / 2.0 - 18.0;
        puntoY = alto / 2.0 - 18.0;

        nodoPunto.setLayoutX(puntoX);
        nodoPunto.setLayoutY(puntoY);
        nodoPunto.setVisible(true);

        segundosSeguidos = 0.0;
        ultimoTiempoNano = 0;
        elegirNuevoObjetivoYVelocidad();

        juegoEnCurso = true;
        buclePunto.start();
    }

    /**
     * Se llama cuando el usuario pierde el punto.
     */
    private void pausarPorSalidaDelPunto() {
        juegoEnCurso = false;
        buclePunto.stop();
        nodoPunto.setVisible(false);

        lblMensajeEstado.setText("Vuelve a intentarlo...");
        lblInstruccion.setText("Has apartado la mirada o el ratón. Concéntrate de nuevo.");
        btnIniciar.setText("VOLVER A INTENTAR");
        btnIniciar.setVisible(true);
    }

    /**
     * Configuración del audio ambiental en bucle y efecto de scream.
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
                // Precalentar el buffer de sonido para eliminar cualquier latencia
                audioClipSusto.play(0.0);
            }
        } catch (Exception e) {
            System.err.println("Aviso audio susto: " + e.getMessage());
        }
    }

    /**
     * Temporizador de piedad (60s) según rúbrica:
     * Si transcurren 60 segundos sin resolver, la sala entrega la letra.
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

    /**
     * Susto repentino e instantáneo:
     * - Disparo de audio en el milisegundo 0.
     * - Cambio súbito a pantalla completa de scream (sin transición).
     * - Se sostiene durante 5 segundos antes de mostrar la letra secreta 'D'.
     */
    public void ejecutarSustoRepentino() {
        resuelto = true;
        juegoEnCurso = false;
        if (buclePunto != null) {
            buclePunto.stop();
        }
        detenerTemporizadores();

        // 1. DISPARAR AUDIO INMEDIATAMENTE EN EL MILISEGUNDO 0
        if (audioClipSusto != null) {
            audioClipSusto.play();
        }

        // 2. Cambio súbito de pantalla (sin transición gradual)
        capaEspejo.setVisible(false);
        capaLetra.setVisible(false);
        capaJumpscare.setVisible(true);

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
        juegoEnCurso = false;

        if (buclePunto != null) {
            buclePunto.stop();
        }
        if (animacionLatidoLetra != null) {
            animacionLatidoLetra.stop();
        }

        // Restaurar visibilidad de las capas
        capaJumpscare.setVisible(false);
        capaLetra.setVisible(false);
        capaEspejo.setVisible(true);

        // Restaurar estado inicial del botón y punto
        nodoPunto.setVisible(false);
        lblMensajeEstado.setText("");
        lblInstruccion.setText("Mira atentamente al punto y síguelo con el ratón...");
        btnIniciar.setText("COMENZAR CONCENTRACIÓN");
        btnIniciar.setVisible(true);

        segundosSeguidos = 0.0;
        ultimoTiempoNano = 0;

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
