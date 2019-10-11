package com.jaysdk.api.rest.resource.apa;

import com.onesdk.api.rest.RestNamesAndValues;
import org.apache.log4j.Logger;

/**
 * Resource paths and link relation types ad-processor API.
 */
public class ApaResourceConstants {

  public static final Logger APA_APPLICATION_LOGGER = RestNamesAndValues.RESOURCE_LOGGER;

  public static final String APA_BASE_PATH = "ap";
  public static final String APA_HOME_PATH = APA_BASE_PATH;
  public static final String APA_CLICK_WRITER_PATH = APA_BASE_PATH + "/cw";
  public static final String APPLINK_PATH = APA_BASE_PATH + "/applink";
  public static final String APA_BCI_PLAIN_CLICKS_PATH = APA_BASE_PATH + "/bci/plain-clicks";
  public static final String APA_BCI_PLAIN_REGISTRATIONS_PATH =
      APA_BASE_PATH + "/bci/plain-registrations";
  public static final String APA_BCI_PLAIN_KPIS_PATH = APA_BASE_PATH + "/bci/plain-kpis";

  public static final String APA_BASE_REL = RestNamesAndValues.BASE_REL + "/adp";
  public static final String APA_BCI_PLAIN_CLICKS_REL = APA_BASE_REL + "/bci/plain-clicks";
  public static final String APA_BCI_PLAIN_REGISTRATIONS_REL =
      APA_BASE_REL + "/bci/plain-registrations";
  public static final String APA_BCI_PLAIN_KPIS_REL = APA_BASE_REL + "/bci/plain-kpis";
}
