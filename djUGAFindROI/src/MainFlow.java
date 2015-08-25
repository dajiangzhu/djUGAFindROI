

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import edu.uga.liulab.djVtkBase.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author dajiang
 */
public class MainFlow {

	public static final float MESH_STEP = 6.0f;
	public static final float MESH_DIS_THRESHOLD = 2.0f;
	private djVtkSurData seedSurData;
	private djNiftiData seedVolData;
	private int seedSubID;
	private int seedGridNum;
	private List<Integer> validGridList = new ArrayList<Integer>();
	private List<Integer> nonSeedSubIDList = new ArrayList<Integer>();

	public void setSeedGridNum(int seedGridNum) {
		this.seedGridNum = seedGridNum;
	}

	public void setSeedSubID(int seedSubID) {
		this.seedSubID = seedSubID;
	}

	public List<Integer> getNonSeedSubIDList() {
		return nonSeedSubIDList;
	}

	private List<djVtkPoint> seedMeshPtList = new ArrayList<djVtkPoint>();

	public void setSeedSurData(djVtkSurData seedSurData) {
		this.seedSurData = seedSurData;
		this.seedSurData.calSurDataBox();
	}

	public void setSeedVolData(djNiftiData seedVolData) {
		this.seedVolData = seedVolData;
	}

	public void geneSeedVolMeshVoxel_back() throws Exception {
		System.out.println("Entering geneSeedVolMeshVoxel...");
		for (int i = 0; i < this.seedVolData.xSize; i++) {
			for (int j = 0; j < this.seedVolData.ySize; j++) {
				for (int k = 0; k < this.seedVolData.zSize; k++) {
					int[] coor = { k, j, i };
					this.seedVolData.rawNiftiData.putPix(Short.valueOf("0"), coor);
				}
			}
		} // for all coords in vol data
		for (int ptIndex = 0; ptIndex < this.seedMeshPtList.size(); ptIndex++) {
			System.out.println("dealing with " + ptIndex + "th point(grid)...");
			djVtkPoint tmpPoint = this.seedMeshPtList.get(ptIndex);
			float[] seedPhyCoords = { tmpPoint.x, tmpPoint.y, tmpPoint.z };
			int[] seedVolCoords = this.seedVolData.convertFromPhysicalToVolume(seedPhyCoords);
			int gridID = ptIndex + 1;
			String sql = "insert into test.t_allmapping (sub_id,pt_id,x,y,z,template_subid,grid_id,status)" + " values ('" + this.seedSubID + "',"
					+ tmpPoint.pointId + "," + seedVolCoords[0] + "," + seedVolCoords[1] + "," + seedVolCoords[2] + "," + this.seedSubID + ","
					+ gridID + ",1)";
			DatabaseTool.executeUpdate(sql);
			System.out.println("exc sql: " + sql + "...");
			int[] seedVolCoordsReverse = { seedVolCoords[2], seedVolCoords[1], seedVolCoords[0] };
			this.seedVolData.rawNiftiData.putPix(Short.valueOf(String.valueOf(gridID)), seedVolCoordsReverse);
		} // for all pt
		this.seedVolData.rawNiftiData.write("seed_" + this.seedSubID + "_vol");
	}

	public void geneSeedVolMeshVoxel() throws Exception {
		System.out.println("Entering geneSeedVolMeshVoxel...");
		for (int i = 0; i < this.seedVolData.xSize; i++) {
			for (int j = 0; j < this.seedVolData.ySize; j++) {
				for (int k = 0; k < this.seedVolData.zSize; k++) {
					int[] coor = { k, j, i };
					this.seedVolData.rawNiftiData.putPix(Short.valueOf("0"), coor);
				}
			}
		} // for all coords in vol data
		for (int ptIndex = 0; ptIndex < this.seedMeshPtList.size(); ptIndex++) {
			System.out.println("dealing with " + ptIndex + "th point(grid)...");
			djVtkPoint tmpPoint = this.seedMeshPtList.get(ptIndex);
			float[] seedPhyCoords = { tmpPoint.x, tmpPoint.y, tmpPoint.z };
			int[] seedVolCoords = this.seedVolData.convertFromPhysicalToVolume(seedPhyCoords);
			int gridID = ptIndex + 1;
			String sql = "insert into test.t_allmapping (sub_id,pt_id,x,y,z,template_subid,grid_id)" + " values ('" + this.seedSubID + "',"
					+ tmpPoint.pointId + "," + seedVolCoords[0] + "," + seedVolCoords[1] + "," + seedVolCoords[2] + "," + this.seedSubID + ","
					+ gridID + ")";
			DatabaseTool.executeUpdate(sql);
			System.out.println("exc sql: " + sql + "...");

			// labe the cube with 255
			int xFree = 1;
			int yFree = 1;
			int zFree = 1;
			if (seedVolCoords[0] == 0 || seedVolCoords[0] == this.seedVolData.xSize - 1) {
				xFree = 0;
			}
			if (seedVolCoords[1] == 0 || seedVolCoords[1] == this.seedVolData.ySize - 1) {
				yFree = 0;
			}
			if (seedVolCoords[2] == 0 || seedVolCoords[2] == this.seedVolData.zSize - 1) {
				zFree = 0;
			}
			for (int i = seedVolCoords[0] - xFree; i <= seedVolCoords[0] + xFree; i++) {
				for (int j = seedVolCoords[1] - yFree; j <= seedVolCoords[1] + yFree; j++) {
					for (int k = seedVolCoords[2] - zFree; k <= seedVolCoords[2] + zFree; k++) {
						int[] seedVolCoordsReverse = { k, j, i };
						this.seedVolData.rawNiftiData.putPix(Short.valueOf("255"), seedVolCoordsReverse);

					}
				}
			}
			this.seedVolData.rawNiftiData.write("./volGridFiles/" + gridID + "_" + this.seedSubID + "_vol");
			// labe the cube back with 0
			for (int i = seedVolCoords[0] - xFree; i <= seedVolCoords[0] + xFree; i++) {
				for (int j = seedVolCoords[1] - yFree; j <= seedVolCoords[1] + yFree; j++) {
					for (int k = seedVolCoords[2] - zFree; k <= seedVolCoords[2] + zFree; k++) {
						int[] seedVolCoordsReverse = { k, j, i };
						this.seedVolData.rawNiftiData.putPix(Short.valueOf("0"), seedVolCoordsReverse);
					}
				}
			}

		} // for all pt

	}

	public void geneSeedSurMeshPts() {
		System.out.println("Begin to geneSeedSurMeshPts...");
		for (float x = this.seedSurData.surBound[0][0]; x <= this.seedSurData.surBound[0][1]; x = x + MainFlow.MESH_STEP) {
			System.out.println("XMin = " + this.seedSurData.surBound[0][0] + " , XMax = " + this.seedSurData.surBound[0][1] + ": x = " + x);
			for (float y = this.seedSurData.surBound[1][0]; y <= this.seedSurData.surBound[1][1]; y = y + MainFlow.MESH_STEP) {
				for (float z = this.seedSurData.surBound[2][0]; z <= this.seedSurData.surBound[2][1]; z = z + MainFlow.MESH_STEP) {
					djVtkPoint tmpPt = new djVtkPoint();
					tmpPt.x = x;
					tmpPt.y = y;
					tmpPt.z = z;
					float tmpDis = 1000.0f;
					int tmpPtIndex = -1;
					for (int ptIndex = 0; ptIndex < this.seedSurData.nPointNum; ptIndex++) {
						float currentDis = djVtkUtil.calDistanceOfPoints(tmpPt, this.seedSurData.getPoint(ptIndex));
						if (currentDis < tmpDis) {
							tmpDis = currentDis;
							tmpPtIndex = ptIndex;
						}
					}
					if (tmpDis <= MainFlow.MESH_DIS_THRESHOLD) {
						seedMeshPtList.add(this.seedSurData.getPoint(tmpPtIndex));
					}
				} // for z
			} // for y
		} // for x
		this.writeToPointsVtkFile("seed_" + this.seedSubID + "_mesh.vtk", seedMeshPtList);
	}

	private float[][] getNewGridVoxelMappingArray() {
		float[][] gridVoxelMapping = new float[this.seedGridNum + 1][4];
		for (int i = 0; i <= this.seedGridNum; i++) {
			for (int j = 0; j < 4; j++) {
				gridVoxelMapping[i][j] = 0.0f;
			}
		}
		return gridVoxelMapping;
	}

	public void mapVolGridToSur() throws Exception {
		System.out.println("Entering mapVolGridToSur...");
		djVtkSurData currentSurData;
		djNiftiData currentVolData;
		// need to get the max num of grid
//		String strSql = "select * from test.t_allmapping_pdsd where sub_id=" + this.seedSubID;
//		List result = new ArrayList();
//		result = DatabaseTool.executeQuery(strSql);
//		this.seedGridNum = result.size();
//		if (this.seedGridNum < 1)
			this.seedGridNum = 2056;

		for (int subIndex = 0; subIndex < this.nonSeedSubIDList.size(); subIndex++) {
			int subID = this.nonSeedSubIDList.get(subIndex);
			System.out.println("Now is sub" + subID + "------------------------------------------------------------");
			String subName = "";
			if (subID < 10)
				subName = "pd000" + subID;
			else
				subName = "pd00" + subID;
			currentSurData = new djVtkSurData(DjFindROI.pdsdDataDir + subName + ".surf.topcorrect.vtk");
			List<djVtkPoint> currentSubGridPoints = new ArrayList<djVtkPoint>();
			List<djVtkPoint> currentSubVolumePoints = new ArrayList<djVtkPoint>(); // for test
			float[][] gridVoxelMapping = this.getNewGridVoxelMappingArray();// [gridIndex][x,y,z,num of voxel with same value]
			for (int gridID = 1; gridID <= this.seedGridNum; gridID++) {
				System.out.println("dealing with grid: " + gridID);
				currentVolData = new djNiftiData(DjFindROI.pdsdDataDir + "volumeFiles/" + subID + "/" + subID + "_" + gridID + "_grid_"
						+ this.seedSubID, DjFindROI.pdsdDataDir + "" + subName + ".b0.mhd");

				// find the grid voxel in the current volume file
				for (int i = 0; i < currentVolData.xSize; i++) {
					for (int j = 0; j < currentVolData.ySize; j++) {
						for (int k = 0; k < currentVolData.zSize; k++) {
							float tmpVoxelValue = currentVolData.getValueBasedOnVolumeCoordinate(i, j, k, 0);
							if (tmpVoxelValue > 1.0f) {
								gridVoxelMapping[gridID][0] = gridVoxelMapping[gridID][0] + i;
								gridVoxelMapping[gridID][1] = gridVoxelMapping[gridID][1] + j;
								gridVoxelMapping[gridID][2] = gridVoxelMapping[gridID][2] + k;
								gridVoxelMapping[gridID][3] += 1;
							} // if this voxel is the grid point
						} // for k
					} // for j
				} // for i

				// calculate the average voxel coords
				for (int j = 0; j < 3; j++) {
					gridVoxelMapping[gridID][j] = gridVoxelMapping[gridID][j] / gridVoxelMapping[gridID][3];
				}
				int[] tmpVolCoord = { (int) gridVoxelMapping[gridID][0], (int) gridVoxelMapping[gridID][1], (int) gridVoxelMapping[gridID][2] };
				float[] tmpPhyCoord = currentVolData.convertFromVolumeToPhysical(tmpVolCoord);
				//for test
				djVtkPoint pt = new djVtkPoint();
				pt.x = tmpPhyCoord[0];
				pt.y = tmpPhyCoord[1];
				pt.z = tmpPhyCoord[2];
				currentSubVolumePoints.add(pt);
				//for test end
				// find the nearest point on the current surface
				float tmpMinDis = 1000.0f;
				int tmpSelectedPtID = -1;
				djVtkPoint tmpPoint = new djVtkPoint();
				tmpPoint.x = tmpPhyCoord[0];
				tmpPoint.y = tmpPhyCoord[1];
				tmpPoint.z = tmpPhyCoord[2];
				for (int ptIndex = 0; ptIndex < currentSurData.nPointNum; ptIndex++) {
					float tmpDis = djVtkUtil.calDistanceOfPoints(tmpPoint, currentSurData.getPoint(ptIndex));
					if (tmpDis < tmpMinDis) {
						tmpMinDis = tmpDis;
						tmpSelectedPtID = ptIndex;
					}
				} // for all pt on the surface, find the nearest point
				currentSubGridPoints.add(currentSurData.getPoint(tmpSelectedPtID));
				// save to DB
				String sql = "insert into test.t_allmapping_ptsd (sub_id,pt_id,x,y,z,template_subid,grid_id)" + " values ('" + subID + "',"
						+ tmpSelectedPtID + "," + tmpVolCoord[0] + "," + tmpVolCoord[1] + "," + tmpVolCoord[2] + "," + this.seedSubID + "," + gridID
						+ ")";
				DatabaseTool.executeUpdate(sql);
				System.out.println("exc sql: " + sql + "...");
			} // for all gridID
				// test begin
			for (int t = 0; t < this.seedGridNum; t++) {
				if (gridVoxelMapping[t][3] != 1) {
					System.out.println("?????????????? when t is :" + t + " , count is " + gridVoxelMapping[t][3]);
				}
			} // test done
			this.writeToPointsVtkFile(DjFindROI.pdsdDataDir+""+subID + "_mesh_" + this.seedSubID + ".vtk", currentSubGridPoints);
			this.writeToPointsVtkFile(DjFindROI.pdsdDataDir+""+subID + "_mesh_" + this.seedSubID + "_volume.vtk", currentSubVolumePoints);
		} // for each non-seed sub
	}

	public void findCommonGridAcrossSubs() throws Exception {
		String strSql = "select max(grid_id) grid_max from test.t_allmapping";
		List result = DatabaseTool.executeQuery(strSql);
		HashMap dataMap = (HashMap) result.get(0);
		int gridMax = Integer.valueOf(String.valueOf(dataMap.get("grid_max")));
		System.out.println("gridMax is : " + gridMax);
		for (int gridIndex = 1; gridIndex <= gridMax; gridIndex++) {
			strSql = "select * from test.t_allmapping where status=1 and grid_id=" + gridIndex;
			result = DatabaseTool.executeQuery(strSql);
			if (result.size() == 15) {
				validGridList.add(gridIndex);
				// strSql = "insert into test.t_commongrid (grid_id,template_subid) values("+gridIndex+",'"+this.seedSubID+"')";
				// DatabaseTool.executeUpdate(strSql);
				// System.out.println("exc sql : "+strSql);
			}
		}
		System.out.println("valid grid num is : " + validGridList.size());
	}

	private float calDis(List<List<djVtkPoint>> allTraceData) {
		int dimMatrix = allTraceData.size();
		System.out.println("The dim of allTraceData is :" + dimMatrix);
		List<djVtkPoint> trace1;
		List<djVtkPoint> trace2;
		fiberBundleService fiberBundleDescriptor = new fiberBundleService();
		float totalDis = 0.0f;
		for (int i = 0; i < dimMatrix - 1; i++) {
			for (int j = i + 1; j < dimMatrix; j++) {
				trace1 = allTraceData.get(i);
				trace2 = allTraceData.get(j);
				float tmpDis = fiberBundleDescriptor.calFiberBundleDistance(trace1, trace2);
				totalDis = totalDis + tmpDis;
			}
		}
		return totalDis;
	}

	public void calculateGridDis() throws Exception {
		System.out.println("Enterig calculateGridDis ...");
		// need to get the max num of grid
		String strSql = "select * from test.t_allmapping where sub_id=" + this.seedSubID;
		List result = DatabaseTool.executeQuery(strSql);
		this.seedGridNum = result.size();

		List<Integer> allSubIDList = new ArrayList<Integer>();
		allSubIDList.addAll(this.nonSeedSubIDList);
		allSubIDList.add(this.seedSubID);

		for (int gridID = 1; gridID <= this.seedGridNum; gridID++) {
			System.out.println("dealing with the " + gridID + "th grid ...");
			List<List<djVtkPoint>> allTraceData = new ArrayList<List<djVtkPoint>>();
			for (int subIndex = 0; subIndex < allSubIDList.size(); subIndex++) { // it might read from db one time, but ensure for each gridID we have
																					// 15 different subID
				System.out.println("Now is sub" + subIndex + "--------------------------------------------------------------------------------");
				int subID = allSubIDList.get(subIndex);
				String traceFileName = "./findROI_traces/" + subID + "/" + gridID + "_" + subID + "_trace.vtk";
				djVtkSurData traceData = new djVtkSurData(traceFileName);
				allTraceData.add(traceData.points);
			} // for all subs
			float disOfCurrentGrid = this.calDis(allTraceData);
			float disOfCurrentGridAfterFormat = Math.round(disOfCurrentGrid * 100) / 100.0f;

			// strSql = "update test.t_commongrid set innerdistance=" + disOfCurrentGridAfterFormat + " where grid_id=" + gridID;
			strSql = "insert into test.t_commongrid (grid_id,template_subid,innerdistance) values(" + gridID + ",'" + this.seedSubID + "',"
					+ disOfCurrentGridAfterFormat + ")";
			System.out.println("exc sql : " + strSql);
			DatabaseTool.executeUpdate(strSql);

		} // for all valid grids
	}

	public void analyzeAllValidGrid() throws Exception {
		System.out.println("Enterig analyzeAllValidGrid ...");
		// need to get the max num of grid
		String strSql = "select * from test.t_allmapping where sub_id=" + this.seedSubID;
		List result = DatabaseTool.executeQuery(strSql);
		this.seedGridNum = result.size();
		// generate fibers and traces
		HashMap dataMap;
		List<Integer> allSubIDList = new ArrayList<Integer>();
		allSubIDList.addAll(this.nonSeedSubIDList);
		allSubIDList.add(this.seedSubID);

		for (int subIndex = 0; subIndex < allSubIDList.size(); subIndex++) {
			System.out.println("Now getting the sub" + subIndex
					+ "'s fiber and trace files...--------------------------------------------------------------------------------");
			int subID = allSubIDList.get(subIndex);
			djVtkSurData currentSurData = new djVtkSurData(DjFindROI.rootDataDir + subID + ".surf.vtk");
			djVtkFiberData currentFiberData = new djVtkFiberData(DjFindROI.rootDataDir + subID + ".asc.fiber.vtk");
			djVtkHybridData hybridData = new djVtkHybridData(currentSurData, currentFiberData);
			hybridData.mapSurfaceToBox();
			hybridData.mapFiberToBox();
			for (int gridID = 1; gridID <= this.seedGridNum; gridID++) {
				System.out.println("finished " + gridID + "th grid...");
				strSql = "select * from test.t_allmapping where sub_id=" + subID + " and template_subid='" + this.seedSubID + "' and grid_id="
						+ gridID;
				result = DatabaseTool.executeQuery(strSql);
				if (result.size() == 1) {
					dataMap = (HashMap) result.get(0);
					int pointID = Integer.valueOf(String.valueOf(dataMap.get("pt_id")));
					Set<djVtkPoint> neighbourPoints = currentSurData.getNeighbourPoints(pointID, 3);
					String fiberFileName = "./findROI_fibers/" + subID + "/" + gridID + "_" + subID + "_fibers.vtk";
					String traceFileName = "./findROI_traces/" + subID + "/" + gridID + "_" + subID + "_trace.vtk";
					hybridData.getFibersConnectToPointsSet(neighbourPoints).writeToVtkFileCompact(fiberFileName);
					djVtkFiberData tmpFiberData = new djVtkFiberData(fiberFileName);
					djVtkPoint tmpPoint = currentSurData.getPoint(pointID);
					fiberBundleService fiberBundleDescriptor = new fiberBundleService();
					fiberBundleDescriptor.setFiberData(tmpFiberData);
					fiberBundleDescriptor.setSeedPnt(tmpPoint);
					fiberBundleDescriptor.createFibersTrace();
					fiberBundleDescriptor.writeToVtkFile(traceFileName);
				} else {
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!sub_id = " + subID + " , grid_id = " + gridID + " : result.size = "
							+ result.size());
				}
			} // for all valid grids
		} // for all subs
	}

	public void displayResult() throws Exception {
		System.out.println("Enterig displayResult ...");
		List<djVtkPoint> ptList = new ArrayList<djVtkPoint>();
		List<Float> attriList = new ArrayList<Float>();
		HashMap dataMap;
		String strSql = "select a.sub_id,a.pt_id,b.innerdistance from test.t_allmapping a,test.t_commongrid b where a.sub_id=" + this.seedSubID
				+ " and a.grid_id=b.grid_id;";
		List result = DatabaseTool.executeQuery(strSql);
		System.out.println("exc sql : " + strSql);
		System.out.println("get " + result.size() + " records.. ");

		for (int i = 0; i < result.size(); i++) {
			dataMap = (HashMap) result.get(i);
			int pointID = Integer.valueOf(String.valueOf(dataMap.get("pt_id")));
			float dis = Float.valueOf(String.valueOf(dataMap.get("innerdistance")));
			ptList.add(this.seedSurData.getPoint(pointID));
			attriList.add(dis);
		}
		this.writeToPointsVtkFileWithAttribute("GridSimResult_" + this.seedSubID + ".vtk", ptList, attriList);
	}

	public void ayalyzeDB() throws Exception {
		String strSqL;
		List result;
		HashMap dataMap;
		djVtkSurData currentSurData;
		this.findCommonGridAcrossSubs();
		for (int subIndex = 0; subIndex < this.nonSeedSubIDList.size(); subIndex++) {
			System.out.println("Now is sub" + subIndex + "--------------------------------------------------------------------------------");
			int subID = this.nonSeedSubIDList.get(subIndex);
			List<djVtkPoint> currentSubGridPoints = new ArrayList<djVtkPoint>();
			currentSurData = new djVtkSurData(DjFindROI.rootDataDir + subID + ".surf.vtk");
			strSqL = "select * from test.t_allmapping where status=1 and sub_id=" + subID;
			result = DatabaseTool.executeQuery(strSqL);
			for (int i = 0; i < result.size(); i++) {
				dataMap = (HashMap) result.get(i);
				int gridID = Integer.valueOf(String.valueOf(dataMap.get("grid_id")));
				if (validGridList.contains(gridID)) {
					int pointID = Integer.valueOf(String.valueOf(dataMap.get("pt_id")));
					currentSubGridPoints.add(currentSurData.getPoint(pointID));
				} // if this grid_id exist across all subs
			} // for all records belong to this sub
			this.writeToPointsVtkFile(subID + "_mesh_" + this.seedSubID + "_CommonGrid.vtk", currentSubGridPoints);
		} // for all subs
	}

	public void printTraceSamplePoints() {
		List<djVtkPoint> tmp = new ArrayList<djVtkPoint>();
		double angleTheta = 0.0;
		double anglePhi = 0.0;
		for (int i = 0; i < 12; i++) {
			angleTheta = fiberBundleService.ANGLE_STEP * i;
			for (int j = 0; j < 12; j++) {
				anglePhi = fiberBundleService.ANGLE_STEP * j;
				djVtkPoint samplePoint = new djVtkPoint();
				samplePoint.x = (float) (Math.cos(angleTheta) * Math.cos(anglePhi));
				samplePoint.y = (float) (Math.sin(angleTheta) * Math.cos(anglePhi));
				samplePoint.z = (float) (Math.sin(anglePhi));
				tmp.add(samplePoint);
			}
		}
		this.writeToPointsVtkFile("sampleOnTrace.vtk", tmp);
	}

	public void writeToPointsVtkFileWithAttribute(String fileName, List<djVtkPoint> ptList, List<Float> attriList) {
		System.out.println("Begin to write file:" + fileName + "...");
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName);
			fw.write("# vtk DataFile Version 3.0\r\n");
			fw.write("vtk output\r\n");
			fw.write("ASCII\r\n");
			fw.write("DATASET POLYDATA\r\n");
			// print points info
			fw.write("POINTS " + ptList.size() + " float\r\n");
			for (int j = 0; j < ptList.size(); j++) {
				fw.write(ptList.get(j).x + " " + ptList.get(j).y + " " + ptList.get(j).z + "\r\n");
			}
			// print VERTICES info
			fw.write("VERTICES " + ptList.size() + " " + ptList.size() * 2 + " \r\n");
			for (int i = 0; i < ptList.size(); i++) {
				fw.write("1 " + i + "\r\n");
			}

			if (attriList.size() == ptList.size()) {
				fw.write("POINT_DATA " + ptList.size() + " \r\n");
				fw.write("SCALARS GridSim float  \r\n");
				fw.write(" LOOKUP_TABLE default  \r\n");
				for (int i = 0; i < attriList.size(); i++) {
					fw.write(attriList.get(i) + "\r\n");
				}
			} // if has point attributes

		} catch (IOException ex) {
			Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("Write file done!");
		System.out.println("That is all!");

	}

	public void writeToPointsVtkFile(String fileName, List<djVtkPoint> ptList) {
		System.out.println("Begin to write file:" + fileName + "...");
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName);
			fw.write("# vtk DataFile Version 3.0\r\n");
			fw.write("vtk output\r\n");
			fw.write("ASCII\r\n");
			fw.write("DATASET POLYDATA\r\n");
			// print points info
			fw.write("POINTS " + ptList.size() + " float\r\n");
			for (int j = 0; j < ptList.size(); j++) {
				fw.write(ptList.get(j).x + " " + ptList.get(j).y + " " + ptList.get(j).z + "\r\n");
			}
			// print VERTICES info
			fw.write("VERTICES " + ptList.size() + " " + ptList.size() * 2 + " \r\n");
			for (int i = 0; i < ptList.size(); i++) {
				fw.write("1 " + i + "\r\n");
			}
		} catch (IOException ex) {
			Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(djVtkData.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("Write file done!");
		System.out.println("That is all!");
	}

	public void mapVolGridToSur_back() throws Exception {
		djVtkSurData currentSurData;
		djNiftiData currentVolData;
		// need to get the max num of grid
		currentVolData = new djNiftiData(DjFindROI.rootDataDir + this.nonSeedSubIDList.get(0) + "_grid_" + this.seedSubID, DjFindROI.rootDataDir
				+ this.nonSeedSubIDList.get(0) + ".b0.mhd");
		float tmpVoxelValue = 0.0f;
		for (int i = 0; i < currentVolData.xSize; i++) {
			for (int j = 0; j < currentVolData.ySize; j++) {
				for (int k = 0; k < currentVolData.zSize; k++) {
					if (currentVolData.getValueBasedOnVolumeCoordinate(i, j, k, 0) >= tmpVoxelValue) {
						tmpVoxelValue = currentVolData.getValueBasedOnVolumeCoordinate(i, j, k, 0);
					}
				}
			}
		}
		this.seedGridNum = (int) tmpVoxelValue;

		for (int subIndex = 0; subIndex < this.nonSeedSubIDList.size(); subIndex++) {
			System.out.println("Now is sub" + subIndex + "--------------------------------------------------------------------------------");
			int subID = this.nonSeedSubIDList.get(subIndex);
			List<djVtkPoint> currentSubGridPoints = new ArrayList<djVtkPoint>();
			currentVolData = new djNiftiData(DjFindROI.rootDataDir + subID + "_grid_" + this.seedSubID, DjFindROI.rootDataDir + subID + ".b0.mhd");
			currentSurData = new djVtkSurData(DjFindROI.rootDataDir + subID + ".surf.vtk");
			float[][] gridVoxelMapping = this.getNewGridVoxelMappingArray();// [gridIndex][x,y,z,num of voxel with same value]
			for (int i = 0; i < currentVolData.xSize; i++) {
				for (int j = 0; j < currentVolData.ySize; j++) {
					for (int k = 0; k < currentVolData.zSize; k++) {
						tmpVoxelValue = currentVolData.getValueBasedOnVolumeCoordinate(i, j, k, 0);
						if (tmpVoxelValue > 0.0f) {
							gridVoxelMapping[(int) tmpVoxelValue][0] += i;
							gridVoxelMapping[(int) tmpVoxelValue][1] += j;
							gridVoxelMapping[(int) tmpVoxelValue][2] += k;
							gridVoxelMapping[(int) tmpVoxelValue][3] += 1;
						}
					}
				}
			} // for iterate the current volume data

			for (int i = 1; i <= this.seedGridNum; i++) {
				int status = 1;
				// get the average of X,Y and Z
				if (Math.abs(gridVoxelMapping[i][3]) < 0.001) {
					System.out.println("the " + i + "th grid is lost!!");
					status = 0;
				}
				for (int j = 0; j < 3; j++) {
					gridVoxelMapping[i][j] = gridVoxelMapping[i][j] / gridVoxelMapping[i][3];
				}
				int[] tmpVolCoord = { (int) gridVoxelMapping[i][0], (int) gridVoxelMapping[i][1], (int) gridVoxelMapping[i][2] };
				float[] tmpPhyCoord = currentVolData.convertFromVolumeToPhysical(tmpVolCoord);

				// find the nearest point on the current surface
				float tmpMinDis = 1000.0f;
				int tmpSelectedPtID = -1;
				djVtkPoint tmpPoint = new djVtkPoint();
				tmpPoint.x = tmpPhyCoord[0];
				tmpPoint.y = tmpPhyCoord[1];
				tmpPoint.z = tmpPhyCoord[2];
				for (int ptIndex = 0; ptIndex < currentSurData.nPointNum; ptIndex++) {
					float tmpDis = djVtkUtil.calDistanceOfPoints(tmpPoint, currentSurData.getPoint(ptIndex));
					if (tmpDis < tmpMinDis) {
						tmpMinDis = tmpDis;
						tmpSelectedPtID = ptIndex;
					}
				}
				// write to file
				if (status == 1) {
					currentSubGridPoints.add(currentSurData.getPoint(tmpSelectedPtID));
				}

				// save to DB
				String sql = "insert into test.t_allmapping (sub_id,pt_id,x,y,z,template_subid,grid_id,status)" + " values ('" + subID + "',"
						+ tmpSelectedPtID + "," + tmpVolCoord[0] + "," + tmpVolCoord[1] + "," + tmpVolCoord[2] + "," + this.seedSubID + "," + i + ","
						+ status + ")";
				DatabaseTool.executeUpdate(sql);
				System.out.println("exc sql: " + sql + "...");
			}
			this.writeToPointsVtkFile(subID + "_mesh_" + this.seedSubID + ".vtk", currentSubGridPoints);

		} // for each non-seed sub
	}
}
