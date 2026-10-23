package tutorials;

public record Person(String name, String zodiacSign) {
    @Override
    public String toString() {
        return "name: " + name + ", zodiac sign: " + zodiacSign;
    }
}