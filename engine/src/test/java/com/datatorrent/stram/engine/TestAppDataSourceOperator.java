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
package com.datatorrent.stram.engine;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.common.experimental.AppData;
import com.datatorrent.common.util.BaseOperator;

public class TestAppDataSourceOperator extends BaseOperator
{
  @AppData.QueryPort
  public final transient InputPort<Object> query = new DefaultInputPort<Object>()
  {
    @Override
    final public void process(Object payload)
    {
    }
  };

  @AppData.ResultPort
  public final transient DefaultOutputPort<Object> result = new DefaultOutputPort<Object>();


  public final transient InputPort<Object> inport1 = new DefaultInputPort<Object>()
  {
    @Override
    final public void process(Object payload)
    {
    }
  };


}
