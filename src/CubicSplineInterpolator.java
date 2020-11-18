import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CubicSplineInterpolator {
    private final double[][] coefficientMatrix; // 4 x 11 matrix of coefficients of cubic equations
    private final double[][] inverseCoefficientsMatrix; // not final incase user wants to calculate it later?
    private final double epsilon;
    private final double[] xValues;
    private final double[] yValues;

    private final int A = 0;
    private final int B = 1;
    private final int C = 2;
    private final int D = 3;

    public String toString() {

        String string = "The coefficients for each spline section are: \nA: ";
        for (double elem : this.coefficientMatrix[A]) {
            string = string.concat(String.valueOf(elem)).concat(" ");
        }
        string = string.concat("\nB: ");
        for (double elem : this.coefficientMatrix[B]) {
            string = string.concat(String.valueOf(elem)).concat(" ");
        }
        string = string.concat("\nC: ");
        for (double elem : this.coefficientMatrix[C]) {
            string = string.concat(String.valueOf(elem)).concat(" ");
        }
        string = string.concat("\nD: ");
        for (double elem : this.coefficientMatrix[D]) {
            string = string.concat(String.valueOf(elem)).concat(" ");
        }
        return string;
    }

    public CubicSplineInterpolator(double[] xValues, double[] yValues, double epsilon)
                                   throws IllegalArgumentException {
        if ((xValues == null)
        ||  (yValues == null)
        ||  (xValues.length != yValues.length))
        throw new IllegalArgumentException("Invalid array parameters to constructor");

        this.epsilon = epsilon;

        this.xValues = new double[xValues.length];
        System.arraycopy(xValues, 0, this.xValues, 0, xValues.length);

        this.yValues = new double[yValues.length];
        System.arraycopy(yValues, 0, this.yValues, 0, yValues.length);

        this.coefficientMatrix = calculateCubicSplines(xValues, yValues);
        this.inverseCoefficientsMatrix = calculateCubicSplines(yValues, xValues);


    }

    private double[][] calculateCubicSplines(double[] X, double[] Y) {
        double a, b, c, d;
        double[][] coefficientMatrix = new double[4][X.length - 1];
        double firstDerivativeXi = 0;
        double firstDerivativeXiMinusOne = 0;
        double secondDerivativeXi = 0;
        double secondDerivativeXiMinusOne = 0;
        for (int i = 1; i <= coefficientMatrix[A].length; ++i) {

            // calculate the derivatives //
            firstDerivativeXi = calculateFirstDerivativeAtXi(X, Y, firstDerivativeXi, i); // arrays are passed by reference so not expensive
            firstDerivativeXiMinusOne = calculateFirstDerivativeAtXiMinusOne(X, Y, firstDerivativeXi, i);
            secondDerivativeXiMinusOne = calculateSecondDerivativeXiMinusOne(X, Y, firstDerivativeXi, firstDerivativeXiMinusOne, i);
            secondDerivativeXi = calculateSecondDerivativeXi(X, Y, firstDerivativeXi, firstDerivativeXiMinusOne, i);

            // calculate spline coefficients //
            d = (secondDerivativeXi - secondDerivativeXiMinusOne) / (6 * (X[i] - X[i - 1]));

            c = (X[i] * secondDerivativeXiMinusOne - X[i - 1] * secondDerivativeXi)
                / (2 * (X[i] - X[i - 1]));

            b = (
                (Y[i] - Y[i - 1])
                - c * (Math.pow(X[i], 2) - Math.pow(X[i - 1], 2))
                - d * (Math.pow(X[i], 3) - Math.pow(X[i - 1], 3))
                )
                / (X[i] - X[i - 1]);

            a = Y[i - 1] - b * X[i - 1]
                - c * Math.pow(X[i - 1], 2)
                - d * Math.pow(X[i - 1], 3);

            // fill the matrix with the calculated coefficients //
            coefficientMatrix[A][i - 1] = a;
            coefficientMatrix[B][i - 1] = b;
            coefficientMatrix[C][i - 1] = c;
            coefficientMatrix[D][i - 1] = d;
        }
        return coefficientMatrix;
    }

    public double calculateFirstDerivativeAtXi(double[] X, double[] Y, double firstDerivativeX1, int i) {
        if (i == 11) {
            firstDerivativeX1 = 3 * (Y[i] - Y[i - 1]) / (2 * (X[i] - X[i - 1]))
                              - (firstDerivativeX1 / 2);
        } else {
            firstDerivativeX1 = 2 /
                    (
                            ((X[i + 1] - X[i]) / (Y[i + 1] - Y[i]))
                                    + ((X[i] - X[i - 1]) / (Y[i] - Y[i - 1]))
                    );
        }
        return firstDerivativeX1;
    }

    public double calculateFirstDerivativeAtXiMinusOne (double[] X, double[] Y, double firstDerivativeX1, int i) {
        double firstDerivativeX0;
        if (i == 1) {
            firstDerivativeX0 = 3 * (Y[i] - Y[i - 1]) / (2 * (X[i] - X[i - 1]))
                              - (firstDerivativeX1 / 2);
        } else {
            firstDerivativeX0 = 2 /
                    (
                                ((X[i] - X[i - 1]) / (Y[i] - Y[i - 1]))
                                + (
                                  (X[i - 1] - X[i - 2])
                                  / (Y[i - 1] - Y[i - 2])
                                  )
                    );
        }
        return firstDerivativeX0;
    }

    public double calculateSecondDerivativeXiMinusOne(double[] X, double[] Y, double firstDerivativeX1, double firstDerivativeX0, int i) {
        double secondDerivativeX0;
        secondDerivativeX0 = (-2 * (firstDerivativeX1 + 2 * firstDerivativeX0) / (X[i] - X[i - 1]))
                           + (6 * (Y[i] - Y[i - 1]))
                           /  Math.pow(X[i] - X[i - 1], 2);
        return secondDerivativeX0;
    }

    public double calculateSecondDerivativeXi (double[] X, double[] Y, double firstDerivativeX1, double firstDerivativeX0, int i) {
        double secondDerivativeX1;
        secondDerivativeX1 = (2 * (2 * firstDerivativeX1 + firstDerivativeX0) / (X[i] - X[i - 1]))
                           - (6 * (Y[i] - Y[i - 1]))
                           / Math.pow(X[i] - X[i - 1], 2);
        return secondDerivativeX1;
    }

    public double interpolateY(double x) throws IllegalArgumentException {
        if (floatEqual(1, x)) x = 1; // check if there was a small error calculating x which put it out of range
        if (floatEqual(0, x)) x = 0;
        if (x > 1 || x < 0) throw new IllegalArgumentException("X out of range (0,1)");      // if the value is more than epsilon out of range then return garbage

        int i;
        for (i = 0; i < this.coefficientMatrix[A].length; ++i) {
            if  ((floatEqualGreaterThan(x, this.xValues[i]))
              && (floatEqualLessThan(x, this.xValues[i + 1])))
                  break;
        }
        return    this.coefficientMatrix[A][i]
                + this.coefficientMatrix[B][i] * x
                + this.coefficientMatrix[C][i] * Math.pow(x, 2)
                + this.coefficientMatrix[D][i] * Math.pow(x, 3);
    }

    public double interpolateX(double y) throws IllegalArgumentException {
        if (floatEqual(1, y)) y = 1; // check if there was a small error calculating x which put it out of range
        if (floatEqual(0, y)) y = 0;
        if ((y > 1) || (y < 0)) throw new IllegalArgumentException("Y out of range (0,1)");

        int i;
        for (i = 0; i < this.inverseCoefficientsMatrix[A].length; ++i) {
            if  ((floatEqualGreaterThan(y, this.yValues[i]))
              && (floatEqualLessThan(y, this.yValues[i + 1])))
                break;
        }
        return    this.inverseCoefficientsMatrix[A][i]
                + this.inverseCoefficientsMatrix[B][i] * y
                + this.inverseCoefficientsMatrix[C][i] * Math.pow(y, 2)
                + this.inverseCoefficientsMatrix[D][i] * Math.pow(y, 3);
    }

    public static boolean floatEqualLessThan(double comparator, double source) {
        return ((comparator < source) || (Math.abs(source - comparator) < 0.00001));
    }

    public static boolean floatEqualGreaterThan(double comparator, double source) {
        return ((comparator > source) || (Math.abs(source - comparator) < 0.00001));
    }

    public static boolean floatEqual(double source, double comparator) {
        return (Math.abs(source - comparator) < 0.00001);
    }

    public void exportToCSV(double minX, double maxX, String fileName) throws IOException {
        double x = minX;
        double increment = (maxX - minX) / 333;

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
            x += increment;
        }
        bw.close();
        System.out.println("Data was written to the file " + fileName);
    }

    public double qLineIntersection(double[] coefficients, double x0) {
        int i;
        int SLOPE = 0;
        int INTERCEPT = 1;

        double increment = this.epsilon * coefficients[SLOPE] / 100; // to go in the direction of the slope.
        for (i = 0; i < this.coefficientMatrix[A].length; ++i) {
            if  ((floatEqualGreaterThan(x0, this.xValues[i]))
              && (floatEqualLessThan(x0, this.xValues[i + 1])))
                break;
        }
        double x = x0 + increment;
        double y;
        do {
            x += increment;
            if ((i == 11) || (i == -1)) return -9999;
            if (floatEqualLessThan(x, this.xValues[i])) --i;
            if (floatEqualGreaterThan(x, this.xValues[i + 1])) ++i;

            y = this.interpolateY(x);
            y -= coefficients[SLOPE] * x + coefficients[INTERCEPT];
        } while (!floatEqual(0, y));
        return x;
    }

    public double QLInt(LinearFunction linearFunction, double x0) {
        int i;
        double increment = this.epsilon * linearFunction.getSlope() / 100;
        for (i = 0; i < this.coefficientMatrix[A].length; ++i) {
            if  ((floatEqualGreaterThan(x0, this.xValues[i]))
                    && (floatEqualLessThan(x0, this.xValues[i + 1])))
                break;
        }
        double currentX = x0 + increment;
        double y;
        do {
            currentX += increment;
            if ((i == 11) || (i == -1)) return -9999;
            if (floatEqualLessThan(currentX, this.xValues[i])) --i;
            if (floatEqualGreaterThan(currentX, this.xValues[i + 1])) ++i;

            y = this.interpolateY(currentX);
            y -= linearFunction.interpolateY(currentX);
        } while (!floatEqual(y, 0));
        return currentX;
    }

    public double ELInt(LinearFunction linearFunction, double x0) {
        int i;
        double increment = this.epsilon * linearFunction.getSlope() ;
        for (i = 0; i < this.coefficientMatrix[A].length; ++i) {
            if  ((floatEqualGreaterThan(x0, this.xValues[i]))
                    && (floatEqualLessThan(x0, this.xValues[i + 1])))
                break;
        }
        double currentX = x0 + increment;
        double y;
        do {
            currentX -= increment;
            if ((i == 11) || (i == -1)) return -9999;
            if (floatEqualLessThan(currentX, this.xValues[i])) --i;
            if (floatEqualGreaterThan(currentX, this.xValues[i + 1])) ++i;

            double y1 = this.interpolateY(currentX);
            double y2 = linearFunction.interpolateY(currentX);
            y = y2 - y1;
        } while (!floatEqual(y, 0));
        return currentX;
    }

    public double enrichingLineIntersection(double[] coefficients, double x0) {
        int i;
        int SLOPE = 0;
        int INTERCEPT = 1;

        double increment = this.epsilon * coefficients[SLOPE] / 100; // to go in the direction of the slope.
        for (i = 0; i < this.coefficientMatrix[A].length; ++i) {
            if  ((floatEqualGreaterThan(x0, this.xValues[i]))
                    && (floatEqualLessThan(x0, this.xValues[i + 1])))
                break;
        }
        double x = x0 + increment;
        double y;
        do {
            x -= increment;
            if ((i == 11) || (i == -1)) return -9999;
            if (floatEqualLessThan(x, this.xValues[i])) --i;
            if (floatEqualGreaterThan(x, this.xValues[i + 1])) ++i;

            y = this.interpolateY(x);
            y -= coefficients[SLOPE] * x + coefficients[INTERCEPT];
        } while (!floatEqual(0, y));
        return x;
    }

}





