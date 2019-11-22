import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    static ArrayList<Double> linspace(double start, double end, int num) {
        if (num == 1)
            return new ArrayList<>(Arrays.asList(start));

        double current = start;
        double step = (end - start) / (num - 1);

        ArrayList<Double> res = new ArrayList<>();
        for (int i = 0; i < num - 1; i++) {
            res.add(current);
            current += step;
        }
        res.add(end); // to make sure last element has no rounding error
        return res;
    }

    // T = time between two samples = windowSizeInSec / N
    // N = #samples
    static void get_fft_values(ArrayList<Double> y_values, double T, int N) {
        ArrayList<Double> f_values = linspace(0.0, 1.0/(2.0*T), N/2);
        ArrayList<Double> fft_values = InplaceFFT.fft_pos_abs(y_values, 2.0/N);
        System.out.println(f_values);
        System.out.println(fft_values);
    }
    // test client
    public static void main(String[] args) {
        //int n = Integer.parseInt(args[0]);
        int n = 8;
        Complex[] x = new Complex[n];
        Double[] data = {4.0, 12.0, 16.0, 48.0, 99.0, 2.0, 45.0, 59.0};

        boolean printRaw = false;
        if (printRaw) {
            // original data
            for (int i = 0; i < n; i++) {
                x[i] = new Complex(data[i], 0);
                // x[i] = new Complex(-2*Math.random() + 1, 0);
            }
            for (int i = 0; i < n; i++)
                System.out.println(x[i]);
            System.out.println();

            // FFT of original data
            InplaceFFT.fft(x);
            for (int i = 0; i < n; i++)
                System.out.println(x[i]);
            System.out.println();
        }

        ArrayList<Double> ddata = new ArrayList<>(Arrays.asList(data));
        //System.out.println(linspace(11, 47, 23));
        get_fft_values(ddata, 0.01, 8);
    }
}
