package com.course;

import com.course.utils.FileUtil;
import com.course.utils.MenuUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Main {

    private static int treadCreateSizeGlobal;
    private static final Object treadWaiterIndexingEnd = new Object();

    static SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    static Index mIndex = new Index();
    static ServerSocket listener = null;

    public static void main(String args[]) throws IOException{
        int clientNumber = 0;

        //1) получаю общий список файлов
        List<File> filesTmpList = FileUtil.getFilesFromDir(System.getProperty("user.dir")+"\\resources\\datasets\\aclImdb\\train\\unsup\\", 0);

        //Початковий індекс згідно до варіанту: N / 50 * (V – 1)
        //Кінцевий індекс згідно до варіанту: N / 50 * V
        int startIndex = filesTmpList.size()/50*(11-1);
        int endIndex = filesTmpList.size()/50*(11);
        List<File> filesList = filesTmpList.subList(startIndex, endIndex);

        //2) спрашиваю сколько нужно потоков
        int treadSize =  MenuUtil.getHandlerSize();
        treadCreateSizeGlobal = treadSize;

        //3) запустить индексацию
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date) + " || Start indexes process...");

        for(int i=0; i<treadSize; i++){
            int pieceIndexStart = filesList.size()/treadSize*i;
            int pieceIndexEnd = filesList.size()/treadSize*(i+1);

            if(i==treadSize-1) pieceIndexEnd = filesList.size();
            int finalPieceIndexEnd = pieceIndexEnd;

            // разбиваю индексацияю на несколько потоков
            new Thread(() -> {
                List<File> pieceList = filesList.subList(pieceIndexStart, finalPieceIndexEnd);
                mIndex.buildIndex(pieceList);
                System.out.println("End indexes pies from "+pieceIndexStart+" to "+ (finalPieceIndexEnd-1) +" Size="+pieceList.size());

                //в конце каждого потока проверяем не последний ли он
                if(--treadCreateSizeGlobal <= 0){
                    synchronized (treadWaiterIndexingEnd){
                        treadWaiterIndexingEnd.notify();
                    }
                }
            }).start();
        }

        synchronized (treadWaiterIndexingEnd) {
            try {
                treadWaiterIndexingEnd.wait();
                date = new Date(System.currentTimeMillis());
                System.out.println(formatter.format(date) + " || All files are indexes!!");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Server is waiting to accept user...");

        try {
            listener = new ServerSocket(7777);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }



        try {
            while (true) {
                Socket socketOfServer = listener.accept();
                new ServiceThread(socketOfServer, clientNumber++).start();
            }
        } finally {
            listener.close();
        }

    }

    private static class ServiceThread extends Thread {

        private int clientNumber;
        private Socket socketOfServer;

        public ServiceThread(Socket socketOfServer, int clientNumber) {
            this.clientNumber = clientNumber;
            this.socketOfServer = socketOfServer;

            System.out.println("New connection with client# " + this.clientNumber + " at " + socketOfServer);
        }

        @Override
        public void run() {

            try {
                // Открыть поток ввода/вывода
                BufferedReader is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
                BufferedWriter os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));

                while (true) {
                    // Получить данные от клиента
                    String line = is.readLine();

                    // Если пользователь отправил QUIT Прервать связь
                    if (line.equals("QUIT")) {
                        os.write(">> CLIENT: QUIT");
                        os.newLine();
                        os.flush();
                        os.write(">> SERVER: OK");
                        os.newLine();
                        os.flush();
                        break;
                    }else {
                        //Отправить данные полученные от клиента
                        os.write(">> CLIENT [Search phrase]: " + line);
                        os.newLine();
                        os.flush();
                        os.write(">> SERVER: "+mIndex.find(line.toLowerCase(Locale.ROOT)));
                        os.newLine();
                        os.flush();
                    }
                }

            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

}