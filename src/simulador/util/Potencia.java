/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.util;

/**
 *
 * @author Manuel
 */
public class Potencia {
    
    private float potenciaWatt;
    private float potenciamWatt;
    private float potenciadbm;
    
    public static final int DECIBELMILI = 101;
    public static final int WATT = 102;
    public static final int MILIWATT = 103;
    
    
    public Potencia(float pot, int tipo){
    
        switch (tipo) {
            case DECIBELMILI:
                potenciadbm = pot;
                potenciamWatt = RadioMat.decibelALineal(pot);
                potenciaWatt = RadioMat.decibelALineal(pot/1000);
                
                break;
            case WATT:
                potenciaWatt = pot;
                potenciamWatt = pot * 1000;
                potenciadbm = RadioMat.aDecibel(pot*1000);
                
                break;
            case MILIWATT:
                potenciamWatt = pot;
                potenciaWatt = pot /1000;
                potenciadbm = RadioMat.aDecibel(pot);
                break;
            default:
                break;
        }
    }
    
    public float getPotenciaDbm(){
    return potenciadbm;
    }
    
    public float getPotenciaMiliWattios(){
    
    return potenciamWatt;
    
    }
    
    public float getPotenciaWattios(){
    
    return potenciaWatt;
    
    }
    
    public float getpotenciaMiliWattios(){
    
    return potenciamWatt;
    
    }
}
