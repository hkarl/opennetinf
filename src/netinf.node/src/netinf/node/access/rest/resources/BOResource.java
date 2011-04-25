package netinf.node.access.rest.resources;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.node.api.impl.LocalNodeConnection;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Resource to retrieve a BO by requesting a NetInf identifier.
 * 
 * @author mmuehe
 *
 */
public class BOResource extends ServerResource {
   
   private static final Logger LOG = Logger.getLogger(BOResource.class);

   private NetInfNodeConnection nodeConnection;
   private DatamodelFactory datamodelFactory;
   private String hash;
   private String label;

   @Override
   protected void doInit() throws ResourceException {
      super.doInit();
      nodeConnection = (NetInfNodeConnection) getContext().getAttributes().get(
            LocalNodeConnection.class.getCanonicalName());
      datamodelFactory = (DatamodelFactory) getContext().getAttributes().get(
            DatamodelFactory.class.getCanonicalName());
      hash = (String) getRequest().getAttributes().get("hash");
      label = (String) getRequest().getAttributes().get("label");
   }

   
   @Get
   public Representation retrieveBO() {
      Identifier identifier = datamodelFactory.createIdentifierFromString(
            buildIdentifier(hash, label));
      
      InformationObject io = null;
      try {
         io = nodeConnection.getIO(identifier);
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
                  return new InputRepresentation(new BufferedInputStream(conn.getInputStream()),
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
      return new StringRepresentation("<h1>404</h1>", MediaType.TEXT_HTML);
   }
   
   private String buildIdentifier(String hash, String label) {
      return "ni:HASH_OF_PK=" + hash + "~"
            + "HASH_OF_PK_IDENT=SHA1~" 
            + "VERSION_KIND=UNVERSIONED~"
            + "UNIQUE_LABEL=" + label;
   }

}
