/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador;

import simulador.util.Elipsoide;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.simsilica.lemur.ActionButton;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.CallMethodAction;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.DragHandler;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.style.Styles;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import simulador.util.Potencia;
import simulador.util.RadioMat;

/**
 *
 * @author Manuel
 */
public class FresnelState extends BaseAppState {

    private static final String GROUP_FRESNEL = "Fresnel";
    private static final FunctionId SIMULAR_FRESNEL = new FunctionId("simularFresnel");
    private static final FunctionId ACTUALIZAR_RESULTADOS = new FunctionId("actualizarResultados");
    public static final ColorRGBA transparente = new ColorRGBA(1, 1, 1, 0.0f);

    private boolean activado;
    private CollisionResults resultados1, resultados2;
    private CameraFocusState focusState;
    private GuiState guiState;
    private InputMapper inputMapper;
    private Node rootNode;
    private Node fresnelNodo;
    private SimpleApplication simpleApp;
    private TerrainQuad terreno;
    private HashMap<String, Object> configGeneral;

    // onjetos relacionados con el elipsoide de fresnel
    private Elipsoide elipse1;
    private float ejeMayor;
    private float radio1;
    private float j;
    private Geometry fresnel1;
    private Geometry lineaEje;
    private Material matCol;

    //parametros necesarios para los calculos del radioenlace:
    // da y db corresponden a las distancias
    //entre el obstaculo y los puntos de emision y recepcion,
    //d1 y d2 corresponden a la distancia terrestre entre antenas
    private float distancia, da, db, d1, d2, h, frecuencia,
            alpha1, alpha2, lambda, factorCurvatura, radioTerrestre,
            v, perdidaDifraccion;
    private Vector3f obstaculo1;

    private float potenciaSeñal, potenciaTransmitida, potenciaRecibida, azimuth, inclinacion,
            nivelRx, campoErecibido, peorFresnel;
    private float radio_ficticio;
    private float admitanciaNormalizada;

    private int formatoPotencia;

    private Potencia potencia;

    private NumberFormat numberFormat;

    // objetos relacionados con las antenas
    private AntenaState antenaState;
    private ArrayList<Antena> antenas;
    private Spatial modeloTransmisor, modeloReceptor;

    //objetos relacionados a la interfaz grafica
    private DragHandler dragHandler;
    private Checkbox simuBox;
    private Checkbox resultadosBox;
    private Container simuCont;
    private ArrayList<Label> arrayLabels;
    private Container contPrueba;
    private Container contResult;
    private Container resultboxCont;
    private Container antenaExtboxCont;
    private Checkbox resultbox;
    private Checkbox antenaExtendidobox;
    private Label dlpotTx;
    private Label dlpotRx;
    private Label dlperTx;
    private Label dlperRx;
    private Container infoAntenasContenedor;
    private Label labelNombreTx;
    private Label labelAlturaTx;
    private Label labelNombreRx;
    private Label labelAlturaRx;
    private Label labelGanaciaTx;
    private Panel panelGanaciaTx;
    private Label labelGananciaRx;
    private Label labelPIRE;
    private Label labelSensibilidad;
    private Label labelPerdidasTx;
    private Label labelPerdidasRx;
    private Label labelGananciaTx;
    private Container panelEnlace;
    private Label labelAzimuth;
    private Label labelFrecuencia;
    private Label labelPotenciaTx;
    private Label labelDistancia;
    private Label labelInclinacion;
    private Label labelRecepcion;
    private Label labelPeorFresnel;
    private Label labelCampoE;
    private Label labelDifraccion;
    private Label labelDiferencia;

    //objetos para debug
    private Geometry linea1, linea2, linea3, esfera1, esfera2;
    private Line line1, line2, line3;
    private Sphere esfera;
    private Node debugNodo;
    private Vector3f nivelContacto;
    private Vector3f colisionLocal;
    private boolean elipsoideVisible, lineasVisibles, panelColisionVisible;
    private Antena transmisor;
    private Antena receptor;
    private Container elipsoideboxCont;
    private Checkbox elipsoidebox;
    private Container lineasboxCont;
    private Checkbox lineasbox;


    @Override
    protected void initialize(Application app) {
        setSimpleApp();
        numberFormat = NumberFormat.getInstance();

        configGeneral = simpleApp.getStateManager().
                getState(ConfiguracionInicialState.class).getConfigGeneral();
        arrayLabels = new ArrayList<Label>();
        inputMapper = GuiGlobals.getInstance().getInputMapper();
        terreno = simpleApp.getStateManager().getState(TerrenoState.class).getTerrain();

        debugNodo = new Node();
        simpleApp.getRootNode().attachChild(debugNodo);
        cargarParametros();

        //importar el array de antenas
        antenaState = this.getApplication().getStateManager().getState(AntenaState.class);
        antenas = antenaState.getAntenaArray();
        if (antenas.size() > 1) {
            System.out.println("sí existen las antenas");
        } else {
            System.err.println("se requiere un minimo de dos antenas!");
            System.exit(0);
        }

        transmisor = antenaState.getTransmisor();
        receptor = antenaState.getReceptor();
        modeloTransmisor = transmisor.getModelo();
        modeloReceptor = receptor.getModelo();

        //Se cargan los valores iniciales de cada parametro
        lambda = RadioMat.calcularLambda(frecuencia);
        perdidaDifraccion = 0;

        ejeMayor = (modeloTransmisor.getWorldTranslation().distance(modeloReceptor.getWorldTranslation()) ) * 0.97f;
        radio1 = RadioMat.calcularRadioF(frecuencia, 1, ejeMayor / (2 * 1000), ejeMayor / (2 * 1000));

        resultados1 = new CollisionResults();
        resultados2 = new CollisionResults();

        simuCont = new Container();
        simuCont.setInsets(new Insets3f(4, 4, 4, 4));
        simuBox = new Checkbox(" Activar Simulación ");
        simuBox.setSize(new Vector3f(200, 30, 2));
        simuCont.addChild(simuBox);
        ActionButton botonPrueba = new ActionButton(new CallMethodAction("imprimir", this, "imprimir"));
        contPrueba = new Container();
       // contPrueba.addChild(botonPrueba);
        contPrueba.setSize(new Vector3f(200, 30, 2));
        simpleApp.getGuiNode().attachChild(contPrueba);
        contPrueba.setLocalTranslation(35, 600, 2);

        resultboxCont = new Container();
        resultbox = new Checkbox("Mostrar panel de resultados");
        resultboxCont.addChild(resultbox);

        elipsoideboxCont = new Container();
        elipsoidebox = new Checkbox("Elipsoides visibles");
        elipsoideboxCont.addChild(elipsoidebox);

        lineasboxCont = new Container();
        lineasbox = new Checkbox("Lineas de colision visibles");
        lineasboxCont.addChild(lineasbox);

        antenaExtboxCont = new Container();
        antenaExtendidobox = new Checkbox("Información detallada de las antenas");
        antenaExtboxCont.addChild(antenaExtendidobox);

        rootNode = ((SimpleApplication) this.getApplication()).getRootNode();
        fresnelNodo = new Node();
        rootNode.attachChild(fresnelNodo);
        crearFresnel();
        System.out.println("simularfresnel state iniciado");
        crearPanelEnlace();
        panelEnlace.setLocalTranslation(300, 500, 2);
        crearPanelAntenas();
        guiState = new GuiState();
        focusState = new CameraFocusState();

    }



    @Override
    protected void cleanup(Application app) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void cargarParametros() {

        frecuencia = (float) configGeneral.get("frecuencia");
        this.potenciaSeñal = (float) configGeneral.get("potencia");
        formatoPotencia = (int) configGeneral.get("formatoPotencia");
        switch (formatoPotencia) {
            case 0:
                potencia = new Potencia(potenciaSeñal, Potencia.DECIBELMILI);
                break;
            case 1:
                if((float)configGeneral.get("potencia") < 1){
                
                    potenciaSeñal = 1;
                
                }
                potencia = new Potencia(potenciaSeñal, Potencia.MILIWATT);
                
                
                break;
            case 2:
                
                    if((float)configGeneral.get("potencia") < 1){
                
                    potenciaSeñal = 1;
                
                }
                    
                potencia = new Potencia(potenciaSeñal, Potencia.WATT);
                break;
            default:
                throw new AssertionError();
        }

        factorCurvatura = (float) configGeneral.get("factorCurvatura");
        radioTerrestre = (int) configGeneral.get("radioTerrestre");
        radio_ficticio = factorCurvatura * radioTerrestre;

        admitanciaNormalizada = RadioMat.calcularAdmitanciaNormalizadaHorizontal(
                RadioMat.CONUCTIVIDAD_TIERRA_MEDIA, RadioMat.PERMITIVIDAD_TIERRA_MEDIA,
                radio_ficticio, frecuencia);
    }

    @Override
    protected void onEnable() {
        System.out.println("FresnelSimulationState is enabled.");

        activado = false;
        this.getApplication().getStateManager().attach(focusState);
        ((SimpleApplication) this.getApplication()).getGuiNode().attachChild(simuCont);
        ((SimpleApplication) this.getApplication()).getGuiNode().attachChild(panelEnlace);

        ((SimpleApplication) this.getApplication()).getGuiNode().attachChild(infoAntenasContenedor);
        ((SimpleApplication) this.getApplication()).getStateManager().attach(guiState);
    }

    @Override
    protected void onDisable() {
        inputMapper.removeDelegate(SIMULAR_FRESNEL, this, "calcularRayos");
        inputMapper.removeDelegate(ACTUALIZAR_RESULTADOS, this, "actualizarResultados");

        activado = false;
    }

    public void setSimpleApp() {

        this.simpleApp = (SimpleApplication) this.getApplication();
    }



    private void crearFresnel() {

        //Para calcula el azimuth
        fresnelNodo.setLocalTranslation(modeloTransmisor.getLocalTranslation());
        fresnelNodo.lookAt(modeloReceptor.getWorldTranslation(), new Vector3f(0, 1, 0));
        float[] angulos = fresnelNodo.getLocalRotation().toAngles(null);
        azimuth = +FastMath.PI - angulos[1];

        if (activado) {
            if (fresnel1 != null) {
                fresnelNodo.detachChild(fresnel1);
            }

            ejeMayor = (modeloTransmisor.getWorldTranslation().distance(modeloReceptor.getWorldTranslation()) ) * 0.97f;
            radio1 = RadioMat.calcularRadioF(frecuencia, 1, ejeMayor / (2 * 1000), ejeMayor / (2 * 1000));
            elipse1 = new Elipsoide(25, 25, radio1, ejeMayor/2, false, false);
            fresnel1 = new Geometry("zona", elipse1);
            Material mat = new Material(this.getApplication().getAssetManager(),
                    "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", new ColorRGBA(0, 1, 1, 0.5f));
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            fresnel1.setMaterial(mat);
            fresnel1.setQueueBucket(RenderQueue.Bucket.Translucent);

            /*El proposito de fresnelNodo es   que el elipsoide tenga su centro de 
            referencia dependiente del transmisor y no de el terreno
             */
            fresnelNodo.attachChild(fresnel1);
            fresnel1.setCullHint(Spatial.CullHint.Inherit);
            float distancia = modeloTransmisor.getWorldTranslation().distance(modeloReceptor.getWorldTranslation());
            fresnel1.setLocalTranslation(0, 0, distancia);

            //trasladar el elipsoide al medio de las antenas
            Vector3f midpoint = new Vector3f(0, 0, 0);
            midpoint.interpolateLocal(modeloTransmisor.getWorldTranslation(),
                    modeloReceptor.getWorldTranslation(), 0.5f);
            Vector3f nuevomidpoint = new Vector3f();
            Vector3f position = modeloReceptor.getWorldTranslation();
            fresnelNodo.worldToLocal(midpoint, nuevomidpoint);

            fresnel1.setLocalTranslation(nuevomidpoint);

            if (elipsoideVisible) {

                fresnel1.setCullHint(Spatial.CullHint.Inherit);
            } else {
                fresnel1.setCullHint(Spatial.CullHint.Always);

            }

        }
    }

    public void actualizarResultados() {
        distancia = transmisor.getModelo().getLocalTranslation().
                distance(receptor.getModelo().getLocalTranslation());
        potenciaTransmitida = potencia.getPotenciaDbm() + transmisor.getGanancia();

        campoErecibido = RadioMat.calcularCampoRecibido(RadioMat.decibelALineal(potenciaTransmitida) / 1000,
                distancia);

        potenciaRecibida = potencia.getPotenciaDbm()
                + transmisor.getGanancia() - RadioMat.calcularPBEL(frecuencia, distancia / 1000)
                - this.perdidaDifraccion + receptor.getGanancia()
                - transmisor.getPerdidaDeLinea() - receptor.getPerdidaDeLinea();
        
        
        
        if (activado) {
            labelRecepcion.setText("Nivel de Recepcion = " + numberFormat.format(potenciaRecibida) + "dBm");
        } else {
            labelRecepcion.setText("Nivel de Recepcion = " + "--------" + "dBm");

        }
        
        
        if (activado && perdidaDifraccion < 1000) {
            if (campoErecibido < 0.01 && campoErecibido > 0.001) {
                labelCampoE.setText("Campo E recibido = " + numberFormat.format(campoErecibido * 1000) + " mV/m");
            } else if (campoErecibido < 0.001) {
                labelCampoE.setText("Campo E recibido = " + numberFormat.format(campoErecibido * 1000) + " uV/m");
            } else if (campoErecibido > 0.01) {
                labelCampoE.setText("Campo E recibido = " + campoErecibido + " V/m");
            }
        } else {

            labelCampoE.setText("Campo E recibido = " + "  ----  " + " mV/m");

        }

        labelAzimuth.setText("Azimuth = " + numberFormat.format(azimuth * FastMath.RAD_TO_DEG) + " ° ");
        labelDistancia.setText("Distancia = " + numberFormat.format(distancia / 1000) + " Kmt");
        labelInclinacion.setText("ángulo de Elevación = " + numberFormat.format((RadioMat.anguloInclinacion(transmisor.getModelo().getWorldTranslation(),
                receptor.getModelo().getWorldTranslation())) * FastMath.RAD_TO_DEG) + "°");

        if (activado) {
            labelPeorFresnel.setText("Peor Fresnel = " + numberFormat.format(peorFresnel) + "F1");

        } else {

            labelPeorFresnel.setText("Peor Fresnel = -------");
        }
        
        if(activado){
        if(perdidaDifraccion >= 1000){
        labelDifraccion.setText("Perdidas por difraccion =  -------");
        
        }else{
        labelDifraccion.setText("Perdidas por difraccion = "+ numberFormat.format(this.perdidaDifraccion) + " dB");
        
        }
        
        }else{
        labelDifraccion.setText("Perdidas por difraccion = ------- dB");
        
        }
        if(activado){
        
        labelDiferencia.setText("Distancia obstaculo/enlace = "+ numberFormat.format(this.h) + " mt");
        }else{
        labelDiferencia.setText("Distancia obstaculo/enlace = --------- ");
        
        }
        
    }

    /*
        Pérdidas de difracción (sin obstaculos aislados)
        para cualquier distancia a 10 MHz
        y frecuencias superiores. Ver UIT P 526-11 p.15
     */
    private void calcularDifraccionPorTerreno() {

        //Se calcula la distancia de visibilidad directa marginal dada por la expresión:
        float dlos = FastMath.sqrt(2 * radio_ficticio)
                * (FastMath.sqrt(transmisor.getAltura()) + FastMath.sqrt(receptor.getAltura()));

        if (distancia >= dlos) {
            System.out.println("metodo 3.1.1.");

            float y1 = RadioMat.alturaNormalizada(radio_ficticio, lambda, transmisor.getAltura());
            float y2 = RadioMat.alturaNormalizada(radio_ficticio, lambda, receptor.getAltura());
            float x = RadioMat.longitudNormalizada(radio_ficticio, lambda, distancia / 1000);

            float diferencia_e = RadioMat.funcionLongitudNormalizada(x)
                    + RadioMat.ganaciaAlturaNormalizada(y1, admitanciaNormalizada)
                    + RadioMat.ganaciaAlturaNormalizada(y2, admitanciaNormalizada);

            this.perdidaDifraccion = diferencia_e;

        } else {
            //Se calcula la altura libre de obstáculos más pequeña entre el trayecto 
            //de Tierra curva y el rayo entre las antenas, h.

            float c = (transmisor.getAltura() - receptor.getAltura())
                    / (transmisor.getAltura() + receptor.getAltura());

            float m = FastMath.sqr(distancia)
                    / (4 * radio_ficticio * (transmisor.getAltura() + receptor.getAltura()));

            float b = 2 * FastMath.sqrt((m + 1) / (3 * m))
                    * FastMath.cos((FastMath.PI / 3) + (1 / 3)
                            * FastMath.acos((3 * c / 2) * FastMath.sqrt((3 * m) / FastMath.pow(m + 1, 3))));

            float d1a = (distancia / 2) * (1 + b);

            float d2a = distancia - d1a;

            float hmin = ((((transmisor.getAltura()) - (FastMath.pow(d1a, 2) / (2 * radio_ficticio))) * d2a)
                    + (((receptor.getAltura()) - (FastMath.pow(d2a, 2) / (2 * radio_ficticio))) * d1a)) / distancia;

            //Se calcula el trayecto libre de obstáculos requerido para 
            //unas pérdidas de difracción cero, hreq, que viene dado por:
            float hreq = 0.552f * FastMath.sqrt(FastMath.abs(d1a * d2a * lambda / distancia));

            if (hmin > hreq) {

                this.perdidaDifraccion = 0;

            } else {
                //Se calcula el radio ficticio de la Tierra modificado
                float radio_modificado = 0.5f * FastMath.pow(distancia
                        / (FastMath.sqrt(transmisor.getAltura()) + FastMath.sqr(receptor.getAltura())), 2);
                float y1 = RadioMat.alturaNormalizada(radio_modificado, lambda, transmisor.getAltura());
                float y2 = RadioMat.alturaNormalizada(radio_modificado, lambda, receptor.getAltura());
                float x = RadioMat.longitudNormalizada(radio_modificado, lambda, distancia / 1000);

                float ah = RadioMat.funcionLongitudNormalizada(x)
                        + RadioMat.ganaciaAlturaNormalizada(y1, admitanciaNormalizada)
                        + RadioMat.ganaciaAlturaNormalizada(y2, admitanciaNormalizada);
                if (FastMath.sign(ah) == -1) {

                    this.perdidaDifraccion = 0;

                } else {

                    float a = (1 - (hmin / hreq)) * ah;

                    this.perdidaDifraccion = a;

                }

            }

        }

    }

    private void calcularRayos() {

        /*calculosNodo es ortogonal al angulo a la linea de vision 
            entre ambas antenas y
            sirve como sistema de referencia 
            para los elementos de debug */
//
        if (activado) {

            Vector3f puntoOrigen = new Vector3f();
            Vector3f puntoDireccion = new Vector3f();
            Vector3f vectorDireccion = new Vector3f();
            Vector3f vectorDireccion2 = new Vector3f();
            Vector3f vectorEje = new Vector3f();
            puntoOrigen = transmisor.getModelo().getWorldTranslation().clone();
            puntoDireccion = receptor.getModelo().getWorldTranslation().clone();

            
            float anguloBarrido = 60f;
            //hacia abajo
            Vector3f alturaArista = new Vector3f(Vector3f.ZERO);
                vectorDireccion.set(puntoDireccion.subtract(puntoOrigen).normalizeLocal());
                vectorDireccion2.set(vectorDireccion.clone().add(0, -1, 0));
                vectorDireccion2.cross(vectorDireccion, vectorEje);
                vectorEje.normalizeLocal();
            Quaternion x60 = new Quaternion().fromAngleAxis(60 * FastMath.DEG_TO_RAD, vectorEje);
                Quaternion x5 = new Quaternion().fromAngleAxis(-5 * FastMath.DEG_TO_RAD, vectorEje);
            
            //comienza desde 60 grados por encima de la linea de vista
            Vector3f nuevaDireccion = x60.mult(vectorDireccion);
            nuevaDireccion.normalizeLocal();
            
            
            do {
                
                CollisionResults resultados1 = new CollisionResults();
                
                Ray rayo1 = new Ray(puntoOrigen, nuevaDireccion);

                terreno.collideWith(rayo1, resultados1);

                if (resultados1.size() > 0) {
                    Vector3f colisionLocal = resultados1.getClosestCollision().getContactPoint();
                    // calculosNodo.worldToLocal(resultados1.getClosestCollision().getContactPoint(), colisionLocal);

                    float distanciaEnlace = puntoOrigen.distance(puntoDireccion);
                    float distanciaColision = puntoOrigen.distance(colisionLocal);
                    float distanciaColision2 = puntoDireccion.distance(colisionLocal);

                    if (distanciaColision < distanciaEnlace) {
                        
                        
                        
                        alpha1 = RadioMat.anguloInclinacion(puntoOrigen,
                                colisionLocal);
                        alpha2 = RadioMat.anguloInclinacion(puntoDireccion,
                                colisionLocal);

                        float razonInterpolacion = ((puntoOrigen.distance(colisionLocal) * FastMath.cos(alpha1))
                                / FastMath.cos(alpha2)) / distanciaEnlace;

                        Vector3f nivelContacto = puntoOrigen.clone().
                                interpolateLocal(puntoDireccion.clone(), razonInterpolacion);

                        h = nivelContacto.getY() - colisionLocal.getY();

                        this.nivelContacto = nivelContacto;
                        this.colisionLocal = colisionLocal;

                        distancia = distanciaEnlace;


                        peorFresnel = RadioMat.calcularPeorFresnel(frecuencia, 8, distanciaColision / 1000f, distanciaColision2 / 1000, h );

                        /*Lo siguiente es para hacer debug*/
                        if (lineasVisibles) {
                            debugNodo.detachAllChildren();

                            Material material1 = new Material(this.getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                            material1.setColor("Color", ColorRGBA.White);
                            Material material2 = new Material(this.getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                            material2.setColor("Color", ColorRGBA.Red);

                            line1 = new Line(puntoOrigen, puntoDireccion);
                            linea1 = new Geometry("linea1", line1);
                            linea1.setMaterial(material1);

                            line2 = new Line(puntoOrigen, colisionLocal);
                            linea2 = new Geometry("linea2", line2);
                            linea2.setMaterial(material1);

                            line3 = new Line(puntoDireccion, colisionLocal);
                            linea3 = new Geometry("linea3", line3);
                            linea3.setMaterial(material1);

                            debugNodo.attachChild(linea1);
                            debugNodo.attachChild(linea2);
                            debugNodo.attachChild(linea3);

                            if (esfera1 == null) {
                                esfera = new Sphere(8, 8, 3);
                                esfera1 = new Geometry("esfera1", esfera);
                                esfera1.setMaterial(material1);
                                debugNodo.attachChild(esfera1);
                            }
                            esfera1.setLocalTranslation(colisionLocal);
                            if (esfera2 == null) {
                                esfera2 = new Geometry("esfera2", esfera);
                                esfera2.setMaterial(material2);
                                debugNodo.attachChild(esfera2);
                            }
                            esfera2.setLocalTranslation(nivelContacto);
                        }
                        
                            if(colisionLocal.getY() <= 
                                modeloReceptor.getLocalTranslation().getY()-receptor.getAltura()){
                            this.calcularDifraccionPorTerreno();
                            return;
                        
                            }
                            v = RadioMat.calcularParametroGeometrico(distanciaColision, distanciaColision2, lambda, h);
                            if (v > -0.79) {
                            perdidaDifraccion = RadioMat.calcularDifraccionFilo(v);
                            } else {
                                
                        // Se escogió arbitrariamente un valor de 1000dB en caso de que las 
                        // perdidas no se puedan calcular por el parametro geometrico
                                
                            perdidaDifraccion = 1000f;

                            }
                        
                        return;
                    }
                    
                }
                    nuevaDireccion = x5.mult(nuevaDireccion.clone());
                anguloBarrido = anguloBarrido - 5;
                
                
            } while (anguloBarrido > -60 );

        }
    }

    private void crearPanelEnlace() {

        QuadBackgroundComponent fondo1 = new QuadBackgroundComponent(new ColorRGBA(0, 0.25f, 0.25f, 0.5f));
        
        /* Se crea la tabla de resultados de la simulacion*/
        panelEnlace = new Container();
        // panelEnlace.setBackground(new QuadBackgroundComponent(new ColorRGBA(0f, 0f, 0f, 0.9f), 5, 5, 0.02f, false));
        panelEnlace.setPreferredSize(new Vector3f(500, 140, 2));
        SpringGridLayout springLayout = new SpringGridLayout(Axis.Y, Axis.X, FillMode.ForcedEven, FillMode.ForcedEven);
        panelEnlace.setLayout(springLayout);

        if (frecuencia < 1000) {
            labelFrecuencia = new Label("Frecuencia = " + frecuencia + " Mhz", "glass");
        } else if (frecuencia > 1000) {
            labelFrecuencia = new Label("Frecuencia = " + frecuencia / 1000 + " Ghz", "glass");

        }
        labelFrecuencia.setBackground(fondo1.clone());

        springLayout.addChild(0, 0, labelFrecuencia);

        labelPotenciaTx = new Label("Potencia de la Señal = " + this.potencia.getPotenciaDbm() + " dbm", "glass");
        labelPotenciaTx.setFontSize(13);
        labelPotenciaTx.setTextVAlignment(VAlignment.Center);
        //labelPotenciaTx.setBackground(new QuadBackgroundComponent(new ColorRGBA(1 / 255f, 255 / 255f, 255 / 255f, 0.9f), 5, 5, 0.02f, false));
//        labelPotenciaTx.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.59f,0.61f,0.67f,0.7f),5,5, 0.02f, false));
        springLayout.addChild(0, 1, labelPotenciaTx);

        labelAzimuth = new Label("Azimuth = " + azimuth + "°");
        springLayout.addChild(1, 0, labelAzimuth);

        labelDistancia = new Label("Distancia = " + distancia / 1000 + " Kmt");
        labelDistancia.setBackground(fondo1.clone());
        springLayout.addChild(1, 1, labelDistancia);

        labelInclinacion = new Label("ángulo de Elevación = " + (RadioMat.anguloInclinacion(transmisor.getModelo().getWorldTranslation(),
                receptor.getModelo().getWorldTranslation())) * FastMath.RAD_TO_DEG + "°");
        labelInclinacion.setBackground(fondo1.clone());
        springLayout.addChild(2, 0, labelInclinacion);

        labelRecepcion = new Label("Nivel de Recepcion = " + potenciaRecibida + "dBm");
        springLayout.addChild(2, 1, labelRecepcion);

        labelPeorFresnel = new Label("Peor Fresnel = " + peorFresnel + "F1");
        springLayout.addChild(3, 0, labelPeorFresnel);

//        if (campoErecibido < 0.01) {
//            labelCampoE = new Label("Campo E recibido = " + campoErecibido * 1000 + " mV/m");
//        } else if (campoErecibido < 0.001) {
//            labelCampoE = new Label("Campo E recibido = " + campoErecibido * 10000 + " uV/m");
//        }
        labelCampoE = new Label("Campo E recibido = " + campoErecibido * 1000 + " mV/m");
        labelCampoE.setBackground(fondo1.clone());
        springLayout.addChild(3, 1, labelCampoE);
        
        labelDiferencia = new Label("Distancia obstaculo/enlace = "+ h + " mt.");
        labelDiferencia.setBackground(fondo1);
        springLayout.addChild(4, 0, labelDiferencia);
     
        labelDifraccion = new Label("Perdida por difraccion = "+ perdidaDifraccion + " dB");
        springLayout.addChild(4, 1, labelDifraccion);

        
        DragHandler draghandler2 = new DragHandler();
        CursorEventControl.addListenersToSpatial(panelEnlace, draghandler2);

    }

    private void crearPanelAntenas() {

        infoAntenasContenedor = new Container();
        infoAntenasContenedor.setPreferredSize(new Vector3f(500, 150, 2));
        SpringGridLayout springLayout2 = new SpringGridLayout(Axis.Y, Axis.X, FillMode.ForcedEven, FillMode.ForcedEven);
        infoAntenasContenedor.setLayout(springLayout2);
        infoAntenasContenedor.setLocalTranslation(400, 600, 2);

        Label labelTx = new Label("Transmisor");
        labelTx.setSize(new Vector3f(400, 120, 2));
        labelTx.setBackground(new QuadBackgroundComponent(new ColorRGBA(0, 0.25f, 0.25f, 0.5f)));
        //labelTx.setPreferredSize(new Vector3f(150, 40, 2));
        //labelTx.setSize(new Vector3f(400, 120, 2));
        // labelTx.setBorder(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 200 / 255f, 1f, 0.5f), 50, 20, 0.02f, false));
        springLayout2.addChild(0, 0, labelTx);

        Label labelRx = new Label("Receptor");
        labelRx.setBackground(new QuadBackgroundComponent(new ColorRGBA(0, 0.25f, 0.25f, 0.5f)));
        //labelRx.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 200 / 255f, 1f, 0.5f), 50, 20, 0.02f, false));
        //labelRx.setSize(new Vector3f(400, 120, 2));
        springLayout2.addChild(0, 1, labelRx);

        labelNombreTx = new Label("Nombre : " + transmisor.getNombre());
        // labelNombreTx.setSize(new Vector3f(400, 120, 2));
        //labelNombreTx.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 176 / 255f, 1f, 0.4f), 50, 20, 0.02f, false));
        springLayout2.addChild(1, 0, labelNombreTx);

        labelNombreRx = new Label("Nombre : " + receptor.getNombre());
        //labelNombreRx.setSize(new Vector3f(400, 120, 2));
        //labelNombreRx.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 176 / 255f, 1f, 0.4f), 50, 20, 0.02f, false));
        springLayout2.addChild(1, 1, labelNombreRx);

        labelAlturaTx = new Label("Altura : " + transmisor.getAltura()+ "mt.");
        //labelAlturaTx.setSize(new Vector3f(400, 120, 2));
        //labelAlturaTx.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 176 / 255f, 1f, 0.4f), 50, 20, 0.02f, false));
        springLayout2.addChild(2, 0, labelAlturaTx);

        labelAlturaRx = new Label("Altura : " + receptor.getAltura()+ "mt.");
        //labelAlturaRx.setSize(new Vector3f(400, 120, 2));
        //labelAlturaRx.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 176 / 255f, 1f, 0.4f), 50, 20, 0.02f, false));
        springLayout2.addChild(2, 1, labelAlturaRx);

        labelGananciaTx = new Label("Ganacia : " + transmisor.getGanancia()+"dBi");
        //labelGananciaTx.setSize(new Vector3f(400, 120, 2));
        //labelGananciaTx.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 176 / 255f, 1f, 0.4f), 50, 20, 0.02f, false));
        springLayout2.addChild(3, 0, labelGananciaTx);

        labelGananciaRx = new Label("Ganancia : " + receptor.getGanancia()+ "dBi");
        //labelGananciaRx.setSize(new Vector3f(400, 120, 2));
        //labelGananciaRx.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 176 / 255f, 1f, 0.4f), 50, 20, 0.02f, false));
        springLayout2.addChild(3, 1, labelGananciaRx);

        String uniPot = null;
        switch (formatoPotencia) {
            case 0:
                uniPot = "dBm";
                break;
            case 1:
                uniPot= "mW";
                break;
            case 2:
                uniPot= "W";
                break;
            default:
                throw new AssertionError();
        }
        
        labelPIRE = new Label("PIRE = " + (potenciaSeñal + transmisor.getGanancia() - antenas.get(0).getPerdidaDeLinea()) + uniPot);
//        labelPIRE.setSize(new Vector3f(400, 120, 2));
        //labelPIRE.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 176 / 255f, 1f, 0.4f), 50, 20, 0.02f, false));
        springLayout2.addChild(4, 0, labelPIRE);

        labelSensibilidad = new Label("Sensibilidad = " + receptor.getSensibilidad() + "dBm");
        //labelSensibilidad.setSize(new Vector3f(400, 120, 2));
        //labelSensibilidad.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 176 / 255f, 1f, 0.4f), 50, 20, 0.02f, false));
        springLayout2.addChild(4, 1, labelSensibilidad);

        labelPerdidasTx = new Label("Perdidas de Linea = " + transmisor.getPerdidaDeLinea()+"dB");
        //labelPerdidasTx.setSize(new Vector3f(400, 120, 2));
        //labelPerdidasTx.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 176 / 255f, 1f, 0.4f), 50, 20, 0.02f, false));
        springLayout2.addChild(5, 0, labelPerdidasTx);

        labelPerdidasRx = new Label("Perdidas de Linea = " + receptor.getPerdidaDeLinea() +"dB");
        labelPerdidasRx.setSize(new Vector3f(400, 120, 2));
        //labelPerdidasRx.setBackground(new QuadBackgroundComponent(new ColorRGBA(83 / 255f, 176 / 255f, 1f, 0.4f), 50, 20, 0.02f, false));
        springLayout2.addChild(5, 1, labelPerdidasRx);

        DragHandler dragHandler = new DragHandler();
        CursorEventControl.addListenersToSpatial(infoAntenasContenedor, dragHandler);

    }

    public void imprimir() {

        debugFunction();
        System.out.println("salida de datos:");
        System.out.println("d: " + distancia);
        System.out.println("h = " + h);
        System.out.println("v = " + v);
        System.out.println("peorFresnel : " + peorFresnel * 1000);
        System.out.println("perdidaObstaculo: " + perdidaDifraccion);
        System.out.println("colisionLocal " + colisionLocal);
        System.out.println("nivelContacto " + nivelContacto);

    }

    public void debugFunction() {
        CollisionResults results = new CollisionResults();
        Ray rayo = new Ray(antenas.get(0).getModelo().getWorldTranslation(),
                antenas.get(1).getModelo().getWorldTranslation().
                        subtract(antenas.get(0).getModelo().getWorldTranslation()).normalizeLocal());
        terreno.collideWith(rayo, results);
        if (results.size() > 0) {
            System.out.println("colison! " + results.getClosestCollision().getContactPoint());
        }

    }

    public Container getSimuCont() {
        return simuCont;

    }

    public Container getContenedoResultados() {

        return contResult;

    }

    public Container getResultBox() {

        return this.resultboxCont;
    }

    public Container getAntenaExtBox() {

        return antenaExtboxCont;

    }

    public Container getElipsoideBox() {
        return this.elipsoideboxCont;

    }

    public Container getLineasBox() {
        return this.lineasboxCont;

    }

    @Override
    public void update(float tpf) {

        activado = simuBox.isChecked();
        elipsoideVisible = elipsoidebox.isChecked();
        lineasVisibles = lineasbox.isChecked();

        if (!lineasVisibles || !activado) {
            debugNodo.detachAllChildren();
        }

        if (!elipsoideVisible) {
            fresnelNodo.detachAllChildren();

        }

        if (resultbox.isChecked()) {

            panelEnlace.setCullHint(Spatial.CullHint.Inherit);

        } else {
            panelEnlace.setCullHint(Spatial.CullHint.Always);
        }

        if (antenaExtendidobox.isChecked()) {
            infoAntenasContenedor.setCullHint(Spatial.CullHint.Inherit);
        } else {
            infoAntenasContenedor.setCullHint(Spatial.CullHint.Always);

        }

        crearFresnel();
        calcularRayos();
        //Actualizar panel del enlace
        actualizarResultados();

    }
}
