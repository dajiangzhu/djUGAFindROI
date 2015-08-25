

public class DDN {

    public int[][] adjacentMatrix = null;
    public double[][] pvalueMatrix = null;
    public double threshhold = 0;
    public double pvaluecutoff = 0;
    public int numPerm = 0;
    public String[] geneList = null;
    public double[][] data1 = null;
    public double[][] data2 = null;
    public String[][] imposedNet = null;
    public boolean isImposed = false;

    public DDN(double t, double p, int n, double[][] d1, double[][] d2, String[] g) {

        threshhold = t;
        pvaluecutoff = p;
        numPerm = n;
        geneList = g;
        data1 = d1;
        data2 = d2;
        adjacentMatrix = new int[geneList.length][geneList.length];
        pvalueMatrix = new double[geneList.length][geneList.length];

        for (int i=0; i<adjacentMatrix.length; i++) {
            for (int j=i; j<adjacentMatrix.length; j++) {
                adjacentMatrix[i][j] = 0;
                pvalueMatrix[i][j] = 1;
                adjacentMatrix[j][i] = 0;
                pvalueMatrix[j][i] = 1;
            }
        }

        for (int i=0; i<geneList.length-1; i++) {
            for (int j=i+1; j<geneList.length; j++) {
                double[] connAttribute = testChange(data1[i], data1[j], data2[i], data2[j]);
                if (connAttribute[2]>0) {
                    adjacentMatrix[i][j] = (int)connAttribute[0];
                    pvalueMatrix[i][j] = connAttribute[1];
                    adjacentMatrix[j][i] = (int)connAttribute[0];
                    pvalueMatrix[j][i] = connAttribute[1];
                }
            }
        }

        mergeSameNode();
    }

    public DDN(double t, double p, int n, double[][] d1, double[][] d2, String[] g, String[][] nt) {

        threshhold = t;
        pvaluecutoff = p;
        numPerm = n;
        geneList = g;
        imposedNet = nt;
        isImposed = true;
        data1 = d1;
        data2 = d2;
        adjacentMatrix = new int[geneList.length][geneList.length];
        pvalueMatrix = new double[geneList.length][geneList.length];

        for (int i=0; i<adjacentMatrix.length; i++) {
            for (int j=i; j<adjacentMatrix.length; j++) {
                adjacentMatrix[i][j] = 0;
                pvalueMatrix[i][j] = 1;
                adjacentMatrix[j][i] = 0;
                pvalueMatrix[j][i] = 1;
            }
        }
        
        for (int v=0; v<imposedNet.length; v++)
            for (int i=0; i<geneList.length-1; i++)
                for (int j=i+1; j<geneList.length; j++) {
                    if ((geneList[i].equals(imposedNet[v][0]) && geneList[j].equals(imposedNet[v][1])) || (geneList[i].equals(imposedNet[v][1]) && geneList[j].equals(imposedNet[v][0]))) {
                        double[] connAttribute = testChange(data1[i], data1[j], data2[i], data2[j]);
                        if (connAttribute[2]>0) {
                            adjacentMatrix[i][j] = (int)connAttribute[0];
                            pvalueMatrix[i][j] = connAttribute[1];
                            adjacentMatrix[j][i] = (int)connAttribute[0];
                            pvalueMatrix[j][i] = connAttribute[1];
                        }
                    }
                }
        
        mergeSameNode();

        for (int v=0; v<imposedNet.length; v++)
            for (int i=0; i<geneList.length-1; i++)
                for (int j=i+1; j<geneList.length; j++)
                    if ((geneList[i].equals(imposedNet[v][0]) && geneList[j].equals(imposedNet[v][1])) || (geneList[i].equals(imposedNet[v][1]) && geneList[j].equals(imposedNet[v][0])))
                        {
                            if (adjacentMatrix[i][j] == 0 || pvalueMatrix[i][j] > pvaluecutoff)
                                {
                                    adjacentMatrix[i][j] = 3;
                                    adjacentMatrix[j][i] = 3;
                                    pvalueMatrix[i][j] = 0;
                                    pvalueMatrix[j][i] = 0;
                                    break;
                                }
                        }

    }

    public void mergeSameNode() {
        int geneNum = geneList.length;
        int[] isUnique = new int[geneList.length];
        for (int i=0; i<geneList.length-1; i++) {
            for (int j=i+1; j<geneList.length; j++) {
                if (geneList[i].equals(geneList[j]) && isUnique[i]==0) {
                    isUnique[j] = 1;
                    geneNum--;
                }
            }
        }

        String[] unqGene = new String[geneNum];
        int[][] adjM = new int[geneNum][geneNum];
        double[][] pM = new double[geneNum][geneNum];
        for (int i=0; i<geneNum; i++)
            for(int j=i; j<geneNum; j++) {
                pM[i][j] = 1;
                pM[j][i] = 1;
            }
        int id = 0;

        for (int i=0; i<geneList.length; i++) {
            if (isUnique[i] == 0) {
                unqGene[id] = geneList[i];
                id++;
            }
        }

        double p = 1;
        int c = 0;
        int conflict = 0;
        for (int i=0; i<geneNum-1; i++) {
            for(int j=i+1; j<geneNum; j++) {
                searchMatch:
                for(int m=0; m<geneList.length; m++) {
                    for (int n=0; n<geneList.length; n++) {
                        if (unqGene[i].equals(geneList[m]) && unqGene[j].equals(geneList[n])) {
                            if (conflict == 0 && adjacentMatrix[m][n] > 0) {
                                if (c == 0) {
                                    c = adjacentMatrix[m][n];
                                    if (pvalueMatrix[m][n] < p)
                                        p = pvalueMatrix[m][n];
                                }
                                else {
                                    if (adjacentMatrix[m][n] != c) {
                                        if (p < pvaluecutoff && pvalueMatrix[m][n] < pvaluecutoff) {
                                            c = 0;
                                            p = 1;
                                            conflict = 1;
                                            break searchMatch;
                                        } else {
                                            if (pvalueMatrix[m][n] < p) {
                                                p = pvalueMatrix[m][n];
                                                c = adjacentMatrix[m][n];
                                            }
                                        }

                                    }
                                    else {
                                        if (pvalueMatrix[m][n] < p)
                                            p = pvalueMatrix[m][n];
                                    }
                                }
                            }
                        }
                    }
                }
                pM[i][j] = p;
                pM[j][i] = p;
                p = 1;
                adjM[i][j] = c;
                adjM[j][i] = c;
                c = 0;
                conflict = 0;
            }
        }
        pvalueMatrix = pM;
        adjacentMatrix = adjM;
        geneList = unqGene;
    }

    public double[] testChange(double[] x1, double[] x2, double[] y1, double[] y2) {
        double sqcor1 = corr(x1,x2)*corr(x1,x2);
        double sqcor2 = corr(y1,y2)*corr(y1,y2);
        double[] attribute = new double[3];
        double theta = Math.abs(sqcor1 - sqcor2);

        double[] permx1 = new double[x1.length];
        double[] permx2 = new double[x2.length];
        double[] permy1 = new double[y1.length];
        double[] permy2 = new double[y2.length];
        double[] merge1 = new double[x1.length+y1.length];
        double[] merge2 = new double[x1.length+y1.length];

        double psqcor1 = 0;
        double psqcor2 = 0;
        double ptheta = 0;
        double B = 0;
        int[] permSeq = new int[x1.length+y1.length];
        int flag = 0;
        int label = 0;

        for (int i=0; i<x1.length; i++) {
            merge1[i] = x1[i];
            merge2[i] = x2[i];
        }
        for (int i=0; i<y1.length; i++) {
            merge1[i+x1.length] = y1[i];
            merge2[i+x1.length] = y2[i];
        }

        if (sqcor1 > threshhold && sqcor2 < threshhold) {
            flag = 1;
            label = 1;
        } else if (sqcor1 < threshhold && sqcor2 > threshhold) {
            flag = 1;
            label = 2;
        }
        if (flag == 1) {
            flag = 0;
            for (int p=0; p<numPerm; p++) {
                permSeq = permutation(x1.length + y1.length);
                for (int i=0; i<x1.length; i++)  {
                    permx1[i] = merge1[permSeq[i]];
                    permx2[i] = merge2[permSeq[i]];
                }
                for (int i=0; i<y1.length; i++) {
                    permy1[i] = merge1[permSeq[i+x1.length]];
                    permy2[i] = merge2[permSeq[i+x1.length]];
                }

                psqcor1 = corr(permx1,permx2)*corr(permx1,permx2);
                psqcor2 = corr(permy1,permy2)*corr(permy1,permy2);
                ptheta = Math.abs(psqcor1 - psqcor2);
                if (ptheta > theta)
                    B++;
            }
            attribute[2] = 1;
            attribute[1] = B / numPerm;
            if (label == 1)
                attribute[0] = 1;
            else
                attribute [0] = 2;
        }
        return attribute;
    }

    public int[] permutation(int N) {
        int[] a = new int[N];

        for (int i = 0; i < N; i++)
            a[i] = i;

        for (int i = 0; i < N; i++) {
            int r = (int) (Math.random() * (i+1));
            int swap = a[r];
            a[r] = a[i];
            a[i] = swap;
        }

        return a;
    }

    /**
     * Calculates the pearson correlation of <code>array x</code> 
     * and <code>array y</code>.
     * @param x     an array
     * @param y     an array
     * @return      the pearson correlation of <code>x, y</code>
     */
    public double corr(double[] x, double y[]) {

        if (x.length != y.length) {
            return -2;
        }

        double correlation = 0;
        double meanX = mean(x);
        double meanY = mean(y);
        for (int i = 0; i < x.length; i ++) {
            correlation += ((x[i] - meanX) * (y[i] - meanY));
        }

        if (std(x) != 0 && std(y) != 0 && x.length > 1) {
            correlation = correlation / ((x.length - 1) * std(x) * std(y));
        } else {
            return -3;
        }
        return correlation;

    }

    /**
     * Calculates the mean of <code>array x</code>.
     * @param x     an array
     * @return      the estimated mean of <code>x</code>
     */
    public double mean(double[] x) {

        double sum = 0;
        for (int i = 0; i < x.length; i ++) {
            sum += x[i];
        }
        return (sum / x.length);
       
    }

    /**
     * Calculates the standard deviation of <code>array x</code>.
     * @param x     an array
     * @return      the estimated standard deviation of <code>x</code>
     */
    public double std(double[] x) {

        double sum = 0;
        double meanX = mean(x);
        for (int i = 0; i < x.length; i ++) {
            sum += (x[i] - meanX) * (x[i] - meanX);
        }
        return Math.sqrt(sum / (x.length - 1));

    }

}
