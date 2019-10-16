package monitor;

/**
 * File: DataFileFormatException.java
 * Date: 28 May 2019
 * @author Logan Hershberger
 * Purpose: Exception used when a discrepancy is found in the data file
 */
public class DataFileFormatException extends Exception{
  static final long serialVersionUID = 1L;
  private String errMessage;

  public DataFileFormatException(int line, int errCount){
    super();

    String err = "";
    if(errCount < 2){
      // specific error message for first 2 errors
      err = "Error in data file found on line " + line + ".\nLine will be ignored.";
    } else if(errCount == 2){
      // Generic error message for third error
      err = "Numerous errors found in data file. Generated data may not reflect "
              + "intended data structure.\nPlease review data file for proper formatting.";
    }

    this.errMessage = err;
  }

  @Override
  public String getMessage(){
    return errMessage;
  }
}
