package pl.pateman.entitygenerator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import pl.pateman.entitygenerator.exception.SourceFileGeneratorException;

public final class SourceFileGenerator {

  private static final String DEFAULT_TEMPLATES_PATH = "/templates/";
  private static final String DEFAULT_ENTITY_TEMPLATE_FILE = "defaultEntityTemplate.ftlh";

  private final Configuration configuration;

  public SourceFileGenerator() {
    this(DEFAULT_TEMPLATES_PATH);
  }

  public SourceFileGenerator(final String templatesRoot) {
    this.configuration = new Configuration(Configuration.VERSION_2_3_27);
    this.configuration.setClassForTemplateLoading(this.getClass(), templatesRoot);
    this.configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
    this.configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    this.configuration.setLogTemplateExceptions(false);
    this.configuration.setWrapUncheckedExceptions(true);
  }

  public String generateSourceFile(final GeneratedEntity generatedEntity) {
    return this.generateSourceFile(generatedEntity, DEFAULT_ENTITY_TEMPLATE_FILE);
  }

  public String generateSourceFile(final GeneratedEntity generatedEntity, final String templateName) {
    try {
      final Template template = this.configuration.getTemplate(templateName);

      final Map<String, Object> params = new HashMap<>();
      params.put("entity", generatedEntity);
      params.put("packageName",
          generatedEntity.getClassFile().substring(0, generatedEntity.getClassFile().lastIndexOf('.')));

      try (final StringWriter stringWriter = new StringWriter()) {
        template.process(params, stringWriter);
        return stringWriter.toString();
      } catch (final TemplateException e) {
        throw new SourceFileGeneratorException(
            "Unable to generate source file for entity '" + generatedEntity.getName() + "'", e);
      }
    } catch (final IOException e) {
      throw new SourceFileGeneratorException("Unable to obtain template", e);
    }
  }
}
