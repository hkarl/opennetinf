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
package netinf.tools.iomanagement.gui.contentassist;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import netinf.common.utils.ValueUtils;
import netinf.tools.iomanagement.Constants;
import netinf.tools.iomanagement.gui.DialogHelper;
import netinf.tools.iomanagement.gui.frames.IOFrame;

import org.apache.log4j.Logger;

/**
 * A content assistant for ValueRaw fields (values of attributes)
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class ValueRawAssistant implements ContentAssistant {
   /** Log4j Logger component */
   private static final Logger log = Logger.getLogger(ValueRawAssistant.class);

   /*
    * (non-Javadoc)
    * @see netinf.tools.iomanagement.gui.contentassist.ContentAssistant#assistanceDialog(javax.swing.JComponent)
    */
   @Override
   public void assistanceDialog(JComponent jComponent) {
      log.trace(Constants.LOG_ENTER);

      String oldValue = null;

      if (jComponent instanceof JTextField) {
         JTextField textfield = (JTextField) jComponent;
         oldValue = textfield.getText();
      }
      if (jComponent instanceof JComboBox) {
         JComboBox combobox = (JComboBox) jComponent;
         oldValue = (String) combobox.getSelectedItem();
      }

      // input for dialog
      String[] predefinedValues = ValueUtils.splitRawValue(oldValue);
      String[] keys = { "Type", "Value" };
      List<String> validTypes = new ArrayList<String>();
      for (String type : Constants.getString("DATATYPES").split(",")) {
         if (!type.isEmpty()) {
            validTypes.add(type);
         }
      }
      validTypes.addAll(IOFrame.ADDITIONAL_DATATYPES);
      String[][] validValues = { validTypes.toArray(new String[validTypes.size()]), null };
      boolean[] onlyValid = { false, false };
      boolean[] allowEmpty = { false, true };
      Icon icon = new ImageIcon(getClass().getResource("/AttributeValue.png"));

      // actual dialog
      log.debug("Displaying ValueRaw content assistant");
      String[] assistedValue = DialogHelper.showStringInputDialog(jComponent.getParent(), "Value Raw", keys, predefinedValues,
            validValues, onlyValid, allowEmpty, icon, null, null);

      // results of assistant
      if (assistedValue == null) {
         log.info("Cancelled content assistant");
         log.trace(Constants.LOG_EXIT);
         return;
      }
      Object assistedObject = ValueUtils.getObjectFromRaw(assistedValue[0], assistedValue[1]);
      if (assistedObject == null) {
         log.warn("Invalid input in content assistant");
         log.trace(Constants.LOG_EXIT);
         return;
      }

      // update corresponding component
      if (jComponent instanceof JTextField) {
         JTextField textfield = (JTextField) jComponent;
         textfield.setText(assistedValue[0] + ":" + assistedValue[1]);
      }
      if (jComponent instanceof JComboBox) {
         JComboBox combobox = (JComboBox) jComponent;
         String setSel = assistedValue[0] + ":" + assistedValue[1];
         combobox.setSelectedItem(setSel);
         if (!combobox.getSelectedItem().equals(setSel)) {
            log.debug("Could not set value returned by assistance dialog");
         }
      }

      log.trace(Constants.LOG_EXIT);

   }

}
