package com.rts.game.core;

import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import com.rts.game.entities.*;
import com.rts.game.systems.*;

import java.nio.*;

import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    private float cameraX, cameraY;
    private int screenWidth, screenHeight;
    
    // VAO and VBO
    private int vaoId;
    private int vboId;
    
    // Simple shader program (color only, no textures)
    private int shaderProgram;
    
    public Renderer() {
        // Create VAO
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        
        // Create VBO
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        
        // Setup vertex attributes (position + color)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 5 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 5 * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        
        // Create simple shader program
        shaderProgram = createShaderProgram();
    }
    
    private int createShaderProgram() {
        String vertexShaderSource = 
            "#version 330 core\n" +
            "layout (location = 0) in vec2 aPos;\n" +
            "layout (location = 1) in vec3 aColor;\n" +
            "out vec3 ourColor;\n" +
            "uniform vec2 uOffset;\n" +
            "uniform vec2 uScreenSize;\n" +
            "void main() {\n" +
            "    vec2 pos = (aPos + uOffset) / (uScreenSize / 2.0) - 1.0;\n" +
            "    gl_Position = vec4(pos.x, -pos.y, 1.0, 1.0);\n" +
            "    ourColor = aColor;\n" +
            "}\0";
            
        String fragmentShaderSource = 
            "#version 330 core\n" +
            "in vec3 ourColor;\n" +
            "out vec4 FragColor;\n" +
            "void main() {\n" +
            "    FragColor = vec4(ourColor, 1.0);\n" +
            "}\0";
        
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        checkShaderCompile(vertexShader);
        
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        checkShaderCompile(fragmentShader);
        
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        checkShaderLink(program);
        
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        
        return program;
    }
    
    private void checkShaderCompile(int shaderId) {
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Shader compile error: " + glGetShaderInfoLog(shaderId, 1024));
        }
    }
    
    private void checkShaderLink(int programId) {
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Shader link error: " + glGetProgramInfoLog(programId, 1024));
        }
    }
    
    public void beginBatch(float cameraX, float cameraY, int screenWidth, int screenHeight) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        glUseProgram(shaderProgram);
        glUniform2f(glGetUniformLocation(shaderProgram, "uOffset"), -cameraX, -cameraY);
        glUniform2f(glGetUniformLocation(shaderProgram, "uScreenSize"), screenWidth, screenHeight);
    }
    
    public void endBatch() {
        glDisable(GL_BLEND);
        glUseProgram(0);
    }
    
    public void renderMapBackground(GameMap gameMap) {
        // Draw map boundary
        drawRectangle(0, 0, gameMap.getWidth(), gameMap.getHeight(), new float[]{0.15f, 0.2f, 0.15f}, false);
    }
    
    public void renderEntity(Entity entity) {
        float x = entity.getX();
        float y = entity.getY();
        
        EntityType type = entity.getType();
        EntityOwner owner = entity.getOwner();
        
        float[] color = getColorForEntity(type, owner);
        
        if (type.isBuilding()) {
            drawBuilding(x, y, type.getWidth(), type.getHeight(), color, entity.getBuildProgress());
        } else {
            drawUnit(x, y, type.getWidth(), type.getHeight(), color, entity.isSelected());
        }
        
        drawHealthBar(x, y - 10, type.getWidth(), entity.getHealth(), entity.getMaxHealth());
    }
    
    private float[] getColorForEntity(EntityType type, EntityOwner owner) {
        switch (owner) {
            case PLAYER:
                switch (type) {
                    case COMMAND_CENTER: return new float[]{0.2f, 0.4f, 0.8f};
                    case BARRACKS: return new float[]{0.3f, 0.5f, 0.9f};
                    case REFINERY: return new float[]{0.4f, 0.6f, 0.7f};
                    case SUPPLY_DEPOT: return new float[]{0.5f, 0.5f, 0.8f};
                    case SCV: return new float[]{0.3f, 0.6f, 1.0f};
                    case MARINE: return new float[]{0.2f, 0.5f, 0.9f};
                    case TANK: return new float[]{0.1f, 0.4f, 0.8f};
                    default: return new float[]{0.3f, 0.5f, 0.9f};
                }
            case AI:
                switch (type) {
                    case COMMAND_CENTER: return new float[]{0.8f, 0.2f, 0.2f};
                    case BARRACKS: return new float[]{0.9f, 0.3f, 0.3f};
                    case REFINERY: return new float[]{0.7f, 0.4f, 0.4f};
                    case SUPPLY_DEPOT: return new float[]{0.8f, 0.5f, 0.5f};
                    case SCV: return new float[]{1.0f, 0.3f, 0.3f};
                    case MARINE: return new float[]{0.9f, 0.2f, 0.2f};
                    case TANK: return new float[]{0.8f, 0.1f, 0.1f};
                    default: return new float[]{0.9f, 0.3f, 0.3f};
                }
            case NEUTRAL:
                switch (type) {
                    case MINERAL_PATCH: return new float[]{0.3f, 0.8f, 0.9f};
                    case VESPENE_GEYSER: return new float[]{0.2f, 0.7f, 0.3f};
                    default: return new float[]{0.5f, 0.5f, 0.5f};
                }
            default:
                return new float[]{0.5f, 0.5f, 0.5f};
        }
    }
    
    private void drawBuilding(float x, float y, int width, int height, float[] color, float buildProgress) {
        drawRectangle(x, y, width, height, color, true);
        
        if (buildProgress < 1.0f) {
            int buildHeight = (int)(height * buildProgress);
            drawRectangle(x, y + height - buildHeight, width, buildHeight, 
                         new float[]{0.5f, 0.5f, 0.5f}, true);
        }
    }
    
    private void drawUnit(float x, float y, int width, int height, float[] color, boolean selected) {
        drawCircle(x + width/2, y + height/2, width/2, color);
        
        if (selected) {
            drawCircleOutline(x + width/2, y + height/2, width/2 + 3, new float[]{0.0f, 1.0f, 0.0f});
        }
    }
    
    private void drawHealthBar(float x, float y, int width, int health, int maxHealth) {
        float healthPercent = (float)health / maxHealth;
        drawRectangle(x, y, width, 4, new float[]{0.8f, 0.0f, 0.0f}, true);
        drawRectangle(x, y, (int)(width * healthPercent), 4, new float[]{0.0f, 0.8f, 0.0f}, true);
    }
    
    public void renderSelectionBox(float[] start, float[] end, float cameraX, float cameraY) {
        float x1 = start[0] + cameraX;
        float y1 = start[1] + cameraY;
        float x2 = end[0] + cameraX;
        float y2 = end[1] + cameraY;
        
        float minX = Math.min(x1, x2);
        float minY = Math.min(y1, y2);
        float width = Math.abs(x2 - x1);
        float height = Math.abs(y2 - y1);
        
        drawRectangle(minX, minY, (int)width, (int)height, new float[]{0.0f, 1.0f, 0.0f, 0.3f}, true);
        drawRectangleOutline(minX, minY, (int)width, (int)height, new float[]{0.0f, 1.0f, 0.0f});
    }
    
    public void renderUI(GameState gameState, float cameraX, float cameraY) {
        // UI rendering would require a font system - simplified for now
    }
    
    // Drawing primitives using modern OpenGL
    private void drawRectangle(float x, float y, int width, int height, float[] color, boolean filled) {
        if (color.length == 3) {
            drawRectangleFilled(x, y, width, height, color);
        } else {
            drawRectangleFilled(x, y, width, height, color[0], color[1], color[2], color[3]);
        }
    }
    
    private void drawRectangleFilled(float x, float y, int width, int height, float[] rgb) {
        drawRectangleFilled(x, y, width, height, rgb[0], rgb[1], rgb[2], 1.0f);
    }
    
    private void drawRectangleFilled(float x, float y, int width, int height, float r, float g, float b, float a) {
        float[] vertices = {
            x, y, r, g, b,
            x + width, y, r, g, b,
            x + width, y + height, r, g, b,
            x, y, r, g, b,
            x + width, y + height, r, g, b,
            x, y + height, r, g, b
        };
        
        drawVertices(vertices, 6);
    }
    
    private void drawRectangleOutline(float x, float y, int width, int height, float[] color) {
        float[] vertices = {
            x, y, color[0], color[1], color[2],
            x + width, y, color[0], color[1], color[2],
            x + width, y + height, color[0], color[1], color[2],
            x, y + height, color[0], color[1], color[2]
        };
        
        // Draw as line loop (4 lines)
        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINE_LOOP, 0, 4);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    private void drawCircle(float centerX, float centerY, float radius, float[] color) {
        int segments = 16;
        float[] vertices = new float[(segments + 2) * 5];
        
        // Center vertex
        vertices[0] = centerX;
        vertices[1] = centerY;
        vertices[2] = color[0];
        vertices[3] = color[1];
        vertices[4] = color[2];
        
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            int idx = (i + 1) * 5;
            vertices[idx] = centerX + (float)(Math.cos(angle) * radius);
            vertices[idx + 1] = centerY + (float)(Math.sin(angle) * radius);
            vertices[idx + 2] = color[0];
            vertices[idx + 3] = color[1];
            vertices[idx + 4] = color[2];
        }
        
        drawVertices(vertices, segments + 2);
    }
    
    private void drawCircleOutline(float centerX, float centerY, float radius, float[] color) {
        int segments = 16;
        float[] vertices = new float[segments * 5];
        
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            int idx = i * 5;
            vertices[idx] = centerX + (float)(Math.cos(angle) * radius);
            vertices[idx + 1] = centerY + (float)(Math.sin(angle) * radius);
            vertices[idx + 2] = color[0];
            vertices[idx + 3] = color[1];
            vertices[idx + 4] = color[2];
        }
        
        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINE_LOOP, 0, segments);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    private void drawVertices(float[] vertices, int vertexCount) {
        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
}
