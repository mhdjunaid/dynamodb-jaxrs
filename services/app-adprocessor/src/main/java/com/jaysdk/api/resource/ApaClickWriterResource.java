package com.jaysdk.api.resource;

import static com.onesdk.api.platform.RequestTrackingHeader.ONESDK_RECEIVED_AT;
import static com.onesdk.api.platform.RequestTrackingHeader.ONESDK_REQUEST_ID;
import static com.onesdk.api.platform.RequestTrackingHeader.ONESDK_SOURCE_ADDRESS;
import static com.onesdk.api.platform.RequestTrackingHeader.ONESDK_SOURCE_DEVICE_FINGERPRINT;
import static com.onesdk.api.platform.RequestTrackingHeader.ONESDK_SOURCE_DEVICE_FP_INPUT;
import static com.onesdk.api.platform.RequestTrackingHeader.PUBLIC_SOURCE_REFERER;

import com.jaysdk.api.resource.helpers.ApaClickWriterResourceHelper;
import com.jaysdk.api.rest.resource.apa.ApaResourceConstants;
import com.onesdk.api.platform.util.ResponseUtils;
import com.onesdk.api.platform.util.UserAgentUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;

@Path(ApaResourceConstants.APA_CLICK_WRITER_PATH)
public class ApaClickWriterResource {

  @GET
  public Response getWithPartnerIdAndClickIdAsQueryParams(
      @Context UriInfo uriInfo,
      @Context HttpHeaders httpHeaders,
      @Size(min = 1) @NotNull @QueryParam("partner-id") String partnerId,
      @Size(min = 1) @NotNull @QueryParam("click-id") String clickId,
      @Size(min = 1) @NotNull @QueryParam("package-name") String packageName,
      @Size(min = 1) @NotNull @QueryParam("publisher-id") String publisherId,
      @QueryParam("campaign-id") String campaignId,
      @Size(min = 1) @NotNull @QueryParam("redirect-uri") String redirectUri,
      @Size(min = 1) @NotNull @HeaderParam("1sdk-rt-received-at") String receivedAt) {
    try {
      return process(uriInfo, httpHeaders, partnerId, clickId, packageName, redirectUri,
          campaignId, publisherId);
    } catch (Exception ex) {
      return ResponseUtils.create500Response(null, ex);
    }
  }

  private Response process(UriInfo uriInfo, HttpHeaders httpHeaders, String partnerId,
      String clickId, String packageName, String redirectUri, String campaignId,
      String publisherId) {
    MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    String requestId = httpHeaders.getHeaderString(ONESDK_REQUEST_ID);
    String receivedAt = httpHeaders.getHeaderString(ONESDK_RECEIVED_AT);
    String receivedFrom = httpHeaders.getHeaderString(ONESDK_SOURCE_ADDRESS);
    String fingerprintInput = httpHeaders.getHeaderString(ONESDK_SOURCE_DEVICE_FP_INPUT);
    String fingerprint = httpHeaders.getHeaderString(ONESDK_SOURCE_DEVICE_FINGERPRINT);
    String referer = httpHeaders.getHeaderString(PUBLIC_SOURCE_REFERER);
    String userAgent = UserAgentUtils.getEffectiveUserAgent(httpHeaders);

    String safeCampaignId = StringUtils.defaultIfBlank(campaignId, "MISSING");
    // Store click ID and all other crap as function value of the source
    // device finger print.
    ApaClickWriterResourceHelper.logBannerClickInflight(
        fingerprint, receivedAt, requestId, partnerId,
        clickId, packageName, redirectUri,
        fingerprintInput, receivedFrom, userAgent, referer,
        safeCampaignId, publisherId, queryParams);

    String responseBody = "To download the app, please go to \"" + redirectUri + "\".\n\n";
    String allow = "OPTIONS, GET";
    String type = MediaType.TEXT_PLAIN;
    return ResponseUtils.create302Response(responseBody, allow, type, null, redirectUri, true);
  }
}
