package lab.s2jh.module.bpm.data;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Activiti database initialization data base processor
 */
public class ActivitiDatabaseDataInitialize {

    private final Logger logger = LoggerFactory.getLogger(ActivitiDatabaseDataInitialize.class);

    private DataSource dataSource;

    @PostConstruct
    public void initialize() {
        logger.info("Running " + this.getClass().getName());
        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            try {
            	// Try to execute the query , if the exception is not initialized Description
                connection.prepareStatement("select count(1) from ACT_ID_USER").execute();
                logger.info("Table ACT_ID_USER exist, skipped.");
            } catch (Exception e) {
                logger.info("VIEW ACT_ID_USER NOT exist, Initializing Activiti Identity DDL...");
             // Initialize SQL script to perform different types depending on the database
                ClassPathResource resource = new ClassPathResource("lab/s2jh/module/bpm/data/ddl_activiti.sql");
                ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator(resource);
                resourceDatabasePopulator.populate(connection);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
