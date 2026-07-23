package com.vtstudio.fxbox.utils;

import java.util.Random;

public class RandomNumberGenerator {
    public static int generateRandomNumber() {
        Random random = new Random();
        int randomNumber;

        // Tạo một số ngẫu nhiên từ 0 đến 9999
        int randomValue = random.nextInt(10000);

        if (randomValue < 8000) {
            // 80% số nằm trong khoảng 1000 đến 100000
            randomNumber = random.nextInt(99001) + 1000;
        } else if (randomValue < 9800) {
            // 18% số nằm trong khoảng 100000 đến 400000
            randomNumber = random.nextInt(300001) + 100000;
        } else {
            // 2% số nằm trong khoảng 400000 đến 10000000
            randomNumber = random.nextInt(9600001) + 400000;
        }

        return randomNumber;
    }
}