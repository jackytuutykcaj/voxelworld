package com.hysun;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryStack;

public class Mesh {
    private int vaoID;
    private int vboID;
    private float[] verticies;
    private int count;
    public Mesh(float[] vertices){
        this.verticies = vertices;
        this.count = vertices.length / 6;
        //create and bind vertex array object
        this.vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        //create and bind vertex buffer object
        this.vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);

        try(MemoryStack stack = stackPush()){
            FloatBuffer vertexBuffer = stack.mallocFloat(this.verticies.length);
            vertexBuffer.put(this.verticies).flip();
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        }

        //define how the shader should read the vertex buffer object data
        // position
        // 3 floats, stride is 6 floats total (xyz rgb), starts at index 0
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        // color
        // 3 floats stride is 6 floats total (xyz rgb), starts after the first 3 floats (xyz)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        //unbind the vertex buffer object and vertex array object
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void bind(){
        //bind the vertex array object
        glBindVertexArray(vaoID);
    }

    public void render(){
        glDrawArrays(GL_TRIANGLES, 0, count);
    }

    public float[] getTextureUVs(int x, int y, int cols, int rows){
        //calculate percentage of one texture
        float u_width = 1.0f / cols;
        float v_height = 1.0f / rows;

        //calculate the left and right U coordinates
        float u_left = x * u_width;
        float u_right = (x + 1) * u_width;

        //calculate the top and bottom V coordinates(inverted)
        float v_top = 1.0f - (y * v_height);
        float v_bottom = 1.0f - ((y + 1) * v_height);
        return new float[]{
            u_left,  v_bottom, // Bottom-Left Vertex
            u_right, v_bottom, // Bottom-Right Vertex
            u_right, v_top,    // Top-Right Vertex
            
            u_right, v_top,    // Top-Right Vertex
            u_left,  v_top,    // Top-Left Vertex
            u_left,  v_bottom  // Bottom-Left Vertex
        };
    }
}
