/*
 * Copyright 2021 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.akkaserverless.javasdk.replicatedentity;

import com.akkaserverless.javasdk.Reply;
import com.google.protobuf.Any;

/**
 * Low level interface for handling Replicated Entity commands.
 *
 * <p>These are instantiated by a {@link ReplicatedEntityHandlerFactory}.
 *
 * <p>Generally, this should not be used, rather, a {@link ReplicatedEntity} annotated class should
 * be used.
 */
public interface ReplicatedEntityHandler {
  /**
   * Handle the given command. During the handling of a command, a Replicated Entity may be created
   * (if not already created) and updated.
   *
   * @param command The command to handle.
   * @param context The context for the command.
   * @return A reply to the command, if any is sent.
   */
  Reply<Any> handleCommand(Any command, CommandContext context);
}
