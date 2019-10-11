package com.jaysdk.api.resource.helpers;

import static com.onesdk.api.platform.persistence.DynamoDbUtils.addItemStringAttr;
import static com.onesdk.api.util.CommonClickAttributes.ALREADYUSED;
import static com.onesdk.api.util.CommonClickAttributes.FINGERPRINT;
import static com.onesdk.api.util.CommonClickAttributes.FPINPUT;
import static com.onesdk.api.util.CommonClickAttributes.PACKAGE_NAME;
import static com.onesdk.api.util.CommonClickAttributes.RECEIVEDAT;
import static com.onesdk.api.util.CommonClickAttributes.RECEIVEDATDAY;
import static com.onesdk.api.util.CommonClickAttributes.RECEIVEDATE;
import static com.onesdk.api.util.CommonClickAttributes.RECEIVEDFROM;
import static com.onesdk.api.util.CommonClickAttributes.REDIRECTURI;
import static com.onesdk.api.util.CommonClickAttributes.REFER;
import static com.onesdk.api.util.CommonClickAttributes.REQUESTID;
import static com.onesdk.api.util.CommonClickAttributes.USER_AGENT;
import static com.onesdk.api.util.CommonDeviceAttributes.ADNETWORK;
import static com.onesdk.api.util.CommonDeviceAttributes.CAMPAIGN;
import static com.onesdk.api.util.CommonDeviceAttributes.CITY;
import static com.onesdk.api.util.CommonDeviceAttributes.CLICKID;
import static com.onesdk.api.util.CommonDeviceAttributes.COUNTRY;
import static com.onesdk.api.util.CommonDeviceAttributes.PUBLISHER;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.google.common.base.CaseFormat;
import com.jaysdk.api.rest.resource.apa.ApaResourceConstants;
import com.jaysdk.api.storage.ClicksStorageUtil;
import com.onesdk.api.platform.ApiPlatformUtils;
import com.onesdk.api.platform.persistence.AppConfigurationUtils;
import com.onesdk.api.platform.persistence.DynamoDbNames;
import com.onesdk.api.platform.persistence.DynamoDbUtils;
import com.onesdk.api.platform.util.IpCountryResolveUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ApaClickWriterResourceHelper {

  private static final Logger LOG = ApaResourceConstants.APA_APPLICATION_LOGGER;
  private static final String MISSING_REFERRER = "Others";
  private static final String MISSING = "MISSING";

  private static final List stdQueryParams = Arrays.asList(
      "click-id", "package-name", "publisher-id", "campaign-id", "partner-id", "redirect-uri"
  );

  private ApaClickWriterResourceHelper() {
  }

  public static void logBannerClickInflight(
      String fingerprint,
      String receivedAt,
      String requestId,
      String partnerId,
      String clickId,
      String packageName,
      String redirectUri,
      String fingerprintInput,
      String receivedFrom,
      String userAgent,
      String referer,
      String campaignId,
      String publisherId,
      MultivaluedMap<String, String> allQueryParams) {

    Map<String, AttributeValue> item = new HashMap<>();
    addItemStringAttr(item, FINGERPRINT, fingerprint);
    addItemStringAttr(item, RECEIVEDFROM, receivedFrom);
    addItemStringAttr(item, RECEIVEDAT, receivedAt);
    addItemStringAttr(item, REQUESTID, requestId);
    addItemStringAttr(item, PACKAGE_NAME, packageName);
    addItemStringAttr(item, CLICKID, clickId);
    addItemStringAttr(item, CAMPAIGN, campaignId.replaceAll("\\s+", ""));
    addItemStringAttr(item, REDIRECTURI, redirectUri);
    addItemStringAttr(item, ALREADYUSED, Boolean.FALSE.toString());
    addItemStringAttr(item, FPINPUT, fingerprintInput);
    addItemStringAttr(item, USER_AGENT, userAgent);

    String receivedDate = new DateTime(receivedAt).withZone(DateTimeZone.UTC).toString();
    addItemStringAttr(item, RECEIVEDATE, receivedDate);

    int randomNum = new Random().nextInt(10) + 1;
    addItemStringAttr(item, RECEIVEDATDAY, receivedDate + "!" + randomNum);

    String sPartnerId = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, partnerId);
    addItemStringAttr(item, ADNETWORK, sPartnerId);

    String sPublisherId = publisherId.replaceAll("\\s+", "");
    addItemStringAttr(item, PUBLISHER, sPublisherId);

    String sReferer = StringUtils.defaultIfEmpty(referer, MISSING_REFERRER);
    addItemStringAttr(item, REFER, sReferer);

    for (String queryKey : allQueryParams.keySet()) {
      if (!stdQueryParams.contains(queryKey)) {
        String queryVal = allQueryParams.getFirst(queryKey);
        LOG.info("Bannerclick extra qs param: " + queryKey + " : " + queryVal);
        addItemStringAttr(item, queryKey, queryVal);
      }
    }

    String countryName = MISSING;
    String cityName = MISSING;
    if (receivedFrom != null) {
      String[] countryDetails = IpCountryResolveUtil.getCountryNameFromIp(receivedFrom);
      countryName = countryDetails[0].toUpperCase();
      cityName = countryDetails[1].toUpperCase();
      addItemStringAttr(item, COUNTRY, countryName);
      addItemStringAttr(item, CITY, cityName);
    }

    try {
      String tableName = DynamoDbNames.getBannerClicksTableName(packageName);
      PutItemRequest piRequest = new PutItemRequest().withTableName(tableName).withItem(item);
      DynamoDbUtils.getDynamoDb().putItem(piRequest);

      // Retrieve the timezone and convert time into custom
      String customTzOffset = AppConfigurationUtils.getServiceProcessingCustomTimezone(packageName);
      DateTimeZone customTz = ApiPlatformUtils.getTimezoneForOffset(LOG, customTzOffset);
      DateTime customTs = ApiPlatformUtils.tsToCustomTz(LOG, DateTime.parse(receivedAt), customTz);

      // Updating Publisher Names in Configuration table
      if (!ClicksStorageUtil.publisherExists(sPublisherId, packageName, sPartnerId)) {
        ClicksStorageUtil.storePublisherInfo(packageName, sPartnerId, sPublisherId);
      }
      // Clicks counting per campaign and publisher.
      ClicksStorageUtil.incClickCount(clickId, packageName,
          sPublisherId, sPartnerId, customTs, sReferer);
      // Clicks Counting per campaign per country
      ClicksStorageUtil.incClickCountCou(packageName,
          cityName, countryName, sPartnerId, customTs);
    } catch (AmazonServiceException ase) {
      LOG.error("Failed to process bannerclick for appid " + packageName, ase);
    }
  }
}
