package com.jaysdk.api.rest.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.onesdk.api.rest.resource.AbstractDirtyReadResource;

@Path("/mu-a34fdcfe-a02b618d-441e41ea-9231ebc4")
/**
 * this class is used for load testing.
 */
public class BlitzIoVerificationResource extends AbstractDirtyReadResource {

  protected String computeResponseBody(Application application, UriInfo uriInfo,
      HttpServletRequest request,
      HttpHeaders httpHeaders, SecurityContext securityContext) {
    return "42";
  }

}
