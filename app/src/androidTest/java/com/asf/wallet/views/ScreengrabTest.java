package com.asf.wallet.views;

import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.SplashActivity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import tools.fastlane.screengrab.Screengrab;

import static org.hamcrest.Matchers.is;

.Espresso;
    .ViewInteraction;
    .rule.ActivityTestRule;
    .runner.AndroidJUnit4;
    .Espresso.onView;
    .action.ViewActions.click;
    .action.ViewActions.scrollTo;
    .matcher.ViewMatchers.isDisplayed;
    .matcher.ViewMatchers.withClassName;
    .matcher.ViewMatchers.withContentDescription;
    .matcher.ViewMatchers.withId;
    .matcher.ViewMatchers.withText;

@LargeTest @RunWith(AndroidJUnit4.class) public class ScreengrabTest {

  @Rule public ActivityTestRule<SplashActivity> mActivityTestRule =
      new ActivityTestRule<>(SplashActivity.class);

  private static Matcher<View> childAtPosition(final Matcher<View> parentMatcher,
      final int position) {

    return new TypeSafeMatcher<View>() {
      @Override public void describeTo(Description description) {
        description.appendText("Child at position " + position + " in parent ");
        parentMatcher.describeTo(description);
      }

      @Override public boolean matchesSafely(View view) {
        ViewParent parent = view.getParent();
        return parent instanceof ViewGroup && parentMatcher.matches(parent) && view.equals(
            ((ViewGroup) parent).getChildAt(position));
      }
    };
  }

  @Test public void screengrabTest() {
    Screengrab.screenshot("1");
    ViewInteraction appCompatButton = onView(allOf(withId(R.id.skip), withText("SKIP"),
        childAtPosition(
            allOf(withId(R.id.bottomContainer), childAtPosition(withId(R.id.bottom), 1)), 1),
        isDisplayed()));
    appCompatButton.perform(click());

    ViewInteraction appCompatButton2 = onView(
        allOf(withId(R.id.import_account_action), withText(R.string.already_have_account),
            childAtPosition(childAtPosition(withId(android.R.id.content), 0), 2), isDisplayed()));
    appCompatButton2.perform(click());

    Screengrab.screenshot("2");

    ViewInteraction appCompatImageButton = onView(allOf(withContentDescription("Navigate up"),
        childAtPosition(allOf(withId(R.id.toolbar),
            childAtPosition(withClassName(is("com.google.android.material.appbar.AppBarLayout")), 0)),
            1), isDisplayed()));
    appCompatImageButton.perform(click());

    ViewInteraction appCompatButton3 = onView(
        allOf(withId(R.id.new_account_action), withText(R.string.action_create_new_account),
            childAtPosition(childAtPosition(withId(android.R.id.content), 0), 0), isDisplayed()));
    appCompatButton3.perform(click());

    Espresso.pressBack();
    //ViewInteraction appCompatButton4 = onView(
    //    allOf(withId(R.id.later_button), withText("Do it later"),
    //        childAtPosition(childAtPosition(withId(android.R.id.content), 0), 0), isDisplayed()));
    //appCompatButton4.perform(click());

    ViewInteraction appCompatButton5 = onView(allOf(withId(android.R.id.button1), withText("OK"),
        childAtPosition(childAtPosition(withId(R.id.buttonPanel), 0), 3)));
    appCompatButton5.perform(scrollTo(), click());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource
    // /index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Screengrab.screenshot("3");

    ViewInteraction bottomNavigationItemView = onView(allOf(withId(R.id.bottom_navigation),
        childAtPosition(childAtPosition(withId(R.id.action_send), 0), 0), isDisplayed()));
    bottomNavigationItemView.perform(click());

    Screengrab.screenshot("4");

    ViewInteraction appCompatImageButton2 = onView(allOf(withContentDescription("Navigate up"),
        childAtPosition(
            allOf(withId(R.id.action_bar), childAtPosition(withId(R.id.action_bar_container), 0)),
            1), isDisplayed()));
    appCompatImageButton2.perform(click());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource
    // /index.html
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
