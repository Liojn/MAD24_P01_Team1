package sg.edu.np.mad.fitnessultimate.foodtracker;

public class Meal {
    public String name;
    public double calories;
    public double carbohydrates;
    public double proteins;
    public double fats;
    public double others;
    public String mealType;
    public String date;
    public Meal() {
        //Default constructor required for calls to DataSnapshot.getValue(Meal.class)
    }

    public Meal(String name, double calories, double carbohydrates, double proteins, double fats, double others, String mealType, String date) {
        this.name = name;
        this.calories = calories;
        this.carbohydrates = carbohydrates;
        this.proteins = proteins;
        this.fats = fats;
        this.others = others;
        this.mealType = mealType;
        this.date = date;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getCalories() {
        return calories;
    }
    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getCarbs() {
        return carbohydrates;
    }

    public void setCarbs(double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public double getProtein() {
        return proteins;
    }

    public void setProtein(double proteins) {
        this.proteins = proteins;
    }

    public double getFats() {
        return fats;
    }

    public void setFats(double fats) {
        this.fats = fats;
    }

    public double getOthers() {
        return others;
    }

    public void setOthers(double others) {
        this.others = others;
    }
    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
