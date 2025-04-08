public class InputSanitizer {
    public static boolean isNumeric(final String value) {
        return value.matches("\\d+");
    }

    public static boolean isNonNegativeInteger(final String number) {
        if (isNumeric(number) && Integer.parseInt(number) > 0) {
            return true;
        }
        
        System.out.println(TextColour.getErrorMessage("Invalid input: Please provide a valid, non-negative integer."));
        return false;
    }

    public static boolean validTimeWindow(final int arrivalTime, final int endTime, final int timer) {
        if (arrivalTime < timer) {
            System.out.println(TextColour.getErrorMessage("Invalid input: Arrival time must be on or after " + timer + "ms."));
            return false;
        } else if (arrivalTime >= endTime) {
            System.out.println(TextColour.getErrorMessage("Invalid input: End time must be greater than the arrival time."));
            return false;
        } 

        return true;
    }
}
