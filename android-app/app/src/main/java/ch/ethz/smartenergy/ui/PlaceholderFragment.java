package ch.ethz.smartenergy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;

import ch.ethz.smartenergy.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_DRAWABLE_RESOURCE = "drawable_resource";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_SUBTITLE = "arg_subtitle";

    // UI Elements
    private int lottieRaw;
    private String title;
    private String subtitle;

    public static PlaceholderFragment newInstance(int drawableResource, String title, String subtitle) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_DRAWABLE_RESOURCE, drawableResource);
        bundle.putString(ARG_TITLE, title);
        bundle.putString(ARG_SUBTITLE, subtitle);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lottieRaw = getArguments().getInt(ARG_DRAWABLE_RESOURCE);
        title = getArguments().getString(ARG_TITLE);
        subtitle = getArguments().getString(ARG_SUBTITLE);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_onboarding, container, false);

        LottieAnimationView imageView =  root.findViewById(R.id.onboarding_fragment_image);
        TextView titleTv = root.findViewById(R.id.onboarding_fragment_title);
        TextView subtitleTv = root.findViewById(R.id.onboarding_fragment_subtitle);

        imageView.setAnimation(lottieRaw);
        titleTv.setText(title);
        subtitleTv.setText(subtitle);
        return root;
    }
}