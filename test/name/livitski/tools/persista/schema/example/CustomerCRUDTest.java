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
package name.livitski.tools.persista.schema.example;

import javax.persistence.EntityTransaction;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import name.livitski.tools.persista.EntityManager;
import name.livitski.tools.persista.AbstractDBTest;
import name.livitski.tools.springlet.ApplicationBeanException;

import static org.junit.Assert.*;

/**
 * Tests basic persistence operations (create, read,
 * update, delete) with {@link Customer} records.
 */
public class CustomerCRUDTest extends AbstractDBTest
{
 @Before
 public void createRecord() throws ApplicationBeanException
 {
  if (null != storedRecordId)
   return;
  setUpJPA();
  final EntityManager db = getEntityManager();
  final EntityTransaction txn = db.getTransaction();
  txn.begin();
  Customer customer = new Customer();
  customer.setName(CUSTOMER_NAME);
  customer.setAddress(CUSTOMER_ADDRESS);
  customer.setPhone(CUSTOMER_PHONE);
  customer.setEmail(CUSTOMER_EMAIL);
  db.persist(customer);
  txn.commit();
  storedRecordId = customer.getId();
 }

 @Test
 public void testCreate()
 {
  assertNotNull("stored object must have a positive id, got null", storedRecordId);
  log().info("Created a customer record with id=" + storedRecordId);
  assertTrue("persisted object must have a positive id, got " + storedRecordId, 0 < storedRecordId);
 }

 @Test
 public void testRead()
 {
  assertNotNull("stored object must have a positive id, got null", storedRecordId);
  final EntityManager db = getEntityManager();
  final Customer record = db.find(Customer.class, storedRecordId);
  assertNotNull(
    "cannot find record of type " + Customer.class + " with id=" + storedRecordId,
    record);
  assertArrayEquals("fields within record of type "
    + Customer.class + " with id=" + storedRecordId,
    expected, 
    new String[]
    {
     record.getName(), record.getAddress(), record.getPhone(), record.getEmail()
    });
 }

 @Test
 public void testUpdateDelete()
 {
  assertNotNull("stored object must have a positive id, got null", storedRecordId);
  final Log log = log();
  final EntityManager db = getEntityManager();
  final EntityTransaction txn = db.getTransaction();
  txn.begin();
  Customer customer = db.find(Customer.class, storedRecordId);
  assertNotNull(
    "cannot find record of type " + Customer.class + " with id=" + storedRecordId,
    customer);
  customer.setName(UPDATED_NAME);
  customer.setAddress(UPDATED_ADDRESS);
  customer.setPhone(UPDATED_PHONE);
  customer.setEmail(UPDATED_EMAIL);
  txn.commit();
  log.info("Updated customer record with id=" + storedRecordId);
  expected = new String[] {
    UPDATED_NAME, UPDATED_ADDRESS, UPDATED_PHONE, UPDATED_EMAIL
  };
  testRead();
  txn.begin();
  db.refresh(customer);
  db.remove(customer);
  txn.commit();
  log.info("Deleted customer record with id=" + storedRecordId);
  customer = db.find(Customer.class, storedRecordId);
  assertNull(
    "record of type " + Customer.class + " with id=" + storedRecordId + " was not deleted",
    customer);
  storedRecordId = null;
 }
 
 public static final String CUSTOMER_NAME = "Funny Gadgets Inc."; 
 public static final String CUSTOMER_ADDRESS = "123 No Way, Techville, YO 99999"; 
 public static final String CUSTOMER_PHONE = "123 456-9999"; 
 public static final String CUSTOMER_EMAIL = "spammers@welcome.com"; 
 
 public static final String UPDATED_NAME = "32 y/o Retirees Inc."; 
 public static final String UPDATED_ADDRESS = "One Turtle Wallet Beach, Turtle, CL 11111"; 
 public static final String UPDATED_PHONE = "567 654-5432"; 
 public static final String UPDATED_EMAIL = "spammers@beware.com"; 

 private static Integer storedRecordId;
 private String[] expected = {
   CUSTOMER_NAME, CUSTOMER_ADDRESS, CUSTOMER_PHONE, CUSTOMER_EMAIL
 };
}
