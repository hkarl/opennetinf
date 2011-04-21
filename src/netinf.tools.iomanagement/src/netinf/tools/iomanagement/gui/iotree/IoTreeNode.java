/*
 * Copyright (C) 2009,2010 University of Paderborn, Computer Networks Group
 * Copyright (C) 2009,2010 Christian Dannewitz <christian.dannewitz@upb.de>
 * Copyright (C) 2009,2010 Thorsten Biermann <thorsten.biermann@upb.de>
 * 
 * Copyright (C) 2009,2010 Eduard Bauer <edebauer@mail.upb.de>
 * Copyright (C) 2009,2010 Matthias Becker <matzeb@mail.upb.de>
 * Copyright (C) 2009,2010 Frederic Beister <azamir@zitmail.uni-paderborn.de>
 * Copyright (C) 2009,2010 Nadine Dertmann <ndertmann@gmx.de>
 * Copyright (C) 2009,2010 Michael Kionka <mkionka@mail.upb.de>
 * Copyright (C) 2009,2010 Mario Mohr <mmohr@mail.upb.de>
 * Copyright (C) 2009,2010 Felix Steffen <felix.steffen@gmx.de>
 * Copyright (C) 2009,2010 Sebastian Stey <sebstey@mail.upb.de>
 * Copyright (C) 2009,2010 Steffen Weber <stwe@mail.upb.de>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.tools.iomanagement.gui.iotree;

import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;

import org.apache.log4j.Logger;

/**
 * A node in the IO tree
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IoTreeNode {
   /** Log4j Logger component */
   private static final Logger log = Logger.getLogger(IoTreeNode.class);

   /** type of the node */
   private final IoTreeNodeType type;
   /** object attached to this node */
   private final Object carriedObject;
   /** text that is to displayed in the GUI */
   private final String displayedText;

   /** direct parent of this node */
   private final IoTreeNode parentNode;

   /**
    * @param type
    *           type for new node
    * @param carriedObject
    *           carried object for new node
    * @param parentNode
    *           parent node to attach to
    */
   public IoTreeNode(IoTreeNodeType type, Object carriedObject, IoTreeNode parentNode) {
      this.type = type;
      this.carriedObject = carriedObject;
      this.parentNode = parentNode;

      String displayedTextTemp = null;
      if (carriedObject instanceof InformationObject) {
         if (carriedObject.getClass().isInterface() || carriedObject.getClass().getInterfaces().length == 0) {
            displayedTextTemp = carriedObject.getClass().getSimpleName();
         } else {
            displayedTextTemp = carriedObject.getClass().getInterfaces()[0].getSimpleName();
         }
      }
      if (carriedObject instanceof Attribute) {
         displayedTextTemp = ((Attribute) (this.carriedObject)).getIdentification();
      }
      if (carriedObject instanceof IdentifierLabel) {
         IdentifierLabel label = ((IdentifierLabel) (this.carriedObject));
         displayedTextTemp = label.getLabelName() + " = " + label.getLabelValue();
      }
      if (carriedObject instanceof String) {
         displayedTextTemp = (String) carriedObject;
      }
      if (displayedTextTemp == null) {
         if (carriedObject == null) {
            if (type.alwaysShowAs() == null) {
               log.warn("no displayedText for null " + type);
            }
         } else {
            displayedTextTemp = carriedObject.toString();
         }
      }
      this.displayedText = displayedTextTemp;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (this == obj) {
         return true;
      }
      if (!(obj instanceof IoTreeNode)) {
         return false;
      }

      IoTreeNode otherNode = (IoTreeNode) obj;

      if (!(this.type == otherNode.type)) {
         return false;
      }
      if (this.type.checkOnlyType() && (this.type == otherNode.type)) {
         return true;
      }
      if (!(otherNode.displayedText.equals(this.displayedText))) {
         return false;
      }
      if (this.carriedObject == otherNode.carriedObject) {
         return true;
      }
      return this.carriedObject.equals(otherNode.carriedObject);
   }

   /**
    * returns the carried object itself
    * 
    * @return carried object
    */
   public Object getCarriedObject() {
      return this.carriedObject;
   }

   /**
    * returns a reference to the parent node of this node
    * 
    * @return parent node
    */
   public IoTreeNode getParentNode() {
      return this.parentNode;
   }

   /**
    * @return node type
    */
   public IoTreeNodeType getType() {
      return this.type;
   }

   @Override
   public String toString() {
      if (this.type.alwaysShowAs() != null) {
         return this.type.alwaysShowAs();
      }
      return this.displayedText;
   }

}
