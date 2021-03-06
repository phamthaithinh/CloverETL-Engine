/*
 *  jETeL/Clover - Java based ETL application framework.
 *  Created on Apr 25, 2003
 *  Copyright (C) 2003, 2002  David Pavlis, Wes Maciorowski
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jetel.exception;

import java.io.FileInputStream;

import org.jetel.data.DataRecord;
import org.jetel.data.DataRecordFactory;
import org.jetel.data.parser.DelimitedDataParser;
import org.jetel.metadata.DataRecordMetadata;
import org.jetel.metadata.DataRecordMetadataXMLReaderWriter;
import org.jetel.test.CloverTestCase;

/**
 * @author maciorowski
 *
 */
public class BadDataFormatExceptionHandler_DelimitedDataParserNIO_Test  extends CloverTestCase {
	private DelimitedDataParser aParser = null;
	private DelimitedDataParser aParser2 = null;
	private DataRecord record;
	private FileInputStream in = null;
	private FileInputStream in2 = null;
	private DataRecordMetadata metadata = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		DataRecordMetadataXMLReaderWriter xmlReader = new DataRecordMetadataXMLReaderWriter();

		metadata = xmlReader.read(new FileInputStream("config/test/rec_def/DL_null_def_rec.xml"));
		in = new FileInputStream("data/in/good/DL28_NL.txt");
		in2 = new FileInputStream("data/in/bad/DL_NL_nulls.txt");
		
		aParser2 = new DelimitedDataParser(metadata);

		aParser = new DelimitedDataParser(metadata);
		record = DataRecordFactory.newRecord(metadata);
		record.init();
	}
	
	@Override
	protected void tearDown() {
		aParser.close();
		aParser = null;

		aParser2.close();
		aParser2 = null;
		record  = null;
		
		metadata = null;
		in = null;
		in2 = null;
	}
	
	/**
	 *  Test for @link for a well formatted data source.
	 *  No handler
	 */
	
	public void test_goodFile() throws ComponentNotReadyException, JetelException {
		// test no handler ------------------------------------
		aParser.init();
		aParser.setDataSource(in);
		while((record=aParser.getNext(record))!=null){}
		aParser.close();
	}
	
	/**
	 *  Test for @link for a well formatted data source.
	 *  strict handler
	 */
	public void test_strict_goodFile() throws ComponentNotReadyException, JetelException {
		IParserExceptionHandler aHandler = null;
		// test strict handler ------------------------------------
		aParser.init();
		aParser.setDataSource(in);
		aHandler = ParserExceptionHandlerFactory.getHandler(PolicyType.STRICT);
		aParser.setExceptionHandler(aHandler);
		while((record=aParser.getNext(record))!=null){}
		aParser.close();
	}
	
	/**
	 *  Test for @link for a well formatted data source.
	 *  controlled handler
	 */
	public void test_controlled_goodFile() throws ComponentNotReadyException, JetelException {
		IParserExceptionHandler aHandler = null;
		// test controlled handler ------------------------------------
		aParser.init();
		aParser.setDataSource(in);

		aHandler = ParserExceptionHandlerFactory.getHandler(PolicyType.CONTROLLED);
		aParser.setExceptionHandler(aHandler);

		// 1.0Stone 101 01/11/93
		// -15.5 Brook 112 11/03/02
		// -0.7Bone Broo 9901/01/03
		int recCount = 0;
		while ((record = aParser.getNext(record)) != null) {
			if (recCount == 0) {
				assertEquals(record.getField(0).toString(), "1.0");
				assertEquals(record.getField(1).toString(), "Stone");
				assertEquals(record.getField(2).toString(), "101");
				assertEquals(record.getField(3).toString(), "01/11/93");
			} else if (recCount == 1) {
				assertEquals(record.getField(0).toString(), "-15.5");
				assertEquals(record.getField(1).toString(), "Brook");
				assertEquals(record.getField(2).toString(), "112");
				assertEquals(record.getField(3).toString(), "11/03/02");
			} else if (recCount == 2) {
				assertEquals(record.getField(0).toString(), "-0.7");
				assertEquals(record.getField(1).toString(), "Bone Broo");
				assertEquals(record.getField(2).toString(), "99");
				assertEquals(record.getField(3).toString(), "01/01/03");
			}
			recCount++;
		}
		assertEquals(3, recCount);

		aParser.close();
	}
	
	/**
	 *  Test for @link for a well formatted data source.
	 *  lenient handler
	 */
	public void test_lenient_goodFile() throws JetelException, ComponentNotReadyException {
		IParserExceptionHandler aHandler = null;
		// test lenient handler ------------------------------------
		aParser.init();
		aParser.setDataSource(in);

		aHandler = ParserExceptionHandlerFactory.getHandler(PolicyType.LENIENT);
		aParser.setExceptionHandler(aHandler);

		// 1.0Stone 101 01/11/93
		// -15.5 Brook 112 11/03/02
		// -0.7Bone Broo 9901/01/03
		int recCount = 0;
		while ((record = aParser.getNext(record)) != null) {
			if (recCount == 0) {
				assertEquals(record.getField(0).toString(), "1.0");
				assertEquals(record.getField(1).toString(), "Stone");
				assertEquals(record.getField(2).toString(), "101");
				assertEquals(record.getField(3).toString(), "01/11/93");
			} else if (recCount == 1) {
				assertEquals(record.getField(0).toString(), "-15.5");
				assertEquals(record.getField(1).toString(), "Brook");
				assertEquals(record.getField(2).toString(), "112");
				assertEquals(record.getField(3).toString(), "11/03/02");
			} else if (recCount == 2) {
				assertEquals(record.getField(0).toString(), "-0.7");
				assertEquals(record.getField(1).toString(), "Bone Broo");
				assertEquals(record.getField(2).toString(), "99");
				assertEquals(record.getField(3).toString(), "01/01/03");
			}
			recCount++;
		}
		assertEquals(3, recCount);
	}
	

	/**
	 *  Test for a data source with poorly formatted fields.
	 *  No handler.
	 * @throws JetelException 
	 */
	
	public void test_badFile() throws ComponentNotReadyException, JetelException {
		boolean failed = false;
		// test no handler ------------------------------------
		aParser2.init();
		aParser2.setDataSource(in2);
		try{
			while((record=aParser2.getNext(record))!=null){
				fail("Should throw BadDataFormatException");
			}
		} catch (BadDataFormatException e){	
			failed = true;
		}
		aParser2.close();
		if(!failed)
			fail("Should raise an RuntimeException");
	}
	

	/**
	 *  Test for a data source with poorly formatted fields.
	 *  No handler.
	 */
	public void test_strict_badFile() throws ComponentNotReadyException, JetelException {
		IParserExceptionHandler aHandler = null;
		boolean failed = false;

		// test strict handler ------------------------------------
		aParser2.init();
		aParser2.setDataSource(in2);
		aHandler = ParserExceptionHandlerFactory.getHandler(PolicyType.STRICT);
		aParser2.setExceptionHandler(aHandler);
		int recCount = 0;
		try{
			while((record=aParser2.getNext(record))!=null){
				recCount++;
			}
		}catch (BadDataFormatException e) {
			failed = true;
		}
		if(!failed)
			fail("Should raise an BadDataFormatException");
		assertEquals(0,recCount);
		aParser2.close();
	}
	

	/**
	 *  Test for a data source with poorly formatted fields.
	 *  controlled handler.
	 */
	public void test_controlled_badFile() throws JetelException, ComponentNotReadyException {
		IParserExceptionHandler aHandler = null;

		// test controlled handler ------------------------------------
		aParser2.init();
		aParser2.setDataSource(in2);
		aHandler = ParserExceptionHandlerFactory.getHandler(PolicyType.CONTROLLED);
		aParser2.setExceptionHandler(aHandler);
		int recCount = 0;
		while((record != null)){
			try {
				record = aParser2.getNext(record);
				recCount++;
			} catch (BadDataFormatException e) {
				for (BadDataFormatException exception : e) {
					System.out.println(exception.getMessage());
				}					
				System.out.println("Message from handler:");
				System.out.println(aHandler.getErrorMessage());
			}
		}
		assertEquals(3,recCount);  //may need to be revised
		//depending how we implement nullable property
		aParser2.close();

	}
	

	/**
	 *  Test for a data source with poorly formatted fields.
	 *  lenient handler.
	 */
	public void test_lenient_badFile() throws ComponentNotReadyException, JetelException {
		IParserExceptionHandler aHandler = null;
		// test lenient handler ------------------------------------
		aParser2.init();
		aParser2.setDataSource(in2);
		aHandler = ParserExceptionHandlerFactory.getHandler(PolicyType.LENIENT);
		aParser2.setExceptionHandler(aHandler);
		int recCount = 0;

		// the content of the test file
		// N/AStone 01/11/93
		// -15.5 112 11/03/02
		// -0.7Bone Broo 99
		while ((record = aParser2.getNext(record)) != null) {
			if (recCount == 0) {
				assertEquals("-15.5", record.getField(0).toString());
				assertEquals("", record.getField(1).toString());
				assertEquals(null, record.getField(1).getValue());
			} else if (recCount == 1) {
				assertEquals("", record.getField(3).toString());
				assertNull(record.getField(3).getValue());
			}
			recCount++;
		}
		assertEquals(2, recCount);
	}

}