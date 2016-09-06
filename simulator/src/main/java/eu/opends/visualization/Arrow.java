package eu.opends.visualization;

import com.jme3.scene.Node;

public class Arrow extends Node {
	
	@Override
	protected void updateWorldTransforms() {
		super.updateWorldTransforms();
		//unable to use parent's rotation
		//worldTransform.setRotation(new Quaternion());
	}
}
