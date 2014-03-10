/*
 *  This file is part of Persista.
 *  Copyright © 2010-2014 Konstantin "Stan" Livitski
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.persistence.EntityTransaction;

import name.livitski.tools.persista.EntityManager;
import name.livitski.tools.persista.StorageBootstrap;
import name.livitski.tools.springlet.ApplicationBeanException;
import name.livitski.tools.springlet.Logging;
import name.livitski.tools.springlet.ManagedLauncher;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Provides resources commonly needed by the data
 * module's tests.
 */
public abstract class AbstractDBTest extends Logging
{
 public static StorageBootstrap getStorageBootstrap()
 {
  return boot;
 }

 public EntityManager getEntityManager()
 {
  return em;
 }

 public static ManagedLauncher getAppContainer()
 {
  if (null == appContainer)
   appContainer = new ManagedLauncher();
  return appContainer;
 }

 public static void setAppContainer(ManagedLauncher appContainer)
 {
  AbstractDBTest.appContainer = appContainer;
 }

 public static final long roundMillisToSecond(long millis)
 {
  millis -= millis % 1000L;
  return millis;
 }

 /**
  * Prepares the test database.
  */
 @BeforeClass
 public static void setUpDB()
 	throws IOException
 {
  if (null != boot)
   return;
  InputStream cfg = AbstractDBTest.class.getResourceAsStream("/jdk-log.test.properties");
  configureJDKLogs(cfg);
  cfg.close();
  setUpDependency(MGR_CONFIG_FILE);
  boot.run();
  final int status = boot.getStatusCode();
  if (0 != status)
   throw new RuntimeException("StorageBootstrap bean exited with code " + status);
 }

 public static void setUpDependency(final String configFileAt)
 {
  if (null != boot)
   return;
  boot = getAppContainer().getBean(StorageBootstrap.class);
  // TODO: request db creation if supplied with credentials
  boot.setSchemaUpdateRequested(true);
  boot.setConfigFile(new File(configFileAt));
  boot.setServerStartRequested(true);
 }

 @AfterClass
 public static void tearDownDB()
 {
  if (null != boot)
   boot.close();
  boot = null;
 }

 @Before
 public void setUpJPA()
	throws ApplicationBeanException
 {
  if (null == em)
   em = boot.createEntityManager();
 }

 @After
 public void tearDownJPA()
 {
  if (null == em)
   return;
  EntityTransaction txn = em.getTransaction();
  if (txn.isActive())
   txn.rollback();
  em.close();
 }

 public static final String MGR_CONFIG_FILE = "config/test.db.cfg";

 protected void setEntityManager(EntityManager em)
 {
  if (null != this.em)
   throw new IllegalStateException("This test class already has an entity manager attached");
  this.em = em;
 }

 private static ManagedLauncher appContainer;
 private static StorageBootstrap boot;
 private EntityManager em;
}