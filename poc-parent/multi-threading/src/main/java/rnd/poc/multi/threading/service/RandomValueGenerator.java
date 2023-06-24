package rnd.poc.multi.threading.service;

import java.math.BigDecimal;
import java.util.Random;

public class RandomValueGenerator {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    public static String randomString(int inclusiveBound) {
        Random random = new Random();

        StringBuilder stringBuilder = new StringBuilder();

        for (int i=0; i<=inclusiveBound; i++) {
            stringBuilder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return stringBuilder.toString();
    }

    public static long randomLong(int inclusiveBound) {
        Random random = new Random();

        return random.nextLong(inclusiveBound) + 1;
    }

    public static BigDecimal randomBigDecimal() {
        return BigDecimal.valueOf(randomLong(200000), 2);
    }
}
