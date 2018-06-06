package app.com.mobileAssignment.Models;

/**
 * Created by aswinimanasa
 */

public class Model {
    public static enum requestType {
        camera(0),
        gallery(1);
        private int value;

        private requestType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
