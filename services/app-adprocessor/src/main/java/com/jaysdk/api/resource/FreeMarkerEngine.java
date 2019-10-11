package com.jaysdk.api.resource;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import java.io.IOException;
import java.io.StringWriter;

public class FreeMarkerEngine {

  private Configuration configuration = new Configuration();

  public FreeMarkerEngine() {

    configuration.setClassForTemplateLoading(getClass(), "/templates");
    configuration.setObjectWrapper(new DefaultObjectWrapper());
    configuration.setDefaultEncoding("UTF-8");
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    configuration.setIncompatibleImprovements(new Version(2, 3, 20));
    configuration.setNumberFormat("0.######");
  }

  public String process(String templateName, Object model) {
    try {

      Template template = configuration.getTemplate(templateName);
      StringWriter stringWriter = new StringWriter();
      template.process(model, stringWriter);

      return stringWriter.toString();

    } catch (IOException | TemplateException e) {
      throw new RuntimeException(e);
    }
  }
}
