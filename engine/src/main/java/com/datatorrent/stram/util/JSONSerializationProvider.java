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
package com.datatorrent.stram.util;

import com.datatorrent.common.util.JacksonObjectMapperProvider;
import com.datatorrent.stram.codec.LogicalPlanSerializer;
import com.datatorrent.stram.plan.logical.LogicalPlan;

/**
 * <p>JSONSerializationProvider class.</p>
 *
 * @since 2.1.0
 */
public class JSONSerializationProvider extends JacksonObjectMapperProvider
{

  public JSONSerializationProvider()
  {
    super();
    addSerializer(LogicalPlan.class, new LogicalPlanSerializer());
  }
}
