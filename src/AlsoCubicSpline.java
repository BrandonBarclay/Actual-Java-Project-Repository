import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AlsoCubicSpline {

    private double parameters[][];
    private double xValues[];
    private double yValues[];
    private final double epsilon;
    private final int X = 0;
    private final int Y = 1;
    private final int A = 0;
    private final int B = 1;
    private final int C = 2;
    private final int D = 3;

    public AlsoCubicSpline(double[] xValues, double[] yValues, double epsilon)  {
        if ((xValues == null) || (xValues.length < 2) || (yValues == null) || (yValues.length != xValues.length))
            throw new IllegalArgumentException("Invalid data passed to constructor");
        this.epsilon = epsilon;
        this.xValues = new double[xValues.length];
        this.yValues = new double[yValues.length];
        this.parameters = new double[4][xValues.length - 1];
        System.arraycopy(xValues, 0, this.xValues, 0, xValues.length);
        System.arraycopy(yValues, 0, this.yValues, 0, yValues.length);

        calculateSplineParameters();
    }

    private void calculateSplineParameters() {
        // calculate h values
        double[] h = new double[xValues.length - 1];
        for (int i = 0; i < h.length; ++i) {
            h[i] = this.xValues[i + 1] - this.xValues[i];
        }
        // end h

        // Calculate r values
        double[] r = new double[this.xValues.length];
        r[0] = 0;
        for (int i = 1; i < r.length - 1; ++i) {
            r[i] =  6
                    * (
                    ((this.yValues[i + 1] - this.yValues[i]) / h[i])
                    - ((this.yValues[i] - this.yValues[i - 1]) / h[i - 1])
                    );
        }
        r[r.length - 1] = 0;
        // end of r values
        thomasAlgorithm(h, r);
    }


    private void thomasAlgorithm(double[] h, double[] r) {
        double[] e = new double[xValues.length]; // e[0] never gets used tho
        double[] f = new double[xValues.length];
        double[] g = new double[xValues.length - 1];
        double[] secondDerivatives = new double[xValues.length];

        f[0] = 1;
        g[0] = -1;
        for (int i = 1; i < f.length - 1; ++i) {
            e[i] = h[i - 1];
            f[i] = 2 * (h[i - 1] + h[i]);
            g[i] = h[i];
        }
        e[e.length - 1] = -1;
        f[f.length - 1] = 1;

        // decomposition step plus forward substitution //
        for (int i = 1; i < f.length; ++i) {
            e[i] /= f[i - 1];
            f[i] -= (e[i] * g[i - 1]);
            r[i] -= (e[i] * r[i - 1]);
        }
        secondDerivatives[secondDerivatives.length - 1] = r[r.length - 1] / f[f.length - 1]; //??????
        for (int i = secondDerivatives.length - 2; i > 0; --i) {
            secondDerivatives[i] = (r[i] - g[i] * secondDerivatives[i + 1]) / f[i] ;
        }
        secondDerivatives[0] = 0;
        calculateCoefficients(secondDerivatives, h);
    }

    private void calculateCoefficients(double[] S, double[] h) {
        for (int i = 0; i < this.parameters[A].length; ++i) {
            this.parameters[A][i] = (S[i + 1] - S[i]) / (6 * h[i]);
            this.parameters[B][i] = S[i] / 2;
            this.parameters[C][i] = ((this.yValues[i + 1] - this.yValues[i]) / h[i])
                                    - ((h[i] * S[i]) / 2)
                                    - ((h[i] * (S[i + 1] + S[i])) / 6) ;
            this.parameters[D][i] = this.yValues[i];
        }
    }

    public double calculateY(double x) {
        if ((x < 0) || (x > 1)) {
            throw new IllegalArgumentException("X value of :" + x + "is out of range");
        }
        int i;
        for (i = 0; i < parameters[A].length - 1; ++i) {
            if ((x > this.xValues[i]) && (x < this.xValues[i + 1])) break;
        }

        System.out.println("i = " + i);
        System.out.println("X = " + x + " Xi = " + this.xValues[i] + " Xi+1 = " + this.xValues[i + 1]);
        double deltaX = x - this.xValues[i];
        return  ( this.parameters[A][i] * Math.pow(deltaX, 3)
                + this.parameters[B][i] * Math.pow(deltaX, 2)
                + this.parameters[C][i] * deltaX
                + this.parameters[D][i]
                );
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
            dataPoint = Double.toString(this.calculateY(x));
            bw.append(dataPoint).append(",").append(Double.toString(x)).append("\n");
            x+= increment;
        }
        bw.close();
        System.out.println("Data was written to the file " + fileName);
    }

    public void alsocalculatesCoefficients() {
        int N= xValues.length - 1;
        double[] a = new double[N + 1];
        for (int i = 0; i < a.length; ++i) {
            a[i] = yValues[i];
        }
        double[] b = new double[N];
        double[] d = new double[N];
        double[] h = new double[N];
        for (int i = 0; i < N; ++i) {
            h[i] = xValues[i + 1] - xValues[i];
        }
        double[] alpha = new double[N];
        for (int i = 1; i < N; ++i) {
            alpha[i] = (3 / h[i]) * (a[i + 1] - a[i]) - (3 / h[i - 1]) * (a[i] - a[i - 1]);
        }
        double[] C = new double[N + 1];
        double[] I = new double[N + 1];
        double[] U = new double[N + 1];
        double[] Z = new double[N + 1];
        I[0] = 1;
        U[0] = Z[0] = 0;
        for (int i = 1; i < N; ++i) {
            I[i] = 2 * (xValues[i + 1] - xValues[i]) - (h[i - 1] * U[i - 1]);
            U[i] = h[i] / I[i];
            Z[i] = (alpha[i] - h[i - 1] * Z[i - 1]) / I[i];
        }
        I[N] = 1;
        Z[N] = C[N] = 0;
        for (int i = N - 2; i >= 0; --i) {
            C[i] = Z[i] - U[i] * C[i + 1];
            b[i] = ((alpha[i + 1] - alpha[i]) / h[i] ) - ((h[i] * (C[i + 1] + 2 * C[i])) / 3);
            d[i] = (C[i + 1] - C[i]) / (3 * h[i]);
        }

        for (int i = 0; i < N; ++i) {
            this.parameters[A][i] = d[i];
            this.parameters[B][i] = C[i];
            this.parameters[2][i] = b[i];
            this.parameters[3][i] = a[i];
        }

    }

    public void printCoefficients() {
        for (int i = 0; i < this.parameters.length; ++i) {
            for (int j = 0; j < this.parameters[i].length; ++j) {
                System.out.print(this.parameters[i][j] + "    ");
            }
        }
    }


}




