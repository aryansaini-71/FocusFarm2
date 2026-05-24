package com.antibrainrot.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Farm tab — will eventually show the isometric farm grid and points.
 * For now this is a placeholder layout your team can build on.
 */
public class FarmFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // Inflate fragment_farm.xml into the fragment_container
    return inflater.inflate(R.layout.fragment_farm, container, false);
  }
}
