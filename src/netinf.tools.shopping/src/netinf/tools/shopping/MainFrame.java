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
package netinf.tools.shopping;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.search.DefinedQueryTemplates;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.JXMapKit.DefaultProviders;
import org.jdesktop.swingx.mapviewer.DefaultWaypointRenderer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * {@link MainFrame} contains UserInterface as well as main parts of business logic of shopping tool. The Shopping Tool is an
 * integral part of Scenario 2. The Shopping Tool is used to illustrate the user's current position and surrounding shops on a
 * OpenStreetMaps map. It sends search requests to check whether products on the user's shopping list (stored in an IO) are
 * present in a near by shop.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class MainFrame extends JFrame {
   private static final long serialVersionUID = 6087288548349336906L;
   private static final Logger LOG = Logger.getLogger(MainFrame.class);

   private static final String DEFAULT_SHOPPINGLIST_IDENTIFIER = "ni:HASH_OF_PK=5e88f4ef66cc4b2c6ae7c0403dd895d1fbbe4798~"
         + "HASH_OF_PK_IDENT=SHA1~VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=Jack's ShoppingList";
   private static final int DEFAULT_RADIUS = 700;
   private static final int DEFAULT_SHOP_VIEW_RADIUS = 100000;
   private static final double DEFAULT_LATITUDE = 51.701964;
   private static final double DEFAULT_LONGITUDE = 8.76113;
   private static final int DEFAULT_ZOOM = 4;
   private static final int SEARCH_TIMEOUT = 5000;

   private List<String> productIds;
   private int radius = DEFAULT_RADIUS;
   private Identifier shoppingListIdentifier;
   private String ioLoadStatus = "Undefined status";
   private final String applicationIdentity;
   private boolean shopsInitiallyMarked = false;
   private final HashMap<String, Identifier> shoppinglistMap;

   private JXMapKit map;
   private JLabel resultLabel;
   private JList productList;
   private JComboBox identifierField;

   private Set<Waypoint> waypoints;
   private final Waypoint myWaypoint = new Waypoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);

   private final RemoteNodeConnection resolutionNodeConnection;
   private final RemoteNodeConnection searchNodeConnection;
   private final SearchThread searchThread;
   private final ShopSearchThread shopSearchThread;
   private final DatamodelFactory dmFactory;
   private final ShoppingEsfConnector esfConnector;
   private final ShoppingEsfMessageProcessor msgProc;

   @Inject
   public MainFrame(@Named("cc.tcp.host") final String host, @Named("cc.tcp.port") final int port,
         @Named("search.tcp.host") final String searchHost, @Named("search.tcp.port") final int searchPort,
         @Named("app.identity") final String appIdentity, final RemoteNodeConnection resolutionNodeConnection,
         final RemoteNodeConnection searchNodeConnection, final DatamodelFactory dmFactory,
         final ShoppingEsfConnector esfConnector, final ShoppingEsfMessageProcessor msgProc) {

      this.dmFactory = dmFactory;
      this.esfConnector = esfConnector;
      this.msgProc = msgProc;
      this.applicationIdentity = appIdentity;

      shoppinglistMap = new HashMap<String, Identifier>();

      updateShoppingListIdentifier(DEFAULT_SHOPPINGLIST_IDENTIFIER);

      this.resolutionNodeConnection = resolutionNodeConnection;
      resolutionNodeConnection.setHostAndPort(host, port);

      this.searchNodeConnection = searchNodeConnection;
      searchNodeConnection.setHostAndPort(searchHost, searchPort);

      // GUI
      setTitle("NetInf Shopping Tool");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Image img = Toolkit.getDefaultToolkit().getImage("images/4ward.png");
      setIconImage(img);
      getContentPane().setLayout(new BorderLayout());
      setResizable(true);

      // demo setup
      if (applicationIdentity.equals("ni:HASH_OF_PK=123~UNIQUE_LABEL=ShoppingToolJack")) {
         setLocation(50, 50);
         // keep default initial waypoint position
      }
      if (applicationIdentity.equals("ni:HASH_OF_PK=123~UNIQUE_LABEL=ShoppingToolPeter")) {
         setLocation(150, 150);
         myWaypoint.setPosition(new GeoPosition(51.708079, 8.780859));
      }

      Container contentPane = getContentPane();
      contentPane.add(createHeaderPanel(), BorderLayout.NORTH);
      contentPane.add(createMapPanel(), BorderLayout.CENTER);
      contentPane.add(createResultPanel(), BorderLayout.SOUTH);
      contentPane.add(createProductPanel(), BorderLayout.EAST);

      pack();

      // set minimal allowed size to initial size
      // setMinimumSize(new Dimension(getWidth(), getHeight()));

      if (applicationIdentity.equals("ni:HASH_OF_PK=123~UNIQUE_LABEL=ShoppingToolJack")) {
         updateShoppingListIdentifier("ni:HASH_OF_PK=5e88f4ef66cc4b2c6ae7c0403dd895d1fbbe4798~HASH_OF_PK_IDENT=SHA1~"
               + "VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=Jack's ShoppingList");
      }
      if (applicationIdentity.equals("ni:HASH_OF_PK=123~UNIQUE_LABEL=ShoppingToolPeter")) {
         updateShoppingListIdentifier("ni:HASH_OF_PK=5e88f4ef66cc4b2c6ae7c0403dd895d1fbbe4798~HASH_OF_PK_IDENT=SHA1~"
               + "VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=Peter's ShoppingList");
      }

      updateProductListFromIO();
      updateProductListView();

      // Shop query
      shopSearchThread = new ShopSearchThread();
      shopSearchThread.start();
      shopSearchThread.setShopRegion(myWaypoint.getPosition(), DEFAULT_SHOP_VIEW_RADIUS);

      startEsfConnector();

      // Initial query
      searchThread = new SearchThread();
      searchThread.start();
      searchThread.scheduleQuery(myWaypoint.getPosition(), radius, productIds);

   }

   private void updateShoppingListIdentifier(final String identifierString) {
      Identifier identifier = null;
      try {
         identifier = dmFactory.createIdentifierFromString(identifierString);
      } catch (Exception e) {
         LOG.warn(e);
         // append empty Identifier if given string identifier is invalid
         this.shoppingListIdentifier = dmFactory.createIdentifier();
         return;
      }
      this.shoppingListIdentifier = identifier;
   }

   void updateProductListView() {
      Vector<String> products = new Vector<String>();

      if (ioLoadStatus.equals("ok")) {
         if (productIds.size() == 0) {
            products.add("Shopping list empty");
         }
         for (String product : productIds) {
            products.add(product);
         }
      } else {
         products.add(ioLoadStatus);
      }

      productList.setListData(products);
   }

   void updateProductListFromIO() {
      List<String> products = new LinkedList<String>();

      if (shoppingListIdentifier.toString().length() > 0) {
         // identifier set => get io from node node
         InformationObject io = null;
         try {
            io = this.resolutionNodeConnection.getIO(shoppingListIdentifier);
         } catch (Exception e) {
            LOG.warn(e);
            // list of products is empty when getting Shoppinglist failed
            this.productIds = products;
            ioLoadStatus = "Error while retrieving IO";
            return;
         }

         if (io == null) {
            ioLoadStatus = "IO does not exist";
         } else {
            Attribute representsAttr = io.getSingleAttribute(DefinedAttributeIdentification.REPRESENTS.getURI());
            if (representsAttr != null) {
               if (representsAttr.getValueRaw().equals("String:ShoppingList")) {
                  List<Attribute> attributes = io.getAttribute(DefinedAttributeIdentification.PRODUCT.getURI());

                  for (Attribute attribute : attributes) {
                     products.add(attribute.getValue(String.class));
                  }
                  ioLoadStatus = "ok";
               } else {
                  ioLoadStatus = "IO does not represent a shopping list";
               }
            } else {
               ioLoadStatus = "IO does not represent a shopping list";
            }
         }
      } else {
         ioLoadStatus = "No shopping list set";
      }

      this.productIds = products;
   }

   private void startEsfConnector() {
      msgProc.setMainFrame(this);
      esfConnector.setName("esfConnector");
      esfConnector.setIdentityIdentifier(dmFactory.createIdentifierFromString(applicationIdentity));

      List<String> subscriptionIdentifications = new ArrayList<String>();
      List<String> subscriptionQueries = new ArrayList<String>();
      List<Long> subscriptionExpireTimes = new ArrayList<Long>();

      subscriptionIdentifications.add(0, "newShops");
      subscriptionQueries.add(0, "SELECT ?old ?new WHERE {?new <" + DefinedAttributeIdentification.REPRESENTS.getURI()
            + "> \"Shop\". ?new <" + DefinedAttributeIdentification.REPRESENTS.getURI() + "> ?newRepr. ?old <"
            + DefinedAttributeIdentification.REPRESENTS.getURI()
            + "> ?oldRepr. FILTER (!bound(?oldRepr)). FILTER (bound(?newRepr)).}");
      subscriptionExpireTimes.add(0, Long.valueOf(1000000000));

      subscriptionIdentifications.add(1, "newShoppinglists");
      subscriptionQueries.add(1, "SELECT ?old ?new WHERE {?new <" + DefinedAttributeIdentification.REPRESENTS.getURI()
            + "> \"ShoppingList\". ?new <" + DefinedAttributeIdentification.REPRESENTS.getURI() + "> ?newRepr. ?old <"
            + DefinedAttributeIdentification.REPRESENTS.getURI()
            + "> ?oldRepr. FILTER (!bound(?oldRepr)). FILTER (bound(?newRepr)).}");
      subscriptionExpireTimes.add(1, Long.valueOf(1000000000));

      if (shoppingListIdentifier.toString().length() > 0 && ioLoadStatus.equals("ok")) {
         subscriptionIdentifications.add(2, shoppingListIdentifier.toString());
         subscriptionQueries.add(2, "SELECT ?old ?new WHERE {?new <" + DefinedAttributeIdentification.IDENTIFIER.getURI()
               + "> \"" + shoppingListIdentifier.toString() + "\".}");
         subscriptionExpireTimes.add(2, Long.valueOf(1000000000));
      }

      esfConnector.setInitialSubscriptionInformation(subscriptionIdentifications, subscriptionQueries, subscriptionExpireTimes);
      esfConnector.start();
   }

   private Component createHeaderPanel() {
      final JLabel identifierLabel = new JLabel("ShoppingList:");

      Identifier jackId = dmFactory
            .createIdentifierFromString("ni:HASH_OF_PK=5e88f4ef66cc4b2c6ae7c0403dd895d1fbbe4798~HASH_OF_PK_IDENT=SHA1~"
                  + "VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=Jack's ShoppingList");
      String jackStr = createReadableRepresentation(jackId, "Jack's ShoppingList");
      shoppinglistMap.put(jackStr, jackId);

      Identifier peterId = dmFactory
            .createIdentifierFromString("ni:HASH_OF_PK=5e88f4ef66cc4b2c6ae7c0403dd895d1fbbe4798~HASH_OF_PK_IDENT=SHA1~"
                  + "VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=Peter's ShoppingList");
      String peterStr = createReadableRepresentation(peterId, "Peter's ShoppingList");
      shoppinglistMap.put(peterStr, peterId);

      String[] identifiers = { jackStr, peterStr };
      identifierField = new JComboBox(identifiers);

      if (applicationIdentity.equals("ni:HASH_OF_PK=123~UNIQUE_LABEL=ShoppingToolJack")) {
         identifierField.setSelectedIndex(0);
      }
      if (applicationIdentity.equals("ni:HASH_OF_PK=123~UNIQUE_LABEL=ShoppingToolPeter")) {
         identifierField.setSelectedIndex(1);
      }

      final JLabel radiusLabel = new JLabel("Radius:");
      final JTextField radiusField = new JTextField(String.valueOf(DEFAULT_RADIUS), 5);
      radiusField.setHorizontalAlignment(JTextField.RIGHT);
      final JLabel radiusSuffix = new JLabel("m");

      final JButton applyButton = new JButton("Apply");

      ActionListener listener = new ActionListener() {

         private String selectedIdentifier = "ni:HASH_OF_PK=5e88f4ef66cc4b2c6ae7c0403dd895d1fbbe4798~HASH_OF_PK_IDENT=SHA1~"
               + "VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=Jack's ShoppingList";

         @Override
         public void actionPerformed(ActionEvent e) {

            if (!shopsInitiallyMarked) {
               shopSearchThread.setShopRegion(myWaypoint.getPosition(), DEFAULT_SHOP_VIEW_RADIUS);
            }

            LOG.log(DemoLevel.DEMO, "(TOOL ) Input data changed...updating");

            Identifier oldId = shoppingListIdentifier;
            String oldStatus = ioLoadStatus;

            if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
               JComboBox cb = (JComboBox) e.getSource();
               if (cb.getItemCount() == 0) {
                  return;
               }
               String identifierItem = (String) cb.getSelectedItem();
               selectedIdentifier = shoppinglistMap.get(identifierItem).toString();
            }

            updateShoppingListIdentifier(selectedIdentifier);
            updateProductListFromIO();
            updateProductListView();

            // before going on, check whether the identifier belongs to a shopping list
            if (ioLoadStatus.equals("ok")) {
               try {
                  radius = Integer.parseInt(radiusField.getText());
               } catch (NumberFormatException ex) {
                  radius = DEFAULT_RADIUS;
                  radiusField.setText(String.valueOf(DEFAULT_RADIUS));
               }
               map.repaint();

               if (!shoppingListIdentifier.equals(oldId)) {
                  if (oldId.toString().length() > 0 && oldStatus.equals("ok")) {
                     LOG.log(DemoLevel.DEMO, "(TOOL ) Unsubscribing from infos about changes of old ShoppingList");
                     esfConnector.sendUnsubscription(oldId.toString());
                  }
                  if (shoppingListIdentifier.toString().length() > 0) {
                     LOG.log(DemoLevel.DEMO, "(TOOL ) Subscribing to infos about changes of selected ShoppingList");
                     // update esf subscriptions
                     esfConnector.sendSubscription(shoppingListIdentifier.toString(), "SELECT ?old ?new WHERE {?new <"
                           + DefinedAttributeIdentification.IDENTIFIER.getURI() + "> \"" + shoppingListIdentifier.toString()
                           + "\".}", 1000000000);
                  }
               }
               searchThread.scheduleQuery(myWaypoint.getPosition(), radius, productIds);
            } else {
               if (oldId.toString().length() > 0 && oldStatus.equals("ok")) {
                  esfConnector.sendUnsubscription(oldId.toString());
               }
            }
         }
      };

      identifierField.addActionListener(listener);
      radiusField.addActionListener(listener);
      applyButton.addActionListener(listener);

      JPanel headerPanel = new JPanel();
      headerPanel.setLayout(new FlowLayout());
      headerPanel.add(identifierLabel);
      headerPanel.add(identifierField);
      headerPanel.add(Box.createRigidArea(new Dimension(20, 0)));
      headerPanel.add(radiusLabel);
      headerPanel.add(radiusField);
      headerPanel.add(radiusSuffix);
      headerPanel.add(Box.createRigidArea(new Dimension(20, 0)));
      headerPanel.add(applyButton);

      return headerPanel;
   }

   /**
    * HowTos
    * <ul>
    * <li>{@link http://today.java.net/pub/a/today/2007/10/30/building-maps-into-swing-app-with-jxmapviewer.html}</li>
    * <li>{@link http://today.java.net/pub/a/today/2007/11/13/mapping-mashups-with-jxmapviewer.html}</li>
    * </ul>
    */
   private Component createMapPanel() {
      map = new JXMapKit();
      map.setDefaultProvider(DefaultProviders.OpenStreetMaps);
      map.setPreferredSize(new Dimension(700, 400));
      map.setAddressLocation(new GeoPosition(DEFAULT_LATITUDE, DEFAULT_LONGITUDE));
      map.setZoom(DEFAULT_ZOOM);

      final JXMapViewer mainMap = map.getMainMap();

      waypoints = new HashSet<Waypoint>();
      synchronized (waypoints) {
         waypoints.add(myWaypoint);
      }

      WaypointPainter<JXMapViewer> painter = new WaypointPainter<JXMapViewer>();
      painter.setWaypoints(waypoints);
      painter.setRenderer(new ShoppingWaypointRenderer());
      mainMap.setOverlayPainter(painter);

      mainMap.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            LOG.trace(null);

            GeoPosition position = mainMap.convertPointToGeoPosition(e.getPoint());
            myWaypoint.setPosition(position);
            map.repaint();

            LOG.log(DemoLevel.DEMO, "(TOOL ) Geo position changed");
            searchThread.scheduleQuery(position, radius, productIds);
         }
      });

      return map;
   }

   private Component createResultPanel() {
      JPanel resultPanel = new JPanel();
      resultLabel = new JLabel(" ");
      resultPanel.add(resultLabel);
      return resultPanel;
   }

   private Component createProductPanel() {
      JPanel productPanel = new JPanel(new BorderLayout(5, 5));
      productList = new JList();
      productList.setPrototypeCellValue("PROTOTYPE CELL VALUE");
      productPanel.add(productList);

      JButton removeButton = new JButton("Remove");

      removeButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            // check whether there is a product selected at all
            if (productList.getSelectedIndex() < 0) {
               return;
            }
            String selectedValue = productList.getSelectedValue().toString();
            LOG.log(DemoLevel.DEMO, "(TOOL ) Removing product '" + selectedValue + "' from ShoppingList");
            deleteProduct(selectedValue);
         };

         private void deleteProduct(String productToDelete) {
            InformationObject shoppingList;
            try {
               shoppingList = resolutionNodeConnection.getIO(shoppingListIdentifier);
            } catch (NetInfCheckedException e1) {
               LOG.error("Couldn't get ShoppingList IO for product deletion. " + e1.getMessage());
               return;
            }

            List<Attribute> products = shoppingList.getAttribute(DefinedAttributeIdentification.PRODUCT.getURI());
            for (Attribute product : products) {
               if (product.getValue(String.class).equals(productToDelete)) {
                  try {
                     shoppingList.removeAttribute(product);
                     resolutionNodeConnection.putIO(shoppingList);
                     break;
                  } catch (NetInfCheckedException e) {
                     LOG.error(e.getMessage());
                  }
               }
            }
         }
      });

      productPanel.add(removeButton, BorderLayout.SOUTH);

      return productPanel;
   }

   private class ShoppingWaypointRenderer extends DefaultWaypointRenderer {
      private final Color ovalFillColor = new Color(0, 0, 255, 64);
      private final Color ovalBorderColor = Color.BLUE;
      private final Stroke ovalBorderStroke = new BasicStroke(2);

      @Override
      public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint waypoint) {
         if (waypoint == myWaypoint) {
            // FIXME: The factor 1.4 is an approximation that works for Paderborn
            int pixelRadius = (int) (MainFrame.this.radius / 1.4 / (int) Math.pow(2, map.getZoom()));

            g.setColor(ovalFillColor);
            g.fillOval(-pixelRadius, -pixelRadius, 2 * pixelRadius, 2 * pixelRadius);

            g.setColor(ovalBorderColor);
            g.setStroke(ovalBorderStroke);
            g.drawOval(-pixelRadius, -pixelRadius, 2 * pixelRadius, 2 * pixelRadius);
         } else {
            super.paintWaypoint(g, map, waypoint);
         }

         return false;
      }
   }

   private class SearchThread extends Thread {
      private GeoPosition position;
      private int radius;
      private String product;

      public void scheduleQuery(GeoPosition position, int radius, List<String> productIds) {
         LOG.trace(null);

         this.position = position;
         this.radius = radius;
         this.product = productListToQueryString(productIds);

         synchronized (this) {
            this.notify();
         }
      }

      private String productListToQueryString(List<String> productIds) {
         StringBuilder productString = new StringBuilder();

         boolean delimiter = false;
         for (String prod : productIds) {
            if (delimiter) {
               productString.append(";");
            }
            productString.append(prod);
            delimiter = true;
         }

         return productString.toString();
      }

      @Override
      public void run() {
         while (true) {
            try {
               if (position == null) {
                  synchronized (searchThread) {
                     this.wait();
                  }
               }

               GeoPosition positionCopy = position;
               position = null;

               performSearch(positionCopy, radius, product);
            } catch (Exception e) {
               LOG.error(e.getMessage());
            }
         }
      }

      private void performSearch(GeoPosition position, int radius, String productIds) {
         LOG.trace(null);

         // perform search only if there are products to look for
         if (productIds.length() < 1) {
            LOG.log(DemoLevel.DEMO, "(TOOL ) No search for shops necessary since ShoppingList is empty");
            resultLabel.setText("No results");
            return;
         }

         try {
            LOG.log(DemoLevel.DEMO, "(TOOL ) Searching for nearby shops that offer products from the ShoppingList");
            resultLabel.setText("Loading...");

            String[] parameters = new String[] { String.valueOf(position.getLatitude()), String.valueOf(position.getLongitude()),
                  String.valueOf(radius), productIds };

            List<Identifier> identifiers;
            synchronized (searchNodeConnection) {
               identifiers = searchNodeConnection.performSearch(DefinedQueryTemplates.POSITION_BASED_SHOP_IN_RADIUS_HAS_PRODUCT,
                     parameters, SEARCH_TIMEOUT);
            }

            StringBuilder sb = new StringBuilder();
            if (identifiers != null) {
               for (Identifier identifier : identifiers) {
                  try {
                     // FIXME: We need asynchronous IO...
                     InformationObject io = resolutionNodeConnection.getIO(identifier);
                     if (io != null) {
                        // check which products of the list are available in this shop
                        // firstly, create list of available products
                        List<Attribute> productsInShop = io.getAttribute(DefinedAttributeIdentification.PRODUCT.getURI());
                        List<String> availableProducts = new ArrayList<String>();
                        List<Attribute> amountList = null;
                        for (Attribute product : productsInShop) {
                           String productName = product.getValue(java.lang.String.class);
                           amountList = product.getSubattribute(DefinedAttributeIdentification.AMOUNT.getURI());

                           switch (amountList.size()) {
                           case 0:
                              LOG.warn("Attribute \"amount\" is missing for product " + productName);
                              break;
                           case 1:
                              int amount = amountList.get(0).getValue(java.lang.Integer.class);
                              if (amount > 0) {
                                 availableProducts.add(productName);
                              }
                              break;
                           default:
                              LOG.warn("Multiple attributes \"amount\" for product " + productName);
                              break;
                           }
                        }

                        String[] requestedProducts = productIds.split(";");
                        // secondly, compare shopping list with available products
                        int foundProducts = 0;
                        for (String requestedProduct : requestedProducts) {
                           if (availableProducts.contains(requestedProduct)) {
                              foundProducts++;
                              if (foundProducts == 1) {
                                 Attribute name = io.getSingleAttribute(DefinedAttributeIdentification.NAME.getURI());
                                 if (sb.length() != 0) {
                                    sb.append(". ");
                                 }
                                 sb.append(name.getValue(String.class) + " has " + requestedProduct);
                              } else {
                                 sb.append(", " + requestedProduct);
                              }
                           }
                        }

                     }
                  } catch (NetInfCheckedException e) {
                     LOG.error(e.getMessage());
                  }
               }
            } else {
               sb.append("No Results");
            }

            resultLabel.setText(sb.toString());
            if (!sb.toString().equals("No Results")) {
               JOptionPane.showMessageDialog(null, sb.toString(), "Products from ShoppingList found",
                     JOptionPane.INFORMATION_MESSAGE);
               LOG.log(DemoLevel.DEMO, "(TOOL ) Found shops offering products from loaded ShoppingList");
            }
         } catch (NetInfCheckedException e) {
            resultLabel.setText("Error: " + e.getMessage());
            LOG.error(e.getMessage());
         }
      }
   }

   private class ShopSearchThread extends Thread {
      private GeoPosition position;
      private int radius;

      public void setShopRegion(GeoPosition position, int radius) {
         LOG.trace(null);
         LOG.log(DemoLevel.DEMO, "(TOOL ) Searching for shops and marking them on the map");

         this.position = position;
         this.radius = radius;

         synchronized (this) {
            this.notify();
         }
      }

      @Override
      public void run() {
         while (true) {
            try {
               if (position == null) {
                  synchronized (shopSearchThread) {
                     this.wait();
                  }
               }

               GeoPosition positionCopy = position;
               position = null;

               performShopSearch(positionCopy, radius);
            } catch (Exception e) {
               LOG.error(e.getMessage());
            }
         }
      }

      private void performShopSearch(GeoPosition position, int radius) {
         LOG.trace(null);

         List<Identifier> identifiers = null;

         try {
            synchronized (searchNodeConnection) {
               identifiers = searchNodeConnection.performSearch(
                     "?id netinf:represents ?blank . ?blank netinf:attributeValue 'String:Shop'.", 60000);
            }

         } catch (Exception e) {
            LOG.error("Error getting Shops. " + e.getMessage());
         }

         resetWaypoints(identifiers);
         map.repaint();
      }

      private void resetWaypoints(List<Identifier> identifiers) {
         synchronized (waypoints) {
            waypoints.clear();
            waypoints.add(myWaypoint);
         }

         if (identifiers == null) {
            return;
         }

         for (Identifier shop : identifiers) {
            InformationObject shopIO;
            try {
               shopIO = resolutionNodeConnection.getIO(shop);

               Double lat = shopIO.getSingleAttribute(DefinedAttributeIdentification.GEO_LAT.getURI()).getValue(Double.class);
               Double lon = shopIO.getSingleAttribute(DefinedAttributeIdentification.GEO_LONG.getURI()).getValue(Double.class);

               synchronized (waypoints) {
                  waypoints.add(new Waypoint(lat, lon));
               }
               shopsInitiallyMarked = true;
            } catch (NetInfCheckedException e) {
               LOG.warn("Unable to resolve IO. " + e.getMessage());
            }
         }
      }
   }

   void addWaypointToMap(final Waypoint point) {
      if (!waypoints.contains(point)) {
         synchronized (waypoints) {
            waypoints.add(point);
         }
         map.repaint();
      }
   }

   void addShoppinglist(final InformationObject io) {
      List<Attribute> attrList = io.getAttribute(DefinedAttributeIdentification.NAME.getURI());
      if (attrList.size() == 1) {
         if (!shoppinglistMap.containsValue(io.getIdentifier())) {
            String printName = createReadableRepresentation(io.getIdentifier(), attrList.get(0).getValue(String.class));
            shoppinglistMap.put(printName, io.getIdentifier());
            identifierField.addItem(printName);
         }
      }
   }

   private String createReadableRepresentation(final Identifier identifier, final String nameAttr) {
      String printName = nameAttr;
      printName += " <" + identifier.toString().substring(0, 50) + "...>";
      return printName;
   }

   Identifier getLoadedIdentifier() {
      return shoppingListIdentifier;
   }
}
