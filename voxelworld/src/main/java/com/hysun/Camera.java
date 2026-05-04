package com.hysun;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

public class Camera {
    private Vector3f cameraPos = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
    private Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

    private float deltaTime = 0.0f;
    private float lastFrame = 0.0f;

    private boolean firstMouse = true;
    private float yaw = -90;
    private float pitch = 0.0f;
    private float lastX = 450.0f;
    private float lastY = 300.0f;

    public Camera() {

    }

    public Matrix4f getViewMatrix() {
        Vector3f target = new Vector3f(cameraPos).add(cameraFront);
        return new Matrix4f().lookAt(cameraPos, target, cameraUp);
    }

    public void projectionMatrix(long window, Shader shader) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetWindowSize(window, w, h);

            int width = w.get(0);
            int height = h.get(0) > 0 ? h.get(0) : 1;

            float aspectRatio = (float) width / height;

            Matrix4f projection = new Matrix4f().identity();

            projection.setPerspective((float) Math.toRadians(60.0f), aspectRatio, 0.1f, 100.0f);
            int projLocation = glGetUniformLocation(shader.getShaderProgram(), "u_projection");

            FloatBuffer projBuffer = stack.mallocFloat(16);
            projection.get(projBuffer);
            glUniformMatrix4fv(projLocation, false, projBuffer);

            // view matrix
            Matrix4f view = getViewMatrix();
            int viewLocation = glGetUniformLocation(shader.getShaderProgram(), "u_view");
            FloatBuffer viewBuffer = stack.mallocFloat(16);
            view.get(viewBuffer);
            glUniformMatrix4fv(viewLocation, false, viewBuffer);
        }
    }

    public void handleMouse(long window, double xpos, double ypos){
        if(firstMouse){
            lastX = (float) xpos;
            lastY = (float) ypos;
            firstMouse = false;
        }

        float xoffset = (float) xpos - lastX;
        float yoffset = lastY - (float) ypos;
        lastX = (float) xpos;
        lastY = (float) ypos;

        float sensitivity = 0.1f;
        xoffset *= sensitivity;
        yoffset *= sensitivity;

        yaw += xoffset;
        pitch += yoffset;
        if(pitch > 89.0f) pitch = 89.0f;
        if(pitch < - 89.0f) pitch = -89.0f;

        Vector3f front = new Vector3f();
        front.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        cameraFront = front.normalize();
    }

    public void processInput(long window){
        float cameraSpeed = 2.5f * deltaTime;
        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS){
            cameraPos.add(new Vector3f(cameraFront).mul(cameraSpeed));
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            cameraPos.sub(new Vector3f(cameraFront).mul(cameraSpeed));
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            cameraPos.sub(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            cameraPos.add(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
        }
    }
}
