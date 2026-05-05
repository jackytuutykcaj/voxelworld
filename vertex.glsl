#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;

uniform mat4 u_projection;
uniform mat4 u_view;

out vec3 ourColor;

void main() {
    gl_Position = u_projection *  u_view * vec4(aPos.x, aPos.y, aPos.z, 1.0);
    ourColor = aColor;
};