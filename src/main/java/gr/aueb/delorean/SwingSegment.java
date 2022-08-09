package gr.aueb.delorean;
public class SwingSegment {

	private long initialTimestamp;
	private LinearFunction line;

	public SwingSegment(long initialTimestamp, LinearFunction line) {
		this.initialTimestamp = initialTimestamp;
		this.line = line;
	}

	public long getInitialTimestamp() {
		return initialTimestamp;
	}

	public LinearFunction getLine() {
		return line;
	}

	@Override
	public String toString() {
		return String.format("%d: %f", getInitialTimestamp(), getLine());
	}

}
