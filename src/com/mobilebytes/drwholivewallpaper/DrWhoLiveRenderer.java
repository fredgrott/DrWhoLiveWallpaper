package com.mobilebytes.drwholivewallpaper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;



// TODO: Auto-generated Javadoc
/**
 * The Class DrWhoLiveRenderer.
 */
public class DrWhoLiveRenderer implements GLSurfaceView.Renderer,
        GLWallpaperService.Renderer {

    /** The m context. */
    private final Context  mContext;

    /** The tunnel. */
    private final Tunnel3D tunnel;

    /** The created. */
    private boolean        created;

    /** The gl. */
    private GL10           gl;

    /** The w. */
    private int            w;

    /** The h. */
    private int            h;

    /** The bmp. */
    private Bitmap         bmp;

    /** The tex. */
    private int            tex;

    /**
     * Instantiates a new dr who live renderer.
     *
     * @param context the context
     */
    public DrWhoLiveRenderer(Context context) {
        mContext = context;

        // Internal members..
        tunnel = new Tunnel3D(10, 20);
        created = false;

    }

    /**
     * Load texture.
     *
     * @param gl the gl
     * @param bmp the bmp
     * @return the int
     */
    private int loadTexture(GL10 gl, Bitmap bmp) {
        ByteBuffer bb = ByteBuffer.allocateDirect(bmp.getHeight()
                * bmp.getWidth() * 4);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer ib = bb.asIntBuffer();

        for (int y = 0; y < bmp.getHeight(); y++) {
            for (int x = 0; x < bmp.getWidth(); x++) {
                ib.put(bmp.getPixel(x, y));
            }
        }
        ib.position(0);
        bb.position(0);

        int[] tmp_tex = new int[1];

        gl.glGenTextures(1, tmp_tex, 0);
        int tex = tmp_tex[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, bmp.getWidth(),
                bmp.getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);

        return tex;
    }

    /**
     * On draw frame.
     *
     * @param gl the gl
     * @see android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
     */
    public void onDrawFrame(GL10 gl) {

        // Check the created flag…
        boolean c = false;
        synchronized (this) {
            c = created;
        }
        if (!c) {
            return;
        }

        // Setting up the projection…
        float ratio = (float) w / h;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glViewport(0, 0, w, h);
        GLU.gluPerspective(gl, 45.0f, ((float) w) / h, 1f, 100f);

        // Setting up the modelview…
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        // Clear the z-buffer…
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        // Render the tunnel…
        tunnel.render(gl, -1.6f);
        tunnel.nextFrame();

        // OpenGL finish
        gl.glFlush();
        gl.glFinish();

    }

    /**
     * On surface changed.
     *
     * @param gl the gl
     * @param width the width
     * @param height the height
     * @see android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        w = width;
        h = height;
    }

    /**
     * On surface created.
     *
     * @param gl the gl
     * @param config the config
     * @see android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        created = true;

        // Enabling the state…
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // Loading texture…
        bmp = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.plants03);
        tex = loadTexture(gl, bmp);

    }
}
