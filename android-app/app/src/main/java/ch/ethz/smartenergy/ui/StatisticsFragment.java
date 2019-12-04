package ch.ethz.smartenergy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.ui.adapters.PagerAdapter;

public class StatisticsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);

        ViewPager viewPager = root.findViewById(R.id.statistics_viewpager);
        FragmentPagerAdapter pagerAdapter = new PagerAdapter(getFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        return root;
    }
}