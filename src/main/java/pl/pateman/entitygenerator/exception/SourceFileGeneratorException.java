package pl.pateman.entitygenerator.exception;

public final class SourceFileGeneratorException extends RuntimeException {

  public SourceFileGeneratorException() {
    super();
  }

  public SourceFileGeneratorException(String message) {
    super(message);
  }

  public SourceFileGeneratorException(String message, Throwable cause) {
    super(message, cause);
  }

  public SourceFileGeneratorException(Throwable cause) {
    super(cause);
  }

  protected SourceFileGeneratorException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
