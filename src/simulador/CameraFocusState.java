package simulador;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.core.VersionedReference;
import java.util.ArrayList;

public class CameraFocusState extends BaseAppState implements AnalogListener, ActionListener {

    private static final String MOUSE_MOVE_RIGHT = "Right";
    private static final String MOUSE_MOVE_LEFT = "Left";
    private static final String MOUSE_MOVE_UP = "Up";
    private static final String MOUSE_MOVE_DOWN = "Down";

    private static final String TOGGLE_ROTATE = "Toggle_Rotate";
    private static final String TOGGLE_TRANSLATE = "Toggle_Translate";
    private static final String RESET_OFFSET = "Reset_Offset";

    private static final String ZOOM_IN = "Zoom_In";
    private static final String ZOOM_OUT = "Zoom_Out";

    private static final String MED_LINEA_VISION = "Punto medio";
    private static final String TRANSMISOR = "transmisor";
    private static final String RECEPTOR = "receptor";
    private static final String VISTA_LIBRE = "Vista libre";
    private static final String CENTRO_MAPA = "centro del mapa";

    private InputManager inputManager;
    private Camera cam;

    private Vector3f focusPoint;
    private Vector3f offset = new Vector3f();

    private float rotationSpeed = FastMath.TWO_PI;

    private float zoomDistance = 15;
    private float zoomSpeed = 30;

    private float minZoom = 5;
    private float maxZoom = 30;

    private boolean invertY = false;

    private Vector3f direction = new Vector3f();
    private boolean rotate, translate;

    private final float[] angles = new float[3];
    private TerrenoState terrenoState;
    private Antena transmisor, receptor;
    private Container contenedorFocos;
    private ListBox selectorFocos;
    private VersionedReference refSelectorFocos;
    private TerrainQuad terreno;

    public CameraFocusState() {
    }

    private void registerInput() {
        inputManager.addMapping(TOGGLE_ROTATE, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping(TOGGLE_TRANSLATE, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        inputManager.addMapping(RESET_OFFSET, new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));

        inputManager.addMapping(MOUSE_MOVE_RIGHT, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping(MOUSE_MOVE_LEFT, new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping(MOUSE_MOVE_UP, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(MOUSE_MOVE_DOWN, new MouseAxisTrigger(MouseInput.AXIS_Y, false));

        inputManager.addMapping(ZOOM_IN, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(ZOOM_OUT, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        inputManager.addListener(this,
                MOUSE_MOVE_UP, MOUSE_MOVE_DOWN,
                MOUSE_MOVE_LEFT, MOUSE_MOVE_RIGHT,
                TOGGLE_ROTATE, TOGGLE_TRANSLATE,
                ZOOM_IN, ZOOM_OUT, RESET_OFFSET);
    }

    private void unregisterInput() {
        inputManager.deleteMapping(TOGGLE_ROTATE);
        inputManager.deleteMapping(TOGGLE_TRANSLATE);

        inputManager.deleteMapping(MOUSE_MOVE_RIGHT);
        inputManager.deleteMapping(MOUSE_MOVE_LEFT);
        inputManager.deleteMapping(MOUSE_MOVE_UP);
        inputManager.deleteMapping(MOUSE_MOVE_DOWN);

        inputManager.deleteMapping(ZOOM_IN);
        inputManager.deleteMapping(ZOOM_OUT);

        inputManager.removeListener(this);
    }

    /**
     * Returns the spatial that the camera is focused on.
     *
     * @return
     */
    public Vector3f getFocusPoint() {
        return focusPoint;
    }

    /**
     * Set the spatial to focus the camera on.
     *
     * @param focusPoint
     */
    public void setFocusPoint(Vector3f focusPoint) {
           this.focusPoint = focusPoint;
        
//        if (focusPoint != null) {
//            this.focusPoint = focusPoint;
//        }

        if (isInitialized()) {
            lookAt();
        }
    }

    public void setFocusPoint(Spatial focusPoint) {
        this.focusPoint = focusPoint.getWorldTranslation();
        if (focusPoint != null) {
            if (isInitialized()) {
                lookAt();
            }
        }
    }

    /**
     * Sets the rotation speed in radians.
     *
     * @return the speed of rotation in radians.
     */
    public float getRotationSpeed() {
        return rotationSpeed;
    }

    /**
     * Gets the rotation speed in radians.
     *
     * @param rotationSpeed the speed of rotation in radians.
     */
    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * Returns the current distance from the world location of the focused
     * spatial.
     *
     * @return the zoom distance.
     */
    public float getZoomDistance() {
        return zoomDistance;
    }

    /**
     * Sets the zoom distance from the focused spatial.
     *
     * @param zoomDistance the distance from the world location of the focused
     * spatial.
     */
    public void setZoomDistance(float zoomDistance) {
        this.zoomDistance = zoomDistance;
        lookAt();
    }

    /**
     * The speed in world units that the camera will zoom in and out.
     *
     * @return the speed in world units.
     */
    public float getZoomSpeed() {
        return zoomSpeed;
    }

    /**
     * Sets the speed of the zoom action in world units.
     *
     * @param zoomSpeed the speed in world units.
     */
    public void setZoomSpeed(float zoomSpeed) {
        this.zoomSpeed = zoomSpeed;
    }

    /**
     * The minimum distance the camera can zoom in.
     *
     * @return the minimum distance of zoom.
     */
    public float getMinZoom() {
        return minZoom;
    }

    /**
     * Sets the minimum distance the camera will zoom in to the spatial.
     *
     * @param minZoom the minimum zoom distance.
     */
    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
    }

    /**
     * The maximum distance the camera can zoom out.
     *
     * @return the maximum zoom distance.
     */
    public float getMaxZoom() {
        return maxZoom;
    }

    /**
     * Sets the maximum zoom distance the camera can zoom out.
     *
     * @param maxZoom the maximum zoom distance.
     */
    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
    }

    /**
     * Returns whether or not the Up/Down rotation is flipped.
     *
     * @return whether or not the up/down rotation is flipped.
     */
    public boolean isInvertY() {
        return invertY;
    }

    /**
     * Sets whether or not to flip the up/down rotation.
     *
     * @param invertY whether or not to flip the up/down rotation.
     */
    public void setInvertY(boolean invertY) {
        this.invertY = invertY;
    }

    /**
     * Gets the offset of the focus point.
     *
     * @return the offset of the focus point.
     */
    public Vector3f getOffset() {
        return offset;
    }

    /**
     * Sets the offset of the focus point.
     *
     * @param offset the offset of the focus point.
     */
    public void setOffset(Vector3f offset) {
        this.offset.set(offset);
    }

    /**
     * Sets the offset point to 0,0,0
     */
    public void resetOffset() {
        this.offset.set(0, 0, 0);
        lookAt();
    }

    @Override
    protected void initialize(Application app) {
        this.inputManager = app.getInputManager();
        this.cam = app.getCamera();

        transmisor = this.getApplication().getStateManager().getState(AntenaState.class).getTransmisor();
        receptor = this.getApplication().getStateManager().getState(AntenaState.class).getReceptor();
        terreno = this.getApplication().getStateManager().getState(TerrenoState.class).getTerrain();

        contenedorFocos = new Container();
       contenedorFocos.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.2f, 0.3f, 0.5f, 0.2f), 20, 20, 0.02f, false));
       
        contenedorFocos.addChild(new Label("Centro de Camara"));
        ArrayList listaFocos = new ArrayList(4);
        listaFocos.add(0, CameraFocusState.TRANSMISOR);
        listaFocos.add(1, CameraFocusState.RECEPTOR);
        listaFocos.add(2, CameraFocusState.CENTRO_MAPA);
        listaFocos.add(3, CameraFocusState.MED_LINEA_VISION);
        listaFocos.add(4, CameraFocusState.VISTA_LIBRE);
        
        selectorFocos = new ListBox(new VersionedList(listaFocos));
        selectorFocos.setSize(new Vector3f(100,150,2));
        refSelectorFocos = selectorFocos.getModel().createReference();
        contenedorFocos.setSize(new Vector3f(100,150,2));
        contenedorFocos.addChild(selectorFocos);

//this.guiNode.attachChild(contenedorCam);
     //   ((SimpleApplication) this.getApplication()).getGuiNode().attachChild(contenedorFocos);
    }

    @Override
    protected void cleanup(Application app) {
        ((SimpleApplication) this.getApplication()).getGuiNode().detachChild(contenedorFocos);

    }

    @Override
    protected void onEnable() {
        registerInput();
        setFocusPoint(((SimpleApplication) this.getApplication()).getRootNode());
        // if we set the focus point before the state was initialized, set the focus now.
        if (focusPoint != null) {
            lookAt();
        }

        System.out.println("FocusState is enable");
        ((SimpleApplication) this.getApplication()).getGuiNode().attachChild(contenedorFocos);

    }

    @Override
    protected void onDisable() {
        unregisterInput();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(TOGGLE_ROTATE)) {
            rotate = isPressed;
        } else if (name.equals(TOGGLE_TRANSLATE)) {
            translate = isPressed;
        } else if (name.equals(RESET_OFFSET) && isPressed) {
            resetOffset();
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {

        if (focusPoint == null) {
            return;
        }

        direction.set(cam.getDirection()).normalizeLocal();

        if (rotate) {

            if (MOUSE_MOVE_UP.equals(name) || MOUSE_MOVE_DOWN.equals(name)) {

                int dirState = MOUSE_MOVE_UP.equals(name) ? 1 : -1;

                if (invertY) {
                    dirState *= -1;
                }

                angles[0] += dirState * (rotationSpeed * tpf);

                // 89 degrees. Avoid the "flip" problem.
                float maxRotX = FastMath.HALF_PI - FastMath.DEG_TO_RAD;

                // limit camera rotation.
                if (angles[0] < -maxRotX) {
                    angles[0] = -maxRotX;
                }

                if (angles[0] > maxRotX) {
                    angles[0] = maxRotX;
                }
            }

            if (MOUSE_MOVE_RIGHT.equals(name) || MOUSE_MOVE_LEFT.equals(name)) {

                int dirState = MOUSE_MOVE_RIGHT.equals(name) ? 1 : -1;
                angles[1] += dirState * (rotationSpeed * tpf);

                // stop the angles from becoming too big.
                if (angles[1] > FastMath.TWO_PI) {
                    angles[1] -= FastMath.TWO_PI;
                } else if (angles[1] < -FastMath.TWO_PI) {
                    angles[1] += FastMath.TWO_PI;
                }
            }
            lookAt();
        }
        if (translate) {
            if (MOUSE_MOVE_UP.equals(name) || MOUSE_MOVE_DOWN.equals(name)) {

                int dirState = MOUSE_MOVE_UP.equals(name) ? 1 : -1;
                offset.addLocal(0, dirState * (30 * rotationSpeed * tpf), 0);
            }

            if (MOUSE_MOVE_RIGHT.equals(name) || MOUSE_MOVE_LEFT.equals(name)) {

                int dirState = MOUSE_MOVE_RIGHT.equals(name) ? 1 : -1;

                Vector3f left = rotation.mult(Vector3f.UNIT_X);
                offset.addLocal(left.mult(30 * dirState * (rotationSpeed * tpf)));
            }

            lookAt();
        }

        if (ZOOM_IN.equals(name)) {
            zoomDistance = Math.max(minZoom, zoomDistance - zoomSpeed);
            lookAt();
        } else if (ZOOM_OUT.equals(name)) {
            zoomDistance = Math.min(maxZoom, zoomDistance + zoomSpeed);
            lookAt();
        }
    }

    private Quaternion rotation = new Quaternion();

    private void lookAt() {

        if(focusPoint != null){ 
            rotation = new Quaternion().fromAngles(angles);

        Vector3f direction = rotation.mult(Vector3f.UNIT_Z);
        Vector3f loc = direction.mult(zoomDistance).add(focusPoint.add(offset));
        // cam.setLocation(loc);

        cam.lookAt(focusPoint.add(offset), Vector3f.UNIT_Y);
        }
    }

    @Override
    public void update(float tpf) {

    

        
     
            if (selectorFocos.getSelectedItem() == CameraFocusState.TRANSMISOR) {
                setFocusPoint(transmisor.getModelo().getWorldTranslation().clone());

            } else if (selectorFocos.getSelectedItem() == CameraFocusState.RECEPTOR) {
                setFocusPoint(receptor.getModelo().getWorldTranslation().clone());

            } else if (selectorFocos.getSelectedItem() == CameraFocusState.CENTRO_MAPA) {
                setFocusPoint(this.getApplication().getStateManager().getState(TerrenoState.class).getTerrain().clone());

            } else if (selectorFocos.getSelectedItem() == CameraFocusState.MED_LINEA_VISION) {
                setFocusPoint(transmisor.getModelo().getWorldTranslation().clone().interpolateLocal(receptor.getModelo().getWorldTranslation().clone(), 0.5f));
            }
             else if (selectorFocos.getSelectedItem() == CameraFocusState.VISTA_LIBRE) {
                 Vector3f nulo = null;
                setFocusPoint(nulo);
            }


        
    }
    
    public Container getContenedorFocos(){
    
    return contenedorFocos;
    
    }
}
