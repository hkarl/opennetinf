package netinf.common.eventservice;

import java.util.Stack;

import netinf.common.messages.NetInfMessage;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Class for a TestModule
 * 
 * @author PG NetInf 3
 */
public class TestModule extends AbstractModule {

   private Stack<NetInfMessage> stack = new Stack<NetInfMessage>();

   @Override
   protected void configure() {
      // TODO Auto-generated method stub

      this.bind(Stack.class).annotatedWith(Names.named("stack")).toInstance(stack);

   }

}
