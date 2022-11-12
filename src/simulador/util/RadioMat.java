/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.util;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 *
 * @author Manuel Carpio
 */
public class RadioMat {

    //el radio ficticio de la Tierra en Kmt., 
    //en caso de no disponer del valor practico
    /**
     *
     */
    public static final float RADIO_TERRESTRE = 6500f; //Kilometros
    public static final float IMPEDANCIA_ESPACIO_LIBRE = 120 * FastMath.TWO_PI; //ohmios
    
    public static final float PERMITIVIDAD_TIERRA_MEDIA = 15f;  //
    public static final float PERMITIVIDAD_TIERRA_POBRE = 4f;   //  Faradios / metros
    public static final float PERMITIVIDAD_TIERRA_RICA = 25f;   //
    public static final float PERMITIVIDAD_ESPACIO_LIBRE = 8.85e-12f;   //
    
    public static final float PERMEABILIDAD_ESPACIO_LIBRE = 12.5663e-7f;   // Henrios / metros
    
    public static final float CONUCTIVIDAD_TIERRA_MEDIA = 0.005f;    //
    public static final float CONUCTIVIDAD_TIERRA_POBRE = 0.001f;   //   Siemens / Metros
    public static final float CONUCTIVIDAD_TIERRA_RICA = 0.020f;   //   
    /**
     *
     */
    public static final float VELOCIDAD_LUZ_VACIO = 3e+8f;

    public static final float[] BoersmanA = {
        1.595769140f,
        -0.000001702f,
        -6.808568854f,
        -0.000576361f,
        6.920691902f,
        -0.016898657f,
        -3.050485660f,
        -0.075752419f,
        0.850663781f,
        -0.025639041f,
        -0.150230960f,
        0.034404779f};

    public static final float[] BoersmanB = {
        -0.000000033f,
        4.255387524f,
        -0.000092810f,
        -7.780020400f
        - 0.009520895f,
        5.075161298f,
        -0.138341947f,
        -1.363729124f,
        -0.403349276f,
        0.702222016f,
        -0.216195929f,
        0.019547031f};
    
    
    public static final float[] BoersmanC = {
    
        0.000000000f,
        -0.024933975f,
        0.000003936f,
        0.005770956f,
        0.000689892f,
        -0.009497136f,
         0.011948809f,
          -0.006748873f,
         0.000246420f,
         0.002102967f,
         -0.001217930f,
         0.000233939f
         
    };
    public static final float[] BoersmanD = {
    
   
         0.199471140f,
        0.000000023f,
        -0.009351341f,
        0.000023006f,
        0.004851466f,
        0.001903218f,
         -0.017122914f,
        0.029064067f,
        -0.027928955f,
        0.016497308f,
        -0.005598515f,
        0.000838386f
    };

 
   

    /**
     * Para calcular el radio en mt de la enesima (n) Zona de Fresnel
     *
     * @param f frecuencia en MHz
     * @param n numero de la Zona de Fresnel
     * @param d1 distancia entre la antena 1 y el obstaculo en Kmt.
     * @param d2 distancia entre la antena 2 y el obstaculo en Kmt.
     * @return
     */
    public static float calcularRadioF(float f, int n, float d1, float d2) {

        float radio = 550f * FastMath.sqrt(((float) n * d1 * d2) / ((d1 + d2) * f));

        return radio;

    }

    /**
     * Longitud normalizada del enlace (Ver UIT P 526 - 11 p.8)
     *
     * @param lambda lambda
     * @param radio_ficticio radio ficticio de la Tierra (Kmt.)
     * @param distancia distancia del enlace (Kmt.)
     * 
     * @return
     */
    public static float longitudNormalizada(float radio_ficticio, float lambda, float distancia){
        
        //float x = 2.188f * FastMath.pow(frecuencia, 1/3f) * FastMath.pow(radio_ficticio,-2/3) * distancia;
       
      float x = FastMath.pow(FastMath.PI /
              (lambda * radio_ficticio * radio_ficticio),1/3) * distancia;
        
        return x;     
    } 
    
    
    public static float funcionLongitudNormalizada(float x){
    
    float fx;
    
    if(x >= 1.6f){
    
    fx = 11 + FastMath.log(x, 10) - 17.6f * x;
    return fx;
    }
    
    fx = -20 * FastMath.log(x, 10) - 5.6488f * FastMath.pow(x, 1.425f);
    return fx;
  
    }
    
    /**
     * Altura normalizada de una de las antenas (Ver UIT P 526 - 11 p.8)
     *
     * @param frecuencia frecuencia en MHz
     * @param radio_ficticio radio ficticio de la Tierra (mt.)
     * @param altura altura de la antena (mt.)
     * 
     * @return
     */

    public static float alturaNormalizada(float radio_ficticio, float lambda, float altura){
        
         float y = 2 + FastMath.pow(FastMath.PI * 
                 FastMath.PI/(lambda * lambda * radio_ficticio),1/3) * altura;
        
        //float y = 9.575e-3f * FastMath.pow(frecuencia, 2/3f) * FastMath.pow(radio_ficticio,-1/3) * distancia;
       return y;     
    } 
    
    
    
    
    
    public static float ganaciaAlturaNormalizada(float y, float k){
        
        float fy = 0;
        
        if(y > 2){
        
        fy = 17.6f * FastMath.pow((y - 1.1f), 1/2) - 5 * FastMath.log(y - 1.1f, 10) -8;
        
        }else if(y < 2){
        
           if(y > 10 * k && y <= 2){
           fy = 20 * FastMath.log(y + 0.1f * FastMath.pow(y, 3), 10);
           
           return fy;
           }else if( y > (k/10) &&  y <= (10 * k)){
           
           fy = 2 + 20 * FastMath.log(k, 10)+ 9 * FastMath.log(y/k, 10) *
                            (FastMath.log(y/k,10 ) + 1);
           return fy;
           }else if(y <= k/10){
           
           fy = 2 + 20 * FastMath.log(k, 10);
           return fy;
           }
        
        }
        
        
        return fy;
    } 

    
     /**
     * Calculo del peor Fresnel
     *
     * @param f frecuencia de la señal
     * @param n radio que se quiere calcular
     * @param d1 distancia desde la antena 1 hasta el punto en el que se evalua el radio
     * @param d2 distancia desde la antena 2 hasta el punto en el que se evalua el radio
     * @param h distancia entre el punto a evaluar y el obsaculo mas cercano
     *
     */
    
    
    public static float calcularPeorFresnel(float f, int n, float d1, float d2, float h) {

        float j = 0;
        float k = 0;
        for (int i = 1; i < n; i++) {
            k = RadioMat.calcularRadioF(f, i, d1, d2);
            if (h / k > 1) {
                j++;

            } else {
                j = j + (h / k);
                return j;
            }
        }

        return j;
    }

    /**
     * Calculo de la potencia isotropica radiada efectiva
     *
     * @param pt la potencia de salida del transmisor en dbW o dbm
     * @param gt la ganacia de la antena en db
     * @param lc las perdidas de linea entre transmisor y antena en db
     *
     */
    public float calcularPIRE(float pt, float gt, float lc) {

        float pire = pt + gt - lc;

        return pire;
    }

    /**
     * Convertir la cantidad lineal n a dB
     *
     * @param n
     * @return
     */
    public static float aDecibel(float n) {

        float db = 10 * FastMath.log(n, 10);
        return db;
    }
    
    

    /**
     * Convertir la cantidad n a dBm
     *
     * @param n potencia en miliWattios
     * @return
     */
//    public static float aDecibelMili(float n){
//        
//        float db = 10f*FastMath.log(n/1e-3f, 10f);
//        return db;
//    }
//    
    public static float decibelALineal(float n) {

        float lineal = FastMath.pow(10, n / 10);
        return lineal;
    }

    /**
     * calcular la longitud de onda de una señal en el espacio libre con
     * frecuencia f
     *
     * @param f
     * @return
     */
    public static float calcularLambda(float f) {

        float lambda = VELOCIDAD_LUZ_VACIO / (f * FastMath.pow(10, 6));

        return lambda;

    }
    
    
    /**
     * Calcular la admitancia de la superficie terrestre 
     * para polarizacion horizontal
     * (Ver UIT R P 526-11 p.6 )
     *
     * @param sigma conductividad efectiva (siemes/ metros) 
     * @param epsilon permitividad relativa efectiva (faradios / metros)
     * @param radio_ficticio radio ficticio de la Tierra (Kilometros).
     * @param frecuencia  frecuencia de trabajo del enlace (MHz.).
     * @return
     */
    
    public static float calcularAdmitanciaNormalizadaHorizontal(float sigma, float epsilon, float radio_ficticio, float frecuencia){
    
            float k = 0.36f * FastMath.pow(frecuencia * radio_ficticio, -1/3f) * 
                    FastMath.pow((FastMath.pow(epsilon - 1 , 2) + FastMath.pow(18000*sigma/frecuencia, 2)),-1/4f);
    
            return k;
    }
    public static float calcularAdmitanciaNormalizadaVertical(float sigma, float epsilon, float radio_ficticio, float frecuencia){
    
            float k = RadioMat.calcularAdmitanciaNormalizadaHorizontal(sigma, epsilon, radio_ficticio, frecuencia)
                    * FastMath.pow((FastMath.pow(epsilon , 2) + FastMath.pow(18000*sigma/frecuencia, 2)),1/2f);
    
            return k;
    }
    
    /**
     * Calcular la perdida basica en el espacio libre en dB
     *
     * @param f frecuencia (Mhz)
     * @param d distancia (Kmt.)
     * @return
     */
    public static float calcularPBEL(float f, float d) {

        float perdida = 32.4f + aDecibel(f) + aDecibel(d);

        return perdida;
    }

    public static float computarPerdidasFresnel(float v) {

        float c = v - ((FastMath.sqr(FastMath.PI) * FastMath.pow(v, 5)) / 40)
                + ((FastMath.pow(FastMath.PI, 4) * FastMath.pow(v, 9)) / 3456);

        float s = (FastMath.PI / 6) - ((FastMath.pow(FastMath.PI, 3) * FastMath.pow(v, 7)) / 336)
                + ((FastMath.pow(FastMath.PI, 5) * FastMath.pow(v, 11)) / 42240);

        float j = -20 * FastMath.log((FastMath.sqrt(FastMath.sqr(1 - c - s) + FastMath.sqr(c - s))) / 2, 10);
        return j;
    }

    /**
     * Calcular la potencia por campo electrico
     *
     *
     * @param campo Campo electrico en Voltios/ metros
     * @param d distancia (mts.)
     * @return la potencia en Watt.
     */
    public static float potenciaPorCampoE(float campo, float d) {

        float pt = FastMath.TWO_PI * FastMath.sqr(campo * d) / IMPEDANCIA_ESPACIO_LIBRE;

        return pt;
    }

    /**
     * Calcular la intensidad del campo electrico recibido
     *
     *
     * @param pt potencia de transmision pt (Wattios)
     * @param d distancia (mts.)
     * @param g ganacia de la antena en dB
     * @return la densidad de campo electrico recibida en V/mt.
     */
    public static float calcularCampoRecibido(float pt, float d, float g) {

        float gl = decibelALineal(g);

        float e = (FastMath.sqrt(IMPEDANCIA_ESPACIO_LIBRE) / d) * FastMath.sqrt(pt * gl / FastMath.TWO_PI);
        return e;

    }

    /**
     * Calcular la intensidad del campo electrico recibido
     * En caso de que la
     * potencia ya tenga en cuenta la ganacia de la antena transmisora.
     *
     *
     * @param pt potencia de transmision pt (Wattios)
     * @param d distancia (mts.)
     * @return la densidad de campo electrico recibida en V/mt.
     */
    public static float calcularCampoRecibido(float pt, float d) {

        float e = calcularCampoRecibido(pt, d, 0);
        return e;

    }

    /**
     * se calcula el angulo entre la componente horizontal(x,z) y la componente
     * vertical (y) de un vector v1(x1,y1,z1) con respecto a un punto en el
     * espacio v2(x2,y2,z2)
     *
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static float anguloInclinacion(
            float x1, float y1, float z1, float x2, float y2, float z2) {

        float angulo = -1 * FastMath.atan2((y1 - y2), (FastMath.sqrt((x1 - x2) * (x1 - x2) + (z1 - z2) * (z1 - z2))));

        return angulo;

    }

    /**
     * se calcula el angulo entre la componente horizontal(x,z) y la componente
     * vertical (y) de un vector v1(x1,y1,z1) con respecto al origen (0,0,0)
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static float anguloInclinacion(float x, float y, float z) {

        float angulo = FastMath.atan2(y, FastMath.sqrt(x * x
                + z * z));

        return angulo;

    }

    /**
     * se calcula el angulo entre la componente horizontal(x,z) y la componente
     * vertical (y) de un vector con respecto a un punto en el espacio
     *
     * @param vector
     * @param referencia
     * @return
     */
    public static float anguloInclinacion(Vector3f vector, Vector3f referencia) {

        float angulo = anguloInclinacion(vector.x, vector.y, vector.z, referencia.x, referencia.y, referencia.z);

        return angulo;

    }

    /**
     * se calcula la agrupacion de todos los parametros geometricos "v "para el
     * caso de obstaculo de unic en arista de filo de cuchillo
     *
     * @param h la distancia entre la altura de la arista y la linea de vista de
     * las antenas
     * @param lambda la longitud de onda de la señal
     * @param d1 distancia entre la antena 1 y el obstaculo
     * @param d2 distancia entre la antena 2 y el obstaculo
     * @return *
     */
//    public static float calcularParametroGeometrico(float h, float lambda, float d1, float d2){
//    float v = h * FastMath.sqrt((2/lambda)*((1/d1)+(1/d2)));
//    return v;
//    }
    /**
     * se calcula la agrupacion de todos los parametros geometricos "v "para el
     * caso de obstaculo de unic en arista de filo de cuchillo
     *
     * @param lambda la longitud de onda de la señal
     * @param alfa1 angulo vertical entre la antena 1 y el obstaculo
     * @param alfa2 angulo vertical entre la antena 2 y el obstaculo
     * @param d longitud del trayecto
     */
//    public static float calcularParametroGeometrico( float d, float alfa1, float alfa2, float lambda){
//    float v = FastMath.sqrt((2*d/lambda)*alfa1*alfa2);
//    return v;
//    }
    public static float calcularParametroGeometrico(float d1, float d2, float lambda, float h) {
        float v = h * FastMath.sqrt((2 / lambda) * ((1 / d1) + (1 / d2)));
        return v;
    }

    /**
     * Retorna la perdida por difracción en decibelios causada por un obstaculo
     * unico con arista en filo de cuchillo
     *
     * Este metodo solo debe ser usado cuando el argumento de entrada v sea
     * mayor a -0.79
     */
    public static float calcularDifraccionFilo(float v) {

        float perdida = 6.9f + 20 * FastMath.log((FastMath.sqrt(FastMath.sqr(v - 0.1f) + 1) + v - 0.1f), 10);

        return perdida;
    }

    public static float funcionAuxiliar(float v){
    
        float c = funcionCoseno(v);
        float s = funcionSeno(v);
        
       float j = -20 * FastMath.log((FastMath.sqrt(FastMath.sqr(1 - c - s) + FastMath.sqr(c - s))) / 2, 10);
       
       return j;
    
    }
    
    //Experimental, referente a la integral compleja de fresnel
    public static float funcionCoseno(float v){
    
        float signo = 1;
        if(FastMath.sign(v) == -1){
        signo = -1;
        v = FastMath.abs(v);
        }
        
        
    float x = 0.5f * FastMath.PI * FastMath.sqr(v);
       
        
        float j = 0;
        if(x >= 0 && x < 4){
            float k = FastMath.cos(x) * FastMath.sqrt(x/4);
            float m = 0;
            
            for (int i = 0; i < 12; i++) {
                
                m = (RadioMat.BoersmanA[i] - RadioMat.BoersmanB[i]) *
                        FastMath.pow((x/4), i);
                j = j +(k * m);
                
            }
                return j * signo;
            
        }else if(x >= 4){
        
        
        float k = FastMath.cos(x) * FastMath.sqrt(x/4);
        float m = 0;
        
                  for (int i = 0; i < 12; i++) {
                
                m = (RadioMat.BoersmanC[i] - RadioMat.BoersmanD[i]) *
                        FastMath.pow((4/x), i);
                j = j +(k * m);
            }
                  j = j + (1/2);
                  
                  return j * signo;
        }
        else{
         return j;
        }
    
    }
    
        //Experimental, referente a la integral compleja de fresnel

    public static float funcionSeno(float v){
    
        
           float signo = 1;
        if(FastMath.sign(v) == -1){
        signo = -1;
        v = FastMath.abs(v);
        }
        
    float x = 0.5f * FastMath.PI * FastMath.sqr(v);
       
        
        float j = 0;
        if(x >= 0 && x < 4){
            float k = FastMath.sin(x) * FastMath.sqrt(x/4);
            float m = 0;
            
            for (int i = 0; i < 12; i++) {
                m = (RadioMat.BoersmanA[i] - RadioMat.BoersmanB[i]) *
                        FastMath.pow((x/4), i);
                j = j +(k * m);
                
            }
                return j * signo;
            
        }else if(x >= 4){
        
        
        float k = FastMath.sin(x) * FastMath.sqrt(x/4);
        float m = 0;
        
                  for (int i = 0; i < 12; i++) {
                
                m = (RadioMat.BoersmanC[i] - RadioMat.BoersmanD[i]) *
                        FastMath.pow((4/x), i);
                j = j +(k * m);
            }
                  j = j + (1/2);
                  
                  return j * signo;
        }
        else{
         return j;
        }
    
    }
}
