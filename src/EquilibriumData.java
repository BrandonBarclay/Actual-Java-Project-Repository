import java.io.*;


public class EquilibriumData {

    private double[][][] data; // index 1 is Venture, index 2 is X(0) or Y(1) data, index 3 is values
    private final int X = 0;
    private final int Y = 1;

    public EquilibriumData(String fileName) {
       this.data = new double[3][2][12]; // because we know the size of the data list otherwise would need to use a list
       try {
           BufferedReader reader = new BufferedReader(new FileReader(fileName));
           String[] elements;
           String line;
           for (int i = 0; i < 3; ++i) {
               for (int j = 0; j < 2; ++j) {
                   line = reader.readLine();
                   elements = line.split(",");
                   for (int k = 0; k < 12; ++k) {
                       data[i][j][k] = Double.parseDouble(elements[k]);
                       }
                   }
               }
        } catch (IOException e) {
            e.printStackTrace();
       }
    }

    public EquilibriumData(EquilibriumData source) {
        int size1 = source.data.length;
        int size2 = source.data[0].length;
        int size3 = source.data[0][0].length;
        this.data = new double[size1][size2][size3];
        for (int i = 0; i < size1; ++i) {
            for (int j = 0; j < size2; ++j) {
                for (int k = 0; k < size3; ++k) {
                    this.data[i][j][k] = source.data[i][j][k];
                }
            }
        }
    }
    public double[][][] getData() {
        int size1 = this.data.length;
        int size2 = this.data[0].length;
        int size3 = this.data[0][0].length;
        double[][][] temp = new double[size1][size2][size3];
        for (int i = 0; i < size1; ++i) {
            for (int j = 0; j < size2; ++j) {
                for (int k = 0; k < size3; ++k) {
                    temp[i][j][k] = this.data[i][j][k];
                }
            }
        }
        return temp;
    }
    public boolean setData(double[][][] source) {
        if (source == null) return false;
        int size1 = source.length;
        int size2 = source[0].length;
        int size3 = source[0][0].length;
        this.data = new double[size1][size2][size3];
        for (int i = 0; i < size1; ++i) {
            for (int j = 0; j < size2; ++j) {
                for (int k = 0; k < size3; ++k) {
                    this.data[i][j][k] = source[i][j][k];
                }
            }
        }
        return true;
    }
    public double[] getXData(int venture) {
        return this.data[venture - 1][X];
    }
    public double[] getYData(int venture) {
        return this.data[venture - 1][Y];
    }


    public EquilibriumData clone() {
        return new EquilibriumData(this);
    }

    public boolean equals(Object source) {
        if ((source == null) || (this.getClass() != source.getClass())) return false;
        if (source == this) return true;
        EquilibriumData that = (EquilibriumData) source;
        if ((this.data.length != that.data.length)
         || (this.data[0].length != that.data[0].length)
         || (this.data[0][0].length != that.data[0][0].length)) return false;
        for (int i = 0; i < this.data.length; ++i) {
            for (int j = 0; j < this.data[0].length; ++j) {
                for (int k = 0; k < this.data[0][0].length; ++k) {
                    if (this.data[i][j][k] != that.data[i][j][k]) return false;
                }
            }
        }
        return true;
    }

}
