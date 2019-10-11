package com.jaysdk.api.resource;

import com.jaysdk.api.resource.helpers.AppLinkClickResourceHelper;
import com.jaysdk.api.rest.resource.apa.ApaResourceConstants;
import com.onesdk.api.rest.MoreHttpHeaders;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ApaResourceConstants.APPLINK_PATH)
public class AppLinkClickResource {

  private static final FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
  private static final String APPLINK_TEMPLATE_FILE_SUFFIX = "_applink.ftl";
  private AppLinkClickResourceHelper helper = new AppLinkClickResourceHelper();

  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response getAppLinkResult(
      @QueryParam("id") String appLinkId,
      @Context HttpHeaders headers) {
    Map<String, Object> appLinkData = this.helper.appLink(appLinkId);
    String platform = (String) appLinkData.get(APAResourceConstants.APPLINK_DATA_PLATFORM_FIELD);
    String output = freeMarkerEngine
        .process(platform.toLowerCase() + APPLINK_TEMPLATE_FILE_SUFFIX, appLinkData);
    this.helper.logAsClick(headers, appLinkData);
    return Response.ok(output).type(MediaType.TEXT_HTML).build();
  }

  @OPTIONS
  @Produces(MediaType.TEXT_HTML)
  public Response processOptionsMethod(@QueryParam("id") String appLinkId) {
    return Response.ok("")
        .header(MoreHttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
        .header(MoreHttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "origin, content-type, accept, authorization")
        .header(MoreHttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
        .header(MoreHttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, HEAD")
        .header(MoreHttpHeaders.ACCESS_CONTROL_MAX_AGE, "1209600")
        .type(MediaType.TEXT_HTML)
        .build();
  }
}
