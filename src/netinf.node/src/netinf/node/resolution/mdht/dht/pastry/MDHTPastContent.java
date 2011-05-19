package netinf.node.resolution.mdht.dht.pastry;

import netinf.common.datamodel.InformationObject;
import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContentHandle;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.p2p.past.PastException;

public class MDHTPastContent implements PastContent {

   private static final long serialVersionUID = -1631388806403566496L;
   private Id id;
   private InformationObject informationObject;
   
   public MDHTPastContent(Id id, InformationObject io) {
      this.id = id;
      this.informationObject = io;
   }

   @Override
   public PastContent checkInsert(Id id, PastContent existingContent) throws PastException {
      return this;
   }

   @Override
   public PastContentHandle getHandle(Past local) {
      return new ContentHashPastContentHandle(local.getLocalNodeHandle(), getId());
   }

   @Override
   public Id getId() {
      return id;
   }

   @Override
   public boolean isMutable() {
      return !informationObject.getIdentifier().isVersioned();
   }

   public InformationObject getInformationObject() {
      return informationObject;
   }
}
