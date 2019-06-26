package com.ghdiri.abdallah;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * A base test class.
 */
public abstract class AbstractTest {

    protected static final String DEFAULT_USER = "username";
    protected static final String DEFAULT_PASSWORD = "password";

    protected void testInContext(int port, String username, String password,
                                 CheckedRunner initiator,
                                 CheckedConsumer<ChannelSftp> channelConsumer) throws Exception {
        // initiate sftp server contents
        initiator.run();

        //create the session
        Session session = createSession(port, username, password);
        ChannelSftp channel = openChannel(session);

        channelConsumer.accept(channel);

        channel.exit();
        session.disconnect();
    }

    protected void testInDefaultContext(SftpGateway gateway,
                                        CheckedRunner initiator,
                                        CheckedConsumer<ChannelSftp> channelConsumer) throws Exception {
        testInContext(gateway.getPort(), DEFAULT_USER, DEFAULT_PASSWORD,
                initiator, channelConsumer);
    }

    protected Session createSession(int port, String username, String password) throws Exception {

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, "localhost", port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();

        return session;
    }


    protected ChannelSftp openChannel(Session session) throws Exception {
        Channel channel = session.openChannel("sftp");
        channel.connect();
        return (ChannelSftp) channel;
    }

    protected void put(Session session, String path, String contents) throws Exception {

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        byte[] bytes = contents.getBytes(StandardCharsets.UTF_8);
        sftpChannel.put(new ByteArrayInputStream(bytes), path);
        sftpChannel.exit();
        session.disconnect();
    }

    public interface CheckedRunner {
        void run() throws Exception;
    }

    public interface CheckedConsumer<T>{
        void accept(T t) throws Exception;
    }

}
