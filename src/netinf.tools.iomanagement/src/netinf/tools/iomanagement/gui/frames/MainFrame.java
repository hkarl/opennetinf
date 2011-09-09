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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedVersionKind;
import netinf.common.datamodel.DeleteMode;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.creator.ValidCreator;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.security.identity.IdentityManager;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;
import netinf.tools.iomanagement.Constants;
import netinf.tools.iomanagement.gui.DialogHelper;
import netinf.tools.iomanagement.gui.contentcheck.ContentChecker;
import netinf.tools.iomanagement.gui.contentcheck.IdentifierChecker;
import netinf.tools.iomanagement.gui.iotree.IoTreeNodeType;
import netinf.tools.iomanagement.identifierdb.IdentifierDatabase;
import netinf.tools.iomanagement.log.TextAreaAppender;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

/**
 * The Class MainFrame.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public final class MainFrame extends JFrame {
   /** Log4j Logger component */
   static final Logger log = Logger.getLogger(MainFrame.class);

   /** serialization stuff */
   private static final long serialVersionUID = 1L;

   /** Guice injector, uses IoManagementModule */
   private final Injector injector;

   /** connection used to communicate with a node */
   private final NetInfNodeConnection communicator;
   /** local Identifier database */
   private final IdentifierDatabase identifierDatabase;
   /** factory for creation of datamodel objects */
   private final DatamodelFactory datamodelFactory;
   /** identity manager used for security stuff */
   private final IdentityManager identityManager;

   /** our identity (IdO String representation) */
   private final String identity;

   /**
    * Main Window for NetInf-Management
    * 
    * @param injector
    *           Guice injector (INJECTED) FIXME injectors shouldn't be injected
    * @param communicator
    *           connection used to communicate with a node (INJECTED)
    * @param identifierDatabase
    *           local Identifier database (INJECTED)
    * @param datamodelFactory
    *           factory for creation of datamodel objects (INJECTED)
    * @param identityManager
    *           identity manager used for security stuff (INJECTED)
    * @param host
    *           hostname of the node (INJECTED)
    * @param port
    *           port of the node (INJECTED)
    * @param format
    *           serialization format (INJECTED)
    */
   @Inject
   public MainFrame(Injector injector, RemoteNodeConnection communicator, IdentifierDatabase identifierDatabase,
         DatamodelFactory datamodelFactory, IdentityManager identityManager, @Named("cc.tcp.host") String host,
         @Named("cc.tcp.port") String port, @Named("communicator_format") String format) {
      super(Constants.getString("GUI_MAINFRAME_TITLE") + " (" + host + ":" + port + ")");

      log.trace(Constants.LOG_ENTER);
      log.log(DemoLevel.DEMO, "(GUI  ) Please wait while I'm starting up");

      // save injected stuff
      this.communicator = communicator;
      communicator.setSerializeFormat(SerializeFormat.valueOf(format));
      communicator.setHostAndPort(host, Integer.parseInt(port));
      this.identifierDatabase = identifierDatabase;
      this.identifierDatabase.init();
      this.datamodelFactory = datamodelFactory;
      this.injector = injector;
      this.identityManager = identityManager;

      this.identity = createIdentity();

      // GUI stuff
      setWindowOptions();
      JPanel logAndStatusbarPanel = createLogPanel();
      JPanel compMainPanel;
      compMainPanel = new JPanel(new GridLayout(1, 1));
      compMainPanel.setBorder(BorderFactory.createTitledBorder(Constants.getString("GUI_MAINBORDER_TITLE")));
      JPanel commandpanel = createCommandPanel();
      getContentPane().add(logAndStatusbarPanel, BorderLayout.SOUTH);
      getContentPane().add(compMainPanel, BorderLayout.CENTER);
      getContentPane().add(commandpanel, BorderLayout.EAST);

      // actually showing this frame
      pack();
      center();
      setVisible(true); // really? -.- [EU]
      
      log.debug("Showing main window");
      log.log(DemoLevel.DEMO, "(GUI  ) I'm ready");
   }

   /**
    * Centers the Frame
    */
   private void center() {
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
      log.debug("Positioning the window on screen resolution " + bounds.width + "x" + bounds.height);
   }

   /**
    * Called when the user clicks Create IO
    * 
    * @param event
    *           event created by click
    */
   void clickedCreateIOButton(final ActionEvent event) {
      log.trace(Constants.LOG_ENTER);
      if (event.getActionCommand().equals(Constants.getString("BUTTON_CREATEIO"))) {
         InformationObject newIO = selectIoType();
         if (newIO != null) {
            log.info("Displaying Window for new IO");
            log.log(DemoLevel.DEMO, "(GUI  ) User wants to create a new IO");
            IOFrame newIOFrame = this.injector.getInstance(IOFrame.class);
            newIOFrame.editIO(newIO);
            newIOFrame.setParentFrame(this);
            setEnabled(false);
            newIOFrame.setVisible(true);
         }
      }
      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Called when the user clicks Display IO
    * 
    * @param e
    *           event created by click
    */
   void clickedDeleteButton(final ActionEvent e) {
      log.trace(Constants.LOG_ENTER);
      if (e.getActionCommand().equals(Constants.getString("BUTTON_DELETEIO"))) {
         String[] keys = { Constants.getString("DIALOG_DELETE_IDENT"), Constants.getString("DIALOG_DELETE_MODE") };
         Set<String> identifierStrings = new HashSet<String>();
         List<String[]> identifiers = this.identifierDatabase.getIdentifiersWithType();
         for (String[] strings : identifiers) {
            identifierStrings.add(strings[1]);
         }
         // identifierStrings.add(DatamodelUtils.identifierFromIdentity(this.identity));
         String[] proposedIdentifiers = identifierStrings.toArray(new String[identifierStrings.size()]);
         ContentChecker[] checker = { new IdentifierChecker() };
         String[] proposedModes = new String[DeleteMode.values().length];
         int i = 0;
         for (DeleteMode mode : DeleteMode.values()) {
            proposedModes[i] = mode.name();
            i++;
         }
         String[][] proposed = { proposedIdentifiers, proposedModes };
         boolean[] onlyValid = { false, true };
         boolean[] allowEmpty = { false, false };
         String[] eingabe = DialogHelper.showStringInputDialog(this, Constants.getString("DIALOG_DELETE_SUBJ"), keys, null,
               proposed, onlyValid, allowEmpty, IoTreeNodeType.IO_TXT.getIcon(), null, checker);
         if (eingabe != null) {
            deleteSingleIO(eingabe[0], eingabe[1]);
         } else {
            log.info("Cancelled IO deletion");
         }
      }
      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Called when the user clicks Display IO
    * 
    * @param e
    *           event created by click
    */
   void clickedDisplayIOButton(final ActionEvent e) {
      log.trace(Constants.LOG_ENTER);
      if (e.getActionCommand().equals(Constants.getString("BUTTON_DISPLAYIO"))) {
         String[] keys = { Constants.getString("DIALOG_DISPLAY_IDENT") };
         boolean[] onlyValid = { false };
         boolean[] allowEmpty = { false };
         Set<String> identifierStrings = new HashSet<String>();
         List<String[]> identifiers = this.identifierDatabase.getIdentifiersWithType();
         for (String[] strings : identifiers) {
            identifierStrings.add(strings[1]);
         }
         // identifierStrings.add(DatamodelUtils.identifierFromIdentity(this.identity));
         String[] proposedIdentifiers = identifierStrings.toArray(new String[identifierStrings.size()]);
         ContentChecker[] checker = { new IdentifierChecker() };
         String[][] proposed = { proposedIdentifiers };
         String[] eingabe = DialogHelper.showStringInputDialog(this, Constants.getString("DIALOG_DISPLAY_SUBJ"), keys, null,
               proposed, onlyValid, allowEmpty, IoTreeNodeType.IO_TXT.getIcon(), null, checker);
         if (eingabe != null) {
            displaySingleIO(eingabe[0]);
         } else {
            log.info("Cancelled IO display");
         }
      }
      log.trace(Constants.LOG_EXIT);
   }

   /**
    * called when the user clicks the import button
    */
   void clickedImportIOButton() {
      log.trace(Constants.LOG_ENTER);
      log.log(DemoLevel.DEMO, "(GUI  ) User wants to import an IO");

      final JFrame frame = new JFrame("Import IO");
      final JTextArea textarea = new JTextArea(20, 80);
      final JScrollPane scrollPane = new JScrollPane(textarea);

      JButton submitButton = new JButton("Import");
      submitButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            byte[] serializedIO = Utils.stringToBytes(textarea.getText());
            InformationObject io = MainFrame.this.getDatamodelFactory().createInformationObjectFromBytes(serializedIO);
            try {
               log.log(DemoLevel.DEMO, "(GUI  ) Importing IO " + io.getIdentifier());
               MainFrame.this.getCommunicator().putIO(io);
               frame.setVisible(false);
            } catch (NetInfCheckedException ex) {
               log.error(ex.getMessage());
            }
         }
      });

      frame.getContentPane().setLayout(new BorderLayout());
      frame.getContentPane().add(scrollPane, BorderLayout.NORTH);
      frame.getContentPane().add(submitButton, BorderLayout.SOUTH);
      frame.pack();
      frame.setVisible(true);

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Called when the user clicks the search button
    */
   protected void clickedSearchButton() {
      log.trace(Constants.LOG_ENTER);
      String[] keys = { Constants.getString("DIALOG_SEARCH_STR") };
      boolean[] onlyValid = { false };
      boolean[] allowEmpty = { true };
      String[] eingabe = DialogHelper.showStringInputDialog(this, Constants.getString("DIALOG_SEARCH_SUBJ"), keys, null, null,
            onlyValid, allowEmpty, Constants.getIcon("SEARCH"), null, null);
      if (eingabe == null || eingabe.length == 0 || eingabe[0].isEmpty()) {
         return;
      }
      search(eingabe[0]);

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Creates the command panel
    * 
    * @return command panel
    */
   private JPanel createCommandPanel() {
      log.trace(Constants.LOG_ENTER);

      JPanel commandpanel = new JPanel(new GridLayout(5, 1));
      commandpanel.setBorder(BorderFactory.createTitledBorder(Constants.getString("GUI_COMMAND_TITLE")));

      // Display IO button
      JButton mySingleIOButton = createDisplayIOButton();
      commandpanel.add(mySingleIOButton);

      // Create IO button
      JButton myCreateIOButton = createCreateIOButton();
      commandpanel.add(myCreateIOButton);

      // Search button
      JButton mySearchButton = createSearchButton();
      commandpanel.add(mySearchButton);

      // Delete button
      JButton myDeleteButton = createDeleteButton();
      commandpanel.add(myDeleteButton);

      // Import Button
      JButton myImportButton = createImportButton();
      commandpanel.add(myImportButton);

      log.trace(Constants.LOG_EXIT);
      return commandpanel;
   }

   /**
    * Creates the create IO button
    * 
    * @return Create IO button
    */
   private JButton createCreateIOButton() {
      log.trace(Constants.LOG_ENTER);

      JButton myCreateIOButton = new JButton(Constants.getString("BUTTON_CREATEIO"));
      myCreateIOButton.setHorizontalAlignment(SwingConstants.LEFT);
      myCreateIOButton.setIcon(Constants.getIcon("CREATEIO"));
      myCreateIOButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            clickedCreateIOButton(e);
         }
      });

      log.trace(Constants.LOG_EXIT);
      return myCreateIOButton;
   }

   /**
    * Creates the delete button
    * 
    * @return delete button
    */
   private JButton createDeleteButton() {
      log.trace(Constants.LOG_ENTER);

      JButton myDeleteButton = new JButton(Constants.getString("BUTTON_DELETEIO"));
      myDeleteButton.setHorizontalAlignment(SwingConstants.LEFT);
      myDeleteButton.setIcon(Constants.getIcon("DELETEIO"));
      myDeleteButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            clickedDeleteButton(e);
         }

      });

      log.trace(Constants.LOG_EXIT);
      return myDeleteButton;
   }

   /**
    * Creates the display IO button
    * 
    * @return display IO button
    */
   private JButton createDisplayIOButton() {
      log.trace(Constants.LOG_ENTER);

      JButton myDisplayIOButton = new JButton(Constants.getString("BUTTON_DISPLAYIO"));
      myDisplayIOButton.setHorizontalAlignment(SwingConstants.LEFT);
      myDisplayIOButton.setIcon(Constants.getIcon("DISPLAYIO"));
      myDisplayIOButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            clickedDisplayIOButton(e);
         }

      });

      log.trace(Constants.LOG_EXIT);
      return myDisplayIOButton;
   }

   /**
    * @return identity string
    */
   private String createIdentity() {
      log.trace(Constants.LOG_ENTER);

      // try to get an identifier string
      String identityString = null;
      try {
         log.info("Loading identity");
         log.log(DemoLevel.DEMO, "(GUI  ) I'm loading my user's identity");
         identityString = this.identityManager.getLocalIdentity();
      } catch (NetInfCheckedException e) {
         log.info("No identity stored, creating new identity");
         log.log(DemoLevel.DEMO, "(GUI  ) My user has no identity, creating one");
         try {
            IdentityObject newIdentity = this.identityManager.createNewMasterIdentity();
            this.communicator.putIO(newIdentity);
            log.trace(Constants.LOG_EXIT);
            String a = this.identityManager.getLocalIdentity();
            this.identifierDatabase.putIdentifier(newIdentity.getIdentifier(), "cre");
            return a;
         } catch (Exception e1) {
            log.error("Something went wrong while creating the new identity", e1);
            log.trace(Constants.LOG_EXIT);
            return null;
         }
      }
      if (identityString == null) {
         log.trace(Constants.LOG_EXIT);
         return null;
      }
      if (this.communicator instanceof RemoteNodeConnection) {
         ((RemoteNodeConnection) (this.communicator)).tearDown();
      }
      log.trace(Constants.LOG_EXIT);
      return identityString;
   }

   /**
    * creates an import button and its listeners
    * 
    * @return import button
    */
   private JButton createImportButton() {
      log.trace(Constants.LOG_ENTER);

      JButton myImportIOButton = new JButton(Constants.getString("BUTTON_IMPORTIO"));
      myImportIOButton.setHorizontalAlignment(SwingConstants.LEFT);
      myImportIOButton.setIcon(Constants.getIcon("CREATEIO"));
      myImportIOButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            clickedImportIOButton();
         }
      });

      log.trace(Constants.LOG_EXIT);
      return myImportIOButton;
   }

   /**
    * Creates the logger panel
    * 
    * @return logger panel
    */
   private JPanel createLogPanel() {
      log.trace(Constants.LOG_ENTER);

      JPanel logpanel = new JPanel();
      logpanel.setBorder(BorderFactory.createTitledBorder(Constants.getString("GUI_LOG_TITLE")));
      JTextArea logger = new JTextArea(5, 80);
      logger.setEditable(false);
      TextAreaAppender.setTextArea(logger);
      final JScrollPane loggerScroller = new JScrollPane(logger);
      logpanel.add(loggerScroller);

      log.trace(Constants.LOG_EXIT);
      return logpanel;
   }

   /**
    * Creates the search button
    * 
    * @return search button
    */
   private JButton createSearchButton() {
      log.trace(Constants.LOG_ENTER);

      JButton mySearchButton = new JButton(Constants.getString("BUTTON_SEARCH"));
      mySearchButton.setHorizontalAlignment(SwingConstants.LEFT);
      mySearchButton.setIcon(Constants.getIcon("SEARCH"));
      mySearchButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            clickedSearchButton();
         }

      });

      log.trace(Constants.LOG_EXIT);
      return mySearchButton;
   }

   /**
    * Performs a search and displays a result selection dialog
    * 
    * @param deleteStringIdentifier
    *           string representation of the Identifier to delete
    * @param deleteMode
    *           see {@link DeleteMode}
    */
   void deleteSingleIO(String deleteStringIdentifier, String deleteMode) {
      log.trace(Constants.LOG_ENTER);

      log.log(DemoLevel.DEMO, "(GUI  ) User wants to delete " + deleteStringIdentifier + " with mode " + deleteMode);

      InformationObject io;
      try {
         io = this.communicator.getIO(this.datamodelFactory.createIdentifierFromString(deleteStringIdentifier));
         this.communicator.deleteIO(io, DeleteMode.valueOf(deleteMode));
      } catch (NetInfCheckedException e) {
         log.error("Error during deletion: " + e);
         log.trace(Constants.LOG_EXIT);
         return;
      }
      this.identifierDatabase.remove(deleteStringIdentifier);
      log.trace(Constants.LOG_EXIT);
   }

   /**
    * Tells the GUI to display an IO
    * 
    * @param identifier
    *           identifier of the IO as string
    */
   void displaySingleIO(String identifier) {
      log.trace(Constants.LOG_ENTER);

      InformationObject io = null;
      try {
         Identifier ident = this.datamodelFactory.createIdentifierFromString(identifier);
         log.log(DemoLevel.DEMO, "(GUI  ) Trying to display any IO that " + ident.describe() + " to my user");
         io = this.communicator.getIO(ident);
      } catch (NetInfCheckedException e) {
         log.info("Could not get IO");
         log.log(DemoLevel.DEMO, "(GUI  ) Could not get IO");
      }
      if (io != null) {
         log.info("Displaying Window for new IO");
         IOFrame newIOFrame = this.injector.getInstance(IOFrame.class);
         this.identifierDatabase.putIdentifier(io.getIdentifier(), "man");
         newIOFrame.editIO(io);
         newIOFrame.setParentFrame(this);
         setEnabled(false);
         newIOFrame.setVisible(true);
      }

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * @return connection to the node we are working with
    */
   public NetInfNodeConnection getCommunicator() {
      return this.communicator;
   }

   /**
    * @return factory we are using to create datamodel objects
    */
   public DatamodelFactory getDatamodelFactory() {
      return this.datamodelFactory;
   }

   /**
    * @return the local identifier database we are using to store identifiers for the user
    */
   public IdentifierDatabase getIdentifierDatabase() {
      return this.identifierDatabase;
   }

   /**
    * Performs a search and displays a result selection dialog
    * 
    * @param searchString
    *           the string to search for
    */
   void search(String searchString) {
      log.trace(Constants.LOG_ENTER);

      log.info("Searching for " + searchString);

      log.log(DemoLevel.DEMO, "(GUI  ) Performing search for " + searchString);

      // perform actual search
      List<Identifier> results = null;
      try {
         results = this.communicator.performSearch(searchString, 1000);
      } catch (NetInfCheckedException e) {
         log.error("Could not search");
      }
      if (results == null || results.isEmpty()) {
         log.info("No search results");
         log.trace(Constants.LOG_EXIT);
         return;
      }

      for (Identifier singleResult : results) {
         this.identifierDatabase.putIdentifier(singleResult, "sea");
      }

      // settings for dialog
      String[] resultArray = new String[results.size()];
      for (int i = 0; i < results.size(); i++) {
         resultArray[i] = results.get(i).toString();
      }
      String[] keys = { Constants.getString("DIALOG_SEARCHRES_IDENT") };
      String[][] validValues = { resultArray };
      boolean[] onlyValid = { true };
      boolean[] allowEmpty = { false };
      Icon icon = Constants.getIcon("SEARCH");
      String[] chosen = DialogHelper.showStringInputDialog(this, Constants.getString("DIALOG_SEARCHRES_SUBJ"), keys, null,
            validValues, onlyValid, allowEmpty, icon, null, null);

      if (chosen == null) {
         log.info("Cancelled displaying of search results");
         log.trace(Constants.LOG_EXIT);
         return;
      }

      displaySingleIO(chosen[0]);
      log.trace(Constants.LOG_EXIT);
   }

   /**
    * lets the user choose an IO type
    * 
    * @return IO of that type, null on error or cancel
    */
   private InformationObject selectIoType() {
      log.trace(Constants.LOG_ENTER);

      // settings for dialog
      String[] keys = { Constants.getString("DIALOG_TYPE_TYPE"), Constants.getString("DIALOG_TYPE_VKIND"),
            Constants.getString("DIALOG_TYPE_ULABEL") };
      String[] values = { Constants.getString("IOTYPE_DEFAULT"), null, null };

      List<String> validTypes = new ArrayList<String>();
      for (String type : Constants.getString("IOTYPES").split(",")) {
         if (!type.isEmpty()) {
            validTypes.add(type);
         }
      }

      List<String> validVkinds = new ArrayList<String>();
      for (DefinedVersionKind vkind : DefinedVersionKind.values()) {
         validVkinds.add(vkind.name());
      }

      String[][] allowedvalues = { validTypes.toArray(new String[validTypes.size()]),
            validVkinds.toArray(new String[validVkinds.size()]), null };
      boolean[] onlyvalid = { true, true, false };
      boolean[] allowempty = { false, false, false };

      // display the dialog
      String[] newValues = DialogHelper.showStringInputDialog(this, Constants.getString("DIALOG_TYPE_SUBJ"), keys, values,
            allowedvalues, onlyvalid, allowempty, Constants.getIcon("CREATEIO"), null, null);

      if (newValues == null) {
         log.info("Cancelled IO creation on type selection");
         log.trace(Constants.LOG_EXIT);
         return null;
      }

      // InformationObject newIO = ValidCreator.createValidInformationObject(newValues[0], this.communicator.getIO(),
      // DefinedVersionKind.valueOf(newValues[1]), newValues[2]);
      InformationObject newIO = null;
      /*
       * try { Class<InformationObject> getClass = (Class<InformationObject>) Class.forName(newValues[0]); newIO =
       * this.datamodelFactory.createDatamodelObject(getClass); } catch (Exception e) {
       * log.error("Something bad happened while creating the IO: " + e.getMessage()); } if (newIO == null) { throw new
       * NetInfUncheckedException("Could not create IO"); }
       */

      Identifier ownerID = this.datamodelFactory.createIdentifierFromString(DatamodelUtils.identifierFromIdentity(this.identity));
      IdentityObject owner = null;
      try {
         owner = (IdentityObject) this.communicator.getIO(ownerID);
      } catch (NetInfCheckedException e) {
         log.error("Error receiving identity");
      }

      newIO = ValidCreator.createValidInformationObject(newValues[0], owner, DefinedVersionKind.valueOf(newValues[1]),
            newValues[2]);
      if (newIO != null) {
         return newIO;
      }
      log.trace(Constants.LOG_EXIT);
      return null;

   }

   /**
    * Sets options for the frame
    */
   private void setWindowOptions() {
      log.trace(Constants.LOG_ENTER);

      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            if (MainFrame.this.getIdentifierDatabase().isConnected()) {
               MainFrame.this.getIdentifierDatabase().disconnect();
            }
            log.debug("Closing main window");
         }
      });
      getContentPane().setLayout(new BorderLayout());
      setResizable(false);
      setIconImage(Constants.getIcon("TOOL").getImage());

      log.trace(Constants.LOG_EXIT);
   }
}
