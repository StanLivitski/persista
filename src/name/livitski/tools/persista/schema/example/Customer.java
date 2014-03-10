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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * A customer record example.
 */
@Entity
public class Customer
{
 public String getName()
 {
  return name;
 }
 public void setName(String name)
 {
  this.name = name;
 }
 public String getAddress()
 {
  return address;
 }
 public void setAddress(String address)
 {
  this.address = address;
 }
 public String getPhone()
 {
  return phone;
 }
 public void setPhone(String phone)
 {
  this.phone = phone;
 }
 public String getEmail()
 {
  return email;
 }
 public void setEmail(String email)
 {
  this.email = email;
 }
 public int getId()
 {
  return id;
 }
 
 @GeneratedValue 
 @Id private int id;
 @Basic(optional=false) private String name;
 @Basic private String address, phone, email;
}
