package tzeth.exhume.sax;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import tzeth.exhume.sax.Path;

public class PathTest {

	@Test(expected=IllegalArgumentException.class)
	public void pathMustStartWithSeparator() {
		Path.of("a/b/c");
	}

	@Test(expected=IllegalArgumentException.class)
	public void pathCannotEndWithSeparator() {
		Path.of("/a/b/c/");
	}

	@Test(expected=IllegalArgumentException.class)
	public void allParticlesMustBeDefined() {
		Path.of("/a//c");
	}
	
	@Test
	public void singleParticleIsAllowed() {
		Path p = Path.of("/a");
		assertEquals(1, p.getParticles().size());
		assertEquals("a", p.getParticles().get(0));
	}
	
	@Test
	public void threeParticlePath() {
		Path p = Path.of("/a/b/c");
		assertEquals(3, p.getParticles().size());
		assertEquals(Arrays.asList("a", "b", "c"), p.getParticles());
	}
	
	@Test
	public void testToString() {
		String s = "/a/b/c";
		Path p = Path.of(s);
		assertEquals(s, p.toString());
	}
	
}
