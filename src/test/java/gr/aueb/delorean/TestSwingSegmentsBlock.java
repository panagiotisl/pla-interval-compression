package gr.aueb.delorean;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

public class TestSwingSegmentsBlock {

	@Test
	public void testBlockBetas() {

		List<Integer> b = new ArrayList<>();
		List<List<Float>> a = new ArrayList<>();
		List<List<List<Integer>>> timestamps = new ArrayList<>();

		List<Float> tempA;
		List<List<Integer>> tempTimestamps;

		b.add(3);
		tempA = new ArrayList<>();
		tempTimestamps = new ArrayList<>();
		tempA.add((float) -0.019582002323217648);
		tempTimestamps.add(ImmutableList.of(6728, 11626, 12251));
		tempA.add((float) -0.013304591836734658);
		tempTimestamps.add(ImmutableList.of(6418));
		tempA.add((float) -0.00913729533050333);
		tempTimestamps.add(ImmutableList.of(15177));
		tempA.add((float) 0.0017928834667683491);
		tempTimestamps.add(ImmutableList.of(23707));
		tempA.add((float) 0.007429490715205004);
		tempTimestamps.add(ImmutableList.of(32424, 3738));
		tempA.add((float) 0.011535233743154542);
		tempTimestamps.add(ImmutableList.of(20818));
		tempA.add((float) 0.014225684508037472);
		tempTimestamps.add(ImmutableList.of(30882));
		a.add(tempA);
		timestamps.add(tempTimestamps);

		b.add(5);
		tempA = new ArrayList<>();
		tempTimestamps = new ArrayList<>();
		tempA.add((float) -0.6783021390374331);
		tempTimestamps.add(ImmutableList.of(15035));
		tempA.add((float) -0.5584696969696971);
		tempTimestamps.add(ImmutableList.of(4070));
		tempA.add((float) -0.4413333333333333);
		tempTimestamps.add(ImmutableList.of(4090, 13117));
		tempA.add((float) -0.2773585858585859);
		tempTimestamps.add(ImmutableList.of(13138));
		tempA.add((float) -0.19664731182795708);
		tempTimestamps.add(ImmutableList.of(13162));
		a.add(tempA);
		timestamps.add(tempTimestamps);

		SwingSegmentsBlock block = new SwingSegmentsBlock(b, a , timestamps);
		List<Integer> newBetas = block.getBetas();
		assertEquals(b, newBetas);

		List<List<Float>> newAlphas = block.getAlphas();
		assertEquals(a, newAlphas);

		List<List<List<Integer>>> newTimestamps = block.getTimestamps();
		assertEquals(timestamps, newTimestamps);
	}

}
