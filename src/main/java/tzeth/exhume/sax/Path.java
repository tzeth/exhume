package tzeth.exhume.sax;

import static com.google.common.base.Preconditions.checkArgument;
import static tzeth.preconds.MorePreconditions.checkNotEmpty;

import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

final class Path {
	public static final String SEPARATOR = "/";
	private final ImmutableList<String> particles;
	
	public static Path of(String s) {
		return new Path(s);
	}
	
	public Path(String s) {
		checkArgument(s.startsWith(SEPARATOR));
		checkArgument(!s.endsWith(SEPARATOR));
		String[] parts = s.substring(1).split("\\/");
		for (String p : parts) {
			checkNotEmpty(p);
		}
		this.particles = ImmutableList.copyOf(parts);
	}
	
	public ImmutableList<String> getParticles() {
		return particles;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Path) {
			return this.particles.equals(((Path) obj).particles);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.particles.hashCode();
	}

	@Override
	public String toString() {
		return this.particles.stream().collect(Collectors.joining(SEPARATOR, SEPARATOR, ""));
	}

}
