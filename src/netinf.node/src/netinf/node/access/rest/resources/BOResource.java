package netinf.node.access.rest.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.utils.DatamodelUtils;
import netinf.node.transferDeluxe.TransferDispatcher;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Resource to retrieve a BO by requesting a NetInf identifier.
 * 
 * @author PG NetInf 3, University of Paderborn
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
   protected void doInit() {
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
         List<Attribute> locators = io.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
         if (!locators.isEmpty()) {
            try {
               if (io instanceof DataObject) {
                  TransferDispatcher tsDispatcher = TransferDispatcher.getInstance();
                  
                  final InputStream inStream = tsDispatcher.getStream((DataObject) io);
                  MediaType mdType = new MediaType(DatamodelUtils.getContentType(io));

                  // return new InputRepresentation(inStream, mdType);
                  return new OutputRepresentation(mdType) {
                     @Override
                     public void write(OutputStream outStream) throws IOException {
                        try {
                           IOUtils.copy(inStream, outStream);
                        } finally {
                           IOUtils.closeQuietly(inStream);
                           IOUtils.closeQuietly(outStream);
                        }
                        
                     }
                  };
               }
            } catch (IOException ioe) {
               LOG.warn("Could not open URL connection", ioe);
               // continue;
            } catch (Exception e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
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
