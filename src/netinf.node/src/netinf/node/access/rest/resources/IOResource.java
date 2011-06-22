package netinf.node.access.rest.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Resource to display an IO by requesting a NetInf identifier.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class IOResource extends NetInfResource {

   private static final Logger LOG = Logger.getLogger(IOResource.class);

   private String hashOfPK;
   private String hashOfPKIdent;
   private String uniqueLabel;
   private String versionKind;
   private String versionNumber;

   /**
    * Initializes the context of a IOResource.
    */
   @Override
   protected void doInit() {
      super.doInit();
      hashOfPK = getQuery().getFirstValue("HASH_OF_PK", true);
      hashOfPKIdent = getQuery().getFirstValue("HASH_OF_PK_IDENT", true);
      uniqueLabel = getQuery().getFirstValue("UNIQUE_LABEL", true);
      versionKind = getQuery().getFirstValue("VERSION_KIND", true);
      versionNumber = getQuery().getFirstValue("VERSION_NUMBER", true);
   }

   /**
    * Handler for GET-requests.
    * 
    * @return Simple String representation of an IO
    */
   @Get
   public Representation showIO() {
      InformationObject io = null;
      try {
         io = getNodeConnection().getIO(createIdentifier());
      } catch (NetInfCheckedException e) {
         LOG.warn("Could not create identifier from given labels");
      }

      if (io != null) {
         // convert to RDF-XML
         byte[] ioByteArray = io.serializeToBytes();
         InputStream is = new ByteArrayInputStream(ioByteArray);
         InputRepresentation iRep = new InputRepresentation(is, MediaType.APPLICATION_XML);
         return iRep;
      } else {
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      }
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
