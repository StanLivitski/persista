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
package name.livitski.tools.persista.cli;

import java.io.File;
import java.util.ListIterator;

import name.livitski.tools.persista.StorageBootstrap;
import name.livitski.tools.springlet.ApplicationBeanException;
import name.livitski.tools.springlet.Command;

/**
 * Handles the <code>--update-schema</code> command line switch
 * for a {@link StorageBootstrap data manager}.
 */
public class SchemaCommand extends Command
{
 /**
  * @param manager the data manager that may receive this
  * command
  */
 public SchemaCommand(StorageBootstrap manager)
 {
  super(manager);
 }

 @Override
 public void process(ListIterator<String> args)
  throws ApplicationBeanException
 {
  final StorageBootstrap manager = (StorageBootstrap)getApplicationBean();
  String arg = fetchArgument(args);
  if (null != arg)
  {
   final File file = new File(arg);
   manager.setDdlDumpFile(file);
  }
  manager.setSchemaUpdateRequested(true);
 }

 public String getArgSpec()
 {
  return "[ddl-dump-file]";
 }

 public String getSummary()
 {
  return "Requests a database schema update."
  + " The optional argument is a file that will store generated DDL statements."
  + " Current contents of that file will be overwritten.";
 }

}
