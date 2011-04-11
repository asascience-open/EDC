/*
 * Applied Science Associates, Inc.
 * Copyright 2008. All Rights Reserved.
 *
 * NcellLookup.java
 *
 * Created on Oct 31, 2008 @ 8:39:07 AM
 */
package com.asascience.openmap.layer.nc.ncell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.asascience.utilities.BinarySearch;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class NcellLookup {

  private Hashtable<Double, ArrayList<Integer>> htLat;
  private Hashtable<Double, ArrayList<Integer>> htLon;
  private Double[] uniqueLats;
  private Double[] deltaLats;
  private Double[] uniqueLons;
  private Double[] deltaLons;
  private BinarySearch binSearch;

  public NcellLookup(double[] lats, double[] lons, int ncells) {
    binSearch = new BinarySearch();
    htLat = new Hashtable<Double, ArrayList<Integer>>();
    htLon = new Hashtable<Double, ArrayList<Integer>>();
    double lat, lon;
    List<Integer> latList;
    List<Integer> lonList;
    for (int i = 0; i < ncells; i++) {
      lat = lats[i];
      if (!htLat.containsKey(lat)) {
        htLat.put(lat, new ArrayList<Integer>());
      }
      latList = htLat.get(lat);
      latList.add(i);

      lon = lons[i];
      if (!htLon.containsKey(lon)) {
        htLon.put(lon, new ArrayList<Integer>());
      }
      lonList = htLon.get(lon);
      lonList.add(i);
    }
    Set<Double> s = htLat.keySet();
    uniqueLats = s.toArray(new Double[0]);
    s = htLon.keySet();
    uniqueLons = s.toArray(new Double[0]);
    Arrays.sort(uniqueLats);
    Arrays.sort(uniqueLons);
    deltaLats = new Double[uniqueLats.length - 1];
    deltaLons = new Double[uniqueLons.length - 1];
    /** Attempt at dealing with dissimilar lat/lon intervals. */
    for (int i = 0; i < uniqueLats.length - 1; i++) {
      deltaLats[i] = (uniqueLats[i + 1] - uniqueLats[i]);
    }
    for (int i = 0; i < uniqueLons.length - 1; i++) {
      deltaLons[i] = (uniqueLons[i + 1] - uniqueLons[i]);
    }
  }

  public void printLatLonKeys() {
    for (int i = 0; i < uniqueLats.length; i++) {
      System.out.println(uniqueLats[i] + "," + uniqueLons[i]);
    }
  }

  public int getNcellIndexFromLatLon(double lat, double lon) {
    try {
      /** Calculate the lat tolerance. */
      double tol = (uniqueLats[1] - uniqueLats[0]) * 0.5;
      /** Determine the "real" lat to search for. */
      int index = binSearch.doubleSearch(uniqueLats, 0, uniqueLats.length, lat, tol);
      // int index = binSearch.doubleSearch(uniqueLats, 0,
      // uniqueLats.length, lat, deltaLats);
      if (index == -1) {
        return -1;
      }
      double searchLat = uniqueLats[index];

      /** Obtain the lat indexes. */
      List<Integer> latIndexes;
      if (htLat.containsKey(searchLat)) {
        latIndexes = htLat.get(searchLat);
      } else {
        return -1;
      }

      /** Calculate the lon tolerance. */
      tol = (uniqueLons[1] - uniqueLons[0]) * 0.5;
      /** Determine the "real" lon to search for. */
      index = binSearch.doubleSearch(uniqueLons, 0, uniqueLons.length, lon, tol);
      // index = binSearch.doubleSearch(uniqueLons, 0, uniqueLons.length,
      // lon, deltaLons);
      if (index == -1) {
        return -1;
      }
      double searchLon = uniqueLons[index];

      // searchLon = -154.66220092773438;
      searchLon = -154.6628875732422;
      /** Obtain the lon indexes. */
      List<Integer> lonIndexes;

      if (htLon.containsKey(searchLon)) {
        lonIndexes = htLon.get(searchLon);
      } else {
        return -1;
      }

      /** Ensure there are values in the returned List objects. */
      if (latIndexes.size() == 0 | lonIndexes.size() == 0) {
        return -1;
      }

      /** Compare the two sets of indexes and return the common entry. */
      for (int lt : latIndexes) {
        if (Collections.binarySearch(lonIndexes, lt) >= 0) {
          return lt;
        }
      }

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return -1;
  }
}
