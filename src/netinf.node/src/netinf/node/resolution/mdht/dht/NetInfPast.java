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
package netinf.node.resolution.mdht.dht;

import java.net.InetAddress;

import netinf.node.resolution.mdht.dht.pastry.FreePastryDHT;
import netinf.node.resolution.mdht.dht.pastry.NetInfInsertMessage;
import netinf.node.resolution.mdht.dht.pastry.NetInfLookupMessage;
import rice.Continuation;
import rice.Continuation.StandardContinuation;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.p2p.past.PastImpl;
import rice.p2p.past.PastPolicy;
import rice.p2p.past.messaging.PastMessage;
import rice.p2p.past.rawserialization.SocketStrategy;
import rice.persistence.Cache;
import rice.persistence.StorageManager;

/***
 * Customized PAST application on top of FreePastry
 * 
 * @author PG NetInf3
 */
public class NetInfPast extends PastImpl {

   private Application application;

   public NetInfPast(Node node, StorageManager manager, int replicas, String instance, Application app) {
      super(node, manager, replicas, instance);
      this.application = app;
   }

   public NetInfPast(Node node, StorageManager manager, int replicas, String instance, PastPolicy policy) {
      super(node, manager, replicas, instance, policy);
   }

   public NetInfPast(Node node, StorageManager manager, Cache backup, int replicas, String instance, PastPolicy policy,
         StorageManager trash) {
      super(node, manager, backup, replicas, instance, policy, trash);
   }

   public NetInfPast(Node node, StorageManager manager, Cache backup, int replicas, String instance, PastPolicy policy,
         StorageManager trash, boolean useOwnSocket) {
      super(node, manager, backup, replicas, instance, policy, trash, useOwnSocket);
   }

   public NetInfPast(Node node, StorageManager manager, Cache backup, int replicas, String instance, PastPolicy policy,
         StorageManager trash, SocketStrategy strategy) {
      super(node, manager, backup, replicas, instance, policy, trash, strategy);
   }

   @SuppressWarnings("unchecked")
   /**
    * Handle the event where a NetInfMessage is received by the current PAST node.
    */
   public void deliver(Id id, Message message) {
      final PastMessage msg = (PastMessage) message;
      if (!msg.isResponse() && msg instanceof NetInfLookupMessage) {
         
         final NetInfLookupMessage niMsg = (NetInfLookupMessage) msg;
         final int level = niMsg.getLevel(); // Get the level
         final Id objectId = id;
         if (this.application instanceof FreePastryDHT) {
            final FreePastryDHT fpParent = (FreePastryDHT) application;

            // If the data is here, we send the reply, as well as push a cached copy
            // back to the previous node
            storage.getObject(niMsg.getId(), new StandardContinuation<Object, Exception>(getResponseContinuation(niMsg)) {
               public void receiveResult(Object o) {
                  // LOG.info("Received object " + o + " for id " + lmsg.getId());
                  fpParent.notifyParent(objectId, level);
                  // send result back
                  parent.receiveResult(o);
               }
            });
         }
      } else {
         if (msg instanceof NetInfInsertMessage) {
            final NetInfInsertMessage imsg = (NetInfInsertMessage) msg;
            if (this.application instanceof FreePastryDHT) {
               // Need to get a reference to the FreePastry DHT "parent"  implementation.
               final FreePastryDHT fpParent = (FreePastryDHT) application;
               if (imsg.getLevel() == imsg.getMaxLevels()) {
            	   // Send the ACK message
                  fpParent.notifyParentAck(id, imsg.getAddress());
               }
            }

         }
      }
      // This is required to also handle the regular type of messages
      super.deliver(id, message);
   }

   @SuppressWarnings("unchecked")
   public void lookup(final Id id, final int level, final boolean cache, final Continuation<Object, Exception> command) {
      logger.log(" Performing lookup on " + id.toStringFull());

      storage.getObject(id, new StandardContinuation<Object, Exception>(command) {
         public void receiveResult(Object o) {
            if (o != null) {
               command.receiveResult(o);
            } else {
               // send the request across the wire, and see if the result is null or not
               sendRequest(id, new NetInfLookupMessage(getUID(), id, getLocalNodeHandle(), id, level), new NamedContinuation(
                     "NetInf LookupMessage for " + id, this) {
                  public void receiveResult(final Object o) {
                     // if we have an object, we return it
                     // otherwise, we must check all replicas in order to make sure that
                     // the object doesn't exist anywhere
                     if (o != null) {
                        // lastly, try and cache object locally for future use
                        if (cache) {
                           cache((PastContent) o, new SimpleContinuation() {
                              public void receiveResult(Object object) {
                                 command.receiveResult(o);
                              }
                           });
                        } else {
                           command.receiveResult(o);
                        }
                     } else {
                        lookupHandles(id, replicationFactor + 1, new Continuation<Object, Exception>() {
                           public void receiveResult(Object o) {
                              PastContentHandle[] handles = (PastContentHandle[]) o;

                              for (int i = 0; i < handles.length; i++) {
                                 if (handles[i] != null) {
                                    fetch(handles[i], new StandardContinuation<Object, Exception>(parent) {
                                       public void receiveResult(final Object o) {
                                          // lastly, try and cache object locally for future use
                                          if (cache) {
                                             cache((PastContent) o, new SimpleContinuation() {
                                                public void receiveResult(Object object) {
                                                   command.receiveResult(o);
                                                }
                                             });
                                          } else {
                                             command.receiveResult(o);
                                          }
                                       }
                                    });

                                    return;
                                 }
                              }

                              // there were no replicas of the object
                              command.receiveResult(null);
                           }

                           public void receiveException(Exception e) {
                              command.receiveException(e);
                           }
                        });
                     }
                  }

                  public void receiveException(Exception e) {
                     // If the lookup message failed , we then try to fetch all of the handles, just
                     // in case. This may fail too, but at least we tried.
                     receiveResult(null);
                  }
               });
            }
         }
      });
   }

   /**
    * Inserts an object with the given ID into this instance of Past. Asynchronously returns a PastException to command, if the
    * operation was unsuccessful. If the operation was successful, a Boolean[] is returned representing the responses from each of
    * the replicas which inserted the object.
    * 
    * @param obj
    *           the object to be inserted
    * @param command
    *           Command to be performed when the result is received
    */

   @SuppressWarnings({ "rawtypes", "unchecked" })
   public void insert(final PastContent obj, final Continuation command, final int level, final int maxlevels,
         final InetAddress source) {
      doInsert(obj.getId(), new MessageBuilder() {
         public PastMessage buildMessage() {
            return new NetInfInsertMessage(getUID(), obj, getLocalNodeHandle(), obj.getId(), source, level, maxlevels);
         }
      }, new StandardContinuation(command) {
         public void receiveResult(final Object array) {
            cache(obj, new SimpleContinuation() {
               public void receiveResult(Object o) {
                  parent.receiveResult(array);
               }
            });
         }
      }, socketStrategy.sendAlongSocket(SocketStrategy.TYPE_INSERT, obj));
   }
}
