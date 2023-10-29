package com.karashok.opengldemo.soul;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author karashok
 * @since 05-29-2023
 */
public class GLImageData {

    private int yLen;
    private int uvLen;
    private byte[] yBytes;
    private byte[] uvBytes;
    private ByteBuffer yBB;
    private ByteBuffer uBB;
    private ByteBuffer vBB;
    private boolean hasImage;

    public void initSize(int width,int height) {

        // 初始化 y 的数据长度
        yLen = width * height;
        // u、v 字节长度
        uvLen = width / 2 * height / 2;

        yBytes = new byte[yLen];
        uvBytes = new byte[uvLen];

        yBB = ByteBuffer.allocateDirect(yLen)
                .order(ByteOrder.nativeOrder());

        uBB = ByteBuffer.allocateDirect(uvLen)
                .order(ByteOrder.nativeOrder());

        vBB = ByteBuffer.allocateDirect(uvLen)
                .order(ByteOrder.nativeOrder());
    }

    public boolean initData(byte[] data) {
        hasImage = readBytes(data,yBB,0,yLen) &&
                readBytes(data,uBB,yLen,uvLen) &&
                readBytes(data,vBB,yLen + uvLen,uvLen);
        return hasImage;
    }

    private boolean readBytes(byte[] data, ByteBuffer buffer,int offset,int len) {
        //有没有这么长的数据刻度
        if (data.length < offset + len) {
            return false;
        }
        byte[] bytes;
        if (len == yBytes.length) {
            bytes = yBytes;
        } else {
            bytes = uvBytes;
        }
        System.arraycopy(data, offset, bytes, 0, len);
        buffer.position(0);
        buffer.put(bytes);
        buffer.position(0);
        return true;
    }

    public boolean hasImage() {
        return hasImage;
    }

    public ByteBuffer getYBB() {
        return yBB;
    }

    public ByteBuffer getUBB() {
        return uBB;
    }

    public ByteBuffer getVBB() {
        return vBB;
    }

}
