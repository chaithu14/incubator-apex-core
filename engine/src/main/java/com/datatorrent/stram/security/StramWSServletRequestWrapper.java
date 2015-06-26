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
package com.datatorrent.stram.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

/**
 * Borrowed from
 *
 * @since 0.9.2
 */
public class StramWSServletRequestWrapper extends HttpServletRequestWrapper {
  private final StramWSPrincipal principal;

  public StramWSServletRequestWrapper(HttpServletRequest request, StramWSPrincipal principal) {
    super(request);
    this.principal = principal;
  }

  @Override
  public Principal getUserPrincipal() {
    return principal;
  }

  @Override
  public String getRemoteUser() {
    return principal.getName();
  }

  @Override
  public boolean isUserInRole(String role) {
    //No role info so far
    return false;
  }

}
