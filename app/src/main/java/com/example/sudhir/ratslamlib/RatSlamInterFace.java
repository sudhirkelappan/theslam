package com.example.sudhir.ratslamlib;

import android.app.Application;
import android.provider.Settings;

import java.util.Calendar;

/**
 * Created by sudhir on 3/10/16.
 */
public class RatSlamInterFace {

    private MyApp m_App = null;

    private float mLastTime, mDeltaTime, mVelocity,mAngularVelocity ;

    // private byte [] mCamView ;

    public void handleUpdateAndProcess (float deltaTime, float velocity, float anglVeocity, byte [] camView)
    {
        mVelocity = velocity;
        mAngularVelocity = anglVeocity;
        //mCamView = camView;

        if  (deltaTime > 0 && camView.length > 0)
        {
            // float currTime = Calendar.getInstance().getTime().getTime();

            //mDeltaTime = currTime - mLastTime;
            // mLastTime = currTime;

            System.out.format("===== SLAM: Velocity [%f] , Angular Vel [%f], Delta Time [%f] ===== \n", mVelocity,mAngularVelocity, deltaTime);


            this.setOdometery(mVelocity,mAngularVelocity);
            this.setDTime(deltaTime);

            System.out.format("===== SLAM: Set View and Process ===== \n");
            this.setViewAndProcess(camView);


        }
        else
        {
            System.out.format("===== SLAM: Velocity [%f] , Angular Vel [%f], Delta Time [%d] ===== \n", mVelocity,mAngularVelocity, deltaTime);
            // mLastTime = Calendar.getInstance().getTime().getTime();
        }
    }

    static
    {

        System.loadLibrary("MyNativeLib");
        System.loadLibrary("ratslam");
        System.loadLibrary("RgbLib");

    }

    public RatSlamInterFace ()
    {

        mLastTime = 0f;
        mDeltaTime  = 0f;
        mVelocity = 0f;

    }

    public void setOdometery (float vel , float vRad)
    {
        setOdom(m_slamHandle,vel,vRad);
    }

    public void setDTime ( double delta_time)
    {
        setDeltatime(m_slamHandle,delta_time);

    }

    public void setView (byte []rgb )
    {
        setViewrgb(m_slamHandle,rgb,rgb.length);

    }

    public void setViewAndProcess (byte []rgb )
    {
        setViewrgbAndProcess (m_slamHandle,rgb,rgb.length);
    }

    public ExpMap [] printExperienceMap ()
    {
        return printExpMap (m_slamHandle);
    }

    public ExpMap getCurrentExp () { return getExpNow (m_slamHandle);}

    public void process ()
    {
        slamProcess (m_slamHandle);

    }

    public char [] convertYuv2rgb (byte [] yuv)
    {

        return  yuv2rgb (yuv);

    }


    private  long m_slamHandle = -1;

    RatSlamInterFace(MyApp app)
    {

        m_App = app;

        System.out.println(" ========== Creating The Slam  ==========");


        String configPath = app.getSettingsFilePath();

        m_slamHandle = initRatslam(configPath );

        System.out.println(" ========== Creating The Slam  Done ========== " + String.format("0x%X",m_slamHandle));
        System.out.println(configPath);
        //System.out.format(" ========== Creating The Slam - Done %H ==========\n", m_slamHandle);

    }

    private native long initRatslam (String p);

    private  native void setOdom(long slam, float vel, float vRad);

    private  native void setDeltatime (long slam, double delta_time_s);

    private native void setViewrgb ( long slam,  byte [] rgb, int length );

    private native void setViewrgbAndProcess (long slam, byte [] rgb, int length );

    private  native void slamProcess ( long slam);
    private  native ExpMap [] printExpMap (long slam);
    private native ExpMap getExpNow (long slam);
    private  native char [] yuv2rgb ( byte [] yuv);



}
