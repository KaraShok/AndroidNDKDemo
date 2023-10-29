
// 把顶点坐标给这个变量，确定要绘制的形状
attribute vec4 vPosition;

// 接收纹理坐标，接收采样器采用图片的坐标
// 不用和矩阵相乘，接收一个点只要 2 个 float 就可以了
attribute vec2 vCoord;

// 传给片元着色器像素点
varying vec2 aCoord;

void main() {

    // 内置变量，我们把顶点数据赋值给这个变量
    gl_Position = vPosition;

    // 与设备有关（有些设备采集不到图像，有些是镜像）
    aCoord = vCoord;
}