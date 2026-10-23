package tutorials.parallel;

public class EveningPlan {
    private String movie;
    private String food;

    public EveningPlan(String movie, String food) {
        this.movie = movie;
        this.food = food;
    }

    @Override
    public String toString() {
        return "movie: " + movie + ", food: " + food;
    }
}
