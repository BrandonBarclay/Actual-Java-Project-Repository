import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {

        String equilibriumDataFileName = "EquilibriumDataFile";
        String dataExportCSVFileName = "EquilibriumCurve.csv";
        String rawMaterialsDataFile = "RawMaterialPhysicalProperties";
        EquilibriumData data = new EquilibriumData(equilibriumDataFileName);
        int ventureNumber = 1;
//        CubicSplineInterpolator spline = new CubicSplineInterpolator(data.getXData(ventureNumber),
//                                                                     data.getYData(ventureNumber),
//                                                              0.0001);
        //spline.exportToCSV(0, 1, dataExportCSVFileName);
        //DistillationColumn columnTest = new DistillationColumn(337, data, 1, 0.00001);

        newDistColumn newTest = new newDistColumn(337, data, 1, 0.00001);

    }
}

