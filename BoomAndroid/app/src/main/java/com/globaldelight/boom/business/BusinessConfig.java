package com.globaldelight.boom.business;

/**
 * Created by adarsh on 13/07/17.
 */

public class BusinessConfig {

    private static final int MS_PER_MIN = 60*1000;
    private static final int MS_PER_HOUR = 60 * MS_PER_MIN;
    private static final int MS_PER_DAY = 24 * MS_PER_HOUR;

    public long trialPeriod() {
        return 5 * MS_PER_MIN;
    };

    public long sharePeriod() {
        return 15 * MS_PER_MIN;
    };

    public long extendedSharePeriod() {
        return 2 * MS_PER_MIN;
    }

    public long initialPopupDelay() {
        return 1 * MS_PER_MIN;
    }

    public long reminderInterval() {
        return 1 * MS_PER_MIN;
    }

    public long purchaseReminderInterval() {
        return 5 * MS_PER_DAY;
    }

    public long videoRewardPeriod() {
        return 5 * MS_PER_MIN;
    }

    public long fullPricePeriod() {
        return 5 * MS_PER_MIN;
    }

    public long discountPeriod() {
        return 15 * MS_PER_MIN;
    }

}
