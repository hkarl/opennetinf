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

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import netinf.tools.iomanagement.Constants;

import org.apache.log4j.Logger;

/**
 * This class is responsible to determine how to display tree nodes
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IoTreeCellRenderer extends DefaultTreeCellRenderer {
   /** Log4j Logger component */
   private static final Logger log = Logger.getLogger(IoTreeCellRenderer.class);

   /** serialization stuff */ private static final long serialVersionUID = 1L;

   /*
    * (non-Javadoc)
    * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean,
    * boolean, boolean, int, boolean)
    */
   @Override
   public Component getTreeCellRendererComponent(JTree tree, Object nodeObject, boolean sel, boolean expanded, boolean leaf,
         int row, boolean cellHasFocus) {
      log.trace(Constants.LOG_ENTER);

      // ask superclass
      super.getTreeCellRendererComponent(tree, nodeObject, sel, expanded, leaf, row, cellHasFocus);

      IoTreeNode node = (IoTreeNode) nodeObject;

      // set prefix
      if (node.getType().getPrefix() != null) {
         setText(node.getType().getPrefix() + ": " + getText());
      }

      // set icon
      setIcon(node.getType().getIcon(16));

      log.trace(Constants.LOG_EXIT);
      return this;
   }
}
