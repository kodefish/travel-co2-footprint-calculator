package ch.ethz.smartenergy.ui.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ResourceBundle;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.ui.PlaceholderFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final Context context;

    public SectionsPagerAdapter(Context context, @NonNull FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return PlaceholderFragment.newInstance(
                        R.drawable.icon_sustainability,
                        context.getString(R.string.onboarding_1_title),
                        context.getString(R.string.onboarding_1_subtitle)
                );
            case 1:
                return PlaceholderFragment.newInstance(
                        R.drawable.icon_lightbulb,
                        context.getString(R.string.onboarding_2_title),
                        context.getString(R.string.onboarding_2_subtitle)
                );
            case 2:
            default:
            return PlaceholderFragment.newInstance(
                    R.drawable.icon_polar_bear,
                    context.getString(R.string.onboarding_3_title),
                    context.getString(R.string.onboarding_3_subtitle)
                );
        }
    }


    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}