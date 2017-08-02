package com.portscanner;

import com.portscanner.utilities.ProgressBar;

import java.io.IOException;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class main {

    private static InetAddress address = null;
    private static int portFrom;
    private static int portTo;
    private static LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();
    private static List<Integer> ports = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {

        processArgs(args);

        try {
            if (!address.isReachable(null, 255, 2000)) {
                System.out.println("Хост недоступен.");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Long timeStart = Calendar.getInstance().getTimeInMillis();

        for (int i = portFrom; i <= portTo ; i++) {
            int port = i;
            blockingQueue.add(() -> {
                try {
                    InetSocketAddress isa = new InetSocketAddress(address, port);
                    Socket socket = new Socket();
                    socket.connect(isa, 200);
                    //System.out.println(String.format("Порт %d открыт", port));
                    ports.add(port);
                    socket.close();

                }  catch (IOException e) {
                    //System.out.println(String.format("Порт %d закрыт", port));
                }
            });
        }

        int queueSize = blockingQueue.size();

        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(200, 256, 500,
                        TimeUnit.MILLISECONDS, blockingQueue );

        threadPoolExecutor.prestartAllCoreThreads();
        threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS);
        threadPoolExecutor.shutdown();



        while (true) {

            ProgressBar.print(queueSize,  threadPoolExecutor.getCompletedTaskCount());

            if (threadPoolExecutor.isTerminated()) {
                Long timeEnd  = Calendar.getInstance().getTimeInMillis();
                Calendar total = Calendar.getInstance();
                total.setTime(new Date(timeEnd - timeStart));
                System.out.println(timeEnd - timeStart);
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                System.out.println(formatter.format(new Date(timeEnd - timeStart)));
                System.out.println(String.format("Время выполнения %tH:%tM:%tS", total, total, total));



                System.out.println("Открытые порты:");
                for (int i = 0; i < ports.size() ; i++) {
                    System.out.println(ports.get(i));
                }
                break;
            }
        }

    }

    static void processArgs(String[] args) {
        if (args.length < 1) {
            usage();
            System.exit(1);
        }

        String host = args[0];
        try {
            address = InetAddress.getByName(host);
        } catch (IOException ioe) {
            System.out.println("Не верный адрес хоста.");
            System.exit(2);
        }

        System.out.println("Сканирование хоста "+host);

        portFrom = 0;
        portTo = 0x10000-1;

        if (args.length==2) {
            if (args[1].indexOf("-")>-1) {
                // range of ports pointed out
                String[] ports = args[1].split("-");
                try {
                    portFrom = Integer.parseInt(ports[0]);
                    portTo = Integer.parseInt(ports[1]);
                } catch (NumberFormatException nfe) {
                    System.out.println("Некорректные порты!");
                    System.exit(3);
                }
            } else {
                // one port pointed out
                try {
                    portFrom = Integer.parseInt(args[1]);
                    portTo = portFrom;
                } catch (NumberFormatException nfe) {
                    System.out.println("Некорректные порты!");
                    System.exit(3);
                }
            }
        }

    }

    static void usage() {
        System.out.println("Java Port Scanner usage: ");
        System.out.println("java Main host port");
        System.out.println("Examples:");
        System.out.println("java Main 192.168.1.1 1-1024");
        System.out.println("java Main 192.168.1.1 1099");
        System.out.println("java Main 192.168.1.1 (this scans all ports from 0 to 65535)");
    }
}
