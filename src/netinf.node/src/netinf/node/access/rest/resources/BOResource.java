package netinf.node.access.rest.resources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.exceptions.NetInfCheckedException;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Resource to retrieve a BO by requesting a NetInf identifier.
 * 
 * @author PG NetInf 3, University of Paderborn
 *
 */
public class BOResource extends NetInfResource {
   
   private static final Logger LOG = Logger.getLogger(BOResource.class);

   private String hashOfPK;
   private String hashOfPKIdent;
   private String versionKind;
   private String uniqueLabel;
   private String versionNumber;

   /**
    * Initializes the context of a BOResource.
    */
   @Override
   protected void doInit() throws ResourceException {
      super.doInit();      
      hashOfPK = getQuery().getFirstValue("HASH_OF_PK", true);
      hashOfPKIdent = getQuery().getFirstValue("HASH_OF_PK_IDENT", true);
      uniqueLabel = getQuery().getFirstValue("UNIQUE_LABEL", true);
      versionKind = getQuery().getFirstValue("VERSION_KIND", true);
      versionNumber = getQuery().getFirstValue("VERSION_NUMBER", true);
   }

   /**
    * Handler for a GET-requests.
    * 
    * @return InputRepresentation if DO was found
    */
   @Get
   public Representation retrieveBO() {
      Identifier identifier = createIdentifier();
      
      InformationObject io = null;
      try {
         io = getNodeConnection().getIO(identifier);
      } catch (NetInfCheckedException e) {
         LOG.warn("Could not get IO", e);
      }
      
      if (io != null) {
         List<Attribute> locators = io.getAttributesForPurpose(
               DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
         if (!locators.isEmpty()) {
            URL url;
            for (Attribute locator: locators) {
               try {
                  url = new URL(locator.getValue(String.class));
                  URLConnection conn = url.openConnection();
                  return new InputRepresentation(conn.getInputStream(),
                        new MediaType(conn.getContentType()));
               } catch (MalformedURLException mue) {
                  LOG.warn("Malformed locator URL", mue);
                  continue;
               } catch (IOException ioe) {
                  LOG.warn("Could not open URL connection", ioe);
                  continue;
               }
            }
         }
      }
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
   }
   
   private Identifier createIdentifier() {
      Identifier identifier = getDatamodelFactory().createIdentifier();      
      // HASH_OF_PK
      IdentifierLabel hashLabel = getDatamodelFactory().createIdentifierLabel();
      hashLabel.setLabelName(DefinedLabelName.HASH_OF_PK.toString());
      hashLabel.setLabelValue(hashOfPK);      
      identifier.addIdentifierLabel(hashLabel);
      // HASH_OF_PK_IDENT
      IdentifierLabel hashIdentLabel = getDatamodelFactory().createIdentifierLabel();
      hashIdentLabel.setLabelName(DefinedLabelName.HASH_OF_PK_IDENT.toString());
      hashIdentLabel.setLabelValue(hashOfPKIdent.toUpperCase());
      identifier.addIdentifierLabel(hashIdentLabel);
      // VERSION_KIND
      IdentifierLabel kindLabel = getDatamodelFactory().createIdentifierLabel();
      kindLabel.setLabelName(DefinedLabelName.VERSION_KIND.toString());
      kindLabel.setLabelValue(versionKind.toUpperCase());
      identifier.addIdentifierLabel(kindLabel);
      // UNIQUE_LABEL
      if (uniqueLabel != null) {
         IdentifierLabel labelLabel = getDatamodelFactory().createIdentifierLabel();
         labelLabel.setLabelName(DefinedLabelName.UNIQUE_LABEL.toString());
         labelLabel.setLabelValue(uniqueLabel);
         identifier.addIdentifierLabel(labelLabel);
      }
      // VERSION_NUMBER
      if (versionKind.equals("VERSIONED")) {
         IdentifierLabel versionLabel = getDatamodelFactory().createIdentifierLabel();
         versionLabel.setLabelName(DefinedLabelName.VERSION_NUMBER.toString());
         versionLabel.setLabelValue(versionNumber);
         identifier.addIdentifierLabel(versionLabel);
      }      
      return identifier;
   }

}
