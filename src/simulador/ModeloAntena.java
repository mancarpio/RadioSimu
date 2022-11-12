/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author Manuel
 */
public class ModeloAntena extends Node{
    
    private float altura;
    private float radio;
    private Geometry esfera, caja, wireBox,cilindro;
    
    public ModeloAntena(float altura, ColorRGBA color, AssetManager assetManager){
    
     super();
        radio = 3;
        this.altura = altura;
        Material naranja = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        naranja.setColor("Color", color);
        
        Material blanco = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blanco.setColor("Color", ColorRGBA.White);
        
        Sphere esf = new Sphere(8,8,radio+2);
        Box b = new Box(15,15,altura/2);
        WireBox w = new WireBox(15, 15,altura/2); 
        Cylinder cil = new Cylinder(16, 16, radio, altura);
        
        wireBox = new Geometry("wireBox", w);
        caja = new Geometry("caja", b );
        esfera = new Geometry("esfera",esf);
        cilindro = new Geometry("cilindro", cil);
        esfera.setMaterial(naranja);
        wireBox.setMaterial(blanco);
        caja.setMaterial(blanco);
        caja.setCullHint(Spatial.CullHint.Always);
        wireBox.setCullHint(Spatial.CullHint.Always);
        cilindro.setMaterial(naranja);
        cilindro.move(0,0,altura/2);
        wireBox.move(0, 0, altura/2);
        caja.move(0, 0, altura/2);
//        wireBox.move(0,0,altura/2);
//        caja.move(0,0,altura/2);
        
        this.attachChild(cilindro);
        this.attachChild(esfera);
        this.attachChild(wireBox);
        this.attachChild(caja);
        
//        cilindro.rotate(0,90 * FastMath.DEG_TO_RAD ,0 );

//        caja.rotate(90 * FastMath.DEG_TO_RAD,0 ,0 );
//        wireBox.rotate(90 * FastMath.DEG_TO_RAD,0 ,0 );
        this.setLocalRotation(new Quaternion().fromAngles(FastMath.PI/2, 0, 0));
    
    } 

    
    public Geometry getCaja(){
    
        return caja;
    
    } 
    public Geometry getWireBox(){
    
    return wireBox;
    
    };
    
    public float getAltura(){
    
    return altura;
    
    }
    
}
