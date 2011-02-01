package netinf.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.Communicator;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.RSGetPriorityResponse;
import netinf.common.utils.Utils;

public class TCPServerCommunicationTest implements AsyncReceiveHandler {

	private static final String PROPERTIES_PATH = "../configs/testing.properties";
	private static Injector injector;
	private static Properties properties;

	private static final String HOST = "127.0.0.1";

	private static final int LOOPS = 10;
	private NetInfMessage receivedMessage;

	private NetInfServer server;
	
	@BeforeClass
	public static void setup() {
		properties = Utils.loadProperties(PROPERTIES_PATH);
		injector = Guice.createInjector(new AccessTestModule(properties));
	}
	
	@Before
	public void initServer(){
		server = injector.getInstance(TCPServer.class);
	}
	
	@After
	public void stopServer() throws IOException{
		server.stop();
	}

	@Test
	public void setupTCPServer() throws IOException, NetInfCheckedException, InterruptedException {
		server.setAsyncReceiveHandler(this);
		server.start();

		int port = Integer.parseInt(properties.getProperty("access.tcp.port"));
		Communicator client = injector.getInstance(Communicator.class);
		client.setup(HOST, port);

		// Send and receive messages
		for (int i = 0; i < LOOPS; i++) {
			RSGetPriorityResponse message = new RSGetPriorityResponse(i);
			client.send(message);

			NetInfMessage receivedMessage = pollForIncomingMessage();
			assertTrue(receivedMessage instanceof RSGetPriorityResponse);
			assertEquals(
					((RSGetPriorityResponse) receivedMessage).getPriority(), i);
		}
	}

	@Test
	public void testTCPGetPort() throws IOException {
		int port = Integer.parseInt(properties.getProperty("access.tcp.port"));
		Assert.assertEquals(port, ((TCPServer)server).getPort());
	}

	@Test
	public void testTCPDescribe() throws IOException {
		Assert.assertTrue(server.describe().contains("TCP"));
	}

	@Test
	public void testTCPGetAddress() throws IOException, NetInfCheckedException {
		server.start();
		Assert.assertEquals(((TCPServer)server).getAddress(), "0.0.0.0/0.0.0.0");
	}

	@Test
	public void testTCPStop() throws NetInfCheckedException, InterruptedException, IOException {
		server.setAsyncReceiveHandler(this);
		server.start();

		Thread.sleep(100);
		server.stop();

		Assert.assertFalse(server.isRunning());
	}

	@Test(expected = NetInfCheckedException.class)
	public void testTCPStartWithError() throws NetInfCheckedException, InterruptedException, IOException {
		server.setAsyncReceiveHandler(this);
		server.start();

		Thread.sleep(100);
		// start again
		server.start();

		server.stop();
	}

	
	@Override
	public void receivedMessage(NetInfMessage message, Communicator communicator) {
		this.receivedMessage = message;
	}

	private NetInfMessage pollForIncomingMessage() throws InterruptedException {
		NetInfMessage returnValue = null;

		int i = 0;
		while (this.receivedMessage == null && i++ < 20) {
			Thread.sleep(100);
		}

		returnValue = this.receivedMessage;
		this.receivedMessage = null;
		return returnValue;
	}

}
