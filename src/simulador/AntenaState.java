/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import com.simsilica.lemur.style.Styles;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Manuel
 */
public class AntenaState extends BaseAppState implements StateFunctionListener {

    private boolean posicionable;
    private CollisionResults colisiones;
    private FresnelState fresnelState;
    private Spatial modeloTransmisor, modeloReceptor;
    private Antena transmisor, receptor;
    private Node antenaNodo;

    private SimpleApplication simpleApp;
    private Ray rayo;
    private TerrenoState terrenoState;

    private int antenaSeleccionada = 1;
    private Antena antenaRetenida;
    private Spatial modeloRetenido;
    protected static final int TRANSMISOR = 1;
    protected static final int RECEPTOR = 2;
    private static boolean antenaMovement = false;
    private TerrainQuad terreno;

    private static final String GROUP_ANTENNA = "Grupo Antena";
    private static final FunctionId ALTERNAR_DESPLAZAMIENTO = new FunctionId(GROUP_ANTENNA, "alternar dezplazamiento");
    private InputMapper inputMapper;
    private ArrayList<Antena> antenas;
    private ArrayList<HashMap<String, Object>> antenaConfig;
    private Container contenedor;
    private ArrayList<Container> contenedoresArray;
    private Container checkboxContenedor;
    private Checkbox antenaCheckbox;
    private Checkbox wireboxCheckbox;
    private boolean mostrarAntenaInfo;
    private File antenaFile;
    private int coordX;
    private Integer coordY;
    private Integer coordZ;
    private Container wireboxContenedor;

    @Override
    protected void initialize(Application app) {

        setSimpleApp();


        Styles styles = GuiGlobals.getInstance().getStyles();

        crearGui();

        antenaConfig = simpleApp.getStateManager().getState(ConfiguracionInicialState.class).getConfigAntenas();
        antenas = new ArrayList<>();
        antenaNodo = new Node();
        terreno = simpleApp.getStateManager().getState(TerrenoState.class).getTerrain();
        simpleApp.getRootNode().attachChild(antenaNodo);
        antenaNodo.setLocalScale(1f, 1f, 1f);
        coordX = (Integer) simpleApp.getStateManager().
                getState(ConfiguracionInicialState.class).getConfigGeneral().get("coordX");
        coordY = (Integer) simpleApp.getStateManager().
                getState(ConfiguracionInicialState.class).getConfigGeneral().get("coordY");
        coordZ = (Integer) simpleApp.getStateManager().
                getState(ConfiguracionInicialState.class).getConfigGeneral().get("coordZ");

        simpleApp.getStateManager().getState(TerrenoState.class).calibrarAltura();

        /*antenaConfig representa el arreglo de configuraciones para cada antena.
          Este arreglo se define por medio de la interfaz de inicio
         */
        try {
            cargarAntenas(antenaConfig);
        } catch (IOException ex) {
            Logger.getLogger(AntenaState.class.getName()).log(Level.SEVERE, null, ex);
        }
        comprobarRoles();
        cargarContenedores();

        posicionable = false;
//        antena1 = new Antena(this,"Tx","Models/antenav1.5.obj",Antena.TRANSMISOR);
//        antena2 = new Antena(this,"Rx","Models/antenav1.5.obj",Antena.RECEPTOR);

        // Este bucle debe ser replanteado para el caso de haber más de dos antenas:
       
        for(Antena antena : antenas){
        
        if(antena.getRol().equals(Antena.TRANSMISOR)){
            transmisor = antena;}else{receptor = antena;}
        
        
        }


        modeloTransmisor = transmisor.getModelo();
        modeloReceptor = receptor.getModelo();

        antenaRetenida = transmisor;
        modeloRetenido = transmisor.getModelo();

//        crearControl(antena1);
//        crearControl(antena2);
        // se distribuye la ubicacion de cada antena
        float ang = 0;
        for (Antena antena : antenas) {

            simpleApp.getRootNode().attachChild(antena.getModelo());
            antena.getModelo().move(10 * FastMath.cos(ang),
                    0, 10 * FastMath.sin(FastMath.PI - ang));
//        antena.getModelo().move(0,-terreno.getHeight(
//                new Vector2f(antena.getModelo().getLocalTranslation().getX(),
//                 antena.getModelo().getLocalTranslation().getZ() )) + antena.getAltura(),0);
            antena.getModelo().move(0, antena.getAltura(), 0);
            ang = ang + (FastMath.TWO_PI / antenas.size());
        }
//        CursorEventControl.addListenersToSpatial(antena1, dragHandler);
//        CursorEventControl.addListenersToSpatial(antena2, dragHandler);

        //Configurando el FresnelSimulayionStare
        fresnelState = new FresnelState();

        simpleApp.getStateManager().attach(fresnelState);

        if (inputMapper == null) {
            inputMapper = GuiGlobals.getInstance().getInputMapper();
        }

        System.out.println("iniciado antenastate");

    }

    /*
        Para futuras versiones con soporte a más de dos antenas.
        Se pretende manejar dos antenas por vez en la interfaz.
        "item" representa lo que el usuario escoja en una interfaz 
        de selección (p.ejem: import com.simsilica.lemur.ListBox).
        "seleccion" define si item se asociara con transmisor o receptor.
    
     */
    private void setAntenaSeleccionada(int item, int seleccion) {

        if (seleccion == this.TRANSMISOR) {

            transmisor = antenas.get(item);

        } else if (seleccion == this.RECEPTOR) {
            receptor = antenas.get(item);

        } else {

            System.out.println("no es una seleccion valida");

        }

    }

    private void crearGui() {

        checkboxContenedor = new Container();
        checkboxContenedor.setSize(new Vector3f(230, 35, 2));
        antenaCheckbox = new Checkbox("Mostrar información de las antenas");
        antenaCheckbox.setSize(new Vector3f(230, 35, 2));
        checkboxContenedor.addChild(antenaCheckbox);

        wireboxContenedor = new Container();
        wireboxContenedor.setSize(new Vector3f(230, 35, 2));
        wireboxCheckbox = new Checkbox("Mostrar wirebox de las antenas");
        wireboxCheckbox.setSize(new Vector3f(230, 35, 2));
        wireboxContenedor.addChild(wireboxCheckbox);

        // simpleApp.getGuiNode().attachChild(antenaCheckbox);
    }

    public Container getCheckboxConatiner() {

        return checkboxContenedor;

    }

    public Container getWireboxContainer() {

        return wireboxContenedor;

    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        inputMapper.activateGroup(GROUP_ANTENNA);
        System.out.println("AntenaState is enabled");
    }

    @Override
    protected void onDisable() {
        System.out.println("AntenaState is disabled");
    }

    public void setSimpleApp() {

        this.simpleApp = (SimpleApplication) (this.getApplication());

    }

    protected void setFresnelState(FresnelState state) {

        this.fresnelState = state;

    }

    public FresnelState getFresnelState() {

        return fresnelState;

    }

    public void attachFresnelState() {

        if (fresnelState != null) {
            this.simpleApp.getStateManager().attach(fresnelState);
        } else {
            System.out.println("no hay fresnelState!");
        }
    }

    public void detachFresnelState() {

        if (fresnelState != null) {
            this.simpleApp.getStateManager().detach(fresnelState);
        } else {
            System.out.println("no hay fresnelState!");
        }
    }

    public void setTerreno(TerrainQuad t) {

        this.terreno = t;

    }

    public ArrayList<Antena> getAntenaArray() {

        return antenas;
    }

    @Override
    public void update(float tpf) {
        if (antenaMovement) {
            Camera cam = simpleApp.getCamera();
            InputManager inputManager = simpleApp.getInputManager();
            Vector3f origin = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
            Vector3f direction = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.3f);
            direction.subtractLocal(origin).normalizeLocal();

            Ray ray = new Ray(origin, direction);
            CollisionResults antenaResults = new CollisionResults();
            CollisionResults terrenoResults = new CollisionResults();
            terreno.collideWith(ray, terrenoResults);

            if (terrenoResults.size() > 0) {

                modeloRetenido.setLocalTranslation(terrenoResults.
                        getClosestCollision().getContactPoint().add(0, antenaRetenida.getAltura(), 0));

            }
        }
        if (antenaCheckbox.isChecked()) {
            mostrarAntenaInfo = true;
        } else {
            mostrarAntenaInfo = false;
        }

        if (wireboxCheckbox.isChecked()) {
            for (Antena antena : antenas) {

                antena.getWireBox().setCullHint(Spatial.CullHint.Inherit);
            }

        } else {
            for (Antena antena : antenas) {

                antena.getWireBox().setCullHint(Spatial.CullHint.Always);
            }

        }

    }

    @Override
    public void valueChanged(FunctionId func, InputState value, double tpf) {

        if (func == (ALTERNAR_DESPLAZAMIENTO)) {
            if (value == InputState.Negative) {
                antenaMovement = false;
            }
        }

    }

    private void cargarAntenas(ArrayList<HashMap<String, Object>> lista) throws IOException {

        for (HashMap<String, Object> config : lista) {
            String nombre = (String) config.get("nombre");
            String rol = null;
            int rolSeleccionado = (Integer) config.get("rol");
            if (rolSeleccionado == 0) {
                rol = Antena.TRANSMISOR;
            } else {
                rol = Antena.RECEPTOR;
            }
            //String modelo = "Models/antenav4.obj";
            float altura = (float) config.get("altura");
            Antena antena = null;

            if (config.get("modelo") != null) {
                antenaFile = (File) config.get("modelo");

                FileLocator locator = new FileLocator();
                locator.setRootPath(antenaFile.getParent());
                simpleApp.getAssetManager().registerLocator(antenaFile.getParent(), FileLocator.class);
                antena = new Antena(this, nombre, antenaFile.getName(), rol);

            } else {

                ModeloAntena modelo = new ModeloAntena(altura, ColorRGBA.Orange, simpleApp.getAssetManager());
                antena = new Antena(this, nombre, modelo, rol);
            }

            int ganancia = (Integer) config.get("ganancia");
            int sensibilidad = (Integer) config.get("sensibilidad");
            int perdidas = (Integer) config.get("perdidas");

            antena.setGanacia(ganancia);
            antena.setSensibilidad(sensibilidad);
            antena.setPerdidas(perdidas);

            antena.getModelo().addControl(new MouseEventControl() {
                @Override
                public void mouseButtonEvent(MouseButtonEvent event, Spatial target, Spatial capture) {
                    if (target != null) {
                        modeloRetenido = target;
                        for (Antena antena : antenas) {
                            if (target.equals(antena.getModelo())) {

                                antenaRetenida = antena;
                            }
                        }
                        antenaMovement = !antenaMovement;

                    } else {
                        antenaMovement = false;

                    }
//else if(event.isReleased()){antenaMovement = false;}
//            else{}

                }
            });

            antenas.add(antena);

        }

    }
    
    private void comprobarRoles(){
    
        int tran = 0;
        int rece = 0;
        
        for(Antena antena: antenas){
        if(antena.getRol().equals(Antena.TRANSMISOR)){
            tran++;}else{rece++;}
        
        }
        
        if(tran == 0 || rece == 0){
        antenas.get(0).setRol(Antena.TRANSMISOR);
        antenas.get(1).setRol(Antena.RECEPTOR);
        
        }
        
        for(Antena antena: antenas){
        
        antena.getContenedor().actualizarRol();
        
        }
                
        
    }
    
    
    private void cargarContenedores() {
        for (Antena antena : antenas) {
            if (((SimpleApplication) this.getApplication()).getGuiNode() != null) {
                ((SimpleApplication) this.getApplication()).getGuiNode().attachChild(antena.getContenedor());
                antena.getModelo().addControl(new AntenaControl(antena));
            } else {
                System.out.println("guiNode es nulo!!!");
            }
        }

    }

    protected class AntenaControl extends AbstractControl {

        private Antena antena;
        private ContenedorAntena contenedor;
        private Vector3f posicion;
        private Geometry wireBox;

        public AntenaControl(Antena antena) {
            this.antena = antena;
            this.contenedor = antena.getContenedor();
            wireBox = antena.getWireBox();
            posicion = new Vector3f(0, 0, 0);
        }

        @Override
        protected void controlUpdate(float tpf) {
            /*Consicional en caso de querer usar un asset en vez de una instancia de ModeloAntena*/
//        if(!((Node)antena.getModelo()).hasChild(wireBox))
//        wireBox.setLocalTranslation(antena.getModelo().getLocalTranslation());
//       

            if (antena.getModelo().getWorldTranslation() != null) {
                posicion = simpleApp.getCamera().getScreenCoordinates(
                        antena.getModelo().getWorldTranslation());
                contenedor.setLocalTranslation(posicion);
                contenedor.setPosicion(antena.getModelo().getWorldTranslation().
                        add(coordX, coordY, coordZ).subtract(0, antena.getAltura(), 0));
            }

            if (mostrarAntenaInfo) {

                antena.getContenedor().setCullHint(Spatial.CullHint.Inherit);

            } else {
                antena.getContenedor().setCullHint(Spatial.CullHint.Always);
            }

        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
        }

    }

    public Antena getTransmisor() {

        return transmisor;

    }

    public Antena getReceptor() {

        return receptor;

    }

}
