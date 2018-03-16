package pl.pateman.entitygenerator.generate.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import pl.pateman.entitygenerator.GeneratedEntity;
import pl.pateman.entitygenerator.generate.Generator;

/**
 * A {@link Generator} which uses the Freemarker library for generating the source code.
 */
public class FreemarkerGenerator implements Generator {

  public static final String DEFAULT_TEMPLATES_PATH = "/templates/";
  public static final String DEFAULT_ENTITY_TEMPLATE_FILE = "defaultEntityTemplate.ftlh";

  private final Configuration configuration;

  private String templateName;

  public FreemarkerGenerator() {
    this(DEFAULT_TEMPLATES_PATH);
  }

  public FreemarkerGenerator(final String templatesRoot) {
    this.configuration = new Configuration(Configuration.VERSION_2_3_27);
    this.configuration.setClassForTemplateLoading(this.getClass(), templatesRoot);
    this.configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
    this.configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    this.configuration.setLogTemplateExceptions(false);
    this.configuration.setWrapUncheckedExceptions(true);

    this.templateName = DEFAULT_ENTITY_TEMPLATE_FILE;
  }

  @Override
  public String generateSource(final GeneratedEntity generatedEntity,
      final Map<String, Object> parameters) throws Exception {
    final Template template = this.configuration.getTemplate(templateName);
    try (final StringWriter stringWriter = new StringWriter()) {
      template.process(parameters, stringWriter);
      return stringWriter.toString();
    }
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    if (StringUtils.isBlank(templateName)) {
      throw new IllegalArgumentException("A valid template name is required");
    }
    this.templateName = templateName;
  }
}
