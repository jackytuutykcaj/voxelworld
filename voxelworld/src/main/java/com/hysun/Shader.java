package com.hysun;

import static org.lwjgl.opengl.GL33.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Shader {
    private int shaderProgram;

    public Shader(String vertexCode, String fragmentCode){
        int vertexShader = createShader(vertexCode, GL_VERTEX_SHADER);
        int fragmentShader = createShader(fragmentCode, GL_FRAGMENT_SHADER);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        if(glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE){
            System.err.println("Shader Linking Failed: " + glGetProgramInfoLog(shaderProgram));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int createShader(String shaderCode, int shaderType){
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);
        if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE){
            System.err.println("Shader Compilation Failed: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public void bind(){
        glUseProgram(shaderProgram);
    }

    public int getShaderProgram(){
        return shaderProgram;
    }

    public void cleanup(){
        glDeleteProgram(shaderProgram);
    }

    public static String readFile(String filepath){
        try{
            return Files.readString(Paths.get(filepath));
        }catch (IOException e){
            throw new RuntimeException("Failed to load shader file: " + filepath);
        }
    }
}
