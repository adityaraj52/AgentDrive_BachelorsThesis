package eu.opends.visualization;

import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import eu.opends.main.Simulator;
import eu.opends.visualization.MaterialProvider.MaterialType;

public class ArrowProvider{

    private static final String MODEL_ARROW       = "Models/arrow/2D_arrow.mesh.xml";
    private static final Quaternion MODEL_LOAD_ROTATION = new Quaternion().fromAngles( -FastMath.HALF_PI, 0, 0);
//    private static final Quaternion MODEL_LOAD_ROTATION = new Quaternion().fromAngles( 0, FastMath.PI, 0);

    private static final Vector3f MODEL_SCALE = new Vector3f(1, 1, 1);
    private static final float ALPHA = 0.3f;

    private static ArrowProvider instance;

    private Node referentialArrow;

    private Simulator sim;

    private ArrowProvider(Simulator vis){
        this.sim = vis;
    }

    private void init() {
        referentialArrow = new Arrow();
        Spatial underNeath = sim.getAssetManager().loadModel(MODEL_ARROW);
        underNeath.rotate(MODEL_LOAD_ROTATION);
        underNeath.scale(MODEL_SCALE.x, MODEL_SCALE.y, MODEL_SCALE.z);
       // underNeath.move(0,0,MODEL_SCALE.z*10);
        referentialArrow.attachChild(underNeath);
    }

    private Spatial getReferentialArrow(){
        if(referentialArrow == null){
            init();
        }
        return referentialArrow;
    }

    public Spatial getNext(int r, int g, int b, boolean visibility){
        Spatial ret = getReferentialArrow().deepClone();
        Material trMat = MaterialProvider.getInstance(sim).getTransparentMaterial(r, g, b, ALPHA, MaterialType.UNSHADED);
        ret.setMaterial(trMat);
        ret.setQueueBucket(Bucket.Transparent);

        return ret;
    }

    public static ArrowProvider getInstance(Simulator vis){
        if(instance == null || instance.sim == null || !instance.sim.equals(vis)){
            instance = new ArrowProvider(vis);
        }
        return instance;
    }

}
