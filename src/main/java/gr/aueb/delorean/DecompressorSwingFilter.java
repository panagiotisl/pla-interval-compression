package gr.aueb.delorean;

import java.util.List;

public class DecompressorSwingFilter {

	private List<? extends SwingSegment> swingSegments;
    private float storedVal = 0f;
    private boolean endOfStream = false;
    private int currentElement = 0;
    private int currentTimestampOffset = 0;
    private SwingSegment swingSegment;
	private SwingSegment nextSwingSegment;
	private long finalTimestamp;
	private long lastTimestamp;

    public DecompressorSwingFilter(List<? extends SwingSegment> swingSegments, long lastTimestamp) {
    	this.swingSegments = swingSegments;
    	this.swingSegment = swingSegments.get(0);
		if (swingSegments.size() > 1)
			this.nextSwingSegment = swingSegments.get(1);
		else
			this.nextSwingSegment = null;
		this.lastTimestamp = lastTimestamp;
    }

    /**
     * Returns the next pair in the time series, if available.
     *
     * @return Pair if there's next value, null if series is done.
     */
    public Float readValue() {
        next();
        if(endOfStream) {
            return null;
        }
        return storedVal;
    }

    private void next() {
		if (nextSwingSegment != null)
			finalTimestamp = nextSwingSegment.getInitialTimestamp();
		else
			finalTimestamp = lastTimestamp + 1;
    	if (finalTimestamp > (swingSegment.getInitialTimestamp() + currentTimestampOffset)) {
    		storedVal = (float) swingSegment.getLine().get(swingSegment.getInitialTimestamp() + currentTimestampOffset);
    		currentTimestampOffset++;
    	} else {
    		currentElement++;
    		if (currentElement < swingSegments.size()) {
    			swingSegment = swingSegments.get(currentElement);
				if (currentElement + 1 < swingSegments.size())
					nextSwingSegment = swingSegments.get(currentElement + 1);
				else
					nextSwingSegment = null;
    			storedVal = (float) swingSegment.getLine().get(swingSegment.getInitialTimestamp());

    			currentTimestampOffset = 1;
    		} else {
    			endOfStream = true;
    		}
    	}
	}

}
