package com.asfoundation.wallet.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.widget.OnImportPrivateKeyListener;

public class ImportPrivateKeyFragment extends Fragment implements View.OnClickListener {

  private EditText privateKey;
  private OnImportPrivateKeyListener onImportPrivateKeyListener;

  public static ImportPrivateKeyFragment create() {
    return new ImportPrivateKeyFragment();
  }

  @Override public void onAttach(Context context) {
    if (!(context instanceof OnImportPrivateKeyListener)) {
      throw new IllegalArgumentException("this fragment should be attached to an "
          + OnImportPrivateKeyListener.class.getSimpleName()
          + " instance");
    }
    onImportPrivateKeyListener = ((OnImportPrivateKeyListener) context);
    super.onAttach(context);
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return LayoutInflater.from(getContext())
        .inflate(R.layout.fragment_import_private_key, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    privateKey = view.findViewById(R.id.private_key);
    view.findViewById(R.id.import_action)
        .setOnClickListener(this);
  }

  @Override public void onClick(View view) {
    privateKey.setError(null);
    String value = privateKey.getText()
        .toString();
    if (TextUtils.isEmpty(value) || value.length() != 64) {
      privateKey.setError(getString(R.string.error_field_required));
    } else {
      onImportPrivateKeyListener.onPrivateKey(privateKey.getText()
          .toString());
    }
  }

}
