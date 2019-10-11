package com.jaysdk.api.storage;

import static com.onesdk.api.platform.persistence.DynamoDbUtils.addItemStringAttr;
import static com.onesdk.api.util.CommonIndexingAttributes.QUALIFIER_COUNTRY_CITY;
import static com.onesdk.api.util.CommonIndexingAttributes.QUALIFIER_CREATIVE;
import static com.onesdk.api.util.CommonIndexingAttributes.QUALIFIER_REFERER;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.onesdk.api.platform.persistence.DynamoDbNames;
import com.onesdk.api.platform.persistence.DynamoDbUtils;
import com.onesdk.api.rest.RestNamesAndValues;
import com.onesdk.api.util.CommonAttribute;
import com.onesdk.api.util.CommonClickAttributes;
import com.onesdk.api.util.CommonDeviceAttributes;
import com.onesdk.api.util.CommonReportingAttribute;
import com.onesdk.api.util.DateUtils;
import com.onesdk.api.util.IndexingConstants;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Runs database operations to update retention and daily events aggregates
 * tables to keep track of count of installs moved to this for better
 * performance
 */
public class ClicksStorageUtil {

  private static final Logger LOG = RestNamesAndValues.RESOURCE_LOGGER;
  private static final String HASH_KEY = "Id";

  private ClicksStorageUtil() {
  }

  public static void storePublisherInfo(String packageName, String partnerId, String publisherId) {
    Map<String, AttributeValue> itemPublisher = new HashMap<>();
    String tableName = DynamoDbNames.getPublishersDetailsTableName();
    String qualifier = "appId+par://" + packageName + "/" + partnerId;
    addItemStringAttr(itemPublisher, HASH_KEY,
        "appId+par+pub://" + packageName + "/" + partnerId + "/" + publisherId);
    addItemStringAttr(itemPublisher, "qualifier", qualifier);
    addItemStringAttr(itemPublisher, CommonDeviceAttributes.PACKAGE_NAME, packageName);
    addItemStringAttr(itemPublisher, CommonDeviceAttributes.PUBLISHER, publisherId);
    try {
      PutItemRequest piRequest = new PutItemRequest().withTableName(tableName)
          .withItem(itemPublisher);
      DynamoDbUtils.getDynamoDb().putItem(piRequest);
    } catch (AmazonServiceException ase) {
      LOG.error("Failed to create publisher information for " + packageName, ase);
    }
  }

  public static boolean publisherExists(String publisherId, String packageName, String partnerId) {
    String TABLE_NAME = DynamoDbNames.getPublishersDetailsTableName();
    QueryResult qr = DynamoDbUtils.queryTableWithHashKeyAndRangeKey(TABLE_NAME, HASH_KEY,
        "appId+par+pub://" + packageName + "/" + partnerId + "/" + publisherId,
        CommonDeviceAttributes.PACKAGE_NAME, packageName);
    return CollectionUtils.isNotEmpty(qr.getItems());
  }

  public static void incClickCount(String clickId, String packageName, String publisherId,
      String partnerId, DateTime customTs, String referer) {
    String tableName = DynamoDbNames.getBannerClicksAggregationDateShiftTableName(packageName);
    String targetDay = customTs.toString().substring(0, 10);

    incClickCountPubRef(tableName, targetDay, referer, publisherId, partnerId);
    incClickCountPubCreative(tableName, targetDay, clickId, publisherId, partnerId);

    int currentHour = customTs.getHourOfDay();
    incSessionCount(currentHour, partnerId, publisherId, packageName, targetDay);
  }

  private static void incClickCountPubRef(String tableName, String targetDay, String referer,
      String publisherId, String partnerId) {
    try {
      String qualifier = createQualifier(QUALIFIER_REFERER, partnerId, publisherId, referer);
      updateItem(tableName, qualifier, targetDay);
    } catch (AmazonServiceException ase) {
      LOG.error("Failed to update click counter (par+camp+pub+ref) item in " + tableName, ase);
    }
  }

  private static void incClickCountPubCreative(String tableName, String targetDay,
      String creativeId, String publisherId, String partnerId) {
    try {
      String qualifier = createQualifier(QUALIFIER_CREATIVE, partnerId, publisherId, creativeId);
      updateItem(tableName, qualifier, targetDay);
    } catch (AmazonServiceException ase) {
      LOG.error("Failed to update click counter (par+pub+creative) item in " + tableName, ase);
    }
  }

  private static void updateItem(String tableName, String qualifier, String targetDay) {
    Map<String, AttributeValue> keys = new HashMap<>();
    keys.put(CommonClickAttributes.TARGET_DAY, new AttributeValue(targetDay));
    keys.put(CommonAttribute.QUALIFIER, new AttributeValue(qualifier));
    Map<String, AttributeValueUpdate> updates = new HashMap<>();
    updates.put(CommonReportingAttribute.CLICK_GSI,
        new AttributeValueUpdate(new AttributeValue().withS(targetDay), AttributeAction.PUT));
    updates.put(CommonReportingAttribute.CLICKS,
        new AttributeValueUpdate(new AttributeValue().withN("1"), AttributeAction.ADD));
    UpdateItemRequest updateItemRequest = new UpdateItemRequest()
        .withTableName(tableName).withAttributeUpdates(updates)
        .withKey(keys);
    DynamoDbUtils.getDynamoDb().updateItem(updateItemRequest);
  }

  private static void incClickCountParCou(String tableName,
      String targetDay,
      String cityName,
      String countryId,
      String partnerId) {
    try {
      String qualifier = createQualifier(QUALIFIER_COUNTRY_CITY,
          StringUtils.defaultIfEmpty(partnerId, "MISSING"), countryId, cityName);
      updateItem(tableName, qualifier, targetDay);
    } catch (AmazonServiceException ase) {
      LOG.error(
          "Failed to update click counter (" + QUALIFIER_COUNTRY_CITY + ") item in " + tableName,
          ase);
    }
  }

  public static void incClickCountCou(String packageName, String cityName, String countryId,
      String partnerId, DateTime customTs) {
    String tableName = DynamoDbNames.getBannerClicksAggregationDateShiftTableName(packageName);
    String targetDay = customTs.toString().substring(0, 10);
    incClickCountParCou(tableName, targetDay, cityName, countryId, partnerId);
  }

  private static void incSessionCount(int currentHour, String partnerId, String publisher,
      String packageName, String time) {
    String tableNameSession = DynamoDbNames.getClicksSessionTableName(packageName);
    String timeSlot = DateUtils.calculateTimeSlot(currentHour);
    try {

      Map<String, AttributeValue> keys = new HashMap<>();
      keys.put(CommonReportingAttribute.CLICK_DAY, new AttributeValue(time));
      String qualifier =
          IndexingConstants.CLICKS_SESSION_QUALIFIER + partnerId + "/" + publisher + "/" + timeSlot;
      keys.put(CommonAttribute.QUALIFIER, new AttributeValue(qualifier));

      Map<String, AttributeValueUpdate> updates = new HashMap<>();
      updates.put(CommonClickAttributes.TOTAL_CLICKS,
          new AttributeValueUpdate(new AttributeValue().withN("1"),
              AttributeAction.ADD));

      UpdateItemRequest updateItemRequest = new UpdateItemRequest().withTableName(tableNameSession)
          .withAttributeUpdates(updates).withKey(keys);
      DynamoDbUtils.getDynamoDb().updateItem(updateItemRequest);
    } catch (AmazonServiceException ase) {
      LOG.error("Failed to update slot based clicks counter  item in " + tableNameSession, ase);
    }
  }

  private static String createQualifier(String prefix, String... strings) {
    return prefix + StringUtils.join(strings, '/');
  }
}
