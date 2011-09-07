/*
 * Copyright (C) 2009,2010 University of Paderborn, Computer Networks Group
 * Copyright (C) 2009,2010 Christian Dannewitz <christian.dannewitz@upb.de>
 * Copyright (C) 2009,2010 Thorsten Biermann <thorsten.biermann@upb.de>
 * 
 * Copyright (C) 2009,2010 Eduard Bauer <edebauer@mail.upb.de>
 * Copyright (C) 2009,2010 Matthias Becker <matzeb@mail.upb.de>
 * Copyright (C) 2009,2010 Frederic Beister <azamir@zitmail.uni-paderborn.de>
 * Copyright (C) 2009,2010 Nadine Dertmann <ndertmann@gmx.de>
 * Copyright (C) 2009,2010 Michael Kionka <mkionka@mail.upb.de>
 * Copyright (C) 2009,2010 Mario Mohr <mmohr@mail.upb.de>
 * Copyright (C) 2009,2010 Felix Steffen <felix.steffen@gmx.de>
 * Copyright (C) 2009,2010 Sebastian Stey <sebstey@mail.upb.de>
 * Copyright (C) 2009,2010 Steffen Weber <stwe@mail.upb.de>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.node.resolution.pastry.past;

import java.io.IOException;

import rice.Continuation;
import rice.Continuation.StandardContinuation;
import rice.environment.logging.Logger;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.NodeHandleSet;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.past.PastException;
import rice.p2p.past.PastImpl;
import rice.p2p.past.PastPolicy;
import rice.p2p.past.messaging.PastMessage;
import rice.p2p.past.rawserialization.SocketStrategy;
import rice.persistence.Cache;
import rice.persistence.StorageManager;

/**
 * This class enhances the Past Implementation with the possibility to delete entries.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class PastDeleteImpl extends PastImpl {

   /**
    * The Class PastDeleteDeserializer.
    * 
    * @author PG Augnet 2, University of Paderborn
    */
   protected class PastDeleteDeserializer extends PastDeserializer {
      @Override
      public Message deserialize(InputBuffer buf, short type, int priority, NodeHandle sender) throws IOException {
         try {
            if (type == DeleteMessage.TYPE) {
               return new DeleteMessage(buf, endpoint);
            } else {
               return super.deserialize(buf, type, priority, sender);
            }
         } catch (IOException e) {
            if (logger.level <= Logger.SEVERE) {
               logger.log("Exception in deserializer in " + PastDeleteImpl.this.endpoint.toString() + ":" + instance + " " + e);
            }
            throw e;
         }
      }
   }

   public PastDeleteImpl(Node node, StorageManager manager, int replicas, String instance) {
      super(node, manager, replicas, instance);
      this.endpoint.setDeserializer(new PastDeleteDeserializer());
   }

   public PastDeleteImpl(Node node, StorageManager manager, int replicas, String instance, PastPolicy policy) {
      super(node, manager, replicas, instance, policy);
      this.endpoint.setDeserializer(new PastDeleteDeserializer());
   }

   public PastDeleteImpl(Node node, StorageManager manager, Cache backup, int replicas, String instance, PastPolicy policy,
         StorageManager trash) {
      super(node, manager, backup, replicas, instance, policy, trash);
      this.endpoint.setDeserializer(new PastDeleteDeserializer());
   }

   public PastDeleteImpl(Node node, StorageManager manager, Cache backup, int replicas, String instance, PastPolicy policy,
         StorageManager trash, boolean useOwnSocket) {
      super(node, manager, backup, replicas, instance, policy, trash, useOwnSocket);
      this.endpoint.setDeserializer(new PastDeleteDeserializer());
   }

   public PastDeleteImpl(Node node, StorageManager manager, Cache backup, int replicas, String instance, PastPolicy policy,
         StorageManager trash, SocketStrategy strategy) {
      super(node, manager, backup, replicas, instance, policy, trash, strategy);
      this.endpoint.setDeserializer(new PastDeleteDeserializer());
   }

   @SuppressWarnings("unchecked")
   @Override
   public void deliver(Id id, Message message) {
      final PastMessage msg = (PastMessage) message;
      if (!msg.isResponse() && msg instanceof DeleteMessage) {
         DeleteMessage dmsg = (DeleteMessage) msg;
         final Id msgid = dmsg.getDestination();
         lockManager.lock(msgid, new StandardContinuation(getResponseContinuation(msg)) {
            public void receiveResult(Object result) {
               storage.unstore(msgid, new StandardContinuation(parent) {
                  public void receiveResult(Object o) {
                     getResponseContinuation(msg).receiveResult(o);
                     lockManager.unlock(msgid);
                  }
               });
            }
         });
      } else {
         super.deliver(id, message);
      }
   }

   @SuppressWarnings("unchecked")
   public void delete(final Id id, final Continuation command) {
      if (logger.level <= Logger.FINER) {
         logger.log("Deleting the object  with the id " + id);
      }

      doDelete(id, new MessageBuilder() {
         public PastMessage buildMessage() {
            return new DeleteMessage(getUID(), getLocalNodeHandle(), id);
         }
      }, new StandardContinuation(command) {
         public void receiveResult(final Object array) {
            parent.receiveResult(array);
         }
      });

   }

   @SuppressWarnings("unchecked")
   protected void doDelete(final Id id, final MessageBuilder builder, Continuation command) {
      getHandles(id, replicationFactor + 1, new StandardContinuation(command) {
         public void receiveResult(Object o) {
            NodeHandleSet replicas = (NodeHandleSet) o;
            if (logger.level <= Logger.FINER) {
               logger.log("Received replicas " + replicas + " for id " + id);
            }

            MultiContinuation multi = new MultiContinuation(parent, replicas.size()) {
               @Override
               public boolean isDone() throws Exception {
                  int numSuccess = 0;
                  for (int i = 0; i < haveResult.length; i++) {
                     if ((haveResult[i]) && (Boolean.TRUE.equals(result[i]))) {
                        numSuccess++;
                     }
                  }

                  if (numSuccess >= (SUCCESSFUL_INSERT_THRESHOLD * haveResult.length)) {
                     return true;
                  }

                  if (super.isDone()) {
                     for (int i = 0; i < result.length; i++) {
                        if (result[i] instanceof Exception && logger.level <= Logger.WARNING) {
                           logger.logException("result[" + i + "]:", (Exception) result[i]);
                        }
                     }

                     throw new PastException("Had only " + numSuccess + " successful deletes out of " + result.length
                           + " - aborting.");
                  }
                  return false;
               }

               @Override
               public Object getResult() {
                  Boolean[] b = new Boolean[result.length];
                  for (int i = 0; i < b.length; i++) {
                     b[i] = Boolean.valueOf((result[i] == null) || Boolean.TRUE.equals(result[i]));
                  }
                  return b;
               }
            };

            for (int i = 0; i < replicas.size(); i++) {
               NodeHandle handle = replicas.getHandle(i);
               PastMessage m = builder.buildMessage();
               Continuation c = new NamedContinuation("DeleteMessage to " + replicas.getHandle(i) + " for " + id, multi
                     .getSubContinuation(i));

               sendRequest(handle, m, c);

            }
         }
      });

   }

}
