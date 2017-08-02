package com.portscanner;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class main {
    public static void main(String[] args) throws InterruptedException {


        int portFrom = 80;
        int portTo = 80;
        LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();
        List<Integer> ports = new ArrayList<>();

        try {

            if(!InetAddress.getByName(args[0]).isReachable(null,255, 2000)) {
                System.out.println("Хост недоступен.");
                System.exit(1);
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Long timeStart = Calendar.getInstance().getTimeInMillis();

        for (int i = portFrom; i <= portTo ; i++) {
            int port = i;
            blockingQueue.add(() -> {
                try {
                    InetSocketAddress isa = new InetSocketAddress(InetAddress.getByName(args[0]), port);
                    Socket socket = new Socket();
                    socket.connect(isa, 200);
                    System.out.println(String.format("Порт %d открыт", port));
                    ports.add(port);
                    socket.close();

                }  catch (IOException e) {
                    System.out.println(String.format("Порт %d закрыт", port));
                }
            });

        }

        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(200, 256, 500,
                        TimeUnit.MILLISECONDS, blockingQueue );

        threadPoolExecutor.prestartAllCoreThreads();
        threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS);
        threadPoolExecutor.shutdown();

        while (true) {
            if (threadPoolExecutor.isTerminated()) {
                Long timeEnd  = Calendar.getInstance().getTimeInMillis();
                System.out.println(String.format("Затрачено %d мс.", (timeEnd - timeStart)));

                for (int i = 0; i < ports.size() ; i++) {
                    System.out.println(ports.get(i));
                }
                break;
            }
        }

    }
}
