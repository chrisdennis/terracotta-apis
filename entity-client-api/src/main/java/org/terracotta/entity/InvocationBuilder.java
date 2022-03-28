/*
 *
 *  The contents of this file are subject to the Terracotta Public License Version
 *  2.0 (the "License"); You may not use this file except in compliance with the
 *  License. You may obtain a copy of the License at
 *
 *  http://terracotta.org/legal/terracotta-public-license.
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 *  the specific language governing rights and limitations under the License.
 *
 *  The Covered Software is Entity API.
 *
 *  The Initial Developer of the Covered Software is
 *  Terracotta, Inc., a Software AG company
 *
 */
package org.terracotta.entity;


import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.EnumSet.of;
import static org.terracotta.entity.InvocationCallback.Types.COMPLETE;
import static org.terracotta.entity.InvocationCallback.Types.FAILURE;
import static org.terracotta.entity.InvocationCallback.Types.RESULT;
import static org.terracotta.entity.InvocationCallback.Types.RETIRED;

/**
 * <p>Instances of this type are used to construct an invocation from the client to the server-side entity.  Each method
 * modifies the state of the instance and then returns itself so that several of these methods can be used in a chain.
 * Note that the invocation doesn't actually get sent to the server until {@link #invoke(InvocationCallback)} is
 * called.</p>
 *
 * @param <M> An {@link EntityMessage}
 * @param <R> An {@link EntityResponse}
 */
public interface InvocationBuilder<M extends EntityMessage, R extends EntityResponse> {

  /**
   * <p>Sets whether or not the invocation should be replicated to any passive servers in this stripe.</p>
   *
   * <p>If this method call is omitted from builder, the default behavior is to replicate the message.</p>
   *
   * @param requiresReplication True if the message should be replicated, false otherwise
   * @return this
   */
  InvocationBuilder<M, R> replicate(boolean requiresReplication);

  /**
   * <p>Sets the message of the invocation.</p>
   *
   * @param message A high-level {@link EntityMessage}
   * @return this
   */
  InvocationBuilder<M, R> message(M message);

  /**
   * <p>Actually sends the invocation staged in the receiver with encoded message {@link M} using
   * {@link MessageCodec<M, R>} to the server. The <code>callback</code> will get called along the way each time a
   * certain acknowledgement is reached.
   *
   * @param callback the callback on which to notify the acknowledgements.
   */
  void invoke(InvocationCallback<R> callback, Set<InvocationCallback.Types> callbacks);

  default Future<R> invoke() {
    CompletableFuture<R> future = new CompletableFuture<>();
    invoke(new InvocationCallback<R>() {

      private volatile R response;

      @Override
      public void result(R response) {
        this.response = response;
      }

      @Override
      public void complete() {
        future.complete(response);
      }

      @Override
      public void failure(Throwable failure) {
        future.completeExceptionally(failure);
      }
    }, of(RESULT, FAILURE, COMPLETE));
    return future;
  }

  default Future<R> invokeAndRetire() {
    CompletableFuture<R> future = new CompletableFuture<>();
    invoke(new InvocationCallback<R>() {

      private volatile R response;

      @Override
      public void result(R response) {
        this.response = response;
      }

      @Override
      public void retired() {
        future.complete(response);
      }

      @Override
      public void failure(Throwable failure) {
        future.completeExceptionally(failure);
      }
    }, of(RESULT, FAILURE, RETIRED));
    return future;
  }
}
