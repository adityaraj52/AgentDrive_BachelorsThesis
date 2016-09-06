package cz.agents.alite.protobuf;

import javax.vecmath.Point3f;

import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;


public class Util {
    
    //FIX check the transformations - hacked to work with SDAgent working only on the Y axis
    
    public static Vector3f highway2OpenDS(Point3f point3f) {        
        return new Vector3f((float)point3f.x, 0, (float)point3f.y);
    }
    public static Point3f openDS2Highway(Vector3f vec){
        return new Point3f( vec.x,vec.z,0f);
    }
    public static javax.vecmath.Vector3f openDS2Highway(float speed,Quaternion rotation){
        //TODO properly
        Matrix3f matrix = rotation.toRotationMatrix();
       // System.out.println(matrix);
        Vector3f vec = matrix.mult(new Vector3f(0, 0, -1));
      //  System.out.println(vec);
        javax.vecmath.Vector3f ret=  new javax.vecmath.Vector3f(vec.x,vec.z,0f);
        ret.normalize();
        ret.scale(speed);
        return ret;
    }
    
}
