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
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Manuel
 */
public class TerrenoState extends BaseAppState {

    private SimpleApplication simpleApp;
    private Node terrainNode;
    private BulletAppState bulletAppState;
    private String nombreHeightmap;
    private TerrainQuad terrainQuad;
    private AntenaState antenaState;
    private FresnelState fresnelState;
    private File mapaFile;
    private Image imagenMapa;
    private float alto, ancho, largo;

    private void cargarImagenMapa() throws IOException {

        mapaFile = (File) simpleApp.getStateManager().
                getState(ConfiguracionInicialState.class).getConfigGeneral().get("heightmap");

        byte[] bytesImagen = Files.readAllBytes(mapaFile.toPath());
        ByteBuffer data = ByteBuffer.wrap(bytesImagen);

        FileLocator locator = new FileLocator();
        System.out.println("path " + mapaFile.getParent());
        locator.setRootPath(mapaFile.getParent());
        simpleApp.getAssetManager().registerLocator(mapaFile.getParent(), FileLocator.class);

        // imagenMapa = new Image(Format.Alpha8,801,801,data,ColorSpace.Linear);
//        imagenMapa.addData(data);
//        imagenMapa.setFormat(Image.Format.ARGB8);
    }

    public void setNombreHeightmap(String s) {

        this.nombreHeightmap = s;

    }

    @Override
    protected void initialize(Application app) {
        System.out.println("iniciarterrenostate");

        simpleApp = (SimpleApplication) app;

        try {
            cargarImagenMapa();
        } catch (IOException ex) {
            Logger.getLogger(TerrenoState.class.getName()).log(Level.SEVERE, null, ex);
        }

        terrainNode = this.simpleApp.getRootNode();

//     bulletAppState = this.simpleApp.getStateManager().getState(BulletAppState.class);
        nombreHeightmap = "Textures/Terrain/splat/mountains512.png";

        ancho = (float) (simpleApp.getStateManager().
                getState(ConfiguracionInicialState.class).getConfigGeneral().get("medidasX"));

        alto = (float) (simpleApp.getStateManager().
                getState(ConfiguracionInicialState.class).getConfigGeneral().get("medidasY"));

        largo = (float) (simpleApp.getStateManager().
                getState(ConfiguracionInicialState.class).getConfigGeneral().get("medidasZ"));

        System.out.println("medidas " + ancho + " " + alto + " " + largo);
        terrainQuad = makeTerrain();
//       bulletAppState.getPhysicsSpace().add(terrainQuad);
        terrainQuad.setLocalScale(1f * ancho, (alto * 1000f) /255, 1f * largo);
        System.out.println("terrainQuad " +terrainQuad.getLocalTranslation() );
        terrainQuad.move(0, -2*terrainQuad.getHeight(new Vector2f(0, 0)), 0);
        terrainNode.attachChild(terrainQuad);
        System.out.println("terrainQuad " +terrainQuad.getLocalTranslation() );
        //Configurando el AntenaState
        antenaState = new AntenaState();
        antenaState.setTerreno(terrainQuad);
        simpleApp.getStateManager().attach(antenaState);
        fresnelState = antenaState.getFresnelState();

    }

    @Override
    protected void cleanup(Application app) {
        //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onEnable() {
        System.out.println("TerrenoState is enabled");
    }

    @Override
    protected void onDisable() {
        System.out.println("terrenoState is disabled");
    }

    private TerrainQuad makeTerrain() {
        //Uncomment for debugging.
        // bulletAppState.setDebugEnabled(true);

        //Material que define las texturas de cada nivel del mapa, 
        //independientemente de su topologia 
        
        Material mat_terrain = new Material(simpleApp.getAssetManager(),
                "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");
        Texture grass = simpleApp.getAssetManager().loadTexture(
                "Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("region1ColorMap", grass);
        Texture dirt = simpleApp.getAssetManager().loadTexture(
                "Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("region2ColorMap", dirt);
        Texture rock = simpleApp.getAssetManager().loadTexture(
                "Textures/Terrain/splat/road.jpg");
        rock.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("region3ColorMap", rock);
        mat_terrain.setFloat("terrainSize", 40000f);
        mat_terrain.setVector3("region1", new Vector3f(-500, 30, 25f));
        mat_terrain.setVector3("region2", new Vector3f(25, 3000, 25f));
        mat_terrain.setVector3("region3", new Vector3f(3000, 3100, 25f));

        /**
         * 2. Create the height map
         */
        AbstractHeightMap heightmap = null;

//        Texture heightMapImage = simpleApp.getAssetManager().loadTexture(
//                this.nombreHeightmap);
        Texture heightMapImage = simpleApp.getAssetManager().loadTexture(
                mapaFile.getName());
        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
        //       heightmap = new ImageBasedHeightMap(imagenMapa);
        heightmap.load();

        /**
         * 3. We have prepared material and heightmap. Now we create the actual
         * terrain: 3.1) Create a TerrainQuad and name it "my terrain". 3.2) A
         * good value for terrain tiles is 64x64 -- so we supply 64+1=65. 3.3)
         * We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
         * 3.4) As LOD step scale we supply Vector3f(1,1,1). 3.5) We supply the
         * prepared heightmap itself.
         */
        TerrainQuad terrain = new TerrainQuad("my terrain", 33, 1025, heightmap.getHeightMap());

        /**
         * 4. We give the terrain its material, position & scale it, and attach
         * it.
         */
        terrain.setMaterial(mat_terrain);
        //terrain.setLocalTranslation(0, -1, 0);
//        terrain.move(0, -terrain.getHeight(new Vector2f(0,0)), 0);
//        terrain.setLocalScale(1f * ancho,(alto*1000f)/255f, 1f *largo);

        /**
         * 5. The LOD (level of detail) depends on were the camera is:
         */
        List<Camera> cameras = new ArrayList<Camera>();
        cameras.add(this.simpleApp.getCamera());
        TerrainLodControl control = new TerrainLodControl(terrain, cameras);

        terrain.addControl(control);

        /**
         * 6. Add physics: We set up collision detection for the scene by
         * creating a static RigidBodyControl with mass zero.
         */
        terrain.addControl(new RigidBodyControl(0));
//    bulletAppState.getPhysicsSpace().add(terrain);

        //bulletAppState.getPhysicsSpace().add(fresnelSimulationState.getFresnel());    }
        return terrain;

    }

    public AntenaState getAntenaState() {

        return antenaState;
    }
    
    public void calibrarAltura(){
    
            terrainQuad.move(0, -1*terrainQuad.getHeight(new Vector2f(0, 0)), 0);

    
    }
    
    public TerrainQuad getTerrain() {
        return terrainQuad;

    }

}
