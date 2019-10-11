package com.jaysdk.api.rest.resource;
/**
 * this class is used for load testing
 */

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.onesdk.api.rest.resource.AbstractDirtyReadResource;

@Path("/" + LoaderIoVerificationResource.VERIFICATION_TOKEN)
public class LoaderIoVerificationResource extends AbstractDirtyReadResource {

  public static final String VERIFICATION_TOKEN = "loaderio-72a2b44c6d74eeeb761077e52484ae63";

  protected String computeResponseBody(Application application, UriInfo uriInfo,
      HttpServletRequest request,
      HttpHeaders httpHeaders, SecurityContext securityContext) {
    return VERIFICATION_TOKEN;
  }

}
