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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;


import name.livitski.tools.persista.EntityManager;
import name.livitski.tools.persista.diagn.AbstractStorageException;
import name.livitski.tools.springlet.Logging;

/**
 * Provides common functionality for Data Access Object (DAO) classes.
 * A DAO class implements lookups, queries, and bulk operations over
 * a class of persistent objects. Objects of this class
 * hold references to an {@link EntityManager} and become invalid
 * when the entity manager {@link EntityManager#close() closes}.
 */
public abstract class AbstractDAO<Entity> extends Logging
{
 /**
  * DAO instances are created and maintained by the {@link EntityManager},
  * one instance per manager. Other classes should not attempt to create
  * these objects. Implementations must have a constructor that accepts
  * {@link EntityManager} as its single argument. The constructor need
  * not be public. 
  * @param db {@link EntityManager} that hosts this DAO
  */
 protected AbstractDAO(EntityManager db, Class<Entity> entityClass)
 {
  this.db = db;
  this.entityClass = entityClass;
 }

 /**
  * Returns dependency DAO classes required for this implementation class.
  * The implementor is responsible for avoiding circular dependencies between
  * DAO classes.
  */
 @SuppressWarnings("unchecked")
 public Class<? extends AbstractDAO<?>>[] dependencies()
 {
  return new Class[0];
 }

 public Object getProperty(Object objectOrMap, String property)
 {
  Method getter = accessor(property);
  if (null == objectOrMap)
   throw new NullPointerException("Cannot access property \"" + property
     + "\" of a null object");
  if (entityClass.isInstance(objectOrMap))
  {
   try
   {
    return getter.invoke(objectOrMap);
   }
   catch (RuntimeException e)
   {
    throw e;
   }
   catch (InvocationTargetException e)
   {
    Throwable cause = e.getCause();
    if (cause instanceof RuntimeException)
     throw (RuntimeException)cause;
    else
     throw new RuntimeException(
       "Could not read property \"" + property + "\" on an object of " + entityClass, e);
   }
   catch (Exception e)
   {
    throw new UnsupportedOperationException(
      "Could not read property \"" + property + "\" on an object of " + entityClass, e);
   }
  }
  else if (objectOrMap instanceof Map<?,?>)
  {
   @SuppressWarnings("unchecked")
   final Map<String, Object> map = (Map<String, Object>)objectOrMap;
   return map.get(property);
  }
  else
   throw new IllegalArgumentException(objectOrMap + " is neither a map nor a "
     + entityClass.getName());
 }

 /**
  * Sets a property on an entity object or a map of properties.
  * @param objectOrMap an entity object, a map of properties,
  * or <code>null</code>
  * @param property the name of a property to set
  * @param value the value of a property to set
  * @return the same entity object or map with the new property
  * value, or a new map if <code>objectOrMap</code> was
  * <code>null</code>
  */
 public Object setProperty(Object objectOrMap, String property, Object value)
 {
  Method setter = mutator(property);
  if (null == objectOrMap)
   objectOrMap = new HashMap<String, Object>();
  if (entityClass.isInstance(objectOrMap))
  {
   try
   {
    setter.invoke(objectOrMap, value);
   }
   catch (RuntimeException e)
   {
    throw e;
   }
   catch (InvocationTargetException e)
   {
    Throwable cause = e.getCause();
    if (cause instanceof RuntimeException)
     throw (RuntimeException)cause;
    else
     throw new RuntimeException(
       "Could not set property \"" + property + "\" on an object of " + entityClass, e);
   }
   catch (Exception e)
   {
    throw new UnsupportedOperationException(
      "Could not set property \"" + property + "\" on an object of " + entityClass, e);
   }
  }
  else if (objectOrMap instanceof Map<?,?>)
  {
   @SuppressWarnings("unchecked")
   final Map<String, Object> map = (Map<String, Object>)objectOrMap;
   map.put(property, value);
  }
  else
   throw new IllegalArgumentException(objectOrMap + " is neither a map nor a "
     + entityClass.getName());
  return objectOrMap;
 }

 /**
  * Loads properties from a map into an entity object.
  */
 public void loadProperties(Entity object, Map<String, Object> properties)
 {
  try
  {
   for (Map.Entry<String, Object> property : properties.entrySet())
   {
    Method setter = mutator(property.getKey());
    setter.invoke(object, property.getValue());
   }
  }
  catch (RuntimeException e)
  {
   throw e;
  }
  catch (InvocationTargetException e)
  {
   Throwable cause = e.getCause();
   if (cause instanceof RuntimeException)
    throw (RuntimeException)cause;
   else
    throw new RuntimeException(
      "Could not load properties into an object of " + entityClass, e);
  }
  catch (Exception e)
  {
   throw new UnsupportedOperationException(
     "Could not load properties into an object of " + entityClass, e);
  }
 }

 protected abstract class Transaction extends TransactionalWork
 {
  public void exec()
  {
   try
   {
    super.exec(db);
   }
   catch (AbstractStorageException e)
   {
    throw new UnsupportedOperationException(e);
   }
  }

  @Override
  protected Log log()
  {
   return AbstractDAO.this.log();
  }
 }

 protected Map<String, PropertyDescriptor> introspectProperties()
 {
  if (null == entityProps)
   try
   {
    final BeanInfo beanInfo = Introspector.getBeanInfo(entityClass);
    final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
    entityProps = new HashMap<String, PropertyDescriptor>(propertyDescriptors.length, 1f);
    for (final PropertyDescriptor pd : propertyDescriptors)
    {
     entityProps.put(pd.getName(), pd);
    }
   }
   catch (IntrospectionException e)
   {
    throw new UnsupportedOperationException("Introspection failed for entity " + entityClass, e);
   }
  return entityProps;
 }
 
 protected EntityManager db;
 protected final Class<Entity> entityClass;

 private Method accessor(String property)
 {
  Map<String, PropertyDescriptor> props = introspectProperties();
  PropertyDescriptor prop = props.get(property);
  if (null == prop)
   throw new IllegalArgumentException("Property \"" + property
     + "\" does not exist in " + entityClass);
  Method getter = prop.getReadMethod();
  if (null == getter)
   throw new IllegalArgumentException("Property \"" + property
     + "\" is not writable in " + entityClass);
  return getter;
 }

 private Method mutator(String property)
 {
  Map<String, PropertyDescriptor> props = introspectProperties();
  PropertyDescriptor prop = props.get(property);
  if (null == prop)
   throw new IllegalArgumentException("Property \"" + property
     + "\" does not exist in " + entityClass);
  Method setter = prop.getWriteMethod();
  if (null == setter)
   throw new IllegalArgumentException("Property \"" + property
     + "\" is not writable in " + entityClass);
  return setter;
 }

 private Map<String, PropertyDescriptor> entityProps; 
}
