package com.collablearn.backend.util;

import java.util.concurrent.ThreadLocalRandom;

public final class OtpUtil {
    private OtpUtil() {
    }

    public static String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }
}
