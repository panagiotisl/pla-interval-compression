package gr.aueb.delorean;

/**
 * Implements the Chimp128 time series compression. Value compression
 * is for floating points only.
 *
 * @author Panagiotis Liakos
 */
public class ChimpN32 {

    private int storedLeadingZeros = Integer.MAX_VALUE;
    private int storedValues[];
    private boolean first = true;
    private int size;
    private int previousValuesLog2;
    private int threshold;

    public final static short[] leadingRepresentation = {0, 0, 0, 0, 0, 0, 0, 0,
			1, 1, 1, 1, 2, 2, 2, 2,
			3, 3, 4, 4, 5, 5, 6, 6,
			7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 7
		};

    public final static short[] leadingRound = {0, 0, 0, 0, 0, 0, 0, 0,
			8, 8, 8, 8, 12, 12, 12, 12,
			16, 16, 18, 18, 20, 20, 22, 22,
			24, 24, 24, 24, 24, 24, 24, 24,
			24, 24, 24, 24, 24, 24, 24, 24,
			24, 24, 24, 24, 24, 24, 24, 24,
			24, 24, 24, 24, 24, 24, 24, 24,
			24, 24, 24, 24, 24, 24, 24, 24
		};
//    public final static short FIRST_DELTA_BITS = 27;

    private OutputBitStream out;
	private int previousValues;

	private int setLsb;
	private int[] indices;
	private int index = 0;
	private int current = 0;

    // We should have access to the series?
    public ChimpN32(int previousValues) {
        out = new OutputBitStream(new byte[1000*4]);
        size = 0;
        this.previousValues = previousValues;
        this.previousValuesLog2 =  (int)(Math.log(previousValues) / Math.log(2));
        this.threshold = 5 + previousValuesLog2;
        this.setLsb = (int) Math.pow(2, threshold + 1) - 1;
        this.indices = new int[(int) Math.pow(2, threshold + 1)];
        this.storedValues = new int[previousValues];
    }

    /**
     * Adds a new long value to the series. Note, values must be inserted in order.
     *
     * @param value next floating point value in the series
     */
    public void addValue(int value) {
        if(first) {
            writeFirst(value);
        } else {
            compressValue(value);
        }
    }

    /**
     * Adds a new double value to the series. Note, values must be inserted in order.
     *
     * @param value next floating point value in the series
     */
    public void addValue(float value) {
        if(first) {
            writeFirst(Float.floatToRawIntBits(value));
        } else {
            compressValue(Float.floatToRawIntBits(value));
        }
    }

    private void writeFirst(int value) {
    	first = false;
        storedValues[current] = value;
        out.writeInt(storedValues[current], 32);
        indices[value & setLsb] = index;
        size += 32;
    }

    /**
     * Closes the block and writes the remaining stuff to the BitOutput.
     */
    public void close() {
    	addValue(Float.NaN);
    	out.writeBit(false);
        out.flush();
    }

    private void compressValue(int value) {
    	int key = value & setLsb;
    	int xor;
    	int previousIndex;
    	int trailingZeros;
    	int currIndex = indices[key];
    	if ((index - currIndex) < previousValues) {
    		int tempXor = value ^ storedValues[currIndex % previousValues];
            trailingZeros = Integer.numberOfTrailingZeros(tempXor);

    		if (trailingZeros > threshold) {
    			previousIndex = currIndex % previousValues;
    			xor = tempXor;
    		} else {
    			previousIndex =  index % previousValues;
    			xor = storedValues[previousIndex] ^ value;
    			trailingZeros = Integer.numberOfTrailingZeros(xor);
    		}
    	} else {
    		previousIndex =  index % previousValues;
    		xor = storedValues[previousIndex] ^ value;
    		trailingZeros = Integer.numberOfTrailingZeros(xor);
    	}

        if(xor == 0) {
            // Write 0
        	out.writeBit(false);
        	out.writeBit(false);
            out.writeInt(previousIndex, previousValuesLog2);
            size += 2 + previousValuesLog2;
            storedLeadingZeros = 33;
        } else {
            int leadingZeros = Integer.numberOfLeadingZeros(xor);

            if (trailingZeros > threshold) {
                int significantBits = 32 - leadingRound[leadingZeros] - trailingZeros;
                out.writeInt(256 * (previousValues + previousIndex) + 32 * leadingRepresentation[leadingZeros] + significantBits, previousValuesLog2 + 10);
                out.writeInt(xor >>> trailingZeros, significantBits); // Store the meaningful bits of XOR
                size += 10 + significantBits + previousValuesLog2;
    			storedLeadingZeros = 33;
    		} else if (leadingRound[leadingZeros] == storedLeadingZeros) {
    			out.writeBit(true);
    			out.writeBit(false);
    			int significantBits = 32 - leadingRound[leadingZeros];
    			out.writeInt(xor, significantBits);
    			size += 2 + significantBits;
    		} else {
    			storedLeadingZeros = leadingRound[leadingZeros];
    			int significantBits = 32 - leadingRound[leadingZeros];
    			out.writeInt(16 + 8 + leadingRepresentation[leadingZeros], 5);
    			out.writeInt(xor, significantBits);
    			size += 5 + significantBits;
    		}
    	}
        current = ((current + 1) % previousValues);
        storedValues[current] = value;
		index++;
		indices[key] = index;

    }

    public int getSize() {
    	return size;
    }
    
    public byte[] getOut() {
		return out.buffer;
	}
}
