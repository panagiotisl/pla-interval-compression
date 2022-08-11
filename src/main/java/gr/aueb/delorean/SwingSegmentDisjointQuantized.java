package gr.aueb.delorean;
public class SwingSegmentDisjointQuantized extends SwingSegment {

	double aMin;
	double aMax;
	float b;

	public SwingSegmentDisjointQuantized(long initialTimestamp, double aMin, double aMax, float b) {
		super(initialTimestamp, new LinearFunction((aMin + aMax) / 2, b));
		this.aMin = aMin;
		this.aMax = aMax;
		this.b = b;
	}

	public double getaMax() {
		return aMax;
	}

	public double getaMin() {
		return aMin;
	}

	public float getB() {
		return b;
	}

}
