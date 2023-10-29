#extension GL_OES_EGL_image_external : require

// float 数据是什么精度
precision mediump float;

// 采样点的坐标
varying vec2 aCoord;

// 采样器
uniform samplerExternalOES vTexture;

void main() {
    // 变量接收像素值，texture2D：采样器；采集 aCoord 的像素
    gl_FragColor = texture2D(vTexture,aCoord);
}
