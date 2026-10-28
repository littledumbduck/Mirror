# 💀 Túnel del Terror: «El aula maldita» — Sala 4: «El Espejo»

**Módulo:** Desarrollo de Interfaces (2.º DAM / DAW) — MEDAC  
**Propietario / GitHub:** `littledumbduck`  
**Letra que entrega la sala:** **`D`**  
**Fecha de entrega / simulacro:** Jueves 29 de octubre de 2026 — 08:00  

---

## 🎃 1. Descripción de la Sala y Coordinación con 1.º

Esta aplicación JavaFX forma parte del escape room interactivo del Túnel del Terror de MEDAC, compuesto por 6 salas coordinadas entre alumnos de 1.º (Lenguaje de Marcas) y 2.º (Desarrollo de Interfaces).

* **Pantalla de 1.º (HTML):** Un cuadro con un retrato antiguo cuyos ojos siguen al visitante a través de la pantalla mientras recorre la sala.
* **Pantalla de 2.º (JavaFX - Sala 4 «El Espejo»):** El espejo ocupa casi la totalidad de la pantalla. Un botón permite iniciar la prueba de concentración. Al pulsarlo, el punto rojo aparece en el centro y no penaliza hasta que el usuario lo toca por primera vez. Una vez iniciado el seguimiento, cuenta con un margen de tolerancia generoso de 85px y una ventana de gracia de fallo de 1.8 segundos antes de perder el contacto.
* **Desencadenante y susto repentino:** Tras **20 segundos** de concentración continua siguiendo el punto, se produce un **cambio de pantalla instantáneo y repentino a pantalla completa**, sin transición gradual, con disparo sonoro fulminante en el milisegundo 0 (`susto.mp3`), mostrando el rostro aterrador desencajado.
* **Duración y entrega de la letra:** El scream a pantalla completa se sostiene durante **5 segundos** generando el clímax de terror tras la intensa concentración. Pasados esos 5 segundos, la pantalla conmuta revelando la letra secreta **`D`** con una animación de latido espectral continuo (`ScaleTransition`). Con esta letra y las del resto de salas (1-5), los grupos podrán abrir la puerta final en la Sala 6.

---

## 🛠️ 2. Estructura y Requisitos Técnicos

El proyecto cumple al 100% las especificaciones de la rúbrica de evaluación:

* **Maven + JavaFX 21:** Configurado en `pom.xml` con las dependencias oficiales `javafx-controls`, `javafx-fxml` y `javafx-media`.
* **Paquete:** `es.medac.littledumbduck.tunel`.
* **Arquitectura limpia MVC:**
  * Vista FXML: `src/main/resources/es/medac/littledumbduck/tunel/sala.fxml`
  * Controlador: `src/main/java/es/medac/littledumbduck/tunel/SalaController.java`
  * Hoja de estilos CSS: `src/main/resources/es/medac/littledumbduck/tunel/estilos.css`
* **Constante PROPIETARIO:** Declarada como `public static final String PROPIETARIO = "littledumbduck";` y visible en el título de la ventana.
* **Pantalla completa forzada:**
  * `setFullScreen(true)`
  * `setFullScreenExitHint("")`
  * `setFullScreenExitKeyCombination(KeyCombination.NO_MATCH)`
* **Tipografía cargada en código:** Se carga la fuente personalizada mediante `Font.loadFont(...)` desde `fonts/terror.ttf`.
* **Animaciones con `Transition` y `Timeline`:**
  * Niebla espectral oscilante en bucle continuo (`FadeTransition`).
  * Susto controlado mediante `ParallelTransition` combinando `FadeTransition` y `ScaleTransition` (zoom controlado y suave sin flashes).
  * Revelación y latido continuo de la letra `D` (`ScaleTransition` en bucle con `autoReverse`).
* **Sonido:**
  * Música ambiente tétrica en bucle con `MediaPlayer`.
  * Efecto sonoro de susto/impacto con `AudioClip`.
* **Mecánica de Piedad (60 segundos):**
  * Si en 60 segundos el grupo no interactúa, la sala *"se apiada"* mediante un temporizador `Timeline`, activando el reflejo y entregando la letra para garantizar que nadie quede atrapado.
* **Autorreinicio y control:**
  * Tras exhibir la letra durante 12 segundos, la sala se restablece sola a su estado inicial.
  * Tecla de control rápido **`F12`** para que el alumno guardián reinicie la sala inmediatamente ante la entrada del nuevo grupo.
* **Seguridad y respeto (Fotosensibilidad):**
  * Sin efectos estroboscópicos ni destellos rápidos por respeto a personas fotosensibles.
  * Salida visible y respeto al protocolo de seguridad con la palabra clave **«CALABAZA»**.

---

## 🚀 3. Instrucciones de Ejecución

Para compilar y ejecutar la aplicación a pantalla completa:

```bash
mvn clean javafx:run
```

*(En este equipo, Maven ha quedado instalado y configurado en el `PATH` del usuario).*

### Controles de la mesa durante el túnel:
* **Botón "COMENZAR CONCENTRACIÓN":** Inicia la aparición y movimiento del punto para el grupo actual.
* **Seguimiento con el ratón:** El visitante debe mantener el cursor sobre el punto mientras este se mueve aleatoriamente.
* **Tecla `F12`:** Reinicia la sala al estado original en cualquier momento.

---

## 🧠 4. Reflexión de Usabilidad y Seguridad (RA4)

### Principio de Usabilidad Roto a Propósito:
Se han roto conscientemente el **Principio de Visibilidad del Estado del Sistema** y la **Correspondencia con el Mundo Real** (Heurísticas de Nielsen 1 y 2):
1. **Falta deliberada de feedback preliminar y expectativa alterada:** En una interfaz de usuario convencional, el sistema informa continuamente de lo que está sucediendo de forma tranquilizadora y predecible. En la Sala del Espejo, la interfaz finge una pasividad inquietante (*"Mírate..."*), escondiendo la verdadera naturaleza del espejo hasta que el usuario se atreve a interactuar.
2. **Respuesta agresiva no solicitada:** El sistema reacciona con una aparición que invade el espacio personal del visitante rompiendo la cuarta pared.

### ¿Por qué da miedo?
El terror psicológico surge de la violación del principio de control del usuario. El visitante cree estar frente a un terminal inofensivo con un reflejo borroso; sin embargo, al pulsar el control o tocar el cristal, la pantalla "despierta" y se abalanza visual y sonoramente hacia él antes de revelarle la recompensa.

### Comprensión en 5 segundos:
A pesar de la ambientación opresiva, el visitante comprende la mecánica de inmediato:
* El espejo está en el centro de atención visual.
* Cuenta con un campo de texto y un botón de acción con llamada clara (*"TOCAR EL CRISTAL"*).
* Además, hacer clic directo sobre el propio cristal activa la interacción, haciendo la mecánica accesible y directa para cualquier perfil de usuario.

### ¿Cómo se corregiría en una aplicación real de producción?
En una aplicación de software real:
* Se proporcionarían indicadores visuales claros de estado (spinners o barras de progreso).
* Las transiciones serían suaves, neutras y sin saltos de escala repentinos.
* La respuesta a una acción del usuario sería estrictamente la esperada (por ejemplo, guardar datos o navegar) sin elementos invasivos no solicitados.

---

## 🎨 5. Créditos de Recursos

* **Imágenes:**
  * Marco de espejo gótico barroco a pantalla casi completa (`espejo.png`).
  * Rostro espectral reflejado con cuencas oculares vacías (`rostro_espejo.png`).
  * Globos oculares demoníacos interactivos independientes (`ojo_terror.png`).
  * Rostro jumpscare para el scream súbito a pantalla completa (`reflejo_espectro.png`).
* **Tipografía:**
  * `terror.ttf` cargada con `Font.loadFont`.
* **Sonido:**
  * Pista ambiental integrada (`ambiente.mp3`) reproducida en bucle continuo mediante `javafx.scene.media.MediaPlayer`.
  * Efecto de impacto sonoro integrado (`susto.mp3`) reproducido mediante `javafx.scene.media.AudioClip`.
