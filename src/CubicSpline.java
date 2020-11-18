import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CubicSpline {
    private final double[][] parameters;
    private final double[][] splineEndPoints;
    private final double epsilon;
    // Constants //
    private final int X = 0;
    private final int Y = 1;
    private final int A = 0;
    private final int B = 1;
    private final int C = 2;
    private final int D = 3;

    public void printSpline() {
        System.out.print("The values of ai are: ");
        for (double elem :
                this.parameters[A]) {
            System.out.print(elem + "   ");
        }
    }

    public CubicSpline(double[] xValues, double[] yValues, double tolerance) {
        this.epsilon = tolerance;
        this.parameters = new double[4][xValues.length - 1];
        this.splineEndPoints = new double[2][xValues.length];
        for (int i = 0; i < xValues.length; ++i) {
            this.splineEndPoints[X][i] = xValues[i];
            this.splineEndPoints[Y][i] = yValues[i];
        }
        calculateCubicSpline(xValues, yValues);
    }

    private void calculateCubicSpline(double[] xValues, double[] yValues) { // N + 1 data points
        int N = xValues.length - 1; // = 11
        double[] h = new double[N];
        double[] f = new double[N];
        double[] g = new double[N - 1];
        double[] e = new double[N];
        double[] secondDerivatives = new double[N + 1];
        double[] r = new double[N];
        int maxPos = N - 1;

        for (int i = 0; i < N; ++i) {
            h[i] = xValues[i + 1] - xValues[i];
        }
        f[0] = f[maxPos] = 1;
        for (int i = 1; i < maxPos; ++i) {
            f[i] = 2 * (h[i - 1] + h[i]);
        }
        g[0] = 0;
        for (int i = 1; i < maxPos; ++i) {
            g[i] = h[i];
        }
        e[maxPos] = 0;
        for (int i = 1; i < maxPos; ++i) {
            e[i] = h[i - 1];
        }
        r[0] = r[maxPos] = 0;
        for (int i = 1; i < maxPos; ++i) {
            r[i] = 6 * (((yValues[i + 1] - yValues[i]) / h[i]) - ((yValues[i] - yValues[i - 1]) / h[i - 1]));
        }

        // THOMAS ALGORITHM //

        // Decomposition //
        for (int i = 1; i <= maxPos; ++i) {
            e[i] = e[i] / f[i - 1];
            f[i] = f[i] - e[i] * g[i - 1];
        }

        // Forward Substitution //
        for (int i = 1; i <= maxPos; ++i) {
            r[i] = r[i] - e[i] * r[i - 1];
        }

        // Back Substitution //
        secondDerivatives[maxPos] = r[maxPos] / f[maxPos];
        for (int i = maxPos; i > 0; --i) {
            secondDerivatives[i] = (r[i] - g[i - 1] * secondDerivatives[i + 1]) / f[i];
        }

        // End of Thomas Algorithm //
        for (int i = 0; i < maxPos; ++i) {
            this.parameters[A][i] = (secondDerivatives[i + 1] - secondDerivatives[i]) / (6 * h[i]);
            this.parameters[B][i] = secondDerivatives[i] / 2;
            this.parameters[C][i] = ((yValues[i + 1] - yValues[i]) / h[i]) - (h[i] * secondDerivatives[i] / 2)
                    - h[i] * ((secondDerivatives[i + 1] + secondDerivatives[i]) / 6);
            this.parameters[D][i] = yValues[i];
        }
        return;
    }

    public double interpolateY(double x) {
        if (equalsDoubles(1, x)) x = 1; // check if there was a small error calculating x which put it out of range
        if (equalsDoubles(0, x)) x = 0;
        if (x > 1 || x < 0) return -9999; // if the value is more than epsilon out of range then return garbage
        int i;
        for (i = 0; i < splineEndPoints[X].length; ++i) {
            if ((x >= splineEndPoints[X][i]) && (x <= splineEndPoints[X][i + 1])) break;
        }
        System.out.println("i = " + i);
        return this.parameters[A][i] * Math.pow(x - splineEndPoints[X][i], 3)
                + this.parameters[B][i] * Math.pow(x - splineEndPoints[X][i], 2)
                + this.parameters[C][i] * (x - splineEndPoints[X][i])
                + this.parameters[D][i];
    }

    public boolean equalsLessThanDoubles(double source, double comparator) {
        return ((comparator < source) || (Math.abs(source - comparator) < this.epsilon));
    }

    /* returns */
    public boolean equalsGreaterThanDoubles(double source, double comparator) {
        return ((comparator > source) || (Math.abs(source - comparator) < this.epsilon));
    }

    public boolean equalsDoubles(double source, double comparator) {
        return (Math.abs(source - comparator) < this.epsilon);
    }

    public void exportToCSV(double minX, double maxX, String fileName) throws IOException {
        double x = minX;
        double increment = (maxX - minX) / 1000 ;

        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(writer);

        String dataPoint;
        while (x < maxX) {
            dataPoint = Double.toString(this.interpolateY(x));
            bw.append(dataPoint).append(",").append(Double.toString(x)).append("\n");
            x+= increment;
        }
        bw.close();
        System.out.println("Data was written to the file " + fileName);
    }
}


