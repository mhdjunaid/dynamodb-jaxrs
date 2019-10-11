package com.jaysdk.api.storage;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.onesdk.api.platform.persistence.DynamoDbNames;
import com.onesdk.api.platform.persistence.DynamoDbUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.ws.rs.InternalServerErrorException;

public class AppLinkConfigDao {

  private static final DynamoDB dynamoDB = new DynamoDB(DynamoDbUtils.connectDynamoDb());
  private static String APPLINK_CONFIGURATION_TABLE_NAME = DynamoDbNames.getApplinkTableName();

  public Map<String, Object> dataForAppLinkId(String appLinkId) {
    Table table = dynamoDB.getTable(APPLINK_CONFIGURATION_TABLE_NAME);
    Map<String, Object> result = new HashMap<>();

    try {
      Item item = table.getItem("Id", appLinkId);
      if (item != null) {
        Logger.getLogger("").info("App-link item exists -- " + item.toJSONPretty());
        result.putAll(item.asMap());
      }
    } catch (Exception e) {
      throw new InternalServerErrorException(e.getMessage());
    }
    return result;
  }
}
