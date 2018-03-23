package com.example.sudhir.ratslamlib;

import android.app.Application;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Pair;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by sudhir on 3/10/16.
 */


public class ShowTheCamera extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    public Camera m_theCamera = null;
    public SurfaceHolder m_sh = null;

    private byte  m_CurrentYUVFrame [];
    private byte   m_CurrentRGBFrame [];
    private int   m_Framecount = 0;

    private Date time_last ;
    private double time_diff = 0;

    Camera.Parameters m_parameters = null;

    Camera.Size m_previewSize = null;

    private MyApp m_app = null;

    public ShowTheCamera(Context context, MyApp app)
    {
        super(context);

        time_last = new Date();

        m_theCamera = Camera.open();
        m_app = app;

        m_sh = getHolder();
        m_sh.addCallback(this);

    }


    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            System.out.println(" =========== Surface Created ===========");
            if (m_theCamera == null)
            {
                m_theCamera = Camera.open();
            }


            Camera.CameraInfo info = new Camera.CameraInfo();
            m_theCamera.getCameraInfo(0,info);

            System.out.println(" =========== Camera Orientation ===========");
            System.out.println(info.orientation);
            System.out.println(" =========== Camera Orientation ===========");

            //m_theCamera.setDisplayOrientation(info.orientation);


            m_theCamera.setPreviewDisplay(holder);
            m_theCamera.setPreviewCallback(this);
            m_parameters = m_theCamera.getParameters();
            m_previewSize = m_parameters.getPreviewSize();

            m_CurrentRGBFrame = new byte [m_previewSize.width*m_previewSize.height];

            m_theCamera.startPreview();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int w, int h)
    {
        System.out.println("=========== Surface Changed ==========");

       /* m_parameters.setPreviewSize(w, h);

        m_theCamera.setParameters(m_parameters);
        */



    }

    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        if (m_theCamera == null)
        {
            return;
        }

        m_theCamera.setPreviewCallback(null);
        m_theCamera.stopPreview();

        m_theCamera.release();
        m_theCamera = null;
    }

     void decodeYUV420SP(byte[] rgb, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)                  r = 0;               else if (r > 262143)
                    r = 262143;
                if (g < 0)                  g = 0;               else if (g > 262143)
                    g = 262143;
                if (b < 0)                  b = 0;               else if (b > 262143)
                    b = 262143;

                rgb[yp] = (byte) (0xff000000 |
                                          ((r << 6) & 0xff0000) |
                                          ((g >> 2) & 0xff00)   |
                                          ((b >> 10) & 0xff));
            }
        }
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {

        decodeYUV420SP(m_CurrentRGBFrame, data, m_previewSize.width,  m_previewSize.height);

    }

    public byte [] getCamView ()
    {
        if(m_CurrentRGBFrame != null) {
            return (m_CurrentRGBFrame.length > 0) ? m_CurrentRGBFrame : null;
        }
        else
        {
            return null;
        }
    }
}