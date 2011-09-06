package netinf.node.access.rest.resources;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.node.access.rest.RESTApplication;

import org.restlet.resource.ServerResource;

/**
 * Abstract resource that provides a NetInfNodeConnection and a DatamodelFactory.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public abstract class NetInfResource extends ServerResource {

   /**
    * Yields a connection to a NetInfNode.
    * 
    * @return A NetInfNodeConnection of the parent application
    */
   protected NetInfNodeConnection getNodeConnection() {
      return ((RESTApplication) getApplication()).getNodeConnection();
   }

   /**
    * Yields an implementation of a DatamodelFactory.
    * 
    * @return A concrete DatamodelFactory
    */
   protected DatamodelFactory getDatamodelFactory() {
      return ((RESTApplication) getApplication()).getDatamodelFactory();
   }

   /**
    * Yields an implementation of a DatamodelTranslator.
    * 
    * @return A DatamodelTranslator
    */
   protected DatamodelTranslator getDatamodelTranslator() {
      return ((RESTApplication) getApplication()).getDatamodelTranslator();
   }

   /**
    * Creates a NetInf Identifier given a number of plain Strings.
    * 
    * @param hashOfPK Hash of Public Key
    * @param hashOfPKIdent Hash Algorithm
    * @param versionKind Version Kind
    * @param uniqueLabel Unique Label
    * @param versionNumber Version Number
    * 
    * @return NetInf Identifier
    */
   protected Identifier createIdentifier(String hashOfPK, String hashOfPKIdent, String versionKind, String uniqueLabel,
         String versionNumber) {
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