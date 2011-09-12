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
/**
 * 
 */
package netinf.tools.iomanagement.gui.frames;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;
import netinf.common.utils.ValueUtils;
import netinf.tools.iomanagement.Constants;
import netinf.tools.iomanagement.gui.DialogHelper;
import netinf.tools.iomanagement.gui.contentassist.ContentAssistant;
import netinf.tools.iomanagement.gui.contentassist.ValueRawAssistant;
import netinf.tools.iomanagement.gui.contentcheck.ContentChecker;
import netinf.tools.iomanagement.gui.contentcheck.ValueRawChecker;
import netinf.tools.iomanagement.gui.iotree.IoTreeCellRenderer;
import netinf.tools.iomanagement.gui.iotree.IoTreeModel;
import netinf.tools.iomanagement.gui.iotree.IoTreeNode;
import netinf.tools.iomanagement.gui.iotree.IoTreeNodeType;
import netinf.tools.iomanagement.identifierdb.IdentifierDatabase;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Frame for IO modification
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public final class IOFrame extends JFrame implements TreeSelectionListener {
   /** Log4j Logger component */
   private static final Logger log = Logger.getLogger(IOFrame.class);

   /** serialization stuff */
   private static final long serialVersionUID = 1L;

   // Swing components
   /** main panel */
   private final JPanel compMainPanel;
   /** edit button */
   private JButton compEditButton;
   /** delete button */
   private JButton compDeleteButton;
   /** add button */
   private JButton compAddButton;
   /** tree display of the IO */
   private JTree compIOTree;

   /** communication interface used for connection to a node */
   private final NetInfNodeConnection communicator;
   /** local database of identifiers */
   private final IdentifierDatabase identifierDatabase;
   /** list of datatypes (subtypes of InformationObject) that will be displayed to the user on IO creation */
   public static final Set<String> ADDITIONAL_DATATYPES = new HashSet<String>();

   /** factory used for creation of datamodel objects */
   private final DatamodelFactory datamodelFactory;

   /** icon for disabled buttons */
   private final ImageIcon disabledButtonIcon = Constants.getIcon("DISABLED");

   /** the frame that opened this IOFrame */
   private MainFrame parentFrame;

   /**
    * @param communicator
    *           communication interface used for connection to a node (INJECTED)
    * @param identifierDatabase
    *           local database of identifiers (INJECTED)
    * @param datamodelFactory
    *           factory used for creation of datamodel objects (INJECTED)
    * @param addDatatypes
    *           comma-separated list of datatypes that will be displayed to the user on IO creation (INJECTED)
    */
   @Inject
   public IOFrame(NetInfNodeConnection communicator, IdentifierDatabase identifierDatabase, DatamodelFactory datamodelFactory,
         @Named("management_addDatatypes") String addDatatypes) {
      super(Constants.getString("GUI_IOFRAME_TITLE"));
      log.trace(Constants.LOG_ENTER);

      // save injected objects
      this.communicator = communicator;
      this.identifierDatabase = identifierDatabase;
      this.datamodelFactory = datamodelFactory;
      for (String type : addDatatypes.split(",")) {
         if (!type.isEmpty()) {
            ADDITIONAL_DATATYPES.add(type);
         }
      }

      setWindowOptions();

      // top-level panels
      this.compMainPanel = new JPanel(new GridLayout(1, 1));
      this.compMainPanel.setBorder(BorderFactory.createTitledBorder(Constants.getString("GUI_IOBORDER_TITLE")));

      getContentPane().add(this.compMainPanel, BorderLayout.CENTER);
      getContentPane().add(createCommandPanel(), BorderLayout.EAST);

      // show the thing
      pack();
      center();

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Centers the Frame
    */
   private void center() {
      log.trace(Constants.LOG_ENTER);
      final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      final Point center = ge.getCenterPoint();
      final Rectangle bounds = ge.getMaximumWindowBounds();
      final int w = Math.max(bounds.width / 2, Math.min(getWidth(), bounds.width));
      final int h = Math.max(bounds.height / 2, Math.min(getHeight(), bounds.height));
      final int x = center.x - w / 2, y = center.y - h / 2;
      this.setBounds(x, y, w, h);
      if (w == bounds.width && h == bounds.height) {
         setExtendedState(Frame.MAXIMIZED_BOTH);
      }
      validate();
      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Called when user clicks the ADD button
    */
   void clickedAdd() {
      log.trace(Constants.LOG_ENTER);

      IoTreeNode parentNode = (IoTreeNode) this.compIOTree.getSelectionPath().getLastPathComponent();
      IoTreeModel treemodel = (IoTreeModel) this.compIOTree.getModel();

      log.debug("Clicked add button for " + parentNode.getType() + ":" + parentNode.toString());

      switch (parentNode.getType()) {
      case IO_IDENTIFIERDETAILS_TXT:
         IoTreeNode labelToAdd = userInputNewIdentifierLabel(parentNode);
         if (labelToAdd != null) {
            treemodel.add(labelToAdd);
         }
         break;
      case ATT_SUBATTRIBUTE_DATA:
      case IO_ATTRIBUTE_DATA:
      case IO_ATTRIBUTES_TXT:
         IoTreeNode attributeToAdd = userInputNewAttribute(parentNode);
         if (attributeToAdd != null) {
            treemodel.add(attributeToAdd);
         }
         break;
      default:
         log.debug("Clicked add on " + parentNode.getType() + ", no idea what to do now");
         break;
      }

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Called when the user clicks the CANCEL button
    */
   void clickedCancel() {
      log.trace(Constants.LOG_ENTER);

      log.info("IO creation cancelled");
      IOFrame.this.setVisible(false);
      this.parentFrame.setEnabled(true);

      log.trace(Constants.LOG_EXIT);
      IOFrame.this.dispose();
   }

   /**
    * Called when the user clicks the DELETE button
    */
   void clickedDelete() {
      log.trace(Constants.LOG_ENTER);

      IoTreeNode nodeToDelete = (IoTreeNode) this.compIOTree.getSelectionPath().getLastPathComponent();
      log.debug("Clicked delete on " + nodeToDelete.getType() + ":" + nodeToDelete.toString());
      IoTreeModel treemodel = (IoTreeModel) this.compIOTree.getModel();
      treemodel.delete(nodeToDelete);
      this.compDeleteButton.setEnabled(false);
      this.compEditButton.setEnabled(false);
      this.compAddButton.setEnabled(false);

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Called when the user clicks the EDIT button
    */
   void clickedEdit() {
      log.trace(Constants.LOG_ENTER);

      IoTreeNode nodeToEdit = (IoTreeNode) this.compIOTree.getSelectionPath().getLastPathComponent();
      log.debug("Clicked edit on " + nodeToEdit.getType() + ":" + nodeToEdit.toString());

      switch (nodeToEdit.getType()) {
      case IO_ATTRIBUTE_DATA:
      case ATT_SUBATTRIBUTE_DATA:
         userInputEditAttribute(nodeToEdit);
         break;
      case IO_IDENTIFIERLABEL_DATA:
         userInputEditIdentifierLabel(nodeToEdit);
         break;
      default:
         log.debug("Clicked edit on " + nodeToEdit.getType() + ", no idea what to do");
      }

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * called when the user clicks the export button
    */
   void clickedExport() {
      log.trace(Constants.LOG_ENTER);

      InformationObject io = (InformationObject) ((IoTreeNode) this.compIOTree.getModel().getRoot()).getCarriedObject();
      String serializedIO = Utils.bytesToString(io.serializeToBytes());

      JFrame frame = new JFrame("Serialized IO");
      JTextArea textarea = new JTextArea(serializedIO, 20, 80);
      JScrollPane scrollPane = new JScrollPane(textarea);
      textarea.setEditable(false);
      frame.getContentPane().add(scrollPane);
      frame.pack();
      frame.setVisible(true);

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Called when the user clicks the submit button
    */
   void clickedSubmit() {
      log.trace(Constants.LOG_ENTER);

      InformationObject newIO = (InformationObject) ((IoTreeNode) this.compIOTree.getModel().getRoot()).getCarriedObject();
      if (!DatamodelUtils.isSyntacticallyValidIO(newIO)) {
         log.warn("This is not a syntactically valid IO, aborting");
      } else {

         log.info("Creating IO " + newIO.toString());
         boolean worked = true;
         try {
            log.log(DemoLevel.DEMO, "(GUI  ) " + newIO.describe() + " will be sent to node");
            this.communicator.putIO(newIO);
         } catch (NetInfCheckedException e) {
            worked = false;
            log.warn("Could not create IO: " + e.getMessage());
         }

         if (worked) {
            this.identifierDatabase.putIdentifier(newIO.getIdentifier(), "cre");
         }
      }
      this.parentFrame.setEnabled(true);

      log.trace(Constants.LOG_EXIT);
      setVisible(false);
      dispose();
   }

   /**
    * builds the add button
    * 
    * @return Add button
    */
   private JButton createAddButton() {
      log.trace(Constants.LOG_ENTER);

      JButton addButton = new JButton(Constants.getString("BUTTON_ADD"));
      addButton.setHorizontalAlignment(SwingConstants.LEFT);
      addButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(Constants.getString("BUTTON_ADD"))) {
               clickedAdd();
            }
         }
      });
      addButton.setEnabled(false);
      addButton.setIcon(this.disabledButtonIcon);

      log.trace(Constants.LOG_EXIT);
      return addButton;
   }

   /**
    * builds the cancel button
    * 
    * @return Cancel button
    */
   private JButton createCancelButton() {
      log.trace(Constants.LOG_ENTER);

      JButton cancelButton = new JButton(Constants.getString("BUTTON_CANCEL"));
      cancelButton.setHorizontalAlignment(SwingConstants.LEFT);
      cancelButton.setIcon(Constants.getIcon("CANCEL"));
      cancelButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(Constants.getString("BUTTON_CANCEL"))) {
               clickedCancel();
            }
         }
      });
      log.trace(Constants.LOG_EXIT);
      return cancelButton;
   }

   /**
    * Builds the command panel
    * 
    * @return Command panel
    */
   private JPanel createCommandPanel() {
      log.trace(Constants.LOG_ENTER);

      JPanel commandpanel = new JPanel(new GridLayout(6, 1));
      commandpanel.setBorder(BorderFactory.createTitledBorder(Constants.getString("GUI_COMMAND_TITLE")));

      // Edit button
      this.compEditButton = createEditButton();
      commandpanel.add(this.compEditButton);

      // Delete button
      this.compDeleteButton = createDeleteButton();
      commandpanel.add(this.compDeleteButton);

      // Add button
      this.compAddButton = createAddButton();
      commandpanel.add(this.compAddButton);

      // Submit button
      JButton mySubmitButton = createSubmitButton();
      commandpanel.add(mySubmitButton);

      // Cancel button
      JButton myCancelButton = createCancelButton();
      commandpanel.add(myCancelButton);

      // Export button
      JButton myExportButton = createExportButton();
      commandpanel.add(myExportButton);

      log.trace(Constants.LOG_EXIT);
      return commandpanel;
   }

   /**
    * Builds the delete Button
    * 
    * @return Delete button
    */
   private JButton createDeleteButton() {
      log.trace(Constants.LOG_ENTER);

      JButton deleteButton = new JButton(Constants.getString("BUTTON_DELETE"));
      deleteButton.setHorizontalAlignment(SwingConstants.LEFT);
      deleteButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(Constants.getString("BUTTON_DELETE"))) {
               clickedDelete();
            }
         }
      });
      deleteButton.setEnabled(false);
      deleteButton.setIcon(this.disabledButtonIcon);

      log.trace(Constants.LOG_EXIT);
      return deleteButton;
   }

   /**
    * Builds the edit button
    * 
    * @return Edit button
    */
   private JButton createEditButton() {
      log.trace(Constants.LOG_ENTER);

      JButton editButton = new JButton(Constants.getString("BUTTON_EDIT"));
      editButton.setHorizontalAlignment(SwingConstants.LEFT);
      editButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(Constants.getString("BUTTON_EDIT"))) {
               clickedEdit();
            }
         }
      });
      editButton.setEnabled(false);
      editButton.setIcon(this.disabledButtonIcon);
      log.trace(Constants.LOG_EXIT);
      return editButton;
   }

   /**
    * creates an export button with its listeners
    * 
    * @return new export button
    */
   private JButton createExportButton() {
      log.trace(Constants.LOG_ENTER);

      JButton exportButton = new JButton(Constants.getString("BUTTON_EXPORT"));
      exportButton.setHorizontalAlignment(SwingConstants.LEFT);
      exportButton.setIcon(Constants.getIcon("SUBMIT"));
      exportButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            clickedExport();
         }
      });
      log.trace(Constants.LOG_EXIT);
      return exportButton;
   }

   /**
    * Builds the submit button
    * 
    * @return Submit button
    */
   private JButton createSubmitButton() {
      log.trace(Constants.LOG_ENTER);

      JButton submitButton = new JButton(Constants.getString("BUTTON_SUBMIT"));
      submitButton.setHorizontalAlignment(SwingConstants.LEFT);
      submitButton.setIcon(Constants.getIcon("SUBMIT"));
      submitButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(Constants.getString("BUTTON_SUBMIT"))) {
               clickedSubmit();
            }

         }
      });
      log.trace(Constants.LOG_EXIT);
      return submitButton;
   }

   /**
    * Sets the IO to edit in this frame
    * 
    * @param io
    *           IO to edit
    */
   public void editIO(InformationObject io) {
      log.trace(Constants.LOG_ENTER);

      final JPanel treepanel = new JPanel(new BorderLayout());
      this.compIOTree = new JTree(new IoTreeModel(io));
      this.compIOTree.setCellRenderer(new IoTreeCellRenderer());
      this.compIOTree.addTreeSelectionListener(this);
      JScrollPane treeScroller = new JScrollPane(this.compIOTree);
      treepanel.add(treeScroller, BorderLayout.CENTER);
      this.compMainPanel.removeAll();
      this.compMainPanel.add(treepanel);
      this.compMainPanel.validate();

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * sets the frame that opened this IOFrame (usually the corresponding MainWindow)
    * 
    * @param parentFrame
    *           parent frame
    */
   public void setParentFrame(MainFrame parentFrame) {
      this.parentFrame = parentFrame;
   }

   /**
    * Sets basic window options (border, close actions ...)
    */
   private void setWindowOptions() {
      log.trace(Constants.LOG_ENTER);
      this.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            clickedCancel();
         }
      });
      // setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      setUndecorated(false);
      setIconImage(Constants.getIcon("IO").getImage());
      getContentPane().setLayout(new BorderLayout());
      setResizable(false);
      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Lets the user edit an Attribute
    * 
    * @param editNode
    *           tree node containing the attribute
    */
   private void userInputEditAttribute(IoTreeNode editNode) {
      log.trace(Constants.LOG_ENTER);

      // read old values
      String oldIdentification = ((Attribute) editNode.getCarriedObject()).getIdentification();
      String oldValueRaw = ((Attribute) editNode.getCarriedObject()).getValueRaw();
      String oldPurpose = ((Attribute) editNode.getCarriedObject()).getAttributePurpose();

      // settings for dialog
      String[] keys = { Constants.getString("DIALOG_ATT_IDENT"), Constants.getString("DIALOG_ATT_VALUERAW"),
            Constants.getString("DIALOG_ATT_PURPOSE") };
      DefinedAttributeIdentification[] allowedIdentificationObjects = DefinedAttributeIdentification.values();
      String[] predefinedIdentifications = new String[allowedIdentificationObjects.length];
      for (int i = 0; i < predefinedIdentifications.length; i++) {
         predefinedIdentifications[i] = allowedIdentificationObjects[i].getURI();
      }
      DefinedAttributePurpose[] allowedPurposeObjects = DefinedAttributePurpose.values();
      String[] predefinedPurposes = new String[allowedPurposeObjects.length];
      for (int i = 0; i < predefinedPurposes.length; i++) {
         predefinedPurposes[i] = allowedPurposeObjects[i].getAttributePurpose();
      }
      String[][] allowedvalues = { predefinedIdentifications, null, predefinedPurposes };
      boolean[] onlyvalid = { false, false, true };
      boolean[] allowempty = { false, false, false };
      ContentAssistant[] assistants = { null, new ValueRawAssistant(), null };
      ContentChecker[] checkers = { null, new ValueRawChecker(), null };
      String[] values = { oldIdentification, oldValueRaw, oldPurpose };

      // show the dialog
      log.debug("Showing Attribute edit dialog");
      String[] newValues = DialogHelper.showStringInputDialog(this, Constants.getString("DIALOG_ATT_SUBJ"), keys, values,
            allowedvalues, onlyvalid, allowempty, IoTreeNodeType.IO_ATTRIBUTE_DATA.getIcon(), assistants, checkers);

      if (newValues != null) {
         String newIdentification = newValues[0];
         Object objectFromRaw = ValueUtils.getObjectFromRaw(newValues[1]);
         if (objectFromRaw == null) {
            log.warn("Invalid raw value");
            log.trace(Constants.LOG_EXIT);
            return;
         }
         String newValueRaw = newValues[1];
         String newPurpose = newValues[2];
         if (!(oldIdentification.equals(newIdentification) && oldValueRaw.equals(newValueRaw) && oldPurpose.equals(newPurpose))) {
            ((IoTreeModel) this.compIOTree.getModel()).edit(editNode, newIdentification, objectFromRaw, newPurpose);

         } else {
            log.info("No change");
         }
      } else {
         log.info("Cancelled Attribute editing");
      }

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Lets the user edit an IdentifierLabel
    * 
    * @param editNode
    *           tree node containing the label
    */
   private void userInputEditIdentifierLabel(IoTreeNode editNode) {
      log.trace(Constants.LOG_ENTER);

      // read old values
      String oldName = ((IdentifierLabel) editNode.getCarriedObject()).getLabelName();
      String oldValue = ((IdentifierLabel) editNode.getCarriedObject()).getLabelValue();

      // settings for dialog
      String[] keys = { Constants.getString("DIALOG_IL_LNAME"), Constants.getString("DIALOG_IL_LVALUE") };
      DefinedLabelName[] allowedLabelNameObjects = DefinedLabelName.values();
      String[] predefinedLabelNames = new String[allowedLabelNameObjects.length];
      for (int i = 0; i < predefinedLabelNames.length; i++) {
         predefinedLabelNames[i] = allowedLabelNameObjects[i].getLabelName();
      }
      String[] values = { oldName, oldValue };
      String[][] allowedvalues = { predefinedLabelNames, null };
      boolean[] onlyvalid = { false, false };
      boolean[] allowempty = { false, false };

      // show the dialog
      String[] newValues = DialogHelper.showStringInputDialog(this, Constants.getString("DIALOG_IL_SUBJ"), keys, values,
            allowedvalues, onlyvalid, allowempty, IoTreeNodeType.IO_IDENTIFIERLABEL_DATA.getIcon(), null, null);

      if (newValues != null) {
         String newName = newValues[0];
         String newValue = newValues[1];
         if (!(oldName.equals(newName) && oldValue.equals(newValue))) {
            ((IoTreeModel) this.compIOTree.getModel()).edit(editNode, newName, newValue, null);
         } else {
            log.info("No change");
         }
      }
      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Lets the user create an Attribute
    * 
    * @param parentNode
    *           tree node that is parent of the new Attribute
    * @return new attribute node
    */
   private IoTreeNode userInputNewAttribute(IoTreeNode parentNode) {
      log.trace(Constants.LOG_ENTER);

      // settings for dialog
      String[] keys = { Constants.getString("DIALOG_ATT_IDENT"), Constants.getString("DIALOG_ATT_VALUERAW"),
            Constants.getString("DIALOG_ATT_PURPOSE") };
      DefinedAttributeIdentification[] allowedIdentificationObjects = DefinedAttributeIdentification.values();
      String[] predefinedIdentifications = new String[allowedIdentificationObjects.length];
      for (int i = 0; i < predefinedIdentifications.length; i++) {
         predefinedIdentifications[i] = allowedIdentificationObjects[i].getURI();
      }
      DefinedAttributePurpose[] allowedPurposeObjects = DefinedAttributePurpose.values();
      String[] predefinedPurposes = new String[allowedPurposeObjects.length];
      for (int i = 0; i < predefinedPurposes.length; i++) {
         predefinedPurposes[i] = allowedPurposeObjects[i].getAttributePurpose();
      }
      String[][] allowedvalues = { predefinedIdentifications, null, predefinedPurposes };
      boolean[] onlyvalid = { false, false, true };
      boolean[] allowempty = { false, false, false };
      ContentAssistant[] assistants = { null, new ValueRawAssistant(), null };
      ContentChecker[] checkers = { null, new ValueRawChecker(), null };

      // show the dialog
      String[] newValues = DialogHelper.showStringInputDialog(this, Constants.getString("DIALOG_ATT_SUBJ"), keys, null,
            allowedvalues, onlyvalid, allowempty, IoTreeNodeType.IO_ATTRIBUTE_DATA.getIcon(), assistants, checkers);

      if (newValues != null) {
         Attribute newAttribute = this.datamodelFactory.createAttribute();
         newAttribute.setIdentification(newValues[0]);
         Object objectFromRaw = ValueUtils.getObjectFromRaw(newValues[1]);
         if (objectFromRaw == null) {
            log.warn("Invalid raw value");
            log.trace(Constants.LOG_EXIT);
            return null;
         }
         newAttribute.setValue(objectFromRaw);
         newAttribute.setAttributePurpose(newValues[2]);
         IoTreeNode newNode = null;
         switch (parentNode.getType()) {
         case IO_ATTRIBUTES_TXT:
            newNode = new IoTreeNode(IoTreeNodeType.IO_ATTRIBUTE_DATA, newAttribute, parentNode);
            break;
         case ATT_SUBATTRIBUTE_DATA:
         case IO_ATTRIBUTE_DATA:
            newNode = new IoTreeNode(IoTreeNodeType.ATT_SUBATTRIBUTE_DATA, newAttribute, parentNode);
            break;
         default:
            log.debug("Can't add attribute here!");
         }

         log.trace(Constants.LOG_EXIT);
         return newNode;
      }
      log.trace(Constants.LOG_EXIT);
      return null;

   }

   /**
    * Lets the user input data for a new Identifier Label
    * 
    * @param parentNode
    *           node that will be the parent
    * @return node containing new Identifier
    */
   private IoTreeNode userInputNewIdentifierLabel(IoTreeNode parentNode) {
      log.trace(Constants.LOG_ENTER);

      // settings for dialog
      String[] keys = { Constants.getString("DIALOG_IL_LNAME"), Constants.getString("DIALOG_IL_LVALUE") };
      DefinedLabelName[] allowedLabelNameObjects = DefinedLabelName.values();
      String[] predefinedLabelNames = new String[allowedLabelNameObjects.length];
      for (int i = 0; i < predefinedLabelNames.length; i++) {
         predefinedLabelNames[i] = allowedLabelNameObjects[i].getLabelName();
      }
      String[][] allowedvalues = { predefinedLabelNames, null };
      boolean[] onlyvalid = { false, false };
      boolean[] allowempty = { false, false };

      // show the dialog
      String[] newValues = DialogHelper.showStringInputDialog(this, Constants.getString("DIALOG_IL_SUBJ"), keys, null,
            allowedvalues, onlyvalid, allowempty, IoTreeNodeType.IO_IDENTIFIERLABEL_DATA.getIcon(), null, null);

      if (newValues != null) {
         IdentifierLabel newLabel = this.datamodelFactory.createIdentifierLabel();
         newLabel.setLabelName(newValues[0]);
         newLabel.setLabelValue(newValues[1]);
         IoTreeNode newNode = new IoTreeNode(IoTreeNodeType.IO_IDENTIFIERLABEL_DATA, newLabel, parentNode);
         log.trace(Constants.LOG_EXIT);
         return newNode;
      }
      log.trace(Constants.LOG_EXIT);
      return null;

   }

   /*
    * (non-Javadoc)
    * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
    */
   @Override
   public void valueChanged(TreeSelectionEvent e) {
      log.trace(Constants.LOG_ENTER);

      IoTreeNode selNode = (IoTreeNode) (e.getPath().getLastPathComponent());
      log.debug("Selection event: " + selNode.getType() + ":" + selNode.toString());

      if (selNode.getType().isEditable()) {
         this.compEditButton.setEnabled(true);
         this.compEditButton.setIcon(selNode.getType().getIcon());
      } else {
         this.compEditButton.setEnabled(false);
         this.compEditButton.setIcon(this.disabledButtonIcon);
      }

      if (selNode.getType().isDeletable()) {
         this.compDeleteButton.setEnabled(true);
         this.compDeleteButton.setIcon(selNode.getType().getIcon());
      } else {
         this.compDeleteButton.setEnabled(false);
         this.compDeleteButton.setIcon(this.disabledButtonIcon);
      }

      if (selNode.getType().getAddableIcon() != null) {
         this.compAddButton.setEnabled(true);
         this.compAddButton.setIcon(selNode.getType().getAddableIcon());
      } else {
         this.compAddButton.setEnabled(false);
         this.compAddButton.setIcon(this.disabledButtonIcon);
      }

      log.trace(Constants.LOG_EXIT);
   }
}
