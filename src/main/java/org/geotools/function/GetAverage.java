package org.geotools.function;
import java.util.Arrays;
import java.util.Vector;

//Different method to get Average of an value array
public class GetAverage {
	public static double get_average_double(Vector<Double> valuearray) {
		if (valuearray == null || valuearray.size() == 0)
			return 0.0;
		double sum = 0;
		for (int i = 0; i < valuearray.size(); i++)
			sum += valuearray.get(i);
		return sum / valuearray.size();
	}

	//Get average of int array
	public static double get_average_int(Vector<Integer> rgb) {
		if (rgb == null || rgb.size() == 0)
			return 0.0;
		double sum = 0;
		for (int i = 0; i < rgb.size(); i++)
			sum += rgb.get(i);
		return sum / rgb.size();
	}
	public static double get_average_float(Vector<Float> rgb) {
		if (rgb == null || rgb.size() == 0)
			return 0.0;
		double sum = 0;
		for (int i = 0; i < rgb.size(); i++)
			sum += rgb.get(i);
		return sum / rgb.size();
	}

	//Get median used in split
	public static double averageofmedian(double[] array) {
		// To avoid GeometryColllection error, the median should not equal to any element in array.
		double median;
		Arrays.sort(array);
		int len = array.length;
	//	System.out.println(len);
		//No matter even or odd, use average of two elements in the middle to avoid GeomClooection Error.
		//If len/2, len/2+1, and len/2-1 are equal, i++ until not equal, we get one distinct pair.
		//Also we can closest pair in the middle, but dosen't affect the average split so much.
		int i = 1;
		while ((array[len/2-1] == array[len/2-1+i]) && (array[len/2-1] == array[len/2-1-i])) {
			i++;
		}
		if (array[len/2-1] != array[len/2-1+i]){
			median = ( array[len / 2 - 1] + array[len / 2 - 1 + i]) / 2;
		} else {
			median = ( array[len / 2 - 1] + array[len / 2 - 1 - i]) / 2;
		}
		
		// Actually lots of polygons will have i = 2, even 3.
		// if (i > 1) System.out.println("i = "+ i);

		return median;
	}

	//Get average used in split
	public static double averageofall(double[] array) {
		double sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum / array.length;
	}


}
