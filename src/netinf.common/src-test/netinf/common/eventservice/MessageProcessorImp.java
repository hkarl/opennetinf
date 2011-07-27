package netinf.common.eventservice;

import netinf.common.messages.ESFEventMessage;

/**
 * @author PG NetInf
 */
public class MessageProcessorImp extends AbstractMessageProcessor {

   @Override
   protected void handleESFEventMessage(ESFEventMessage eventMessage) {
      System.out.println(eventMessage.getOldInformationObject());
      System.out.println(eventMessage.getNewInformationObject());
   }
}