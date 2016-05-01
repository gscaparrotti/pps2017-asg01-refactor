package com.android.gscaparrotti.bendermobile;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by gscap_000 on 01/05/2016.
 */
public class ServerInteractor {

    private Socket socket;

    public Object sendCommandAndGetResult(final String address, final int port, final String command) throws Exception {
        socket = new Socket();
        Object datas = new Object();
        socket.connect(new InetSocketAddress(address, port), 1000);
        socket.setSoTimeout(1000);
        final OutputStream os = socket.getOutputStream();
        final InputStream is = socket.getInputStream();
        final ObjectOutputStream output = new ObjectOutputStream(os);
        output.writeObject(command);
        final ObjectInputStream input = new ObjectInputStream(is);
        datas = input.readObject();
        return datas;
    }

    public void interactionEnded() throws IOException {
        socket.close();
    }
}
