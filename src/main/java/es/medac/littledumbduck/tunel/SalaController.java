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
 * - Mecánica de concentración con punto móvil.
 * - Margen de tolerancia de 1.8 segundos antes de dar fallo.
 * - Rango de tolerancia de contacto ampliado (~85px) para jugabilidad óptima.
 * - Inicio justo: el punto aparece centrado y no penaliza hasta que el usuario lo toca.
 * - A los 30 segundos de seguimiento -> Scream fulminante instantáneo durante 5s.
 * - Entrega de la letra secreta "D" con animación espectral.
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
    private boolean primerContactoRealizado = false;

    // Posición del ratón en la escena
    private double mouseX = -1000;
    private double mouseY = -1000;

    // Lógica del punto móvil
    private AnimationTimer buclePunto;
    private double puntoX = 0;
    private double puntoY = 0;
    private double objetivoX = 0;
    private double objetivoY = 0;
    private double velocidadActual = 150.0;
    private double segundosSeguidos = 0.0;
    private double tiempoFuera = 0.0; // Contador de tolerancia de fallo
    private long ultimoTiempoNano = 0;
    private final Random azar = new Random();

    // Rango de tolerancia de captura del ratón ampliado a 85px (muy cómodo y justo)
    private static final double RADIO_TOLERANCIA_RATON = 85.0;
    // Tolerancia de fallo: 1.8 segundos fuera del punto antes de perder
    private static final double MAX_TIEMPO_FUERA = 1.8;
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
     * Bucle continuo a 60 FPS con AnimationTimer con tolerancia de 1.8s.
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

                // Medir distancia entre ratón y el punto
                Point2D puntoEscena = nodoPunto.localToScene(nodoPunto.getWidth() / 2.0, nodoPunto.getHeight() / 2.0);
                double distMouse = Math.hypot(mouseX - puntoEscena.getX(), mouseY - puntoEscena.getY());

                boolean ratonEncima = distMouse <= RADIO_TOLERANCIA_RATON;

                // Esperar a que el usuario toque el punto por primera vez tras pulsar el botón
                if (!primerContactoRealizado) {
                    if (ratonEncima) {
                        primerContactoRealizado = true;
                        lblInstruccion.setText("¡Bien! No apartes el ratón del punto...");
                        lblMensajeEstado.setText("");
                    } else {
                        // Aún no ha puesto el ratón encima por primera vez: no penalizar
                        lblMensajeEstado.setText("Coloca el ratón sobre el punto rojo para comenzar...");
                        return;
                    }
                }

                if (ratonEncima) {
                    // Está dentro del rango de tolerancia -> resetear tiempo fuera y acumular éxito
                    tiempoFuera = 0.0;
                    segundosSeguidos += dt;
                    lblMensajeEstado.setText("");
                } else {
                    // Se ha salido del punto: aplicar margen de tolerancia de fallo (1.8 segundos)
                    tiempoFuera += dt;
                    lblMensajeEstado.setText("¡CUIDADO! Vuelve al punto...");

                    if (tiempoFuera >= MAX_TIEMPO_FUERA) {
                        // Se acabó el margen de tolerancia -> Detener
                        pausarPorSalidaDelPunto();
                        return;
                    }
                }

                // Si llega a los 30 segundos continuos, ¡DISPARAR EL SUSTO!
                if (segundosSeguidos >= SEGUNDOS_OBJETIVO) {
                    buclePunto.stop();
                    ejecutarSustoRepentino();
                    return;
                }

                // Mover el punto hacia su objetivo aleatorio
                double dx = objetivoX - puntoX;
                double dy = objetivoY - puntoY;
                double distObjetivo = Math.hypot(dx, dy);

                if (distObjetivo < 25.0) {
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
     * Elige una nueva coordenada y velocidad aleatoria dentro del área del espejo.
     */
    private void elegirNuevoObjetivoYVelocidad() {
        double ancho = Math.max(800, raiz.getWidth());
        double alto = Math.max(600, raiz.getHeight());

        double minX = ancho * 0.20;
        double maxX = ancho * 0.80;
        double minY = alto * 0.18;
        double maxY = alto * 0.78;

        objetivoX = minX + azar.nextDouble() * (maxX - minX);
        objetivoY = minY + azar.nextDouble() * (maxY - minY);

        // Velocidad accesible y controlada (entre 100 y 230 px/s)
        velocidadActual = 110.0 + azar.nextDouble() * 120.0;
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
        lblInstruccion.setText("Coloca el cursor sobre el punto rojo para comenzar a seguirlo...");
        lblMensajeEstado.setText("");

        // Posicionar el punto en el centro de la pantalla
        double ancho = Math.max(800, raiz.getWidth());
        double alto = Math.max(600, raiz.getHeight());
        puntoX = ancho / 2.0 - 20.0;
        puntoY = alto / 2.0 - 20.0;

        nodoPunto.setLayoutX(puntoX);
        nodoPunto.setLayoutY(puntoY);
        nodoPunto.setVisible(true);

        primerContactoRealizado = false;
        segundosSeguidos = 0.0;
        tiempoFuera = 0.0;
        ultimoTiempoNano = 0;
        elegirNuevoObjetivoYVelocidad();

        juegoEnCurso = true;
        buclePunto.start();
    }

    /**
     * Se llama cuando el usuario pierde el punto durante más de 1.8 segundos.
     */
    private void pausarPorSalidaDelPunto() {
        juegoEnCurso = false;
        buclePunto.stop();
        nodoPunto.setVisible(false);

        lblMensajeEstado.setText("Vuelve a intentarlo...");
        lblInstruccion.setText("Has apartado el ratón del punto. Pulsa para reiniciar.");
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
                // Precalentar el buffer de sonido para eliminar latencia
                audioClipSusto.play(0.0);
            }
        } catch (Exception e) {
            System.err.println("Aviso audio susto: " + e.getMessage());
        }
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
        primerContactoRealizado = false;

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
        tiempoFuera = 0.0;
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
