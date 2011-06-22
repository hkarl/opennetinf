package netinf.common.communication;

import java.util.Properties;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DatamodelTest;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.Utils;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class RemoteNodeConnectionTest {
   
   private static final String PROPERTIES_PATH = "../configs/testing.properties";
   public static final String NETINFNODE_PROPERTIES = "../configs/testing/netinfnode_testing.properties";

   private Injector injector;
   private Injector createInjector;
   private Properties properties;

   private MessageEncoder messageEncoder;
   private DatamodelFactory datamodelFactory;
   private DatamodelFactory datamodelFactoryN2;
   private Identifier testIdentifier;
   private Identifier testIdentifier2;
   private Identifier testIdentity;
   private InformationObject testIO;
   private InformationObject oldTestIO; // Only for the ESFEventMessage test

   @Before
   public void setup() {
      this.properties = Utils.loadProperties(PROPERTIES_PATH);
      this.injector = Guice.createInjector(new CommonTestModule(this.properties));

      this.messageEncoder = this.injector.getInstance(MessageEncoderProtobuf.class);
      this.datamodelFactory = this.injector.getInstance(DatamodelFactory.class);

      final Properties node2properties = Utils.loadProperties(NETINFNODE_PROPERTIES);
      // Guice.createInjector(new LogModule(node2properties));
      this.createInjector = Guice.createInjector(new SecurityModule(), new DatamodelImplModule(), new AbstractModule() {

         @Override
         protected void configure() {
            Names.bindProperties(binder(), node2properties);
            bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
         }
      });
      datamodelFactoryN2 = createInjector.getInstance(DatamodelFactory.class);

      IdentifierLabel testIdentifierLabel = this.datamodelFactory.createIdentifierLabel();
      testIdentifierLabel.setLabelName("Uni");
      testIdentifierLabel.setLabelValue("Paderborn");
      this.testIdentifier = this.datamodelFactory.createIdentifier();
      this.testIdentifier.addIdentifierLabel(testIdentifierLabel);

      IdentifierLabel testIdentifierLabel2 = this.datamodelFactory.createIdentifierLabel();
      testIdentifierLabel2.setLabelName("Universität");
      testIdentifierLabel2.setLabelValue("Moscow");
      this.testIdentifier2 = this.datamodelFactory.createIdentifier();
      this.testIdentifier2.addIdentifierLabel(testIdentifierLabel2);

      IdentifierLabel testIdentityLabel = this.datamodelFactory.createIdentifierLabel();
      testIdentityLabel.setLabelName("Chuck");
      testIdentityLabel.setLabelValue("Norris");
      this.testIdentity = this.datamodelFactory.createIdentifier();
      this.testIdentity.addIdentifierLabel(testIdentityLabel);

      // this.testIO = this.datamodelFactory.createInformationObject();
      // this.testIO.setIdentifier(this.testIdentifier);
      this.testIO = DatamodelTest.createDummyInformationObject(this.datamodelFactory);

      this.oldTestIO = this.datamodelFactory.createInformationObject();
      this.oldTestIO.setIdentifier(testIdentifier2);
   }

   protected Injector getInjector() {
      return this.injector;
   }

   /**
    * Testing the getIO method by passing an identifier to the getIO method
    */
   @Test
   public void testGetIOsByIdentifier() {
      RemoteNodeConnection rnc = this.createInjector.getInstance(RemoteNodeConnection.class);
      try {
         rnc.setHostAndPort("127.0.0.1", 5000);
         rnc.putIO(testIO);
         rnc.getIO(this.testIO.getIdentifier());
         rnc.tearDown();
      } catch (NetInfCheckedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}
