package com.jaysdk.api.resource.helpers;

import com.jaysdk.api.resource.APAResourceConstants;
import com.jaysdk.api.storage.AppLinkConfigDao;
import com.jaysdk.api.utilities.OneSdkInstallReferrerUrl;
import com.onesdk.api.platform.RequestTrackingHeader;
import com.onesdk.api.platform.util.UserAgentUtils;

import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;

public class AppLinkClickResourceHelper {

  public void logAsClick(HttpHeaders headers, Map<String, Object> appLinkData) {

    String requestId = headers.getHeaderString(RequestTrackingHeader.ONESDK_REQUEST_ID);
    String receivedAt = headers.getHeaderString(RequestTrackingHeader.ONESDK_RECEIVED_AT);
    String fingerPrint = headers
        .getHeaderString(RequestTrackingHeader.ONESDK_SOURCE_DEVICE_FINGERPRINT);
    String fingerprintInput = headers
        .getHeaderString(RequestTrackingHeader.ONESDK_SOURCE_DEVICE_FP_INPUT);
    String receivedFromIp = headers.getHeaderString(RequestTrackingHeader.ONESDK_SOURCE_ADDRESS);

    String clickId = (String) appLinkData.get(APAResourceConstants.APPLINK_DATA_ID_FIELD);
    String partnerId = (String) appLinkData.get(APAResourceConstants.APPLINK_DATA_PARTNERID_FIELD);
    String publisherId = (String) appLinkData
        .get(APAResourceConstants.APPLINK_DATA_CAMPAIGNID_FIELD);
    String referrerId = (String) appLinkData.get(APAResourceConstants.APPLINK_DATA_ADSETID_FIELD);
    String packageName = (String) appLinkData.get(APAResourceConstants.APPLINK_DATA_APPID_FIELD);
    String redirectUrl = (String) appLinkData
        .get(APAResourceConstants.APPLINK_DATA_APPSTOREURL_FIELD);
    String userAgent = UserAgentUtils.getEffectiveUserAgent(headers);

    MultivaluedHashMap queryParams = new MultivaluedHashMap();
    if (appLinkData.containsKey(APAResourceConstants.GOOGLEINSTALLREFERRER_FIELD)) {
      queryParams.putSingle(
          APAResourceConstants.GOOGLEINSTALLREFERRER_FIELD,
          appLinkData.get(APAResourceConstants.GOOGLEINSTALLREFERRER_FIELD));
    }

    if (headers.getRequestHeaders()
        .containsKey(RequestTrackingHeader.REQUEST_HEADER_REFERER_FIELD)) {
      String referrer = headers.getRequestHeader(RequestTrackingHeader.REQUEST_HEADER_REFERER_FIELD)
          .get(0);
      queryParams.putSingle(APAResourceConstants.CLIENT_REFERER_FIELD, referrer);
    }

    ApaClickWriterResourceHelper.logBannerClickInflight(
        fingerPrint,
        receivedAt,
        requestId,
        partnerId,
        clickId,
        packageName,
        redirectUrl,
        fingerprintInput,
        receivedFromIp,
        userAgent,
        referrerId,
        "",
        publisherId,
        queryParams);
  }

  public Map<String, Object> appLink(String applinkId) {
    AppLinkConfigDao appLinkConfigDao = new AppLinkConfigDao();
    Map<String, Object> appLinkData = appLinkConfigDao.dataForAppLinkId(applinkId);
    String redirectUrl = (String) appLinkData
        .get(APAResourceConstants.APPLINK_DATA_REDIRECTURL_FIELD);
    String publisherId = (String) appLinkData
        .get(APAResourceConstants.APPLINK_DATA_CAMPAIGNID_FIELD);
    OneSdkInstallReferrerUrl oneSdkInstallReferrerUrl = new OneSdkInstallReferrerUrl(redirectUrl,
        publisherId);
    appLinkData.put(APAResourceConstants.APPLINK_DATA_REDIRECTURL_FIELD,
        oneSdkInstallReferrerUrl.getUrl());
    if (oneSdkInstallReferrerUrl.getInstallReferrer() != null) {
      appLinkData.put(APAResourceConstants.GOOGLEINSTALLREFERRER_FIELD,
          oneSdkInstallReferrerUrl.getInstallReferrer());
    }
    return appLinkData;
  }
}
