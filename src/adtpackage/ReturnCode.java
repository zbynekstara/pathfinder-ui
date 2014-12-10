package adtpackage;

/**
 *
 * @author Zbynda
 */
public enum ReturnCode {
    SUCCESS("The addition was successful."),
    FAILURE("The addition was unsuccessful."),
        SKIP("The addition was skipped."),
        OVERFLOW("The container overflowed during addition process."),
    UNDEFINED("The addition status is undefined.");

    private String description;

    private ReturnCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
