/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.simsilica.lemur.ActionButton;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.CallMethodAction;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.OptionPanel;
import com.simsilica.lemur.OptionPanelState;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.BoxLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.Styles;

/**
 *
 * @author Manuel
 */
public class GuiState extends BaseAppState {

    private SimpleApplication simpleApp;

    private AntenaState antenaState;
    private FresnelState fresnelState;
    private TerrenoState terrenoState;
    private CameraKeyState cameraKeyState;
    private TerrainQuad terreno;
    private static boolean guiIsLoaded;

    private Container contenedorCam;
    private Container contenedorInfoAntenas;
    private Container contenedorInfoExtendidaAntenas;

    private Container contenedorFocos;
    private Container contenedorResultFresnel;
    private Container contenedorResultBox;
    private Container contenedorWireBox;
    private Container contenedorActivarFresnel;
    private Container contenedorPadre;
    private Container contenedorSecundario;
    private float ancho;
    private float alto;
    private Panel panel;
    private Checkbox simuBox;
    private Checkbox estadisticaBox;
    private Checkbox antenasBox;
    private Button botonMenu;
    private OptionPanelState optionState;
    private OptionPanel panelMenu;
    private ActionButton botonReiniciar;
    private OptionPanel panelReiniciar;
    private OptionPanel panelSalir;
    private ActionButton botonSalir;
    private ActionButton botonRegresar;
    private Container contenedorLineasDebug;
    private Container contenedorElipsoide;

    @Override
    protected void initialize(Application app) {

        setSimpleApp();
        Simulador main = (Simulador) simpleApp;

        crearElementos();
        System.out.println("GuiState se ha iniciado!");

        optionState = new OptionPanelState(simpleApp.getGuiNode());

        simpleApp.getStateManager().attach(optionState);
        panelMenu = new OptionPanel("",
                new CallMethodAction("Regresar", optionState, "close"));

        panelReiniciar = new OptionPanel("¿Reiniciar el simulador?", new CallMethodAction("Sí", main, "restablecer"),
                new CallMethodAction("No", optionState, "close"));

        botonReiniciar = new ActionButton(
                new CallMethodAction("Reiniciar Simulador", this, "showPopUpReiniciar"));

        panelSalir = new OptionPanel("¿Salir del simulador?", new CallMethodAction("Sí", this, "salir"),
                new CallMethodAction("No", optionState, "close"));

        botonSalir = new ActionButton(
                new CallMethodAction("Salir del Simulador", this, "showPopUpSalir"));
        botonRegresar = new ActionButton(
                new CallMethodAction("Regresar", optionState, "close"));

        panelMenu.getContainer().getLayout().addChild(botonReiniciar, 1, 0);
        panelMenu.getContainer().getLayout().addChild(botonSalir, 2, 0);
        //   panelMenu.getContainer().getLayout().addChild(botonRegresar, 3,0);

        ancho = simpleApp.getCamera().getWidth();

        alto = simpleApp.getCamera().getHeight();

        contenedorPadre = new Container();
        contenedorSecundario = new Container();
        contenedorPadre.setPreferredSize(new Vector3f(180, 500, 2));
        contenedorSecundario.setPreferredSize(new Vector3f(150, 300, 2));
        contenedorPadre.setLayout(new BoxLayout(Axis.Y, FillMode.Even));
        contenedorSecundario.setLayout(new BoxLayout(Axis.Y, FillMode.Even));
        contenedorPadre.setSize(new Vector3f(180, 500, 2));
        this.contenedorActivarFresnel = simpleApp.getStateManager().
                getState(FresnelState.class).getSimuCont();

        this.contenedorResultFresnel = simpleApp.getStateManager().
                getState(FresnelState.class).getContenedoResultados();

        this.contenedorResultBox = simpleApp.getStateManager().
                getState(FresnelState.class).getResultBox();

        this.contenedorCam = simpleApp.getStateManager().
                getState(CameraKeyState.class).getContenedorCam();

        this.contenedorFocos = simpleApp.getStateManager().
                getState(CameraFocusState.class).getContenedorFocos();

        this.contenedorInfoAntenas = simpleApp.getStateManager().
                getState(AntenaState.class).getCheckboxConatiner();
        this.contenedorInfoExtendidaAntenas = simpleApp.getStateManager().
                getState(FresnelState.class).getAntenaExtBox();
        
        this.contenedorLineasDebug = simpleApp.getStateManager().
                getState(FresnelState.class).getLineasBox();
        this.contenedorElipsoide = simpleApp.getStateManager().
                getState(FresnelState.class).getElipsoideBox();
        
        
        this.contenedorWireBox = simpleApp.getStateManager().
                getState(AntenaState.class).getWireboxContainer();
        contenedorPadre.addChild(contenedorActivarFresnel);
        contenedorPadre.addChild(contenedorInfoAntenas);
        contenedorPadre.addChild(contenedorInfoExtendidaAntenas);
        contenedorPadre.addChild(contenedorResultBox);
        contenedorPadre.addChild(contenedorFocos);
        contenedorPadre.addChild(contenedorCam);
        
        contenedorSecundario.addChild(contenedorWireBox);
        contenedorSecundario.addChild(contenedorLineasDebug);
        contenedorSecundario.addChild(contenedorElipsoide);
        
        
        System.out.println("contenedorActivarFresnel " + contenedorActivarFresnel.getSize());
        System.out.println("contenedorCam " + contenedorCam.getSize());
        System.out.println("contenedorFocos " + contenedorFocos.getSize());
        System.out.println("contenedorInfoAntenas " + contenedorInfoAntenas.getSize());

        contenedorPadre.setLocalTranslation(ancho - 180 - 35, alto - 35, 2);
        contenedorSecundario.setLocalTranslation(50, alto - 300, 2);

        botonMenu = new ActionButton(new CallMethodAction("Menu", this, "showMenu"));
        botonMenu.setFontSize(18);
        botonMenu.setTextHAlignment(HAlignment.Center);
        botonMenu.setInsets(new Insets3f(4, 4, 4, 4));
        botonMenu.setLocalTranslation(50, alto - 50, 2);
        botonMenu.setPreferredSize(new Vector3f(80, 30, 2));

    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        simpleApp.getGuiNode().attachChild(contenedorPadre);
        simpleApp.getGuiNode().attachChild(contenedorSecundario);
        simpleApp.getGuiNode().attachChild(botonMenu);
        //  simpleApp.getGuiNode().attachChild(contenedorResultFresnel);

        System.out.println("GUIState is enable");
    }

    @Override
    protected void onDisable() {
    }

    public void setSimpleApp() {
        if (simpleApp == null) {
            this.simpleApp = (SimpleApplication) (this.getApplication());
        }

    }

    private void crearElementos() {

        /*Se ajusta el flag en Main para que se entienda que la gui esta cargada*/
        guiIsLoaded = true;

    }

    public void imprimir() {

        System.out.println("imprimir");
        System.out.println(ancho + " * " + alto);
    }

    public boolean isGuiLoaded() {

        return guiIsLoaded;

    }

    public void showMenu() {
        System.out.println("showMenu");
        optionState.show(panelMenu);

    }

    public void showPopUpReiniciar() {
        optionState.show(panelReiniciar);

    }

    public void showPopUpSalir() {
        optionState.show(panelSalir);

    }

    private void salir() {

        System.exit(0);

    }
}
