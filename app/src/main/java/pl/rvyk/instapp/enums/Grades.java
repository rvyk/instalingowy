package pl.rvyk.instapp.enums;

public enum Grades {
    CORRECT("Dobrze"),
    SYNONYM("Synonim"),
    WRONG_CASE("Zła wielkość liter"),
    TYPO("Literówka"),
    WRONG("Niepoprawnie");

    private final String label;

    Grades(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Grades fromGrade(int grade) {
        switch (grade) {
            case 1:
                return CORRECT;
            case 2:
                return SYNONYM;
            case 3:
                return WRONG_CASE;
            case 4:
                return TYPO;
            default:
                return WRONG;
        }
    }
}
