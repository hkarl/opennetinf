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
package netinf.node.resolution.mdht;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;

/**
 * @author PG NetInf
 */
public class AsyncRunner implements InvocationHandler {
   /**
    * @param interfaceToProxy
    *           - An interface class, used to generate a proxy.
    * @param objectToWrap
    *           - The original object, the synchronous one to wrap.
    * @param executorService
    *           - The executor service used for asynchronous execution of the implementation class.
    * @return - The proxy object, used to replace objectToWrap. This object implements the passed interface and hence can be cast
    *         to it.
    */
   @SuppressWarnings("unchecked")
   public static <T> T getInstance(Class<T> interfaceToProxy, T objectToWrap, ExecutorService executorService) {
      return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { interfaceToProxy },
            new AsyncRunner(objectToWrap, executorService));
   }

   private Object objectToWrap;
   private ExecutorService executorService;

   private AsyncRunner(Object objectToWrap, ExecutorService executorService) {
      this.objectToWrap = objectToWrap;
      this.executorService = executorService;
   }

   public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
      executorService.submit(new Runnable() {
         public void run() {
            try {
               method.invoke(objectToWrap, args);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      });
      return null;
   }

}
