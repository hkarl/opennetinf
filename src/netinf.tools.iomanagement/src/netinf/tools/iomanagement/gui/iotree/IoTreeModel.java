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
package netinf.tools.iomanagement.gui.iotree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.tools.iomanagement.Constants;

import org.apache.log4j.Logger;

/**
 * Tree model backing up the IO tree
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IoTreeModel implements TreeModel {
   /** Log4j Logger component */
   private static final Logger log = Logger.getLogger(IoTreeModel.class);

   /** direct reference to the root node */
   private final IoTreeNode root;
   /** direct reference to the attributes node */
   private final IoTreeNode attributesNode;
   /** direct reference to the identifier node */
   private final IoTreeNode identifierNode;

   /** list of listeners for this model */
   private final List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();

   /**
    * @param io
    *           the InformationObject this model is for
    */
   public IoTreeModel(InformationObject io) {
      super();
      this.root = new IoTreeNode(IoTreeNodeType.IO_TXT, io, null);
      this.attributesNode = new IoTreeNode(IoTreeNodeType.IO_ATTRIBUTES_TXT, null, this.root);
      this.identifierNode = new IoTreeNode(IoTreeNodeType.IO_IDENTIFIERDETAILS_TXT, null, this.root);
   }

   /**
    * Add a node
    * 
    * @param nodeToAdd
    *           node that is to be added
    */
   public void add(IoTreeNode nodeToAdd) {
      log.trace(Constants.LOG_ENTER);

      TreeModelEvent event = null;

      if (nodeToAdd.getType() == IoTreeNodeType.IO_IDENTIFIERLABEL_DATA) {
         IdentifierLabel labelToAdd = (IdentifierLabel) nodeToAdd.getCarriedObject();
         if (io().getIdentifier().getIdentifierLabel(labelToAdd.getLabelName()) != null) {
            log.warn("tried to add Identifier Label " + labelToAdd.getLabelName() + " twice");
            return;
         }
         io().getIdentifier().addIdentifierLabel(labelToAdd);
         int[] childIndices = { io().getIdentifier().getIdentifierLabels().indexOf(labelToAdd) };
         event = new TreeModelEvent(this.root, pathFromIo(this.identifierNode), childIndices, null);
      }

      if (nodeToAdd.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA) {
         Attribute attributeToAdd = (Attribute) nodeToAdd.getCarriedObject();
         io().addAttribute(attributeToAdd);
         int[] childIndices = { io().getAttributes().indexOf(attributeToAdd) };
         event = new TreeModelEvent(this.root, pathFromIo(this.attributesNode), childIndices, null);
      }

      if (nodeToAdd.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
         Attribute attributeToAdd = (Attribute) nodeToAdd.getCarriedObject();
         Attribute parentAttribute = (Attribute) nodeToAdd.getParentNode().getCarriedObject();
         boolean firstSubattribute = parentAttribute.getSubattributes().isEmpty();
         parentAttribute.addSubattribute(attributeToAdd);
         if (firstSubattribute) {
            int[] childIndices = { 2 };
            event = new TreeModelEvent(this.root, pathFromIo(nodeToAdd.getParentNode()), childIndices, null);
         } else {
            int[] childIndices = { parentAttribute.getSubattributes().indexOf(attributeToAdd) };
            event = new TreeModelEvent(this.root, pathFromIo((IoTreeNode) getChild(nodeToAdd.getParentNode(), 2)), childIndices,
                  null);
         }

      }

      if (event != null) {
         for (TreeModelListener listener : this.treeModelListeners) {
            listener.treeNodesInserted(event);
         }
      }

      log.trace(Constants.LOG_EXIT);
   }

   /*
    * (non-Javadoc)
    * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
    */
   @Override
   public void addTreeModelListener(TreeModelListener arg0) {
      log.trace(Constants.LOG_ENTER);
      this.treeModelListeners.add(arg0);
      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Gets the tree node that represents the IO attribute at a specific position
    * 
    * @param position
    *           position in attribute list
    * @return IoTreeNode respresenting that Attribute
    */
   private IoTreeNode attributeNode(int position) {
      log.trace(Constants.LOG_ENTER);
      IoTreeNode node = new IoTreeNode(IoTreeNodeType.IO_ATTRIBUTE_DATA, io().getAttributes().get(position), this.attributesNode);
      log.trace(Constants.LOG_EXIT);
      return node;
   }

   /**
    * Gets the tree node that represents the purpose of a given attribute
    * 
    * @param attributeNode
    *           node containing the attribute
    * @return node containing the purpose
    */
   private IoTreeNode attributePurposeNode(IoTreeNode attributeNode) {
      log.trace(Constants.LOG_ENTER);
      IoTreeNode node = new IoTreeNode(IoTreeNodeType.ATT_ATTPURPOSE_DATA, ((Attribute) attributeNode.getCarriedObject())
            .getAttributePurpose(), attributeNode);
      log.trace(Constants.LOG_EXIT);
      return node;
   }

   /**
    * Gets the tree node that represents the raw value of a given attribute
    * 
    * @param attributeNode
    *           node containing the attribute
    * @return node containing the raw value
    */
   private IoTreeNode attributeValueRawNode(IoTreeNode attributeNode) {
      log.trace(Constants.LOG_ENTER);
      IoTreeNode node = new IoTreeNode(IoTreeNodeType.ATT_ATTVALUE_DATA, ((Attribute) attributeNode.getCarriedObject())
            .getValueRaw(), attributeNode);
      log.trace(Constants.LOG_EXIT);
      return node;
   }

   /**
    * deletes a given node from the model
    * 
    * @param node
    *           node to delete
    */
   public void delete(IoTreeNode node) {
      log.trace(Constants.LOG_ENTER);

      TreeModelEvent event = null;

      // delete IdentifierLabel
      if (node.getType() == IoTreeNodeType.IO_IDENTIFIERLABEL_DATA) {
         log.debug("deleting IdentifierLabel " + node.toString());
         event = deletionEvent(node);
         IdentifierLabel label = (IdentifierLabel) node.getCarriedObject();
         io().getIdentifier().removeIdentifierLabel(label);
      }

      // delete Attribute
      if (node.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA || node.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
         Attribute attribute = (Attribute) node.getCarriedObject();
         log.debug("deleting Attribute " + node.toString());
         event = deletionEvent(node);
         if (node.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA) {
            attribute.getInformationObject().removeAttribute(attribute);
         }
         if (node.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
            attribute.getParentAttribute().removeSubattribute(attribute);
         }
      }

      // send the generated event
      if (event != null) {
         for (TreeModelListener listener : this.treeModelListeners) {
            listener.treeNodesRemoved(event);
         }
      }

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * generate a deletion event to send to listeners
    * 
    * @param node
    *           deleted node
    * @return event that represents the deletion
    */
   private TreeModelEvent deletionEvent(IoTreeNode node) {
      log.trace(Constants.LOG_ENTER);

      // event for IdentifierLabel deletion
      if (node.getType() == IoTreeNodeType.IO_IDENTIFIERLABEL_DATA) {
         Object[] pathElements = { this.root, this.identifierNode };
         TreePath path = new TreePath(pathElements);
         int[] childIndexArray = { getIndexOfChild(this.identifierNode, node) };
         Object[] childArray = { node };
         TreeModelEvent event = new TreeModelEvent(this.root, path, childIndexArray, childArray);
         log.trace(Constants.LOG_EXIT);
         return event;
      }

      // event for IO Attribute deletion
      if (node.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA) {
         Object[] pathElements = { this.root, this.attributesNode };
         TreePath path = new TreePath(pathElements);
         int[] childIndexArray = { getIndexOfChild(this.attributesNode, node) };
         Object[] childArray = { node };
         TreeModelEvent event = new TreeModelEvent(this.root, path, childIndexArray, childArray);
         log.trace(Constants.LOG_EXIT);
         return event;
      }

      // event for Subattribute deletion
      if (node.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
         List<IoTreeNode> path = new ArrayList<IoTreeNode>();
         int childIndex = -1;
         boolean lastSubattribute = false;

         IoTreeNode currentNode = node;
         while (currentNode != null) {
            if (currentNode != node) { // NOPMD by pgaugnet2 on 16.03.10 14:25
               path.add(currentNode);
            } else {
               lastSubattribute = ((Attribute) currentNode.getCarriedObject()).getParentAttribute().getSubattributes().size() == 1;
               childIndex = ((Attribute) currentNode.getCarriedObject()).getParentAttribute().getSubattributes().indexOf(
                     currentNode.getCarriedObject());
            }
            currentNode = currentNode.getParentNode();
         }

         Collections.reverse(path);

         if (lastSubattribute) {
            int[] childIndexArray = { 2 };
            Object[] childArray = { node.getParentNode() };
            path.remove(path.size() - 1);
            log.trace(Constants.LOG_EXIT);
            return new TreeModelEvent(this.root, path.toArray(), childIndexArray, childArray);
         }
         int[] childIndexArray = { childIndex };
         Object[] childArray = { node };
         log.trace(Constants.LOG_EXIT);
         return new TreeModelEvent(this.root, path.toArray(), childIndexArray, childArray);

      }

      log.debug("falling through checks - deletionEvent for " + node.getType() + ":" + node.toString());
      log.trace(Constants.LOG_EXIT);
      return null;
   }

   /**
    * Multi-purpose function to change contents of a node
    * 
    * @param editNode
    *           node to edit
    * @param newName
    *           new name for object (attribute identification/identifierlabel name)
    * @param newValue
    *           new value for object (attribute/identifierlabel value)
    * @param newPurpose
    *           new purpose for object (attribute purpose)
    */
   public void edit(IoTreeNode editNode, String newName, Object newValue, String newPurpose) {
      log.trace(Constants.LOG_ENTER);

      if (editNode.getType() == IoTreeNodeType.IO_IDENTIFIERLABEL_DATA) {
         IdentifierLabel label = (IdentifierLabel) editNode.getCarriedObject();
         io().getIdentifier().removeIdentifierLabel(label);
         label.setLabelName(newName);
         label.setLabelValue((String) newValue);
         io().getIdentifier().addIdentifierLabel(label);
         for (TreeModelListener listener : this.treeModelListeners) {
            listener.treeStructureChanged(new TreeModelEvent(this.root, pathFromIo(this.identifierNode), null, null)); // NOPMD by
            // pgaugnet2
            // on
            // 16.03.10
            // 14:25
         }
      }
      if (editNode.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA || editNode.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
         Attribute editAttribute = (Attribute) editNode.getCarriedObject();
         if (editNode.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA) {
            io().removeAttribute(editAttribute);
         }
         if (editNode.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
            ((Attribute) editNode.getParentNode().getCarriedObject()).removeSubattribute(editAttribute);
         }

         editAttribute.setIdentification(newName);
         editAttribute.setValue(newValue);
         editAttribute.setAttributePurpose(newPurpose);

         if (editNode.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA) {
            io().addAttribute(editAttribute);
         }
         if (editNode.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
            ((Attribute) editNode.getParentNode().getCarriedObject()).addSubattribute(editAttribute);
         }
         for (TreeModelListener listener : this.treeModelListeners) {
            listener.treeStructureChanged(new TreeModelEvent(this.root, pathFromIo(editNode.getParentNode()), null, null)); // NOPMD
            // by
            // pgaugnet2
            // on
            // 16.03.10
            // 14:25
         }
      }
      log.trace(Constants.LOG_EXIT);
   }

   /*
    * (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
    */
   @Override
   public Object getChild(Object nodeObject, int position) {
      log.trace(Constants.LOG_ENTER);

      IoTreeNode node = (IoTreeNode) nodeObject;

      // Root node
      if (node == this.root) {
         if (position == 0) {
            log.trace(Constants.LOG_EXIT);
            return this.identifierNode;
         }
         if (position == 1) {
            log.trace(Constants.LOG_EXIT);
            return this.attributesNode;
         }
      }

      // Attributes node
      if (node == this.attributesNode) {
         IoTreeNode retNode = attributeNode(position);
         log.trace(Constants.LOG_EXIT);
         return retNode;
      }

      // Identifier node
      if (node == this.identifierNode) {
         IoTreeNode retNode = identifierLabelNode(position);
         log.trace(Constants.LOG_EXIT);
         return retNode;
      }

      // Attribute node
      if (node.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA || node.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
         if (position == 0) {
            IoTreeNode retNode = attributeValueRawNode(node);
            log.trace(Constants.LOG_EXIT);
            return retNode;
         }
         if (position == 1) {
            IoTreeNode retNode = attributePurposeNode(node);
            log.trace(Constants.LOG_EXIT);
            return retNode;
         }
         if (position == 2) {
            IoTreeNode retNode = subattributesNode(node);
            log.trace(Constants.LOG_EXIT);
            return retNode;
         }
      }

      // Subattributes node
      if (node.getType() == IoTreeNodeType.ATT_SUBATTRIBUTES_TXT) {
         IoTreeNode retNode = subattributeNode(node, position);
         log.trace(Constants.LOG_EXIT);
         return retNode;
      }

      log.warn("falling through checks - getChild " + position + " for " + node.getType() + ":" + node.toString());
      log.trace(Constants.LOG_EXIT);
      return null;
   }

   /*
    * (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
    */
   @Override
   public int getChildCount(Object nodeObject) {
      log.trace(Constants.LOG_ENTER);

      IoTreeNode node = (IoTreeNode) nodeObject;

      // Root node
      if (node == this.root) {
         log.trace(Constants.LOG_EXIT);
         return 2;
      }

      // Attributes node
      if (node == this.attributesNode) {
         int retVal = io().getAttributes().size();
         log.trace(Constants.LOG_EXIT);
         return retVal;
      }

      // Identifier details node
      if (node == this.identifierNode) {
         int retVal = io().getIdentifier().getIdentifierLabels().size();
         log.trace(Constants.LOG_EXIT);
         return retVal;
      }

      // Attribute node
      if (node.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA || node.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
         Attribute attribute = (Attribute) node.getCarriedObject();
         int retVal = 2 + (attribute.getSubattributes().isEmpty() ? 0 : 1);
         log.trace(Constants.LOG_EXIT);
         return retVal;
      }

      // Subattributes node
      if (node.getType() == IoTreeNodeType.ATT_SUBATTRIBUTES_TXT) {
         Attribute attribute = (Attribute) node.getCarriedObject();
         int retVal = attribute.getSubattributes().size();
         log.trace(Constants.LOG_EXIT);
         return retVal;
      }

      log.warn("falling through checks - childCount for " + node.getType() + ":" + node.toString());
      log.trace(Constants.LOG_EXIT);
      return -1;
   }

   /*
    * (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
    */
   @Override
   public int getIndexOfChild(Object parentObject, Object childObject) {
      log.trace(Constants.LOG_ENTER);

      IoTreeNode parent = (IoTreeNode) parentObject;
      IoTreeNode child = (IoTreeNode) childObject;

      // Root parent ...
      if (parent == this.root) {
         // ... identifier details child
         if (child == this.identifierNode) {
            log.trace(Constants.LOG_EXIT);
            return 0;
         }
         // ... attributes child
         if (child == this.attributesNode) {
            log.trace(Constants.LOG_EXIT);
            return 1;
         }
      }

      // Attributes parent
      if (parent == this.attributesNode) {
         int retVal = io().getAttributes().indexOf(child.getCarriedObject());
         log.trace(Constants.LOG_EXIT);
         return retVal;
      }

      // Identifier details parent
      if (parent == this.identifierNode) {
         int retVal = io().getIdentifier().getIdentifierLabels().indexOf(child.getCarriedObject());
         log.trace(Constants.LOG_EXIT);
         return retVal;
      }

      // Attribute parent ...
      if (parent.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA || parent.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
         // ... value child
         if (child.getType() == IoTreeNodeType.ATT_ATTVALUE_DATA) {
            log.trace(Constants.LOG_EXIT);
            return 0;
         }
         // ... attribute purpose child
         if (child.getType() == IoTreeNodeType.ATT_ATTPURPOSE_DATA) {
            log.trace(Constants.LOG_EXIT);
            return 1;
         }
         // subattributes child
         if (child.getType() == IoTreeNodeType.ATT_SUBATTRIBUTES_TXT) {
            log.trace(Constants.LOG_EXIT);
            return 2;
         }
      }

      if (parent.getType() == IoTreeNodeType.ATT_SUBATTRIBUTES_TXT) {
         Attribute attribute = (Attribute) parent.getCarriedObject();
         int retVal = attribute.getSubattributes().indexOf(child.getCarriedObject());
         log.trace(Constants.LOG_EXIT);
         return retVal;
      }

      log.warn("falling through checks - indexOfChild for " + parent.getType() + ":" + parent.toString() + " / "
            + child.getType() + ":" + child.toString());
      log.trace(Constants.LOG_EXIT);
      return -1;
   }

   /*
    * (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getRoot()
    */
   @Override
   public Object getRoot() {
      log.trace(Constants.LOG_ENTER);
      log.trace(Constants.LOG_EXIT);
      return this.root;
   }

   /**
    * Gets a node that represents the identifier label at a certain position
    * 
    * @param position
    *           position in label list
    * @return node representing that label
    */
   private IoTreeNode identifierLabelNode(int position) {
      log.trace(Constants.LOG_ENTER);
      IoTreeNode node = new IoTreeNode(IoTreeNodeType.IO_IDENTIFIERLABEL_DATA, io().getIdentifier().getIdentifierLabels().get(
            position), this.identifierNode);
      log.trace(Constants.LOG_EXIT);
      return node;
   }

   /**
    * Convenience method to access the IO
    * 
    * @return the IO saved in this model
    */
   private InformationObject io() {
      log.trace(Constants.LOG_ENTER);
      InformationObject io = (InformationObject) this.root.getCarriedObject();
      log.trace(Constants.LOG_EXIT);
      return io;
   }

   /*
    * (non-Javadoc)
    * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
    */
   @Override
   public boolean isLeaf(Object nodeObject) {
      log.trace(Constants.LOG_ENTER);

      IoTreeNode node = (IoTreeNode) nodeObject;

      // Root node
      if (node == this.root) {
         log.trace(Constants.LOG_EXIT);
         return false;
      }

      // Attributes node
      if (node == this.attributesNode) {
         boolean isNode = io().getAttributes() == null || io().getAttributes().isEmpty();
         log.trace(Constants.LOG_EXIT);
         return isNode;
      }

      // Identifier details node
      if (node == this.identifierNode) {
         Identifier id = io().getIdentifier();
         boolean isNode = id == null || id.getIdentifierLabels() == null || id.getIdentifierLabels().isEmpty();
         log.trace(Constants.LOG_EXIT);
         return isNode;
      }

      // Attribute node
      if (node.getType() == IoTreeNodeType.IO_ATTRIBUTE_DATA || node.getType() == IoTreeNodeType.ATT_SUBATTRIBUTE_DATA) {
         log.trace(Constants.LOG_EXIT);
         return false;
      }

      // Subattributes node
      if (node.getType() == IoTreeNodeType.ATT_SUBATTRIBUTES_TXT) {
         log.trace(Constants.LOG_EXIT);
         return false;
      }
      log.trace(Constants.LOG_EXIT);
      return true;
   }

   /**
    * generates a TreePath from the IO to the given node
    * 
    * @param node
    *           end node of path
    * @return path to node
    */
   private TreePath pathFromIo(IoTreeNode node) {
      log.trace(Constants.LOG_ENTER);

      List<IoTreeNode> pathList = new ArrayList<IoTreeNode>();
      IoTreeNode currentNode = node;
      while (currentNode != null) {
         pathList.add(currentNode);
         currentNode = currentNode.getParentNode();
      }
      Collections.reverse(pathList);

      TreePath path = new TreePath(pathList.toArray());
      log.trace(Constants.LOG_EXIT);
      return path;
   }

   /*
    * (non-Javadoc)
    * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
    */
   @Override
   public void removeTreeModelListener(TreeModelListener arg0) {
      log.trace(Constants.LOG_ENTER);
      this.treeModelListeners.remove(arg0);
      log.trace(Constants.LOG_EXIT);
   }

   /**
    * gets a node representing a subattribute of an attribute at a certain position
    * 
    * @param attributeNode
    *           parent node containing the attribute
    * @param position
    *           position of the subattribute
    * @return node representing the subattribute
    */
   private IoTreeNode subattributeNode(IoTreeNode attributeNode, int position) {
      log.trace(Constants.LOG_ENTER);
      IoTreeNode node = new IoTreeNode(IoTreeNodeType.ATT_SUBATTRIBUTE_DATA, ((Attribute) attributeNode.getCarriedObject())
            .getSubattributes().get(position), attributeNode);
      log.trace(Constants.LOG_EXIT);
      return node;
   }

   /**
    * returns the static node representing the subattributes of an attribute
    * 
    * @param attributeNode
    *           parent node representing the attribute
    * @return subattributes node
    */
   private IoTreeNode subattributesNode(IoTreeNode attributeNode) {
      log.trace(Constants.LOG_ENTER);
      IoTreeNode node = new IoTreeNode(IoTreeNodeType.ATT_SUBATTRIBUTES_TXT, attributeNode.getCarriedObject(), attributeNode);
      log.trace(Constants.LOG_EXIT);
      return node;
   }

   /*
    * (non-Javadoc)
    * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
    */
   @Override
   public void valueForPathChanged(TreePath arg0, Object arg1) {
      // unneeded in this model
   }

}
