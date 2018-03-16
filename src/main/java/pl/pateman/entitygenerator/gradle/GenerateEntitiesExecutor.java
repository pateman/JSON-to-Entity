package pl.pateman.entitygenerator.gradle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import pl.pateman.entitygenerator.ClasspathEntitiesSchemaScanner;
import pl.pateman.entitygenerator.EntityGenerator;
import pl.pateman.entitygenerator.GeneratedEntity;
import pl.pateman.entitygenerator.SourceFileGenerator;
import pl.pateman.entitygenerator.generate.impl.FreemarkerGenerator;

/**
 * An example class which could be called from a Gradle scripts, which looks up entity schemas in the classpath,
 * and generates source code for them.
 */
final class GenerateEntitiesExecutor {

  public static void main(String[] args) {
    final ClasspathEntitiesSchemaScanner classpathEntitiesSchemaScanner = new ClasspathEntitiesSchemaScanner();

    final Set<String> schemas = new HashSet<>(classpathEntitiesSchemaScanner.findEntitySchemas());
    Arrays
        .stream(args)
        .skip(1)
        .filter(StringUtils::isNotBlank)
        .map(classpathEntitiesSchemaScanner::findEntitySchemas)
        .forEach(schemas::addAll);

    final Collection<InputStream> inputStreams = classpathEntitiesSchemaScanner
        .retrieveInputStreamsToResources(schemas);
    if (inputStreams.isEmpty()) {
      System.out.println("No schemas found. Aborting...");
      return;
    }

    System.out.println("Found the following schemas: " + schemas);

    final String rootDir = args[0];
    System.out.println("Outputting entities into: '" + rootDir + "'");

    final EntityGenerator entityGenerator = new EntityGenerator();
    final Collection<GeneratedEntity> generatedEntities = entityGenerator.generateEntities(inputStreams);

    final SourceFileGenerator sourceFileGenerator = new SourceFileGenerator(new FreemarkerGenerator());
    for (final GeneratedEntity generatedEntity : generatedEntities) {
      final String sourceFile = sourceFileGenerator.generateSourceFile(generatedEntity);

      final int lastDot = generatedEntity.getClassFile().lastIndexOf('.');
      final String directory = generatedEntity.getClassFile().substring(0, lastDot)
          .replace('.', '\\') + "\\";
      final String fileName =
          generatedEntity.getClassFile().substring(lastDot + 1) + ".java";

      final File srcFile = new File(rootDir + "\\" + directory + fileName);
      srcFile.getParentFile().mkdirs();
      try (final FileWriter fw = new FileWriter(srcFile)) {
        fw.write(sourceFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
