package com.android.gscaparrotti.bendermobile;

import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by gscap_000 on 01/05/2016.
 */
public class ServerInteractor {

    private Socket socket;
    private static ServerInteractor instance;

    public static ServerInteractor getInstance() {
        if (instance == null) {
            instance = new ServerInteractor();
        }
        return instance;
    }

    private ServerInteractor() {
        socket = new Socket();
    }

    public Object sendCommandAndGetResult(final String address, final int port, final Object input) {
        Object datas;
        if (socket == null) {
            socket = new Socket();
        }
        if (!socket.isConnected()) {
            try {
                socket.connect(new InetSocketAddress(address, port), 1000);
                socket.setSoTimeout(1000);
            } catch (IOException e) {
                try {
                    datas = e;
                    interactionEnded();
                    return datas;
                } catch (IOException e1) {
                    datas = e1;
                    return datas;
                }
            }
        }
        try {
            final OutputStream os = socket.getOutputStream();
            final InputStream is = socket.getInputStream();
            final ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(input);
            final ObjectInputStream inputStream = new ObjectInputStream(is);
            datas = inputStream.readObject();
        } catch (IOException | ClassNotFoundException e2) {
            try {
                interactionEnded();
                datas = e2;
                return datas;
            } catch (IOException e3) {
                datas = e3;
                return datas;
            }
        }
        return datas;
    }

    public void interactionEnded() throws IOException {
        socket.close();
        socket = null;
        instance = null;
    }
}
