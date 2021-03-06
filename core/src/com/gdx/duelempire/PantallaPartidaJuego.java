package com.gdx.duelempire;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gdx.duelempire.actores.ActorEstructura;
import com.gdx.duelempire.actores.ActorMinion;
import com.gdx.duelempire.actores.ActorRecurso;
import com.gdx.duelempire.componentes.Carta;
import com.gdx.duelempire.componentes.Casilla;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by juanjo on 03/05/2017.
 */

public class PantallaPartidaJuego extends PantallaBaseJuego {
    //Variables que guardan la dimension de pantalla del dispositivo
    private float ancho, alto;
    /*IMPORTANTE SABER QUE:
     Las dimensiones de pantalla por defecto son de 480*640 unidades, por lo tanto las posiciones
     de todos los componentes usados comprenderán entre esos rangos escalados a la resolución
     correspondiente.*/
    private float relacionx, relaciony;//Escalado para la pantalla
    //Variables encargadas de centrar la vista en la pantalla
    private OrthographicCamera camera;
    private FitViewport viewp;
    //Casillas de juego que recogen la ubicacion de los personajes y las estructuras
    private final int NUMCASILLAS = 20;
    private Casilla[] casillas;/*Array que contendrá las casillas de
    juego (que contendrán las estructuras o personajes)*/
    private ActorMinion actualizaMinion;//para usar los métodos de actualizacion de minions

    //Label que muestra el tiempo de partida
    private Label lcronometro;
    private Label lmovimientoP2;//Label que indica la última carta jugada por el P2;
    //Contadores de recursos
    private int contOro, contMana, incrementoOro, incrementoMana;
    //Mazo
    private final int NCARTASMAZO = 30;
    private int cartasActuales;//cartas restantes
    private ArrayList<Carta> cartaEnMazo;
    //Mano
    private final int CARTASMANO = 5;
    private Carta[] cartaEnMano;/*CARTASENMANO cartas en mano, representa con una instancia Carta()
     si esta vacía una posición*/
    //Cronómetro
    private float sumDeltaTiempo;
    private int contador;
    //Escenario
    private Stage stage;
    //Actores fijos de inicio
    private ActorEstructura base1, base2;
    private ActorRecurso recursoOro, recursoMana;
    //Texturas de esos actores
    private Texture tbase1, tbase2;
    private Texture toro, tmana;
    //Textura y objeto que recogen los datos del personaje que va a ser invocado
    Texture tminion;
    ActorMinion minion;
    /*Skin: contiene los datos y formatos necesarios para escribir y establecer gráficos para
     los labels y los botones de la interfaz*/
    private Skin skin;
    //Mazo
    private TextButton btnmazo;
    private Label lmazo;
    //Mano del jugador
    private TextButton btnmano1;
    private Label lmano1;
    private TextButton btnmano2;
    private Label lmano2;
    private TextButton btnmano3;
    private Label lmano3;
    private TextButton btnmano4;
    private Label lmano4;
    private TextButton btnmano5;
    private Label lmano5;
    //Label que anunciará al ganador de la partida
    private Label ganador;
    boolean noMostrado;
    float tiempoMostrado;//Tiempo que dura mostrado el label de ganador
    //Lista que guarda todos los posibles efectos de carta y su acción al ser activados
    private String[][] listaDeEfectos;
    //Variables de sonido y música
    private Music musica;
    private Sound drawCard, playHechizo, playMinion;
    //Imagen de fondo y textura de fondo
    private Texture tfondo;
    private Image fondo;

    private int existe;
    private Random rnd;//Variable para generar una carta u otra en el generador de mazos
    private float recogernd;//Guarda el valor del Random

    /*********************************************IA**********************************************/
    private ArrayList<Carta> cartaEnMazoIA = new ArrayList<Carta>();
    private int contOroIA, contManaIA, incrementoOroIA, incrementoManaIA;
    private boolean CPUespera;

    private DuelEmpire_CardGame juego;

    /**
     * Constructor para la pantalla de juego.
     * Al comenzar la partida, lo primero que se hace es cargar todas las texturas iniciales,
     * fijar la resolución de la pantalla e instanciar todos los contadores y demás variables
     */
    public PantallaPartidaJuego(DuelEmpire_CardGame juego) {
        super(juego);
        this.juego = juego;
        musica = juego.getManager().get("music/HeroicBattleMusic.mp3");
        drawCard = juego.getManager().get("music/DrawCard.mp3");
        playHechizo = juego.getManager().get("music/summonSpell.mp3");
        playMinion = juego.getManager().get("music/summonMinion.mp3");
        camera = new OrthographicCamera();
        ancho = Gdx.graphics.getWidth();
        alto = Gdx.graphics.getHeight();
        relacionx = 640 / ancho;
        relaciony = 480 / alto;
        camera.position.set(ancho / 2f, alto / 2f, 0);
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        if (ancho < alto) {
            ancho = Gdx.graphics.getHeight();
            alto = Gdx.graphics.getWidth();
        }
        sumDeltaTiempo = 0;
        contador = 0;
        /*VARIABLES PLAYER*/
        contMana = 200;
        contOro = 200;
        incrementoMana = 2;
        incrementoOro = 2;
        /*VARIABLES CPU*/
        contManaIA = 200;
        contOroIA = 200;
        incrementoManaIA = 2;
        incrementoOroIA = 2;
        CPUespera = false;

        /*****************AQUÍ SE PODRÍA USAR UNA BD********************/
        /*Se instancia la lista de los efectos existentes .Cada linea admite como máximo unos 15 caracteres
        con el tamaño de fuente usado en los label de carta que los escriben en pantalla*/
        listaDeEfectos = new String[][]{{"0", ""}, {"1", "Da +2 AT a los\nminions"}, {"2", "Hace 20 danos a\nla base enemiga"},
                {"3", "Da +2 de tasa\nde oro"}, {"4", "Cura 10 vidas a\ntu base y da +2\nde tasa de mana"}};

        existe = -1;//variable usada para comprobar si hay un mazo precargado (-1 indica que no)
        rnd = new Random();// genera valores para las cartas

        cartasActuales = NCARTASMAZO;
        cartaEnMazo = new ArrayList<Carta>();
        //Se rellena el mazo con NCARTASMAZO cartas aleatorias
        generarMazo(NCARTASMAZO);
        cartaEnMano = new Carta[5];
        //Se genera el mazo de la IA
        generarMazoIA();

        tfondo = new Texture("backgrounds/fondo.png");
        fondo = new Image(tfondo);
        fondo.setSize(ancho, alto);
        tbase1 = new Texture("structures/basePortal.png");
        tbase2 = new Texture("structures/basePortal_2.png");
        toro = new Texture("resources/gold.png");
        tmana = new Texture("resources/mana.png");
        base1 = new ActorEstructura(tbase1, "Player1", skin, relacionx, relaciony);
        base2 = new ActorEstructura(tbase2, "Player2", skin, relacionx, relaciony);
        casillas = new Casilla[NUMCASILLAS];
        for (int i = 0; i < casillas.length; i++) {
            casillas[i] = new Casilla();
        }
        //Se guardan las casillas que son fijas
        casillas[0].setEstructura(base1);
        casillas[casillas.length - 1].setEstructura(base2);
        lcronometro = new Label("0", skin);
        lcronometro.setPosition(300f / relacionx, 430f / relaciony);
        lmovimientoP2 = new Label("ULTIMA JUGADA: ", skin);
        lmovimientoP2.setPosition(380f / relacionx, 400f / relaciony);
        switch (Gdx.app.getType()) {
            case Android:
                lcronometro.setFontScale(5);
                lmovimientoP2.setFontScale(2);
                break;
            case Desktop:
                lcronometro.setFontScale(2);
                lmovimientoP2.setFontScale(1);
                break;
        }
        tiempoMostrado = 0;
        ganador = new Label("", skin);
        ganador.setPosition(ancho / 8f, alto / 1.3f);
        noMostrado = true;
        switch (Gdx.app.getType()) {
            case Android:
                ganador.setFontScale(6f);
                break;
            case Desktop:
                ganador.setFontScale(3);
                break;
        }
        //se instancian los botones de la pantalla
        instanciarBotones();
    }

    @Override
    public void show() {
        camera.setToOrtho(false, ancho, alto);
        camera.update();
        camera.position.set(ancho / 2f, alto / 2f, 0);
        camera.update();
        FitViewport viewp = new FitViewport(ancho, alto, camera);
        stage = new Stage(viewp);

        recursoMana = new ActorRecurso(tmana, "Mana", skin, relacionx, relaciony);
        recursoOro = new ActorRecurso(toro, "Oro", skin, relacionx, relaciony);
        recursoMana.setTextoLabel(contMana + " +" + incrementoMana);
        recursoOro.setTextoLabel(contOro + " +" + incrementoOro);
        //Se incluyen en el escenario todos los componentes que son fijos desde que se entra a la pantalla
        stage.addActor(fondo);
        stage.addActor(base1);
        stage.addActor(base2);
        stage.addActor(base1.getLrecurso());
        stage.addActor(base2.getLrecurso());
        stage.addActor(lcronometro);
        stage.addActor(lmovimientoP2);
        stage.addActor(recursoMana);
        stage.addActor(recursoOro);
        stage.addActor(recursoMana.getLrecurso());
        stage.addActor(recursoOro.getLrecurso());
        stage.addActor(btnmazo);
        stage.addActor(lmazo);
        stage.addActor(btnmano1);
        stage.addActor(lmano1);
        stage.addActor(btnmano2);
        stage.addActor(lmano2);
        stage.addActor(btnmano3);
        stage.addActor(lmano3);
        stage.addActor(btnmano4);
        stage.addActor(lmano4);
        stage.addActor(btnmano5);
        stage.addActor(lmano5);
        //Se activa la entrada de datos en el escenario
        Gdx.input.setInputProcessor(stage);
        musica.setVolume(0.6f);
        musica.play();
    }

    @Override
    public void hide() {
        musica.setVolume(0);
        musica.stop();
        stage.dispose();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        musica.setVolume(0);
        musica.stop();
        stage.dispose();
        Gdx.input.setInputProcessor(null);
    }

    /**
     * El método render es el encargado de actualizar el estado del juego cada x tiempo
     * delta representa el lapso de tiempo entre cada actualización en el juego en milisegundos
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.2f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Es necesario ejecutar glClear para limpiar el buffer de bits
        stage.act();
        stage.draw();
        updateTiempo_Recursos_Minions(delta);
        if (!musica.isPlaying() && ganador.getText().equals("")) {
            musica.play();
        }
        //Despues de cada render hay que comprobar si ha ganado alguien
        if (casillas[0].getEstructura().getSalud() <= 0 && noMostrado) {
            ganador.setText("HAS SIDO DERROTADO");
            ganador.setColor(Color.RED);
            stage.addActor(ganador);
            noMostrado = false;
        } else if (casillas[casillas.length - 1].getEstructura().getSalud() <= 0 && noMostrado) {
            ganador.setText("GANASTE LA PARTIDA!");
            ganador.setColor(Color.GOLD);
            stage.addActor(ganador);
            noMostrado = false;
        } else if (!noMostrado) {
            stage.addAction(Actions.sequence(Actions.delay(3f), Actions.run(new Runnable() {
                @Override
                public void run() {
                    juego.setScreen(juego.menuPrincipal);
                }
            })));
        }
    }

    @Override
    public void resize(int width, int height) {
        ancho = width;
        alto = height;
        camera.setToOrtho(false, ancho, alto);
        camera.update();
        camera.position.set(ancho / 2f, alto / 2f, 0);
        stage.getViewport().update(width, height, true);
    }

    /**
     * Método que se utiliza en el método render para modificar los contadores en tiempo de juego.
     *
     * @param delta lapso de tiempo en ms.
     */
    public void updateTiempo_Recursos_Minions(float delta) {
        sumDeltaTiempo += delta;
        for (int i = 0; i < casillas.length; i++) {
            if (casillas[i].getMinion() != null) {
                actualizaMinion = casillas[i].getMinion();
                actualizaMinion.actualizarAccionMinion(casillas, i);
            }
        }
        //}
        if (sumDeltaTiempo >= 1) {
            contador++;
            lcronometro.setText("" + contador);
            sumDeltaTiempo = sumDeltaTiempo - 1;
            contOro += incrementoOro;
            contMana += incrementoMana;
            contOroIA += incrementoOroIA;
            contManaIA += incrementoManaIA;
            recursoMana.setTextoLabel(contMana + " +" + incrementoMana);
            recursoOro.setTextoLabel(contOro + " +" + incrementoOro);
            CPUespera = false;
        }
        if (contador % 3 == 0 && !CPUespera) {//CPUespera evita que se itere varias veces en un segundo
            IA();
            CPUespera = true;
        }
    }

    /**
     * Método encargado de instanciar todos los botones de la interfaz, que son la parte con la
     * que interactua el jugador. En este método se indica también el comportamiento de cada botón
     * ante cada interacción con él. También es importante para no sobrecargar el código en el constructor.
     */
    public void instanciarBotones() {
        btnmazo = new TextButton("-25 oro\n\nQuedan " + NCARTASMAZO, skin);
        btnmazo.setPosition(530 / relacionx, 30 / relaciony);
        btnmazo.setSize(100 / relacionx, 100 / relaciony);
        btnmazo.setColor(Color.GOLD);
        lmazo = new Label("Deck", skin);
        lmazo.setPosition(530 / relacionx, 130 / relaciony);
        switch (Gdx.app.getType()) {
            case Android:
                lmazo.setFontScale(3);
                break;
            case Desktop:
                lmazo.setFontScale(1);
                break;
        }
        btnmazo.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (cartasActuales >= 1 && contOro >= 25) {
                    drawCard.play();
                    //Ahora hay que mirar si hay hueco en la mano del jugador para poder robar
                    if (cartaEnMano[0].getCoste()[0] == -1) {
                        cartaEnMano[0] = cartaEnMazo.get(0);
                        if (cartaEnMano[0].getTipo().equals("minion")) {
                            btnmano1.setText("PERSONAJE:\n" + cartaEnMano[0].getId() + "\nAT +" + cartaEnMano[0].getAtaque() +
                                    ", DF +" + cartaEnMano[0].getDefensa() + "\nMV +" + cartaEnMano[0].getMovilidad() +
                                    ", RA +" + cartaEnMano[0].getRatioAtaque() +
                                    "\nEfecto:\n" + listaDeEfectos[Integer.parseInt(cartaEnMano[0].getEfecto())][1] +
                                    "\nCoste:\n" + cartaEnMano[0].getCoste()[0] + " Oro, " + cartaEnMano[0].getCoste()[1] + " Mana");
                        } else {
                            btnmano1.setText("HECHIZO:\n" + cartaEnMano[0].getId() + "\nEfecto:\n" + listaDeEfectos[Integer.parseInt(cartaEnMano[0].getEfecto())][1]
                                    + "\nCoste:\n" + cartaEnMano[0].getCoste()[0] + " Oro, " + cartaEnMano[0].getCoste()[1] + " Mana");
                        }
                    } else if (cartaEnMano[1].getCoste()[0] == -1) {
                        cartaEnMano[1] = cartaEnMazo.get(0);
                        if (cartaEnMano[1].getTipo().equals("minion")) {
                            btnmano2.setText("PERSONAJE:\n" + cartaEnMano[1].getId() + "\nAT +" + cartaEnMano[1].getAtaque() +
                                    ", DF +" + cartaEnMano[1].getDefensa() + "\nMV +" + cartaEnMano[1].getMovilidad() +
                                    ", RA +" + cartaEnMano[1].getRatioAtaque() +
                                    "\nEfecto:\n" + listaDeEfectos[Integer.parseInt(cartaEnMano[1].getEfecto())][1]
                                    + "\nCoste:\n" + cartaEnMano[1].getCoste()[0] + " Oro, " + cartaEnMano[1].getCoste()[1] + " Mana");
                        } else {
                            btnmano2.setText("HECHIZO:\n" + cartaEnMano[1].getId() + "\nEfecto:\n" + listaDeEfectos[Integer.parseInt(cartaEnMano[1].getEfecto())][1]
                                    + "\nCoste:\n" + cartaEnMano[1].getCoste()[0] + " Oro, " + cartaEnMano[1].getCoste()[1] + " Mana");
                        }
                    } else if (cartaEnMano[2].getCoste()[0] == -1) {
                        cartaEnMano[2] = cartaEnMazo.get(0);
                        if (cartaEnMano[2].getTipo().equals("minion")) {
                            btnmano3.setText("PERSONAJE:\n" + cartaEnMano[2].getId() + "\nAT +" + cartaEnMano[2].getAtaque() +
                                    ", DF +" + cartaEnMano[2].getDefensa() + "\nMV +" + cartaEnMano[2].getMovilidad() +
                                    ", RA +" + cartaEnMano[2].getRatioAtaque() +
                                    "\nEfecto:\n" + listaDeEfectos[Integer.parseInt(cartaEnMano[2].getEfecto())][1]
                                    + "\nCoste:\n" + cartaEnMano[2].getCoste()[0] + " Oro, " + cartaEnMano[2].getCoste()[1] + " Mana");
                        } else {
                            btnmano3.setText("HECHIZO:\n" + cartaEnMano[2].getId() + "\nEfecto:\n" + listaDeEfectos[Integer.parseInt(cartaEnMano[2].getEfecto())][1]
                                    + "\nCoste:\n" + cartaEnMano[2].getCoste()[0] + " Oro, " + cartaEnMano[2].getCoste()[1] + " Mana");
                        }
                    } else if (cartaEnMano[3].getCoste()[0] == -1) {
                        cartaEnMano[3] = cartaEnMazo.get(0);
                        if (cartaEnMano[3].getTipo().equals("minion")) {
                            btnmano4.setText("PERSONAJE:\n" + cartaEnMano[3].getId() + "\nAT +" + cartaEnMano[3].getAtaque() +
                                    ", DF +" + cartaEnMano[3].getDefensa() + "\nMV +" + cartaEnMano[3].getMovilidad() +
                                    ", RA +" + cartaEnMano[3].getRatioAtaque() +
                                    "\nEfecto:\n" + listaDeEfectos[Integer.parseInt(cartaEnMano[3].getEfecto())][1]
                                    + "\nCoste:\n" + cartaEnMano[3].getCoste()[0] + " Oro, " + cartaEnMano[3].getCoste()[1] + " Mana");
                        } else {
                            btnmano4.setText("HECHIZO:\n" + cartaEnMano[3].getId() + "\nEfecto:\n" + listaDeEfectos[Integer.parseInt(cartaEnMano[3].getEfecto())][1]
                                    + "\nCoste:\n" + cartaEnMano[3].getCoste()[0] + " Oro, " + cartaEnMano[3].getCoste()[1] + " Mana");
                        }
                    } else if (cartaEnMano[4].getCoste()[0] == -1) {
                        cartaEnMano[4] = cartaEnMazo.get(0);
                        if (cartaEnMano[4].getTipo().equals("minion")) {
                            btnmano5.setText("PERSONAJE:\n" + cartaEnMano[4].getId() + "\nAT +" + cartaEnMano[4].getAtaque() +
                                    ", DF +" + cartaEnMano[4].getDefensa() + "\nMV +" + cartaEnMano[4].getMovilidad() +
                                    ", RA +" + cartaEnMano[4].getRatioAtaque() +
                                    "\nEfecto:\n" + listaDeEfectos[Integer.parseInt(cartaEnMano[4].getEfecto())][1]
                                    + "\nCoste:\n" + cartaEnMano[4].getCoste()[0] + " Oro, " + cartaEnMano[4].getCoste()[1] + " Mana");
                        } else {
                            btnmano5.setText("HECHIZO:\n" + cartaEnMano[4].getId() + "\nEfecto:\n" + listaDeEfectos[Integer.parseInt(cartaEnMano[4].getEfecto())][1]
                                    + "\nCoste:\n" + cartaEnMano[4].getCoste()[0] + " Oro, " + cartaEnMano[4].getCoste()[1] + " Mana");
                        }
                    }
                    /*En cualquier caso, después de darle a robar se quita la carta 0 del mazo, que se considera
                    como la siguiente carta a robar del mazo. Si tenias la mano llena 'quemas'/ pierdes esa carta*/
                    cartaEnMazo.remove(0);
                    cartasActuales--;
                    btnmazo.setText("-25 oro\n\nQuedan " + cartasActuales);
                    //cartaEnMano[0].getCoste()[0] != -1 && cartaEnMano[1].getCoste()[0] != -1 && cartaEnMano[2].getCoste()[0] != -1 &&
                    //cartaEnMano[3].getCoste()[0] != -1 && cartaEnMano[4].getCoste()[0] != -1
                    if (cartaEnMazo.size() == 0) {
                        btnmazo.setColor(Color.GRAY);
                    } else {
                        btnmazo.setColor(Color.GOLD);
                    }
                    /*Mecánica de robo*/
                    contOro -= 25;
                    recursoOro.setTextoLabel(contOro + " +" + incrementoOro);
                }
            }
        });
        cartaEnMano[0] = new Carta();
        btnmano1 = new TextButton("Nada", skin);
        btnmano1.setPosition(10 / relacionx, 30 / relaciony);
        btnmano1.setSize(100 / relacionx, 150 / relaciony);
        lmano1 = new Label("Hueco 1", skin);
        lmano1.setPosition(10 / relacionx, 180 / relaciony);
        btnmano1.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (cartaEnMano[0].getCoste()[0] > -1) {
                    //Se pasan los datos del minion o hechizo, y se mira si se puede jugar
                    if (jugarCarta(cartaEnMano[0], 1)) {
                        if (cartaEnMano[0].getAtaque() < 0) {
                            playHechizo.play();
                        } else {
                            playMinion.play();
                        }
                        cartaEnMano[0] = new Carta();//Se limpia la posición en mano tras jugar la carta
                        btnmano1.setText("Nada");
                    }
                }
            }
        });
        cartaEnMano[1] = new Carta();
        btnmano2 = new TextButton("Nada", skin);
        btnmano2.setPosition(110 / relacionx, 30 / relaciony);
        btnmano2.setSize(100 / relacionx, 150 / relaciony);
        lmano2 = new Label("Hueco 2", skin);
        lmano2.setPosition(110 / relacionx, 180 / relaciony);
        btnmano2.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (cartaEnMano[1].getCoste()[0] > -1) {
                    if (cartaEnMano[1].getCoste()[0] > -1) {
                        //Se pasa los datos del minion o hechizo, y se mira si se puede jugar
                        if (jugarCarta(cartaEnMano[1], 1)) {
                            if (cartaEnMano[1].getAtaque() < 0) {
                                playHechizo.play();
                            } else {
                                playMinion.play();
                            }
                            cartaEnMano[1] = new Carta();//Se limpia la posición en mano tras jugar la carta
                            btnmano2.setText("Nada");
                        }
                    }
                }
            }
        });
        cartaEnMano[2] = new Carta();
        btnmano3 = new TextButton("Nada", skin);
        btnmano3.setPosition(210 / relacionx, 30 / relaciony);
        btnmano3.setSize(100 / relacionx, 150 / relaciony);
        lmano3 = new Label("Hueco 3", skin);
        lmano3.setPosition(210 / relacionx, 180 / relaciony);
        btnmano3.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (cartaEnMano[2].getCoste()[0] > -1) {
                    if (cartaEnMano[2].getCoste()[0] > -1) {
                        //Se pasan los datos del minion o hechizo, y se mira si se puede jugar
                        if (jugarCarta(cartaEnMano[2], 1)) {
                            if (cartaEnMano[2].getAtaque() < 0) {
                                playHechizo.play();
                            } else {
                                playMinion.play();
                            }
                            cartaEnMano[2] = new Carta();//Se limpia la posición en mano tras jugar la carta
                            btnmano3.setText("Nada");
                        }
                    }
                }
            }
        });
        cartaEnMano[3] = new Carta();
        btnmano4 = new TextButton("Nada", skin);
        btnmano4.setPosition(310 / relacionx, 30 / relaciony);
        btnmano4.setSize(100 / relacionx, 150 / relaciony);
        lmano4 = new Label("Hueco 4", skin);
        lmano4.setPosition(310 / relacionx, 180 / relaciony);
        btnmano4.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (cartaEnMano[3].getCoste()[0] > -1) {
                    if (cartaEnMano[3].getCoste()[0] > -1) {
                        //Se pasan los datos del minion o hechizo, y se mira si se puede jugar
                        if (jugarCarta(cartaEnMano[3], 1)) {
                            if (cartaEnMano[3].getAtaque() < 0) {
                                playHechizo.play();
                            } else {
                                playMinion.play();
                            }
                            cartaEnMano[3] = new Carta();//Se limpia la posición en mano tras jugar la carta
                            btnmano4.setText("Nada");
                        }
                    }
                }
            }
        });
        cartaEnMano[4] = new Carta();
        btnmano5 = new TextButton("Nada", skin);
        btnmano5.setPosition(410 / relacionx, 30 / relaciony);
        btnmano5.setSize(100 / relacionx, 150 / relaciony);
        lmano5 = new Label("Hueco 5", skin);
        lmano5.setPosition(410 / relacionx, 180 / relaciony);
        btnmano5.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (cartaEnMano[4].getCoste()[0] > -1) {
                    if (cartaEnMano[4].getCoste()[0] > -1) {
                        //Se pasan los datos del minion o hechizo, y se mira si se puede jugar
                        if (jugarCarta(cartaEnMano[4], 1)) {
                            if (cartaEnMano[4].getAtaque() < 0) {
                                playHechizo.play();
                            } else {
                                playMinion.play();
                            }
                            cartaEnMano[4] = new Carta();//Se limpia la posición en mano tras jugar la carta
                            btnmano5.setText("Nada");
                        }
                    }
                }
            }
        });
        switch (Gdx.app.getType()) {
            case Android:
                lmano1.setFontScale(2);
                lmano2.setFontScale(2);
                lmano3.setFontScale(2);
                lmano4.setFontScale(2);
                lmano5.setFontScale(2);
                btnmano1.getLabel().setFontScale(1);
                btnmano2.getLabel().setFontScale(1);
                btnmano3.getLabel().setFontScale(1);
                btnmano4.getLabel().setFontScale(1);
                btnmano5.getLabel().setFontScale(1);
                break;
            case Desktop:
                lmano1.setFontScale(1);
                lmano2.setFontScale(1);
                lmano3.setFontScale(1);
                lmano4.setFontScale(1);
                lmano5.setFontScale(1);
                btnmano1.getLabel().setFontScale(0.7f);
                btnmano2.getLabel().setFontScale(0.7f);
                btnmano3.getLabel().setFontScale(0.7f);
                btnmano4.getLabel().setFontScale(0.7f);
                btnmano5.getLabel().setFontScale(0.7f);
                break;
        }
    }

    /**
     * Método usado para jugar una carta de la mano
     *
     * @param carta  carta objetivo de ser jugada
     * @param player 1 o 2
     * @return devuelve true en caso de haberla podido jugar, false en caso contrario
     */
    public boolean jugarCarta(Carta carta, int player) {
        if (player == 1) {
            if (carta.getCoste()[0] <= contOro && carta.getCoste()[1] <= contMana) {
                //Se mira el caso de invocar un minion y que además la casilla de invocación esté libre
                boolean casillaInvocacionLibre;
                if (casillas[0].getMinion() == null) {
                    casillaInvocacionLibre = true;
                } else {
                    casillaInvocacionLibre = false;
                }
                if (carta.getTipo().equals("minion") && casillaInvocacionLibre) {
                    contMana -= carta.getCoste()[1];
                    contOro -= carta.getCoste()[0];
                    tminion = new Texture(carta.getRutaTextura());
                    minion = new ActorMinion(1, 1, carta.getId(), tminion, relacionx, relaciony, carta.getTamXminion(), carta.getTamYminion(), carta.getDefensa(), carta.getAtaque(), carta.getRatioAtaque(), carta.getMovilidad(), juego, skin);
                    minion.setPosition((640f / relacionx) / casillas.length, 220f / relaciony);//Se invoca en la base
                    minion.getLabelSalud().setPosition((640f / relacionx) / casillas.length + carta.getTamXminion() / 3f / relacionx, 220f / relaciony + carta.getTamYminion() / relaciony);
                    casillas[0].setMinion(minion);
                    stage.addActor(minion);
                    stage.addActor(minion.getLabelSalud());
                    activarEfectoCarta(carta, 1);
                    return true;
                } else if (carta.getTipo().equals("hechizo")) {
                    contMana -= carta.getCoste()[1];
                    contOro -= carta.getCoste()[0];
                    //EFECTO
                    activarEfectoCarta(carta, 1);
                    return true;
                }
            } else {
                //NO SE HA PODIDO JUGAR POR COSTE
                return false;
            }
        } else {//player 2
            if (carta.getCoste()[0] <= contOroIA && carta.getCoste()[1] <= contManaIA) {
                //Se mira el caso de invocar un minion y que además la casilla de invocación esté libre
                boolean casillaInvocacionLibre;
                if (casillas[casillas.length - 1].getMinion() == null) {
                    casillaInvocacionLibre = true;
                } else {
                    casillaInvocacionLibre = false;
                }
                if (carta.getTipo().equals("minion") && casillaInvocacionLibre) {
                    contManaIA -= carta.getCoste()[1];
                    contOroIA -= carta.getCoste()[0];
                    tminion = new Texture(carta.getRutaTextura());
                    minion = new ActorMinion(2, casillas.length, carta.getId(), tminion, relacionx, relaciony, carta.getTamXminion(), carta.getTamYminion(), carta.getDefensa(), carta.getAtaque(), carta.getRatioAtaque(), carta.getMovilidad(), juego, skin);
                    minion.setPosition(580f / relacionx, 220f / relaciony);//se invoca en la base
                    minion.getLabelSalud().setPosition(580f / relacionx + carta.getTamXminion() / 3f / relacionx, 220f / relaciony + carta.getTamYminion() / relaciony);
                    casillas[casillas.length - 1].setMinion(minion);
                    stage.addActor(minion);
                    stage.addActor(minion.getLabelSalud());
                    //Despues de invocar el esbirro, si este tiene efecto se activa
                    activarEfectoCarta(carta, 2);
                    return true;
                } else if (carta.getTipo().equals("hechizo")) {
                    contManaIA -= carta.getCoste()[1];
                    contOroIA -= carta.getCoste()[0];
                    //EFECTO
                    activarEfectoCarta(carta, 2);
                    return true;
                }
            } else {
                //NO SE HA PODIDO JUGAR POR COSTE
                return false;
            }
        }
        return false;
    }

    /**
     * Método utilizado para generar un mazo con un nº de cartas igual a NCARTASMAZO, SOLO se utilizará
     * cuando no hayas creado un mazo en el editor, o hayas borrado el mazo creado
     *
     * @param NCARTASMAZO
     */
    public void generarMazo(int NCARTASMAZO) {
        //Se comprueba si hay algun mazo ya guardado de  antes
        try {
            existe = juego.mazoGuardado.getInteger("C1");
        } catch (Exception e) {
            existe = -1;
        }
        //Se mira que en la posicion uno haya una de las 6 cartas que existen en el juego
        if (existe > 0 && existe < 6) {
            //se va asignando el mazo a cada carta existente en el glosario (de 6 cartas)
            for (int i = 0; i <= 30; i++) {
                if (juego.mazoGuardado.getInteger("C" + i) == 1) {
                    cartaEnMazo.add(new Carta("Guerrero", listaDeEfectos[0][0], "minions/orco.png", 40f, 60f, new int[]{30, 30}, 3, 4, 1, 0.5f));
                } else if (juego.mazoGuardado.getInteger("C" + i) == 2) {
                    cartaEnMazo.add(new Carta("Titan de Fuego", listaDeEfectos[0][0], "minions/FlameDemon2_2.png", 80f, 90f, new int[]{40, 90}, 6, 25, 2, 4));
                } else if (juego.mazoGuardado.getInteger("C" + i) == 3) {
                    cartaEnMazo.add(new Carta("SuperAt", listaDeEfectos[1][0], new int[]{0, 60}));
                } else if (juego.mazoGuardado.getInteger("C" + i) == 4) {
                    cartaEnMazo.add(new Carta("Bombazo", listaDeEfectos[2][0], new int[]{10, 40}));
                } else if (juego.mazoGuardado.getInteger("C" + i) == 5) {
                    cartaEnMazo.add(new Carta("Mina de oro", listaDeEfectos[3][0], new int[]{50, 10}));
                } else if (juego.mazoGuardado.getInteger("C" + i) == 6) {
                    cartaEnMazo.add(new Carta("Energizante", listaDeEfectos[4][0], new int[]{20, 60}));
                }
            }
        } else {
            for (int i = 0; i < NCARTASMAZO; i++) {
                recogernd = rnd.nextFloat();//se genera un numero para evaluar el intervalo
                if (recogernd < 0.25) {
                    cartaEnMazo.add(new Carta("Mina de oro", listaDeEfectos[3][0], new int[]{50, 10}));
                } else if (recogernd < 0.4) {
                    cartaEnMazo.add(new Carta("Energizante", listaDeEfectos[4][0], new int[]{20, 60}));
                } else if (recogernd < 0.6) {
                    cartaEnMazo.add(new Carta("Guerrero", listaDeEfectos[0][0], "minions/orco.png", 40f, 60f, new int[]{30, 30}, 3, 4, 1, 0.5f));
                } else if (recogernd < 0.8) {
                    cartaEnMazo.add(new Carta("SuperAT", listaDeEfectos[1][0], new int[]{0, 60}));
                } else if (recogernd < 0.95) {
                    cartaEnMazo.add(new Carta("Titan de Fuego", listaDeEfectos[0][0], "minions/FlameDemon2_2.png", 80f, 90f, new int[]{40, 90}, 6, 25, 2, 4));
                } else {
                    cartaEnMazo.add(new Carta("Bombazo", listaDeEfectos[2][0], new int[]{10, 40}));
                }
            }
        }
    }

    /**
     * Método encargado de activar los efectos de una carta
     *
     * @param carta  carta que contiene el efecto
     * @param player 1 o 2
     */
    public void activarEfectoCarta(Carta carta, int player) {
        String efecto = carta.getEfecto();
        if (player == 1) {
            if (efecto.equals("0")) {
                //sin efecto
            } else if (efecto.equals("1")) {
                //+2 AT a los minions
                for (int i = 0; i < casillas.length - 1; i++) {
                    if (casillas[i].getMinion() != null) {
                        if (casillas[i].getMinion().getPropietario() == player) {
                            casillas[i].getMinion().setAtaque(casillas[i].getMinion().getAtaque() + 2);
                            casillas[i].getMinion().setTextoLabel(casillas[i].getMinion().getAtaque() + "-" + casillas[i].getMinion().getSalud());
                        }
                    }
                }
            } else if (efecto.equals("2")) {
                //20 daños a la base enemiga
                base2.setSalud(base2.getSalud() - 20);
                base2.setTextoLabel("" + base2.getSalud());
            } else if (efecto.equals("3")) {
                //+2 tasa de oro
                incrementoOro += 2;
            } else if (efecto.equals("4")) {
                //+20 ps +2 tasa de mana
                if (base1.getSalud() >= 80) {
                    base1.setSalud(100);
                    base1.setTextoLabel("" + base1.getSalud());
                } else {
                    base1.setSalud(base1.getSalud() + 10);
                    base1.setTextoLabel("" + base1.getSalud());
                }
                incrementoMana += 2;
            }
        } else {
            if (efecto.equals("0")) {
                //sin efecto
            } else if (efecto.equals("1")) {
                //+2 AT a otros minions
                for (int i = 0; i < casillas.length - 1; i++) {
                    if (casillas[i].getMinion() != null) {
                        if (casillas[i].getMinion() != null) {
                            if (casillas[i].getMinion().getPropietario() == player) {
                                casillas[i].getMinion().setAtaque(casillas[i].getMinion().getAtaque() + 2);
                                casillas[i].getMinion().setTextoLabel(casillas[i].getMinion().getAtaque() + "-" + casillas[i].getMinion().getSalud());
                            }
                        }
                    }
                }
            } else if (efecto.equals("2")) {
                //20 daños a la base enemiga
                base1.setSalud(base1.getSalud() - 20);
                base1.setTextoLabel("" + base1.getSalud());
            } else if (efecto.equals("3")) {
                //+2 tasa de oro
                incrementoOroIA += 2;
            } else if (efecto.equals("4")) {
                //+20 ps +2 tasa de mana
                if (base2.getSalud() >= 80) {
                    base2.setSalud(100);
                    base2.setTextoLabel("" + base2.getSalud());
                } else {
                    base2.setSalud(base2.getSalud() + 10);
                    base2.setTextoLabel("" + base2.getSalud());
                }
                incrementoManaIA += 2;
            }
        }
    }

    /**
     * Igual que el generador de el player1 pero para la IA
     */
    public void generarMazoIA() {
        for (int i = 0; i < NCARTASMAZO; i++) {
            recogernd = rnd.nextFloat();//se genera un numero para evaluar los intervalos
            if (recogernd < 0.25) {
                cartaEnMazoIA.add(new Carta("Guerrero", listaDeEfectos[0][0], "minions/orco_2.png", 40f, 60f, new int[]{30, 30}, 3, 4, 1, 0.5f));
            } else if (recogernd < 0.4) {
                cartaEnMazoIA.add(new Carta("Titan de Fuego", listaDeEfectos[0][0], "minions/FlameDemon2.png", 80f, 90f, new int[]{40, 90}, 6, 25, 2, 4));
            } else if (recogernd < 0.45) {
                cartaEnMazoIA.add(new Carta("SuperAT", listaDeEfectos[1][0], new int[]{0, 60}));
            } else if (recogernd < 0.65) {
                cartaEnMazoIA.add(new Carta("Bombazo", listaDeEfectos[2][0], new int[]{10, 40}));
            } else if (recogernd < 0.85) {
                cartaEnMazoIA.add(new Carta("Mina de oro", listaDeEfectos[3][0], new int[]{50, 10}));
            } else {
                cartaEnMazoIA.add(new Carta("Energizante", listaDeEfectos[4][0], new int[]{20, 60}));
            }
        }
    }

    /**
     * Método que hace la función de inteligencia artificial enemiga
     */
    private void IA() {
        int i = 0;
        if (cartaEnMazoIA.size() > 0 && contOroIA >= 25) {
            if (jugarCarta(cartaEnMazoIA.get(0), 2)) {
                contOroIA -= 25;//se resta el coste por robo
                lmovimientoP2.setText("ULTIMA JUGADA P2: " + cartaEnMazoIA.get(0).getId());
                cartaEnMazoIA.remove(0);
                i++;
            }
        }
    }
}