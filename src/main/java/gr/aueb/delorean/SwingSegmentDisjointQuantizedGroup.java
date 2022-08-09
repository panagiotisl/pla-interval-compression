package gr.aueb.delorean;

import java.util.ArrayList;
import java.util.Iterator;

public class SwingSegmentDisjointQuantizedGroup {

	ArrayList<Long> timestamps;
	double aMin;
	double aMax;
	float b;

	public SwingSegmentDisjointQuantizedGroup(long timestamp, double aMin, double aMax, float b) {
		this.timestamps = new ArrayList<>();
		this.timestamps.add(timestamp);
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

	public double getA(){
		return (aMax + aMin) / 2;
	}

	public float getB() {
		return b;
	}

	public void addTimestamp(long timestamp) {timestamps.add(timestamp);}

	public Iterator<Long> getTimestampIterator(){
		return timestamps.iterator();
	}

}
