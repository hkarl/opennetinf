package netinf.common.eventservice;

import java.util.Stack;

import netinf.common.communication.Communicator;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.RSGetRequest;
import netinf.common.messages.RSGetResponse;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author PG NetInf 3, University of Paderborn
 */
public class MockErrorCommunicator extends Communicator {

   @Inject
   @Named("stack")
   private Stack<NetInfMessage> stack = new Stack<NetInfMessage>();

   @Override
   public void send(NetInfMessage message) throws NetInfCheckedException {
      stack.push(message);
   }

   @Override
   public NetInfMessage receive() throws NetInfCheckedException {
      NetInfMessage msg = stack.pop();
      if (msg instanceof RSGetRequest) {
         RSGetRequest rsgr = (RSGetRequest) msg;
         RSGetResponse rsGetResponse = new RSGetResponse();
         rsGetResponse.setErrorMessage("MOCK ERROR");
         return rsGetResponse;
      }
      return msg;
   }

}
