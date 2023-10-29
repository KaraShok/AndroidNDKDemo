
// float 数据是什么精度
precision mediump float;

// 采样点坐标
varying vec2 aCoord;

// 采样器
uniform sampler2D vTexture;

void main() {
    // 内置变量，texture2D：采样器 采集 aCoord的像素
    gl_FragColor = texture2D(vTexture,aCoord);
}