/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * BinarySearch.java
 *
 * Created on Jan 5, 2009 @ 3:10:29 PM
 */
package com.asascience.utilities;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class BinarySearch {

  public BinarySearch() {
  }

  /** INTEGER */
  /** Integer binary search without tolerance. */
  public int intSearch(Integer[] inArray, int firstIndex, int lastIndex, int targetVal) {
    return intSearch(inArray, firstIndex, lastIndex, targetVal, 0);
  }

  /** Integer binary search without tolerance. */
  public int intSearch(Integer[] inArray, int firstIndex, int lastIndex, int targetVal, int tolerance) {
    int midPoint = 0;
    int arrVal;

    /** Find the first midpoint. */
    if (inArray.length % 2 == 0) {
      midPoint = ((firstIndex + lastIndex) / 2) - 1;
    } else {
      midPoint = (firstIndex + lastIndex) / 2;
    }

    /** Perform the binary search. */
    while (firstIndex <= lastIndex) {
      if (midPoint < 0 | midPoint >= inArray.length) {
        return -1;
      }
      arrVal = inArray[midPoint];

      /** Determine if the midpoint is appropriate and if so, return it. */
      if (targetVal == arrVal) {
        return midPoint;
      } else if ((targetVal >= (arrVal - tolerance)) & (targetVal < (arrVal + tolerance))) {
        return midPoint;
      }

      /** Increment the first or last index accordingly. */
      if (targetVal < arrVal) {
        lastIndex = midPoint - 1;
      } else {
        firstIndex = midPoint + 1;
      }

      /** Recalculate the midpoint. */
      midPoint = (firstIndex + lastIndex) / 2;
    }
    return -1;
  }

  /** Integer binary search without tolerance. */
  public int intSearch(int[] inArray, int firstIndex, int lastIndex, int targetVal) {
    return intSearch(inArray, firstIndex, lastIndex, targetVal, 0);
  }

  /** Integer Binary Search with tolerance. */
  public int intSearch(int[] inArray, int firstIndex, int lastIndex, int targetVal, int tolerance) {
    int midPoint = 0;
    int arrVal;

    /** Find the first midpoint. */
    if (inArray.length % 2 == 0) {
      midPoint = ((firstIndex + lastIndex) / 2) - 1;
    } else {
      midPoint = (firstIndex + lastIndex) / 2;
    }

    /** Perform the binary search. */
    while (firstIndex <= lastIndex) {
      if (midPoint < 0 | midPoint >= inArray.length) {
        return -1;
      }
      arrVal = inArray[midPoint];

      /** Determine if the midpoint is appropriate and if so, return it. */
      if (targetVal == arrVal) {
        return midPoint;
      } else if ((targetVal >= (arrVal - tolerance)) & (targetVal < (arrVal + tolerance))) {
        return midPoint;
      }

      /** Increment the first or last index accordingly. */
      if (targetVal < arrVal) {
        lastIndex = midPoint - 1;
      } else {
        firstIndex = midPoint + 1;
      }

      /** Recalculate the midpoint. */
      midPoint = (firstIndex + lastIndex) / 2;
    }
    return -1;
  }

  /** LONG */
  /** Long Binary Search with tolerance. */
  public int longSearch(Long[] inArray, int firstIndex, int lastIndex, long targetVal) {
    return longSearch(inArray, firstIndex, lastIndex, targetVal, 0l);
  }

  /** Long Binary Search with tolerance. */
  public int longSearch(Long[] inArray, int firstIndex, int lastIndex, long targetVal, long tolerance) {
    int midPoint = 0;
    long arrVal;

    /** Find the first midpoint. */
    if (inArray.length % 2 == 0) {
      midPoint = ((firstIndex + lastIndex) / 2) - 1;
    } else {
      midPoint = (firstIndex + lastIndex) / 2;
    }

    /** Perform the binary search. */
    while (firstIndex <= lastIndex) {
      if (midPoint < 0 | midPoint >= inArray.length) {
        return -1;
      }
      arrVal = inArray[midPoint];

      /** Determine if the midpoint is appropriate and if so, return it. */
      if (targetVal == arrVal) {
        return midPoint;
      } else if ((targetVal >= (arrVal - tolerance)) & (targetVal < (arrVal + tolerance))) {
        return midPoint;
      }

      /** Increment the first or last index accordingly. */
      if (targetVal < arrVal) {
        lastIndex = midPoint - 1;
      } else {
        firstIndex = midPoint + 1;
      }

      /** Recalculate the midpoint. */
      midPoint = (firstIndex + lastIndex) / 2;
    }
    return -1;
  }

  /** Long Binary Search with tolerance. */
  public int longSearch(long[] inArray, int firstIndex, int lastIndex, long targetVal) {
    return longSearch(inArray, firstIndex, lastIndex, targetVal, 0l);
  }

  /** Long Binary Search with tolerance. */
  public int longSearch(long[] inArray, int firstIndex, int lastIndex, long targetVal, long tolerance) {
    int midPoint = 0;
    long arrVal;

    /** Find the first midpoint. */
    if (inArray.length % 2 == 0) {
      midPoint = ((firstIndex + lastIndex) / 2) - 1;
    } else {
      midPoint = (firstIndex + lastIndex) / 2;
    }

    /** Perform the binary search. */
    while (firstIndex <= lastIndex) {
      if (midPoint < 0 | midPoint >= inArray.length) {
        return -1;
      }
      arrVal = inArray[midPoint];

      /** Determine if the midpoint is appropriate and if so, return it. */
      if (targetVal == arrVal) {
        return midPoint;
      } else if ((targetVal >= (arrVal - tolerance)) & (targetVal < (arrVal + tolerance))) {
        return midPoint;
      }

      /** Increment the first or last index accordingly. */
      if (targetVal < arrVal) {
        lastIndex = midPoint - 1;
      } else {
        firstIndex = midPoint + 1;
      }

      /** Recalculate the midpoint. */
      midPoint = (firstIndex + lastIndex) / 2;
    }
    return -1;
  }

  /** FLOAT */
  /** Float binary search without tolerance. */
  public int floatSearch(Float[] inArray, int firstIndex, int lastIndex, float targetVal) {
    return floatSearch(inArray, firstIndex, lastIndex, targetVal, 0f);
  }

  /** Float binary search without tolerance. */
  public int floatSearch(Float[] inArray, int firstIndex, int lastIndex, float targetVal, float tolerance) {
    int midPoint = 0;
    float arrVal;
    float min, max;
    /** Get the precision of the number. */
    String sValue = Double.valueOf(targetVal).toString();
    int precision = sValue.substring(sValue.indexOf(".") + 1).length();

    /** Find the first midpoint. */
    if (inArray.length % 2 == 0) {
      midPoint = ((firstIndex + lastIndex) / 2) - 1;
    } else {
      midPoint = (firstIndex + lastIndex) / 2;
    }

    /** Perform the binary search. */
    while (firstIndex <= lastIndex) {

      if (midPoint < 0 | midPoint >= inArray.length) {
        return -1;
      }
      arrVal = inArray[midPoint];

      /**
       * Round the upper and lower bounds to the precision of the
       * targetVal.
       */
      min = roundFloat((arrVal - tolerance), precision);
      max = roundFloat((arrVal + tolerance), precision);

      /** Determine if the midpoint is appropriate and if so, return it. */
      if (targetVal == arrVal) {
        return midPoint;
      } else if ((targetVal >= min) & (targetVal < max)) {
        return midPoint;
      }

      /** Increment the first or last index accordingly. */
      if (targetVal < arrVal) {
        lastIndex = midPoint - 1;
      } else {
        firstIndex = midPoint + 1;
      }

      /** Recalculate the midpoint. */
      midPoint = (firstIndex + lastIndex) / 2;
    }
    return -1;
  }

  /** Float binary search without tolerance. */
  public int floatSearch(float[] inArray, int firstIndex, int lastIndex, float targetVal) {
    return floatSearch(inArray, firstIndex, lastIndex, targetVal, 0f);
  }

  /** Float Binary Search with tolerance. */
  public int floatSearch(float[] inArray, int firstIndex, int lastIndex, float targetVal, float tolerance) {
    int midPoint = 0;
    float arrVal;
    float min, max;
    /** Get the precision of the number. */
    String sValue = Double.valueOf(targetVal).toString();
    int precision = sValue.substring(sValue.indexOf(".") + 1).length();

    /** Find the first midpoint. */
    if (inArray.length % 2 == 0) {
      midPoint = ((firstIndex + lastIndex) / 2) - 1;
    } else {
      midPoint = (firstIndex + lastIndex) / 2;
    }

    /** Perform the binary search. */
    while (firstIndex <= lastIndex) {

      if (midPoint < 0 | midPoint >= inArray.length) {
        return -1;
      }
      arrVal = inArray[midPoint];

      /**
       * Round the upper and lower bounds to the precision of the
       * targetVal.
       */
      min = roundFloat((arrVal - tolerance), precision);
      max = roundFloat((arrVal + tolerance), precision);

      /** Determine if the midpoint is appropriate and if so, return it. */
      if (targetVal == arrVal) {
        return midPoint;
      } else if ((targetVal >= min) & (targetVal < max)) {
        return midPoint;
      }

      /** Increment the first or last index accordingly. */
      if (targetVal < arrVal) {
        lastIndex = midPoint - 1;
      } else {
        firstIndex = midPoint + 1;
      }

      /** Recalculate the midpoint. */
      midPoint = (firstIndex + lastIndex) / 2;
    }
    return -1;
  }

  /** DOUBLE */
  /** Double binary search without tolerance. */
  public int doubleSearch(Double[] inArray, int firstIndex, int lastIndex, double targetVal) {
    return doubleSearch(inArray, firstIndex, lastIndex, targetVal, 0d);
  }

  /** Double Binary Search with tolerance. */
  public int doubleSearch(Double[] inArray, int firstIndex, int lastIndex, double targetVal, double tolerance) {
    int midPoint = 0;
    double arrVal;
    double min, max;
    /** Get the precision of the number. */
    String sValue = Double.valueOf(targetVal).toString();
    int precision = sValue.substring(sValue.indexOf(".") + 1).length();

    /** Find the first midpoint. */
    if (inArray.length % 2 == 0) {
      midPoint = ((firstIndex + lastIndex) / 2) - 1;
    } else {
      midPoint = (firstIndex + lastIndex) / 2;
    }

    /** Perform the binary search. */
    while (firstIndex <= lastIndex) {

      if (midPoint < 0 | midPoint >= inArray.length) {
        return -1;
      }
      arrVal = inArray[midPoint];

      /**
       * Round the upper and lower bounds to the precision of the
       * targetVal.
       */
      min = roundDouble((arrVal - tolerance), precision);
      max = roundDouble((arrVal + tolerance), precision);

      /** Determine if the midpoint is appropriate and if so, return it. */
      if (targetVal == arrVal) {
        return midPoint;
      } else if ((targetVal >= min) & (targetVal < max)) {
        return midPoint;
      }

      /** Increment the first or last index accordingly. */
      if (targetVal < arrVal) {
        lastIndex = midPoint - 1;
      } else {
        firstIndex = midPoint + 1;
      }

      /** Recalculate the midpoint. */
      midPoint = (firstIndex + lastIndex) / 2;
    }
    return -1;
  }

  /** Double binary search without tolerance. */
  public int doubleSearch(double[] inArray, int firstIndex, int lastIndex, double targetVal) {
    return doubleSearch(inArray, firstIndex, lastIndex, targetVal, 0d);
  }

  /** Double binary search with tolerance. */
  public int doubleSearch(double[] inArray, int firstIndex, int lastIndex, double targetVal, double tolerance) {
    int midPoint = 0;
    double arrVal;
    double min, max;
    /** Get the precision of the number. */
    String sValue = Double.valueOf(targetVal).toString();
    int precision = sValue.substring(sValue.indexOf(".") + 1).length();

    /** Find the first midpoint. */
    if (inArray.length % 2 == 0) {
      midPoint = ((firstIndex + lastIndex) / 2) - 1;
    } else {
      midPoint = (firstIndex + lastIndex) / 2;
    }

    /** Perform the binary search. */
    while (firstIndex <= lastIndex) {

      if (midPoint < 0 | midPoint >= inArray.length) {
        return -1;
      }
      arrVal = inArray[midPoint];

      /**
       * Round the upper and lower bounds to the precision of the
       * targetVal.
       */
      min = roundDouble((arrVal - tolerance), precision);
      max = roundDouble((arrVal + tolerance), precision);

      /** Determine if the midpoint is appropriate and if so, return it. */
      if (targetVal == arrVal) {
        return midPoint;
      } else if ((targetVal >= min) & (targetVal < max)) {
        return midPoint;
      }

      /** Increment the first or last index accordingly. */
      if (targetVal < arrVal) {
        lastIndex = midPoint - 1;
      } else {
        firstIndex = midPoint + 1;
      }

      /** Recalculate the midpoint. */
      midPoint = (firstIndex + lastIndex) / 2;
    }
    return -1;
  }

  public int doubleSearch(Double[] inArray, int firstIndex, int lastIndex, double targetVal, Double[] tolArray) {
    int midPoint = 0;
    double arrVal;
    double min, max;
    /** Get the precision of the number. */
    String sValue = Double.valueOf(targetVal).toString();
    int precision = sValue.substring(sValue.indexOf(".") + 1).length();

    /** Find the first midpoint. */
    if (inArray.length % 2 == 0) {
      midPoint = ((firstIndex + lastIndex) / 2) - 1;
    } else {
      midPoint = (firstIndex + lastIndex) / 2;
    }

    /** Perform the binary search. */
    while (firstIndex <= lastIndex) {

      if (midPoint < 0 | midPoint >= inArray.length) {
        return -1;
      }
      arrVal = inArray[midPoint];

      /**
       * Round the upper and lower bounds to the precision of the
       * targetVal.
       */
      min = roundDouble((arrVal - (tolArray[(midPoint == 0) ? midPoint : midPoint - 1] * 0.5)), precision);
      max = roundDouble((arrVal + (tolArray[(midPoint == tolArray.length) ? midPoint - 1 : midPoint] * 0.5)),
              precision);

      /** Determine if the midpoint is appropriate and if so, return it. */
      if (targetVal == arrVal) {
        return midPoint;
      } else if ((targetVal >= min) & (targetVal < max)) {
        return midPoint;
      }

      /** Increment the first or last index accordingly. */
      if (targetVal < arrVal) {
        lastIndex = midPoint - 1;
      } else {
        firstIndex = midPoint + 1;
      }

      /** Recalculate the midpoint. */
      midPoint = (firstIndex + lastIndex) / 2;
    }
    return -1;
  }

  /** Local rounding functions for speed. */
  private double roundDouble(double value, int precision) {
    int sign = (value >= 0) ? 1 : -1;
    double factor = Math.pow(10, precision);
    double n = value * factor;

    n = sign * Math.abs(Math.floor(n + 0.5));

    return n / factor;
  }

  private float roundFloat(float value, int precision) {
    int sign = (value >= 0) ? 1 : -1;
    double factor = Math.pow(10, precision);
    double n = value * factor;

    n = sign * Math.abs(Math.floor(n + 0.5));

    return Double.valueOf((n / factor)).floatValue();
  }
}
