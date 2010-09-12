/*******************************************************************************
 * Copyright 2010 fredgrott
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.mobilebytes.drwholivewallpaper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

// TODO: Auto-generated Javadoc
/**
 * The Class Tunnel3D.
 */
public class Tunnel3D {

    /** The vertices. */
    private final float vertices[];

    /** The faces. */
    private final short faces[];

    /** The colors. */
    private final byte  colors[];

    /** The texture. */
    private final float texture[];

    /** The vertices_direct. */
    private ByteBuffer  vertices_direct;

    /** The texture_direct. */
    private ByteBuffer  texture_direct;

    /** The vertices_buffer. */
    private FloatBuffer vertices_buffer;

    /** The faces_direct. */
    private ByteBuffer  faces_direct;

    /** The faces_buffer. */
    private ShortBuffer faces_buffer;

    /** The colors_buffer. */
    private ByteBuffer  colors_buffer;

    /** The texture_buffer. */
    private FloatBuffer texture_buffer;

    /** The nv. */
    private final int   nx, ny, nv;

    /** The start_a. */
    private double      start_a;

    /** The start_v. */
    private float       start_v;

    /** The py. */
    private float       px, py;


    /**
     * Instantiates a new tunnel3 d.
     *
     * @param revolution the revolution
     * @param depth the depth
     */
    public Tunnel3D(int revolution, int depth) {
        start_a = 0;
        start_v = 0;

        // Calculate number of vertices...
        nx = revolution;
        ny = depth;
        nv = nx * ny;

        // Allocate arrays...
        colors = new byte[nv * 3];
        vertices = new float[nv * 3];
        faces = new short[((nx + 1) * (ny - 1)) << 1];
        texture = new float[nv * 2];

        // Generate object data...
        genVertex();
        genFaces();
        genColors();
        genTexture();

        // Build direct buffer objects...
        buildBuffers();

        // Blit data...
        fillVertex();
        fillFaces();
        fillColors();
        fillTexture();
    }

    //
    // Create direct buffers objects...
    //
    /**
     * Builds the buffers.
     */
    private void buildBuffers() {
        vertices_direct = ByteBuffer.allocateDirect(vertices.length
                * (Float.SIZE >> 3));
        vertices_direct.order(ByteOrder.nativeOrder());
        vertices_buffer = vertices_direct.asFloatBuffer();

        faces_direct = ByteBuffer.allocateDirect(faces.length
                * (Short.SIZE >> 3));
        faces_direct.order(ByteOrder.nativeOrder());
        faces_buffer = faces_direct.asShortBuffer();

        colors_buffer = ByteBuffer.allocateDirect(colors.length);

        texture_direct = ByteBuffer.allocateDirect(texture.length
                * (Float.SIZE >> 3));
        texture_direct.order(ByteOrder.nativeOrder());
        texture_buffer = texture_direct.asFloatBuffer();
    }

    //
    // Blit colors data in garbage memory into direct memory heap (buffers)...
    //
    /**
     * Fill colors.
     */
    private void fillColors() {
        colors_buffer.clear();
        colors_buffer.put(colors);
        colors_buffer.position(0);
    }

    //
    // Blit faces data in garbage memory into direct memory heap (buffers)...
    //
    /**
     * Fill faces.
     */
    private void fillFaces() {
        faces_buffer.clear();
        faces_buffer.put(faces);
        faces_buffer.position(0);
    }

    //
    // Blit colors data in garbage memory into direct memory heap (buffers)...
    //
    /**
     * Fill texture.
     */
    private void fillTexture() {
        texture_buffer.clear();
        texture_buffer.put(texture);
        texture_buffer.position(0);
    }

    //
    // Blit vertex data in garbage memory into direct memory heap (buffers)...
    //
    /**
     * Fill vertex.
     */
    private void fillVertex() {
        vertices_buffer.clear();
        vertices_buffer.put(vertices);
        vertices_buffer.position(0);
    }

    //
    // Generate colors data (RGB) of the tunnel...
    //
    /**
     * Gen colors.
     */
    private void genColors() {
        int i = 0;
        float sy = 1.0f;
        float dy = 1.0f / ny;
        float dx = 1.0f / nx;
        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                int r_ci = (int) (sy * 255.0f);
                byte r_cb = (byte) ((r_ci > 128) ? (r_ci - 256) : r_ci);

                int g_ci = (int) (sy * 255.0f);
                byte g_cb = (byte) ((g_ci > 128) ? (g_ci - 256) : g_ci);

                int b_ci = (int) (sy * 255.0f);
                byte b_cb = (byte) ((b_ci > 128) ? (b_ci - 256) : b_ci);

                colors[i + 0] = r_cb;
                colors[i + 1] = g_cb;
                colors[i + 2] = b_cb;
                i += 3;
            }
            sy -= dy;
        }
    }

    //
    // Generate faces data (index) of the tunnel...
    //
    /**
     * Gen faces.
     */
    private void genFaces() {
        int i = 0;
        int dy = 0;
        for (int y = 0; y < (ny - 1); y++) {
            for (int x = 0; x < nx; x++) {
                faces[i + 0] = (short) (x + dy);
                faces[i + 1] = (short) (x + dy + nx);
                i += 2;
            }
            faces[i + 0] = (short) dy;
            faces[i + 1] = (short) (dy + nx);
            i += 2;
            dy += nx;
        }
    }

    //
    // Generate texture data (UV) for the tunnel...
    //
    /**
     * Gen texture.
     */
    private void genTexture() {
        int i = 0;
        float delta_x = 1.0f / nx;
        float delta_y = 1.0f / ny;

        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                texture[i + 0] = x * delta_x;
                texture[i + 1] = y * delta_y;
                i += 2;
            }
        }
    }

    //
    // Generate vertex data (XYZ) for the tunnel...
    //
    /**
     * Gen vertex.
     */
    private void genVertex() {
        int i = 0;
        double delta_x = 360.0 / nx;
        double delta_y = 1.0;

        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                vertices[i + 0] = (float) Math.sin(Math.toRadians(x * delta_x));
                vertices[i + 1] = (float) Math.cos(Math.toRadians(x * delta_x));
                vertices[i + 2] = (float) -(y * delta_y);
                i += 3;
            }
        }
    }

    /**
     * Next frame.
     */
    public void nextFrame() {
        int i = 0;
        double delta_x = 360.0 / nx;
        double delta_y = 1.0;

        double delta_z = 220.0 / ny;

        for (int y = 0; y < ny; y++) {
            double sa = start_a + ((ny - y) * delta_z);
            float sx = (float) Math.cos(Math.toRadians(sa));
            float sy = (float) Math.sin(Math.toRadians(sa));
            if (y == 0) {
                px = sx;
                py = sy;
            }

            for (int x = 0; x < nx; x++) {
                vertices[i + 0] = sx
                        + (float) Math.sin(Math.toRadians(x * delta_x));
                vertices[i + 1] = sy
                        + (float) Math.cos(Math.toRadians(x * delta_x));
                vertices[i + 2] = (float) -(y * delta_y);
                i += 3;
            }
        }

        start_a += 2.0;
        fillVertex();

        i = 0;
        delta_x = 1.0f / nx;
        delta_y = 1.0f / ny;
        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                texture[i + 0] = x * (float) delta_x;
                texture[i + 1] = start_v + (y * (float) delta_y);
                i += 2;
            }
        }
        start_v += 0.05f;
        fillTexture();
    }

    //
    // Render the object into the GL object...
    //
    /**
     * Render.
     *
     * @param gl the gl
     * @param depth the depth
     */
    public void render(GL10 gl, float depth) {
        gl.glTranslatef(-px, -py, depth);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices_buffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture_buffer);
        gl.glColorPointer(3, GL10.GL_UNSIGNED_BYTE, 0, colors_buffer);

        int dy = 0;
        int nf = (nx + 1) << 1;
        faces_buffer.position(0);
        for (int y = 0; y < (ny - 1); y++) {
            gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, nf,
                    GL10.GL_UNSIGNED_SHORT, faces_buffer);
            dy += nf;
            faces_buffer.position(dy);
        }
    }

}
