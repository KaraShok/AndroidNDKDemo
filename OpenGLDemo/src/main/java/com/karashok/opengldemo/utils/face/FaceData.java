package com.karashok.opengldemo.utils.face;

/**
 * @author karashok
 * @since 05-20-2023
 */
public class FaceData {

    // 人脸坐标，第一个为人脸的 x、y；之后的才是人脸关键点
    public float[] landMarks;

    // 人脸的宽高
    public int width;
    public int height;

    // 图片的宽高
    public int imgWidth;
    public int imgHeight;

    public FaceData(float[] landMarks, int width, int height, int imgWidth, int imgHeight) {
        this.landMarks = landMarks;
        this.width = width;
        this.height = height;
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
    }
}
