package com.portscanner.utilities;

public class ProgressBar {
    public static void print(int fullSize, long nowSize) {
        int percent = ((int) nowSize) * 100 / fullSize;
        String bar = buildBar(percent/10);

        StringBuilder builder = new StringBuilder();
        builder.append("|");
        builder.append(bar);
        builder.append(String.format(" %2d", percent));
        builder.append("%");
        builder.append("|");

        if (percent < 100) {
            System.out.print(builder.toString());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print("\r");
        }
        else {
            System.out.println(builder.toString());
        }

    }

    private static String buildBar (int countSharps) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i < countSharps) {
                builder.append("#");
            }
            else {
                builder.append(" ");
            }

        }
        return builder.toString();
    }
}
