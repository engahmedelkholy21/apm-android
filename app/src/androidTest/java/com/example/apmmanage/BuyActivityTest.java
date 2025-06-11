package com.example.apmmanage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BuyActivityTest {
    @Rule
    public ActivityScenarioRule<BuyActivity> rule = new ActivityScenarioRule<>(BuyActivity.class);

    @Test
    public void showErrorToastWhenNoProducts() {
        onView(withId(R.id.saveInvoiceButton)).perform(click());
        onView(withText("لا توجد منتجات"))
                .inRoot(new ToastMatcher())
                .check(matches(withText("لا توجد منتجات")));
    }
}
