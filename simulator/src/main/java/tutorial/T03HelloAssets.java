/*
 * Copyright (c) 2009-2012 jMonkeyEngine
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

package tutorial;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.util.SkyFactory;


/** Sample 3 - how to load an OBJ model, and OgreXML model, 
 * a material/texture, or text. */
public class T03HelloAssets extends SimpleApplication {

    public static void main(String[] args) {
        T03HelloAssets app = new T03HelloAssets();
        app.start();
    }

    @Override
    public void simpleInitApp() {

    	assetManager.registerLocator("assets", FileLocator.class);
    	
    	
        /** Load a teapot model (OBJ file from test-data) */
        Spatial teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
        Material mat_default = new Material( assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        teapot.setMaterial(mat_default);
        rootNode.attachChild(teapot);

        
        /** Create a wall (Box with material and texture from test-data) */
        Box box = new Box(2.5f,2.5f,1.0f);
        Spatial wall = new Geometry("Box", box );
        Material mat_brick = new Material( assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_brick.setTexture("ColorMap", assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
        wall.setMaterial(mat_brick);
        wall.setLocalTranslation(2.0f,-2.5f,0.0f);
        rootNode.attachChild(wall);

        
        /** Load a Ninja model (OgreXML + material + texture from test_data) */
        Spatial body = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
        
        /** TODO scale, rotate and set position of the body */

        body.scale(0.05f);
        body.rotate(0, 180*FastMath.DEG_TO_RAD, 0);
        body.move(0.0f, -5.0f, -2.0f);
        
        rootNode.attachChild(body);
        
        
        /** You must add a light to make the model visible */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f).normalizeLocal());
        rootNode.addLight(sun);

        
        /** TODO Display a line of text and move it to the center of the screen */
       
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText helloText = new BitmapText(guiFont, false);
        helloText.setSize(guiFont.getCharSet().getRenderedSize());
        helloText.setText("Hello World in the center");
        helloText.setLocalTranslation(300, helloText.getLineHeight(), 0);
        helloText.move(0,200,0);
        guiNode.attachChild(helloText);
        

        
        /** TODO Load town model, scale it and set its position */

        Spatial town = assetManager.loadModel("Scenes/town/main.scene");
        town.move(0,-5.2f,0);
        town.scale(2f);
        rootNode.attachChild(town);
        
        
        /** TODO Set up a sky texture */
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/mountain.dds", false));
        
        
        
        /** TODO Add a red point light */

        PointLight point = new PointLight();
        point.setColor(ColorRGBA.Red);
        point.setRadius(100f);
        point.setPosition(new Vector3f(0f,0.0f,0.0f));
       rootNode.addLight(point);
        
        
        
    }
}
