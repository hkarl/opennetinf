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
package netinf.tools.iomanagement.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import netinf.tools.iomanagement.Constants;
import netinf.tools.iomanagement.gui.contentassist.AssistListener;
import netinf.tools.iomanagement.gui.contentassist.ContentAssistant;
import netinf.tools.iomanagement.gui.contentcheck.CheckListener;
import netinf.tools.iomanagement.gui.contentcheck.ContentChecker;
import netinf.tools.iomanagement.gui.contentcheck.NotEmptyChecker;

import org.apache.log4j.Logger;

/**
 * Helper methods to display dialogs and get user input
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class DialogHelper {
   /** Log4j Logger component */
   private static final Logger log = Logger.getLogger(DialogHelper.class);

   /** red color constant, used for checker output */
   public static final Color RED = new Color(255, 150, 150);
   /** green color constant, used for checker output */
   public static final Color GREEN = new Color(150, 255, 150);
   /** OK-String constant, used for checker output */
   public static final String OK = "OK";
   /** UNKNOWN-String constant, used for checker output */
   public static final String UNKNOWN = "???";
   /** ERR(OR)-String constant, used for checker output */
   public static final String ERR = "ERR";

   /**
    * Shows a string input dialog and gets input from the user
    * 
    * @param parentComponent
    *           component to lock while working with the dialog
    * @param subject
    *           what is the information in this dialog about
    * @param keys
    *           array of labels for input fields (can't be null)
    * @param values
    *           array of prefilled values for input fields (may be null)
    * @param validValues
    *           array of arrays with valid inputs for input fields (may be null)
    * @param onlyValid
    *           boolean array, allow arbitrary user input (false) or only presets (false)
    * @param allowEmpty
    *           boolean array, allow empty field (true) or not (false)
    * @param icon
    *           icon to display
    * @param assistants
    *           array of content assistants for the fields (may be null)
    * @param checker
    *           array of content checkers for the fields (may be null)
    * @return array of entered field values, null on error
    */
   public static String[] showStringInputDialog(Component parentComponent, String subject, String[] keys, String[] values,
         String[][] validValues, boolean[] onlyValid, boolean[] allowEmpty, Icon icon, final ContentAssistant[] assistants,
         ContentChecker[] checker) {
      log.trace(Constants.LOG_ENTER);

      // array with all things to display
      Object[] message = new Object[2];

      // message to the user
      message[0] = "Please enter data for " + subject;

      // input fields
      final JComponent[] inputfields = new JComponent[keys.length];
      JPanel inputPanel = new JPanel(new GridLayout(keys.length, 1));

      // for all requested input fields ...
      for (int i = 0; i < keys.length; i++) {

         // ... set the label text
         String labelText = "unnamed";
         String fieldText = "";
         if (keys[i] != null) {
            labelText = keys[i];
         }

         // ... set the input field value
         if (values != null && i < values.length && values[i] != null) {
            fieldText = values[i];
         }

         // ... display label and input field
         JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // NOPMD by pgaugnet2 on 16.03.10 14:19
         JLabel label = new JLabel(labelText); // NOPMD by pgaugnet2 on 16.03.10 14:19

         // ... show check-field (instead of coloring the background)
         JTextField checkField = new JTextField("???"); // NOPMD by pgaugnet2 on 16.03.10 14:18
         checkField.setBorder(null);

         // ... use combobox when valid values are present
         boolean emptyInputOk = false;
         if (checker != null && i < checker.length && checker[i] != null) {
            emptyInputOk = checker[i].isValidInput("");
         }
         if (validValues != null && i < validValues.length && validValues[i] != null && validValues[i].length > 0) {
            if (onlyValid != null && i < onlyValid.length) {
               JComboBox combobox = new JComboBox(validValues[i]); // NOPMD by pgaugnet2 on 16.03.10 14:19
               // make it read-only if only valid entries are allowed
               combobox.setEditable(!onlyValid[i]);
               combobox.setSelectedItem(fieldText);
               // create checker if needed
               if (!onlyValid[i]) {
                  if (checker != null && i < checker.length && checker[i] != null) {
                     checkField.setText(emptyInputOk ? OK : ERR);
                     checkField.setBackground(emptyInputOk ? GREEN : RED);
                     // combobox.getEditor().getEditorComponent().setBackground(checker[i].isValidInput("") ? Color.GREEN :
                     // Color.RED);
                     ((JTextComponent) combobox.getEditor().getEditorComponent()).getDocument().addDocumentListener(
                           new CheckListener(combobox, checkField, checker[i])); // NOPMD by pgaugnet2 on 16.03.10 14:19
                  } else {
                     if (!allowEmpty[i]) { // NOPMD by pgaugnet2 on 16.03.10 14:18
                        // combobox.getEditor().getEditorComponent().setBackground(Color.RED);
                        checkField.setText(ERR);
                        checkField.setBackground(RED);
                        ((JTextComponent) combobox.getEditor().getEditorComponent()).getDocument().addDocumentListener(
                              new CheckListener(combobox, checkField, new NotEmptyChecker())); // NOPMD by pgaugnet2 on 16.03.10
                        // 14:19
                     }
                  }
               }
               inputfields[i] = combobox;
            }

            // ... use textfield when no valid values are present
         } else {
            JTextField textfield = new JTextField(20); // NOPMD by pgaugnet2 on 16.03.10 14:19
            textfield.setText(fieldText);
            // create checker if needed
            if (onlyValid == null || !onlyValid[i]) {
               if (checker != null && i < checker.length && checker[i] != null) {
                  // textfield.setBackground(checker[i].isValidInput("") ? Color.GREEN : Color.RED);
                  checkField.setText(emptyInputOk ? OK : ERR);
                  checkField.setBackground(emptyInputOk ? GREEN : RED);
                  textfield.getDocument().addDocumentListener(new CheckListener(textfield, checkField, checker[i])); // NOPMD by
                  // pgaugnet2
                  // on
                  // 16.03.10
                  // 14:19
               } else {
                  if (!allowEmpty[i]) {
                     // textfield.setBackground(Color.RED);
                     checkField.setText(ERR);
                     checkField.setBackground(RED);
                     textfield.getDocument().addDocumentListener(new CheckListener(textfield, checkField, new NotEmptyChecker())); // NOPMD
                     // by
                     // pgaugnet2
                     // on
                     // 16.03.10
                     // 14:19
                  }
               }
            }
            inputfields[i] = textfield;
         }

         // ... show assist button (only active if there is an assistant)
         JButton assistButton = new JButton("..."); // NOPMD by pgaugnet2 on 16.03.10 14:19
         assistButton.setEnabled(false);
         if (assistants != null && i < assistants.length && assistants[i] != null) {
            assistButton.setEnabled(true);
            assistButton.addActionListener(new AssistListener(inputfields[i], assistants[i])); // NOPMD by pgaugnet2 on 16.03.10
            // 14:19
         }
         panel.add(label);
         panel.add(inputfields[i]);
         panel.add(checkField);
         panel.add(assistButton);
         inputPanel.add(panel);
      }

      // add the labels/fields to the dialog, set further options and show the dialog
      message[1] = inputPanel;
      Object[] options = { "Submit", "Cancel" };
      int result = JOptionPane.showOptionDialog(parentComponent, message, subject, JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE, icon, options, options[0]);

      // process the results by ...
      if (result == JOptionPane.YES_OPTION) {
         String[] returnArray = new String[keys.length];

         // ... collecting the user inputs
         for (int i = 0; i < keys.length; i++) {
            boolean emptyAllowed = false;

            // ... obeying allowEmpty
            if (allowEmpty != null && i < allowEmpty.length) {
               emptyAllowed = allowEmpty[i];
            }

            // ... collecting from textfields
            if (inputfields[i] instanceof JTextField) {
               JTextField textfield = (JTextField) inputfields[i];

               if (!emptyAllowed && textfield.getText().isEmpty()) {
                  log.warn("Empty " + keys[i] + " is not allowed");
                  return null;
               }
               returnArray[i] = textfield.getText();

               // collecting from comboboxes
            }
            if (inputfields[i] instanceof JComboBox) {
               JComboBox combobox = (JComboBox) inputfields[i];
               if (!emptyAllowed && ((String) combobox.getSelectedItem()).isEmpty()) {
                  log.warn("Empty " + keys[i] + " is not allowed");
                  log.trace(Constants.LOG_EXIT);
                  return null;
               }
               returnArray[i] = (String) combobox.getSelectedItem();
            }
         }
         log.trace(Constants.LOG_EXIT);
         return returnArray;
      }
      // dialog canceled -> return expected null [EU]
      log.trace(Constants.LOG_EXIT);
      return null;
   }
}