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
package netinf.tools.iomanagement.gui.contentcheck;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import netinf.tools.iomanagement.gui.DialogHelper;

/**
 * This listener calls a given checker on each update of a component. It can only be used on JTextField or JComboBox
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class CheckListener implements DocumentListener {
   // /** Log4j Logger component */ private static final Logger log = Logger.getLogger(CheckListener.class);

   /** the component to listen on */
   private final JComponent component;

   /** the checker to call for the component */
   private final ContentChecker contentChecker;

   /** the text field in which the check result will be displayed */
   private final JTextField checkField;

   /**
    * @param component
    *           the component to listen on (JTextField or JComboBox only)
    * @param checkField
    *           the text field in which the check result will be displayed
    * @param contentChecker
    *           the checker to call for the component
    */
   public CheckListener(JComponent component, JTextField checkField, ContentChecker contentChecker) {
      if (!(component instanceof JTextField || component instanceof JComboBox)) {
         throw new IllegalArgumentException("Only JTextField or JComboBox allowed in CheckListener");
      }
      this.component = component;
      this.checkField = checkField;
      this.contentChecker = contentChecker;
   }

   @Override
   public void changedUpdate(DocumentEvent e) {
      fireUpdate();
   }

   /**
    * called on update of the attached component
    */
   private void fireUpdate() {
      String text = null;
      if (this.component instanceof JTextField) {
         text = ((JTextField) this.component).getText();
         boolean inputValid = this.contentChecker.isValidInput(text);
         // ((JTextField) this.component).setBackground(this.contentChecker.isValidInput(text) ? Color.GREEN : Color.RED);
         this.checkField.setText(inputValid ? DialogHelper.OK : DialogHelper.ERR);
         this.checkField.setBackground(inputValid ? DialogHelper.GREEN : DialogHelper.RED);
      }
      if (this.component instanceof JComboBox) {
         text = ((JTextComponent) ((JComboBox) this.component).getEditor().getEditorComponent()).getText();
         boolean inputValid = this.contentChecker.isValidInput(text);
         // ((JComboBox) this.component).getEditor().getEditorComponent().setBackground(this.contentChecker.isValidInput(text) ?
         // Color.GREEN : Color.RED);
         this.checkField.setText(inputValid ? DialogHelper.OK : DialogHelper.ERR);
         this.checkField.setBackground(inputValid ? DialogHelper.GREEN : DialogHelper.RED);
      }
   }

   @Override
   public void insertUpdate(DocumentEvent e) {
      fireUpdate();

   }

   @Override
   public void removeUpdate(DocumentEvent e) {
      fireUpdate();
   }
}
