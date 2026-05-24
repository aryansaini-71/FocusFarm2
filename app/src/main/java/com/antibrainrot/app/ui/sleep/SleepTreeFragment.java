package com.antibrainrot.app.ui.sleep;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antibrainrot.app.R;
import com.antibrainrot.app.data.GameDataManager;

/**
 * Sleep Tree tab — tree health reflects remaining plants.
 */
public class SleepTreeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sleep_tree, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root == null || getContext() == null) {
            return;
        }
        TextView label = root.findViewById(R.id.tv_sleep_tree_status);
        if (label != null) {
            int plants = GameDataManager.getPlantsAlive(requireContext());
            label.setText(getString(R.string.sleep_tree_plants_format, plants));
        }
    }
}
