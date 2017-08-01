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
    public static void main(String[] args) {

        String ip = args[0];
        int portFrom = 1;
        int portTo = 65535;
        LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();
        List<Integer> ports = new ArrayList<>();

//        try {
//
//            if(!InetAddress.getByName(ip).isReachable(null,255, 1000)) {
//                System.out.println("Хост недоступен.");
//                System.exit(1);
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }

        Long timeStart = Calendar.getInstance().getTimeInMillis();

        for (int i = portFrom; i <= portTo ; i++) {
            int port = i;
                blockingQueue.add(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InetSocketAddress isa = new InetSocketAddress(ip, port);
                            Socket socket = new Socket();
                            socket.connect(isa, 100);

                            if (socket.isConnected()) {
                                System.out.println(String.format("Порт %d открыт", port));
                                ports.add(port);
                                socket.close();
                            }

                        } catch (ConnectException e) {
                            //e.printStackTrace();
                        } catch (SocketTimeoutException e) {
                            System.out.println(String.format("Порт %d открыт", port));
                            ports.add(port);
                            // e.printStackTrace();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        }

        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(4, 8, 500,
                        TimeUnit.MILLISECONDS, blockingQueue );

        threadPoolExecutor.prestartAllCoreThreads();
        threadPoolExecutor.shutdown();

        while (true) {
            if (threadPoolExecutor.isTerminated()) {
                Long timeEnd  = Calendar.getInstance().getTimeInMillis();
                System.out.println(String.format("Затрачено %d мс.", (timeEnd - timeStart)));
                break;
            }
        }

    }
}
