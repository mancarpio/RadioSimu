package simulador;

import simulador.util.CameraKeyMovementFunctions;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
//import com.jme3.math.Line;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Line;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.Styles;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;


public class Simulador extends SimpleApplication {

    private static final String INICIAR_FRESNEL = "Iniciar Fresnel";
    private static final String GEN_ANTENA = "Generar antena";
    private static final String SELECCIONAR_PRI = "Click primario";
    private static final String SELECCIONAR_SEC = "Click secundario";
    private static final String ROTAR_MAPA_X_NEG = "Rotar Mapa en -X";
    private static final String ROTAR_MAPA_X_POS = "Rotar camara en +X";
    private static final String ROTAR_CAM_X_NEG = "Rotar camara en -X";

    private boolean hayTransmisor = false;
    private Node terrainNode;
    private Spatial transmisor, receptor;

    private BulletAppState bulletAppState;
    private Geometry linea;
    private Line line;

    private ArrayList<Object> arrayConfiguraciones;

    private CameraKeyMovementFunctions camFun;

    private TerrenoState terrenoState;
    private AntenaState antenaState;
    private FresnelState fresnelSimulationState;
    private CameraKeyState cameraState;
    private CameraFocusState focusState;
    private GuiState guiState;
    private ConfiguracionInicialState configState;

   

    public Simulador() {
        super(new StatsAppState(), new ConfiguracionInicialState());
    }

    @Override
    public void simpleInitApp() {
        
        
        
        this.setShowSettings(false);
        this.setDisplayStatView(false);
        this.setDisplayFps(false);
        
        //Se cargan los estilos necesarios para los componentes de Lemur
        
        GuiGlobals.initialize(this);
        GuiGlobals globals = GuiGlobals.getInstance();
        Styles styles = GuiGlobals.getInstance().getStyles();

        BaseStyles.loadGlassStyle();
        globals.getStyles().setDefaultStyle("glass");

        styles.getSelector(Panel.ELEMENT_ID, "glass").set("background",
                new QuadBackgroundComponent(new ColorRGBA(0, 0.25f, 0.25f, 0.5f)));

        styles.getSelector("glass").set("shadowColor",
                new ColorRGBA(0, 0f, 0f, 1));
        styles.getSelector(Checkbox.ELEMENT_ID, "glass").set("background",
                new QuadBackgroundComponent(new ColorRGBA(0, 0.5f, 0.5f, 0.5f)));
        styles.getSelector("spacer", "glass").set("background",
                new QuadBackgroundComponent(new ColorRGBA(1, 0.0f, 0.0f, 0.0f)));
        styles.getSelector("header", "glass").set("background",
                new QuadBackgroundComponent(new ColorRGBA(0, 0.75f, 0.75f, 0.5f)));
        styles.getSelector("header", "glass").set("shadowColor",
                new ColorRGBA(1, 0f, 0f, 1));


        inputManager.setCursorVisible(true);



        
        
 
        
        //Ajusta la distancia de renderizado

        getCamera().setFrustumFar(20000);

    }

    @Override
    public void simpleUpdate(float tpf) {

        // linea.updateModelBound();
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }



    public void restablecer() {

        terrenoState = stateManager.getState(TerrenoState.class);
        antenaState = stateManager.getState(AntenaState.class);
        cameraState = stateManager.getState(CameraKeyState.class);
        focusState = stateManager.getState(CameraFocusState.class);
        fresnelSimulationState = stateManager.getState(FresnelState.class);
        guiState = stateManager.getState(GuiState.class);
        configState = stateManager.getState(ConfiguracionInicialState.class);

        stateManager.detach(guiState);
        stateManager.detach(focusState);
        stateManager.detach(fresnelSimulationState);
        stateManager.detach(antenaState);
        stateManager.detach(terrenoState);
        stateManager.detach(cameraState);

        rootNode.detachAllChildren();
        guiNode.detachAllChildren();

        configState.onDisable();
        configState.onEnable();
    }

//    public BulletAppState getbulletAppState() {
//        return bulletAppState;
//    }
//
//    public TerrenoState getTerrenoState(){
//    
//    return terrenoState;
//    
//    }
}
