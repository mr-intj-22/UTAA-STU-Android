package com.msl.utaastu.Food;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 7/12/2017.
 */

@Keep
public class FoodItem {

    String meal="";
    int cal;

    public String getMeal() {
        return meal;
    }

    public FoodItem setMeal(String meal) {
        this.meal = meal;
        return this;
    }

    public int getCal() {
        return cal;
    }

    public FoodItem setCal(int cal) {
        this.cal = cal;
        return this;
    }
}
