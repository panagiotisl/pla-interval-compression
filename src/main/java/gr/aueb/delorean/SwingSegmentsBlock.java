package gr.aueb.delorean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

public class SwingSegmentsBlock {

	int size;
	int minB;
	int maxB;
	BitSet betas;
	private byte[] alphas;
	private byte[] timestamps;

	public SwingSegmentsBlock(List<Integer> b, List<List<Float>> a, List<List<List<Integer>>> t) {

		this.size = b.size();
		this.minB = b.get(0);
		this.maxB = b.get(b.size() - 1);
		if (this.minB > this.maxB) {
			throw new IllegalArgumentException("b values must be ordered.");
		}

		betas = new BitSet(this.maxB - this.minB + 1);
		for (Integer value : b) {
			this.betas.set(value - this.minB);
		}

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    ObjectOutputStream os = new ObjectOutputStream(bos);) {
			for (List<Float> list : a) {
				os.writeByte(list.size());  // assumes maximum 255 alphas per beta
				for (Float value : list) {
					os.writeFloat(value);
				}
			}
			os.close();
			alphas = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    ObjectOutputStream os = new ObjectOutputStream(bos);) {
			for (List<List<Integer>> outlist : t) {
				for (List<Integer> list : outlist) {
					os.writeByte(list.size());  // assumes maximum 255 timestamps per alpha
					for (int value : list) {
						os.writeInt(value);
					}
				}
			}
			os.close();
			timestamps = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println((betas.length() / 8) + 1 + alphas.length + timestamps.length);

	}

	public List<Integer> getBetas() {
		return betas.stream().mapToObj(i -> i + this.minB).collect(Collectors.toList());
	}

	public List<List<Float>> getAlphas() {
		int elements = this.size;
		List<List<Float>> newAlphas = new ArrayList<>();
		try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(this.alphas));) {
				while (elements-- > 0) {
				int length = is.readUnsignedByte();  // assumes maximum 255 alphas per beta
				List<Float> tempList = new ArrayList<>();
				for (int i = 0; i < length; i++) {
					tempList.add(is.readFloat());
				}
				newAlphas.add(tempList);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newAlphas;
	}

	public List<List<List<Integer>>> getTimestamps() {
		int elements = this.size;
		List<List<List<Integer>>> newTimestamps = new ArrayList<>();
		try (ObjectInputStream isAlphas = new ObjectInputStream(new ByteArrayInputStream(this.alphas));
				ObjectInputStream isTimestamps = new ObjectInputStream(new ByteArrayInputStream(this.timestamps));) {
			while (elements-- > 0) {
				int length = isAlphas.readUnsignedByte();  // assumes maximum 255 alphas per beta
				List<List<Integer>> tempListA = new ArrayList<>();
				for (int i = 0; i < length; i++) {
					isAlphas.readFloat();
					int timestampsLength = isTimestamps.readUnsignedByte(); // assumes maximum 255 timestamps per alpha
					List<Integer> tempListT = new ArrayList<>();
					for (int j = 0; j < timestampsLength; j++) {
						tempListT.add(isTimestamps.readInt());
					}
					tempListA.add(tempListT);
				}
				newTimestamps.add(tempListA);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return newTimestamps;
	}

}
