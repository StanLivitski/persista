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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import name.livitski.tools.springlet.Logging;

/**
 * Provides {@link javax.persistence.EntityManager access to the database}
 * and {@link AbstractDAO DAOs} of persistent classes stored therein.
 */
public class EntityManager extends Logging
 implements javax.persistence.EntityManager
{
 /**
  * Looks up DAO object of a particular class associated with this
  * entity manager. Instantiates and registers such object if necessary.
  * @param clazz the DAO class to look for
  * @return an object of requested class associated with this connection
  */
 @SuppressWarnings("unchecked")
 public <D extends AbstractDAO<?>> D findDAO(Class<D> clazz)
 {
  D dao = (D)daoMap.get(clazz);
  if (null == dao)
  {
   try
   {
    Constructor<?> ctr = clazz.getDeclaredConstructor(this.getClass());
    ctr.setAccessible(true);
    dao = (D) ctr.newInstance(this);
    // TODO: initialize dependencies if necessary
    daoMap.put(clazz, dao);
   }
   catch (NoSuchMethodException e)
   {
    throw new ExceptionInInitializerError("DAO " + clazz
      + " does not have a single argument constructor that accepts a "
      + getClass().getName());
   }
   catch (InstantiationException e)
   {
    throw new ExceptionInInitializerError("DAO " + clazz + " is abstract");
   }
   catch (IllegalAccessException e)
   {
    throw (Error) new ExceptionInInitializerError("No access to DAO " + clazz
      + " constructor").initCause(e);
   }
   catch (InvocationTargetException e)
   {
    throw (Error) new ExceptionInInitializerError(
      "Exception in constructor of DAO " + clazz).initCause(e
      .getTargetException());
   }   
  }
  return dao;
 }

 @Override
 public void clear()
 {
  internal.clear();
 }

 @Override
 public void close()
 {
  internal.close();
 }

 @Override
 public boolean contains(Object arg0)
 {
  return internal.contains(arg0);
 }

 @Override
 public <T> TypedQuery<T> createNamedQuery(String arg0, Class<T> arg1)
 {
  return internal.createNamedQuery(arg0, arg1);
 }

 @Override
 public Query createNamedQuery(String arg0)
 {
  return internal.createNamedQuery(arg0);
 }

 @SuppressWarnings("unchecked")
 @Override
 public Query createNativeQuery(String arg0, Class arg1)
 {
  return internal.createNativeQuery(arg0, arg1);
 }

 @Override
 public Query createNativeQuery(String arg0, String arg1)
 {
  return internal.createNativeQuery(arg0, arg1);
 }

 @Override
 public Query createNativeQuery(String arg0)
 {
  return internal.createNativeQuery(arg0);
 }

 @Override
 public <T> TypedQuery<T> createQuery(CriteriaQuery<T> arg0)
 {
  return internal.createQuery(arg0);
 }

 @Override
 public <T> TypedQuery<T> createQuery(String arg0, Class<T> arg1)
 {
  return internal.createQuery(arg0, arg1);
 }

 @Override
 public Query createQuery(String arg0)
 {
  return internal.createQuery(arg0);
 }

 @Override
 public void detach(Object arg0)
 {
  internal.detach(arg0);
 }

 @Override
 public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2,
   Map<String, Object> arg3)
 {
  return internal.find(arg0, arg1, arg2, arg3);
 }

 @Override
 public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2)
 {
  return internal.find(arg0, arg1, arg2);
 }

 @Override
 public <T> T find(Class<T> arg0, Object arg1, Map<String, Object> arg2)
 {
  return internal.find(arg0, arg1, arg2);
 }

 @Override
 public <T> T find(Class<T> arg0, Object arg1)
 {
  return internal.find(arg0, arg1);
 }

 @Override
 public void flush()
 {
  internal.flush();
 }

 @Override
 public CriteriaBuilder getCriteriaBuilder()
 {
  return internal.getCriteriaBuilder();
 }

 @Override
 public Object getDelegate()
 {
  return internal.getDelegate();
 }

 @Override
 public EntityManagerFactory getEntityManagerFactory()
 {
  return internal.getEntityManagerFactory();
 }

 @Override
 public FlushModeType getFlushMode()
 {
  return internal.getFlushMode();
 }

 @Override
 public LockModeType getLockMode(Object arg0)
 {
  return internal.getLockMode(arg0);
 }

 @Override
 public Metamodel getMetamodel()
 {
  return internal.getMetamodel();
 }

 @Override
 public Map<String, Object> getProperties()
 {
  return internal.getProperties();
 }

 @Override
 public <T> T getReference(Class<T> arg0, Object arg1)
 {
  return internal.getReference(arg0, arg1);
 }

 @Override
 public EntityTransaction getTransaction()
 {
  return internal.getTransaction();
 }

 @Override
 public boolean isOpen()
 {
  return internal.isOpen();
 }

 @Override
 public void joinTransaction()
 {
  internal.joinTransaction();
 }

 @Override
 public void lock(Object arg0, LockModeType arg1, Map<String, Object> arg2)
 {
  internal.lock(arg0, arg1, arg2);
 }

 @Override
 public void lock(Object arg0, LockModeType arg1)
 {
  internal.lock(arg0, arg1);
 }

 @Override
 public <T> T merge(T arg0)
 {
  return internal.merge(arg0);
 }

 @Override
 public void persist(Object arg0)
 {
  internal.persist(arg0);
 }

 @Override
 public void refresh(Object arg0, LockModeType arg1, Map<String, Object> arg2)
 {
  internal.refresh(arg0, arg1, arg2);
 }

 @Override
 public void refresh(Object arg0, LockModeType arg1)
 {
  internal.refresh(arg0, arg1);
 }

 @Override
 public void refresh(Object arg0, Map<String, Object> arg1)
 {
  internal.refresh(arg0, arg1);
 }

 @Override
 public void refresh(Object arg0)
 {
  internal.refresh(arg0);
 }

 @Override
 public void remove(Object arg0)
 {
  internal.remove(arg0);
 }

 @Override
 public void setFlushMode(FlushModeType arg0)
 {
  internal.setFlushMode(arg0);
 }

 @Override
 public void setProperty(String arg0, Object arg1)
 {
  internal.setProperty(arg0, arg1);
 }

 @Override
 public <T> T unwrap(Class<T> arg0)
 {
  return internal.unwrap(arg0);
 }

 protected EntityManager(javax.persistence.EntityManager internal)
 {
  this.internal = internal;
 }

 private javax.persistence.EntityManager internal;
 private Map<Class<? extends AbstractDAO<?>>, AbstractDAO<?>> daoMap
	= new HashMap<Class<? extends AbstractDAO<?>>, AbstractDAO<?>>();
}
