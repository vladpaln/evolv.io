package evolv.io.tests;

import org.junit.Assert;
import org.junit.Test;

import evolv.io.RouletteWheel;

public class RouletteWheelTest {
	@Test(expected = IllegalArgumentException.class)
	public void testBadChanceThrows() {
		RouletteWheel<String> rw = new RouletteWheel<>();
		rw.addElement(-1, "Hi");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testZeroChanceThrows() {
		RouletteWheel<String> rw = new RouletteWheel<>();
		rw.addElement(0, "Hi");
	}

	@Test
	public void testNoElements() {
		RouletteWheel<String> rw = new RouletteWheel<>();
		String r = rw.getRandom();
		Assert.assertNull(r);
	}

	@Test
	public void testOneElement() {
		RouletteWheel<String> rw = new RouletteWheel<>();
		Assert.assertTrue(rw.addElement(1, "Hi"));
		String s = rw.getRandom();
		Assert.assertEquals("Hi", s);
	}

	@Test
	public void testAlreadyAddedElement() {
		RouletteWheel<String> rw = new RouletteWheel<>();
		Assert.assertTrue(rw.addElement(1, "Hi"));
		Assert.assertFalse(rw.addElement(2, "Hi"));
	}

	@Test
	public void testRemoveAddedElement() {
		RouletteWheel<String> rw = new RouletteWheel<>();
		Assert.assertTrue(rw.addElement(1, "Hi"));
		Assert.assertTrue(rw.removeElement("Hi"));
	}

	@Test
	public void testRemoveNotAddedElement() {
		RouletteWheel<String> rw = new RouletteWheel<>();
		Assert.assertTrue(rw.addElement(1, "Hi"));
		Assert.assertFalse(rw.removeElement("Goodbye"));
	}

	@Test
	public void testElementCount() {
		RouletteWheel<String> rw = new RouletteWheel<>();
		Assert.assertEquals(0, rw.getElementCount());
		rw.addElement(1, "Hi");
		Assert.assertEquals(1, rw.getElementCount());
		rw.addElement(2, "Hi");
		Assert.assertEquals(1, rw.getElementCount());
		rw.addElement(3, "Goodbye");
		Assert.assertEquals(2, rw.getElementCount());
		rw.removeElement("Hi");
		Assert.assertEquals(1, rw.getElementCount());
		rw.removeElement("Goodbye");
		Assert.assertEquals(0, rw.getElementCount());
	}

	@Test
	public void testTotalChance() {
		RouletteWheel<String> rw = new RouletteWheel<>();
		double delta = 1E-9;
		Assert.assertEquals(0, rw.getTotalChance(), delta);
		rw.addElement(1, "Hi");
		Assert.assertEquals(1, rw.getTotalChance(), delta);
		rw.addElement(2, "Hi");
		Assert.assertEquals(1, rw.getTotalChance(), delta);
		rw.addElement(3, "Goodbye");
		Assert.assertEquals(4, rw.getTotalChance(), delta);
		rw.removeElement("Hi");
		Assert.assertEquals(3, rw.getTotalChance(), delta);
		rw.removeElement("Goodbye");
		Assert.assertEquals(0, rw.getTotalChance(), delta);
	}

	@Test
	public void testChances() {
		double maxSquaredError = 0;
		for (int j = 0; j < 1_000; j++) {
			RouletteWheel<Character> rw = new RouletteWheel<>();
			double aChance = 0.1;
			double bChance = 0.3;
			double cChance = 0.6;
			double xChance = 0.0;
			rw.addElement(aChance, 'a');
			rw.addElement(bChance, 'b');
			rw.addElement(cChance, 'c');
			final int total = 10_000;
			int aCount = 0;
			int bCount = 0;
			int cCount = 0;
			int xCount = 0;
			for (int i = 0; i < total; i++) {
				char ch = rw.getRandom();
				if (ch == 'a') {
					aCount++;
				} else if (ch == 'b') {
					bCount++;
				} else if (ch == 'c') {
					cCount++;
				} else {
					xCount++;
				}
			}
			double aActual = (float) aCount / total;
			double bActual = (float) bCount / total;
			double cActual = (float) cCount / total;
			double xActual = (float) xCount / total;
			double aDiff = aActual - aChance;
			double bDiff = bActual - bChance;
			double cDiff = cActual - cChance;
			double xDiff = xActual - xChance;
			double squaredError = aDiff * aDiff + bDiff * bDiff + cDiff * cDiff + xDiff * xDiff;
			maxSquaredError = Math.max(maxSquaredError, squaredError);
		}
		Assert.assertTrue(maxSquaredError < 1E-3);
	}
}
