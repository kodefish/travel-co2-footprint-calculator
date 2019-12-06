package ch.ethz.smartenergy;

import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.rd.PageIndicatorView;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import ch.ethz.smartenergy.ui.adapters.SectionsPagerAdapter;

public class OnboardingActivity extends AppCompatActivity {

    public static final String COMPLETED_ONBOARDING_PREF_NAME = "completed_onboarding_pref_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        MaterialButton skip = findViewById(R.id.onboarding_skip);
        ExtendedFloatingActionButton next = findViewById(R.id.onboarding_next);

        PageIndicatorView pageIndicatorView = findViewById(R.id.onboarding_piv);
        ViewPager viewPager = findViewById(R.id.onboarding_viewpager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {/*empty*/}

            @Override
            public void onPageSelected(int position) {
                pageIndicatorView.setSelection(position);

                if (viewPager.getCurrentItem() == sectionsPagerAdapter.getCount() - 1) {
                    // Set arrow to checkmark
                    next.setIconResource(R.drawable.icon_done);
                } else {
                    // Set checkmark to arrow
                    next.setIconResource(R.drawable.icon_arrow_right);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {/*empty*/}
        });

        skip.setOnClickListener(v -> finishOnboarding());
        next.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < sectionsPagerAdapter.getCount() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            } else {
                finishOnboarding();
            }
        });


    }

    private void finishOnboarding() {
        setResult(RESULT_OK);
        finish();
    }
}