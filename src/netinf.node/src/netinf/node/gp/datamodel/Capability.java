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
package netinf.node.gp.datamodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import netinf.node.gp.messages.GPNetInfMessages.NICapability;
import netinf.node.gp.messages.GPNetInfMessages.NIProperty;
import netinf.node.gp.messages.GPNetInfMessages.NICapability.Builder;

/**
 * A {@link Capability} consists of a representative name {@link Capability#getName()} and a list of properties
 * {@link Capability#getProperties()}. The possibility exists that several properties share the same name within the list.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class Capability {

   private String name;
   private final List<Property> properties;

   public Capability() {
      properties = new ArrayList<Property>();
   }

   public Capability(NICapability niCapability) {
      this();

      fromProto(niCapability);
   }

   private void fromProto(NICapability niCapability) {
      name = niCapability.getName();

      List<NIProperty> propertiesList = niCapability.getPropertiesList();
      for (NIProperty niProperty : propertiesList) {
         this.addProperty(new Property(niProperty));
      }
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void removeProperty(Property property) {
      properties.remove(property);
   }

   /**
    * Removes all the {@link Property} instances where the name equals <code>propertyName</code>.
    * 
    * @param propertyName
    */
   public void removeProperty(String propertyName) {
      Iterator<Property> propertiesIterator = properties.iterator();

      while (propertiesIterator.hasNext()) {
         Property next = propertiesIterator.next();

         if (next.getName() != null && next.getName().equals(propertyName)) {
            propertiesIterator.remove();
         }
      }
   }

   public void addProperty(Property property) {
      properties.add(property);
   }

   public List<Property> getProperties() {
      return properties;
   }

   public NICapability toProto() {
      Builder builder = NICapability.newBuilder();

      builder.setName(name);

      for (Property property : properties) {
         builder.addProperties(property.toProto());
      }

      return builder.build();
   }

   @Override
   public String toString() {
      return "Capability [name=" + name + ", properties=" + properties + "]";
   }
}
