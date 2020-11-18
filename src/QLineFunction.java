public class QLineFunction extends LinearFunction {

    public QLineFunction(double q, double x0) {
        if (q == 1) q = 0.9999;
        double intercept = -x0 / (q - 1);
        this.slope = q / (q - 1);
        this.intercept = intercept;
    }

    public QLineFunction(QLineFunction that) {
        this.intercept = that.intercept;
        this.slope = that.slope;
    }

    @Override
    public QLineFunction clone() {
        return new QLineFunction(this);
    }
}
