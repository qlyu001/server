package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.Geometry;
import edu.ucr.cs.hdf.*;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;

/**
 * A raster manager that can open HDF files.
 * Created by Ahmed Eldawy on 5/26/17.
 */
public class HDFRasterManager implements RasterManager {
  /**The name of the  layer to read from the HDF file*/
  private final String datasetName;
  /**The underlying HDF file*/
  private final HDFFile hdfFile;
  /**The location of this file in the MODIS sinusoidal grid*/
  private int h, v;

  /**Resolution of the raster file (either 1200, 2400, or 4800)*/
  private int resolution;

  /**Maximum value as appears in HDF metadata*/
  private int maxValue;
  /**Minimum value as appears in HDF metadata*/
  private int minValue;

  /**The data that has been loaded*/
  private DataBufferByte dataBuffer;

  public HDFRasterManager(String filePath, String datasetName) throws IOException {
    this.hdfFile = new HDFFile(filePath);
    // Retrieve the h and v values (only for MODIS files from the LP DAAC archive)
    String archiveMetadata = (String) hdfFile.findHeaderByName("ArchiveMetadata.0").getEntryAt(0);
    String coreMetadata = (String) hdfFile.findHeaderByName("CoreMetadata.0").getEntryAt(0);
    try {
      this.h = getIntByName(archiveMetadata, "HORIZONTALTILENUMBER");
      this.v = getIntByName(archiveMetadata, "VERTICALTILENUMBER");
    } catch (RuntimeException e) {
      // For WaterMask (MOD44W), these values are found somewhere else
      try {
        this.h = getIntByName(coreMetadata, "HORIZONTALTILENUMBER");
        this.v = getIntByName(coreMetadata, "VERTICALTILENUMBER");
      } catch (RuntimeException e2) {
        throw new RuntimeException("Could not getPixel h and v values for an HDF file");
      }
    }
    this.resolution = getIntByName(archiveMetadata, "DATACOLUMNS");
    this.datasetName = datasetName;
  }

  @Override
  public int[] getPixel(int column, int row, int[] value) {
    if (value == null)
      value = new int[1];
    value[0] = dataBuffer.getElem(row * resolution + column);
    return value;
  }

  @Override
  public int[] getPixels(int column, int row, int width, int height, int[] value) {
    return new int[0];
  }

  @Override
  public void loadRaster(Rectangle rect) throws IOException {
    if (this.dataBuffer != null)
      return;
    // TODO user the rect to read part of the raster file
    // Find the required dataset and read it
    DDVGroup dataGroup = hdfFile.findGroupByName(datasetName);
    int fillValuee = 0;
    boolean fillValueFound = false;
    for (DataDescriptor dd : dataGroup.getContents()) {
      if (dd instanceof DDVDataHeader) {
        DDVDataHeader vheader = (DDVDataHeader) dd;
        if (vheader.getName().equals("_FillValue")) {
          Object fillValue = vheader.getEntryAt(0);
          if (fillValue instanceof Integer)
            fillValuee = (Integer) fillValue;
          else if (fillValue instanceof Short)
            fillValuee = (Short) fillValue;
          else if (fillValue instanceof Byte)
            fillValuee = (Byte) fillValue;
          else
            throw new RuntimeException("Unsupported type: "+fillValue.getClass());
          fillValueFound = true;
        } else if (vheader.getName().equals("valid_range")) {
          Object minValue = vheader.getEntryAt(0);
          if (minValue instanceof Integer)
            this.minValue = (Integer) minValue;
          else if (minValue instanceof Short)
            this.minValue = (Short) minValue;
          else if (minValue instanceof Byte)
            this.minValue = (Byte) minValue;
          Object maxValue = vheader.getEntryAt(1);
          if (maxValue instanceof Integer)
            this.maxValue = (Integer) maxValue;
          else if (maxValue instanceof Short)
            this.maxValue = (Short) maxValue;
          else if (maxValue instanceof Byte)
            this.maxValue = (Byte) maxValue;
        }
      }
    }
    // Retrieve data
    int valueSize;
    for (DataDescriptor dd : dataGroup.getContents()) {
      if (dd instanceof DDNumericDataGroup) {
        DDNumericDataGroup numericDataGroup = (DDNumericDataGroup) dd;
        valueSize = numericDataGroup.getDataSize();
        resolution = numericDataGroup.getDimensions()[0];
        byte[] unparsedDataArray = new byte[valueSize * resolution * resolution];
        if (fillValueFound) {
          byte[] fillValueBytes = new byte[valueSize];
          HDFConstants.writeAt(fillValueBytes, 0, fillValuee, valueSize);
          for (int i = 0; i < unparsedDataArray.length; i++)
            unparsedDataArray[i] = fillValueBytes[i % valueSize];
        }
        numericDataGroup.getAsByteArray(unparsedDataArray, 0, unparsedDataArray.length);
        this.dataBuffer = new DataBufferByte(unparsedDataArray, resolution * resolution);
      }
    }
  }

  @Override
  public int getHeight() {
    return resolution;
  }

  @Override
  public int getWidth() {
    return resolution;
  }

  @Override
  public int getNumBands() {
    return 1;
  }

  @Override
  public GridCoverage2D getGridCoverage() throws IOException {
    return null;
  }

  @Override
  public int getTileWidth() {
    return resolution;
  }

  @Override
  public int getTileHeight() {
    return 1;
  }

  @Override
  public int getTileX() {
    return 0;
  }

  @Override
  public int getTileY() {
    return 0;
  }

  @Override
  public Geometry reproject(Geometry geom, CoordinateReferenceSystem crs) throws TransformException {
    throw new RuntimeException("Not supported (yet)");
  }

  private static int getIntByName(String metadata, String name) {
    String strValue = getStringByName(metadata, name);
    if (strValue == null)
      throw new RuntimeException("Couldn't find value with name '"+name+"'");
    return Integer.parseInt(strValue);
  }

  private static String getStringByName(String metadata, String name) {
    int offset = metadata.indexOf(name);
    if (offset == -1)
      return null;
    offset = metadata.indexOf(" VALUE", offset);
    if (offset == -1)
      return null;
    offset = metadata.indexOf('=', offset);
    if (offset == -1)
      return null;
    do offset++; while (offset < metadata.length() &&
        metadata.charAt(offset) == ' ' || metadata.charAt(offset) == '"');
    int endOffset = offset;
    do endOffset++;  while (endOffset < metadata.length() && metadata.charAt(endOffset) != ' '
        && metadata.charAt(endOffset) != '"'
        && metadata.charAt(endOffset) != '\n'
        && metadata.charAt(endOffset) != '\r');
    if (offset < metadata.length())
      return metadata.substring(offset, endOffset);
    return null;
  }

  public DirectPosition2D r2v(int col, int row, DirectPosition2D outPoint) throws TransformException {
    if (outPoint == null)
      outPoint = new DirectPosition2D();
    outPoint.y = (90 - v * 10) -
        (row+0.5) * 10 / resolution;
    outPoint.x = (h * 10 - 180) +
        (col+0.5) * 10 / resolution;
    return outPoint;
  }

  public DirectPosition2D v2r(DirectPosition2D inPoint, DirectPosition2D outPoint) throws TransformException {
    if (outPoint == null)
      outPoint = new DirectPosition2D();
    outPoint.y = (90 - v * 10 - inPoint.y) * resolution / 10 - 0.5;
    outPoint.x = (inPoint.x - (h * 10 - 180)) * resolution / 10 - 0.5;
    // Convert to raster by rounding the number
    outPoint.x = Math.floor(outPoint.x);
    outPoint.y = Math.floor(outPoint.y);
    return outPoint;
  }
}
