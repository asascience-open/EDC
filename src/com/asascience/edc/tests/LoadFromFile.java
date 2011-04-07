package com.asascience.edc.tests;

import java.io.IOException;
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

    String dap = "http://edac-dap3.northerngulfinstitute.org/thredds/dodsC/ncom_fukushima_agg/Fukushima_best.ncd";
    String tdslink = "http://edac-dap3.northerngulfinstitute.org/thredds/ncom_fukushima_agg/catalog.xml#ncom_fukushima_agg/Fukushima_best.ncd";
    
    ThreddsDataFactory.Result tdf = new ThreddsDataFactory().openFeatureDataset(tdslink,null);
    ucar.nc2.dt.grid.GridDataset gds1 = (ucar.nc2.dt.grid.GridDataset)tdf.featureDataset;
    LatLonRect bounds1 = gds1.getBoundingBox();
    System.out.println(bounds1);
    
    NetcdfDataset ncd = new ThreddsDataFactory().openDataset(tdslink,true,null,null);
    ucar.nc2.dt.grid.GridDataset gds2 = ucar.nc2.dt.grid.GridDataset.open(ncd.getLocation());
    LatLonRect bounds2 = gds2.getBoundingBox();
    System.out.println(bounds2);
    
    
    ucar.nc2.dt.grid.GridDataset gds3 = ucar.nc2.dt.grid.GridDataset.open(dap);
    LatLonRect bounds3 = gds3.getBoundingBox();
    System.out.println(bounds3);
  }

}
