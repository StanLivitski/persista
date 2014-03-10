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

import javax.persistence.EntityTransaction;

import org.apache.commons.logging.Log;

import name.livitski.tools.persista.diagn.AbstractStorageException;
import name.livitski.tools.springlet.Logging;

/**
 * Encloses the implementors' code block in JPA transactional brackets.
 */
public abstract class TransactionalWork extends Logging
{
 /**
  * Subclasses should place their code that updates the database
  * in this method's implementation.
  * @param db the entity manager the implementor should work with
  * @return <code>true</code> upon successful update indicating
  * that the transaction can be committed, <code>false</code> if
  * the transaction must be rolled back. Implementors SHOULD log
  * an explanation message at the {@link Log#debug(Object) DEBUG}
  * or a higher level with a word "rollback" when they request
  * a rollback without throwing an exception.
  * @throws AbstractStorageException implementors may throw objects
  * of any subclass thereof to indicate an error and roll back the
  * transaction  
  * @throws RuntimeException unchecked exceptions thrown by this
  * method also roll back the transaction
  */
 protected abstract boolean code(EntityManager db)
  throws AbstractStorageException;

 /**
  * Performs the {@link #code database operations} provided by
  * the subclass in JPA transactional brackets. If there is currently
  * no active transaction at the entity manager, a new transaction
  * is started.
  * If the code finishes normally and does not request the rollback,
  * the transaction is committed if it was started by this method
  * (local transaction).
  * If the implementor's code throws an exception or requests the
  * rollback, the local transaction is rolled back and any ongoing
  * transaction is marked
  * {@link EntityTransaction#setRollbackOnly() rollback-only}.
  * @param db the entity manager the operation will be performed with
  * @return <code>true</code> if the transaction has or may still
  * be committed
  * @throws AbstractStorageException indicates an error during
  * the operation
  * @throws RuntimeException any unchecked implementor's exceptions
  * will be rethrown
  * @see EntityManager#getTransaction()
  */
 public boolean exec(final EntityManager db) throws AbstractStorageException
 {
  final EntityTransaction transaction = db.getTransaction();
  boolean commit = false;
  Exception status = null;
  boolean localTransaction;
  if (transaction.isActive())
   localTransaction = false;
  else
  {
   transaction.begin();
   localTransaction = true;
  }
  try
  {
   commit = code(db);
   return commit;
  }
  catch (AbstractStorageException fault)
  {
   status = fault;
   throw fault;
  }
  catch (RuntimeException fault)
  {
   status = fault;
   throw fault;
  }
  finally
  {
   if (!localTransaction)
   {
    if (commit)
     db.flush();
    else
     transaction.setRollbackOnly();
   }
   else if (!commit || transaction.getRollbackOnly())
   {
    try { transaction.rollback(); }
    catch (RuntimeException fault)
    {
     if (null != status)
      log().error("Transaction rollback failed", fault);
     else
      throw fault;
    }
   }
   else // commit local transaction
   {
    try { transaction.commit(); }
    catch (RuntimeException fault)
    {
     throw fault;
    }
   }
  }
 }
}
