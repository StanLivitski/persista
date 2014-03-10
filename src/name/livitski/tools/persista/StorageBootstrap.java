/*
 *  This file is part of Persista.
 *  Copyright © 2013, 2014 Konstantin "Stan" Livitski
 *
 *  Persista is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Additional permissions under GNU Affero GPL version 3 section 7:
 *
 *  1. If you modify this Program, or any covered work, by linking or combining
 *  it with any library or component covered by the terms of Eclipse Public
 *  License version 1.0 and/or Eclipse Distribution License version 1.0, the
 *  licensors of this Program grant you additional permission to convey the
 *  resulting work. Corresponding Source for a non-source form of such a
 *  combination shall include the source code for the aforementioned library or
 *  component as well as that of the covered work.
 *
 *  2. If you modify this Program, or any covered work, by linking or combining
 *  it with the Java Server Pages Expression Language API library (or a
 *  modified version of that library), containing parts covered by the terms of
 *  JavaServer Pages Specification License, the licensors of this Program grant
 *  you additional permission to convey the resulting work.
 *
 ******************************************************************************/
package name.livitski.tools.persista;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Table;
import javax.persistence.spi.PersistenceUnitTransactionType;

import name.livitski.tools.persista.config.DBDriverClassSetting;
import name.livitski.tools.persista.config.DBNameSetting;
import name.livitski.tools.persista.config.DBSubprotocolSetting;
import name.livitski.tools.persista.config.HibernateSQLDialectSetting;
import name.livitski.tools.persista.config.PersistenceUnitNameSetting;
import name.livitski.tools.persista.config.ReaderPasswordSetting;
import name.livitski.tools.persista.config.ReaderUserNameSetting;
import name.livitski.tools.persista.config.ServerAddressSetting;
import name.livitski.tools.persista.config.ServerPortSetting;
import name.livitski.tools.persista.config.ServerStatupCommandSetting;
import name.livitski.tools.persista.config.UpdaterPasswordSetting;
import name.livitski.tools.persista.config.UpdaterUserNameSetting;
import name.livitski.tools.persista.config.credentials.PasswordSetting;
import name.livitski.tools.persista.config.credentials.UserNameSetting;
import name.livitski.tools.persista.diagn.AbstractStorageException;
import name.livitski.tools.persista.diagn.DatabaseException;
import name.livitski.tools.persista.diagn.SchemaUpdateException;
import name.livitski.tools.persista.diagn.ServerStartException;
import name.livitski.tools.persista.diagn.Status;
import name.livitski.tools.persista.diagn.StorageConfigurationException;
import name.livitski.tools.proper2.Configuration;
import name.livitski.tools.proper2.ConfigurationException;
import name.livitski.tools.springlet.ApplicationBeanException;
import name.livitski.tools.springlet.config.ConfigurableApplicationBean;

import org.apache.commons.logging.Log;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.hibernate.ejb.packaging.PersistenceXmlLoader;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Configures, creates and makes available data stores for
 * dependent projects. You can run this class from the
 * command line to create and initialize a database. 
 * It also bootstraps database access for applications within
 * its JVM. When done accessing databases via this object,
 * dependency applications should {@link #close() close it}.
 */
public class StorageBootstrap extends ConfigurableApplicationBean<Status>
 implements Closeable
{
 public StorageBootstrap()
 {
  super(Status.OK);
 }

 /**
  * Starts the server and creates the database if requested,
  * then updates the schema as configured. This method
  * uses the {@link UserNameSetting account},
  * {@link PasswordSetting password}, 
  * {@link DBNameSetting database name}, 
  * and other connection settings from the 
  * {@link #getConfigFile() configuration file} assigned
  * to this object.
  * @see #requestDatabaseCreate(String, String)
  * @see #isServerStartRequested()
  */
 @Override
 public final void run()
 {
  try
  {
   log().info(COPYRIGHT_NOTICE);
   if (serverStartRequested)
    startServer();
   if (null != creatorUser)
   {
    createDatabase();
    setSchemaUpdateRequested(true);
   }
   if (null == db)
    openDefaultDB();
   if (isSchemaUpdateRequested())
    updateSchema();
   status = Status.OK;
  }
  catch(AbstractStorageException fault)
  {
   fault.updateBeanStatus();
   log().error(fault.getMessage(), fault);
  }
  catch(Exception internal)
  {
   log().error("Internal error: " + internal.getMessage(), internal);
   status = Status.INTERNAL;
  }
  finally
  {
   if (null != db)
    closeDB(db);
   db = null;
  }
 }

 @Override
 public void updateStatus(ApplicationBeanException ex)
 {
  if (ex instanceof AbstractStorageException)
   status = ((AbstractStorageException)ex).asStatus();
  else
   status = Status.INTERNAL;
 }

 /**
  * Releases resources used by this object. The
  * {@link #getConfig() configuration bean}, if any, remains
  * attached to the object until reset by {@link #setConfig}.  
  * {@link #createEntityManager() Entity managers} bootstrapped
  * from this object are automatically closed as well.
  */
 public void close()
 {
  if (null != db)
  {
   closeDB(db);
   db = null;
  }
  if (null != persistenceFactory)
  {
   persistenceFactory.close();
   persistenceFactory = null;
  }
 }

 /**
  * Creates an entity manager using this object's configuration.
  * Both {@link UserNameSetting user's name} and her
  * {@link PasswordSetting password} are read from the
  * {@link #getConfigFile() configuration file}. 
  * @return new entity manager. The caller is responsible for
  * {@link EntityManager#close() closing} the entity manager
  * when it is no longer needed.
  * @throws ConfigurationException if there is a problem
  * with configuration
  */
 public EntityManager createEntityManager()
 	throws StorageConfigurationException
 {
  try
  {
   final File configFile = getConfigFile(); // use the default config file if necessary
   final Configuration config = getConfig();
   final UserNameSetting userNameSetting = config.findSetting(UserNameSetting.class);
   if (!userNameSetting.isSet())
    throw new StorageConfigurationException(this,
      "Required setting '" + userNameSetting.getName()
      + "' missing from the settings file " + configFile);
   final PasswordSetting passwordSetting = config.findSetting(PasswordSetting.class);
   if (!passwordSetting.isSet())
    throw new StorageConfigurationException(this,
      "Required setting '" + passwordSetting.getName()
      + "' missing from the settings file " + configFile);
 
   final String url = jdbcURL(readSetting(DBNameSetting.class));
   if (null == persistenceFactory)
   {
    Map<String, Object> emfSettings = new TreeMap<String, Object>();
    emfSettings.put(AvailableSettings.DIALECT, readSetting(HibernateSQLDialectSetting.class).getName());
    emfSettings.put(HibernatePersistence.JDBC_DRIVER, getJDBCDriverClass().getName());
    emfSettings.put(HibernatePersistence.JDBC_PASSWORD, passwordSetting.getValue());
    emfSettings.put(HibernatePersistence.JDBC_USER, userNameSetting.getValue());
    emfSettings.put(HibernatePersistence.JDBC_URL, url);
    persistenceFactory = Persistence.createEntityManagerFactory(getPersistenceUnit(), emfSettings);
   }
   return new EntityManager(persistenceFactory.createEntityManager());
  }
  catch (ConfigurationException badConfig)
  {
   throw new StorageConfigurationException(this, badConfig);   
  }
 }

 /**
  * Checks whether the database server is available and
  * attempts to start it on the local machine if not.
  * @throws ServerStartException if there is an error
  * during server startup
  * @throws ConfigurationException if there is a problem
  * with configuration
  */
 public void startServer()
 	throws ServerStartException, ConfigurationException
 {
  boolean wasOpen = (null != db);
  try
  {
   String user, pass;
   if (null == creatorPass)
   {
    user = readSetting(UserNameSetting.class);
    pass  = readSetting(PasswordSetting.class);
   }
   else
   {
    user = creatorUser;
    pass = creatorPass;
   }
   if (!wasOpen)
    db = openDB(null, user, pass);
   if (null != db)
   {
    log().info("Database server is running, skipping server launch request ...");
    return;
   }
  }
  catch(ConfigurationException fault)
  {
   Throwable cause = fault.getCause();
   if (cause instanceof SQLException && "08S01".equals(((SQLException)cause).getSQLState()))
   {
    log().trace("Intercepted connection failure, proceeding with server start ...",
      cause);
   }
   else
    throw fault;
  }
  finally
  {
   if (!wasOpen && null != db)
   {
    closeDB(db);
    db = null;
   }
  }
  String command = readSetting(ServerStatupCommandSetting.class);
  log().debug("Starting local database server ...");
  Process process = null;
  try
  {
   process = Runtime.getRuntime().exec(command);
   InputStream stderr = process.getErrorStream();
   byte[] messages = new byte[2048];
   int messagesLength;
   for (messagesLength = 0; messagesLength < messages.length;)
   {
    int read = stderr.read(messages, messagesLength, messages.length - messagesLength);
    if (0 > read) break;
    messagesLength += read;
   }
   int exitCode = process.waitFor();
   process = null;
   if (0 != exitCode)
    throw new ServerStartException(this, 
      "Server launch command '" + command + "' returned code " + exitCode
      + (0 < messagesLength ? " with message(s): " + new String(messages, 0, messagesLength) : ""));
  }
  catch (InterruptedException e)
  {
   throw new ServerStartException(this, 
     "Server launch command '" + command + "' did not complete", e);
  }
  catch (IOException e)
  {
   throw new ServerStartException(this, 
     "Server launch command '" + command + "' caused an I/O error", e);
  }
  finally
  {
   if (null != process)
    process.destroy();
  }
 }

 /**
  * Returns the persistence unit name used to bootstrap
  * {@link #createEntityManager() entity managers}
  * from this object. The name of persistence unit is
  * obtained from the {@link #getConfigFile() config file}
  * unless {@link #setPersistenceUnit(String) overridden}
  * by the client.
  * @throws ConfigurationException if there is a problem
  * reading persistence unit configuration
  * @see PersistenceUnitNameSetting  
  */
 public String getPersistenceUnit()
  throws ConfigurationException
 {
  if (null == persistenceUnit)
  {
   String pu = readSetting(PersistenceUnitNameSetting.class);
   setPersistenceUnit(pu);
  }
  return persistenceUnit;
 }

 /**
  * Overrides the default persistence unit name used to
  * bootstrap {@link #createEntityManager() entity managers}
  * from this object. Since any
  * {@link #createEntityManager() Entity managers} bootstrapped
  * from this objects using the previous persistence unit are
  * automatically closed, it is advisable not to change this
  * property on a bootstrap object in use.
  * @see #getPersistenceUnit
  * @see #setPersistenceUnit(Class)
  */
 public void setPersistenceUnit(String persistenceUnit)
 {
  this.persistenceUnit = persistenceUnit;
  if (null != persistenceFactory)
   persistenceFactory.close();
  this.persistenceFactory = null;
  this.entities = null;
 }

 /**
  * Uses the package name of a persistent class to designate
  * a persistence unit for this object.
  * @see #setPersistenceUnit(String)
  * @see #getPersistenceUnit
  */
 public void setPersistenceUnit(Class<?> persistentClass)
 {
  Package pkg = persistentClass.getPackage();
  if (null == pkg)
   throw new IllegalArgumentException("Class " + persistentClass.getName()
     + " from the default package cannot be used to set the persistence unit.");
  setPersistenceUnit(pkg.getName());
 }

 /**
  * Returns location of the file that will contain the DDL
  * statements used to
  * {@link #isSchemaUpdateRequested() update the database schema}.
  * @return the DDL dump file location or <code>null</code>
  * @see #setDdlDumpFile(File)
  * @see #setSchemaUpdateRequested(boolean)
  */
 public File getDdlDumpFile()
 {
  return ddlDumpFile;
 }

 /**
  * Sets the location of a file that will contain the DDL
  * statements used to
  * {@link #isSchemaUpdateRequested() update the database schema}.
  * By default this property is not set.
  * @param ddlDumpFile the DDL dump file location or
  * <code>null</code> if the DDL dump need not be written
  */
 public void setDdlDumpFile(File ddlDumpFile)
 {
  this.ddlDumpFile = ddlDumpFile;
 }

 /**
  * Returns the list of entity classes configured for this
  * object's {@link #getPersistenceUnit() persistence unit}
  * or an empty list if no classes have been configured.
  * Persistence unit's XML descriptor is expected to list
  * all its persistent classes explicitly.
  * @throws StorageConfigurationException if there is a problem
  * reading persistence unit configuration
  * @throws IllegalStateException if a persistent class cannot
  * be loaded
  */
 public List<Class<?>> getEntityClasses()
  throws StorageConfigurationException
 {
  if (null == entities)
  {
   final ClassLoader loader = Thread.currentThread().getContextClassLoader();
   final URL resource = loader.getResource(PERSISTENCE_RESOURCE);
   if (null == resource)
    throw new IllegalStateException("Persistence metadata is not deployed in " + PERSISTENCE_RESOURCE);
   try
   {
    final String persistenceUnit = getPersistenceUnit();
    final List<PersistenceMetadata> descriptor = PersistenceXmlLoader.deploy(
      resource, Collections.emptyMap(), new EntityResolverStub(log()), PersistenceUnitTransactionType.RESOURCE_LOCAL);
    PersistenceMetadata source = null;
    for (PersistenceMetadata candidate : descriptor)
     if (candidate.getName().equals(persistenceUnit))
     {
      source = candidate;
      break;
     }
    if (null == source)
     throw new ConfigurationException("Persistence unit '" + persistenceUnit
    	+ "' is not defined in " + resource + ". ");
    entities = new ArrayList<Class<?>>();
    for (String qName : source.getClasses())
     entities.add(Class.forName(qName, false, loader));
   }
   catch (ClassNotFoundException noclass)
   {
    throw new IllegalStateException(
      "Could not load persistent class. " + noclass.getMessage(), noclass);
   }
   catch (ConfigurationException e)
   {
    throw new StorageConfigurationException(this, e);
   }
   catch (Exception e)
   {
    throw new StorageConfigurationException(this,
      "Error parsing persistence metadata at " + resource + ". " + e.getMessage(), e);
   }
  }
  return Collections.unmodifiableList(entities);
 }

 private static class EntityResolverStub implements EntityResolver
 {
  @Override
  public InputSource resolveEntity(String publicId, String systemId)
  {
   log.info("Resolving entity " + publicId + " @ " + systemId + " ...");
   return EMPTY;
  }

  public EntityResolverStub(Log log)
  {
   this.log = log;
  }

  static final InputSource EMPTY = new InputSource();
  Log log;
 }

 /**
  * Tells whether the database schema update has been
  * requested. Defaults to <code>false</code> for existing
  * database, <code>false</code> for new ones.
  * @see #setSchemaUpdateRequested(boolean)
  */
 public boolean isSchemaUpdateRequested()
 {
  return schemaUpdateRequested;
 }

 /**
  * Tells this object whether is should update database schema.
  * The schema update operation is configured by querying the
  * {@link #getEntityClasses()} method.
  * @see #isSchemaUpdateRequested()
  * @see #getEntityClasses()
  */
 public void setSchemaUpdateRequested(boolean schemaUpdateRequested)
 {
  this.schemaUpdateRequested = schemaUpdateRequested;
 }

 /**
  * Tells whether the {@link #run()} method will start
  * the database server on the local machine if it is not running.
  * @see #setServerStartRequested(boolean)
  */
 public boolean isServerStartRequested()
 {
  return serverStartRequested;
 }

 /**
  * Tells the {@link #run()} method whether or not to
  * start the database server on the local machine if it is not running.
  * @see #isServerStartRequested()
  */
 public void setServerStartRequested(boolean serverStartRequested)
 {
  this.serverStartRequested = serverStartRequested;
 }

 /**
  * After this call, the manager will create a database for its
  * current configuration and set up accounts for database access
  * as soon as it {@link #run() connects to the server}.
  * Call this method with
  * <code>null</code> arguments to cancel the database creation
  * request.
  * @param creatorUser the name of the server account that has
  * permissions to create a database and user accounts and grant
  * those users access to the new database
  * @param creatorPass the password for <code>creatorUser</code>
  */
 public void requestDatabaseCreate(String creatorUser, String creatorPass)
 {
  this.creatorUser = creatorUser;
  this.creatorPass = creatorPass;
 }

 /**
  * Returns the underlying table name for an entity class. The name is
  * retrieved according to paragraphs 8.1, 9.1.1 of JPA specification
  * JSR 220. 
  * @param entityClass entity class object
  * @return table name, may be qualified with catalog and schema prefixes
  * @throws IllegalArgumentException if the argument is not an entity class
  * @throws NullPointerException if the argument is null
  */
 public String entityTableName(Class<?> entityClass)
 {
  String[] components;
  final Entity entityAnn = entityClass.getAnnotation(Entity.class);
  if (null == entityAnn)
   throw new IllegalArgumentException("Not an entity class: " + entityClass.getName());
  final Table tableAnn = entityClass.getAnnotation(Table.class);
  if (null == tableAnn)
   components = new String[] {"", "", ""};
  else
   components = new String[] { tableAnn.catalog(), tableAnn.schema(), tableAnn.name() };
  if ("".equals(components[2]))
  {
   components[2] = entityAnn.name();
  }
  if ("".equals(components[2]))
  {
   String name = entityClass.getName();
   int at = name.lastIndexOf('.');
   if (0 <= at)
    name = name.substring(++at);
   components[2] = name;
  }
  // TODO: check whether the database supports schemas if a schema is specified 
  // TODO: substitute default catalog, schema if necessary
  StringBuilder buf = new StringBuilder();
  for (String part : components)
   if (null != part && 0 < part.length())
   {
    if (0 < buf.length())
     buf.append('.');
    buf.append(part);
   }
  return buf.toString();
 }

 @Override
 public Status getLocalStatus()
 {
  return status;
 }

 public static final String JDBC_LOCATION_PREFIX = "jdbc:";

 public static final String PERSISTENCE_RESOURCE = "META-INF/persistence.xml";

 public static final String COPYRIGHT_NOTICE =
  "Persista is copyright 2010-2014 Konstantin Livitski." +
  " Please review enclosed 'NOTICE' and 'LICENSE' files for the licensing terms" +
  " or download those files at <https://github.com/StanLivitski/persista>";

  protected void createDatabase()
  throws ConfigurationException, DatabaseException
 {
  String dbName = readSetting(DBNameSetting.class);
  String updateUser = readSetting(UpdaterUserNameSetting.class);
  String updatePass  = readSetting(UpdaterPasswordSetting.class);
  String readUser = readSetting(ReaderUserNameSetting.class);
  String readPass  = readSetting(ReaderPasswordSetting.class);
  String legend = "Creating database " + dbName;
  Connection jdbcAdmin = openDB(null, creatorUser, creatorPass);
  try
  {
   String url = jdbcAdmin.getMetaData().getURL();
   legend += " at " + url;
   List<String> script = new ArrayList<String>();
   script.add("CREATE DATABASE " + dbName);
   script.add(
     "GRANT ALTER, CREATE, CREATE TEMPORARY TABLES, CREATE VIEW, DELETE, DROP,"
     + " INDEX, INSERT, LOCK TABLES, REFERENCES, SELECT, SHOW VIEW, UPDATE ON "
     + dbName + ".* TO '" + updateUser + "' IDENTIFIED BY '" + updatePass + "'"
   );
   if (null != readUser)
   {
    script.add(
      "GRANT SELECT ON " + dbName + ".* TO '" + readUser + "' IDENTIFIED BY '"
      + (null == readPass ? "" : readPass) + "'"
    );
   }
   ScriptRunner runner = new ScriptRunner(jdbcAdmin, log(), script.toArray(), legend);
   runner.execute();
   creatorPass = null;
  }
  catch (SQLException fail)
  {
   throw new DatabaseException(this, "Error " + legend, fail);
  }
  finally
  {
   closeDB(jdbcAdmin);
  }
 }

 private void openDefaultDB() throws ConfigurationException
 {
  if (null != db)
   throw new IllegalStateException("Database connection is already open: " + db);
  String dbName = readSetting(DBNameSetting.class);
  String user = readSetting(UserNameSetting.class);
  String pass  = readSetting(PasswordSetting.class);
  db = openDB(dbName, user, pass);
 }

 /**
  * Any parameter may be <code>null</code> if missing.
  * @return the database connection on success
  * @throws ConfigurationException if there is a connection error
  */
 private Connection openDB(String dbName, String user, String password)
   throws ConfigurationException
 {
  String url = jdbcURL(dbName);
  // load the JDBC driver class
  getJDBCDriverClass();
  try
  {
   return DriverManager.getConnection(url, user, password);
  }
  catch (Exception conErr)
  {
   throw new ConfigurationException("Could not open the database. " + conErr.getMessage(), 
     conErr);
  }
 }

 private Class<?> getJDBCDriverClass()
 {
  if (null == jdbcDriverClass)
   jdbcDriverClass = (Class<?>)readSetting(DBDriverClassSetting.class);
  return jdbcDriverClass;
 }

 private String jdbcURL(String dbName) throws ConfigurationException
 {
  StringBuilder url = new StringBuilder(200).append(JDBC_LOCATION_PREFIX);
  url.append(readSetting(DBSubprotocolSetting.class)).append("://");
  url.append(readSetting(ServerAddressSetting.class).getHostAddress());
  url.append(':').append(readSetting(ServerPortSetting.class).toString());
  url.append('/');
  if (null != dbName)
   url.append(dbName);
  return url.toString();
 }

 private void closeDB(Connection db)
 {
  try
  {
   db.close();
  }
  catch (Exception e)
  {
   log().warn("Database close failed. " + e.getMessage(), e);
  }
 }

 @SuppressWarnings("unchecked")
 private void updateSchema()
 	throws ApplicationBeanException
 {
  try
  {
   String user = readSetting(UpdaterUserNameSetting.class);
   String password;
   if (null != user)
    password = readSetting(UpdaterPasswordSetting.class);
   else
   {
    user = readSetting(UserNameSetting.class);
    password = readSetting(PasswordSetting.class);
   }
 
   org.hibernate.cfg.Configuration cfg = new org.hibernate.cfg.Configuration();
   cfg.setProperty(AvailableSettings.HBM2DDL_AUTO, "update");
   cfg.setProperty(AvailableSettings.DIALECT, readSetting(HibernateSQLDialectSetting.class).getName());
   cfg.setProperty(AvailableSettings.DRIVER, getJDBCDriverClass().getName());
   cfg.setProperty(AvailableSettings.URL, db.getMetaData().getURL());
   cfg.setProperty(AvailableSettings.USER, user);
   cfg.setProperty(AvailableSettings.PASS, password);
 
   for(Class<?> clazz : getEntityClasses())
    cfg.addAnnotatedClass(clazz);
 
   SchemaUpdate worker = new SchemaUpdate(cfg);
   worker.setDelimiter(";");
   worker.setHaltOnError(true);
   if (null != ddlDumpFile)
    worker.setOutputFile(ddlDumpFile.getAbsolutePath()); 
   worker.execute(true, true);
   List<Throwable> errs = (List<Throwable>)worker.getExceptions();
   if (null != errs && !errs.isEmpty())
    for (Iterator<Throwable> erri = errs.iterator();;)
    {
     Throwable err = erri.next();
     if (erri.hasNext())
      log().error("", err);
     else
      throw new SchemaUpdateException(this, 
        "Error(s) occured during the schema update, the last error is shown.", err);
    }
  }
  catch (ConfigurationException badConfig)
  {
   throw new StorageConfigurationException(this, badConfig);
  }
  catch (SQLException e)
  {
   throw new DatabaseException(this, e);
  }
 }

 private Connection db;
 private File ddlDumpFile;
 private List<Class<?>> entities;
 private Class<?> jdbcDriverClass;
 private boolean serverStartRequested, schemaUpdateRequested;
 private String creatorUser, creatorPass;
 private String persistenceUnit;
 private EntityManagerFactory persistenceFactory;
 private Status status;
}
