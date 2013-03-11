package org.dobots.robotalk.client.gui.robots;

public interface IAccelerometerListener
{
    public void onAccelerationChanged( float x, float y, float z, boolean tx );
//    public void onShake( float force );
}

