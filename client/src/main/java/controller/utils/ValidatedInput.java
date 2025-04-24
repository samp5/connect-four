package controller.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import javafx.scene.control.TextField;

public class ValidatedInput {
  TextField input;
  ArrayList<ValidationMethod> methods;
  boolean valid;
  ValidationMethod reason = ValidationMethod.NONE;

  public static enum ValidationMethod {
    IP_ADDRESS, PORT, LENGTH, NO_SPACES, NOT_EMPTY, NONE;

    private static final Pattern ipPattern = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");

    private boolean validate(String input) {
      switch (this) {
        case IP_ADDRESS:
          return ipPattern.matcher(input).matches();
        case PORT:
          Integer val;
          try {
            val = Integer.valueOf(input);
          } catch (NumberFormatException e) {
            return false;
          }
          return val <= 65535 && val > 0; 
        case LENGTH:
          return input.length() <= 12;
        case NO_SPACES:
          return input.indexOf(' ') == -1;
        case NOT_EMPTY:
          return input.length() != 0;
        case NONE:
          return true;
        default:
          break;
      }
      return false;
    }

    private String string() {
      switch (this) {
		case IP_ADDRESS:
      return "is an invalid IP Address";
		case LENGTH:
      return "is too long";
		case NOT_EMPTY:
      return "cannot be empty";
		case NO_SPACES:
      return "cannot contain spaces";
		case PORT:
      return "is an invalid Port number";
		case NONE:
		default:
			return "[null]";
      }
    }
  }

  public ValidatedInput(TextField input, ValidationMethod ...methods) {
    this.input = input;
    
    this.methods = new ArrayList<>();
    Collections.addAll(this.methods, methods);

    input.textProperty().addListener((o, oldV, newV) -> this.validate());

    this.validate();
  }

  private void validate() {
    String input = this.input.getText();
    boolean valid = true;
    
    for (ValidationMethod method : methods) {
      if (!method.validate(input)) {
        valid = false;
        reason = method;
        break;
      }
    }

    if (!valid) {
      this.input.getStyleClass().add("invalid-text");
    } else {
      this.input.getStyleClass().removeAll("invalid-text");
    }
    this.valid = valid;
  }

  public boolean isValid() {
    return this.valid;
  }

  public String getReason() {
    return this.reason.string();
  }
}
