package eu.opends.environment;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;

import eu.opends.basics.SimulationBasics;

public class TerrainLoader 
{
	private Material matTerrain;
	
	
	public TerrainLoader(SimulationBasics sim) 
	{
		boolean isDebug = false;
		float heightScale = 0.2f;
		float smoothPercentage = 0.9f;
		int smoothRadius = 1;
		int patchSize = 65;
		int totalSize = 513;
		float lodFactor = 2.7f;
		String materialPath = "Materials/MyTerrain2.j3m";
		String heightMapImagePath = "Textures/Terrain/splat/mountains512.png";
		Vector3f translation = new Vector3f(0, 100, 0);
		Quaternion rotation = new Quaternion();
		Vector3f scale = new Vector3f(2f, 1f, 2f);
		
		
		AssetManager assetManager = sim.getAssetManager();
		
		// load terrain texture material
		matTerrain = assetManager.loadMaterial(materialPath);
		     
		// create heightmap
		Texture heightMapImage = assetManager.loadTexture(heightMapImagePath);
		AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), heightScale);
		heightmap.load();
		heightmap.smooth(smoothPercentage, smoothRadius);
		
		// create terrain
		// The tiles will be 65x65, and the total size of the terrain will be 513x513.
		// Optimal terrain patch size is 65 (64x64). The total size is up to you. At 1025 
		// it ran fine for me (200+FPS), however at size=2049, it got really slow. But that 
		// is a jump from 2 million to 8 million triangles...
		TerrainQuad terrain = new TerrainQuad("terrain", patchSize, totalSize, heightmap.getHeightMap());
		TerrainLodControl control = new TerrainLodControl(terrain, sim.getCamera());
		control.setLodCalculator(new DistanceLodCalculator(patchSize, lodFactor) );
		terrain.addControl(control);
		terrain.setMaterial(matTerrain);
		terrain.setModelBound(new BoundingBox());
		terrain.updateModelBound();
		
		// set translation, rotation, scale
		terrain.setLocalTranslation(translation);
		terrain.setLocalRotation(rotation);
		terrain.setLocalScale(scale);
		
		// attach terrain to root node
		sim.getRootNode().attachChild(terrain);
		
		// add terrain to physics node
		terrain.addControl(new RigidBodyControl(0));
		sim.getBulletAppState().getPhysicsSpace().add(terrain);
		
		// enable debug mode
		if(isDebug)
		{
			Material debugMat = assetManager.loadMaterial("Common/Materials/VertexColor.j3m");
			terrain.generateDebugTangents(debugMat);
		}
	}
	
	
	public void setWireframe(boolean isOn)
	{
		matTerrain.getAdditionalRenderState().setWireframe(isOn);
	}
    
    
/*
	BUILD material (instead of loading)

	matTerrain = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
    matTerrain.setBoolean("useTriPlanarMapping", false);
    matTerrain.setFloat("Shininess", 0.0f);

    // ALPHA map (for splat textures)
    matTerrain.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alpha1.png"));
    matTerrain.setTexture("AlphaMap_1", assetManager.loadTexture("Textures/Terrain/splat/alpha2.png"));
    // this material also supports 'AlphaMap_2', so you can get up to 12 diffuse textures
           
    // DIRT texture, Diffuse textures 0 to 3 use the first AlphaMap
    Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
    dirt.setWrap(WrapMode.Repeat);
    matTerrain.setTexture("DiffuseMap", dirt);
    matTerrain.setFloat("DiffuseMap_0_scale", dirtScale);
    
    // GRASS texture
    Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
    grass.setWrap(WrapMode.Repeat);
    matTerrain.setTexture("DiffuseMap_1", grass);
    matTerrain.setFloat("DiffuseMap_1_scale", grassScale);

    // ROCK texture
    Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
    rock.setWrap(WrapMode.Repeat);
    matTerrain.setTexture("DiffuseMap_2", rock);
    matTerrain.setFloat("DiffuseMap_2_scale", rockScale);

    // BRICK texture
    Texture brick = assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg");
    brick.setWrap(WrapMode.Repeat);
    matTerrain.setTexture("DiffuseMap_3", brick);
    matTerrain.setFloat("DiffuseMap_3_scale", rockScale);

    // RIVER ROCK texture, this texture will use the next alphaMap: AlphaMap_1
    Texture riverRock = assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg");
    riverRock.setWrap(WrapMode.Repeat);
    matTerrain.setTexture("DiffuseMap_4", riverRock);
    matTerrain.setFloat("DiffuseMap_4_scale", rockScale);
    
    // diffuse textures 4 to 7 use AlphaMap_1
    // diffuse textures 8 to 11 use AlphaMap_2

    Texture normalMap0 = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg");
    normalMap0.setWrap(WrapMode.Repeat);
    Texture normalMap1 = assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png");
    normalMap1.setWrap(WrapMode.Repeat);
    Texture normalMap2 = assetManager.loadTexture("Textures/Terrain/splat/road_normal.png");
    normalMap2.setWrap(WrapMode.Repeat);
    //matTerrain.setTexture("NormalMap", normalMap0);
    matTerrain.setTexture("NormalMap_1", normalMap2);
    matTerrain.setTexture("NormalMap_2", normalMap2);
    matTerrain.setTexture("NormalMap_4", normalMap2);
*/   
}
