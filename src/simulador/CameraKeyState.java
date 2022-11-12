
/*
 * $Id$
 *
 * Copyright (c) 2013-2013 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package simulador;

import simulador.util.CameraKeyMovementFunctions;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.MethodCommand;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import com.simsilica.lemur.component.SpringGridLayout;

/**
 *
 * @author PSpeed
 */
public class CameraKeyState extends BaseAppState
        implements AnalogFunctionListener, StateFunctionListener{

    private GuiState guiState;
    private CameraFocusState focusState;
    private InputMapper inputMapper;
    private Camera camera;
    private double turnSpeed = 2.5;  // one half complete revolution in 2.5 seconds
    private double yaw = FastMath.PI;
    private double pitch;
    private double maxPitch = FastMath.HALF_PI;
    private double minPitch = -FastMath.HALF_PI;
    private Quaternion cameraFacing = new Quaternion().fromAngles((float) pitch, (float) yaw, 0);
    private double forward;
    private double side;
    private double elevation;
    private double speed = 600.0;
    private Button[] botones;
    private MethodCommand movAde;
    private Container contenedorCam;

    private Container contenedorBotones;
    private SpringGridLayout springLayout;
    private int ancho;
    private int alto;
    private MethodCommand resetearMov, movAtras;
    private MethodCommand movIzq;
    private MethodCommand movDer;
    private MethodCommand movArr;
    private MethodCommand movAba;
    private TerrenoState terrenoState;
    

    public CameraKeyState() {
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
        updateFacing();
    }

    public double getPitch() {
        return pitch;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
        updateFacing();
    }

    public double getYaw() {
        return yaw;
    }

    public void setRotation(Quaternion rotation) {
        // Do our best
        float[] angle = rotation.toAngles(null);
        this.pitch = angle[0];
        this.yaw = angle[1];
        updateFacing();
    }

    public Quaternion getRotation() {
        return camera.getRotation();
    }

    @Override
    protected void initialize(Application app) {
        this.camera = app.getCamera();
        CameraKeyMovementFunctions.initializeDefaultMappings(GuiGlobals.getInstance().getInputMapper());

        GuiGlobals.getInstance().setCursorEventsEnabled(true);
        //    this.getInputManager().setCursorVisible(true);
        this.getApplication().getStateManager().getState(GuiState.class);

        if (inputMapper == null) {
            inputMapper = GuiGlobals.getInstance().getInputMapper();
        }

        // Most of the movement functions are treated as analog.        
        inputMapper.addAnalogListener(this,
                CameraKeyMovementFunctions.F_Y_LOOK,
                CameraKeyMovementFunctions.F_X_LOOK,
                CameraKeyMovementFunctions.F_MOVE,
                CameraKeyMovementFunctions.F_ELEVATE,
                CameraKeyMovementFunctions.F_STRAFE);

        // Only run mode is treated as a 'state' or a trinary value.
        // (Positive, Off, Negative) and in this case we only care about
        // Positive and Off.  See CameraMovementFunctions for a description
        // of alternate ways this could have been done.
        inputMapper.addStateListener(this,
                CameraKeyMovementFunctions.F_RUN);

        
        /*Se crrean los MethodCommands para el movimiento de la camara*/
        
        movAde = new MethodCommand(this, "moverAdelante");
        movAtras = new MethodCommand(this,"moverAtras");
        movIzq = new MethodCommand(this,"moverIzquierda");
        movDer = new MethodCommand(this,"moverDerecha");
        movArr = new MethodCommand(this,"moverArriba");
        movAba = new MethodCommand(this,"moverAbajo");
        resetearMov = new MethodCommand(this, "resetearMovimiento");

        // Se declara la instancia de TerrenoState:
        
        terrenoState = new TerrenoState();
    }

    @Override
    protected void cleanup(Application app) {

        inputMapper.removeAnalogListener(this,
                CameraKeyMovementFunctions.F_Y_LOOK,
                CameraKeyMovementFunctions.F_X_LOOK,
                CameraKeyMovementFunctions.F_MOVE,
                CameraKeyMovementFunctions.F_ELEVATE,
                CameraKeyMovementFunctions.F_STRAFE);
        inputMapper.removeStateListener(this,
                CameraKeyMovementFunctions.F_RUN);
    }

    @Override
    protected void enable() {
        // Make sure our input group is enabled
        inputMapper.activateGroup(CameraKeyMovementFunctions.GROUP_MOVEMENT);

//        // And kill the cursor
//        GuiGlobals.getInstance().setCursorEventsEnabled(false);
//        
//        // A 'bug' in Lemur causes it to miss turning the cursor off if
//        // we are enabled before the MouseAppState is initialized.
//        getApplication().getInputManager().setCursorVisible(false);  
//        
        this.getApplication().getStateManager().attach(terrenoState);
        crearBotones();
        System.out.println("CameraMovementState is enable");
    }

    @Override
    protected void disable() {
        System.out.println("CameraMovementState is disable");

        inputMapper.deactivateGroup(CameraKeyMovementFunctions.GROUP_MOVEMENT);
        GuiGlobals.getInstance().setCursorEventsEnabled(true);
    }

    @Override
    public void update(float tpf) {

        // 'integrate' camera position based on the current move, strafe,
        // and elevation speeds.
        if (forward != 0 || side != 0 || elevation != 0) {
            Vector3f loc = camera.getLocation();

            Quaternion rot = camera.getRotation();
            Vector3f move = rot.mult(Vector3f.UNIT_Z).multLocal((float) (forward * speed * tpf));
            Vector3f strafe = rot.mult(Vector3f.UNIT_X).multLocal((float) (side * speed * tpf));

            // Note: this camera moves 'elevation' along the camera's current up
            // vector because I find it more intuitive in free flight.
            Vector3f elev = rot.mult(Vector3f.UNIT_Y).multLocal((float) (elevation * speed * tpf));

            loc = loc.add(move).add(strafe).add(elev);
            camera.setLocation(loc);
            
        }
    }

    /**
     * Implementation of the StateFunctionListener interface.
     */
    @Override
    public void valueChanged(FunctionId func, InputState value, double tpf) {

        // Change the speed based on the current run mode
        // Another option would have been to use the value
        // directly:
        //    speed = 3 + value.asNumber() * 5
        //...but I felt it was slightly less clear here.   
        boolean b = value == InputState.Positive;
        if (func == CameraKeyMovementFunctions.F_RUN) {
            if (b) {
                speed = 10;
            } else {
                speed = 3;
            }
        }
    }

    /**
     * Implementation of the AnalogFunctionListener interface.
     */
    @Override
    public void valueActive(FunctionId func, double value, double tpf) {

        // Setup rotations and movements speeds based on current
        // axes states.    
        if (func == CameraKeyMovementFunctions.F_Y_LOOK) {
            pitch += -value * tpf * turnSpeed;
            if (pitch < minPitch) {
                pitch = minPitch;
            }
            if (pitch > maxPitch) {
                pitch = maxPitch;
            }
        } else if (func == CameraKeyMovementFunctions.F_X_LOOK) {
            yaw += -value * tpf * turnSpeed;
            if (yaw < 0) {
                yaw += Math.PI * 2;
            }
            if (yaw > Math.PI * 2) {
                yaw -= Math.PI * 2;
            }
        } else if (func == CameraKeyMovementFunctions.F_MOVE) {
         
            this.forward = value;
            return;
        } else if (func == CameraKeyMovementFunctions.F_STRAFE) {
            this.side = -value;
            return;
        } else if (func == CameraKeyMovementFunctions.F_ELEVATE) {
            this.elevation = value;
            return;
        } else {
            return;
        }
        updateFacing();
    }

 

    protected void updateFacing() {
        cameraFacing.fromAngles((float) pitch, (float) yaw, 0);
        camera.setRotation(cameraFacing);
    }




    private void crearBotones() {

    
        contenedorBotones = new Container();
        springLayout = new SpringGridLayout();
        contenedorBotones.setLayout(springLayout);
        contenedorCam = new Container();
        contenedorCam.setLayout(new BorderLayout());
       // contenedorCam.setPreferredSize(new Vector3f(230, 230, 0));

        /*
        Se crean los botones de las flechas
         */
        botones = new Button[6];

        Button boton2 = new Button("");
        botones[0] = boton2;
        Button boton3 = new Button("");
        botones[1] = boton3;
        Button boton4 = new Button("");
        botones[2] = boton4;
        Button boton5 = new Button("");
        botones[3] = boton5;
        Button boton6 = new Button("");
        botones[4] = boton6;
        Button boton7 = new Button("");
        botones[5] = boton7;

//        
        /*se calcula el tamaño ideal para los botones*/
//        Vector2f vectorIconos = new Vector2f(FastMath.floor(ancho *0.03f),FastMath.floor(alto *0.03f));
//        Vector3f vectorBotones = new Vector3f(FastMath.floor(ancho *0.03f),FastMath.floor(alto *0.03f),0f);
        Vector2f vectorIconos = new Vector2f(20, 20);
        Vector3f vectorBotones = new Vector3f(20, 20, 2);

        IconComponent flechaArr = new IconComponent("Textures/flechaArr.png");
        flechaArr.setIconSize(vectorIconos);

        botones[0].setSize(vectorBotones);
        botones[0].setIcon(flechaArr);

        IconComponent flechaDer = new IconComponent("Textures/flechaDer.png");
        flechaDer.setIconSize(vectorIconos);
        botones[1].setSize(vectorBotones);
        botones[1].setIcon(flechaDer);

        IconComponent flechaAba = new IconComponent("Textures/flechaAba.png");
        flechaAba.setIconSize(vectorIconos);
        botones[2].setIcon(flechaAba);
        botones[2].setSize(vectorBotones);

        IconComponent flechaIzq = new IconComponent("Textures/flechaIzq.png");
        flechaIzq.setIconSize(vectorIconos);
        botones[3].setSize(vectorBotones);
        botones[3].setIcon(flechaIzq);

        //IconComponent flechaIzq = new IconComponent("Textures/flechaIzq.png");
        //flechaIzq.setIconSize(vectorIconos);
        Label arr = new Label("+");
        arr.setFontSize(30);
        botones[4].setTextHAlignment(HAlignment.Center);
        botones[4].setTextVAlignment(VAlignment.Center);
        botones[4].setSize(vectorBotones);
       botones[4].attachChild(arr);
        //boton6.setIcon(flechaIzq);

        //IconComponent flechaIzq = new IconComponent("Textures/flechaIzq.png");
        //flechaIzq.setIconSize(vectorIconos);
        Label aba = new Label("-");
        aba.setFontSize(30);
        botones[5].setTextHAlignment(HAlignment.Center);
        botones[5].setTextVAlignment(VAlignment.Center);
        botones[5].setSize(vectorBotones);
        botones[5].attachChild(aba);
        //boton5.setIcon(flechaIzq);

        springLayout.addChild(0, 1, botones[0]);
        springLayout.addChild(1, 2, botones[1]);
        springLayout.addChild(2, 1, botones[2]);
        springLayout.addChild(1, 0, botones[3]);
        springLayout.addChild(0, 2, botones[4]);
        springLayout.addChild(2, 2, botones[5]);
        
        


        /*
        Se crean los sliders
         */
      
      //  contenedorBotones.setPreferredSize(new Vector3f(230, 230, 0));
        contenedorCam.addChild(contenedorBotones);
        contenedorCam.setLocalTranslation(900, 650, 2);
        //((SimpleApplication)this.getApplication()).getGuiNode().attachChild(contenedorCam);
        
        
        /*Se aplican los MethodMommands para el movimiento de la camara*/
         botones[0].addCommands(Button.ButtonAction.Down,movAde);
         botones[0].addCommands(Button.ButtonAction.Up,resetearMov);
         
     //    Method movatras = this.getClass().getMethod("moverAtras", Float.class);
         botones[2].addCommands(Button.ButtonAction.Down, movAtras);
         botones[2].addCommands(Button.ButtonAction.Up, resetearMov);
    
         
         botones[3].addCommands(Button.ButtonAction.Down, movIzq);
         botones[3].addCommands(Button.ButtonAction.Up, resetearMov);
         
         botones[1].addCommands(Button.ButtonAction.Down, movDer);
         botones[1].addCommands(Button.ButtonAction.Up, resetearMov);
    
         botones[4].addCommands(Button.ButtonAction.Down, movArr);
         botones[4].addCommands(Button.ButtonAction.Up, resetearMov);
         
         botones[5].addCommands(Button.ButtonAction.Down, movAba);
         botones[5].addCommands(Button.ButtonAction.Up, resetearMov);
         
    
    for(Button boton: botones){
    
        System.out.println(""+boton.getName()+" "+boton.getSize());
    }
    }
    
    public void moverAdelante() {
        
        this.forward =30.0* ((SimpleApplication)this.getApplication()).getTimer().getTimePerFrame();
        System.out.println("mover adelante");

    }
    public void moverAtras(){
        this.forward = -30.0 * ((SimpleApplication)this.getApplication()).getTimer().getTimePerFrame();
        System.out.println("mover atras");
    }
    
    public void moverIzquierda(){
        this.side = 30.0 * ((SimpleApplication)this.getApplication()).getTimer().getTimePerFrame();
        
    }
    
    public void moverDerecha(){
        this.side = -30.0 * ((SimpleApplication)this.getApplication()).getTimer().getTimePerFrame();
    }
        
    public void moverArriba(){
        this.elevation = 30.0 * ((SimpleApplication)this.getApplication()).getTimer().getTimePerFrame();
    }
        
    public void moverAbajo(){
        this.elevation = -30.0 * ((SimpleApplication)this.getApplication()).getTimer().getTimePerFrame();
    }
    
    public void resetearMovimiento(){
    
    this.forward = 0;
    this.elevation = 0;
    this.side = 0;
    }
    
        public Container getContenedorCam(){
    
    return contenedorCam;
    }
}
