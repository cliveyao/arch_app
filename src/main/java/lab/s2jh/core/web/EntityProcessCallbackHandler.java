package lab.s2jh.core.web;

/**
 * A single entity for batch data processing logic anonymous callback interface
 * @param <T>
 */
public interface EntityProcessCallbackHandler<T> {

    void processEntity(T entity) throws EntityProcessCallbackException;

    public class EntityProcessCallbackException extends Exception {

        private static final long serialVersionUID = -2803641078892909145L;

        public EntityProcessCallbackException(String msg) {
            super(msg);
        }
    }
}
