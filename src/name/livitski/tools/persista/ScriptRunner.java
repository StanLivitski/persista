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
import java.util.Arrays;

import org.apache.commons.logging.Log;

public class ScriptRunner extends StatementHandler
{
 public ScriptRunner(Connection jdbc, Log log, Object[] script, String legend)
 {
  super(jdbc, log);
  this.script = script;
  this.legend = legend;
 }

 @Override
 protected void handleStatement(Statement stmt)
 	throws SQLException
 {
  this.stmt = stmt;
  runScript(Arrays.asList(script));
  this.stmt = null;
 }

 private void runScript(Iterable<?> script) throws SQLException
 {
  for (Object item : script)
  {
   if (item instanceof String)
   {
    String sql = (String)item;
    log().trace(sql);
    stmt.execute(sql);
   }
   else if (item instanceof Iterable<?>)
    runScript((Iterable<?>)item);
   else if (item instanceof Object[])
    runScript(Arrays.asList((Object[])item));
   else
    throw new IllegalArgumentException(item + (null == item ? "" : " of " + item.getClass()));
  }
 }

 @Override
 protected String legend()
 {
  return legend;
 }

 protected String legend;
 private Statement stmt;
 private final Object[] script;
}