package lab.s2jh.core.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.persistence.EntityManager;

import lab.s2jh.core.util.DateUtils;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 *Database Data base class initialization process
 */
public abstract class DatabaseDataInitializeProcessor {

    private final static Logger logger = LoggerFactory.getLogger(DatabaseDataInitializeProcessor.class);

    private EntityManager entityManager;

    public void initialize(EntityManager entityManager) {
        this.entityManager = entityManager;

        logger.debug("Invoking data process for {}", this);
        initializeInternal();


     // Ensure finally commit the transaction
        commitAndResumeTransaction();

        if (DynamicConfigService.isDevMode()) {

        	// Reset temporary recovery time simulation data set
            DateUtils.setCurrentDate(null);
        }
    }

    /**
     * Help class method to read the text from the current class below the classpath string to String
     * @param FileName file name
     * @return
     */
    protected String getStringFromTextFile(String fileName) {
        InputStream is = this.getClass().getResourceAsStream(fileName);
        try {
            String text = IOUtils.toString(is, "UTF-8");
            return text;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    protected int executeNativeSQL(String sql) {
        return entityManager.createNativeQuery(sql).executeUpdate();
    }

    /**
     * Discover the entire data object table
     */
    @SuppressWarnings("unchecked")
    protected <X> List<X> findAll(Class<X> entity) {
        return entityManager.createQuery("from " + entity.getSimpleName()).getResultList();
    }

    /**
     * Get the total number of records of data tables
     */
    protected int countTable(Class<?> entity) {
        Object count = entityManager.createQuery("select count(1) from " + entity.getSimpleName()).getSingleResult();
        return Integer.valueOf(String.valueOf(count));
    }

    /**
     * If the entity determines the object corresponding to the table is empty
     */
    protected boolean isEmptyTable(Class<?> entity) {
        Object count = entityManager.createQuery("select count(1) from " + entity.getSimpleName()).getSingleResult();
        if (count == null || String.valueOf(count).equals("0")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Commits the current transaction and starting a new transaction
     */
    protected void commitAndResumeTransaction() {
        Session session = entityManager.unwrap(org.hibernate.Session.class);


        // Commit the current transaction
        Transaction existingTransaction = session.getTransaction();
        existingTransaction.commit();
        Assert.isTrue(existingTransaction.wasCommitted(), "Transaction should have been committed.");
        entityManager.clear();

        // Cannot reuse existing Hibernate transaction, so start a new one.
        Transaction newTransaction = session.beginTransaction();

        // Now need to update Spring transaction infrastructure with new Hibernate transaction.
        HibernateEntityManagerFactory emFactory = (HibernateEntityManagerFactory) entityManager.getEntityManagerFactory();
        SessionFactory sessionFactory = emFactory.getSessionFactory();
        SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        if (sessionHolder == null) {
            sessionHolder = new SessionHolder(session);
            TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
        }
        sessionHolder.setTransaction(newTransaction);
    }

    public abstract void initializeInternal();
}
