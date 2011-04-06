package netinf.common.eventservice;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.ESFFetchMissedEventsRequest;
import netinf.common.messages.ESFUnsubscriptionRequest;
import netinf.common.messages.NetInfMessage;

import com.google.inject.Provider;

public class AbstractEsfConnectorImp extends AbstractEsfConnector {

   MockErrorCommunicator communicator;
   private AbstractMessageProcessor procHandler;
   private MessageReceiver receiveHandler;
   private LinkedBlockingQueue<ESFEventMessage> messageQueue = new LinkedBlockingQueue<ESFEventMessage>();
   private Provider<MockErrorCommunicator> provider;
   private Identifier identifier = null;

   private List<String> subscriptionIdentifications = null;
   private List<String> subscriptionQueries = null;
   private List<Long> subscriptionExpireTimes = null;

   ESFEventMessage msg1 = new ESFEventMessage();
   ESFEventMessage msg2 = new ESFEventMessage();

   public AbstractEsfConnectorImp(DatamodelFactory dmFactory, MessageReceiver receiveHandler,
         AbstractMessageProcessor procHandler, String host, String port) {
      super(dmFactory, receiveHandler, procHandler, host, port);

   }

   public void setId(Identifier id) {

      identifier = id;
   }

   @Override
   protected boolean systemReadyToHandleReceivedMessage() {
      return true;
   }

   private boolean setup() {

      return true;

   }

   @Override
   public void run() {

      messageQueue.add(msg1);
      messageQueue.add(msg2);

      boolean result = true;

      result = systemReadyToHandleReceivedMessage();

      if (result) {
         result = setup();
      }

      if (result) {
         result = sendESFRegistrationRequest();
      }

      if (result) {
         result = sendESFSubscriptionRequest();
      }

      if (result) {
         // start message processor
         procHandler.setMessageQueue(messageQueue);
         procHandler.setName("EsfConnector_MessageProcessor");
         procHandler.start();

         // start async receiver
         receiveHandler.setMessageQueue(messageQueue);
         communicator.startAsyncReceive(receiveHandler, true);

         // send ESFFetchMissedEvents (response will be handled by async receiver)
         LOG.debug("Send ESFFetchMissedEventsRequest message");
         try {
            communicator.send(new ESFFetchMissedEventsRequest());
         } catch (NetInfCheckedException e) {
            LOG.error(e.toString());
            result = false;
         }

      }

      if (result) {
         // LOG.log(DemoLevel.DEMO, "(ESCON) I am connected to an event service at " + this.host + ":" + this.port);
      } else {
         LOG.error("Something went wrong while initializing the ESF connector. See above error message for details");
         // if connection is open, close it as cleanup
         try {
            this.communicator.close();
         } catch (Exception e) {
            // no logging necessary
         }
      }
   }

   @Override
   public void setIdentityIdentifier(Identifier identifier) {
      // TODO Auto-generated method stub
      super.setIdentityIdentifier(identifier);
   }

   @Override
   public void setInitialSubscriptionInformation(List<String> subscriptionIdentifications, List<String> subscriptionQueries,
         List<Long> subscriptionExpireTimes) {
      // TODO Auto-generated method stub
      super.setInitialSubscriptionInformation(subscriptionIdentifications, subscriptionQueries, subscriptionExpireTimes);
   }

   @Override
   public void sendSubscription(String subscriptionIdentification, String subscriptionQuery, long subscriptionExpires) {
      // TODO Auto-generated method stub
      super.sendSubscription(subscriptionIdentification, subscriptionQuery, subscriptionExpires);
   }

   @Override
   public void sendUnsubscription(String subscriptionIdentification) {
      // TODO Auto-generated method stub
      super.sendUnsubscription(subscriptionIdentification);
   }

   private boolean sendESFRegistrationRequest() {

      return true;
   }

   private boolean sendESFSubscriptionRequest() {

      return true;

   }

   private void tryRollback(final int indexOfErroneousSubscription) {
      LOG.info("Try rollback of former subscriptions");
      boolean success = true;
      for (int i = 0; i < indexOfErroneousSubscription; i++) {
         ESFUnsubscriptionRequest unsubRequest = new ESFUnsubscriptionRequest(subscriptionIdentifications.get(i));

         // send ESFUnsubscriptionRequest
         LOG.debug("Send ESFUnsubscriptionRequest message");
         try {
            communicator.send(unsubRequest);
         } catch (NetInfCheckedException e) {
            LOG.error("The following error occured while sending the ESFUnsubscriptionRequest: " + e.toString());
            success = false;
            break;
         }
         NetInfMessage message = null;
         LOG.debug("Wait for receiving ESFUnsubscriptionResponse message");
         try {
            message = communicator.receive();
         } catch (NetInfCheckedException e1) {
            LOG.error("The following error occured while waiting for / receiving ESFUnsubscriptionResponse: " + e1);
            success = false;
            break;
         }
         if (message.getErrorMessage() != null) {
            LOG.error("Received NetInf message with the following error message: " + message.getErrorMessage());
            success = false;
         }
      }
      if (success) {
         LOG.info("Rollback successful");
      } else {
         LOG.info("Rollback not successful");
      }

   }

}
