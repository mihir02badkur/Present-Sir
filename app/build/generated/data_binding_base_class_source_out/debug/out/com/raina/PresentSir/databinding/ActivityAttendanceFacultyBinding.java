// Generated by view binder compiler. Do not edit!
package com.raina.PresentSir.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.appbar.AppBarLayout;
import com.raina.PresentSir.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityAttendanceFacultyBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final AppBarLayout appbar;

  @NonNull
  public final TextView month;

  @NonNull
  public final ImageView nextMonth;

  @NonNull
  public final ImageView prevMonth;

  @NonNull
  public final TextView subject1;

  @NonNull
  public final TextView subject2;

  @NonNull
  public final TextView subject3;

  @NonNull
  public final TextView subject4;

  @NonNull
  public final TextView subject5;

  @NonNull
  public final TextView subject6;

  @NonNull
  public final ImageView subjectName;

  @NonNull
  public final ImageView subjectName1;

  @NonNull
  public final ImageView subjectName2;

  @NonNull
  public final ImageView subjectName3;

  @NonNull
  public final ImageView subjectName4;

  @NonNull
  public final ImageView subjectName5;

  @NonNull
  public final TextSwitcher textSwitch;

  @NonNull
  public final Toolbar toolbar;

  @NonNull
  public final TextView year;

  private ActivityAttendanceFacultyBinding(@NonNull LinearLayout rootView,
      @NonNull AppBarLayout appbar, @NonNull TextView month, @NonNull ImageView nextMonth,
      @NonNull ImageView prevMonth, @NonNull TextView subject1, @NonNull TextView subject2,
      @NonNull TextView subject3, @NonNull TextView subject4, @NonNull TextView subject5,
      @NonNull TextView subject6, @NonNull ImageView subjectName, @NonNull ImageView subjectName1,
      @NonNull ImageView subjectName2, @NonNull ImageView subjectName3,
      @NonNull ImageView subjectName4, @NonNull ImageView subjectName5,
      @NonNull TextSwitcher textSwitch, @NonNull Toolbar toolbar, @NonNull TextView year) {
    this.rootView = rootView;
    this.appbar = appbar;
    this.month = month;
    this.nextMonth = nextMonth;
    this.prevMonth = prevMonth;
    this.subject1 = subject1;
    this.subject2 = subject2;
    this.subject3 = subject3;
    this.subject4 = subject4;
    this.subject5 = subject5;
    this.subject6 = subject6;
    this.subjectName = subjectName;
    this.subjectName1 = subjectName1;
    this.subjectName2 = subjectName2;
    this.subjectName3 = subjectName3;
    this.subjectName4 = subjectName4;
    this.subjectName5 = subjectName5;
    this.textSwitch = textSwitch;
    this.toolbar = toolbar;
    this.year = year;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityAttendanceFacultyBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityAttendanceFacultyBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_attendance_faculty, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityAttendanceFacultyBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.appbar;
      AppBarLayout appbar = ViewBindings.findChildViewById(rootView, id);
      if (appbar == null) {
        break missingId;
      }

      id = R.id.month;
      TextView month = ViewBindings.findChildViewById(rootView, id);
      if (month == null) {
        break missingId;
      }

      id = R.id.nextMonth;
      ImageView nextMonth = ViewBindings.findChildViewById(rootView, id);
      if (nextMonth == null) {
        break missingId;
      }

      id = R.id.prevMonth;
      ImageView prevMonth = ViewBindings.findChildViewById(rootView, id);
      if (prevMonth == null) {
        break missingId;
      }

      id = R.id.subject1;
      TextView subject1 = ViewBindings.findChildViewById(rootView, id);
      if (subject1 == null) {
        break missingId;
      }

      id = R.id.subject2;
      TextView subject2 = ViewBindings.findChildViewById(rootView, id);
      if (subject2 == null) {
        break missingId;
      }

      id = R.id.subject3;
      TextView subject3 = ViewBindings.findChildViewById(rootView, id);
      if (subject3 == null) {
        break missingId;
      }

      id = R.id.subject4;
      TextView subject4 = ViewBindings.findChildViewById(rootView, id);
      if (subject4 == null) {
        break missingId;
      }

      id = R.id.subject5;
      TextView subject5 = ViewBindings.findChildViewById(rootView, id);
      if (subject5 == null) {
        break missingId;
      }

      id = R.id.subject6;
      TextView subject6 = ViewBindings.findChildViewById(rootView, id);
      if (subject6 == null) {
        break missingId;
      }

      id = R.id.subject_name;
      ImageView subjectName = ViewBindings.findChildViewById(rootView, id);
      if (subjectName == null) {
        break missingId;
      }

      id = R.id.subject_name1;
      ImageView subjectName1 = ViewBindings.findChildViewById(rootView, id);
      if (subjectName1 == null) {
        break missingId;
      }

      id = R.id.subject_name2;
      ImageView subjectName2 = ViewBindings.findChildViewById(rootView, id);
      if (subjectName2 == null) {
        break missingId;
      }

      id = R.id.subject_name3;
      ImageView subjectName3 = ViewBindings.findChildViewById(rootView, id);
      if (subjectName3 == null) {
        break missingId;
      }

      id = R.id.subject_name4;
      ImageView subjectName4 = ViewBindings.findChildViewById(rootView, id);
      if (subjectName4 == null) {
        break missingId;
      }

      id = R.id.subject_name5;
      ImageView subjectName5 = ViewBindings.findChildViewById(rootView, id);
      if (subjectName5 == null) {
        break missingId;
      }

      id = R.id.textSwitch;
      TextSwitcher textSwitch = ViewBindings.findChildViewById(rootView, id);
      if (textSwitch == null) {
        break missingId;
      }

      id = R.id.toolbar;
      Toolbar toolbar = ViewBindings.findChildViewById(rootView, id);
      if (toolbar == null) {
        break missingId;
      }

      id = R.id.year;
      TextView year = ViewBindings.findChildViewById(rootView, id);
      if (year == null) {
        break missingId;
      }

      return new ActivityAttendanceFacultyBinding((LinearLayout) rootView, appbar, month, nextMonth,
          prevMonth, subject1, subject2, subject3, subject4, subject5, subject6, subjectName,
          subjectName1, subjectName2, subjectName3, subjectName4, subjectName5, textSwitch, toolbar,
          year);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
