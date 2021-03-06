// Generated by view binder compiler. Do not edit!
package com.raina.PresentSir.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.raina.PresentSir.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityRecognizeFaceBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final AppBarLayout appbar;

  @NonNull
  public final Button capture;

  @NonNull
  public final FloatingActionButton check;

  @NonNull
  public final ImageView image;

  @NonNull
  public final TextView infoText;

  @NonNull
  public final TextView text;

  @NonNull
  public final Toolbar toolbar;

  private ActivityRecognizeFaceBinding(@NonNull ConstraintLayout rootView,
      @NonNull AppBarLayout appbar, @NonNull Button capture, @NonNull FloatingActionButton check,
      @NonNull ImageView image, @NonNull TextView infoText, @NonNull TextView text,
      @NonNull Toolbar toolbar) {
    this.rootView = rootView;
    this.appbar = appbar;
    this.capture = capture;
    this.check = check;
    this.image = image;
    this.infoText = infoText;
    this.text = text;
    this.toolbar = toolbar;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityRecognizeFaceBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityRecognizeFaceBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_recognize_face, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityRecognizeFaceBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.appbar;
      AppBarLayout appbar = ViewBindings.findChildViewById(rootView, id);
      if (appbar == null) {
        break missingId;
      }

      id = R.id.capture;
      Button capture = ViewBindings.findChildViewById(rootView, id);
      if (capture == null) {
        break missingId;
      }

      id = R.id.check;
      FloatingActionButton check = ViewBindings.findChildViewById(rootView, id);
      if (check == null) {
        break missingId;
      }

      id = R.id.image;
      ImageView image = ViewBindings.findChildViewById(rootView, id);
      if (image == null) {
        break missingId;
      }

      id = R.id.infoText;
      TextView infoText = ViewBindings.findChildViewById(rootView, id);
      if (infoText == null) {
        break missingId;
      }

      id = R.id.text;
      TextView text = ViewBindings.findChildViewById(rootView, id);
      if (text == null) {
        break missingId;
      }

      id = R.id.toolbar;
      Toolbar toolbar = ViewBindings.findChildViewById(rootView, id);
      if (toolbar == null) {
        break missingId;
      }

      return new ActivityRecognizeFaceBinding((ConstraintLayout) rootView, appbar, capture, check,
          image, infoText, text, toolbar);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
