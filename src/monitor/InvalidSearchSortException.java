package monitor;

/**
 * File: InvalidSearchSortException.java
 * Date: 27 May 2019
 * @author Logan Hershberger
 * Purpose: Exception used when invalid search input is detected
 */
public class InvalidSearchSortException extends Exception {
  static final long serialVersionUID = 1L;

  public InvalidSearchSortException(String errorMessage){
    super(errorMessage);
  }
}
