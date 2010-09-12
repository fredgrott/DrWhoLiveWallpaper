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

import java.io.Writer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import com.mobilebytes.drwholivewallpaper.BaseConfigChooser.ComponentSizeChooser;
import com.mobilebytes.drwholivewallpaper.BaseConfigChooser.SimpleEGLConfigChooser;

// TODO: Auto-generated Javadoc
/**
 * From RichardGreen's
 * http://www.rbgrn.net/content/354-glsurfaceview-adapted-3d-live-wallpapers
 *
 */
abstract class BaseConfigChooser implements EGLConfigChooser {
	/**
	 *
	 *
	 *
	 */
    public static class ComponentSizeChooser extends BaseConfigChooser {

        /** The m value. */
        private final int[] mValue;

        // Subclasses can adjust these values:
        /** The m red size. */
        protected int       mRedSize;

        /** The m green size. */
        protected int       mGreenSize;

        /** The m blue size. */
        protected int       mBlueSize;

        /** The m alpha size. */
        protected int       mAlphaSize;

        /** The m depth size. */
        protected int       mDepthSize;

        /** The m stencil size. */
        protected int       mStencilSize;

        /**
         * Instantiates a new component size chooser.
         *
         * @param redSize the red size
         * @param greenSize the green size
         * @param blueSize the blue size
         * @param alphaSize the alpha size
         * @param depthSize the depth size
         * @param stencilSize the stencil size
         */
        public ComponentSizeChooser(int redSize, int greenSize, int blueSize,
                int alphaSize, int depthSize, int stencilSize) {
            super(new int[] { EGL10.EGL_RED_SIZE, redSize,
                    EGL10.EGL_GREEN_SIZE, greenSize, EGL10.EGL_BLUE_SIZE,
                    blueSize, EGL10.EGL_ALPHA_SIZE, alphaSize,
                    EGL10.EGL_DEPTH_SIZE, depthSize, EGL10.EGL_STENCIL_SIZE,
                    stencilSize, EGL10.EGL_NONE });
            mValue = new int[1];
            mRedSize = redSize;
            mGreenSize = greenSize;
            mBlueSize = blueSize;
            mAlphaSize = alphaSize;
            mDepthSize = depthSize;
            mStencilSize = stencilSize;
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                EGLConfig[] configs) {
            EGLConfig closestConfig = null;
            int closestDistance = 1000;
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);
                if ((d >= mDepthSize) && (s >= mStencilSize)) {
                    int r = findConfigAttrib(egl, display, config,
                            EGL10.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config,
                            EGL10.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config,
                            EGL10.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config,
                            EGL10.EGL_ALPHA_SIZE, 0);
                    int distance = Math.abs(r - mRedSize)
                            + Math.abs(g - mGreenSize)
                            + Math.abs(b - mBlueSize)
                            + Math.abs(a - mAlphaSize);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestConfig = config;
                    }
                }
            }
            return closestConfig;
        }

        /**
         * Find config attrib.
         *
         * @param egl the egl
         * @param display the display
         * @param config the config
         * @param attribute the attribute
         * @param defaultValue the default value
         * @return the int
         */
        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }
    }

    /**
     * This class will choose a supported surface as close to RGB565 as
     * possible, with or without a depth buffer.
     *
     */
    public static class SimpleEGLConfigChooser extends ComponentSizeChooser {

        /**
         * Instantiates a new simple egl config chooser.
         *
         * @param withDepthBuffer the with depth buffer
         */
        public SimpleEGLConfigChooser(boolean withDepthBuffer) {
            super(4, 4, 4, 0, withDepthBuffer ? 16 : 0, 0);
            // Adjust target values. This way we'll accept a 4444 or
            // 555 buffer if there's no 565 buffer available.
            mRedSize = 5;
            mGreenSize = 6;
            mBlueSize = 5;
        }
    }

    /** The m config spec. */
    protected int[] mConfigSpec;

    /**
     * Instantiates a new base config chooser.
     *
     * @param configSpec the config spec
     */
    public BaseConfigChooser(int[] configSpec) {
        mConfigSpec = configSpec;
    }

    /**
     * Choose config.
     *
     * @param egl the egl
     * @param display the display
     * @return the eGL config
     * @see com.mobilebytes.drwholivewallpaper.EGLConfigChooser#chooseConfig(javax.microedition.khronos.egl.EGL10, javax.microedition.khronos.egl.EGLDisplay)
     */
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config);

        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        EGLConfig[] configs = new EGLConfig[numConfigs];
        egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
                num_config);
        EGLConfig config = chooseConfig(egl, display, configs);
        if (config == null) {
            throw new IllegalArgumentException("No config chosen");
        }
        return config;
    }

    /**
     * Choose config.
     *
     * @param egl the egl
     * @param display the display
     * @param configs the configs
     * @return the eGL config
     */
    abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
            EGLConfig[] configs);
}

/**
 * DefaultContextFactory.
 * @author fredgrott
 *
 */
class DefaultContextFactory implements EGLContextFactory {

    /**
     * Creates a new DefaultContext object.
     *
     * @param egl the egl
     * @param display the display
     * @param config the config
     * @return the EGL context
     * @see com.mobilebytes.drwholivewallpaper.EGLContextFactory#createContext(javax.microedition.khronos.egl.EGL10, javax.microedition.khronos.egl.EGLDisplay, javax.microedition.khronos.egl.EGLConfig)
     */
    public EGLContext createContext(EGL10 egl, EGLDisplay display,
            EGLConfig config) {
        return egl
                .eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, null);
    }

    /**
     * Destroy context.
     *
     * @param egl the egl
     * @param display the display
     * @param context the context
     * @see com.mobilebytes.drwholivewallpaper.EGLContextFactory#destroyContext(javax.microedition.khronos.egl.EGL10, javax.microedition.khronos.egl.EGLDisplay, javax.microedition.khronos.egl.EGLContext)
     */
    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        egl.eglDestroyContext(display, context);
    }
}

// ----------------------------------------------------------------------
/**
 *
 */
class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {

	/**
	 * Creates a new DefaultWindowSurface object.
	 *
	 * @param egl the egl
	 * @param display the display
	 * @param config the config
	 * @param nativeWindow the native window
	 * @return the EGL surface
	 * @see com.mobilebytes.drwholivewallpaper.EGLWindowSurfaceFactory#createWindowSurface(javax.microedition.khronos.egl.EGL10, javax.microedition.khronos.egl.EGLDisplay, javax.microedition.khronos.egl.EGLConfig, java.lang.Object)
	 */
    public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
            EGLConfig config, Object nativeWindow) {
        // this is a bit of a hack to work around Droid init problems - if you
        // don't have this, it'll get hung up on orientation changes
        EGLSurface eglSurface = null;
        while (eglSurface == null) {
            try {
                eglSurface = egl.eglCreateWindowSurface(display, config,
                        nativeWindow, null);
            } catch (Throwable t) {
            } finally {
                if (eglSurface == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException t) {
                    }
                }
            }
        }
        return eglSurface;
    }

    /**
     * Destroy surface.
     *
     * @param egl the egl
     * @param display the display
     * @param surface the surface
     * @see com.mobilebytes.drwholivewallpaper.EGLWindowSurfaceFactory#destroySurface(javax.microedition.khronos.egl.EGL10, javax.microedition.khronos.egl.EGLDisplay, javax.microedition.khronos.egl.EGLSurface)
     */
    public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
        egl.eglDestroySurface(display, surface);
    }
}

interface EGLConfigChooser {
    EGLConfig chooseConfig(EGL10 egl, EGLDisplay display);
}

/**
 * An interface for customizing the eglCreateContext and eglDestroyContext
 * calls.
 *
 *
 * This interface must be implemented by clients wishing to call
 * {@link GLWallpaperService#setEGLContextFactory(EGLContextFactory)}
 */
interface EGLContextFactory {
    EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig);

    /**
     * Destroy context.
     *
     * @param egl the egl
     * @param display the display
     * @param context the context
     */
    void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context);
}
/**
 *
 * @author fredgrott
 *
 */
class EglHelper {

    /** The m egl. */
    private EGL10                         mEgl;

    /** The m egl display. */
    private EGLDisplay                    mEglDisplay;

    /** The m egl surface. */
    private EGLSurface                    mEglSurface;

    /** The m egl context. */
    private EGLContext                    mEglContext;

    /** The m egl config. */
    EGLConfig                             mEglConfig;

    /** The m egl config chooser. */
    private final EGLConfigChooser        mEGLConfigChooser;

    /** The m egl context factory. */
    private final EGLContextFactory       mEGLContextFactory;

    /** The m egl window surface factory. */
    private final EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;

    /** The m gl wrapper. */
    private final GLWrapper               mGLWrapper;

    /** The last instance id. */
    private static int                    lastInstanceId = 0;

    /** The instance id. */
    private final int                     instanceId     = ++lastInstanceId;

    /**
     * Instantiates a new egl helper.
     *
     * @param chooser the chooser
     * @param contextFactory the context factory
     * @param surfaceFactory the surface factory
     * @param wrapper the wrapper
     */
    public EglHelper(EGLConfigChooser chooser,
            EGLContextFactory contextFactory,
            EGLWindowSurfaceFactory surfaceFactory, GLWrapper wrapper) {
        mEGLConfigChooser = chooser;
        mEGLContextFactory = contextFactory;
        mEGLWindowSurfaceFactory = surfaceFactory;
        mGLWrapper = wrapper;
    }

    /**
     * React to the creation of a new surface by creating and returning an
     * OpenGL interface that renders to that surface.
     *
     * @param holder the holder
     * @return the gL
     */
    public GL createSurface(SurfaceHolder holder) {
        /*
         * The window size has changed, so we need to create a new surface.
         */
        if ((mEglSurface != null) && (mEglSurface != EGL10.EGL_NO_SURFACE)) {

            /*
             * Unbind and destroy the old EGL surface, if there is one.
             */
            mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay,
                    mEglSurface);
        }

        /*
         * Create an EGL surface we can render into.
         */
        mEglSurface = mEGLWindowSurfaceFactory.createWindowSurface(mEgl,
                mEglDisplay, mEglConfig, holder);

        if ((mEglSurface == null) || (mEglSurface == EGL10.EGL_NO_SURFACE)) {
            throw new RuntimeException("createWindowSurface failed");
        }

        /*
         * Before we can issue GL commands, we need to make sure the context is
         * current and bound to a surface.
         */
        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface,
                mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed.");
        }

        GL gl = mEglContext.getGL();
        if (mGLWrapper != null) {
            gl = mGLWrapper.wrap(gl);
        }

        /*
         * if ((mDebugFlags & (DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS))!= 0)
         * { int configFlags = 0; Writer log = null; if ((mDebugFlags &
         * DEBUG_CHECK_GL_ERROR) != 0) { configFlags |=
         * GLDebugHelper.CONFIG_CHECK_GL_ERROR; } if ((mDebugFlags &
         * DEBUG_LOG_GL_CALLS) != 0) { log = new LogWriter(); } gl =
         * GLDebugHelper.wrap(gl, configFlags, log); }
         */
        return gl;
    }

    /**
     * Destroy surface.
     */
    public void destroySurface() {
        if ((mEglSurface != null) && (mEglSurface != EGL10.EGL_NO_SURFACE)) {
            mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay,
                    mEglSurface);
            mEglSurface = null;
        }
    }

    /**
     * Finish.
     */
    public void finish() {
        if (mEglContext != null) {
            mEGLContextFactory.destroyContext(mEgl, mEglDisplay, mEglContext);
            mEglContext = null;
        }
        if (mEglDisplay != null) {
            mEgl.eglTerminate(mEglDisplay);
            mEglDisplay = null;
        }
    }

    /**
     * Initialize EGL for a given configuration spec.
     *
     * @param configSpec
     */
    public void start() {
        Log.d("EglHelper" + instanceId, "start()");
        if (mEgl == null) {
            Log.d("EglHelper" + instanceId, "getting new EGL");
            /*
             * Get an EGL instance
             */
            mEgl = (EGL10) EGLContext.getEGL();
        } else {
            Log.d("EglHelper" + instanceId, "reusing EGL");
        }

        if (mEglDisplay == null) {
            Log.d("EglHelper" + instanceId, "getting new display");
            /*
             * Get to the default display.
             */
            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        } else {
            Log.d("EglHelper" + instanceId, "reusing display");
        }

        if (mEglConfig == null) {
            Log.d("EglHelper" + instanceId, "getting new config");
            /*
             * We can now initialize EGL for that display
             */
            int[] version = new int[2];
            mEgl.eglInitialize(mEglDisplay, version);
            mEglConfig = mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);
        } else {
            Log.d("EglHelper" + instanceId, "reusing config");
        }

        if (mEglContext == null) {
            Log.d("EglHelper" + instanceId, "creating new context");
            /*
             * Create an OpenGL ES context. This must be done only once, an
             * OpenGL context is a somewhat heavy object.
             */
            mEglContext = mEGLContextFactory.createContext(mEgl, mEglDisplay,
                    mEglConfig);
            if ((mEglContext == null) || (mEglContext == EGL10.EGL_NO_CONTEXT)) {
                throw new RuntimeException("createContext failed");
            }
        } else {
            Log.d("EglHelper" + instanceId, "reusing context");
        }

        mEglSurface = null;
    }

    /**
     * Display the current render surface.
     *
     * @return false if the context has been lost.
     */
    public boolean swap() {
        mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);

        /*
         * Always check for EGL_CONTEXT_LOST, which means the context and all
         * associated data were lost (For instance because the device went to
         * sleep). We need to sleep until we get a new surface.
         */
        return mEgl.eglGetError() != EGL11.EGL_CONTEXT_LOST;
    }
}

/**
 * An interface for customizing the eglCreateWindowSurface and eglDestroySurface
 * calls.
 *
 *
 * This interface must be implemented by clients wishing to call
 * {@link GLWallpaperService#setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory)}
 */
interface EGLWindowSurfaceFactory {

    /**
     * Creates a new EGLWindowSurface object.
     *
     * @param egl the egl
     * @param display the display
     * @param config the config
     * @param nativeWindow the native window
     * @return the EGL surface
     */
    EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
            EGLConfig config, Object nativeWindow);

    /**
     * Destroy surface.
     *
     * @param egl the egl
     * @param display the display
     * @param surface the surface
     */
    void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface);
}

/**
 *
 * @author fredgrott
 *
 */
class GLThread extends Thread {
    /**
     *
     * @author fredgrott
     *
     */
	private class GLThreadManager {

        /**
         * Release egl surface.
         *
         * @param thread the thread
         */
        public synchronized void releaseEglSurface(GLThread thread) {
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }

        /**
         * Thread exiting.
         *
         * @param thread the thread
         */
        public synchronized void threadExiting(GLThread thread) {
            if (LOG_THREADS) {
                Log.i("GLThread", "exiting tid=" + thread.getId());
            }
            thread.mDone = true;
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }

        /**
         * Tries once to acquire the right to use an EGL surface. Does not
         * block.
         *
         * @param thread the thread
         * @return true if the right to use an EGL surface was acquired.
         */
        public synchronized boolean tryAcquireEglSurface(GLThread thread) {
            if ((mEglOwner == thread) || (mEglOwner == null)) {
                mEglOwner = thread;
                notifyAll();
                return true;
            }
            return false;
        }
    }

    /** The Constant LOG_THREADS. */
    private final static boolean              LOG_THREADS          = false;

    /** The Constant DEBUG_CHECK_GL_ERROR. */
    public final static int                   DEBUG_CHECK_GL_ERROR = 1;

    /** The Constant DEBUG_LOG_GL_CALLS. */
    public final static int                   DEBUG_LOG_GL_CALLS   = 2;

    /** The s gl thread manager. */
    private final GLThreadManager             sGLThreadManager     = new GLThreadManager();

    /** The m egl owner. */
    private GLThread                          mEglOwner;

    /** The m egl config chooser. */
    private final EGLConfigChooser            mEGLConfigChooser;

    /** The m egl context factory. */
    private final EGLContextFactory           mEGLContextFactory;

    /** The m egl window surface factory. */
    private final EGLWindowSurfaceFactory     mEGLWindowSurfaceFactory;

    /** The m gl wrapper. */
    private final GLWrapper                   mGLWrapper;

    /** The m holder. */
    public SurfaceHolder                      mHolder;

    /** The m size changed. */
    private boolean                           mSizeChanged         = true;
    // Once the thread is started, all accesses to the following member
    // variables are protected by the sGLThreadManager monitor
    /** The m done. */
    public boolean                            mDone;

    /** The m paused. */
    private boolean                           mPaused;

    /** The m has surface. */
    private boolean                           mHasSurface;

    /** The m waiting for surface. */
    private boolean                           mWaitingForSurface;

    /** The m have egl. */
    private boolean                           mHaveEgl;

    /** The m width. */
    private int                               mWidth;

    /** The m height. */
    private int                               mHeight;

    /** The m render mode. */
    private int                               mRenderMode;

    /** The m request render. */
    private boolean                           mRequestRender;

    /** The m events waiting. */
    private boolean                           mEventsWaiting;
    // End of member variables protected by the sGLThreadManager monitor.
    /** The m renderer. */
    private final GLWallpaperService.Renderer mRenderer;

    /** The m event queue. */
    @SuppressWarnings("unchecked")
    private final ArrayList                   mEventQueue          = new ArrayList();

    /** The m egl helper. */
    private EglHelper                         mEglHelper;

    /**
     * Instantiates a new gL thread.
     *
     * @param renderer the renderer
     * @param chooser the chooser
     * @param contextFactory the context factory
     * @param surfaceFactory the surface factory
     * @param wrapper the wrapper
     */
    GLThread(GLWallpaperService.Renderer renderer, EGLConfigChooser chooser,
            EGLContextFactory contextFactory,
            EGLWindowSurfaceFactory surfaceFactory, GLWrapper wrapper) {
        super();
        mDone = false;
        mWidth = 0;
        mHeight = 0;
        mRequestRender = true;
        mRenderMode = GLWallpaperService.GLEngine.RENDERMODE_CONTINUOUSLY;
        mRenderer = renderer;
        mEGLConfigChooser = chooser;
        mEGLContextFactory = contextFactory;
        mEGLWindowSurfaceFactory = surfaceFactory;
        mGLWrapper = wrapper;
    }

    /**
     * Gets the event.
     *
     * @return the event
     */
    private Runnable getEvent() {
        synchronized (this) {
            if (mEventQueue.size() > 0) {
                return (Runnable) mEventQueue.remove(0);
            }

        }
        return null;
    }

    /**
     * Gets the render mode.
     *
     * @return the render mode
     */
    public int getRenderMode() {
        synchronized (sGLThreadManager) {
            return mRenderMode;
        }
    }

    /**
     * Guarded run.
     *
     * @throws InterruptedException the interrupted exception
     */
    private void guardedRun() throws InterruptedException {
        mEglHelper = new EglHelper(mEGLConfigChooser, mEGLContextFactory,
                mEGLWindowSurfaceFactory, mGLWrapper);
        try {
            GL10 gl = null;
            boolean tellRendererSurfaceCreated = true;
            boolean tellRendererSurfaceChanged = true;

            /*
             * This is our main activity thread's loop, we go until asked to
             * quit.
             */
            while (!isDone()) {
                /*
                 * Update the asynchronous state (window size)
                 */
                int w = 0;
                int h = 0;
                boolean changed = false;
                boolean needStart = false;
                boolean eventsWaiting = false;

                synchronized (sGLThreadManager) {
                    while (true) {
                        // Manage acquiring and releasing the SurfaceView
                        // surface and the EGL surface.
                        if (mPaused) {
                            stopEglLocked();
                        }
                        if (!mHasSurface) {
                            if (!mWaitingForSurface) {
                                stopEglLocked();
                                mWaitingForSurface = true;
                                sGLThreadManager.notifyAll();
                            }
                        } else {
                            if (!mHaveEgl) {
                                if (sGLThreadManager.tryAcquireEglSurface(this)) {
                                    mHaveEgl = true;
                                    mEglHelper.start();
                                    mRequestRender = true;
                                    needStart = true;
                                }
                            }
                        }

                        // Check if we need to wait. If not, update any state
                        // that needs to be updated, copy any state that
                        // needs to be copied, and use "break" to exit the
                        // wait loop.

                        if (mDone) {
                            return;
                        }

                        if (mEventsWaiting) {
                            eventsWaiting = true;
                            mEventsWaiting = false;
                            break;
                        }

                        if ((!mPaused)
                                && mHasSurface
                                && mHaveEgl
                                && (mWidth > 0)
                                && (mHeight > 0)
                                && (mRequestRender || (mRenderMode == GLWallpaperService.GLEngine.RENDERMODE_CONTINUOUSLY))) {
                            changed = mSizeChanged;
                            w = mWidth;
                            h = mHeight;
                            mSizeChanged = false;
                            mRequestRender = false;
                            if (mHasSurface && mWaitingForSurface) {
                                changed = true;
                                mWaitingForSurface = false;
                                sGLThreadManager.notifyAll();
                            }
                            break;
                        }

                        // By design, this is the only place where we wait().

                        if (LOG_THREADS) {
                            Log.i("GLThread", "waiting tid=" + getId());
                        }
                        sGLThreadManager.wait();
                    }
                } // end of synchronized(sGLThreadManager)

                /*
                 * Handle queued events
                 */
                if (eventsWaiting) {
                    Runnable r;
                    while ((r = getEvent()) != null) {
                        r.run();
                        if (isDone()) {
                            return;
                        }
                    }
                    // Go back and see if we need to wait to render.
                    continue;
                }

                if (needStart) {
                    tellRendererSurfaceCreated = true;
                    changed = true;
                }
                if (changed) {
                    gl = (GL10) mEglHelper.createSurface(mHolder);
                    tellRendererSurfaceChanged = true;
                }
                if (tellRendererSurfaceCreated) {
                    mRenderer.onSurfaceCreated(gl, mEglHelper.mEglConfig);
                    tellRendererSurfaceCreated = false;
                }
                if (tellRendererSurfaceChanged) {
                    mRenderer.onSurfaceChanged(gl, w, h);
                    tellRendererSurfaceChanged = false;
                }
                if ((w > 0) && (h > 0)) {
                    /* draw a frame here */
                    mRenderer.onDrawFrame(gl);

                    /*
                     * Once we're done with GL, we need to call swapBuffers() to
                     * instruct the system to display the rendered frame
                     */
                    mEglHelper.swap();
                }
            }
        } finally {
            /*
             * clean-up everything...
             */
            synchronized (sGLThreadManager) {
                stopEglLocked();
                mEglHelper.finish();
            }
        }
    }

    /**
     * Checks if is done.
     *
     * @return true, if is done
     */
    private boolean isDone() {
        synchronized (sGLThreadManager) {
            return mDone;
        }
    }

    /**
     * On pause.
     */
    public void onPause() {
        synchronized (sGLThreadManager) {
            mPaused = true;
            sGLThreadManager.notifyAll();
        }
    }

    /**
     * On resume.
     */
    public void onResume() {
        synchronized (sGLThreadManager) {
            mPaused = false;
            mRequestRender = true;
            sGLThreadManager.notifyAll();
        }
    }

    /**
     * On window resize.
     *
     * @param w the w
     * @param h the h
     */
    public void onWindowResize(int w, int h) {
        synchronized (sGLThreadManager) {
            mWidth = w;
            mHeight = h;
            mSizeChanged = true;
            sGLThreadManager.notifyAll();
        }
    }

    /**
     * Queue an "event" to be run on the GL rendering thread.
     *
     * @param r
     *            the runnable to be run on the GL rendering thread.
     */
    @SuppressWarnings("unchecked")
    public void queueEvent(Runnable r) {
        synchronized (this) {
            mEventQueue.add(r);
            synchronized (sGLThreadManager) {
                mEventsWaiting = true;
                sGLThreadManager.notifyAll();
            }
        }
    }

    /**
     * Request exit and wait.
     */
    public void requestExitAndWait() {
        // don't call this from GLThread thread or it is a guaranteed
        // deadlock!
        synchronized (sGLThreadManager) {
            mDone = true;
            sGLThreadManager.notifyAll();
        }
        try {
            join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Request render.
     */
    public void requestRender() {
        synchronized (sGLThreadManager) {
            mRequestRender = true;
            sGLThreadManager.notifyAll();
        }
    }

    @Override
    public void run() {
        setName("GLThread " + getId());
        if (LOG_THREADS) {
            Log.i("GLThread", "starting tid=" + getId());
        }

        try {
            guardedRun();
        } catch (InterruptedException e) {
            // fall thru and exit normally
        } finally {
            sGLThreadManager.threadExiting(this);
        }
    }

    /**
     * Sets the render mode.
     *
     * @param renderMode the new render mode
     */
    public void setRenderMode(int renderMode) {
        if (!((GLWallpaperService.GLEngine.RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= GLWallpaperService.GLEngine.RENDERMODE_CONTINUOUSLY))) {
            throw new IllegalArgumentException("renderMode");
        }
        synchronized (sGLThreadManager) {
            mRenderMode = renderMode;
            if (renderMode == GLWallpaperService.GLEngine.RENDERMODE_CONTINUOUSLY) {
                sGLThreadManager.notifyAll();
            }
        }
    }

    /**
     * This private method should only be called inside a
     * synchronized(sGLThreadManager) block.
     */
    private void stopEglLocked() {
        if (mHaveEgl) {
            mHaveEgl = false;
            mEglHelper.destroySurface();
            sGLThreadManager.releaseEglSurface(this);
        }
    }

    /**
     * Surface created.
     *
     * @param holder the holder
     */
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        synchronized (sGLThreadManager) {
            if (LOG_THREADS) {
                Log.i("GLThread", "surfaceCreated tid=" + getId());
            }
            mHasSurface = true;
            sGLThreadManager.notifyAll();
        }
    }

    /**
     * Surface destroyed.
     */
    public void surfaceDestroyed() {
        synchronized (sGLThreadManager) {
            if (LOG_THREADS) {
                Log.i("GLThread", "surfaceDestroyed tid=" + getId());
            }
            mHasSurface = false;
            sGLThreadManager.notifyAll();
            while (!mWaitingForSurface && isAlive() && !mDone) {
                try {
                    sGLThreadManager.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

/**
 * The Class GLWallpaperService.
 */
public class GLWallpaperService extends WallpaperService {

    /**
     * The Class GLEngine.
     */
    public class GLEngine extends Engine {

        /** The Constant RENDERMODE_WHEN_DIRTY. */
        public final static int         RENDERMODE_WHEN_DIRTY   = 0;

        /** The Constant RENDERMODE_CONTINUOUSLY. */
        public final static int         RENDERMODE_CONTINUOUSLY = 1;

        /** The m gl thread. */
        private GLThread                mGLThread;

        /** The m egl config chooser. */
        private EGLConfigChooser        mEGLConfigChooser;

        /** The m egl context factory. */
        private EGLContextFactory       mEGLContextFactory;

        /** The m egl window surface factory. */
        private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;

        /** The m gl wrapper. */
        private GLWrapper               mGLWrapper;

        /** The m debug flags. */
        private int                     mDebugFlags;

        /**
         * Instantiates a new gL engine.
         */
        public GLEngine() {
            super();
        }

        /**
         * Check render thread state.
         */
        private void checkRenderThreadState() {
            if (mGLThread != null) {
                throw new IllegalStateException(
                        "setRenderer has already been called for this instance.");
            }
        }

        /**
         * Gets the debug flags.
         *
         * @return the debug flags
         */
        public int getDebugFlags() {
            return mDebugFlags;
        }

        /**
         * Gets the render mode.
         *
         * @return the render mode
         */
        public int getRenderMode() {
            return mGLThread.getRenderMode();
        }

        /* (non-Javadoc)
         * @see android.service.wallpaper.WallpaperService.Engine#onCreate(android.view.SurfaceHolder)
         */
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            // Log.d(TAG, "GLEngine.onCreate()");
        }

        /* (non-Javadoc)
         * @see android.service.wallpaper.WallpaperService.Engine#onDestroy()
         */
        @Override
        public void onDestroy() {
            super.onDestroy();
            // Log.d(TAG, "GLEngine.onDestroy()");
            mGLThread.requestExitAndWait();
        }

        /**
         * On pause.
         */
        public void onPause() {
            mGLThread.onPause();
        }

        /**
         * On resume.
         */
        public void onResume() {
            mGLThread.onResume();
        }

        /* (non-Javadoc)
         * @see android.service.wallpaper.WallpaperService.Engine#onSurfaceChanged(android.view.SurfaceHolder, int, int, int)
         */
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                int width, int height) {
            // Log.d(TAG, "onSurfaceChanged()");
            mGLThread.onWindowResize(width, height);
            super.onSurfaceChanged(holder, format, width, height);
        }

        /* (non-Javadoc)
         * @see android.service.wallpaper.WallpaperService.Engine#onSurfaceCreated(android.view.SurfaceHolder)
         */
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "onSurfaceCreated()");
            mGLThread.surfaceCreated(holder);
            super.onSurfaceCreated(holder);
        }

        /* (non-Javadoc)
         * @see android.service.wallpaper.WallpaperService.Engine#onSurfaceDestroyed(android.view.SurfaceHolder)
         */
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "onSurfaceDestroyed()");
            mGLThread.surfaceDestroyed();
            super.onSurfaceDestroyed(holder);
        }

        /* (non-Javadoc)
         * @see android.service.wallpaper.WallpaperService.Engine#onVisibilityChanged(boolean)
         */
        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                onResume();
            } else {
                onPause();
            }
            super.onVisibilityChanged(visible);
        }

        /**
         * Queue event.
         *
         * @param r the r
         */
        public void queueEvent(Runnable r) {
            mGLThread.queueEvent(r);
        }

        /**
         * Request render.
         */
        public void requestRender() {
            mGLThread.requestRender();
        }

        /**
         * Sets the debug flags.
         *
         * @param debugFlags the new debug flags
         */
        public void setDebugFlags(int debugFlags) {
            mDebugFlags = debugFlags;
        }

        /**
         * Sets the eGL config chooser.
         *
         * @param needDepth the new eGL config chooser
         */
        public void setEGLConfigChooser(boolean needDepth) {
            setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
        }

        /**
         * Sets the eGL config chooser.
         *
         * @param configChooser the new eGL config chooser
         */
        public void setEGLConfigChooser(EGLConfigChooser configChooser) {
            checkRenderThreadState();
            mEGLConfigChooser = configChooser;
        }

        /**
         * Sets the egl config chooser.
         *
         * @param redSize the red size
         * @param greenSize the green size
         * @param blueSize the blue size
         * @param alphaSize the alpha size
         * @param depthSize the depth size
         * @param stencilSize the stencil size
         */
        public void setEGLConfigChooser(int redSize, int greenSize,
                int blueSize, int alphaSize, int depthSize, int stencilSize) {
            setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize,
                    blueSize, alphaSize, depthSize, stencilSize));
        }

        /**
         * Sets the eGL context factory.
         *
         * @param factory the new eGL context factory
         */
        public void setEGLContextFactory(EGLContextFactory factory) {
            checkRenderThreadState();
            mEGLContextFactory = factory;
        }

        /**
         * Sets the eGL window surface factory.
         *
         * @param factory the new eGL window surface factory
         */
        public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
            checkRenderThreadState();
            mEGLWindowSurfaceFactory = factory;
        }

        /**
         * An EGL helper class.
         *
         * @param glWrapper the new gL wrapper
         */
        public void setGLWrapper(GLWrapper glWrapper) {
            mGLWrapper = glWrapper;
        }

        /**
         * Sets the renderer.
         *
         * @param renderer the new renderer
         */
        public void setRenderer(Renderer renderer) {
            checkRenderThreadState();
            if (mEGLConfigChooser == null) {
                mEGLConfigChooser = new SimpleEGLConfigChooser(true);
            }
            if (mEGLContextFactory == null) {
                mEGLContextFactory = new DefaultContextFactory();
            }
            if (mEGLWindowSurfaceFactory == null) {
                mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
            }
            mGLThread = new GLThread(renderer, mEGLConfigChooser,
                    mEGLContextFactory, mEGLWindowSurfaceFactory, mGLWrapper);
            mGLThread.start();
        }

        /**
         * Sets the render mode.
         *
         * @param renderMode the new render mode
         */
        public void setRenderMode(int renderMode) {
            mGLThread.setRenderMode(renderMode);
        }
    }

    /**
     * The Interface Renderer.
     */
    public interface Renderer {

        /**
         * On draw frame.
         *
         * @param gl the gl
         */
        public void onDrawFrame(GL10 gl);

        /**
         * On surface changed.
         *
         * @param gl the gl
         * @param width the width
         * @param height the height
         */
        public void onSurfaceChanged(GL10 gl, int width, int height);

        /**
         * On surface created.
         *
         * @param gl the gl
         * @param config the config
         */
        public void onSurfaceCreated(GL10 gl, EGLConfig config);
    }

    /** The Constant TAG. */
    private static final String TAG = "GLWallpaperService";

    /* (non-Javadoc)
     * @see android.service.wallpaper.WallpaperService#onCreateEngine()
     */
    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }
}

interface GLWrapper {
    /**
     * Wraps a gl interface in another gl interface.
     *
     * @param gl
     *            a GL interface that is to be wrapped.
     * @return either the input argument or another GL object that wraps the
     *         input argument.
     */
    GL wrap(GL gl);
}
/**
 *
 * @author fredgrott
 *
 */
class LogWriter extends Writer {

    /** The m builder. */
    private final StringBuilder mBuilder = new StringBuilder();

    @Override
    public void close() {
        flushBuilder();
    }

    @Override
    public void flush() {
        flushBuilder();
    }

    /**
     * Flush builder.
     */
    private void flushBuilder() {
        if (mBuilder.length() > 0) {
            Log.v("GLSurfaceView", mBuilder.toString());
            mBuilder.delete(0, mBuilder.length());
        }
    }

    @Override
    public void write(char[] buf, int offset, int count) {
        for (int i = 0; i < count; i++) {
            char c = buf[offset + i];
            if (c == '\n') {
                flushBuilder();
            } else {
                mBuilder.append(c);
            }
        }
    }
}
