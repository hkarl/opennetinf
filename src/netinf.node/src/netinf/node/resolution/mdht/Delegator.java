/**
 * 
 */
package netinf.node.resolution.mdht;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author PG NetInf3
 * @since 2011
 */
public class Delegator {

   /**
    * @param target
    *           non-null target with a bindable method
    * @param MethodName
    *           name of the method to be called
    * @return non-null EventDelegate. Returned delegate will be a dynamic proxy implementing that interface More info about
    *         proxies: http://www.webreference.com/internet/reflection/3.html
    */
   public EventDelegate build(Object target, String methodName) {
      HandlerProxy theDelegate = new HandlerProxy(target, methodName);

      Class[] interfaces = { EventDelegate.class };
      EventDelegate ret = (EventDelegate) java.lang.reflect.Proxy.newProxyInstance(target.getClass().getClassLoader(),
            interfaces, theDelegate);
      return ret;
   }

   private class HandlerProxy implements InvocationHandler {
      private Object target;
      private String methodName;
      private Method interfaceMethod;

      public HandlerProxy(Object target, String methodName) {
         this.target = target;
         this.methodName = methodName;
         // Yes, I know it's hard coded, but there's no need to generalize it just yet
         Method[] methods = EventDelegate.class.getDeclaredMethods();
         // There is only one method defined in the interface, just the one we need - called "action"
         this.interfaceMethod = methods[0];
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         if (method.getName() == this.interfaceMethod.getName()) {
            // Replace the method with the one named as in the class, but with the same parameter types
            method = this.target.getClass().getMethod(this.methodName, this.interfaceMethod.getParameterTypes());
         }
         return method.invoke(this.target, new Object[] {});
      }
   }
}
