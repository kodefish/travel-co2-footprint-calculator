import java.util.ArrayList;

public class InplaceFFT {


    // return array with absolute values of positive frequencies after the FFT given array `a`
    public static ArrayList<Double> fft_pos_abs(ArrayList<Double> a, double factor) {
        Complex[] x = new Complex[a.size()];
        for (int i = 0; i < a.size(); i++) {
            x[i] = new Complex(a.get(i), 0);
        }
        fft(x);

        ArrayList<Double> res = new ArrayList<>();
        for (int i = 0; i < x.length / 2; i++) {
            res.add(factor * x[i].abs());
        }
        return res;
    }

    // compute the FFT of x[], assuming its length is a power of 2
    public static void fft(Complex[] x) {

        // check that length is a power of 2
        int n = x.length;
        if (Integer.highestOneBit(n) != n) {
            throw new RuntimeException("n is not a power of 2");
        }

        // bit reversal permutation
        int shift = 1 + Integer.numberOfLeadingZeros(n);
        for (int k = 0; k < n; k++) {
            int j = Integer.reverse(k) >>> shift;
            if (j > k) {
                Complex temp = x[j];
                x[j] = x[k];
                x[k] = temp;
            }
        }

        // butterfly updates
        for (int L = 2; L <= n; L = L+L) {
            for (int k = 0; k < L/2; k++) {
                double kth = -2 * k * Math.PI / L;
                Complex w = new Complex(Math.cos(kth), Math.sin(kth));
                for (int j = 0; j < n/L; j++) {
                    Complex tao = w.times(x[j*L + k + L/2]);
                    x[j*L + k + L/2] = x[j*L + k].minus(tao); 
                    x[j*L + k]       = x[j*L + k].plus(tao); 
                }
            }
        }
    }
}
