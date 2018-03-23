package com.example.sudhir.ratslamlib;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;

/**
 * Created by sudhir on 10/3/18.
 */
public class SensorsHandler extends LocationCallback implements SensorEventListener {

    private Context mCtxt = null;
    private TextView m_gpsLog = null;

    public float mVelocity, mAngularVelocity, mTareAngularVelocity;
    public boolean m_bVelocityAvailable = false;
    private DecimalFormat df = null;

    private int mLastAccuracy = 0;

    private FusedLocationProviderClient mFusedLocationClient;

    public Location m_LastLocation, m_CurrLocation ;
    private LocationRequest mLocationRequest;
    private float mLastTime, mDeltaTime ;
    private int m_AngularVelocityInt;

    private ShowTheCamera m_sc;

    private SensorManager mSensorManager = null;

    private Sensor mGyro = null;

    private final WindowManager mWindowManager;

    public SensorsHandler (Activity act, ShowTheCamera sc )
    {
        df = new DecimalFormat("###.#");
        m_sc = sc;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient (act);
        createLocationRequest();

        mCtxt = act.getApplicationContext();
        mSensorManager = (SensorManager) mCtxt.getSystemService(Context.SENSOR_SERVICE);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


        m_LastLocation = null;
        m_CurrLocation = null;
        mLastTime = 0f;
        mDeltaTime  = 0f;
        mVelocity = 0f;
        m_AngularVelocityInt = 0;
        mAngularVelocity = 0;

        mTareAngularVelocity = 0;

        m_gpsLog = (TextView)act.findViewById(R.id.gpsLog);

        mWindowManager = act.getWindow().getWindowManager();

    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startGyro ()
    {
        System.out.format("=========== GYRO : STARTING =====\n" );
        if (mGyro == null)
        {
            System.out.format("=========== GYRO : NO GYRO !! =====\n" );
            return;
        }

        mSensorManager.registerListener(this,mGyro,SensorManager.SENSOR_DELAY_GAME);
    }

    public void stopGyro ()
    {
        System.out.format("=========== GYRO : STOPPING =====\n" );
        mSensorManager.unregisterListener(this);
    }

    public void startLocation ()
    {
        System.out.format("=========== LOCATION : STARTING =====\n" );
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,this, null);
    }

    public void stopLocation ()
    {
        System.out.format("=========== LOCATION : STOPPING =====\n" );
        mFusedLocationClient.removeLocationUpdates(this);

    }

    @Override
    public void onLocationAvailability (LocationAvailability locationAvailability)
    {
        System.out.format("=========== LOCATION : LocationAvailability  [ %b ]=====\n",locationAvailability.isLocationAvailable() );

    };

    @Override
    public void onLocationResult(LocationResult locationResult) {

        System.out.format("=========== LOCATION : RECEIVED =====\n" );

        m_bVelocityAvailable  = computeVariables ( locationResult );

        if ( m_bVelocityAvailable  )
        {
            System.out.format("=========== LOCATION : Velocity [%f] \n", mVelocity);
        }
        else
        {
            // Not Calling Slam
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float yaw = 0;

        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        if (sensorEvent.sensor == mGyro) {


            float orientation [] ;

            orientation = updateOrientation(sensorEvent.values);
            yaw = orientation [0];
            // System.out.format("Orientation: Azimuth [%f], Pitch [%f], Roll [%f] =====\n",orientation[0],orientation[1],orientation[2] );
        }


        // float x = sensorEvent.values[0];
        // float y = sensorEvent.values[1];
        // float z = sensorEvent.values[2];

        float ngVel = Float.parseFloat(df.format(yaw));


        if (mAngularVelocity == ngVel)
        {

        }
        else
        {
            mAngularVelocity = ngVel;

            // System.out.format("=========== GYRO : EVENT RECEIVED [%f]  =====\n", mAngularVelocity);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }

    private float[] updateOrientation (float[] rotationVector) {


        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        final int worldAxisForDeviceAxisX;
        final int worldAxisForDeviceAxisY;

        // Remap the axes as if the device screen was the instrument panel,
        // and adjust the rotation matrix for the device orientation.
        switch (mWindowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            default:
                worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
                break;
            case Surface.ROTATION_90:
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                break;
        }

        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                worldAxisForDeviceAxisY, adjustedRotationMatrix);

        // Transform rotation matrix into azimuth/pitch/roll
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        return orientation;

        // Convert radians to degrees
        // float pitch = orientation[1] * -57;
        // float roll = orientation[2] * -57;
        // float yaw = orientation[3] * -57;

    }

    private boolean computeVariables (LocationResult locationResult)
    {
        float currTime = 0;

        if (locationResult == null) {
            return false;
        }

        for (Location location : locationResult.getLocations()) {
            m_CurrLocation  = location;
        }

        if (m_LastLocation != null && (m_CurrLocation == m_LastLocation) )
        {
            return false;
        }

        if (m_LastLocation == null)
        {
            m_LastLocation = m_CurrLocation;
            mLastTime = m_CurrLocation.getElapsedRealtimeNanos();
            return false;
        }

        currTime = m_CurrLocation.getElapsedRealtimeNanos();

        mDeltaTime = (currTime  - mLastTime ) / (1000 * 1000 * 1000);
        mLastTime = currTime;

        if (m_CurrLocation.hasSpeed())
        {
            mVelocity = m_CurrLocation.getSpeed();
        }
        else
        {
            float distanceTravelled = m_CurrLocation.distanceTo(m_LastLocation);
            mVelocity = distanceTravelled / mDeltaTime;
        }

        m_LastLocation = m_CurrLocation;

        return true;
    };


}
