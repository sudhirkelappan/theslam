package com.example.sudhir.ratslamlib;

import android.app.Application;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by sudhir on 30/9/16.
 */
public class MyApp extends Application {


    private String m_configFilePath;

    private RatSlamInterFace m_ratSlam = null;

    public RatSlamInterFace getSlamInterFace()
    {

        return  m_ratSlam;
    }


    public String getSettingsFilePath () {
        return m_configFilePath;
    }



    public void onCreate() {

        super.onCreate();


        System.out.println("============== In Application Create ================");

        createSettingsFile  ();

        m_ratSlam = new RatSlamInterFace(this);

    }

    private void createSettingsFile ()
    {
        System.out.println("============== Creating Config file ================");

        try
        {

            AssetManager mgr = getAssets();

            File f = new File( getCacheDir() + "/config.txt");

            InputStream is = getAssets().open("config.txt");

            int size = is.available();
            byte[] buff = new byte[size];

            is.read(buff);

            is.close();

            FileOutputStream fos = new FileOutputStream(f);

            fos.write(buff);
            fos.close();

            m_configFilePath = f.getAbsolutePath();

            System.out.println("============== Creating Config file - DONE ================");

        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }

    }


}
