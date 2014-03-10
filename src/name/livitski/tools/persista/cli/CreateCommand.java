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

import java.io.Console;
import java.io.File;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Properties;

import name.livitski.tools.persista.StorageBootstrap;
import name.livitski.tools.persista.config.credentials.PasswordSetting;
import name.livitski.tools.persista.config.credentials.UserNameSetting;
import name.livitski.tools.persista.diagn.StorageConfigurationException;
import name.livitski.tools.proper2.Configuration;
import name.livitski.tools.proper2.ConfigurationException;
import name.livitski.tools.springlet.Command;

/**
 * Handles the <code>--create</code> command line switch
 * for a {@link StorageBootstrap data manager}.
 */
public class CreateCommand extends Command
{
 /**
  * @param manager the data manager that may receive this
  * command
  */
 public CreateCommand(StorageBootstrap manager)
 {
  super(manager);
 }

 @Override
 public void process(ListIterator<String> args)
  throws StorageConfigurationException
 {
  String[] creds = null;
  try
  {
   String arg = fetchArgument(args);
   if (null != arg)
   {
    File file = new File(arg);
    creds = readCredentialsFile(file);
   }
   if (null == creds)
    creds = inputCredentials();
   ((StorageBootstrap)getApplicationBean()).requestDatabaseCreate(creds[0], creds[1]);
  }
  catch (ConfigurationException fault)
  {
   throw new StorageConfigurationException(getApplicationBean(), fault);
  }
 }

 public String getArgSpec()
 {
  return "[credentials-file]";
 }

 public String getSummary()
 {
  return "Creates a database for the current configuration and sets up"
 	+ " accounts with access to that database. The optional argument"
 	+ " points to a file with administrator's username and password"
 	+ " used to log on to the DBMS when setting up the new database."
 	+ " If that argument is omitted, the user is asked for"
	+ " administrator's credentials interactively.";
 }

 private String[] readCredentialsFile(File file)
  throws ConfigurationException
 {
  Configuration config = new Configuration(this);
  config.setCachingEnabled(false);
  config.setConfigFile(file);
  final Properties props = config.readConfiguration();
  final String[] creds = new String[] {
    config.readSetting(UserNameSetting.class, props),
    config.readSetting(PasswordSetting.class, props)
  };
  return creds;
 }

 private String[] inputCredentials()
  throws ConfigurationException
 {
  Console console = System.console();
  if (null == console)
   throw new ConfigurationException("Cannot read credentials interactively - console is not avaliable.");
  final String[] creds = new String[2];
  do {
   String cred = console.readLine("Enter administrator's user name to create a database: ");
   if (null == cred)
    throw new ConfigurationException("End of input reached while reading credentials from the console.");
   cred = cred.trim();
   if (0 != cred.length())
    creds[0] = cred;
  } while (null == creds[0]);
  do {
   char[] cred = console.readPassword("Enter the password for account '%s': ", creds[0]);
   if (null == cred)
    throw new ConfigurationException("End of input reached while reading credentials from the console.");
   creds[1] = new String(cred);
   Arrays.fill(cred, '\0');
  } while (null == creds[1]);
  return creds;
 }
}
