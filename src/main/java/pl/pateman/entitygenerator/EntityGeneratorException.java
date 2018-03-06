package pl.pateman.entitygenerator;

public final class EntityGeneratorException extends RuntimeException {

  public EntityGeneratorException() {
  }

  public EntityGeneratorException(String message) {
    super(message);
  }

  public EntityGeneratorException(String message, Throwable cause) {
    super(message, cause);
  }

  public EntityGeneratorException(Throwable cause) {
    super(cause);
  }

  public EntityGeneratorException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
