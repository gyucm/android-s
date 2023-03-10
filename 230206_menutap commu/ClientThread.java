package org.techtown.tab;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientThread extends Thread {
    String serverIp;
    int serverPort = 5000;
    Socket socket = null; // 함수 적용을 위해 밖으로 뻄
    ClientThread(String strIp, int intPort){
        serverIp = strIp;
        serverPort = intPort;
    }
    @Override
    public void run() {
        try {
            socket = new Socket();
            //    System.out.println("[연결 요청]"); 콘솔로 출력
            displayText("[연결 요청]");
            Log.d("run()", "ip: " + serverIp + "port: " + serverPort);
            socket.connect(new InetSocketAddress(serverIp, serverPort));//접근 권한 넣어주기
            displayText("[연결 성공]");

            byte[] bytes = new byte[100];
            String message = null;
            InputStream is = socket.getInputStream();
            while (true) {
                int readByteCount = is.read(bytes);
                if (readByteCount <= 0)
                    break;
                message = new String(bytes, 0, readByteCount, "UTF-8");
                displayText("[데이터 받기 성공]: " + message);
                sendMainActivity(message);
            }
            is.close();
        } catch (Exception e) {
            displayText("서버가 중지되었습니다");
        }

        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e1) {
            }
        }
    }

    void stopClient(){
        if(socket != null && !socket.isClosed()){
            displayText("클라이언트 중지");
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    synchronized void sendData(String data) { // final data
        Thread sendThread = new Thread() {
            @Override
            public void run() {
                try {
                    byte[] bytes = data.getBytes("UTF-8");
                    OutputStream os = socket.getOutputStream();
                    os.write(bytes);
                    os.flush();
                    displayText("데이터 보내기 성공");
                } catch (Exception e) {
                    displayText("서버를 확인하세요");
                }
            }
        };
        sendThread.start();
    }
    synchronized void sendMainActivity (String text) { //메인으로 전송하는 함수
        Message message = MainActivity.mainHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("msg",text);
        message.setData(bundle);
        MainActivity.mainHandler.sendMessage(message);
    }
    synchronized void displayText (String text){ //문자열 출력 함수 생성
        Log.d("displayText", text);
    }
}