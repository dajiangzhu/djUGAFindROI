/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import edu.uga.liulab.djVtkBase.*;
import org.jmat.data.*;
import javax.vecmath.*;

/**
 * 
 * @author dajiang
 */
public class fiberBundleService {

	private djVtkFiberData fiberData;
	private djVtkSurData surData;
	private int pointInterval = 8;// consider 9 points when PCA
	private ArrayList<ArrayList<djVtkPoint>> traceOnSphere;
	private ArrayList<djVtkPoint> pontCloud;
	private djVtkPoint seedPnt;
	// ///////////////////////
	public static final double ANGLE_STEP = Math.PI / 6.0;
	public static final double DENSITY_RADIUS = 0.3;
	public static final int FIBER_PTNUM_THRESHOLD=20;

	public fiberBundleService() {
		this.traceOnSphere = new ArrayList<ArrayList<djVtkPoint>>();
		this.pontCloud = new ArrayList<djVtkPoint>();
	}

	public void setFiberData(djVtkFiberData fiberData) {
		this.fiberData = fiberData;
	}

	public void setPointInterval(int pointInterval) {
		this.pointInterval = pointInterval;
	}

	public void setPontCloud(ArrayList<djVtkPoint> pontCloud) {
		this.pontCloud = pontCloud;
	}

	public void setSeedPnt(djVtkPoint seedPnt) {
		this.seedPnt = seedPnt;
	}

	public void setSurData(djVtkSurData surData) {
		this.surData = surData;
	}

	public djVtkFiberData getFiberData() {
		return fiberData;
	}

	public int getPointInterval() {
		return pointInterval;
	}

	public ArrayList<djVtkPoint> getPontCloud() {
		return pontCloud;
	}

	public djVtkPoint getSeedPnt() {
		return seedPnt;
	}

	public djVtkSurData getSurData() {
		return surData;
	}

	private double getDensityInfo(djVtkPoint pt, List<djVtkPoint> ptList) {
		double count = 0.0;
		int dim = ptList.size();
		djVtkPoint tmpPt;
		for (int i = 0; i < dim; i++) {
			tmpPt = ptList.get(i);
			if (djVtkUtil.calDistanceOfPoints(pt, tmpPt) <= fiberBundleService.DENSITY_RADIUS) {
				count++;
			}
		}
		if (dim != 0)
			return count / dim;
		else
			return 0.0;
	}

	public List<Float> calFeatureOfTrace(List<djVtkPoint> fiberTrace) {
		List<Float> fiberFeature = new ArrayList<Float>();
		List<Float> tmpFiberFeature = new ArrayList<Float>();
		float tmpSum = 0.0f;
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
				float densityOnSamplePt = (float) this.getDensityInfo(samplePoint, fiberTrace);
				tmpSum = tmpSum + densityOnSamplePt;
				tmpFiberFeature.add(densityOnSamplePt);
			}
		}
		return tmpFiberFeature;

//		float denAfterNorm;
//		for (int i = 0; i < tmpFiberFeature.size(); i++) {
//			if (Math.abs(tmpSum) < 0.1) {
//				denAfterNorm = 0.0f;
//			} else {
//				denAfterNorm = tmpFiberFeature.get(i) / tmpSum;
//			}
//			fiberFeature.add(denAfterNorm);
//		}
//		return fiberFeature;
	}

	public float calFeatureDis(List<Float> fiberFeature1, List<Float> fiberFeature2) {
		if (fiberFeature1.size() == fiberFeature2.size()) {
			float dis = 0.0f;
			for (int i = 0; i < fiberFeature1.size(); i++) {
				if (fiberFeature1.get(i) < fiberFeature2.get(i)) {
					dis = dis + fiberFeature1.get(i);
				} else {
					dis = dis + fiberFeature2.get(i);
				}
			}
			return dis;
		} else {
			System.out.println("ERROR!!!!!!!fiberFeature1.size is not equal to fiberFeature2.size!!");
			return -1.0f;
		}
	}
	
	public float calFeatureDis_SD(List<Float> f1, List<Float> f2) {
		float dis = 0.0f;
		if (f1.size() == f2.size()) {
			for (int i = 0; i < f1.size(); i++) {
				dis += Math.pow(Math.abs(f1.get(i) - f2.get(i)), 2);
			}
		} else {
			System.out.println("the size of f1 and f2 are not equal!!");
		}
		return dis;
	}

	public float calFiberBundleDistance(List<djVtkPoint> fb1, List<djVtkPoint> fb2) {
		List<Float> fiberFeature1 = this.calFeatureOfTrace(fb1);
		List<Float> fiberFeature2 = this.calFeatureOfTrace(fb2);
		float tmpSum1 = this.getSumFeature(fiberFeature1);
		float tmpSum2 = this.getSumFeature(fiberFeature2);

		return this.calFeatureDis(fiberFeature1, fiberFeature2);
	}

	private float getSumFeature(List<Float> fiberFeature1) {
		float tmpSum = 0.0f;
		for (int i = 0; i < fiberFeature1.size(); i++) {
			tmpSum = tmpSum + fiberFeature1.get(i);
		}
		return tmpSum;
	}

	public void createFibersTrace() {
		djVtkCell tmpFiber;
		for (int i = 0; i < this.fiberData.nCellNum; i++) {
			// System.out.println("dealing with the " + i + "th fiber..");
			ArrayList<djVtkPoint> tmpPointsList = new ArrayList<djVtkPoint>();
			tmpFiber = this.fiberData.getcell(i);
			if (djVtkUtil.calDistanceOfPoints(seedPnt, tmpFiber.pointsList.get(0)) < djVtkUtil.calDistanceOfPoints(seedPnt,
					tmpFiber.pointsList.get(tmpFiber.pointsList.size() - 1))) {
				// System.out.println("---right order!");
				tmpPointsList.addAll(tmpFiber.pointsList);
			} else {
				// System.out.println("!!!false order!");
				for (int k = tmpFiber.pointsList.size() - 1; k >= 0; k--) {
					tmpPointsList.add(tmpFiber.pointsList.get(k));
				}
			}
			if (tmpFiber.pointsList.size() < fiberBundleService.FIBER_PTNUM_THRESHOLD) {
				traceOnSphere.add(new ArrayList<djVtkPoint>());
			} else {
				ArrayList<djVtkPoint> newTrace = new ArrayList<djVtkPoint>();
				int segNum = tmpFiber.pointsList.size() / this.pointInterval;
				for (int j = this.pointInterval; j < tmpFiber.pointsList.size() - this.pointInterval; j = j + this.pointInterval) {
					djVtkPoint tmpPoint = new djVtkPoint();
					int tmpStart = j - this.pointInterval;
					int tmpEnd = j + this.pointInterval;
					djVtkPoint pt1 = tmpPointsList.get(tmpStart);
					djVtkPoint pt2 = tmpPointsList.get(tmpEnd);
					Vector3d vRefDirection = new Vector3d(pt2.x - pt1.x, pt2.y - pt1.y, pt2.z - pt1.z);
					// tmpPoint.x = pt1.x - pt2.x;
					// tmpPoint.y = pt1.y - pt2.y;
					// tmpPoint.z = pt1.z - pt2.z;// we can abs it.
					// tmpPoint.normalize();//reference direction
					// PCA
					double[][] ptBeforePCA = new double[tmpEnd - tmpStart + 1][3];
					int tmpCount = 0;
					for (int m = tmpStart; m <= tmpEnd; m++) {
						ptBeforePCA[tmpCount][0] = tmpPointsList.get(m).x;
						ptBeforePCA[tmpCount][1] = tmpPointsList.get(m).y;
						ptBeforePCA[tmpCount][2] = tmpPointsList.get(m).z;
						tmpCount++;
					}
					Matrix ptMatrixToPCA = new Matrix(ptBeforePCA);
					PCA pcaHandler = new PCA(ptMatrixToPCA);
					int eigenVecIndex = -1;
					if (pcaHandler.getValues().get(0, 0) > pcaHandler.getValues().get(1, 1)) {
						if (pcaHandler.getValues().get(0, 0) > pcaHandler.getValues().get(2, 2)) {
							eigenVecIndex = 0;
						}
					} else {
						if (pcaHandler.getValues().get(1, 1) > pcaHandler.getValues().get(2, 2)) {
							eigenVecIndex = 1;
						} else {
							eigenVecIndex = 2;
						}
					}
					Vector3d vDirection = new Vector3d(pcaHandler.getVectors().get(0, eigenVecIndex), pcaHandler.getVectors().get(1, eigenVecIndex),
							pcaHandler.getVectors().get(2, eigenVecIndex));
					double angle = vRefDirection.angle(vDirection);
					if (angle > (3.1415927 / 2)) {
						// System.out.println("need to flip the vDirection!!!!");
						vDirection.x = vDirection.x * (-1);
						vDirection.y = vDirection.y * (-1);
						vDirection.z = vDirection.z * (-1);
					}
					// end of PCA
					tmpPoint.x = (float) vDirection.x;
					tmpPoint.y = (float) vDirection.y;
					tmpPoint.z = (float) vDirection.z;

					newTrace.add(tmpPoint);// add PCA result
					pontCloud.add(tmpPoint);
				}
				traceOnSphere.add(newTrace);
			}
		}
	}

	public List<djVtkPoint> getAllPoints() {
		List<djVtkPoint> allPointsList = new ArrayList<djVtkPoint>();
		for (int i = 0; i < this.traceOnSphere.size(); i++) {
			for (int j = 0; j < this.traceOnSphere.get(i).size(); j++) {
				allPointsList.add(this.traceOnSphere.get(i).get(j));
			}
		}
		return allPointsList;
	}

	public void writeToVtkFile(String fileName) {
		System.out.println("begin to write to file:" + fileName);
		int count = 0;
		for (int i = 0; i < this.traceOnSphere.size(); i++) {
			for (int j = 0; j < this.traceOnSphere.get(i).size(); j++) {
				count++;
			}
		}

		System.out.println("Begin to write file:" + fileName + "...");
		FileWriter fw = null;
		try {
			count++;
			fw = new FileWriter(fileName);
			fw.write("# vtk DataFile Version 3.0\r\n");
			fw.write("vtk output\r\n");
			fw.write("ASCII\r\n");
			fw.write("DATASET POLYDATA\r\n");
			// print points info
			fw.write("POINTS " + count + " float\r\n");
			fw.write("0.0 0.0 0.0\r\n");
			for (int i = 0; i < this.traceOnSphere.size(); i++) {
				for (int j = 0; j < this.traceOnSphere.get(i).size(); j++) {
					fw.write(this.traceOnSphere.get(i).get(j).x + " " + this.traceOnSphere.get(i).get(j).y + " " + this.traceOnSphere.get(i).get(j).z
							+ "\r\n");
				}
			}
			// print VERTICES info
			fw.write("VERTICES " + count + " " + count * 2 + " \r\n");
			for (int i = 0; i < count; i++) {
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
}
