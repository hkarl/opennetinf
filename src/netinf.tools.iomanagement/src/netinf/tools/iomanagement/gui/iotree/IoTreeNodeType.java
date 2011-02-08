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

import javax.swing.ImageIcon;

import netinf.tools.iomanagement.Constants;

/**
 * Possible Types of IO tree nodes
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public enum IoTreeNodeType {
   /** root node, contains attributes and identifier */
   IO_TXT(true, false, false, null, null, "IO", null),
   /** attributes node, contains attributes */
   IO_ATTRIBUTES_TXT(true, false, false, "SUBATTRIBUTE", Constants.getString("TREE_ATTRIBUTES"), "ATTRIBUTES", null),
   /** identifier node, contains identifier details */
   IO_IDENTIFIERDETAILS_TXT(
         true, false, false, "IDENTIFIERLABEL", Constants.getString("TREE_IDENTIFIERDETAILS"), "IDENTIFIER", null),
         /** attribute node */
         IO_ATTRIBUTE_DATA(false, true, true, "SUBATTRIBUTE", null, "ATTRIBUTE", null),
         /** identifier label node */
         IO_IDENTIFIERLABEL_DATA(false, true, true, null, null, "IDENTIFIERLABEL", Constants.getString("TREE_LABEL")),
         /** attribute value node */
         ATT_ATTVALUE_DATA(false, false, false, null, null, "ATTRIBUTEVALUE", Constants.getString("TREE_VALUE")),
         /** subattributes node, contains subattributes */
         ATT_SUBATTRIBUTES_TXT(false, false, false, null, Constants.getString("TREE_SUBATTRIBUTES"), "SUBATTRIBUTES", null),
         /** subattribute node */
         ATT_SUBATTRIBUTE_DATA(false, true, true, "SUBATTRIBUTE", null, "ATTRIBUTE", null),
         /** attribute purpose node */
         ATT_ATTPURPOSE_DATA(false, false, false, null, null, "ATTRIBUTEPURPOSE", Constants.getString("TREE_PURPOSE"));

   /** true iff only the type of the node is to be checked for equality */
   private final boolean checkOnlyType;
   /** is this node editable? */
   private final boolean isEditable;
   /** is this node deletable? */
   private final boolean isDeletable;
   /** the icon to show on the add button */
   private final String addableIcon;
   /** overrides the displayed text attribute of a node */
   private final String alwaysShowAs;
   /** the icon to show in the tree */
   private final String icon;
   /** prefix for displayed text */
   private final String prefix;

   /**
    * @param checkOnlyType
    *           true iff only the type of the node is to be checked for equality
    * @param isEditable
    *           is this node editable?
    * @param isDeletable
    *           is this node deletable?
    * @param addableIcon
    *           the icon to show on the add button
    * @param alwaysShowAs
    *           overrides the displayed text attribute of a node
    * @param icon
    *           the icon to show in the tree
    * @param prefix
    *           prefix for displayed text
    */
   IoTreeNodeType(boolean checkOnlyType, boolean isEditable, boolean isDeletable, String addableIcon, String alwaysShowAs,
         String icon, String prefix) {
      this.checkOnlyType = checkOnlyType;
      this.isEditable = isEditable;
      this.isDeletable = isDeletable;
      this.addableIcon = addableIcon;
      this.alwaysShowAs = alwaysShowAs;
      this.icon = icon;
      this.prefix = prefix;
   }

   /**
    * @return the text this node is always to be displayed as
    */
   public String alwaysShowAs() {
      return this.alwaysShowAs;
   }

   /**
    * @return true iff only the type of the node is to be checked
    */
   public boolean checkOnlyType() {
      return this.checkOnlyType;
   }

   /**
    * @return icon for objects that are addable to this tree node (original size)
    */
   public ImageIcon getAddableIcon() {
      if (this.addableIcon == null) {
         return null;
      }
      return Constants.getIcon(this.icon);
   }

   /**
    * @param size
    *           size of the icon (will be square)
    * @return icon for objects that are addable to this tree node
    */
   public ImageIcon getAddableIcon(int size) {
      if (this.addableIcon == null) {
         return null;
      }
      return new ImageIcon(getAddableIcon().getImage().getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH));
   }

   /**
    * @return icon for the tree (original size)
    */
   public ImageIcon getIcon() {
      return Constants.getIcon(this.icon);
   }

   /**
    * @param size
    *           size of the icon (will be square)
    * @return icon for the tree
    */
   public ImageIcon getIcon(int size) {
      return new ImageIcon(getIcon().getImage().getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH));
   }

   /**
    * @return prefix to be displayed before displayed text of the node
    */
   public String getPrefix() {
      return this.prefix;
   }

   /**
    * @return true iff the node is deletable
    */
   public boolean isDeletable() {
      return this.isDeletable;
   }

   /**
    * @return true iff the node is editable
    */
   public boolean isEditable() {
      return this.isEditable;
   }
}
