package netinf.common.eventservice;


import java.util.Stack;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hp.hpl.jena.sparql.resultset.RDFInput;

import netinf.common.communication.Communicator;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.RSGetRequest;
import netinf.common.messages.RSGetResponse;

public class MockErrorCommunicator extends Communicator {

	   @Inject
	   @Named("stack")
       Stack<NetInfMessage> stack = new Stack<NetInfMessage>();
        
        @Override
        public void send(NetInfMessage message) throws NetInfCheckedException {
                stack.push(message);
        }

        @Override
        public NetInfMessage receive() throws NetInfCheckedException {
                NetInfMessage msg = stack.pop();
                if(msg instanceof RSGetRequest) {
                        RSGetRequest rsgr = (RSGetRequest) msg;
                        RSGetResponse rsGetResponse = new RSGetResponse();
                        rsGetResponse.setErrorMessage("MOCK ERROR");
                        return rsGetResponse;
                }
				return msg;
        }
        
        

}

