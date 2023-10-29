package com.karashok.opengldemo.soul;

/**
 * @author karashok
 * @since 05-29-2023
 */
public interface ISurface {

    void offer(byte[] data);

    byte[] poll();

    void setVideoParams(int width,int height,int fps);
}
