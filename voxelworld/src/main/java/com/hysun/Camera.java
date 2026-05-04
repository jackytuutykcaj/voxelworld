package com.hysun;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f cameraPos = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
    private Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

    private float yaw = -90;
    private float pitch = 0.0f;

    public Camera(){

    }

    public Matrix4f getViewMatrix(){
        Vector3f target = new Vector3f(cameraPos).add(cameraFront);
        return new Matrix4f().lookAt(cameraPos, target, cameraUp);
    }
}
