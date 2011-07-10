package netinf.node.transferDeluxe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;

/**
 * @author PG NetInf 3
 */
public class LocatorSelector implements Iterator<String> {

   private InformationObject io;
   private List<Attribute> locatorList;
   private Iterator<Attribute> locatorIterator;

   public LocatorSelector(InformationObject io) {
      this.io = io;
      locatorList = getLocatorList();
      locatorIterator = locatorList.iterator();
   }

   private List<Attribute> getLocatorList() {
      List<Attribute> result = new ArrayList<Attribute>();
      List<Attribute> locators = io.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      for (Attribute locator : locators) {
         if (locator.getIdentification() != DefinedAttributeIdentification.CHUNK.getURI()) {
            result.add(locator);
         }
      }
      return result;
   }

   @Override
   public boolean hasNext() {
      return locatorIterator.hasNext();
   }

   @Override
   public String next() {
      Attribute loc = locatorIterator.next();
      return loc.getValue(String.class);
   }

   @Override
   public void remove() {
      locatorIterator.remove();
   }

}