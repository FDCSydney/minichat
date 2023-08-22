package com.chat.minichat.managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.webrtc.ThreadUtils;

@SuppressLint("MissingPermission")
public class ProximitySensor implements SensorEventListener {
    private static final String TAG = ProximitySensor.class.getSimpleName();

    private final ThreadUtils.ThreadChecker threadChecker = new ThreadUtils.ThreadChecker();
    private final Runnable onSensorStateListener;
    private final SensorManager sensorManager;
    private Sensor proximitySensor = null;
    private boolean lastStateReportIsNear = false;

    private ProximitySensor(Context context, Runnable sensorStateListener) {
        onSensorStateListener = sensorStateListener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public static ProximitySensor create(Context context, Runnable sensorStateListener) {
        return new ProximitySensor(context, sensorStateListener);
    }

    public boolean start() {
        threadChecker.checkIsOnValidThread();
        if (!initDefaultSensor()) {
            return false;
        }
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        return true;
    }

    public void stop() {
        threadChecker.checkIsOnValidThread();
        if (proximitySensor == null) {
            return;
        }
        sensorManager.unregisterListener(this, proximitySensor);
    }

    public boolean sensorReportsNearState() {
        threadChecker.checkIsOnValidThread();
        return lastStateReportIsNear;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        threadChecker.checkIsOnValidThread();
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        threadChecker.checkIsOnValidThread();
        float distanceInCentimeters = event.values[0];
        lastStateReportIsNear = distanceInCentimeters < proximitySensor.getMaximumRange();
        if (onSensorStateListener != null) {
            onSensorStateListener.run();
        }
    }

    private boolean initDefaultSensor() {
        if (proximitySensor != null) {
            return true;
        }
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        return proximitySensor != null;
    }

    private void logProximitySensorInfo() {
        if (proximitySensor == null) {
            return;
        }
        StringBuilder info = new StringBuilder("Proximity sensor: ");
        info.append("name=").append(proximitySensor.getName());
        info.append(", vendor: ").append(proximitySensor.getVendor());
        info.append(", power: ").append(proximitySensor.getPower());
        info.append(", resolution: ").append(proximitySensor.getResolution());
        info.append(", max range: ").append(proximitySensor.getMaximumRange());
        info.append(", min delay: ").append(proximitySensor.getMinDelay());
        info.append(", type: ").append(proximitySensor.getStringType());
        info.append(", max delay: ").append(proximitySensor.getMaxDelay());
        info.append(", reporting mode: ").append(proximitySensor.getReportingMode());
        info.append(", isWakeUpSensor: ").append(proximitySensor.isWakeUpSensor());
    }
}
