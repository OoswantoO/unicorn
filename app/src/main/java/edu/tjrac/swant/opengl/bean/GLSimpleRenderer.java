package edu.tjrac.swant.opengl.bean;

import android.opengl.GLSurfaceView;

import com.yckj.baselib.util.L;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wpc on 2018/4/19.
 */

public class GLSimpleRenderer implements GLSurfaceView.Renderer {
    private float[] mTriangleArray = {0f, 0f, 0f,
            -1f, -1f, 0f,
            1f, -1f, 0f,
    };
    private float[] mColor = new float[]{
            1, 1, 0, 1,
            0, 1, 1, 1,
            1, 0, 1, 1
    };


    private FloatBuffer mTriangleBuffer;

    private FloatBuffer mColorBuffer;


    public GLSimpleRenderer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(mTriangleArray.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mTriangleBuffer = bb.asFloatBuffer();
        mTriangleBuffer.put(mTriangleArray);
        mTriangleBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(mColor.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        mColorBuffer = bb2.asFloatBuffer();
        mColorBuffer.put(mColor);
        mColorBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        L.i("onSurfaceCreated");
        gl.glClearColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        L.i("onSurfaceChanged");
        gl.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        // 设置OpenGL场景的大小,(0,0)表示窗口内部视口的左下角，(w,h)指定了视口的大小
        gl.glViewport(0, 0, width, height);
        // 设置投影矩阵
        gl.glMatrixMode(GL10.GL_PROJECTION);
        // 重置投影矩阵
        gl.glLoadIdentity();
        // 设置视口的大小
//            gl.glFrustumf(-ratio,ratio,-1,1,1,10);
        gl.glOrthof(-ratio, ratio, -1, 1, 1, 10);
        //以下两句声明，以后所有的变换都是针对模型(即我们绘制的图形)
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        L.i("onDrawFrame");
//            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        // 清除屏幕和深度缓存
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        // 重置当前的模型观察矩阵
        gl.glLoadIdentity();
// 允许设置顶点
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // 允许设置颜色
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        //将三角形在z轴上移动
        gl.glTranslatef(0f, 0.0f, -2.0f);
        // 设置三角形
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mTriangleBuffer);
        // 设置三角形颜色
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
        // 绘制三角形
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
// 取消颜色设置
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        // 取消顶点设置
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        //绘制结束
        gl.glFinish();
    }
}
