// 把顶点的数据给这个坐标，确定要绘画的形状
attribute vec4 vPosition;

// 接收纹理坐标，接收采样器采样图片的坐标
attribute vec4 vCoord;

// 变换矩阵，需要将原本的 vCoord 与矩阵相乘，才能够得到 SurfaceTexture 的正确的坐标
uniform mat4 vMatrix;

// 传给片元着色器，像素点
varying vec2 aCoord;

void main() {

    // 内置变量，我们把顶点数据赋值给这个变量，OpenGL 就知道他要绘制什么形状
    gl_Position = vPosition;

    // 经过测试和设备有关
    aCoord = (vMatrix * vCoord).xy;
    //aCoord =  vec2((vCoord*vMatrix).x,(vCoord*vMatrix).y);
}
