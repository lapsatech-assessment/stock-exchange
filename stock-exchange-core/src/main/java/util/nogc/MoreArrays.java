package util.nogc;

import java.util.Arrays;
import java.util.Comparator;

public final class MoreArrays {

  private MoreArrays() {
  }

  /**
   * Sort and reposition values in the given array by putting distinct values of
   * the array in the beginning of the array
   * 
   * Comparing values in natural order.
   * 
   * Returns the number of distinct value in that array
   * 
   * @param input the array will modified during this operation
   * @return the number of distinct values of the array
   */
  public static <T extends Comparable<? super T>> int distinct(T[] input) {
    return MoreArrays.distinct(input, Comparator.naturalOrder());
  }

  /**
   * Sort and reposition values in the given array by putting distinct values of
   * the array in the beginning of the array
   * 
   * Comparing values using provided comparator
   * 
   * Returns the number of distinct value in that array
   * 
   * 
   * @param input      the array will modified during this operation
   * @param comparator the comparator will be used comparing values to find
   *                   distinction
   * @return the number of distinct values of the array
   */
  public static <T> int distinct(T[] input, Comparator<T> comparator) {
    int newLen = input.length;

    int cpos = 1;
    Arrays.sort(input, comparator);
    while (cpos < newLen) {

      int shift = 0;

      while (cpos + shift < newLen
          && comparator.compare(input[cpos - 1], input[cpos + shift]) == 0) {
        shift++;
      }

      if (shift > 0) {
        for (int j = cpos + shift; j < input.length; j++) {
          input[j - shift] = input[j];
        }
        newLen -= shift;
      } else {
        cpos++;
      }
    }

    return newLen;
  }
}
