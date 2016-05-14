package com.android.gscaparrotti.bendermobile;

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

    public Object sendCommandAndGetResult(final String address, final int port, final Object input) throws IOException, ClassNotFoundException {
        Object datas;
        if (!socket.isConnected()) {
            try {
                socket.connect(new InetSocketAddress(address, port), 1000);
                socket.setSoTimeout(1000);
            } catch (IOException e) {
                interactionEnded();
            }
        }
        final OutputStream os = socket.getOutputStream();
        final InputStream is = socket.getInputStream();
        final ObjectOutputStream outputStream = new ObjectOutputStream(os);
        outputStream.writeObject(input);
        final ObjectInputStream inputStream = new ObjectInputStream(is);
        datas = inputStream.readObject();
        return datas;
    }

    public void interactionEnded() throws IOException {
        socket.close();
        instance = null;
    }
}
