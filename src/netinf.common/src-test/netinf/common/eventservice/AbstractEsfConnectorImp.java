package netinf.common.eventservice;

import netinf.common.datamodel.DatamodelFactory;

public class AbstractEsfConnectorImp extends AbstractEsfConnector {

public AbstractEsfConnectorImp(DatamodelFactory dmFactory,
MessageReceiver receiveHandler, AbstractMessageProcessor procHandler, String host,
 String port) 
{
     super(dmFactory, receiveHandler, procHandler, host, port);

}

@Override
  protected boolean systemReadyToHandleReceivedMessage() {
    return true;
  }
}
