package com.numbercruncher.rainbow;

import static java.lang.Math.*;

/**
 * Sobol quasi-random sequence (up to 6 dimensions).
 *
 * Instance-based: each thread can create its own Sobol object.
 *
 * Based on Numerical Recipes. Original Java translation by Huang Wen Hui 2012.
 */
public class Sobol {
  private static final int MAXBIT = 30;
  private static final int MAXDIM = 6;
  private static final int[] mdeg = {1, 2, 3, 3, 4, 4};
  private static final int[] ip   = {0, 1, 1, 2, 1, 4};
  private static final int[] IV_INIT = {
      1, 1, 1, 1, 1, 1,
      3, 1, 3, 3, 1, 1,
      5, 7, 7, 3, 3, 5,
     15,11, 5,15,13, 9
  };

  private int in;
  private final int[] ix = new int[MAXDIM];
  private final int[][] iu = new int[MAXBIT][MAXDIM];
  private final double fac;

  public Sobol() {
    fac = 1.0 / (1 << MAXBIT);
    // Copy initial direction numbers
    for (int j = 0; j < 4; j++)
      for (int k = 0; k < MAXDIM; k++)
        iu[j][k] = IV_INIT[j * MAXDIM + k];
    // Compute remaining direction numbers
    for (int k = 0; k < MAXDIM; k++) {
      for (int j = 0; j < mdeg[k]; j++)
        iu[j][k] <<= (MAXBIT - 1 - j);
      for (int j = mdeg[k]; j < MAXBIT; j++) {
        int ipp = ip[k];
        int i = iu[j - mdeg[k]][k];
        i ^= (i >> mdeg[k]);
        for (int l = mdeg[k] - 1; l >= 1; l--) {
          if ((ipp & 1) != 0)
            i ^= iu[j - l][k];
          ipp >>= 1;
        }
        iu[j][k] = i;
      }
    }
    in = 0;
  }

  /**
   * Generate the next quasi-random point.
   *
   * @param n   number of dimensions to fill (1..6)
   * @param x   output array, x[0..n-1] filled with values in [0, 1)
   */
  public void next(int n, double[] x) {
    int im = in++;
    int j;
    for (j = 0; j < MAXBIT; j++) {
      if ((im & 1) == 0) break;
      im >>= 1;
    }
    if (j >= MAXBIT) throw new IllegalStateException("MAXBIT too small in Sobol sequence");
    for (int k = 0; k < min(n, MAXDIM); k++) {
      ix[k] ^= iu[j][k];
      x[k] = ix[k] * fac;
    }
  }

  /**
   * Skip ahead by generating (and discarding) {@code count} points.
   * Useful for partitioning the sequence across threads:
   * thread i skips i*chunkSize, then generates chunkSize points.
   */
  public void skip(int count) {
    double[] dummy = new double[MAXDIM];
    for (int i = 0; i < count; i++)
      next(MAXDIM, dummy);
  }
}
