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
package netinf.tools.productlist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.DefinedVersionKind;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.creator.ValidCreator;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.security.identity.IdentityManager;
import netinf.common.utils.DatamodelUtils;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * {@link MainFrame} contains UserInterface as well as main parts of business logic of Product List Tool. The Product List Tool is
 * an integral part of Scenario 2. The Product List Tool is used to manage Shopping Lists of users as well as Shop IOs of shops
 * (containing the products present in the shop). The tool may be tailored to scenario 2: The "Girlfriend" mode is used to manage
 * Shopping Lists, the "Checkout" mode is manages Shop IOs.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class MainFrame extends JFrame {

   private static final long serialVersionUID = 6087288548349336906L;
   private static final Logger LOG = Logger.getLogger(MainFrame.class);

   private static final String DEFAULT_PRODUCT_ID = "Fresh milk";
   private static final int DEFAULT_PRODUCT_QUANTITY = 1;

   private static final int DEFAULT_SEARCH_TIMEOUT = 5000;

   private JTable table;
   private JLabel toolTypeLabel;
   private JLabel ioNameLabel;
   private InformationObject io;
   private Identifier identifier;
   private final String applicationIdentity;
   private Component listPanel;

   private final JButton addButton = new JButton("Add");
   private final JButton upButton = new JButton("+");
   private final JButton downButton = new JButton("-");
   private final JButton deleteButton = new JButton("Delete");
   private JLabel productQuantityLabel = null;
   private JTextField productQuantityField = null;
   private final Container contentPane;

   private final DatamodelFactory datamodelFactory;
   private final RemoteNodeConnection remoteNodeConnection;
   private final RemoteNodeConnection searchNodeConnection;
   private final IdentityManager idManager;
   private final ProductListEsfConnector esfConnector;
   private final ProductListEsfMessageProcessor msgProc;

   private String currentApplicationType = "STANDARD";

   private static final String APPLICATION_TYPE_CHECKOUT = "CHECKOUT";
   private static final String APPLICATION_TYPE_GIRLFRIEND = "GIRLFRIEND";

   private static final boolean RELY_ON_EVENTSERVICE_UPDATE = true;

   private HashMap<String, Identifier> map = null;

   @Inject
   public MainFrame(@Named("cc.tcp.host") final String host, @Named("cc.tcp.port") final int port,
         @Named("search.tcp.host") final String searchHost, @Named("search.tcp.port") final int searchPort,
         @Named("app.identity") final String applicationIdentity, final RemoteNodeConnection convenienceCommunicator,
         final RemoteNodeConnection searchNodeConnection, final DatamodelFactory datamodelFactory,
         final IdentityManager idManager, final ProductListEsfConnector esfConnector, final ProductListEsfMessageProcessor msgProc) {

      this.remoteNodeConnection = convenienceCommunicator;
      convenienceCommunicator.setHostAndPort(host, port);

      this.searchNodeConnection = searchNodeConnection;
      searchNodeConnection.setHostAndPort(searchHost, searchPort);

      this.datamodelFactory = datamodelFactory;
      this.idManager = idManager;
      idManager.setFilePath("../configs/Identities/privateKeyFile.pkf");
      this.esfConnector = esfConnector;
      this.msgProc = msgProc;
      this.applicationIdentity = applicationIdentity;

      // GUI
      setTitle("NetInf Product List Tool");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setPreferredSize(new Dimension(420, 480));
      // 
      setResizable(false);
      Image img = Toolkit.getDefaultToolkit().getImage("images/4ward.png");
      setIconImage(img);
      getContentPane().setLayout(new BorderLayout());

      // demo setup: set positions for 1024 resolution
      if (applicationIdentity.equals("ni:HASH_OF_PK=123~UNIQUE_LABEL=GirlfriendPeter")) {
         setLocation(60, 144);
      }
      if (applicationIdentity.equals("ni:HASH_OF_PK=123~UNIQUE_LABEL=Checkout")) {
         setLocation(540, 144);
      }

      contentPane = getContentPane();
      contentPane.add(createHeaderPanel(), BorderLayout.PAGE_START);
      listPanel = createListPanel();
      contentPane.add(listPanel, BorderLayout.CENTER);
      contentPane.add(createButtonPanel(), BorderLayout.LINE_END);

      contentPane.add(createBottomPanel(), BorderLayout.SOUTH);

      pack();

      startEsfConnector();

      LOG.log(DemoLevel.DEMO, "(LIST ) I am ready");
   }

   public void setApplicationType(final String applicationType) {
      if (applicationType == null) {
         LOG.log(DemoLevel.DEMO, "(LIST ) I am running in general mode");
         return;
      }

      this.currentApplicationType = applicationType;

      toolTypeLabel.setText(applicationType);

      if (currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_GIRLFRIEND)) {

         LOG.log(DemoLevel.DEMO, "(LIST ) I am running in girlfriend mode");

         if (upButton != null) {
            upButton.setVisible(false);
         }
         if (downButton != null) {
            downButton.setVisible(false);
         }
         if (productQuantityField != null) {
            productQuantityField.setVisible(false);
         }
         if (productQuantityLabel != null) {
            productQuantityLabel.setVisible(false);
         }

         // GIRLFRIEND Mode needs specific list, so replace standard one
         contentPane.remove(listPanel);
         listPanel = createListPanel();
         contentPane.add(listPanel, BorderLayout.CENTER);
      }

      if (currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_CHECKOUT)) {
         LOG.log(DemoLevel.DEMO, "(LIST ) I am running in checkout mode");
      }

   }

   private void startEsfConnector() {
      msgProc.setMainFrame(this);
      esfConnector.setName("esfConnector");
      esfConnector.setIdentityIdentifier(datamodelFactory.createIdentifierFromString(applicationIdentity));
      esfConnector.start();
   }

   private Component createBottomPanel() {
      JPanel southPanel = new JPanel(new GridBagLayout());

      toolTypeLabel = new JLabel();
      toolTypeLabel.setFont(new Font("Dialog", 0, 20));
      GridBagConstraints cToolType = new GridBagConstraints();
      cToolType.anchor = GridBagConstraints.CENTER;
      cToolType.gridy = 0;
      southPanel.add(toolTypeLabel, cToolType);

      ioNameLabel = new JLabel("<no list loaded>");
      ioNameLabel.setFont(new Font("Dialog", 0, 18));
      GridBagConstraints cIoName = new GridBagConstraints();
      cIoName.anchor = GridBagConstraints.CENTER;
      cIoName.gridy = 1;

      MouseListener mouseListener = new MouseListener() {

         @Override
         public void mouseReleased(MouseEvent e) {
         }

         @Override
         public void mousePressed(MouseEvent e) {
         }

         @Override
         public void mouseExited(MouseEvent e) {
         }

         @Override
         public void mouseEntered(MouseEvent e) {
         }

         @Override
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
               if (identifier != null) {
                  JOptionPane.showInputDialog(null, "Identifier:", "Identifier of loaded list", JOptionPane.INFORMATION_MESSAGE,
                        null, null, identifier.toString());
               }
            }
         }
      };

      ioNameLabel.addMouseListener(mouseListener);
      southPanel.add(ioNameLabel, cIoName);

      return southPanel;
   }

   private Component createHeaderPanel() {
      final JButton newButton = new JButton("New List");

      ActionListener newListener = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {

            String[] results = showDialogNewList();

            boolean statusOk;
            if (results != null) {

               LOG.log(DemoLevel.DEMO, "(LIST ) Creating and storing a new '" + results[0] + "' Information Object");

               statusOk = true;
               // user wants to create a new list => remove old list data from tool
               if (io != null) {
                  LOG.log(DemoLevel.DEMO, "(LIST ) Unsubscribing from infos about changes of old list");
                  esfConnector.sendUnsubscription(io.getIdentifier().toString());
               }
               io = null;
               identifier = null;
               ((DefaultTableModel) table.getModel()).setRowCount(0);
               ioNameLabel.setText("<no list loaded>");
            } else {
               statusOk = false;
            }

            String identity = "";
            if (statusOk) {
               identity = getIdentity();
               if (identity == null) {
                  statusOk = false;
               }
            }

            IdentityObject owner = null;
            if (statusOk) {
               Identifier ownerID = datamodelFactory.createIdentifierFromString(DatamodelUtils.identifierFromIdentity(identity));
               try {
                  owner = (IdentityObject) remoteNodeConnection.getIO(ownerID);
               } catch (NetInfCheckedException er) {
                  LOG.error("Error receiving identity");
                  statusOk = false;
               }
               if (owner == null) {
                  statusOk = false;
               }
            }

            if (statusOk) {
               io = ValidCreator.createValidInformationObject(owner, DefinedVersionKind.UNVERSIONED, results[1]);
               if (io == null) {
                  LOG.error("Could not create Information Object.");
                  statusOk = false;
               }
            }

            if (statusOk) {
               Attribute represents = datamodelFactory.createAttribute(DefinedAttributeIdentification.REPRESENTS.getURI(),
                     results[0]);
               represents.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
               io.addAttribute(represents);

               Attribute name = datamodelFactory.createAttribute(DefinedAttributeIdentification.NAME.getURI(), results[2]);
               name.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
               io.addAttribute(name);

               if (results[0].equals("Shop")) {
                  Double latitudeValue = 0.0;
                  try {
                     latitudeValue = Double.parseDouble(results[3]);
                  } catch (NumberFormatException ex) {
                     LOG.error("Specified latitude value can not be parsed as Double value.");
                     statusOk = false;
                  }
                  Attribute latitude = datamodelFactory.createAttribute(DefinedAttributeIdentification.GEO_LAT.getURI(),
                        latitudeValue);
                  latitude.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
                  io.addAttribute(latitude);

                  Double longitudeValue = 0.0;
                  try {
                     longitudeValue = Double.parseDouble(results[4]);
                  } catch (NumberFormatException ex) {
                     LOG.error("Specified longitude value can not be parsed as Double value.");
                     statusOk = false;
                  }
                  Attribute longitude = datamodelFactory.createAttribute(DefinedAttributeIdentification.GEO_LONG.getURI(),
                        longitudeValue);
                  longitude.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
                  io.addAttribute(longitude);
               }
            }

            if (statusOk) {
               try {
                  remoteNodeConnection.putIO(io);
                  identifier = io.getIdentifier();
               } catch (NetInfCheckedException e1) {
                  LOG.error(e1.getMessage());
                  io = null;
                  identifier = null;
                  statusOk = false;
               }
            }

            if (statusOk) {
               addButton.setEnabled(true);
               deleteButton.setEnabled(false);
               upButton.setEnabled(false);
               downButton.setEnabled(false);

               ioNameLabel.setText(results[2]);

               LOG.log(DemoLevel.DEMO, "(LIST ) Subscribing to infos about changes of this list");
               esfConnector.sendSubscription(io.getIdentifier().toString(), "SELECT ?old ?new WHERE {?new <"
                     + DefinedAttributeIdentification.IDENTIFIER.getURI() + "> \"" + io.getIdentifier().toString() + "\".}",
                     1000000000);
            }

         }
      };

      newButton.addActionListener(newListener);

      final JButton loadButton = new JButton("Load List");

      ActionListener loadListener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent ev) {

            UIManager.put("OptionPane.okButtonText", "OK");
            UIManager.put("OptionPane.cancelButtonText", "Cancel");
            Object result = JOptionPane.showInputDialog(null, "Choose List", "Load List", JOptionPane.QUESTION_MESSAGE, null,
                  getIdentifiers(), null);

            String identifierString = "";
            if (result != null) {

               LOG.log(DemoLevel.DEMO, "(LIST ) Loading list:  " + result.toString());

               identifierString = result.toString();
               identifier = map.get(identifierString);

               if (io != null) {
                  LOG.log(DemoLevel.DEMO, "(LIST ) Unsubscribing from infos about changes of old list");
                  esfConnector.sendUnsubscription(io.getIdentifier().toString());
               }

               boolean ok = updateList(); // io is set to new value within this method

               if (ok) {
                  LOG.log(DemoLevel.DEMO, "(LIST ) Subscribing to infos about changes of this list");
                  esfConnector.sendSubscription(io.getIdentifier().toString(), "SELECT ?old ?new WHERE {?new <"
                        + DefinedAttributeIdentification.IDENTIFIER.getURI() + "> \"" + io.getIdentifier().toString() + "\".}",
                        1000000000);
               }

            }
         }
      };
      loadButton.addActionListener(loadListener);

      JPanel headerPanel = new JPanel();
      headerPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
      headerPanel.add(newButton);
      headerPanel.add(loadButton);
      return headerPanel;
   }

   protected String getIdentity() {
      LOG.trace("enter");

      // try to get an identifier string
      String identityString = null;
      try {
         LOG.info("Loading identity");
         identityString = idManager.getLocalIdentity();
      } catch (NetInfCheckedException e) {
         LOG.info("No identity stored.");
      }
      if (identityString == null) {
         LOG.trace("exit");
         return null;
      }

      LOG.trace("exit");
      return identityString;
   }

   protected String[] showDialogNewList() {
      String[] inputfieldlabels = { "Represents", "Unique Label", "Name", "Latitude", "Longitude" };
      final boolean[] allowEmpty = { false, false, false, false, false };

      LOG.trace("enter");

      // array with all things to display
      Object[] message = new Object[2];

      // message to the user
      message[0] = "Please enter data for a new product list";

      // input fields
      final JComponent[] inputfields = new JComponent[inputfieldlabels.length];
      JPanel inputPanel = new JPanel(new GridLayout(inputfieldlabels.length, 1));

      // ... use combobox for first input field (Represents)
      String[] validValues = { "ShoppingList", "Shop" };
      // Restrict choice in case application Type is set
      if (currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_CHECKOUT)) {
         String[] tempValues = { "Shop" };
         validValues = tempValues;
      } else if (currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_GIRLFRIEND)) {
         String[] tempValues = { "ShoppingList" };
         validValues = tempValues;
      }
      final JComboBox combobox = new JComboBox(validValues);

      // for all requested input fields ...
      for (int i = 0; i < inputfieldlabels.length; i++) {

         // ... set the label text
         String labelText = inputfieldlabels[i];

         // ... display label and input field
         JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         JLabel label = new JLabel(labelText);

         if (i > 0) {
            JTextField textfield = new JTextField(20);
            textfield.setText("");
            inputfields[i] = textfield;

            if (i == 3 || i == 4) {
               NumberFormat format = DecimalFormat.getInstance(Locale.ENGLISH);
               format.setMaximumIntegerDigits(3);
               format.setMaximumFractionDigits(8);
               JFormattedTextField formattedTextField = new JFormattedTextField(format);
               formattedTextField.setColumns(20);
               formattedTextField.setText("");
               inputfields[i] = formattedTextField;
            }
         } else {

            combobox.setEditable(false);
            ActionListener representsfieldListener = new ActionListener() {
               @Override
               public void actionPerformed(ActionEvent ev) {
                  if (combobox.getSelectedItem().toString().equalsIgnoreCase("Shop")) {
                     inputfields[3].setEnabled(true);
                     inputfields[4].setEnabled(true);
                     allowEmpty[3] = false;
                     allowEmpty[4] = false;
                  } else {
                     inputfields[3].setEnabled(false);
                     inputfields[4].setEnabled(false);
                     allowEmpty[3] = true;
                     allowEmpty[4] = true;
                  }
               }
            };
            combobox.addActionListener(representsfieldListener);

            inputfields[i] = combobox;
         }
         // ... use textfield for the other fields

         // Do not show lat/lon fields in Girlfriend-Mode
         if (i > 2 && currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_GIRLFRIEND)) {
            continue;
         }

         panel.add(label);
         panel.add(inputfields[i]);
         inputPanel.add(panel);
      }

      combobox.setSelectedIndex(0);

      // add the labels/fields to the dialog, set further options and show the dialog
      message[1] = inputPanel;
      Object[] options = { "Submit", "Cancel" };
      int result = JOptionPane.showOptionDialog(null, message, "New Product List", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

      // process the results by ...
      if (result == JOptionPane.YES_OPTION) {
         String[] resultArray = new String[inputfieldlabels.length];

         // FIXME if some input is not ok, the user should be informed about that

         // ... collecting the user inputs
         for (int i = 0; i < inputfieldlabels.length; i++) {

            // ... collecting from textfields
            if (inputfields[i] instanceof JTextField) {
               JTextField textfield = (JTextField) inputfields[i];

               if (!allowEmpty[i] && textfield.getText().isEmpty()) {
                  LOG.warn("Empty " + inputfieldlabels[i] + " is not allowed");
                  return null;
               }
               resultArray[i] = textfield.getText().trim();

               // collecting from comboboxes
            }
            if (inputfields[i] instanceof JComboBox) {
               JComboBox inputCombobox = (JComboBox) inputfields[i];
               if (!allowEmpty[i] && ((String) inputCombobox.getSelectedItem()).isEmpty()) {
                  LOG.warn("Empty " + inputfieldlabels[i] + " is not allowed");
                  LOG.trace("exit");
                  return null;
               }
               resultArray[i] = (String) inputCombobox.getSelectedItem();
            }
         }
         LOG.trace("exit");
         return resultArray;
      }
      LOG.trace("exit");
      return null;
   }

   private Component createButtonPanel() {
      final JLabel productIdLabel = new JLabel("Product:");
      final JTextField productIdField = new JTextField(DEFAULT_PRODUCT_ID, 10);

      productQuantityLabel = new JLabel("Quantity:");
      productQuantityField = new JTextField(String.valueOf(DEFAULT_PRODUCT_QUANTITY), 2);
      productQuantityField.setHorizontalAlignment(JTextField.RIGHT);

      ActionListener listener = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            Integer amount = null;
            boolean statusOk = false;

            if (productIdField.getText().trim().length() > 0) {
               statusOk = true;
               try {
                  amount = Integer.valueOf(productQuantityField.getText().trim());
               } catch (NumberFormatException ex) {
                  LOG.error("Specified amount value can not be parsed as Integer value.");
                  statusOk = false;
               }
            }
            if (statusOk) {
               if (currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_GIRLFRIEND)) {
                  LOG.log(DemoLevel.DEMO, "(LIST ) Adding product '" + productIdField.getText().trim() + "' to list");
               } else {
                  LOG.log(DemoLevel.DEMO, "(LIST ) Adding product '" + productIdField.getText().trim() + "' with quantity "
                        + amount + " to list");
               }

               Object[] newRow = { productIdField.getText().trim(), amount };

               if (currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_GIRLFRIEND)) {
                  Object[] tempNewRow = { productIdField.getText().trim() };
                  newRow = tempNewRow;
               }

               if (!RELY_ON_EVENTSERVICE_UPDATE) {
                  ((DefaultTableModel) table.getModel()).addRow(newRow);
               }
               addProduct(productIdField.getText(), Integer.parseInt(productQuantityField.getText().trim()));

               if (((DefaultTableModel) table.getModel()).getRowCount() == 0) {
                  addButton.setEnabled(true);
                  deleteButton.setEnabled(true);
                  upButton.setEnabled(true);
                  downButton.setEnabled(true);
               }
            }
         }
      };

      addButton.addActionListener(listener);

      ActionListener deleteListener = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row != -1) {

               LOG.log(DemoLevel.DEMO, "(LIST ) Deleting product '" + table.getValueAt(row, 0).toString() + "' from list");

               deleteProduct(table.getValueAt(row, 0).toString());

               if (!RELY_ON_EVENTSERVICE_UPDATE) {
                  ((DefaultTableModel) table.getModel()).removeRow(row);
               }
               if (table.getRowCount() == 0) {
                  addButton.setEnabled(true);
                  deleteButton.setEnabled(false);
                  upButton.setEnabled(false);
                  downButton.setEnabled(false);
               }
            }
         }
      };

      deleteButton.addActionListener(deleteListener);

      ActionListener upListener = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row != -1) {
               Integer value = (Integer) table.getValueAt(row, 1);
               List<Attribute> products = io.getAttribute(DefinedAttributeIdentification.PRODUCT.getURI());
               for (Attribute product : products) {
                  if (product.getValue(String.class) == table.getValueAt(row, 0).toString()) {
                     product.getSingleSubattribute(DefinedAttributeIdentification.AMOUNT.getURI()).setValue(value + 1);
                     break;
                  }
               }

               LOG.log(DemoLevel.DEMO, "(LIST ) Increasing quantity of product '" + table.getValueAt(row, 0).toString()
                     + "' by 1");

               try {
                  remoteNodeConnection.putIO(io);
               } catch (NetInfCheckedException e1) {
                  LOG.error(e1.getMessage());
               }
               if (!RELY_ON_EVENTSERVICE_UPDATE) {
                  table.setValueAt(value + 1, row, 1);
               }
            }
         }
      };

      upButton.addActionListener(upListener);

      ActionListener downListener = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row != -1) {
               Integer value = (Integer) table.getValueAt(row, 1);
               List<Attribute> products = io.getAttribute(DefinedAttributeIdentification.PRODUCT.getURI());
               if (value > 1) {
                  LOG.log(DemoLevel.DEMO, "(LIST ) Reducing quantity of  product '" + table.getValueAt(row, 0).toString()
                        + "' by 1");

                  for (Attribute product : products) {
                     if (product.getValue(String.class) == table.getValueAt(row, 0).toString()) {
                        product.getSingleSubattribute(DefinedAttributeIdentification.AMOUNT.getURI()).setValue(value - 1);
                        break;
                     }
                  }
                  try {
                     remoteNodeConnection.putIO(io);
                  } catch (NetInfCheckedException e2) {
                     LOG.error(e2.getMessage());
                  }

                  if (!RELY_ON_EVENTSERVICE_UPDATE) {
                     table.setValueAt(value - 1, row, 1);
                  }
               } else {
                  LOG.log(DemoLevel.DEMO, "(LIST ) Deleting  product '" + table.getValueAt(row, 0).toString()
                        + "' since quantity is reduced to 0");

                  deleteProduct(table.getValueAt(row, 0).toString());
                  if (!RELY_ON_EVENTSERVICE_UPDATE) {
                     ((DefaultTableModel) table.getModel()).removeRow(row);
                  }
                  if (table.getRowCount() == 0) {
                     addButton.setEnabled(true);
                     deleteButton.setEnabled(false);
                     upButton.setEnabled(false);
                     downButton.setEnabled(false);
                  }
               }
            }
         }
      };

      downButton.addActionListener(downListener);

      addButton.setEnabled(false);
      deleteButton.setEnabled(false);
      upButton.setEnabled(false);
      downButton.setEnabled(false);

      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new MigLayout());
      buttonPanel.add(productIdLabel);
      buttonPanel.add(productIdField, "wrap");
      buttonPanel.add(productQuantityLabel);
      buttonPanel.add(productQuantityField, "wrap");
      buttonPanel.add(addButton, "span, w 80!, wrap 15");
      buttonPanel.add(deleteButton, "span, w 80!, wrap 15");
      buttonPanel.add(upButton, "span, split 2, w 50!");
      buttonPanel.add(downButton, "w 50!");

      return buttonPanel;
   }

   private Component createListPanel() {
      String[][] data = null;
      String[] columnNames = { "Product", "Quantity" };

      if (currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_GIRLFRIEND)) {
         String[] tempColumnNames = { "Product" };
         columnNames = tempColumnNames;
      }

      table = new JTable(data, columnNames);

      DefaultTableModel model = new DefaultTableModel(data, columnNames) {
         private static final long serialVersionUID = 1L;

         @Override
         public boolean isCellEditable(int row, int column) {
            return false;
         }
      };

      table.setModel(model);
      table.getColumnModel().getColumn(0).setPreferredWidth(150);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      JPanel listPanel = new JPanel();
      listPanel.setBackground(Color.WHITE);
      listPanel.add(table);

      return listPanel;
   }

   protected void addProduct(final String newProduct, final Integer quantity) {
      Attribute product = datamodelFactory.createAttribute(DefinedAttributeIdentification.PRODUCT.getURI(), newProduct);
      product.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      Attribute amount = datamodelFactory.createAttribute(DefinedAttributeIdentification.AMOUNT.getURI(), quantity);
      amount.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.toString());
      product.addSubattribute(amount);
      io.addAttribute(product);
      try {
         remoteNodeConnection.putIO(io);
      } catch (NetInfCheckedException e) {
         LOG.error(e.getMessage());
      }
   }

   protected void deleteProduct(final String productToDelete) {
      List<Attribute> products = io.getAttribute(DefinedAttributeIdentification.PRODUCT.getURI());
      for (Attribute product : products) {
         if (product.getValue(String.class).equals(productToDelete)) {
            try {
               io.removeAttribute(product);
               remoteNodeConnection.putIO(io);
               break;
            } catch (NetInfCheckedException e) {
               LOG.error(e.getMessage());
            }
         }
      }
   }

   /**
    * Get human readable array of Identifiers of Lists manageable by this tool
    * 
    * @return Object array of strings representing ShoppingLists, ShopIOs,...
    */
   private Object[] getIdentifiers() {
      map = new HashMap<String, Identifier>();

      List<Identifier> identifiers = new LinkedList<Identifier>();

      // get Shop IOs (but only if not in Girlfriend Mode)
      if (!currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_GIRLFRIEND)) {
         try {
            identifiers = searchNodeConnection.performSearch(
                  "?id netinf:represents ?blank . ?blank netinf:attributeValue 'String:Shop'.", DEFAULT_SEARCH_TIMEOUT);
         } catch (NetInfCheckedException e) {
            LOG.error("Error getting ShoppingList IOs. " + e.getMessage());
         }
      }

      // get ShoppingList IOs (but only if not in CHECKOUT Mode)
      if (!currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_CHECKOUT)) {
         try {
            List<Identifier> tempIdentifiers = searchNodeConnection.performSearch(
                  "?id netinf:represents ?blank . ?blank netinf:attributeValue 'String:ShoppingList'.", DEFAULT_SEARCH_TIMEOUT);

            if (tempIdentifiers != null) {
               for (Identifier identifier : tempIdentifiers) {
                  identifiers.add(identifier);
               }
            }
         } catch (NetInfCheckedException e) {
            LOG.error("Error getting ShoppingList IOs. " + e.getMessage());
         }
      }

      // prepare readable list of identifiers
      List<String> elements = new LinkedList<String>();

      for (Identifier identifier : identifiers) {
         String selectionValue;
         selectionValue = identifier.getIdentifierLabel(DefinedLabelName.UNIQUE_LABEL.getLabelName()).getLabelValue();
         selectionValue += " <" + identifier.toString().substring(0, 20) + "...>";
         elements.add(selectionValue);
         map.put(selectionValue, identifier);
      }

      return elements.toArray();
   }

   public boolean updateList() {

      LOG.log(DemoLevel.DEMO, "(LIST ) Updating the list");

      // user wants to load a new list => remove old list data from tool
      io = null;
      ((DefaultTableModel) table.getModel()).setRowCount(0);
      ioNameLabel.setText("<updating list>");

      try {
         io = remoteNodeConnection.getIO(identifier);
      } catch (NetInfCheckedException e1) {
         LOG.error(e1.getMessage());
      }

      if (io != null) {
         List<Attribute> products = io.getAttribute(DefinedAttributeIdentification.PRODUCT.getURI());
         if (!products.isEmpty()) {
            for (Attribute product : products) {
               Integer amount = Integer.valueOf(1);
               Attribute amountAttribute = product.getSingleSubattribute(DefinedAttributeIdentification.AMOUNT.getURI());
               if (amountAttribute != null) {
                  amount = amountAttribute.getValue(Integer.class);
               }

               Object[] newRow = { product.getValue(String.class), amount };
               // if in Girlfriendmode, do not add amount
               if (currentApplicationType.equalsIgnoreCase(APPLICATION_TYPE_GIRLFRIEND)) {
                  Object[] tempNewRow = { product.getValue(String.class) };
                  newRow = tempNewRow;
               }
               ((DefaultTableModel) table.getModel()).addRow(newRow);
            }
            addButton.setEnabled(true);
            deleteButton.setEnabled(true);
            upButton.setEnabled(true);
            downButton.setEnabled(true);
         } else {
            addButton.setEnabled(true);
            deleteButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
         }

         ioNameLabel.setText(io.getSingleAttribute(DefinedAttributeIdentification.NAME.getURI()).getValue(String.class));
         return true;

      } else {
         ioNameLabel.setText("<no list loaded>");
         JOptionPane.showMessageDialog(null, "IO with identifier " + identifier.toString() + " not found.");
         identifier = null;
         return false;
      }
   }

}
