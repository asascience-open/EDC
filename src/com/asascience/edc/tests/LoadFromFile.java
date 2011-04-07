package com.asascience.edc.tests;

import java.io.IOException;
import java.util.Formatter;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.thredds.ThreddsDataFactory;
import ucar.unidata.geoloc.LatLonRect;
/**
 *
 * @author Kyle
 */
public class LoadFromFile {
  public static void main(String[] args) throws IOException {
    testEDC();
  }

  private static void testEDC() throws IOException {
    ThreddsDataFactory.Result tdf = new ThreddsDataFactory().openFeatureDataset("http://edac-dap.northerngulfinstitute.org/thredds/catalog/ncom_reg6_agg/catalog.xml#ncom_reg6_agg/NCOM_Region_6_Aggregation_best.ncd",null);
    ucar.nc2.dt.grid.GridDataset gds1 = (ucar.nc2.dt.grid.GridDataset)tdf.featureDataset;
    LatLonRect bounds1 = gds1.getBoundingBox();
    System.out.println(bounds1);

    NetcdfDataset ncd = new ThreddsDataFactory().openDataset("http://edac-dap.northerngulfinstitute.org/thredds/catalog/ncom_reg6_agg/catalog.xml#ncom_reg6_agg/NCOM_Region_6_Aggregation_best.ncd",true,null,null);
    ucar.nc2.dt.grid.GridDataset gds2 = ucar.nc2.dt.grid.GridDataset.open(ncd.getLocation());
    LatLonRect bounds2 = gds2.getBoundingBox();
    System.out.println(bounds2);

    ucar.nc2.dt.grid.GridDataset gds3 = ucar.nc2.dt.grid.GridDataset.open("http://edac-dap.northerngulfinstitute.org/thredds/dodsC/ncom_reg6_agg/NCOM_Region_6_Aggregation_best.ncd");
    LatLonRect bounds3 = gds3.getBoundingBox();
    System.out.println(bounds3);
  }

}
