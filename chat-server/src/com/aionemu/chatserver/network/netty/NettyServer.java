package com.aionemu.chatserver.network.netty;

import java.nio.ByteOrder;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.network.NetworkConfig;
import com.aionemu.chatserver.network.aion.ClientPacketHandler;
import com.aionemu.chatserver.network.gameserver.GsConnectionFactoryImpl;
import com.aionemu.chatserver.network.netty.pipeline.LoginToClientPipeLineFactory;
import com.aionemu.commons.network.NioServer;
import com.aionemu.commons.network.ServerCfg;

/**
 * @author ATracer, Neon
 */
public class NettyServer {

	private static final Logger log = LoggerFactory.getLogger(NettyServer.class);
	private static final NettyServer instance = new NettyServer();
	private final ChannelFactory aionClientChannelFactory;
	private final ChannelGroup aionClientChannelGroup;
	private final NioServer nioServer;

	private NettyServer() {
		aionClientChannelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(),
			NetworkConfig.NIO_READ_WRITE_THREADS + 1);
		aionClientChannelGroup = new DefaultChannelGroup(NettyServer.class.getName());
		aionClientChannelGroup.add(initChannel());

		log.info("Server listening on {} Port: {} for Aion Connections",
			NetworkConfig.CLIENT_SOCKET_ADDRESS.getAddress().isAnyLocalAddress() ? "all interfaces,"
				: "IP: " + NetworkConfig.CLIENT_SOCKET_ADDRESS.getAddress().getHostAddress(),
			NetworkConfig.CLIENT_SOCKET_ADDRESS.getPort());

		nioServer = new NioServer(NetworkConfig.NIO_READ_WRITE_THREADS,
			new ServerCfg(NetworkConfig.GAMESERVER_SOCKET_ADDRESS, "GS Connections", new GsConnectionFactoryImpl()));
		nioServer.connect(Executors.newSingleThreadExecutor());
	}

	public static NettyServer getInstance() {
		return instance;
	}

	private Channel initChannel() {
		ServerBootstrap bootstrap = new ServerBootstrap(aionClientChannelFactory);
		bootstrap.setPipelineFactory(new LoginToClientPipeLineFactory(new ClientPacketHandler()));
		bootstrap.setOption("child.bufferFactory", HeapChannelBufferFactory.getInstance(ByteOrder.LITTLE_ENDIAN));
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.setOption("child.reuseAddress", true);
		bootstrap.setOption("child.connectTimeoutMillis", 100);
		bootstrap.setOption("readWriteFair", true);
		return bootstrap.bind(NetworkConfig.CLIENT_SOCKET_ADDRESS);
	}

	public void shutdownAll() {
		aionClientChannelGroup.close().awaitUninterruptibly();
		aionClientChannelFactory.releaseExternalResources();
		nioServer.shutdown();
	}
}
