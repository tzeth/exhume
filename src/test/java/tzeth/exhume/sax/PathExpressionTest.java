package tzeth.exhume.sax;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import tzeth.exhume.sax.PathExpression;

public final class PathExpressionTest {

	@Test(expected=IllegalArgumentException.class)
	public void cannotEndWithSeparator() {
		PathExpression.of("/a/b/");
	}

	@Test(expected=IllegalArgumentException.class)
	@Ignore("Wildcard support not in place yet")
	public void mustEndWithName() {
		PathExpression.of("/a/*");
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore("Wildcard support not in place yet")
	public void consecutiveMultipleWildcardsNotAllowed() {
		PathExpression.of("a/**/**");
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore("Wildcard support not in place yet")
	public void consecutiveMixedWildcardsNotAllowed() {
		PathExpression.of("a/*/**");
	}

	@Test(expected=IllegalArgumentException.class)
	@Ignore("Wildcard support not in place yet")
	public void consecutiveSingleWildcardsNotAllowed() {
		PathExpression.of("a/*/*");
	}
	
	@Test
	public void testFullAbsolutePath() {
		String path = "/a/b/c";
		PathExpression expr = PathExpression.of(path);
		assertTrue(expr.matches(path));
		assertFalse(expr.matches("/x/y/z"));
	}

	@Test
	public void testFullRelativePath() {
		String path = "a/b/c";
		PathExpression expr = PathExpression.of(path);
		assertTrue(expr.matches("/" + path));
		assertTrue(expr.matches("/root/" + path));
		assertFalse(expr.matches("/root/x/y/z"));
	}

	@Test
	public void testLeaf() {
		PathExpression expr = PathExpression.of("z");
		assertTrue(expr.matches("/z"));
		assertTrue(expr.matches("/x/y/z"));
		assertFalse(expr.matches("/z/a"));
		assertFalse(expr.matches("/a/z/x"));
	}
	
}
