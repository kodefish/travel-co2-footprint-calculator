package ch.ethz.smartenergy.ui.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import ch.ethz.smartenergy.ui.CarbonConsumptionStatsFragment;
import ch.ethz.smartenergy.ui.TransportationModeStatsFragment;

public class PagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 2;

    public PagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return TransportationModeStatsFragment.newInstance();
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return CarbonConsumptionStatsFragment.newInstance();
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return "Transportation Modes";
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return "CO2 Consumption";
            default:
                return null;
        }
    }

}
