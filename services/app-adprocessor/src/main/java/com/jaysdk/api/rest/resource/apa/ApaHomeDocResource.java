package com.jaysdk.api.rest.resource.apa;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import com.onesdk.api.platform.StandardAttribute;
import com.onesdk.api.platform.StandardRel;
import com.onesdk.api.rest.resource.AbstractDirtyReadResource;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * class is invoked to return home doc.
 */
@Path(ApaResourceConstants.APA_HOME_PATH)
public class ApaHomeDocResource extends AbstractDirtyReadResource {

  protected String computeResponseBody(Application application, UriInfo uriInfo,
      HttpServletRequest request,
      HttpHeaders httpHeaders, SecurityContext securityContext) {

    // We only return JSON here.
    JsonObject homeDoc = new JsonObject();

    try {
      List<MediaType> acceptableMediaTypes = httpHeaders.getAcceptableMediaTypes();
      if (!acceptableMediaTypes.contains(MediaType.APPLICATION_JSON_TYPE)) {
        // According to HTTP 1.1 we MAY return JSON even if the client
        // said it is not acceptable. In this case we make a note.
        homeDoc.add("remarks", "This resource comes in JSON format only.");
      }

      // Prepare all those links.
      String baseUri = uriInfo.getBaseUri().toString();
      JsonObject links = new JsonObject();
      JsonObject selfLink = new JsonObject();
      JsonObject plainClicksLink = new JsonObject();
      JsonObject plainRegistrationsLink = new JsonObject();
      JsonObject plainKpisLink = new JsonObject();

      selfLink.add(StandardAttribute.HREF, baseUri + ApaResourceConstants.APA_HOME_PATH);
      plainClicksLink.add(StandardAttribute.HREF,
          baseUri + ApaResourceConstants.APA_BCI_PLAIN_CLICKS_PATH);
      plainRegistrationsLink.add(StandardAttribute.HREF,
          baseUri + ApaResourceConstants.APA_BCI_PLAIN_REGISTRATIONS_PATH);
      plainKpisLink.add(StandardAttribute.HREF,
          baseUri + ApaResourceConstants.APA_BCI_PLAIN_KPIS_PATH);

      links.add(StandardRel.SELF, selfLink);
      links.add(ApaResourceConstants.APA_BCI_PLAIN_CLICKS_REL, plainClicksLink);
      links.add(ApaResourceConstants.APA_BCI_PLAIN_REGISTRATIONS_REL, plainRegistrationsLink);
      links.add(ApaResourceConstants.APA_BCI_PLAIN_KPIS_REL, plainKpisLink);

      homeDoc.add("links", links);
    } catch (ParseException ex) {
      LOG.error("Problem with JSON objects: " + ex.getMessage());
    }

    return homeDoc.toString();
  }

}
