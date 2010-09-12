package com.mobilebytes.drwholivewallpaper;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

// TODO: Auto-generated Javadoc
/**
 * The Class MainActivity.
 */
public class MainActivity  extends Activity {

    /** The gl view. */
    private GLSurfaceView glView;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glView = new GLSurfaceView(this);
        glView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);
        glView.setRenderer(new DrWhoLiveRenderer(getBaseContext()));
        setContentView(glView);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        glView.onPause();
        super.onPause();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();
    }
}