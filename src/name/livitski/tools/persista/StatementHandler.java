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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;

/**
 * Implements the standard pattern of JDBC statement usage.
 */
public abstract class StatementHandler
{
 protected abstract void handleStatement(Statement stmt) 
	throws SQLException;

 protected abstract String legend();

 public void execute()
 	throws SQLException
 {
  Statement stmt = null;
  try
  {
   stmt = createStatement();
   handleStatement(stmt);
  }
  finally
  {
   if (null != stmt)
    try { close(stmt); }
    catch (SQLException thrown)
    {
     log.warn("Error closing statement handle after " + legend(), thrown);
    }
  }
 }

 @Override
 public String toString()
 {
  return "statement for " + legend();
 }

 protected void close(Statement stmt) throws SQLException
 {
  stmt.close();
 }

 protected Statement createStatement() throws SQLException
 {
  return getJdbc().createStatement();
 }

 protected final Log log()
 {
  return log;
 }

 protected final Connection getJdbc()
 {
  return jdbc;
 }

 public StatementHandler(Connection jdbc, Log log)
 {
  this.jdbc = jdbc;
  this.log = log;
 }

 protected Connection jdbc;
 protected Log log;
}
