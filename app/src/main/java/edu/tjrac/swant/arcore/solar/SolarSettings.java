package edu.tjrac.swant.arcore.solar;

/**
 * Created by wpc on 2018/5/31.
 */

class SolarSettings {
    private float orbitSpeedMultiplier = 1.0f;
    private float rotationSpeedMultiplier = 1.0f;

    public float getOrbitSpeedMultiplier() {
        return orbitSpeedMultiplier;
    }

    public void setOrbitSpeedMultiplier(float orbitSpeedMultiplier) {
        this.orbitSpeedMultiplier = orbitSpeedMultiplier;
    }

    public float getRotationSpeedMultiplier() {
        return rotationSpeedMultiplier;
    }

    public void setRotationSpeedMultiplier(float rotationSpeedMultiplier) {
        this.rotationSpeedMultiplier = rotationSpeedMultiplier;
    }
}
