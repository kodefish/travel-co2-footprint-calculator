import jdk.internal.util.xml.impl.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    static DDL get_fft_values(ArrayList<Double> y_values, double T, int N) {
        ArrayList<Double> f_values = linspace(0.0, 1.0/(2.0*T), N/2);
        ArrayList<Double> fft_values = InplaceFFT.fft_pos_abs(y_values, 2.0/N);

        return new DDL(f_values, fft_values);
    }

    // percentile using linear interpolation
    public static double get_percentile(ArrayList<Double> numbers, double percentile) {
        percentile /= 100;
        //Collections.sort(numbers); // assume data is already sorted!
        double index = percentile * (numbers.size() - 1);
        int lower = (int)Math.floor(index);
        if (lower >= numbers.size()-1) // the 100%p case
            return numbers.get(numbers.size()-1);

        double fraction = index-lower;
        return numbers.get(lower) + fraction*(numbers.get(lower+1)-numbers.get(lower));
    }

    // mpd = 1, threshold = 0, edge='rising', kpsh=false, valley=false, show = false, ax=none
    static ArrayList<Integer> detect_peaks(ArrayList<Double> x, double mph) {
        if (x.size() < 3)
            return new ArrayList<Integer>();

        double[] dx1 = new double[x.size()];
        dx1[dx1.length-1] = 0;

        double[] dx2 = new double[x.size()];
        dx2[0] = 0;

        for (int i = 0; i < x.size() - 1; i++) {
            dx1[i] = x.get(i+1) - x.get(i);
            dx2[i+1] = dx1[i];
        }

        ArrayList<Integer> ind = new ArrayList<>();

        // find peaks
        for (int i = 0; i < x.size(); i++) {
            boolean isPeak = dx1[i] <= 0 && dx2[i] > 0;
            boolean isNotBoundary = i != 0 && i != x.size() - 1;
            boolean isMPH = x.get(i) >= mph;
            System.out.println("[" + isPeak + ", " + isNotBoundary + ", " + isMPH);
            if (isPeak && isNotBoundary && isMPH) // peak && notEdge
                ind.add(i);
        }

        return ind;
    }

    public void run() {
        //int n = Integer.parseInt(args[0]);
        Double[] data = {4.0, 12.0, 16.0, 48.0, 99.0, 2.0, 45.0, 59.0,
                4.0, 12.0, 16.0, 48.0, 99.0, 2.0, 45.0, 59.0,
                4.0, 12.0, 16.0, 48.0, 99.0, 2.0, 45.0, 59.0,
                4.0, 12.0, 16.0, 48.0, 99.0, 2.0, 45.0, 59.0};
        int n = data.length;
        Complex[] x = new Complex[n];

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

        // do FFT and get positive frequencies as absolute values
        ArrayList<Double> ddata = new ArrayList<>(Arrays.asList(data));
        //System.out.println(linspace(11, 47, 23));
        DDL vals = get_fft_values(ddata, 0.01, ddata.size());
        System.out.println(vals.a);
        System.out.println(vals.b);

        // detect peaks
        ArrayList<Double> sortedVals = new ArrayList<>(vals.b);
        Collections.sort(sortedVals);
        double percentile = 5;
        double denominator = 10;
        double signal_min = get_percentile(sortedVals, percentile);
        double signal_max = get_percentile(sortedVals, 100-percentile);
        double mph = signal_min + (signal_max - signal_min)/denominator;

        System.out.println("Signal.min = " + signal_min + ", max = " + signal_max);

        ArrayList<Integer> peaks = detect_peaks(vals.b, mph);
        System.out.println(peaks);

        // get first n peaks padded with 0's as features
        int no_peaks = 5;
        ArrayList<Double> peaks_x_y = new ArrayList<>(Collections.nCopies(no_peaks * 2, 0.0));
        for (int i = 0; i < no_peaks; i++) {
            if (i < peaks.size()) {
                peaks_x_y.set(i, vals.a.get(peaks.get(i)));
                peaks_x_y.set(no_peaks + i, vals.b.get(peaks.get(i)));
            }
        }

        System.out.println("FEATURES:");
        System.out.println(peaks_x_y);

    }
    // test client
    public static void main(String[] args) {
        Main m = new Main();
        m.run();
    }
}