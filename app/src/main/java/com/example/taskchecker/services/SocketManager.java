package com.example.taskchecker.services;

import com.example.taskchecker.services.UserApiService;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;

public class SocketManager {
    private static SocketManager instance;
    private Socket mSocket;

    private SocketManager() {
        try {
            IO.Options options = IO.Options.builder()
                    .setTimeout(9000)
                    .setReconnection(true)
                    .setReconnectionAttempts(Integer.MAX_VALUE)
                    .setReconnectionDelay(1000)
                    //.setTransports(new String[] { "websocket" }) // Force WebSocket transport
                    .build();
            mSocket = IO.socket(UserApiService.BASE_URL + "users", options);
            setupSocketEvents();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public Socket getSocket() {
        return mSocket;
    }

    public void connect() {
        mSocket.connect();
    }

    public void disconnect() {
        mSocket.disconnect();
    }

    private void setupSocketEvents() {
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Socket connected");
            }
        });

        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Socket disconnected");
            }
        });

        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Socket connect error: " + args[0]);
            }
        });


    }
}
