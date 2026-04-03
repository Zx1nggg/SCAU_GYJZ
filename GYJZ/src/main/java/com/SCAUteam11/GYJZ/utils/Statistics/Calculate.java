package com.SCAUteam11.GYJZ.utils.Statistics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Calculate {
    public static Double calculateProgressPercentage(BigDecimal current, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        if (current == null) {
            return 0.0;
        }

        return current
                .multiply(new BigDecimal(100)) // 乘100
                .divide(target, 2, RoundingMode.HALF_UP) // 除以目标金额，保留两位小数，四舍五入
                .doubleValue();
    }

    public static Double calculateAverageDonation(BigDecimal current, Long count){
        if (current == null || current.compareTo(BigDecimal.ZERO) == 0){
            return 0.0;
        }
        if(count == 0){
            throw new RuntimeException("除数不为0");
        }
        return current
                .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

}
