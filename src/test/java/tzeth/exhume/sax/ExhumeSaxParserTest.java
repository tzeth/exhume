package tzeth.exhume.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.xml.sax.SAXException;

import tzeth.exhume.ExhumeException;
import tzeth.exhume.XmlBuilder;
import tzeth.exhume.sax.ElementEnd;
import tzeth.exhume.sax.ElementStart;
import tzeth.exhume.sax.ExhumeSaxParser;
import tzeth.exhume.sax.RootPath;
import tzeth.exhume.sax.StartOfElement;

public final class ExhumeSaxParserTest {
	
	@Test
	public void sumOfBookPrices() {
		String xml = buildInventoryXml();
		BookPriceGatherer g = new BookPriceGatherer();
		ExhumeSaxParser p = new ExhumeSaxParser(g);
		
		try {
			p.parseXml(xml);
		} catch (SAXException e) {
			fail(e.getMessage());
		}
		
		int expectedPrice = 15 + 12;
		int actualPrice = g.totalPrice;
		assertEquals(expectedPrice, actualPrice);
	}
	
	public static class BookPriceGatherer {
		public int totalPrice;
		
		@ElementEnd("/Inventory/Books/Book/Price")
		public void price(Integer price) {
			totalPrice += price;
		}
	}

	
	@Test
	public void sumOfDvdPrices() {
		String xml = buildInventoryXml();
		DvdPriceGatherer g = new DvdPriceGatherer();
		ExhumeSaxParser p = new ExhumeSaxParser(g);
		
		try {
			p.parseXml(xml);
		} catch (SAXException e) {
			fail(e.getMessage());
		}
		
		int expectedPrice = 8 + 4 + 22;
		int actualPrice = g.totalPrice;
		assertEquals(expectedPrice, actualPrice);
	}
	
	@RootPath("/Inventory/DVDs/DVD")
	public static class DvdPriceGatherer {
		public int totalPrice;
		
		@ElementEnd("Price")
		public void price(Integer price) {
			totalPrice += price;
		}
	}


	@Test
	public void totalPriceOfAllItems() {
		String xml = buildInventoryXml();
		AllPricesGatherer g = new AllPricesGatherer();
		ExhumeSaxParser p = new ExhumeSaxParser(g);
		
		try {
			p.parseXml(xml);
		} catch (SAXException e) {
			fail(e.getMessage());
		}
		
		int expectedPrice = 15 + 12 + 8 + 4 + 22;
		int actualPrice = g.totalPrice;
		assertEquals(expectedPrice, actualPrice);
	}
	
	public static class AllPricesGatherer {
		public int totalPrice;
		
		@ElementEnd("Price")
		public void price(Integer price) {
			totalPrice += price;
		}
	}
	
	
	@Test
	public void dvdsOnSale() {
		String xml = buildInventoryXml();
		OnSaleDvdGatherer g = new OnSaleDvdGatherer();
		ExhumeSaxParser p = new ExhumeSaxParser(g);
		
		try {
			p.parseXml(xml);
		} catch (SAXException e) {
			fail(e.getMessage());
		}
		
		List<String> expectedTitles = Arrays.asList("Chinatown", "Pi");
		assertEquals(expectedTitles, g.titles);
	}
	
	public static class OnSaleDvdGatherer {
		public final List<String> titles = new ArrayList<>();
		private boolean onSale;
		
		@ElementStart("DVD")
		public void start(StartOfElement soe) {
			this.onSale = "true".equals(soe.attributeValue("onSale"));
		}
		
		@ElementEnd("DVD/Title")
		public void end(String title) {
			if (this.onSale) {
				this.titles.add(title);
			}
		}
	}

	@Test
	public void valueTypes() {
		XmlBuilder builder = new XmlBuilder();
		builder.root("values")
			.child("string").withValue("string").close()
			.child("integer").withValue("77").close()
			.child("double").withValue("1.25").close()
			.child("bigDecimal").withValue("7.7787").close()
			.child("booleanTrue").withValue("true").close()
			.child("boolean1").withValue("1").close()
			.child("booleanFalse").withValue("false").close()
			.child("boolean0").withValue("0").close()
			.child("date").withValue("2017-11-17").close();
		String xml = builder.toXml();
		TypeDetector d = new TypeDetector();
		ExhumeSaxParser p = new ExhumeSaxParser(d);
		
		try {
			p.parseXml(xml);
		} catch (SAXException e) {
			fail(e.getMessage());
		}
		
		assertEquals("string", d.stringVal);
		assertEquals(Integer.valueOf(77), d.integerVal);
		assertEquals(Double.valueOf(1.25), d.doubleVal);
		assertEquals(new BigDecimal("7.7787"), d.bigDecimalVal);
		assertEquals(Boolean.TRUE, d.booleanTrueVal);
		assertEquals(Boolean.TRUE, d.boolean1Val);
		assertEquals(Boolean.FALSE, d.booleanFalseVal);
		assertEquals(Boolean.FALSE, d.boolean0Val);
		assertEquals(LocalDate.of(2017, 11, 17), d.localDateVal);
	}
	
	public static class TypeDetector {
		public String stringVal;
		public Integer integerVal;
		public Double doubleVal;
		public BigDecimal bigDecimalVal;
		public Boolean booleanTrueVal;
		public Boolean boolean1Val;
		public Boolean booleanFalseVal;
		public Boolean boolean0Val;
		public LocalDate localDateVal;
		
		@ElementEnd("string")
		public void stringVal(String value) {
			this.stringVal = value;
		}
		
		@ElementEnd("integer")
		public void integerVal(Integer value) {
			this.integerVal = value;
		}
		
		@ElementEnd("double")
		public void doubleVal(Double value) {
			this.doubleVal = value;
		}
		
		@ElementEnd("bigDecimal")
		public void bigDecimalVal(BigDecimal value) {
			this.bigDecimalVal = value;
		}
		
		@ElementEnd("booleanTrue")
		public void booleanTrueVal(Boolean value) {
			this.booleanTrueVal = value;
		}
		
		@ElementEnd("boolean1")
		public void boolean1Val(Boolean value) {
			this.boolean1Val = value;
		}
		
		@ElementEnd("booleanFalse")
		public void booleanFalseVal(Boolean value) {
			this.booleanFalseVal = value;
		}
		
		@ElementEnd("boolean0")
		public void boolean0Val(Boolean value) {
			this.boolean0Val = value;
		}
		
		@ElementEnd("date")
		public void localDateVal(LocalDate value) {
			this.localDateVal = value;
		}
	}
	
	
	@Test(expected=ExhumeException.class)
	public void leafAppendedToRootPathCannotBeAbsolute() {
		// TODO: This test really belongs in a PathExpression unit test.
		String xml = buildInventoryXml();
		AbsoluteLeaf g = new AbsoluteLeaf();
		ExhumeSaxParser p = new ExhumeSaxParser(g);
		
		try {
			p.parseXml(xml);
		} catch (SAXException e) {
			fail(e.getMessage());
		}
	}
	
	@RootPath("/Inventory/Books/Book")
	public static class AbsoluteLeaf {
		
		@ElementEnd("/Title")
		public void title(String title) {
			fail("This handler should have been rejected at time of registration");
		}
	}
	
	
	@Test(expected=ExhumeException.class)
	public void typeConversionError() {
		String xml = buildInventoryXml();
		WrongValueType g = new WrongValueType();
		ExhumeSaxParser p = new ExhumeSaxParser(g);
		
		try {
			p.parseXml(xml);
		} catch (SAXException e) {
			fail(e.getMessage());
		}
	}
	
	public static class WrongValueType {
		
		@ElementEnd("/Inventory/Books/Book/Title")
		public void title(Double val) {
			fail("Type conversion should have failed before invoking this method");
		}
	}
	
	
	private static String buildInventoryXml() {
		XmlBuilder builder = new XmlBuilder();
		builder.root("Inventory")
			.child("Books")
				.child("Book").attribute("onSale", "false")
					.child("Title").withValue("East of Eden").close()
					.child("Price").withValue("15").close()
				.close()
				.child("Book").attribute("onSale", "false")
					.child("Title").withValue("Ghost Story").close()
					.child("Price").withValue("12").close()
				.close()
			.close()
			.child("DVDs")
				.child("DVD").attribute("onSale", "true")
					.child("Title").withValue("Chinatown").close()
					.child("Price").withValue("8").close()
				.close()
				.child("DVD").attribute("onSale", "true")
					.child("Title").withValue("Pi").close()
					.child("Price").withValue("4").close()
				.close()
				.child("DVD").attribute("onSale", "false")
					.child("Title").withValue("Interstellar").close()
					.child("Price").withValue("22").close()
				.close();
		return builder.toXml();
	}

}
