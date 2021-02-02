package com.ami.batterwatcher.util;

import android.os.BatteryManager;

public class BatteryUtil {
    public String getPlugTypeString(int plugged) {
        String plugType = "Unknown";

        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                plugType = "AC";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                plugType = "USB";
                break;
        }
        return plugType;
    }

    public String getHealthString(int health) {
        String healthString = "Unknown";
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthString = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthString = "Good Condition";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthString = "Over Voltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthString = "Over Heat";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                healthString = "Failure";
                break;
        }
        return healthString;
    }

    public String getStatusString(int status) {
        String statusString = "Unknown";

        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusString = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusString = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusString = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusString = "Not Charging";
                break;
        }
        return statusString;
    }

    /*
    Charging
    Very slow 0-100
    Slow 100-300
    Medium 300-600
    Fast 600-1000
    Very fast 1000+

    Discharging
    Very slow less than 100
    Slow 100-200
    Medium 200-300
    Fast 300-500
    Very fast  500+
     */
    public String getChargingAndDischargingSpeed(int mA) {
        if (mA > 1500) {
            return "Super fast";
        } else if (mA > 1000 && mA <= 1500) {//Charging
            return "Very fast";
        } else if (mA > 600 && mA <= 1000) {
            return "Fast";
        } else if (mA > 300 && mA <= 600) {
            return "Medium";
        } else if (mA > 100 && mA <= 300) {
            return "Slow";
        } else if (mA <= 100 && mA >= 0) {
            return "Very slow";
        } else if (mA < 0 && mA > -100) {//Discharging
            return "Very slow";
        } else if (mA <= -100 && mA >= -200) {
            return "Slow";
        } else if (mA < -200 && mA >= -300) {
            return "Medium";
        } else if (mA < -300 && mA >= -500) {
            return "Fast";
        } else if (mA < -500) {
            return "Very fast";
        }
        return "";
    }
}
