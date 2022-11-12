/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador;

import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import simulador.util.Potencia;

/**
 *
 * @author Manuel
 */
public class Antena {

    static final String TRANSMISOR = "Transmisor";
    static final String RECEPTOR = "Receptor";
    private String rol;
    private String nombre;
    private float sensibilidad,ganancia,perdidaDeLinea;
    private Spatial modelo;
    private AntenaState antenaState;
    private float alturaAntena;
    private BoundingBox boundingBox;
    private Geometry wireBox,caja;
    private Label nombreLabel, corrdLabel;
    private ContenedorAntena contenedor;

    public Antena() {

    }

    public Antena(AntenaState antenaState, String nombre, String ruta, String rol) {
        
        this.rol = rol;
        this.antenaState = antenaState;
        this.nombre = nombre;
        modelo = this.antenaState.getApplication().getAssetManager().loadModel(ruta);
        Material material = new Material(this.antenaState.getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Orange);
        modelo.setMaterial(material);
        modelo.setName(nombre);
        crearBoundingBox();
        
        alturaAntena = boundingBox.getYExtent();
        crearWireBox();
        modelo.setModelBound(boundingBox);
        contenedor = new ContenedorAntena(this);
        
        /*
            Los parametros sensibilidad(uV), perdida de linea(dB) y ganancia(dBm) vienen por defecto en 0.
        */
        perdidaDeLinea = 0;
        ganancia = 0;
        sensibilidad = 0;

    }

    public Antena(AntenaState antenaState, String nombre, ModeloAntena modelo, String rol) {
        
        this.rol = rol;
        this.antenaState = antenaState;
        this.nombre = nombre;
        this.modelo = modelo;
        modelo.setName(nombre);
        alturaAntena = modelo.getAltura();
        this.caja = modelo.getCaja();
        this.wireBox = modelo.getWireBox();
        contenedor = new ContenedorAntena(this);
        
        /*
            Los parametros sensibilidad(uV), perdida de linea(dB) y ganancia(dBm) vienen por defecto en 0.
        */
        perdidaDeLinea = 0;
        ganancia = 0;
        sensibilidad = 0;

    }

    public String getNombre() {

        return nombre;
    }
    
    public float getPerdidaDeLinea(){
    
    return perdidaDeLinea;
    
    }
    
    public void setNombre(String nombre) {

        this.nombre = nombre;

    }

    public void setAntennastate(AntenaState state) {

        this.antenaState = state;

    }
    
    
    
    public void setModelo(Spatial modelo) {

        this.modelo = modelo;
        alturaAntena = ((BoundingBox) modelo.getWorldBound()).getYExtent();

    }

    public void setModelo(String ruta, AntenaState state) {
        this.antenaState = state;
        modelo = this.antenaState.getApplication().getAssetManager().loadModel(ruta);
        Material material = new Material(this.antenaState.getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Orange);
        modelo.setMaterial(material);
        alturaAntena = ((BoundingBox) modelo.getWorldBound()).getYExtent();

    }
    
    public void setModelo(ModeloAntena modelo){
        
        
        this.modelo = modelo;
        this.alturaAntena = modelo.getAltura();
               
    
    }

    public void setModelo(String ruta) {

        Spatial modeloAntena = this.antenaState.getApplication().getAssetManager().loadModel(ruta);
        Material material = new Material(this.antenaState.getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Orange);
        modeloAntena.setMaterial(material);
        alturaAntena = ((BoundingBox) modelo.getWorldBound()).getYExtent();

    }

    public Spatial getModelo() {

        return modelo;

    }

    public void setRol(String rol) {

        this.rol = rol;

    }

    public String getRol() {

        return rol;

    }

    public float getAltura() {

        return alturaAntena;
    }
    
    public ContenedorAntena getContenedor(){
    
        return contenedor;
        
    }
    
    
    private void crearWireBox(){
    
    wireBox = WireBox.makeGeometry(boundingBox);
    wireBox.setMaterial(new Material(antenaState.getApplication().getAssetManager(),
                                              "Common/MatDefs/Misc/Unshaded.j3md"));
    wireBox.setLocalTranslation(modelo.getLocalTranslation());
    wireBox.setLocalRotation(modelo.getLocalRotation());
    wireBox.setLocalScale(modelo.getLocalScale());
    }
    protected Geometry getWireBox(){
    
    return wireBox;
            
    }
    
    
    private void crearBoundingBox(){
    
    boundingBox = (BoundingBox)modelo.getWorldBound();
    
    }
    
    public BoundingBox getBoundingBox(){
    
    return boundingBox;
    
    }
    
    
      public float getGanancia(){
    
    return ganancia;
    
    }
    public void setGanacia(float g){
    
        ganancia = g;
    
    }
    
    public void setSensibilidad(float s){
    
        this.sensibilidad = s;
    
    }
    
    public float getSensibilidad(){
    
    return sensibilidad;
    }
      
    public void setPerdidas(float per){
    this.perdidaDeLinea = per;
    }

}
  class ContenedorAntena extends Container {
        
        private Label alturaLabel;
        private Label rolLabel;
        private Label posLabel;
        private Label nombreLabel;
        private Antena antena;
        
        
        public ContenedorAntena(Antena antena){
        
        super();
        this.setSize(new Vector3f(100,150,2));
        this.antena = antena;
        nombreLabel = new Label(antena.getNombre());
        nombreLabel.setSize(new Vector3f(100,30,2));
        nombreLabel.setBackground(new QuadBackgroundComponent(new ColorRGBA(202/255,233/255,243/255,0.5f)));
        alturaLabel = new Label("Altura: "+ FastMath.floor(antena.getAltura()) +"mt.");
        alturaLabel.setSize(new Vector3f(100,30,2));
        alturaLabel.setBackground(new QuadBackgroundComponent(new ColorRGBA(202/255,233/255,243/255,0.5f)));
        posLabel = new Label("Posicion: ( "+FastMath.floor(antena.getModelo().getLocalTranslation().getX())
                            +", "+FastMath.floor(antena.getModelo().getLocalTranslation().getY())
                            +", "+FastMath.floor(antena.getModelo().getLocalTranslation().getZ())+ " )");
        posLabel.setSize(new Vector3f(100,30,2));
        posLabel.setBackground(new QuadBackgroundComponent(new ColorRGBA(202/255,233/255,243/255,0.5f)));
        rolLabel = new Label("Rol : "+antena.getRol());
        rolLabel.setSize(new Vector3f(100,30,2));
        rolLabel.setBackground(new QuadBackgroundComponent(new ColorRGBA(202/255,233/255,243/255,0.5f)));
        
        this.addChild(nombreLabel);
        this.addChild(alturaLabel);
        this.addChild(posLabel);
        this.addChild(rolLabel);
        
        
        }
        
        
        public void setNombre(String n){
        
        nombreLabel.setText(n);
        }
        
        public void actualizarRol(){
        rolLabel.setText(antena.getRol());
        }
        
        public void setPosicion(Vector3f pos){
        posLabel.setText("Posicion: ( "+FastMath.floor(pos.getX())
                            +", "+FastMath.floor(pos.getY())
                            +", "+FastMath.floor(pos.getZ())+ " )");
        }
        
        
    }
   