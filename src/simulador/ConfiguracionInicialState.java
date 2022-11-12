/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.system.AppSettings;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;

/**
 *
 * @author Manuel
 */
public class ConfiguracionInicialState extends BaseAppState {

    private ArrayList<Object> arrayConfiguraciones;
    private HashMap<String,Object> configGeneral;
    private ArrayList<HashMap<String,Object>> antenaConfigArray; 
    private JFrame frame;
    private Dialogo dialogo;
    private CameraKeyState cameraKeyState;
    private AppSettings appSettings;
    private SimpleApplication simpleApp;

    @Override
    protected void initialize(Application app) {
        
        setSimpleApp();
        appSettings = new AppSettings(true);
        frame = new JFrame();
//        dialogo = new Dialogo(frame,true); 
//       dialogo.pack();
//       dialogo.setResizable(false);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dialogo = new Dialogo(frame,true); 
        dialogo.pack();
        dialogo.setResizable(false);
       
        cameraKeyState = new CameraKeyState();
        
        
        
    }

    @Override
    protected void cleanup(Application app) {
        
        System.exit(0);
    }

    @Override
    protected void onEnable() {
        
        AppSettings settings = new AppSettings(true);
        
        
       if(dialogo.getValores() == null){  // En caso de que el dialogo se cierre sin entregar valores
       System.exit(0);
       }
       
       ArrayList config = dialogo.getValores();
       
       int resolucionEscogida = (Integer)((HashMap<String,Object>)config.get(0)).get("resolucion");
       
       System.out.println("resolucionEscogida "+resolucionEscogida);

        switch (resolucionEscogida) {
            case 0:
                settings.setWidth(800);
                settings.setHeight(600);
                break;
            case 1:
                settings.setWidth(1024);
                settings.setHeight(768);
                break;
            case 2:
                settings.setWidth(1360);
                settings.setHeight(768);
                break;
            default:
                break;
        }
simpleApp.setSettings(settings);
        this.setConfiguracion(config);
        simpleApp.restart();
//       arrayConfiguraciones = dialogo.getValores();
       configGeneral = (HashMap)arrayConfiguraciones.get(0);
       antenaConfigArray = (ArrayList)arrayConfiguraciones.get(1);

       
       getApplication().getStateManager().attach(cameraKeyState);
       
    }

    @Override
    protected void onDisable() {
        
        configGeneral.clear();
        antenaConfigArray.clear();
    }
    
    public ArrayList<HashMap<String,Object>> getConfigAntenas(){
    
    return this.antenaConfigArray;
    }
    
    public HashMap<String,Object> getConfigGeneral(){
    
    return this.configGeneral;
    }
    
    public void setConfiguracion(ArrayList config){
    this.arrayConfiguraciones = config;
    
    }
    
      public void setSimpleApp() {

        this.simpleApp = (SimpleApplication) (this.getApplication());

    }
}
