package com.example.sudhir.ratslamlib;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private static final int HISTORY_SIZE = 10000;
    private XYPlot aprHistoryPlot = null;
    private SimpleXYSeries azimuthHistorySeries = null;
    private Redrawer redrawer;



    private RatSlamInterFace m_Slam = null;

    private Activity m_act = this;

    private TextView m_Tv = null;
    private TextView m_gpsLog = null;

    private Handler mHandler = new Handler();
    private long mStartTime = 0;
    private float mDeltaTime = 0;

    private boolean mSlamOn = false;

    private Runnable mUpdateTimeTask = new Runnable() {

        public void run() {

            long totalMillis = 0;
            long start = 0;
            long millis = 0;
            long next = 0;

            start = mStartTime;
            totalMillis = SystemClock.uptimeMillis();

            millis = totalMillis - start;
            next = totalMillis + 1200;

            mStartTime = totalMillis;

            // ToDo: Use seconds value

            mDeltaTime = millis / 1000 ;

            // System.out.format("===== Time Ticker [%d]->[%d] [%f] ===== \n", totalMillis,next, mDeltaTime);
            String sLog;
            sLog = String.format(Locale.ENGLISH," TimeDelta = [%f], Velocity = [%f], Angle = [%f]\n",mDeltaTime,mSensorsHandler.mVelocity, mSensorsHandler.mAngularVelocity);
            m_gpsLog.setText(sLog);

            if (mSensorsHandler.m_bVelocityAvailable && mSlamOn )
            {

                String content = String.format(Locale.ENGLISH,"GPS [%f]:[%f]\n", mSensorsHandler.m_CurrLocation.getLatitude(), mSensorsHandler.m_CurrLocation.getLongitude());

                m_Slam.handleUpdateAndProcess(mDeltaTime, mSensorsHandler.mVelocity, mSensorsHandler.mAngularVelocity, m_sc.getCamView());
                ExpMap m = m_Slam.getCurrentExp();

                content += String.format(Locale.ENGLISH,"Exp [%f]:[%f] \n", m.x,m.y);
                m_Tv.append(content);

                azimuthHistorySeries.addLast(m.x,m.y);

            }

            //
            // Next iteration of
            //
            mHandler.postAtTime(this,next);
        }
    };


    private GoogleApiClient client;
    private SensorsHandler mSensorsHandler;

    private ShowTheCamera m_sc = null;

    private XYPlot m_Plot;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {

            System.out.println("PERMISSION_NOT_GRANTED");
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        else
        {
            System.out.println("PERMISSION_GRANTED");
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        verifyStoragePermissions (this);

        System.out.format(Locale.ENGLISH,"On Create\n");


        //setContentView(R.layout.activity_main);
        setContentView(R.layout.content_main);


        MyApp app = (MyApp)getApplication();
        m_Slam = app.getSlamInterFace();

        m_sc = new ShowTheCamera( this, (MyApp) (getApplication()));

        RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.CamHolder);


        android.widget.RelativeLayout.LayoutParams lp = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        // lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        // lp.height = 600;
        // lp.width  = 600;

        // lp.bottomMargin = 200;
        // lp.rightMargin = 200;
        // lp.rightMargin = 20;

        rl.addView(m_sc,lp);

        setupGraphView();

        m_Tv     = (TextView)this.findViewById(R.id.expLog);
        m_gpsLog = (TextView)this.findViewById(R.id.gpsLog);

        m_gpsLog.setMovementMethod(new ScrollingMovementMethod());

        // Set up Location
        setupSensors ();


    }

    private void setupGraphView()
    {

        aprHistoryPlot = (XYPlot)this.findViewById(R.id.plot);
        azimuthHistorySeries = new SimpleXYSeries("Path Map.");

        aprHistoryPlot.setRangeBoundaries(-180, 359, BoundaryMode.AUTO);
        aprHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.AUTO);

        aprHistoryPlot.addSeries(azimuthHistorySeries,
                new LineAndPointFormatter(
                        Color.rgb(255, 0, 0), null, null, null));
        aprHistoryPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);

        // aprHistoryPlot.setDomainStepValue(HISTORY_SIZE/10);
        // aprHistoryPlot.setLinesPerRangeLabel(3);
        aprHistoryPlot.setDomainLabel("");
        aprHistoryPlot.getDomainTitle().pack();
        aprHistoryPlot.setRangeLabel("");
        aprHistoryPlot.getRangeTitle().pack();

        aprHistoryPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
                setFormat(new DecimalFormat("####.#"));

        aprHistoryPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("####.#"));



        redrawer = new Redrawer(Arrays.asList(new Plot[]{aprHistoryPlot}), 100, false);



    }
    private void setupSensors () {
        mSensorsHandler = new SensorsHandler (this,m_sc) ;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        System.out.println("=== Creating Menu ====");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        System.out.format("=== Menu ID = [%d] ====\n", R.id.action_settings);

        toggleSlamMenu (menu.getItem(0));

        // MenuItem settings = menu.add(0, R.id.action_settings, 0, R.string.action_settings);
        //locationItem.setIcon(R.drawable.ac);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
        int id;
        id = item.getItemId();
        File f = null;

        System.out.format("=== Menu ID = [%d] [%d] ====\n", id, R.id.action_settings);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            System.out.println("=== Menu Clicked====");

            mSlamOn = !mSlamOn;
            toggleSlamMenu (item);
            CharSequence t = String.format(Locale.ENGLISH,item.getTitle().toString());
            Toast.makeText(this.getApplicationContext(),t,Toast.LENGTH_LONG).show();


        }
        if (id == R.id.action_save)
        {
            CharSequence t = String.format(Locale.ENGLISH,"Saving Experience");
            Toast.makeText(this.getApplicationContext(),t,Toast.LENGTH_SHORT).show();
            saveExpMap ();
        }

        return super.onOptionsItemSelected(item);

    }

    private void saveExpMap ()
    {
        Date d = new Date ();
        SimpleDateFormat fmt = new SimpleDateFormat ("yyyy_MM_dd_HH_mm_ss");
        String date = fmt.format(d);
        String fileName = "/ExpMap_" + date + ".csv";
        File f = new File( this.getApplication().getExternalFilesDir(null)+ fileName);

        FileOutputStream fos;

        try
        {
            fos = new FileOutputStream(f);



            ExpMap [] map = m_Slam.printExperienceMap();
            String line = "";
            for (int i = 0; i< map.length;i++)
            {
                line = String.format(Locale.ENGLISH, "%f,%f\n", map[i].x,map[i].y);
                fos.write(line.getBytes());
            }
            fos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            CharSequence t = String.format(Locale.ENGLISH,"Could Not Save Expereince");
            Toast.makeText(this.getApplicationContext(),t,Toast.LENGTH_SHORT).show();

            return;
        }

        CharSequence t = String.format(Locale.ENGLISH,"Save Expereince... Done !!");
        Toast.makeText(this.getApplicationContext(),t,Toast.LENGTH_LONG).show();


    }

    private void toggleSlamMenu (MenuItem item)
    {
        if (mSlamOn == true) {
            item.setTitle(R.string.slam_on);
        } else {
            item.setTitle(R.string.slam_off);
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        mSensorsHandler.startLocation();
        mSensorsHandler.startGyro();

        if (mStartTime == 0L) {
            mStartTime = SystemClock.uptimeMillis();
            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.postDelayed(mUpdateTimeTask, 1000);
        }
        redrawer.start();
    }

    @Override
    public void onStop() {
        redrawer.finish();
        super.onStop();

        mSensorsHandler.stopLocation();
        mSensorsHandler.stopGyro();
        mHandler.removeCallbacks(mUpdateTimeTask);
  }


}
