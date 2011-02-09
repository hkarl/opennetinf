package netinf.common.eventservice;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.module.LogModule;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.ESFFetchMissedEventsResponse;
import netinf.common.messages.ESFSubscriptionResponse;
import netinf.common.messages.ESFUnsubscriptionResponse;
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


public class AbstractEsfConnectorImpTest {

	
	 public static final String IDENTIFICATION_1 = DefinedAttributeIdentification.AMOUNT.getURI();
     public static final String IDENTIFICATION_2 = DefinedAttributeIdentification.PUBLIC_KEY.getURI();
     public static final String IDENTIFICATION_3 = DefinedAttributeIdentification.OWNER.getURI();

	
	 private static final String CONFIGS_TESTING_PROPERTIES = "../configs/testing.properties";
	 private MessageReceiver messageReceiver;
	 private MessageProcessorImp messageProcessor;
	 private LinkedBlockingQueue<ESFEventMessage> messageQueue;
     private Properties properties;
     private MockErrorCommunicator comm;
     private DatamodelFactory dmFactory;
     private Provider<MockErrorCommunicator> provider;
	 private Injector injector;

	 String port = "5000";
	 String host = "127.0.0.1";
	 AbstractEsfConnectorImp esfConnector;
	
	@Before
	public void setUp() throws Exception {
		
		 PropertyConfigurator.configure(Utils.loadProperties(CONFIGS_TESTING_PROPERTIES));
			
	      this.properties = Utils.loadProperties(CONFIGS_TESTING_PROPERTIES);
	      
	      injector = Guice.createInjector( new LogModule(properties), new SecurityModule(), 
	    		  new DatamodelImplModule(), new TestModule(),
		            new AbstractModule() {

		               @Override
		               protected void configure() {
		                  Names.bindProperties(binder(), properties);
		                  bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
		               }
		            }
	     
	      
	      );
	      
	      this.messageReceiver = this.injector.getInstance(MessageReceiver.class);
	      this.messageProcessor = this.injector.getInstance(MessageProcessorImp.class);
	      this.dmFactory = this.injector.getInstance(DatamodelFactory.class);
	//      this.provider = this.injector.getProvider(MockErrorCommunicator.class);
	    
	      this.messageQueue = new LinkedBlockingQueue<ESFEventMessage>();
	    
	      esfConnector = new AbstractEsfConnectorImp(dmFactory,
					messageReceiver, messageProcessor, host,port		
			);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRun() {
		
//		esfConnector.setProvider(provider);
		
	//	esfConnector.setCommunicatorProvider((Provider) provider);
		
		esfConnector.run();
		esfConnector.stop();
	}

	@Test
	public void testSystemReadyToHandleReceivedMessage() {
	
	}

	@Test
	public void testAbstractEsfConnectorImp() {
		
	}

	@Test
	public void testSetIdentityIdentifier() {
		
	}

	@Test
	public void testSetInitialSubscriptionInformation() {
	
	}

	@Test
	public void testTearDown() {
	
	}

	@Test
	public void testSendSubscription() {
		
	}

	@Test
	public void testSendUnsubscription() {
	
	}

}
