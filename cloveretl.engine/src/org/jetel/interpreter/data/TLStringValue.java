/*
 *    jETeL/Clover - Java based ETL application framework.
 *    Copyright (C) 2002-2007  David Pavlis <david.pavlis@centrum.cz> and others.
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *    
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU    
 *    Lesser General Public License for more details.
 *    
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Created on 1.11.2007 by dadik
 *
 */

package org.jetel.interpreter.data;

import org.jetel.data.DataField;
import org.jetel.util.string.Compare;

public class TLStringValue extends TLValue implements CharSequence {

	StringBuilder value;
	
	public TLStringValue(){
		super(TLValueType.STRING);
		value=new StringBuilder();
	}
	
	
	public TLStringValue(CharSequence value){
		super(TLValueType.STRING);
		this.value=new StringBuilder(value.length());
		this.value.append(value);
	}
	
	public TLStringValue(StringBuilder value){
		super(TLValueType.STRING);
		this.value=value;
	}
	
	public void setValue(TLValue value){
		this.value.setLength(0);
		if (value instanceof TLStringValue){
			this.value.append(((TLStringValue)value).value);
		}else{
			this.value.append(value.toString());
		}
	}
	
	
	public Object getValue(){
		return value;
	}
	
	public CharSequence getCharSequence(){
		return value;
	}
	
	@Override
	public void copyToDataField(DataField field) {
		field.fromString(value);
	}

	@Override
	public void setValue(Object _value) {
		this.value.setLength(0);
		if (_value instanceof CharSequence){
			this.value.append((CharSequence)_value);
		}else{
			this.value.append(_value.toString());
		}
	}


	@Override
	public void setValue(DataField field) {
		if (!field.isNull()){
			this.value.setLength(0);
			this.value.append(field.toString());
		}else{
			throw new IllegalArgumentException("Can't assign value of field "+field.getMetadata().getName()+
					" (type " + field.getMetadata().getTypeAsString()+") to String value");
		}
		
	}


	@Override public int compareTo(TLValue val){
		if (val instanceof TLStringValue){
			return Compare.compare(value, ((TLStringValue)val).value);
		}
		return Compare.compare(value, val.toString());
	}
	
	@Override
	public TLValue duplicate() {
		return new TLStringValue(new StringBuilder(value));
	}
	
	@Override public String toString(){
		return value.toString();
	}


	public char charAt(int arg0) {
		return value.charAt(arg0);
	}


	public int length() {
		return value.length();
	}


	public CharSequence subSequence(int arg0, int arg1) {
		return value.subSequence(arg0, arg1);
	}

}
