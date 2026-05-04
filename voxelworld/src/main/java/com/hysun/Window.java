package com.hysun;

import org.joml.Matrix4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {

    private long window;
    private int vaoID;
    private int vboID;
    Shader shader;
    private int width;
    private int height;
    Camera camera = new Camera();

    public Window(int width, int height){
        this.width = width;
        this.height = height;
    }

    public void run(){
        System.out.println("Starting...");
        init();
        loop();

        // cleanup
        glDeleteVertexArrays(vaoID);
        glDeleteBuffers(vboID);
        shader.cleanup();

        // Free the window and destroy window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        //terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init(){
        System.out.println("Init...");
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if(!glfwInit()){
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Window hints
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);//macOS

        //Create the window
        window = glfwCreateWindow(this.width, this.height, "Voxel World", NULL, NULL);
        if(window == NULL){
            throw new RuntimeException("Failed to create window");
        }

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) ->{
            camera.handleMouse(windowHandle, xpos, ypos);
        });

        //Resize window callback
        glfwSetFramebufferSizeCallback(window, (window, newWidth, newHeight) ->{
            glViewport(0, 0, newWidth, newHeight);
        });

        // Set esc key to close window
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->{
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE){
                glfwSetWindowShouldClose(window, true);
            }
        });

        try (MemoryStack stack = stackPush()){
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            //Center the window
            glfwSetWindowPos(window, (vidMode.width() - pWidth.get(0)) / 2, (vidMode.height() - pHeight.get(0)) / 2);
        }

        // Make the opengl context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        GL.createCapabilities();

        shader = new Shader(Shader.readFile("vertex.glsl"), Shader.readFile("fragment.glsl"));
    }

    public void loop(){
        System.out.println("rendering...");

        float[] vertices = {
            -0.5f, 0.5f, -3.0f,
            0.5f, 0.5f, -3.0f,
            -0.5f, -0.5f, -3.0f,
            0.5f, 0.5f, -3.0f,
            0.5f, -0.5f, -3.0f,
            -0.5f, -0.5f, -3.0f
        };

        //create and bind vertex arrray object
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        //create and bind vertex buffer object
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);

        try(MemoryStack stack = stackPush()){
            FloatBuffer vertexBuffer = stack.mallocFloat(vertices.length);
            vertexBuffer.put(vertices).flip();
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        }

        //define how the shader should  read the vertex buffer object data
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        //unbind the vertex buffer object and vertex array object
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //set the color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // rendering loop
        while(!glfwWindowShouldClose(window)){
            // Process WASD inputs
            camera.processInput(window);
            //clear frame
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            //tell opengl to use shader program
            shader.bind();

            //projection matrix
            camera.projectionMatrix(window, shader);
            
            //bind the vertex array object
            glBindVertexArray(vaoID);

            //draw the vertices
            glDrawArrays(GL_TRIANGLES, 0, 6);

            //unbind
            glBindVertexArray(0);
            glUseProgram(0);

            //swap the color buffers
            glfwSwapBuffers(window);

            //poll for window events
            glfwPollEvents();
        }

        shader.cleanup();
    }
    
}
