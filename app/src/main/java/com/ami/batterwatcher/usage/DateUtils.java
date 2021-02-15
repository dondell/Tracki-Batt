package com.ami.batterwatcher.usage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static String format(UsageStatsWrapper usageStatsWrapper) {

        //SimpleDateFormat.getDateInstance(DateFormat.SHORT);
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        return format.format(usageStatsWrapper.getUsageStats().getLastTimeUsed());
    }

    public static String formatTotTimeInForeground(UsageStatsWrapper usageStatsWrapper) {

        /*DateFormat format = new SimpleDateFormat("hh:mm:ss");
        return format.format(usageStatsWrapper.getUsageStats().getTotalTimeInForeground());*/
        long millis = usageStatsWrapper.getUsageStats().getTotalTimeInForeground();
       return   String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public static String formatStandardDateTime(long timeInMille) {

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        return format.format(timeInMille);
    }
}
