/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.node;

import java.util.Properties;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DefinedVersionKind;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.creator.ValidCreator;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.security.identity.IdentityManager;
import netinf.common.utils.Utils;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Creates all the default IOs that are necessary within netinf to present the scenarios.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class CreateDefaultIOs {

   private static final Logger LOG = Logger.getLogger(CreateDefaultIOs.class);

   public static final String APPLICATION_PROPERTIES = "../configs/createIOs.properties";

   private static Injector injector;
   private static RemoteNodeConnection remoteNodeConnection;
   private static DatamodelFactory datamodelFactory;
   private static IdentityManager identityManager;

   /**
    * Expects the standard node to be running.
    * 
    * @param args
    * @throws NetInfCheckedException
    */
   public static void main(String[] args) throws NetInfCheckedException {
      CreateIOsModule createIOsModule = new CreateIOsModule(APPLICATION_PROPERTIES);
      injector = Guice.createInjector(createIOsModule);
      final Properties properties = Utils.loadProperties(APPLICATION_PROPERTIES);

      remoteNodeConnection = (RemoteNodeConnection) injector.getInstance(NetInfNodeConnection.class);
      remoteNodeConnection.setSerializeFormat(SerializeFormat.RDF);
      datamodelFactory = injector.getInstance(DatamodelFactory.class);
      identityManager = injector.getInstance(IdentityManager.class);
      identityManager.setFilePath("../configs/Identities/privateKeyFile.pkf");

      // TODO: Ede: how about deleting optionally all the other stuff in our databases. Is this even possible somehow?

      // create second IdentityObject
      IdentityObject identity2 = ValidCreator.createValidIdentityObject(Utils.stringToPublicKey(properties
            .getProperty("publicKeyIdentity2")));
      remoteNodeConnection.putIO(identity2);

      // first get public key and create IdentityObject
      IdentityObject owner = ValidCreator.createValidIdentityObject(Utils.stringToPublicKey(properties
            .getProperty("publicKeyOwner")));

      /*
       * Attribute eMailAddress = datamodelFactory.createAttribute();
       * eMailAddress.setIdentification(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI());
       * eMailAddress.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.getAttributePurpose());
       * eMailAddress.setValue("pg-augnet2@lists.uni-paderborn.de"); owner.addAttribute(eMailAddress);
       */
      Attribute reader = datamodelFactory.createAttribute();
      reader.setIdentification(DefinedAttributeIdentification.READER.getURI());
      reader.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.getAttributePurpose());
      reader.setValue("ni:HASH_OF_PK=f4f7d1cad86a2829d7ddc5c85651d59e161d9170~HASH_OF_PK_IDENT=SHA1~VERSION_KIND=UNVERSIONED");
      Attribute authReaders = datamodelFactory.createAttribute();
      authReaders.setIdentification(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI());
      authReaders.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.getAttributePurpose());
      authReaders.setValue("empty");
      authReaders.addSubattribute(reader);
      Attribute anEmailAddress = datamodelFactory.createAttribute();
      anEmailAddress.setIdentification(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI());
      anEmailAddress.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.getAttributePurpose());
      anEmailAddress.setValue("myaddress@somewhere_secret.com");
      anEmailAddress.addSubattribute(authReaders);
      owner.addAttribute(anEmailAddress);
      remoteNodeConnection.putIO(owner);

      // createSomeIOs();
      createScenario2IOs(owner);
      createIOsForInFoxTestPage(owner);

      remoteNodeConnection.tearDown();
   }

   /**
    * Use these methods in order to create the appropriate IOs.
    * 
    * @throws NetInfCheckedException
    */
   public static void createSomeIOs() throws NetInfCheckedException {
      // Although this is not a create, all the methods to create information objects should look like that and be called from the
      // main method
      // remoteNodeConnection.getIO(datamodelFactory.createIdentifierFromString("ni:HASH_OF_PK=asfaaa"));
   }

   private static void createScenario2IOs(IdentityObject owner) throws NetInfCheckedException {

      // create Shop IOs
      createWalmartShop(owner);
      createFoodlandShop(owner);
      createTescoShop(owner);
      createFarmfoodsShop(owner);
      createTheHomeDepotShop(owner);
      createEuronicsShop(owner);

      // create ShoppingList IOs
      createShoppingListJack(owner);
      createShoppingListPeter(owner);
   }

   private static void createWalmartShop(IdentityObject owner) throws NetInfCheckedException {
      InformationObject io = createSimpleShop(owner, "Walmart", 51.702930, 8.768587);

      io = addShopProduct(io, "Fresh milk", 1);
      io = addShopProduct(io, "Bread", 27);

      remoteNodeConnection.putIO(io);
   }

   private static void createFoodlandShop(IdentityObject owner) throws NetInfCheckedException {
      InformationObject io = createSimpleShop(owner, "Foodland", 51.717304, 8.775802);

      io = addShopProduct(io, "Fresh milk", 20);

      remoteNodeConnection.putIO(io);
   }

   private static void createTescoShop(IdentityObject owner) throws NetInfCheckedException {
      InformationObject io = createSimpleShop(owner, "Tesco", 51.712469, 8.74621);

      io = addShopProduct(io, "Fresh milk", 5);
      io = addShopProduct(io, "Bread", 2);

      remoteNodeConnection.putIO(io);
   }

   private static void createFarmfoodsShop(IdentityObject owner) throws NetInfCheckedException {
      InformationObject io = createSimpleShop(owner, "Farmfoods", 51.717888, 8.749489);

      io = addShopProduct(io, "Fresh milk", 10);
      io = addShopProduct(io, "Bread", 20);

      remoteNodeConnection.putIO(io);
   }

   private static void createTheHomeDepotShop(IdentityObject owner) throws NetInfCheckedException {
      InformationObject io = createSimpleShop(owner, "The Home Depot", 51.704642, 8.777903);

      io = addShopProduct(io, "Electric bulb", 5);

      remoteNodeConnection.putIO(io);
   }

   private static void createEuronicsShop(IdentityObject owner) throws NetInfCheckedException {
      InformationObject io = createSimpleShop(owner, "Euronics", 51.704019, 8.770798);

      io = addShopProduct(io, "Washing machine", 3);

      remoteNodeConnection.putIO(io);
   }

   private static void createShoppingListJack(IdentityObject owner) throws NetInfCheckedException {
      InformationObject io = createSimpleShoppingList(owner, "Jack's ShoppingList");

      io = addProductListProduct(io, "Fresh milk");

      remoteNodeConnection.putIO(io);
   }

   private static void createShoppingListPeter(IdentityObject owner) throws NetInfCheckedException {
      InformationObject io = createSimpleShoppingList(owner, "Peter's ShoppingList");

      remoteNodeConnection.putIO(io);
   }

   private static InformationObject createSimpleShop(IdentityObject owner, String name, double latitude, double longitude) {
      InformationObject io = ValidCreator.createValidInformationObject(owner, DefinedVersionKind.UNVERSIONED, name);
      if (io == null) {
         LOG.error("Could not create Information Object.");
      }

      Attribute representsAttr = datamodelFactory.createAttribute(DefinedAttributeIdentification.REPRESENTS.getURI(), "Shop");
      representsAttr.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      io.addAttribute(representsAttr);

      Attribute nameAttr = datamodelFactory.createAttribute(DefinedAttributeIdentification.NAME.getURI(), name);
      nameAttr.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      io.addAttribute(nameAttr);

      Attribute latAttr = datamodelFactory.createAttribute(DefinedAttributeIdentification.GEO_LAT.getURI(), latitude);
      latAttr.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      io.addAttribute(latAttr);

      Attribute longAttr = datamodelFactory.createAttribute(DefinedAttributeIdentification.GEO_LONG.getURI(), longitude);
      longAttr.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      io.addAttribute(longAttr);

      return io;
   }

   private static InformationObject createSimpleShoppingList(IdentityObject owner, String name) {
      InformationObject io = ValidCreator.createValidInformationObject(owner, DefinedVersionKind.UNVERSIONED, name);
      if (io == null) {
         LOG.error("Could not create Information Object.");
      }

      Attribute representsAttr = datamodelFactory.createAttribute(DefinedAttributeIdentification.REPRESENTS.getURI(),
            "ShoppingList");
      representsAttr.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      io.addAttribute(representsAttr);

      Attribute nameAttr = datamodelFactory.createAttribute(DefinedAttributeIdentification.NAME.getURI(), name);
      nameAttr.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      io.addAttribute(nameAttr);

      return io;
   }

   private static InformationObject addShopProduct(InformationObject io, String product, int amount) {
      Attribute productAttr = datamodelFactory.createAttribute(DefinedAttributeIdentification.PRODUCT.getURI(), product);
      productAttr.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      Attribute amountAttr = datamodelFactory.createAttribute(DefinedAttributeIdentification.AMOUNT.getURI(), amount);
      amountAttr.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      productAttr.addSubattribute(amountAttr);
      io.addAttribute(productAttr);
      return io;
   }

   private static InformationObject addProductListProduct(InformationObject io, String product) {
      Attribute productAttr = datamodelFactory.createAttribute(DefinedAttributeIdentification.PRODUCT.getURI(), product);
      productAttr.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      io.addAttribute(productAttr);
      return io;
   }

   /**
    * Creates all IOs/DOs for the InFox testpage
    * 
    * @throws NetInfCheckedException
    */
   private static void createIOsForInFoxTestPage(IdentityObject testIdentity) throws NetInfCheckedException {

      // FIXME: Some error handling would be fine ...

      LOG.info("Creating IOs for InFox test page");

      // Firstly we create a DataObject with two http_url attributes

      InformationObject doTest = ValidCreator.createValidInformationObject("netinf.common.datamodel.DataObject", testIdentity,
            DefinedVersionKind.UNVERSIONED, "DOTEST");
      if (doTest == null) {
         LOG.error("Could not create Information Object.");
      }

      Attribute hashOfData = datamodelFactory.createAttribute();
      hashOfData.setIdentification(DefinedAttributeIdentification.HASH_OF_DATA.getURI());
      hashOfData.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      hashOfData.setValue("fb28a9a9c907dc11504f1e927cad7cdfd5a1f461");

      Attribute aHttpUrl1 = datamodelFactory.createAttribute();
      aHttpUrl1.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
      aHttpUrl1.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.getAttributePurpose());
      aHttpUrl1.setValue("http://10.10.10.5/image.png");

      Attribute aHttpUrl2 = datamodelFactory.createAttribute();
      aHttpUrl2.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
      aHttpUrl2.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.getAttributePurpose());
      aHttpUrl2.setValue("http://10.10.10.4/image.png");

      doTest.addAttribute(aHttpUrl1);
      doTest.addAttribute(aHttpUrl2);
      doTest.addAttribute(hashOfData);
      remoteNodeConnection.putIO(doTest);

      LOG.info("Information Object DOTEST created");

      // Secondly we create a DataObject for the integration test

      InformationObject gpTest = ValidCreator.createValidInformationObject("netinf.common.datamodel.DataObject", testIdentity,
            DefinedVersionKind.UNVERSIONED, "GPTEST");
      if (gpTest == null) {
         LOG.error("Could not create Information Object.");
      }

      Attribute aGpUrl = datamodelFactory.createAttribute();
      aGpUrl.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
      aGpUrl.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.getAttributePurpose());
      aGpUrl.setValue("gp:server");

      gpTest.addAttribute(aGpUrl);
      remoteNodeConnection.putIO(gpTest);

      LOG.info("Information Object for integration test created");

      // Lastly we create an InformationObject containing a HTTP_URL

      InformationObject ioTest = ValidCreator
            .createValidInformationObject(testIdentity, DefinedVersionKind.UNVERSIONED, "IOTEST");
      if (ioTest == null) {
         LOG.error("Could not create Information Object.");
      }

      Attribute fourwardUrl = datamodelFactory.createAttribute();
      fourwardUrl.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
      fourwardUrl.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.getAttributePurpose());
      fourwardUrl.setValue("http://www.4ward-project.eu");

      ioTest.addAttribute(fourwardUrl);
      remoteNodeConnection.putIO(ioTest);

      LOG.info("Information Object for integration test created");
   }

}
