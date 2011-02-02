package netinf.common.eventservice;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;
import netinf.common.communication.Communicator;
import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.creator.ValidCreator;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.log.module.LogModule;
import netinf.common.messages.ESFEventMessage;
import netinf.common.security.SignatureAlgorithm;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.Utils;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Names;

public class AbstractEsfConnectorTest {


	public static final String IDENTIFICATION_1 = DefinedAttributeIdentification.AMOUNT.getURI();
    public static final String IDENTIFICATION_2 = DefinedAttributeIdentification.PUBLIC_KEY.getURI();
    public static final String IDENTIFICATION_3 = DefinedAttributeIdentification.OWNER.getURI();

	
	 private static final String CONFIGS_TESTING_PROPERTIES = "../configs/testing.properties";
	 private MessageReceiver messageReceiver;
	 private MessageProcessorImp messageProcessor;
	 private LinkedBlockingQueue<ESFEventMessage> messageQueue;
     private Properties properties;
     private Communicator comm;
     private DatamodelFactory dmFactory;
     private Provider<Communicator> provider;
	 private Injector injector;

	 String port = "5000";
	 String host = "127.0.0.1";
	 AbstractEsfConnectorImp esfConnector;
	
	@Before
	public void setUp() throws Exception {
	
		 PropertyConfigurator.configure(Utils.loadProperties(CONFIGS_TESTING_PROPERTIES));
		
	      this.properties = Utils.loadProperties(CONFIGS_TESTING_PROPERTIES);
	          
	      injector = Guice.createInjector(new LogModule(properties), new SecurityModule(), new DatamodelImplModule(),
		            new AbstractModule() {

		               @Override
		               protected void configure() {
		                  Names.bindProperties(binder(), properties);
		                  bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
		               }
		            });
	      
	           
	      this.messageReceiver = this.injector.getInstance(MessageReceiver.class);
	      this.messageProcessor = this.injector.getInstance(MessageProcessorImp.class);
	      this.dmFactory = this.injector.getInstance(DatamodelFactory.class);
	      this.provider = this.injector.getProvider(Communicator.class);
	     
	      this.messageQueue = new LinkedBlockingQueue<ESFEventMessage>();
	     
	      ValidCreator.setDatamodelFactory(this.injector.getInstance(DatamodelFactory.class));
	      ValidCreator.setSignatureAlgorithm(this.injector.getInstance(SignatureAlgorithm.class));
	        
	      esfConnector = new AbstractEsfConnectorImp(dmFactory,
					messageReceiver, messageProcessor, host, port		
			);
		
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	@Ignore
	public void testRun() {
		// the running is not yet successful.
		esfConnector.run();
		
	}

	@Test
	public void testSetCommunicatorProvider() {
		esfConnector.setCommunicatorProvider(provider);
	}

	@Test
	public void testSetIdentityIdentifier() {
		
    	Identifier identifier = dmFactory.createIdentifierFromString(
             "ni:HASH_OF_PK=a294ac791b2cc6ccb6e2554324d787b775448a78~HASH_OF_PK_IDENT=SHA1~VERSION_KIND=UNVERSIONED");	
		esfConnector.setIdentityIdentifier(identifier);
		
	}

	@Test
	public void testSetInitialSubscriptionInformation() {
		
		List<String> subscriptionIdentifications = new LinkedList<String>() ;
		subscriptionIdentifications.add(IDENTIFICATION_1);
		subscriptionIdentifications.add(IDENTIFICATION_2);
		subscriptionIdentifications.add(IDENTIFICATION_3);
		
		List<String> subscriptionQueries = new LinkedList<String>() ;
		subscriptionQueries.add("query_1");
		subscriptionQueries.add("query_2");
		subscriptionQueries.add("query_3");
	
	    List<Long> subscriptionExpireTimes = new LinkedList<Long>() ;
	    subscriptionExpireTimes.add((long) 1234);
	    subscriptionExpireTimes.add((long) 2345);
	    subscriptionExpireTimes.add((long) 1245);
	    
	    esfConnector.setInitialSubscriptionInformation(subscriptionIdentifications,
	    		subscriptionQueries, subscriptionExpireTimes);
	}

	@Test
	public void testTearDown() {
	}

	@Test
	public void testSystemReadyToHandleReceivedMessage() {
		
		
	}

	@Test
	public void testSendSubscription() {
		
		//esfConnector.sendSubscription(IDENTIFICATION_1, "query_1", 5000);
		
	}

	@Test
	public void testSendUnsubscription() {
		
		//esfConnector.sendUnsubscription(IDENTIFICATION_1);
	}
	
	@Test
	public void testSetMessageQueue(){
		
		messageReceiver.setMessageQueue(messageQueue);
		ESFEventMessage msg1 = new ESFEventMessage();

		messageReceiver.receivedMessage(msg1, comm);
	}
	
	@Test
	public void testMessageProcessor(){
		
		ESFEventMessage msg1 = new ESFEventMessage();
		ESFEventMessage msg2 = new ESFEventMessage();
		messageQueue.add(msg1);
		messageQueue.add(msg2);
		messageProcessor.setMessageQueue(messageQueue);
		Assert.assertEquals(true, messageProcessor.isWaiting());
	//	messageProcessor.run();
	}
}
