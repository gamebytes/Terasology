package org.terasology.components;

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.QuaternionUtil;
import org.lwjgl.util.vector.Matrix4f;
import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Component represent the location and facing of an entity in the world
 * @author Immortius <immortius@gmail.com>
 */
public final class LocationComponent extends AbstractComponent {
    // Standard position/rotation
    private Vector3f position = new Vector3f();
    // TODO: Represent as Euler angles instead?
    private Quat4f rotation = new Quat4f(0,0,0,1);
    // TODO: Should this be here? Probably needs to be common as it affects several other components
    private float scale = 1.0f;

    // Relative to
    public EntityRef parent = null;

    /**
     * @return The position of this component relative to any parent. Can be directly modified to update the component
     */
    public Vector3f getLocalPosition() {
        return position;
    }

    public void setLocalPosition(Vector3f newPos) {
        position.set(newPos);
    }
    
    public Quat4f getLocalRotation() {
        return rotation;
    }
    
    public void setLocalRotation(Quat4f newQuat) {
        rotation.set(newQuat);
    }

    public void setLocalScale(float scale) {
        this.scale = scale;
    }

    public float getLocalScale() {
        return scale;
    }

    public Vector3f getWorldPosition() {
        return getWorldPosition(new Vector3f());
    }

    public Vector3f getWorldPosition(Vector3f output) {
        output.set(position);
        LocationComponent parentLoc = getParentLocationComponent();
        while (parentLoc != null) {
            output.scale(parentLoc.scale);
            QuaternionUtil.quatRotate(parentLoc.getLocalRotation(), output, output);
            output.add(parentLoc.position);
            parentLoc = parentLoc.getParentLocationComponent();
        }
        return output;
    }
    
    public Quat4f getWorldRotation() {
        return getWorldRotation(new Quat4f(0,0,0,1));
    }
    
    public Quat4f getWorldRotation(Quat4f output) {
        output.set(rotation);
        LocationComponent parentLoc = getParentLocationComponent();
        while (parentLoc != null) {
            output.mul(parentLoc.rotation, output);
            parentLoc = parentLoc.getParentLocationComponent();
        }
        return output;
    }

    public float getWorldScale() {
        float result = scale;
        LocationComponent parentLoc = getParentLocationComponent();
        while (parentLoc != null) {
            result *= parentLoc.getLocalScale();
            parentLoc = parentLoc.getParentLocationComponent();
        }
        return result;
    }
    
    public void setWorldPosition(Vector3f position) {
        this.position.set(position);
        LocationComponent parentLoc = getParentLocationComponent();
        if (parentLoc != null) {
            this.position.sub(parentLoc.getWorldPosition());
            this.position.scale(1f / parentLoc.getWorldScale());
            Quat4f rot = new Quat4f(0,0,0,1);
            rot.inverse(parentLoc.getWorldRotation());
            QuaternionUtil.quatRotate(rot, this.position, this.position);
        }
    }
    
    public void setWorldRotation(Quat4f rotation) {
        this.rotation.set(rotation);
        LocationComponent parentLoc = getParentLocationComponent();
        if (parentLoc != null) {
            Quat4f worldRot = parentLoc.getWorldRotation();
            worldRot.inverse();
            this.rotation.mul(worldRot, this.rotation);
        }
    }

    public void setWorldScale(float scale) {
        this.scale = scale;
        LocationComponent parentLoc = getParentLocationComponent();
        if (parentLoc != null) {
            this.scale /= parentLoc.getWorldScale();
        }
    }

    private LocationComponent getParentLocationComponent() {
        if (parent != null) {
            return parent.getComponent(LocationComponent.class);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationComponent component = (LocationComponent) o;

        if (Float.compare(component.scale, scale) != 0) return false;
        if (parent != null ? !parent.equals(component.parent) : component.parent != null) return false;
        if (position != null ? !position.equals(component.position) : component.position != null) return false;
        if (rotation != null ? !rotation.equals(component.rotation) : component.rotation != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = position != null ? position.hashCode() : 0;
        result = 31 * result + (rotation != null ? rotation.hashCode() : 0);
        result = 31 * result + (scale != +0.0f ? Float.floatToIntBits(scale) : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }
}