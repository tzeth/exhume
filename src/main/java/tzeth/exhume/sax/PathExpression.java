package tzeth.exhume.sax;

import static com.google.common.base.Preconditions.checkArgument;
import static tzeth.exhume.sax.Path.SEPARATOR;
import static tzeth.preconds.MorePreconditions.checkNotEmpty;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import tzeth.exceptions.NotImplementedYetException;
import tzeth.exhume.ExhumeException;

public final class PathExpression {
	// TODO: Implement wildcard support
	
	private final ImmutableList<Particle> particles;
	private final boolean absolute;

	public static PathExpression of(String parent, String leaf) {
		checkNotEmpty(leaf);
		if (parent.isEmpty()) {
			return of(leaf);
		}
		if (leaf.startsWith(SEPARATOR)) {
			throw new ExhumeException("Leaf expression cannot be absolute when using a root path");
		}
		String expression = parent.endsWith(SEPARATOR)
				? parent + leaf
				: parent + SEPARATOR + leaf;
		return of(expression);
	}
	
	public static PathExpression of(String expression) {
		return new PathExpression(expression);
	}
	
	public PathExpression(String expression) {
		checkArgument(!expression.endsWith(SEPARATOR));
		this.absolute = expression.startsWith(SEPARATOR);
		ImmutableList.Builder<Particle> builder = ImmutableList.builder();
		String toSplit = this.absolute
				? expression.substring(1)
				: expression;
		for (String part : toSplit.split("\\/")) {
			if (part.equals("*")) {
				builder.add(new OneLevelWildcard());
			} else if (part.equals("**")) {
				builder.add(new MultiLevelWildcard());
			} else {
				builder.add(new Name(part));
			}
		}
		this.particles = builder.build();
		checkArgument(this.particles.size() > 0);
		checkArgument(this.particles.get(this.particles.size() - 1) instanceof Name, "The expression must end with a name");
		boolean prevWasWildcard = false;
		for (Particle p : this.particles) {
			if (p instanceof Name) {
				prevWasWildcard = false;
				continue;
			} else if (prevWasWildcard) {
				throw new IllegalArgumentException("Consecutive wildcards not allowed");
			} else {
				prevWasWildcard = true;
			}
		}
	}

	public boolean matches(String path) {
		return matches(Path.of(path));
	}

	public boolean matches(Path path) {
		ImmutableList<String> pathParticles = path.getParticles();
		if (absolute) {
			List<String> expressionParts = this.particles.stream()
					.map(Particle::name)
					.collect(Collectors.toList());
			return pathParticles.equals(expressionParts);
		} else {
			String pathAsString = path.toString();
			String expressionPartsAsString = this.toString();
			return pathAsString.endsWith(expressionPartsAsString);
		}
	}

	@Override
	public String toString() {
		String prefix = absolute
				? SEPARATOR
				: "";
		return this.particles.stream()
				.map(Particle::name)
				.collect(Collectors.joining(SEPARATOR, prefix, ""));
	}
	
	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof PathExpression) {
			return this.toString().equals(obj.toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	private static interface Particle {
		public String name();
	}
	
	private static final class Name implements Particle {
		private final String name;
		
		public Name(String name) {
			this.name = name;
		}
		
		@Override
		public String name() {
			return name;
		}
	}
	
	private static final class OneLevelWildcard implements Particle {
		public OneLevelWildcard() {
			throw new NotImplementedYetException();
		}
		
		@Override
		public String name() {
			return "*";
		}
	}
	
	private static final class MultiLevelWildcard implements Particle {
		public MultiLevelWildcard() {
			throw new NotImplementedYetException();
		}
		
		@Override
		public String name() {
			return "**";
		}
	}
}
