package eu.opends.visualization;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;

import eu.opends.main.Simulator;

public class MaterialProvider {
	
	private static MaterialProvider instance;
	
	private Simulator vis;
	private Material lightMat;
	private Material unshadedMat;
	private Material texturedMat;
	
	private MaterialProvider(Simulator vis) {
		this.vis = vis;
	}
	
	private Material getLightMaterial(){
		if(lightMat == null){
			lightMat    = new Material(vis.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
		}
		return lightMat;
	}
	
	private Material getUnshadedMaterial(){
		if(unshadedMat == null){
			unshadedMat = new Material(vis.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		}
		return unshadedMat;
	}
	
	private Material getTexturedMaterial(){
		if(texturedMat == null){
			texturedMat = new Material(vis.getAssetManager(), "Common/MatDefs/Misc/SimpleTextured.j3md");
		}
		return texturedMat;
	}
	
	public Material getTransparentMaterial(int r, int g, int b, float a, MaterialType materialType){
		ColorRGBA color = new ColorRGBA(r / 255f, g / 255f, b / 255f, a);
		Material material;
		switch (materialType) {
		case LIGHT:
			material = getLightMaterial().clone();
			material.setBoolean("UseMaterialColors", true);
			material.setBoolean("UseAlpha", true);
			material.setColor("Diffuse", color);
			break;
		case UNSHADED:
		default:
			material = getUnshadedMaterial().clone();
			material.setColor("Color", color);
			break;
		}
	    material.setTransparent(true);
	    material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        material.setReceivesShadows(false);
	    return material;
	}
	
	public Material getTexturedMaterial(Texture texture){
		Material material = getTexturedMaterial().clone();
		material.setTexture("ColorMap", texture);
		return material;
	}
	
	public Material getSolidMaterial(int r, int g, int b, MaterialType materialType){
		switch(materialType){
		case LIGHT:
			return getSolidLightMaterial(r, g, b);
		case UNSHADED:
			return getSolidUnshadedMaterial(r, g, b);
		default:
			return getSolidUnshadedMaterial(r, g, b);
		}
	}
	
	public Material getSolidUnshadedMaterial(int r, int g, int b) {
		Material material = getUnshadedMaterial().clone();
		ColorRGBA color = new ColorRGBA(r / 255f, g / 255f, b / 255f, 1f);
	    material.setColor("Color", color);
	    return material;
	}

	public Material getSolidLightMaterial(int r, int g, int b) {
		Material material = getLightMaterial().clone();
		material.setBoolean("UseMaterialColors", true);
	    material.setColor("Diffuse", new ColorRGBA(r / 255f, g / 255f, b / 255f, 1f));
	    material.setColor("Ambient", new ColorRGBA(0.3f, 0.3f, 0.3f, 1f));
	    return material;
	}

	public static MaterialProvider getInstance(Simulator vis) {
		if(instance == null || instance.vis == null || !vis.equals(instance.vis)){
			instance = new MaterialProvider(vis);
		}
		return instance;
	}
	
	public enum MaterialType{
		UNSHADED, LIGHT
	}

}
