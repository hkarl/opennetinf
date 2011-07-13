package netinf.node.resolution.mdht;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;

public class AsyncRunner implements InvocationHandler {	
	/**
	 * 
	 * @param interfaceToProxy - An interface class, used to generate a proxy.
	 * @param objectToWrap - The original object, the synchronous one to wrap.
	 * @param executorService - The executor service used for asynchronous execution of the implementation class.
	 * @return - The proxy object, used to replace objectToWrap. This object implements the passed interface
	 * and hence can be cast to it.
	 */
	@SuppressWarnings("unchecked")
	public static<T> T getInstance(Class<T> interfaceToProxy, T objectToWrap,
			ExecutorService executorService) {
		return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class[] {interfaceToProxy},
				new AsyncRunner(objectToWrap,executorService));
	}

	private Object objectToWrap;
	private ExecutorService executorService;
	
	private AsyncRunner(Object objectToWrap, ExecutorService executorService) {
		this.objectToWrap = objectToWrap;
		this.executorService = executorService;
	}

	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		executorService.submit(new Runnable(){
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
