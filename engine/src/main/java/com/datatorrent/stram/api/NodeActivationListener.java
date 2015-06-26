/**
 * Copyright (C) 2015 DataTorrent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.stram.api;

import com.datatorrent.stram.engine.Node;

/**
 * When the node active state changes, listeners are notified of the changes.
 *
 * @since 0.3.5
 */
public interface NodeActivationListener
{
  /**
   * Callback to notify the listner that the node has been activated.
   *
   * @param node node which got activated.
   */
  public void activated(Node<?> node);

  /**
   * Callback to notify the listner that the node has been activated.
   *
   * @param node node which got deactivated.
   */
  public void deactivated(Node<?> node);

}
