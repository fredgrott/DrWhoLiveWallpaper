package com.mobilebytes.drwholivewallpaper;

import android.service.wallpaper.WallpaperService.Engine;

public class DrWhoLiveWallpaperService extends GLWallpaperService {

    private DrWhoLiveRenderer renderer;

    @Override
    public Engine onCreateEngine() {
        renderer = new DrWhoLiveRenderer(getBaseContext());
        return new GLEngine() {
            {
                setRenderer(renderer);
                setRenderMode(RENDERMODE_CONTINUOUSLY);
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}